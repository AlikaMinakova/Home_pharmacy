

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
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    //    private final MedicationRepository medicationRepository;
    private final DiseaseRepository diseaseRepository;


    @Transactional(readOnly = true)
    public PharmacyDto getById(Long id) {
        return pharmacyRepository.findById(id)
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

        Page<PharmacyDto> all;
        if (keyword != null && !keyword.isBlank()) {
            all = pharmacyRepository.findByMedicationNameContainingIgnoreCase(keyword, pageable)
                    .map(this::toResponse);
        } else {
            all = pharmacyRepository.findAll(pageable)
                    .map(this::toResponse);
        }

        LocalDate today = LocalDate.now();

        // Купили на этой неделе
        LocalDate weekAgo = today.minusDays(7);
        List<PharmacyDto> recentlyBought = pharmacyRepository.findBoughtAfter(weekAgo)
                .stream()
                .map(this::toResponse)
                .limit(4)
                .toList();

        List<PharmacyDto> expiringSoon = pharmacyRepository.findExpiringSoon(LocalDate.now())
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
        pharmacy.setPurchaseDate(request.getPurchaseDate());
        pharmacy.setExpirationDate(request.getExpirationDate());
    }

    public Disease findDiseaseById(Long diseaseId) {
        return diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new RuntimeException("Disease not found: " + diseaseId));
    }

    @Transactional
    public void deleteAllExpired() {
        pharmacyRepository.deleteAllExpired(LocalDate.now());
    }


    private PharmacyDto toResponse(Pharmacy pharmacy) {
        return PharmacyDto.builder()
                .id(pharmacy.getId())
                .medication(MedicationDto
                        .builder()
                        .id(pharmacy.getMedication().getId())
                        .name(pharmacy.getMedication().getName())
                        .description(pharmacy.getMedication().getDescription())
                        .image(pharmacy.getMedication().getImage())
                        .diseases(pharmacy.getMedication().getDiseases())
                        .build())
                .quantity(pharmacy.getQuantity())
                .expirationDate(pharmacy.getExpirationDate())
                .purchaseDate(pharmacy.getPurchaseDate())
                .build();
    }

    public void delete(Long id) {
        pharmacyRepository.deleteById(id);
    }
}
