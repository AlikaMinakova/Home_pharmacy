package ru.home_pharmacy.home_pharmacy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Сводный остаток лекарства на складе: одна карточка на лекарство
 * с суммарным количеством по всем неистёкшим партиям.
 */
@Data
@Builder
public class StockDto {

    private Long medicationId;
    /** id партии (наиболее свежей) для перехода в карточку склада /pharmacies/detail. */
    private Long batchId;
    private String name;
    private String image;
    private BigDecimal price;
    private Long totalQuantity;
}