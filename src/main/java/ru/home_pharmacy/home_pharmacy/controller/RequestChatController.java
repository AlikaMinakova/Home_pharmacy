package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.home_pharmacy.home_pharmacy.entity.RequestMessage;
import ru.home_pharmacy.home_pharmacy.service.RequestService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RequestChatController {

    private final RequestService requestService;

    /** Возвращает фрагмент панели чата (история сообщений + форма отправки). */
    @GetMapping("/chat/{id}")
    public String chat(@PathVariable Long id, Model model) {
        boolean isEmployee = currentUserIsEmployee();
        String username = currentUsername();
        List<RequestMessage> messages = requestService.getMessages(id, username, isEmployee);
        if (isEmployee) {
            requestService.markPharmacistRead(id);
        } else {
            requestService.markRead(id, username);
        }
        model.addAttribute("messages", messages);
        model.addAttribute("requestId", id);
        return "chat/panel :: messages";
    }

    @PostMapping("/chat/{id}/send")
    public String send(@PathVariable Long id, @RequestParam String text) {
        requestService.sendMessage(id, text, currentUsername(), currentUserIsEmployee());
        return "redirect:/chat/" + id;
    }

    private boolean currentUserIsEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}