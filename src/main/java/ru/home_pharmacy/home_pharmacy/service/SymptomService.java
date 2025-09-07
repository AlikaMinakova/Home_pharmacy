package ru.home_pharmacy.home_pharmacy.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseResponse;
import ru.home_pharmacy.home_pharmacy.dto.SymptomResponse;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
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
    private SymptomResponse mapToResponse(Symptom symptom) {
        return SymptomResponse.builder()
                .id(symptom.getId())
                .name(symptom.getName())
                .build();
    }
}
