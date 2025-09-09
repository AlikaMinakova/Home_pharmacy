

package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        // 1. Сохраняем Medication
        Medication medication = new Medication();
        medication.setName(request.getMedication().getName());
        medication.setDescription(request.getMedicationDescription());
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String uploadDir = new File("uploads").getAbsolutePath();
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String filename = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                        + "_" + Objects.requireNonNull(request.getImage().getOriginalFilename());
                File uploadFile = new File(dir, filename);
                request.getImage().transferTo(uploadFile);
                medication.setImage("/" + filename);

            } catch (IOException e) {
                throw new RuntimeException("Ошибка при сохранении файла", e);
            }
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

        medication = medicationRepository.save(medication);

        // 2. Сохраняем Pharmacy (кол-во и даты)
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setMedication(medication);
        pharmacy.setQuantity(request.getQuantity());
        pharmacy.setPurchaseDate(request.getPurchaseDate());
        pharmacy.setExpirationDate(request.getExpirationDate());

        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return toResponse(saved);
    }

    @Transactional
    public PharmacyResponse update(Long id, PharmacyRequest request) {
        // 1. Находим запись Pharmacy
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with id: " + id));

        Medication medication = pharmacy.getMedication();

        // 2. Обновляем данные лекарства
        medication.setName(request.getMedication().getName());
        medication.setDescription(request.getMedicationDescription());

        // 3. Загружаем новое фото (если есть)
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String uploadDir = new File("uploads").getAbsolutePath();
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String filename = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                        + "_" + Objects.requireNonNull(request.getImage().getOriginalFilename());
                File uploadFile = new File(dir, filename);
                request.getImage().transferTo(uploadFile);
                medication.setImage("/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при сохранении файла", e);
            }
        }

        // 4. Обновляем болезни
        Set<Disease> diseases = new HashSet<>(diseaseRepository.findAllById(
                request.getMedication()
                        .getDiseases()
                        .stream()
                        .map(Disease::getId)
                        .collect(Collectors.toSet())
        ));
        medication.setDiseases(diseases);

        // 5. Обновляем количество и даты
        pharmacy.setQuantity(request.getQuantity());
        pharmacy.setPurchaseDate(request.getPurchaseDate());
        pharmacy.setExpirationDate(request.getExpirationDate());

        // 6. Сохраняем изменения
        medicationRepository.save(medication);
        Pharmacy updated = pharmacyRepository.save(pharmacy);

        return toResponse(updated);
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
