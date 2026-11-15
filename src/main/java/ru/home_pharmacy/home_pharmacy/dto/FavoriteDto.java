package ru.home_pharmacy.home_pharmacy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Избранный товар для отображения. Содержит данные о лекарстве и актуальный
 * остаток на складе (чтобы не тянуть ленивые связи в шаблон).
 */
@Data
@Builder
public class FavoriteDto {

    private Long id;
    private Long medicationId;
    private String name;
    private String image;
    private BigDecimal price;
    private boolean inStock;
    private Long availableQuantity;
}