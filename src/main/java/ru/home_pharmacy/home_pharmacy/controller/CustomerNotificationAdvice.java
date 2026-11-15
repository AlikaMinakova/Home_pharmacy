package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.home_pharmacy.home_pharmacy.dto.FavoriteDto;
import ru.home_pharmacy.home_pharmacy.service.CartService;
import ru.home_pharmacy.home_pharmacy.service.FavoriteService;
import ru.home_pharmacy.home_pharmacy.service.OrderService;
import ru.home_pharmacy.home_pharmacy.service.RequestService;

import java.util.List;

/**
 * Добавляет на каждую страницу данные, нужные в навигации и для уведомлений:
 * количество непрочитанных комментариев аптекаря, счётчик корзины и список
 * избранных товаров, которые снова появились в наличии.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class CustomerNotificationAdvice {

    private final RequestService requestService;
    private final FavoriteService favoriteService;
    private final CartService cartService;
    private final OrderService orderService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated()
                && auth.getName() != null && !"anonymousUser".equals(auth.getName());

        model.addAttribute("cartCount", 0);
        model.addAttribute("orderUnread", 0L);
        model.addAttribute("newOrders", 0L);
        model.addAttribute("newRequests", 0L);

        if (!authenticated) {
            model.addAttribute("unreadRequests", 0L);
            model.addAttribute("pharmacistUnread", 0L);
            model.addAttribute("restockNotifications", List.of());
            return;
        }

        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isEmployee) {
            model.addAttribute("newOrders", orderService.countNew());
            model.addAttribute("newRequests", requestService.countNew());
            model.addAttribute("unreadRequests", 0L);
            model.addAttribute("pharmacistUnread", requestService.countPharmacistUnread());
            model.addAttribute("restockNotifications", List.of());
            return;
        }

        String username = auth.getName();
        model.addAttribute("cartCount", cartService.getCartCount(username));
        model.addAttribute("orderUnread", orderService.countUnreadByPhone(username));
        model.addAttribute("unreadRequests", requestService.countUnread(username));
        model.addAttribute("pharmacistUnread", 0L);
        // избранные товары, которые снова появились в наличии
        List<FavoriteDto> restocked = favoriteService.notifyRestocked(username);
        model.addAttribute("restockNotifications", restocked);
    }
}