package ru.home_pharmacy.home_pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.home_pharmacy.home_pharmacy.util.PhoneUtils;

/**
 * Данные покупателя на странице оформления заказа из корзины.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartCheckoutDto {

    @NotBlank(message = "Укажите ваше имя")
    private String customerName;

    @NotBlank(message = "Укажите телефон")
    @Pattern(regexp = PhoneUtils.PHONE_REGEX,
            message = "Некорректный номер. Пример: 8 900 123-45-67")
    private String customerPhone;
}