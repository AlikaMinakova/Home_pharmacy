package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.home_pharmacy.home_pharmacy.service.RequestService;

/**
 * Добавляет на каждую страницу количество непрочитанных комментариев аптекаря
 * для текущего авторизованного покупателя (для показа значка в меню).
 */
@ControllerAdvice
@RequiredArgsConstructor
public class CustomerNotificationAdvice {

    private final RequestService requestService;

    @ModelAttribute
    public void addUnreadCount(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            model.addAttribute("unreadRequests", 0L);
            model.addAttribute("pharmacistUnread", 0L);
            return;
        }
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (isEmployee) {
            model.addAttribute("unreadRequests", 0L);
            model.addAttribute("pharmacistUnread", requestService.countPharmacistUnread());
            return;
        }
        String username = auth.getName();
        long unread = (username != null && !"anonymousUser".equals(username))
                ? requestService.countUnread(username) : 0L;
        model.addAttribute("unreadRequests", unread);
        model.addAttribute("pharmacistUnread", 0L);
    }
}