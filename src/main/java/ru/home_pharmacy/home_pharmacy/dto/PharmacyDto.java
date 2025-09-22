package ru.home_pharmacy.home_pharmacy.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.home_pharmacy.home_pharmacy.entity.Medication;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDto {
    private Long id;
    private MedicationDto medication;
    @NotNull
    @Min(value = 1, message = "Количество должно быть больше 0")
    private Integer quantity;
    @NotNull(message = "Срок годности обязателен")
    @FutureOrPresent(message = "Срок годности не может быть в прошлом")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;
    @NotNull(message = "Дата покупки обязательна")
    @PastOrPresent(message = "Дата покупки не может быть в будущем")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;
}
