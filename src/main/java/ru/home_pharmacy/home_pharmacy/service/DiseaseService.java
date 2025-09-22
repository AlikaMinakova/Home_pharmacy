package ru.home_pharmacy.home_pharmacy.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseDto;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Symptom;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.SymptomRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DiseaseService {

    @Autowired
    private final DiseaseRepository diseaseRepository;
    @Autowired
    private final SymptomRepository symptomRepository;

    @Transactional
    public DiseaseDto createDisease(DiseaseDto request) {
        Set<Symptom> symptoms = new HashSet<>();
        if (!request.getSymptomIds().isEmpty()) {
            symptoms = new HashSet<>(symptomRepository.findAllById(request.getSymptomIds()));
        }

        Disease disease = Disease.builder()
                .name(request.getName())
                .description(request.getDescription())
                .symptoms(symptoms)
                .build();

        Disease saved = diseaseRepository.save(disease);
        return mapToResponse(saved);
    }


    @Transactional(readOnly = true)
    public DiseaseDto getDisease(Long id) {
        Disease disease = diseaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disease not found with id = " + id));
        return mapToResponse(disease);
    }

    public List<DiseaseDto> getAll() {
        return diseaseRepository.findAll().stream().map(this::mapToResponse).toList();
    }


    @Transactional(readOnly = true)
    public Page<DiseaseDto> getAllDiseases(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return diseaseRepository.findAll(pageable)
                .map(disease -> new DiseaseDto(
                        disease.getId(),
                        disease.getName(),
                        disease.getDescription(),
                        null
                ));
    }

    @Transactional
    public DiseaseDto updateDisease(Long id, DiseaseDto request) {
        Disease disease = diseaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disease not found with id = " + id));

        disease.setName(request.getName());
        disease.setDescription(request.getDescription());

        if (request.getSymptomIds() != null) {
            Set<Symptom> symptoms = new HashSet<>(symptomRepository.findAllById(request.getSymptomIds()));
            disease.setSymptoms(symptoms);
        } else {
            disease.setSymptoms(new HashSet<>());
        }

        Disease updated = diseaseRepository.save(disease);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDisease(Long id) {
        if (!diseaseRepository.existsById(id)) {
            throw new RuntimeException("Disease not found with id = " + id);
        }
        diseaseRepository.deleteById(id);
    }

    private DiseaseDto mapToResponse(Disease disease) {
        return DiseaseDto.builder()
                .id(disease.getId())
                .name(disease.getName())
                .description(disease.getDescription())
                .symptomIds(
                        disease.getSymptoms().stream()
                                .map(Symptom::getId)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public Page<Disease> findBySymptomId(Long id, Pageable pageable) {

        return diseaseRepository.findBySymptomId(id, pageable);}

    public Optional<Disease> findById(Long id) {
        return diseaseRepository.findById(id);
    }
}
