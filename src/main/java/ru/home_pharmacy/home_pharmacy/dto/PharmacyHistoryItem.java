package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Элемент единой истории поступлений: активная партия, списанная или удалённая.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyHistoryItem {

    private Long id;
    private String medicationName;
    private String image;
    private Integer quantity;
    private BigDecimal retailPrice;
    private LocalDate expirationDate;
    private LocalDate purchaseDate;

    /** ACTIVE — в аптечке, WRITTEN_OFF — списано, DELETED — удалено вручную. */
    private String status;

    /** Дата для сортировки (покупка/поступление или дата списания/удаления). */
    private LocalDate sortDate;
}