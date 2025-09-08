

package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseResponse;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyOverviewResponse;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyRequest;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyResponse;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final MedicationRepository medicationRepository;
    private final DiseaseRepository diseaseRepository;


    public PharmacyResponse create(PharmacyRequest request) {
        Medication medication = buildMedicationFromRequest(request, null);
        medication = medicationRepository.save(medication);

        Pharmacy pharmacy = Pharmacy.builder()
                .medication(medication)
                .quantity(request.getQuantity())
                .expirationDate(request.getExpirationDate())
                .purchaseDate(request.getPurchaseDate())
                .build();

        return toResponse(pharmacyRepository.save(pharmacy));
    }


    public PharmacyResponse update(Long id, PharmacyRequest request) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        Medication medication = buildMedicationFromRequest(request, pharmacy.getMedication());
        medication = medicationRepository.save(medication);

        pharmacy.setMedication(medication);
        pharmacy.setQuantity(request.getQuantity());
        pharmacy.setExpirationDate(request.getExpirationDate());
        pharmacy.setPurchaseDate(request.getPurchaseDate());

        return toResponse(pharmacyRepository.save(pharmacy));
    }


    public void delete(Long id) {
        pharmacyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PharmacyResponse getById(Long id) {
        return pharmacyRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
    }


    @Transactional(readOnly = true)
    public List<PharmacyResponse> getAll() {
        return pharmacyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<PharmacyResponse> getAllMedications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseDate").ascending());
        return pharmacyRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PharmacyOverviewResponse getPharmacyOverview(int page, int size) {
        LocalDate today = LocalDate.now();

        // 1. Весь список
        Page<PharmacyResponse> all = getAllMedications(page, size);

        // 2. Купили на этой неделе
        LocalDate weekAgo = today.minusDays(7);
        List<PharmacyResponse> recentlyBought = pharmacyRepository.findBoughtAfter(weekAgo)
                .stream()
                .map(this::toResponse)
                .limit(4)
                .toList();

        // 3. Истекает срок годности (до 7 дней)
        LocalDate nextWeek = today.plusDays(7);
        List<PharmacyResponse> expiringSoon = pharmacyRepository.findExpiringSoon(nextWeek)
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

    // 🔹 Вспомогательный метод для Medication
    private Medication buildMedicationFromRequest(PharmacyRequest request, Medication existing) {
        Medication medication = existing != null ? existing : new Medication();

        medication.setName(request.getMedicationName());
        medication.setDescription(request.getMedicationDescription());

        // картинка
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                medication.setImage(request.getImage().getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Ошибка загрузки изображения", e);
            }
        }

        // болезни
        if (request.getDiseaseIds() != null && !request.getDiseaseIds().isEmpty()) {
            var diseases = new HashSet<Disease>(diseaseRepository.findAllById(request.getDiseaseIds()));
            medication.setDiseases(diseases);
        }

        return medication;
    }

    // 🔹 Конвертер в DTO
    private PharmacyResponse toResponse(Pharmacy pharmacy) {
        return PharmacyResponse.builder()
                .id(pharmacy.getId())
                .medicationId(pharmacy.getMedication().getId())
                .medicationName(pharmacy.getMedication().getName())
                .medicationDescription(pharmacy.getMedication().getDescription())
                .diseaseNames(pharmacy.getMedication().getDiseases()
                        .stream()
                        .map(Disease::getName)
                        .collect(Collectors.toList()))
                .quantity(pharmacy.getQuantity())
                .expirationDate(pharmacy.getExpirationDate())
                .purchaseDate(pharmacy.getPurchaseDate())
                .build();
    }
}
