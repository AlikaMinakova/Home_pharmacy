// PharmacyResponse.java
package ru.home_pharmacy.home_pharmacy.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyResponse {
    private Long id;
    private Long medicationId;
    private String medicationName;
    private String medicationDescription;
    private List<String> diseaseNames;
    private Integer quantity;
    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private MultipartFile image;
}
