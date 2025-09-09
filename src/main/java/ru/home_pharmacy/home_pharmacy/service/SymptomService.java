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
import ru.home_pharmacy.home_pharmacy.dto.SymptomRequest;
import ru.home_pharmacy.home_pharmacy.dto.SymptomResponse;
import ru.home_pharmacy.home_pharmacy.entity.Symptom;
import ru.home_pharmacy.home_pharmacy.repository.SymptomRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class SymptomService {
    @Autowired
    SymptomRepository symptomRepository;

    @Transactional(readOnly = true)
    public List<SymptomResponse> getAllSymptoms() {
        return symptomRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<SymptomResponse> getPaginateSymptoms(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return symptomRepository.findAll(pageable)
                .map(symptom -> new SymptomResponse(
                        symptom.getId(),
                        symptom.getName()
                ));
    }

    @Transactional
    public SymptomResponse createSymptom(SymptomRequest request) {
        Symptom symptom = Symptom.builder()
                .name(request.getName())
                .build();

        Symptom saved = symptomRepository.save(symptom);
        return mapToResponse(saved);
    }


    @Transactional(readOnly = true)
    public SymptomResponse getSymptom(Long id) {
        Symptom symptom = symptomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Symptom not found with id = " + id));
        return mapToResponse(symptom);
    }


    @Transactional
    public SymptomResponse updateSymptom(Long id, SymptomRequest request) {
        Symptom symptom = symptomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Symptom not found with id = " + id));

        symptom.setName(request.getName());

        Symptom updated = symptomRepository.save(symptom);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSymptom(Long id) {
        if (!symptomRepository.existsById(id)) {
            throw new RuntimeException("Symptom not found with id = " + id);
        }
        symptomRepository.deleteById(id);
    }

    private SymptomResponse mapToResponse(Symptom symptom) {
        return SymptomResponse.builder()
                .id(symptom.getId())
                .name(symptom.getName())
                .build();
    }
}
