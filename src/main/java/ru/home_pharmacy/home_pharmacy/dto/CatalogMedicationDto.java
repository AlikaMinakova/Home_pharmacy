package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogMedicationDto {

    private Long id;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Long availableQuantity;
    private Boolean prescriptionRequired = false;

    public boolean isInStock() {
        return availableQuantity != null && availableQuantity > 0;
    }
}