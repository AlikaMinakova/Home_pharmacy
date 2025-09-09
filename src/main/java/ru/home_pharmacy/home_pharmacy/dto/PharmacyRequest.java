package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import ru.home_pharmacy.home_pharmacy.entity.Medication;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyRequest {
    private Long id;
    private Long medicationId;
    private Medication medication;
    private String medicationDescription;
    private List<String> diseaseNames;
    private Integer quantity;
    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private MultipartFile image;
}
