package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.entity.OrderStatus;
import ru.home_pharmacy.home_pharmacy.service.OrderService;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "createdAt") String sortBy,
                             @RequestParam(defaultValue = "desc") String sortDir,
                             Model model) {
        model.addAttribute("orders", orderService.search(search, parseStatus(status), sortBy, sortDir));
        model.addAttribute("newCount", orderService.countNew());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        return "orders/list";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id) {
        orderService.confirm(id);
        return "redirect:/orders";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id) {
        orderService.reject(id);
        return "redirect:/orders";
    }

    @PostMapping("/{id}/purchase")
    public String purchase(@PathVariable Long id) {
        orderService.purchase(id);
        return "redirect:/orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return "redirect:/orders";
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}