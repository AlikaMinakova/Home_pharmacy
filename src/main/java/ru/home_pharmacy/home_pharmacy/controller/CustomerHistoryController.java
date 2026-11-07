package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.home_pharmacy.home_pharmacy.service.OrderService;
import ru.home_pharmacy.home_pharmacy.service.RequestService;

@Controller
@RequiredArgsConstructor
public class CustomerHistoryController {

    private final OrderService orderService;
    private final RequestService requestService;

    @GetMapping("/my-orders")
    public String myOrders(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("orders", orderService.getOrdersByPhone(principal.getUsername()));
        return "my/orders";
    }

    @GetMapping("/my-requests")
    public String myRequests(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("requests", requestService.getRequestsByPhone(principal.getUsername()));
        return "my/requests";
    }

    @PostMapping("/my-requests/{id}/confirm")
    public String confirm(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails principal,
                          RedirectAttributes redirectAttributes) {
        try {
            requestService.buyerConfirm(id, principal.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Заявка подтверждена.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-requests";
    }

    @PostMapping("/my-requests/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        try {
            requestService.buyerCancel(id, principal.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Заявка отменена.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-requests";
    }
}