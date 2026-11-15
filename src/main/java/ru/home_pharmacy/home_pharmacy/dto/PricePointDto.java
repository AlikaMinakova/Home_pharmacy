package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Точка на графике изменения цены лекарства.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricePointDto {

    private LocalDate date;
    private BigDecimal price;

    /** Дата в формате дд.ММ.гггг для подписи оси графика. */
    private String label;
}