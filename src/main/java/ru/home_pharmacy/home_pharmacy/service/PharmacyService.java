

package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import ru.home_pharmacy.home_pharmacy.dto.StockDto;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        Pageable pageable = PageRequest.of(page, size);
        LocalDate today = LocalDate.now();

        // Сводные остатки: одна карточка на лекарство с суммарным количеством.
        // Партии с нулевым остатком тоже учитываются (лекарство остаётся, но «нет в наличии»).
        List<Pharmacy> batches = pharmacyRepository.findAllOnStockList(today);
        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.trim().toLowerCase();
            batches = batches.stream()
                    .filter(b -> b.getMedication().getName() != null
                            && b.getMedication().getName().toLowerCase().contains(lower))
                    .toList();
        }

        // Сортируем партии по дате покупки (новые сверху), чтобы batchId указывал на самую свежую
        batches = new ArrayList<>(batches);
        batches.sort(Comparator.comparing(Pharmacy::getPurchaseDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        Map<Long, StockDto> grouped = new LinkedHashMap<>();
        for (Pharmacy batch : batches) {
            Medication medication = batch.getMedication();
            StockDto dto = grouped.computeIfAbsent(medication.getId(), id -> StockDto.builder()
                    .medicationId(id)
                    .batchId(batch.getId())
                    .name(medication.getName())
                    .image(medication.getImage())
                    .price(medication.getPrice() != null ? medication.getPrice() : BigDecimal.ZERO)
                    .totalQuantity(0L)
                    .build());
            dto.setTotalQuantity(dto.getTotalQuantity()
                    + (batch.getQuantity() != null ? batch.getQuantity() : 0));
        }

        List<StockDto> stockList = new ArrayList<>(grouped.values());
        String sortField = sort == null ? "name" : sort;
        Comparator<StockDto> comparator = "quantity".equals(sortField)
                ? Comparator.comparing(StockDto::getTotalQuantity)
                : Comparator.comparing(StockDto::getName, Comparator.nullsLast(Comparator.naturalOrder()));
        stockList.sort(comparator);

        int total = stockList.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        Page<StockDto> all = new PageImpl<>(stockList.subList(from, to), pageable, total);

        List<PharmacyDto> expiringSoon = pharmacyRepository.findExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(7))
                .stream()
                .map(this::toResponse)
                .limit(4)
                .toList();

        return PharmacyOverviewResponse.builder()
                .all(all)
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

    @Transactional(readOnly = true)
    public long countExpired() {
        return pharmacyRepository.findAllExpired(LocalDate.now()).size();
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
     * Единая история поступлений: активные партии («на складе») + списанные
     * и удалённые (архив). Отсортированы по дате по убыванию.
     */
    @Transactional(readOnly = true)
    public List<PharmacyHistoryItem> getHistory(String name, String status, String sort, String dir) {
        List<PharmacyHistoryItem> items = new ArrayList<>();
        for (Pharmacy p : pharmacyRepository.findAll()) {
            items.add(toHistoryItem(p, "ACTIVE", p.getPurchaseDate()));
        }
        for (Archive a : archiveRepository.findAll()) {
            String s = "WRITTEN_OFF".equals(a.getReason()) ? "WRITTEN_OFF" : "DELETED";
            items.add(toHistoryItem(a, s, a.getWrittenOffAt().toLocalDate()));
        }
        if (name != null && !name.isBlank()) {
            String lower = name.trim().toLowerCase();
            items = items.stream()
                    .filter(i -> i.getMedicationName() != null
                            && i.getMedicationName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isBlank()) {
            items = items.stream()
                    .filter(i -> status.equals(i.getStatus()))
                    .collect(Collectors.toList());
        }
        Comparator<PharmacyHistoryItem> comparator = switch (sort == null ? "" : sort) {
            case "name" -> Comparator.comparing(PharmacyHistoryItem::getMedicationName,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "expirationDate" -> Comparator.comparing(PharmacyHistoryItem::getExpirationDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(PharmacyHistoryItem::getPurchaseDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("asc".equals(dir)) {
            items.sort(comparator);
        } else {
            items.sort(comparator.reversed());
        }
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
