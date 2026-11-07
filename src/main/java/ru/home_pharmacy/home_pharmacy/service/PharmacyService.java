

package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyOverviewResponse;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyDto;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyHistoryItem;
import ru.home_pharmacy.home_pharmacy.dto.ArchiveDto;
import ru.home_pharmacy.home_pharmacy.entity.CustomerOrder;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.OrderStatus;
import ru.home_pharmacy.home_pharmacy.entity.Archive;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;
import ru.home_pharmacy.home_pharmacy.repository.CustomerOrderRepository;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.ArchiveRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    //    private final MedicationRepository medicationRepository;
    private final DiseaseRepository diseaseRepository;
    private final CustomerOrderRepository orderRepository;
    private final ArchiveRepository archiveRepository;


    @Transactional(readOnly = true)
    public PharmacyDto getById(Long id) {
        return pharmacyRepository.findWithMedicationById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
    }

    @Transactional(readOnly = true)
    public Page<PharmacyDto> getAllMedications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").descending());
        return pharmacyRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PharmacyOverviewResponse getPharmacyOverview(int page, int size, String keyword, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        LocalDate today = LocalDate.now();

        // Только актуальные партии: есть остаток и срок не истёк.
        // Каждая поставка — отдельный товар со своей ценой, датой и количеством.
        Page<PharmacyDto> all;
        if (keyword != null && !keyword.isBlank()) {
            all = pharmacyRepository.findAvailableByMedicationNameContaining(keyword, today, pageable)
                    .map(this::toResponse);
        } else {
            all = pharmacyRepository.findAllAvailable(today, pageable)
                    .map(this::toResponse);
        }

        // Недавно подтверждённые заказы покупателей (последние 4)
        List<CustomerOrder> recentlyBought = orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.CONFIRMED)
                .stream()
                .limit(4)
                .toList();

        List<PharmacyDto> expiringSoon = pharmacyRepository.findExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(7))
                .stream()
                .map(this::toResponse)
                .limit(4)
                .toList();

        return PharmacyOverviewResponse.builder()
                .all(all)
                .recentlyBought(recentlyBought)
                .expiringSoon(expiringSoon)
                .build();
    }

    @Transactional
    public Page<PharmacyDto> getExpiredPharmacies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expirationDate").ascending());
        return pharmacyRepository.findExpired(LocalDate.now(), pageable)
                .map(this::toResponse); // Page<T>.map() позволяет преобразовать сущности в DTO
    }

    @Transactional(readOnly = true)
    public Page<PharmacyDto> getExpiringSoonPharmacies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expirationDate").ascending());
        return pharmacyRepository.findExpiringSoonPage(LocalDate.now(), LocalDate.now().plusDays(7), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ArchiveDto> getArchive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("writtenOffAt").descending());
        return archiveRepository.findAllByOrderByWrittenOffAtDesc(pageable)
                .map(this::toArchiveResponse);
    }

    @Transactional
    public PharmacyDto create(PharmacyDto request) {
        Pharmacy pharmacy = new Pharmacy();
        fillPharmacyFromRequest(pharmacy, request);
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return toResponse(saved);
    }

    @Transactional
    public PharmacyDto update(Long id, PharmacyDto request) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with id: " + id));

        fillPharmacyFromRequest(pharmacy, request);

        Pharmacy updated = pharmacyRepository.save(pharmacy);
        return toResponse(updated);
    }


    private void fillPharmacyFromRequest(Pharmacy pharmacy, PharmacyDto request) {
        pharmacy.setMedication(Medication
                .builder()
                .id(request.getMedication().getId())
                .name(request.getMedication().getName())
                .description(request.getMedication().getDescription())
                .image(request.getMedication().getImage())
                .diseases(request.getMedication().getDiseases())
                .build());
        pharmacy.setQuantity(request.getQuantity());
        pharmacy.setRetailPrice(request.getRetailPrice());
        pharmacy.setPurchaseDate(request.getPurchaseDate());
        pharmacy.setExpirationDate(request.getExpirationDate());
    }

    public Disease findDiseaseById(Long diseaseId) {
        return diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new RuntimeException("Disease not found: " + diseaseId));
    }

    @Transactional
    public void writeOffAllExpired() {
        LocalDate today = LocalDate.now();
        List<Pharmacy> expired = pharmacyRepository.findAllExpired(today);

        List<Archive> archiveEntries = new ArrayList<>();
        for (Pharmacy batch : expired) {
            archiveEntries.add(Archive.builder()
                    .medication(batch.getMedication())
                    .quantity(batch.getQuantity())
                    .retailPrice(batch.getRetailPrice())
                    .expirationDate(batch.getExpirationDate())
                    .purchaseDate(batch.getPurchaseDate())
                    .writtenOffAt(LocalDateTime.now())
                    .reason("WRITTEN_OFF")
                    .build());
        }
        archiveRepository.saveAll(archiveEntries);
        pharmacyRepository.deleteAll(expired);
    }

    private ArchiveDto toArchiveResponse(Archive archive) {
        return ArchiveDto.builder()
                .id(archive.getId())
                .medicationName(archive.getMedication().getName())
                .image(archive.getMedication().getImage())
                .quantity(archive.getQuantity())
                .retailPrice(archive.getRetailPrice())
                .expirationDate(archive.getExpirationDate())
                .purchaseDate(archive.getPurchaseDate())
                .writtenOffAt(archive.getWrittenOffAt())
                .reason(archive.getReason())
                .build();
    }

    /**
     * Единая история поступлений: активные партии («в аптечке») + списанные
     * и удалённые (архив). Отсортированы по дате по убыванию.
     */
    @Transactional(readOnly = true)
    public List<PharmacyHistoryItem> getHistory() {
        List<PharmacyHistoryItem> items = new ArrayList<>();
        for (Pharmacy p : pharmacyRepository.findAll()) {
            items.add(toHistoryItem(p, "ACTIVE", p.getPurchaseDate()));
        }
        for (Archive a : archiveRepository.findAll()) {
            String status = "WRITTEN_OFF".equals(a.getReason()) ? "WRITTEN_OFF" : "DELETED";
            items.add(toHistoryItem(a, status, a.getWrittenOffAt().toLocalDate()));
        }
        items.sort(Comparator.comparing(PharmacyHistoryItem::getSortDate,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return items;
    }

    private PharmacyHistoryItem toHistoryItem(Pharmacy p, String status, LocalDate sortDate) {
        return PharmacyHistoryItem.builder()
                .id(p.getId())
                .medicationName(p.getMedication().getName())
                .image(p.getMedication().getImage())
                .quantity(p.getQuantity())
                .retailPrice(p.getRetailPrice())
                .expirationDate(p.getExpirationDate())
                .purchaseDate(p.getPurchaseDate())
                .status(status)
                .sortDate(sortDate)
                .build();
    }

    private PharmacyHistoryItem toHistoryItem(Archive a, String status, LocalDate sortDate) {
        return PharmacyHistoryItem.builder()
                .id(a.getId())
                .medicationName(a.getMedication().getName())
                .image(a.getMedication().getImage())
                .quantity(a.getQuantity())
                .retailPrice(a.getRetailPrice())
                .expirationDate(a.getExpirationDate())
                .purchaseDate(a.getPurchaseDate())
                .status(status)
                .sortDate(sortDate)
                .build();
    }


    private PharmacyDto toResponse(Pharmacy pharmacy) {
        return PharmacyDto.builder()
                .id(pharmacy.getId())
                .medication(MedicationDto.builder()
                        .id(pharmacy.getMedication().getId())
                        .name(pharmacy.getMedication().getName())
                        .description(pharmacy.getMedication().getDescription())
                        .image(pharmacy.getMedication().getImage())
                        .price(pharmacy.getMedication().getPrice() != null
                                ? pharmacy.getMedication().getPrice() : BigDecimal.ZERO)
                        .diseases(pharmacy.getMedication().getDiseases())
                        .activeIngredients(pharmacy.getMedication().getActiveIngredients())
                        .build())
                .quantity(pharmacy.getQuantity())
                .retailPrice(pharmacy.getRetailPrice())
                .expirationDate(pharmacy.getExpirationDate())
                .purchaseDate(pharmacy.getPurchaseDate())
                .build();
    }

    public void delete(Long id) {
        Pharmacy batch = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Поступление не найдено: " + id));
        // Не удаляем безвозвратно, а переносим в архив — сохраняем историю.
        archiveRepository.save(Archive.builder()
                .medication(batch.getMedication())
                .quantity(batch.getQuantity())
                .retailPrice(batch.getRetailPrice())
                .expirationDate(batch.getExpirationDate())
                .purchaseDate(batch.getPurchaseDate())
                .writtenOffAt(LocalDateTime.now())
                .reason("DELETED")
                .build());
        pharmacyRepository.delete(batch);
    }
}
