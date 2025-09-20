package ru.home_pharmacy.home_pharmacy.dto;


import jakarta.validation.constraints.NotBlank;
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
public class DiseaseRequest {
    private Long id;
    @NotBlank(message = "Название обязательно")
    private String name;

    @NotBlank(message = "Описание обязательно")
    private String description;
    @Builder.Default
    @Size(min = 1, message = "Выберите хотя бы один симптом")
    private List<Long> symptomIds = new ArrayList<>();
}
