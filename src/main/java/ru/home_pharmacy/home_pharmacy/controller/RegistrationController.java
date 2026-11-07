package ru.home_pharmacy.home_pharmacy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.home_pharmacy.home_pharmacy.dto.RegistrationDto;
import ru.home_pharmacy.home_pharmacy.service.UserService;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registration", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") RegistrationDto registration,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(registration);
        } catch (RuntimeException e) {
            bindingResult.rejectValue("phone", "phone", e.getMessage());
            return "register";
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Регистрация прошла успешно! Войдите, используя номер телефона и пароль.");
        return "redirect:/login";
    }
}