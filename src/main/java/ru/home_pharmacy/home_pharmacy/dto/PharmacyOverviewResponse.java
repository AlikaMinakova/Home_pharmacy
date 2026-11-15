package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharmacyOverviewResponse {

    private Page<StockDto> all;           // сводные остатки по лекарствам (по одной карточке на лекарство)
    private List<PharmacyDto> expiringSoon;   // истекает срок годности (7 дней и меньше)
}
