package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.CatalogMedicationDto;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;

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