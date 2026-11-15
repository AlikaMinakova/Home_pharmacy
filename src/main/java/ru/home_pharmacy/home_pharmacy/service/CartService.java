package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.CartItemDto;
import ru.home_pharmacy.home_pharmacy.entity.CartItem;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.repository.CartItemRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;
import ru.home_pharmacy.home_pharmacy.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Корзина покупателя, хранящаяся в БД и привязанная к конкретному пользователю.
 * Каждый аккаунт видит только свой набор товаров; при выходе/входе данные не теряются.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;

    private User user(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems(String username) {
        Long userId = user(username).getId();
        List<CartItemDto> result = new ArrayList<>();
        for (CartItem item : cartItemRepository.findAllByUserIdOrderByIdAsc(userId)) {
            Medication medication = item.getMedication();
            Long available = pharmacyRepository.sumAvailableQuantity(medication.getId(), LocalDate.now());
            result.add(CartItemDto.builder()
                    .medicationId(medication.getId())
                    .name(medication.getName())
                    .image(medication.getImage())
                    .price(medication.getPrice() != null ? medication.getPrice() : BigDecimal.ZERO)
                    .quantity(item.getQuantity())
                    .availableQuantity(available)
                    .inStock(available != null && available > 0)
                    .build());
        }
        return result;
    }

    public void add(String username, Long medicationId, int quantity) {
        User user = user(username);
        CartItem item = cartItemRepository.findByUserIdAndMedicationId(user.getId(), medicationId)
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .medication(medicationRepository.findById(medicationId)
                                .orElseThrow(() -> new RuntimeException("Лекарство не найдено: " + medicationId)))
                        .quantity(0)
                        .build());
        item.setQuantity(item.getQuantity() + Math.max(quantity, 1));
        cartItemRepository.save(item);
    }

    public void updateQuantity(String username, Long medicationId, int quantity) {
        Long userId = user(username).getId();
        CartItem item = cartItemRepository.findByUserIdAndMedicationId(userId, medicationId).orElse(null);
        if (item == null) {
            return;
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    public void remove(String username, Long medicationId) {
        Long userId = user(username).getId();
        cartItemRepository.deleteByUserIdAndMedicationId(userId, medicationId);
    }

    public void clear(String username) {
        cartItemRepository.deleteByUserId(user(username).getId());
    }

    @Transactional(readOnly = true)
    public int getCartCount(String username) {
        Long userId = user(username).getId();
        return cartItemRepository.findAllByUserIdOrderByIdAsc(userId)
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getQuantityMap(String username) {
        Long userId = user(username).getId();
        Map<Long, Integer> map = new LinkedHashMap<>();
        for (CartItem item : cartItemRepository.findAllByUserIdOrderByIdAsc(userId)) {
            map.put(item.getMedication().getId(), item.getQuantity());
        }
        return map;
    }
}