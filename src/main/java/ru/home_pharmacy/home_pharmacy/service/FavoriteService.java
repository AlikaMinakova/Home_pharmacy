package ru.home_pharmacy.home_pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.FavoriteDto;
import ru.home_pharmacy.home_pharmacy.entity.Favorite;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.repository.FavoriteRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;
import ru.home_pharmacy.home_pharmacy.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;

    public void add(String username, Long medicationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
        if (favoriteRepository.existsByUserIdAndMedicationId(user.getId(), medicationId)) {
            return;
        }
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Лекарство не найдено: " + medicationId));
        // если товар уже в наличии — уведомлять не нужно
        boolean inStock = isInStock(medicationId);
        favoriteRepository.save(Favorite.builder()
                .user(user)
                .medication(medication)
                .notified(inStock)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public void remove(String username, Long medicationId) {
        userRepository.findByUsername(username)
                .ifPresent(user -> favoriteRepository.deleteByUserIdAndMedicationId(user.getId(), medicationId));
    }

    public void removeById(String username, Long favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId).orElse(null);
        if (favorite == null) {
            return;
        }
        userRepository.findByUsername(username).ifPresent(user -> {
            if (favorite.getUser().getId().equals(user.getId())) {
                favoriteRepository.delete(favorite);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> getFavorites(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }
        return favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String username, Long medicationId) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && favoriteRepository.existsByUserIdAndMedicationId(user.getId(), medicationId);
    }

    /**
     * Проверяет избранное покупателя и возвращает товары, которые снова появились
     * в наличии (ранее были недоступны). Такие позиции помечаются уведомлёнными,
     * чтобы показать покупателю уведомление при входе в систему. Если товар снова
     * исчез со склада — флаг сбрасывается, чтобы уведомить при следующем поступлении.
     */
    public List<FavoriteDto> notifyRestocked(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }
        List<Favorite> favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        List<FavoriteDto> newlyAvailable = new ArrayList<>();
        for (Favorite favorite : favorites) {
            boolean inStock = isInStock(favorite.getMedication().getId());
            if (inStock && !Boolean.TRUE.equals(favorite.getNotified())) {
                newlyAvailable.add(toDto(favorite));
                favorite.setNotified(true);
            } else if (!inStock) {
                favorite.setNotified(false);
            }
        }
        favoriteRepository.saveAll(favorites);
        return newlyAvailable;
    }

    private boolean isInStock(Long medicationId) {
        Long quantity = pharmacyRepository.sumAvailableQuantity(medicationId, LocalDate.now());
        return quantity != null && quantity > 0;
    }

    private FavoriteDto toDto(Favorite favorite) {
        Medication medication = favorite.getMedication();
        Long available = pharmacyRepository.sumAvailableQuantity(medication.getId(), LocalDate.now());
        boolean inStock = available != null && available > 0;
        return FavoriteDto.builder()
                .id(favorite.getId())
                .medicationId(medication.getId())
                .name(medication.getName())
                .image(medication.getImage())
                .price(medication.getPrice() != null ? medication.getPrice() : BigDecimal.ZERO)
                .inStock(inStock)
                .availableQuantity(available)
                .build();
    }
}