package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.OrderDto;
import ru.home_pharmacy.home_pharmacy.entity.CustomerOrder;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.OrderStatus;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;
import ru.home_pharmacy.home_pharmacy.repository.CustomerOrderRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;
import ru.home_pharmacy.home_pharmacy.util.PhoneUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "status", "status",
            "quantity", "quantity",
            "customerName", "customerName",
            "customerPhone", "customerPhone",
            "medication", "medication.name"
    );

    private final CustomerOrderRepository orderRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;

    public CustomerOrder createOrder(OrderDto dto) {
        Medication medication = medicationRepository.findById(dto.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Лекарство не найдено: " + dto.getMedicationId()));

        CustomerOrder order = CustomerOrder.builder()
                .medication(medication)
                .quantity(dto.getQuantity())
                .customerName(dto.getCustomerName().trim())
                .customerPhone(PhoneUtils.normalize(dto.getCustomerPhone()))
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersByPhone(String customerPhone) {
        return orderRepository.findAllByCustomerPhoneOrderByCreatedAtDesc(customerPhone);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> search(String search, OrderStatus status, String sortBy, String sortDir) {
        Specification<CustomerOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("customerPhone")), like),
                        cb.like(cb.lower(root.join("medication").get("name")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return orderRepository.findAll(spec, buildSort(sortBy, sortDir));
    }

    @Transactional(readOnly = true)
    public long countNew() {
        return orderRepository.countByStatus(OrderStatus.NEW);
    }

    public void confirm(Long id) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: " + id));

        if (order.getStatus() != OrderStatus.NEW) {
            return; // заказ уже обработан — не списываем повторно
        }

        // списываем остаток с партий по FIFO (сначала партии с ближайшим сроком годности)
        int remaining = order.getQuantity();
        List<Pharmacy> batches = pharmacyRepository.findAvailableBatches(order.getMedication().getId(), LocalDate.now());
        for (Pharmacy batch : batches) {
            if (remaining <= 0) {
                break;
            }
            int subtract = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - subtract);
            remaining -= subtract;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    public void reject(Long id) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: " + id));
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
    }

    public void purchase(Long id) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: " + id));
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.PURCHASED);
            orderRepository.save(order);
        }
    }

    /**
     * «Отменить» после подтверждения: заказ не выкуплен, поэтому возвращаем
     * забронированный остаток на склад (в партии по FIFO) и отклоняем заказ.
     */
    public void cancel(Long id) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: " + id));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            return;
        }

        int remaining = order.getQuantity();
        List<Pharmacy> batches = pharmacyRepository.findAvailableBatches(
                order.getMedication().getId(), LocalDate.now());
        if (batches.isEmpty()) {
            // на складе не осталось активных партий — создаём новую
            pharmacyRepository.save(Pharmacy.builder()
                    .medication(order.getMedication())
                    .quantity(remaining)
                    .retailPrice(order.getMedication().getPrice())
                    .purchaseDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusYears(1))
                    .build());
        } else {
            Pharmacy first = batches.get(0);
            first.setQuantity(first.getQuantity() + remaining);
        }

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String field = SORT_FIELDS.getOrDefault(sortBy, "createdAt");
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}