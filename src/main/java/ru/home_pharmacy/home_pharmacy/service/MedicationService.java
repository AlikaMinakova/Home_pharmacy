package ru.home_pharmacy.home_pharmacy.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class MedicationService {
    private final MedicationRepository medicationRepository;
    private final DiseaseRepository diseaseRepository;

    public List<MedicationDto> getAll() {
        return medicationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void create(MedicationDto request, MultipartFile file) {
        Medication medication = new Medication();
        fillMedicationFromRequest(request, medication);
        if (file != null && !file.isEmpty()) {
            medication.setImage(uploadImage(file));
        } else {
            medication.setImage("/no-photo.jpg");
        }
        medicationRepository.save(medication);
    }

    @Transactional
    public void update(Long id, MedicationDto medicationDto, MultipartFile file) {
        Optional<Medication> medication = medicationRepository.findById(id);
        if (medication.isPresent()) {
            fillMedicationFromRequest(medicationDto, medication.get());
            if (file != null && !file.isEmpty()) {
                medication.get().setImage(uploadImage(file));
            }
            medicationRepository.save(medication.get());
        }
    }

    @Transactional(readOnly = true)
    public MedicationDto getById(Long id) {
        Optional<Medication> medication = medicationRepository.findById(id);
        return medication.map(this::toResponse).orElse(null);
    }

    public List<Medication> findByDiseaseId(Long diseaseId, int page, int size) {
        return medicationRepository.findByDiseaseId(diseaseId);
    }

    @Transactional
    public void delete(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new RuntimeException("Medication not found with id = " + id);
        }
        medicationRepository.deleteById(id);
    }

    private void fillMedicationFromRequest(MedicationDto request, Medication medication) {
        medication.setName(request.getName());
        medication.setDescription(request.getDescription());

        Set<Disease> diseases = new HashSet<>(diseaseRepository.findAllById(
                request.getDiseases()
                        .stream()
                        .map(Disease::getId)
                        .collect(Collectors.toSet())
        ));
        medication.setDiseases(diseases);
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

    private MedicationDto toResponse(Medication medication) {
        return MedicationDto.builder()
                .id(medication.getId())
                .name(medication.getName())
                .description(medication.getDescription())
                .diseases(medication.getDiseases())
                .image(medication.getImage())
                .build();


    }
}

