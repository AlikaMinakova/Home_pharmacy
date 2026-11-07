package ru.home_pharmacy.home_pharmacy.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.home_pharmacy.home_pharmacy.dto.ActiveIngredientDto;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseDto;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.entity.ActiveIngredient;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.repository.ActiveIngredientRepository;
import ru.home_pharmacy.home_pharmacy.repository.ArchiveRepository;
import ru.home_pharmacy.home_pharmacy.repository.CustomerOrderRepository;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
    private final ActiveIngredientRepository activeIngredientRepository;
    private final ArchiveRepository archiveRepository;
    private final CustomerOrderRepository orderRepository;

    public List<MedicationDto> getAll() {
        return medicationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MedicationDto> getAllMedications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return medicationRepository.findAll(pageable)
                .map(medication -> new MedicationDto(
                        medication.getId(),
                        medication.getName(),
                        medication.getDescription(),
                        medication.getImage(),
                        medication.getPrice(),
                        medication.getPrescriptionRequired(),
                        null,
                        null));
    }

    public Page<MedicationDto> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return medicationRepository.findByNameContaining(name, pageable)
                .map(medication -> new MedicationDto(
                        medication.getId(),
                        medication.getName(),
                        medication.getDescription(),
                        medication.getImage(),
                        medication.getPrice(),
                        medication.getPrescriptionRequired(),
                        null,
                        null));
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
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id = " + id));
        // Разорвать связи с болезнями и действующими веществами (junction-таблицы)
        medication.getDiseases().clear();
        medication.getActiveIngredients().clear();
        // Удалить зависимые записи (FK): архив и заказы покупателей
        archiveRepository.deleteByMedication_Id(id);
        orderRepository.deleteByMedication_Id(id);
        // Партии в аптечке удалятся каскадно (orphanRemoval)
        medicationRepository.delete(medication);
    }

    private void fillMedicationFromRequest(MedicationDto request, Medication medication) {
        medication.setName(request.getName());
        medication.setDescription(request.getDescription());
        medication.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
        medication.setPrescriptionRequired(
                request.getPrescriptionRequired() != null && request.getPrescriptionRequired());

        // Болезни
        Set<Disease> diseases = new HashSet<>(diseaseRepository.findAllById(
                request.getDiseases().stream()
                        .map(Disease::getId)
                        .collect(Collectors.toSet())
        ));
        medication.setDiseases(diseases);

        // Действующие вещества
        if (request.getActiveIngredients() != null) {
            Set<ActiveIngredient> ingredients = new HashSet<>(activeIngredientRepository.findAllById(
                    request.getActiveIngredients().stream()
                            .map(ActiveIngredient::getId)
                            .collect(Collectors.toSet())
            ));
            medication.setActiveIngredients(ingredients);
        } else {
            medication.setActiveIngredients(new HashSet<>());
        }
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

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<MedicationDto> getMedicationsByActiveIngredientId(Long ingredientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medication> medications = medicationRepository.findByActiveIngredientId(ingredientId, pageable);
        return medications.map(this::toResponse);
    }

    private MedicationDto toResponse(Medication medication) {
        return MedicationDto.builder()
                .id(medication.getId())
                .name(medication.getName())
                .description(medication.getDescription())
                .image(medication.getImage())
                .price(medication.getPrice() != null ? medication.getPrice() : BigDecimal.ZERO)
                .prescriptionRequired(Boolean.TRUE.equals(medication.getPrescriptionRequired()))
                .diseases(medication.getDiseases())
                .activeIngredients(medication.getActiveIngredients())
                .build();
    }
}

