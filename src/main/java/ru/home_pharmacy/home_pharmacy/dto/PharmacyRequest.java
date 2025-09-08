package ru.home_pharmacy.home_pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyRequest {

    @NotBlank
    private String medicationName;

    private String medicationDescription;

    private MultipartFile image;

    private List<Long> diseaseIds;

    @NotNull
    private Integer quantity;

    @NotNull
    private LocalDate expirationDate;

    private LocalDate purchaseDate;
}
