package ru.home_pharmacy.home_pharmacy.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Builder.Default
    @NotNull(message = "Выберите хотя бы один симптом")
    @Size(min = 1, message = "Выберите хотя бы один симптом")
    private List<Long> symptomIds = new ArrayList<>();
}
