package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.home_pharmacy.home_pharmacy.service.FavoriteService;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("favorites", favoriteService.getFavorites(principal.getUsername()));
        return "favorite/list";
    }

    @PostMapping("/add/{medicationId}")
    public String add(@PathVariable Long medicationId,
                      @AuthenticationPrincipal UserDetails principal,
                      RedirectAttributes redirectAttributes) {
        favoriteService.add(principal.getUsername(), medicationId);
        redirectAttributes.addFlashAttribute("successMessage", "Товар добавлен в избранное.");
        return "redirect:/catalog/medication/" + medicationId;
    }

    @PostMapping("/remove/{medicationId}")
    public String remove(@PathVariable Long medicationId,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        favoriteService.remove(principal.getUsername(), medicationId);
        redirectAttributes.addFlashAttribute("successMessage", "Товар удалён из избранного.");
        return "redirect:/catalog/medication/" + medicationId;
    }

    @PostMapping("/{favoriteId}/delete")
    public String deleteFromList(@PathVariable Long favoriteId,
                                 @AuthenticationPrincipal UserDetails principal) {
        favoriteService.removeById(principal.getUsername(), favoriteId);
        return "redirect:/favorites";
    }
}