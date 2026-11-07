package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveDto {

    private Long id;
    private String medicationName;
    private String image;
    private Integer quantity;
    private BigDecimal retailPrice;
    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private LocalDateTime writtenOffAt;
    private String reason;
}