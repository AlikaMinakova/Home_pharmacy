

package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.home_pharmacy.home_pharmacy.dto.*;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;
import ru.home_pharmacy.home_pharmacy.entity.Symptom;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final MedicationRepository medicationRepository;
    private final DiseaseRepository diseaseRepository;


    @Transactional(readOnly = true)
    public PharmacyResponse getById(Long id) {
        return pharmacyRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
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

    @Transactional
    public PharmacyResponse create(PharmacyRequest request) {
        // создаём новое лекарство
        Medication medication = new Medication();
        fillMedicationFromRequest(medication, request);

        medication = medicationRepository.save(medication);

        Pharmacy pharmacy = new Pharmacy();
        fillPharmacyFromRequest(pharmacy, medication, request);

        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return toResponse(saved);
    }

    @Transactional
    public PharmacyResponse update(Long id, PharmacyRequest request) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with id: " + id));

        Medication medication = pharmacy.getMedication();
        fillMedicationFromRequest(medication, request);

        medicationRepository.save(medication);

        fillPharmacyFromRequest(pharmacy, medication, request);

        Pharmacy updated = pharmacyRepository.save(pharmacy);
        return toResponse(updated);
    }

    private void fillMedicationFromRequest(Medication medication, PharmacyRequest request) {
        medication.setName(request.getMedication().getName());
        medication.setDescription(request.getMedicationDescription());

        // загружаем картинку (если есть)
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            medication.setImage(uploadImage(request.getImage()));
        }

        // находим болезни по id
        Set<Disease> diseases = new HashSet<>(diseaseRepository.findAllById(
                request.getMedication()
                        .getDiseases()
                        .stream()
                        .map(Disease::getId)
                        .collect(Collectors.toSet())
        ));
        medication.setDiseases(diseases);
    }

    private void fillPharmacyFromRequest(Pharmacy pharmacy, Medication medication, PharmacyRequest request) {
        pharmacy.setMedication(medication);
        pharmacy.setQuantity(request.getQuantity());
        pharmacy.setPurchaseDate(request.getPurchaseDate());
        pharmacy.setExpirationDate(request.getExpirationDate());
    }

    private String uploadImage(MultipartFile image) {
        try {
            String uploadDir = new File("uploads").getAbsolutePath();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                    + "_" + Objects.requireNonNull(image.getOriginalFilename());

            File uploadFile = new File(dir, filename);
            image.transferTo(uploadFile);

            return "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }
    }


    @Transactional
    public void deleteMedication(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new RuntimeException("Medication not found with id = " + id);
        }
        System.out.println(id);
        medicationRepository.deleteById(id);
    }

    // 🔹 Конвертер в DTO
    private PharmacyResponse toResponse(Pharmacy pharmacy) {
        return PharmacyResponse.builder()
                .id(pharmacy.getId())
                .medicationId(pharmacy.getMedication().getId())
                .medication(pharmacy.getMedication())
                .medicationDescription(pharmacy.getMedication().getDescription())
                .diseaseNames(pharmacy.getMedication().getDiseases()
                        .stream()
                        .map(Disease::getName)
                        .collect(Collectors.toList()))
                .quantity(pharmacy.getQuantity())
                .expirationDate(pharmacy.getExpirationDate())
                .purchaseDate(pharmacy.getPurchaseDate())
                .image(pharmacy.getMedication().getImage())
                .build();
    }
}
