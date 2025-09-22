package ru.home_pharmacy.home_pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.home_pharmacy.home_pharmacy.entity.Disease;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationDto {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private String image;
    @NotNull(message = "Выберите хотя бы одну болезнь")
    @Size(min = 1, message = "Выберите хотя бы одну болезнь")
    private Set<Disease> diseases = new HashSet<>();
}
