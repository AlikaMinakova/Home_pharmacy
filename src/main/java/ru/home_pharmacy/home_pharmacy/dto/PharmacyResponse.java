// PharmacyResponse.java
package ru.home_pharmacy.home_pharmacy.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import ru.home_pharmacy.home_pharmacy.entity.Medication;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyResponse {
    private Long id;
    private Long medicationId;
    private Medication medication;
    private String medicationDescription;
    private List<String> diseaseNames;
    private Integer quantity;
    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private String image;
}
