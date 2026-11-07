package ru.home_pharmacy.home_pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.home_pharmacy.home_pharmacy.util.PhoneUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDto {

    @NotBlank(message = "Укажите ваше имя")
    private String name;

    @NotBlank(message = "Укажите телефон")
    @Pattern(regexp = PhoneUtils.PHONE_REGEX,
            message = "Некорректный номер. Пример: 8 900 123-45-67")
    private String phone;

    @NotBlank(message = "Придумайте пароль")
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    private String password;
}