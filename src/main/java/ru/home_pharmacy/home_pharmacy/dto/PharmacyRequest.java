package ru.home_pharmacy.home_pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import ru.home_pharmacy.home_pharmacy.entity.Medication;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyRequest {
    private Long id;
    private Long medicationId;
    @Valid
    private Medication medication;
    private String medicationDescription;
    private List<String> diseaseNames;
    @NotNull(message = "Количество обязательно")
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
    private MultipartFile image;
}
