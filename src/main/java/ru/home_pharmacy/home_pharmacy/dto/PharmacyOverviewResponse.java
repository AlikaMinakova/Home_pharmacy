package ru.home_pharmacy.home_pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import ru.home_pharmacy.home_pharmacy.entity.CustomerOrder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharmacyOverviewResponse {

    private Page<PharmacyDto> all;          // весь список
    private List<CustomerOrder> recentlyBought; // недавно подтверждённые заказы покупателей
    private List<PharmacyDto> expiringSoon;   // истекает срок годности (7 дней и меньше)
}
