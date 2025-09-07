package ru.home_pharmacy.home_pharmacy.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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
    @Builder.Default
    private List<Long> symptomIds  = new ArrayList<>();
}
