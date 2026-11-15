package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.home_pharmacy.home_pharmacy.service.MedicationService;
import ru.home_pharmacy.home_pharmacy.service.OrderService;
import ru.home_pharmacy.home_pharmacy.service.PharmacyService;
import ru.home_pharmacy.home_pharmacy.service.RequestService;

/**
 * Сводная страница сотрудника: ключевые показатели (заказы, заявки, склад).
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final OrderService orderService;
    private final RequestService requestService;
    private final PharmacyService pharmacyService;
    private final MedicationService medicationService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("newOrders", orderService.countNew());
        model.addAttribute("totalOrders", orderService.countAll());
        model.addAttribute("newRequests", requestService.countNew());
        model.addAttribute("totalRequests", requestService.countAll());
        model.addAttribute("expiredCount", pharmacyService.countExpired());
        model.addAttribute("medicationCount", medicationService.count());
        return "dashboard";
    }
}