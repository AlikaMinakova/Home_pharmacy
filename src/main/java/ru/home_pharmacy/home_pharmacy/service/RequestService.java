package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.RequestDto;
import ru.home_pharmacy.home_pharmacy.entity.CustomerRequest;
import ru.home_pharmacy.home_pharmacy.entity.RequestMessage;
import ru.home_pharmacy.home_pharmacy.entity.RequestStatus;
import ru.home_pharmacy.home_pharmacy.repository.CustomerRequestRepository;
import ru.home_pharmacy.home_pharmacy.repository.RequestMessageRepository;
import ru.home_pharmacy.home_pharmacy.util.PhoneUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestService {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "status", "status",
            "desiredQuantity", "desiredQuantity",
            "customerName", "customerName",
            "customerPhone", "customerPhone",
            "medicationName", "medicationName"
    );

    private final CustomerRequestRepository requestRepository;
    private final RequestMessageRepository messageRepository;

    public CustomerRequest createRequest(RequestDto dto) {
        CustomerRequest request = CustomerRequest.builder()
                .medicationName(dto.getMedicationName().trim())
                .desiredQuantity(dto.getDesiredQuantity())
                .comment(dto.getComment() != null ? dto.getComment().trim() : null)
                .customerName(dto.getCustomerName().trim())
                .customerPhone(PhoneUtils.normalize(dto.getCustomerPhone()))
                .status(RequestStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();
        request = requestRepository.save(request);

        // Начальный комментарий покупателя становится первым сообщением чата
        if (dto.getComment() != null && !dto.getComment().isBlank()) {
            saveMessage(request, "CUSTOMER", dto.getComment().trim());
        }
        return request;
    }

    @Transactional(readOnly = true)
    public List<CustomerRequest> getRequestsByPhone(String customerPhone) {
        return requestRepository.findAllByCustomerPhoneOrderByCreatedAtDesc(customerPhone);
    }

    @Transactional(readOnly = true)
    public List<CustomerRequest> getAllRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<CustomerRequest> search(String search, RequestStatus status, String sortBy, String sortDir) {
        Specification<CustomerRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("medicationName")), like),
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("customerPhone")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return requestRepository.findAll(spec, buildSort(sortBy, sortDir));
    }

    @Transactional(readOnly = true)
    public long countNew() {
        return requestRepository.countByStatus(RequestStatus.NEW);
    }

    public void approve(Long id) {
        changeStatus(id, RequestStatus.APPROVED);
    }

    public void reject(Long id) {
        changeStatus(id, RequestStatus.REJECTED);
    }

    private void changeStatus(Long id, RequestStatus status) {
        CustomerRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена: " + id));
        request.setStatus(status);
        requestRepository.save(request);
    }

    /**
     * История переписки по заявке. Покупатель видит только свои заявки.
     */
    @Transactional(readOnly = true)
    public List<RequestMessage> getMessages(Long id, String username, boolean isEmployee) {
        CustomerRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена: " + id));
        if (!isEmployee && !request.getCustomerPhone().equals(username)) {
            throw new RuntimeException("Заявка не принадлежит текущему покупателю");
        }
        return messageRepository.findAllByRequestIdOrderByCreatedAtAsc(id);
    }

    /**
     * Отправка сообщения в чат заявки. Аптекарь = PHARMACIST, покупатель = CUSTOMER.
     * Сообщение аптекаря помечается непрочитанным для покупателя.
     */
    public void sendMessage(Long id, String text, String username, boolean isEmployee) {
        CustomerRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена: " + id));
        if (!isEmployee && !request.getCustomerPhone().equals(username)) {
            throw new RuntimeException("Заявка не принадлежит текущему покупателю");
        }
        if (text == null || text.isBlank()) {
            return;
        }
        String sender = isEmployee ? "PHARMACIST" : "CUSTOMER";
        saveMessage(request, sender, text.trim());
        if (isEmployee) {
            request.setCommentRead(false);
        } else {
            request.setPharmacistRead(false);
        }
        requestRepository.save(request);
    }

    /** Покупатель отметил заявку прочитанной (открыл чат). */
    public void markRead(Long id, String username) {
        CustomerRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена: " + id));
        if (request.getCustomerPhone().equals(username) && Boolean.FALSE.equals(request.getCommentRead())) {
            request.setCommentRead(true);
            requestRepository.save(request);
        }
    }

    /** Аптекарь отметил заявку прочитанной (открыл чат). */
    public void markPharmacistRead(Long id) {
        CustomerRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена: " + id));
        if (Boolean.FALSE.equals(request.getPharmacistRead())) {
            request.setPharmacistRead(true);
            requestRepository.save(request);
        }
    }

    @Transactional(readOnly = true)
    public long countPharmacistUnread() {
        return requestRepository.countByPharmacistReadFalse();
    }

    /** Покупатель подтверждает заявку (например, готов предъявить рецепт). */
    public void buyerConfirm(Long id, String customerPhone) {
        CustomerRequest request = findOwn(id, customerPhone);
        if (request.getStatus() == RequestStatus.NEW) {
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCommentRead(true);
            requestRepository.save(request);
        }
    }

    /** Покупатель отменяет свою заявку. */
    public void buyerCancel(Long id, String customerPhone) {
        CustomerRequest request = findOwn(id, customerPhone);
        if (request.getStatus() == RequestStatus.NEW) {
            request.setStatus(RequestStatus.CANCELLED);
            request.setCommentRead(true);
            requestRepository.save(request);
        }
    }

    @Transactional(readOnly = true)
    public long countUnread(String customerPhone) {
        return requestRepository.countByCustomerPhoneAndCommentReadFalse(customerPhone);
    }

    private void saveMessage(CustomerRequest request, String sender, String text) {
        messageRepository.save(RequestMessage.builder()
                .request(request)
                .sender(sender)
                .text(text)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private CustomerRequest findOwn(Long id, String customerPhone) {
        CustomerRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена: " + id));
        if (!request.getCustomerPhone().equals(customerPhone)) {
            throw new RuntimeException("Заявка не принадлежит текущему покупателю");
        }
        return request;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String field = SORT_FIELDS.getOrDefault(sortBy, "createdAt");
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}