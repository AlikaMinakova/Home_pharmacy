package ru.home_pharmacy.home_pharmacy.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    private Set<Long> symptomIds;
}
