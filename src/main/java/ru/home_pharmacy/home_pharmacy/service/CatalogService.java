package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.CatalogMedicationDto;
import ru.home_pharmacy.home_pharmacy.dto.PricePointDto;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;
import ru.home_pharmacy.home_pharmacy.repository.PriceHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public Page<CatalogMedicationDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return medicationRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<CatalogMedicationDto> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return medicationRepository.findByNameContaining(name, pageable).map(this::toResponse);
    }

    public Page<CatalogMedicationDto> findByDisease(Long diseaseId, int page, int size) {
        return medicationRepository.findPageByDiseaseId(diseaseId, PageRequest.of(page, size)).map(this::toResponse);
    }

    public Page<CatalogMedicationDto> findByIngredient(Long ingredientId, int page, int size) {
        return medicationRepository.findByActiveIngredientId(ingredientId, PageRequest.of(page, size)).map(this::toResponse);
    }

    public Page<CatalogMedicationDto> findBySymptom(Long symptomId, int page, int size) {
        return medicationRepository.findBySymptomId(symptomId, PageRequest.of(page, size)).map(this::toResponse);
    }

    public CatalogMedicationDto getById(Long id) {
        return medicationRepository.findById(id).map(this::toResponse).orElse(null);
    }

    public Long getAvailableQuantity(Long medicationId) {
        return pharmacyRepository.sumAvailableQuantity(medicationId, LocalDate.now());
    }

    /** История изменения цены лекарства для графика в карточке товара. */
    public List<PricePointDto> getPriceHistory(Long medicationId) {
        List<PricePointDto> points = new ArrayList<>(priceHistoryRepository
                .findAllByMedicationIdOrderByChangedAtAsc(medicationId)
                .stream()
                .map(h -> PricePointDto.builder()
                        .date(h.getChangedAt())
                        .price(h.getPrice())
                        .label(h.getChangedAt().getDayOfMonth() + "." + h.getChangedAt().getMonthValue()
                                + "." + h.getChangedAt().getYear())
                        .build())
                .toList());

        // гарантируем, что график всегда заканчивается текущей ценой лекарства
        Medication medication = medicationRepository.findById(medicationId).orElse(null);
        if (medication != null && medication.getPrice() != null) {
            boolean lastMatches = !points.isEmpty()
                    && points.get(points.size() - 1).getPrice().compareTo(medication.getPrice()) == 0;
            if (!lastMatches) {
                LocalDate today = LocalDate.now();
                points.add(PricePointDto.builder()
                        .date(today)
                        .price(medication.getPrice())
                        .label(today.getDayOfMonth() + "." + today.getMonthValue() + "." + today.getYear())
                        .build());
            }
        }
        return points;
    }

    private CatalogMedicationDto toResponse(Medication medication) {
        return CatalogMedicationDto.builder()
                .id(medication.getId())
                .name(medication.getName())
                .description(medication.getDescription())
                .image(medication.getImage())
                .price(medication.getPrice() != null ? medication.getPrice() : BigDecimal.ZERO)
                .availableQuantity(pharmacyRepository.sumAvailableQuantity(medication.getId(), LocalDate.now()))
                .prescriptionRequired(Boolean.TRUE.equals(medication.getPrescriptionRequired()))
                .build();
    }
}