package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home_pharmacy.home_pharmacy.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByMedicationIdOrderByCreatedAtDesc(Long medicationId);

    Optional<Review> findByUserIdAndMedicationId(Long userId, Long medicationId);

    boolean existsByUserIdAndMedicationId(Long userId, Long medicationId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.medication.id = :id")
    Double findAverageRating(@Param("id") Long medicationId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.medication.id = :id")
    Long countByMedicationId(@Param("id") Long medicationId);
}