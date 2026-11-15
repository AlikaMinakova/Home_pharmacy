package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.home_pharmacy.home_pharmacy.entity.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndMedicationId(Long userId, Long medicationId);

    boolean existsByUserIdAndMedicationId(Long userId, Long medicationId);

    void deleteByUserIdAndMedicationId(Long userId, Long medicationId);
}