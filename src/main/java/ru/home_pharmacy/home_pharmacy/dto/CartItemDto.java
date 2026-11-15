package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Позиция корзины для отображения: лекарство + количество + актуальный остаток.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {

    private Long medicationId;
    private String name;
    private String image;
    private BigDecimal price;
    private Integer quantity;
    private Long availableQuantity;
    private boolean inStock;

    /** Стоимость позиции = цена × количество. */
    public BigDecimal getLineTotal() {
        return price != null ? price.multiply(BigDecimal.valueOf(quantity != null ? quantity : 0)) : BigDecimal.ZERO;
    }
}