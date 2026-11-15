package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.home_pharmacy.home_pharmacy.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<CartItem> findByUserIdAndMedicationId(Long userId, Long medicationId);

    void deleteByUserIdAndMedicationId(Long userId, Long medicationId);

    void deleteByUserId(Long userId);
}