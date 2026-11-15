package ru.home_pharmacy.home_pharmacy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.home_pharmacy.home_pharmacy.dto.CartCheckoutDto;
import ru.home_pharmacy.home_pharmacy.dto.CartItemDto;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.service.CartService;
import ru.home_pharmacy.home_pharmacy.service.CatalogService;
import ru.home_pharmacy.home_pharmacy.service.OrderService;
import ru.home_pharmacy.home_pharmacy.service.UserService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CatalogService catalogService;
    private final OrderService orderService;
    private final UserService userService;

    @PostMapping("/add/{medicationId}")
    public String add(@PathVariable Long medicationId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @AuthenticationPrincipal UserDetails principal,
                      RedirectAttributes redirectAttributes) {
        cartService.add(principal.getUsername(), medicationId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Товар добавлен в корзину.");
        return "redirect:/cart";
    }

    @GetMapping
    public String cartPage(@AuthenticationPrincipal UserDetails principal, Model model) {
        List<CartItemDto> items = cartService.getCartItems(principal.getUsername());
        model.addAttribute("items", items);
        model.addAttribute("cartTotal", total(items));
        return "cart/cart";
    }

    @PostMapping("/update/{medicationId}")
    public String update(@PathVariable Long medicationId, @RequestParam int quantity,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        Long available = catalogService.getAvailableQuantity(medicationId);
        if (available != null && quantity > available) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Нельзя заказать больше " + available + " шт — этого количества нет в наличии.");
            return "redirect:/cart";
        }
        cartService.updateQuantity(principal.getUsername(), medicationId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{medicationId}")
    public String remove(@PathVariable Long medicationId,
                         @AuthenticationPrincipal UserDetails principal) {
        cartService.remove(principal.getUsername(), medicationId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        List<CartItemDto> items = cartService.getCartItems(principal.getUsername());
        if (items.isEmpty()) {
            return "redirect:/cart";
        }
        CartCheckoutDto dto = new CartCheckoutDto();
        prefillCustomer(dto, principal.getUsername());
        model.addAttribute("checkout", dto);
        model.addAttribute("items", items);
        model.addAttribute("cartTotal", total(items));
        model.addAttribute("isCustomer", true);
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("checkout") CartCheckoutDto dto,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        String username = principal.getUsername();
        // телефон — из аккаунта (защита от подмены)
        dto.setCustomerPhone(username);

        List<CartItemDto> items = cartService.getCartItems(username);

        // нельзя заказать больше, чем есть на складе по каждой позиции
        if (!bindingResult.hasErrors()) {
            for (CartItemDto item : items) {
                if (!item.isInStock() || item.getQuantity() > item.getAvailableQuantity()) {
                    bindingResult.reject("stock",
                            "«" + item.getName() + "» недоступно в нужном количестве (остаток: "
                                    + item.getAvailableQuantity() + " шт). Обновите корзину.");
                }
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("items", items);
            model.addAttribute("cartTotal", total(items));
            model.addAttribute("isCustomer", true);
            return "cart/checkout";
        }

        orderService.createOrdersFromCart(cartService.getQuantityMap(username), dto);
        cartService.clear(username);
        redirectAttributes.addFlashAttribute("successMessage",
                "Заказ оформлен! Заберите лекарства в аптечном пункте.");
        return "redirect:/my-orders";
    }

    private void prefillCustomer(CartCheckoutDto dto, String username) {
        User user = userService.findByUsername(username).orElse(null);
        if (user != null) {
            dto.setCustomerName(user.getName());
            dto.setCustomerPhone(username);
        }
    }

    private BigDecimal total(List<CartItemDto> items) {
        return items.stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}