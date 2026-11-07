package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    @Query("SELECT p FROM Pharmacy p " +
            "JOIN p.medication m " +
            "WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Pharmacy> findByMedicationNameContainingIgnoreCase(
            @Param("keyword") String keyword,
            Pageable pageable);

    // Актуальные партии: есть остаток и срок ещё не истёк
    @Query("SELECT p FROM Pharmacy p WHERE p.quantity > 0 AND p.expirationDate > :today")
    Page<Pharmacy> findAllAvailable(@Param("today") LocalDate today, Pageable pageable);

    @Query("SELECT p FROM Pharmacy p JOIN p.medication m " +
            "WHERE p.quantity > 0 AND p.expirationDate > :today " +
            "AND LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Pharmacy> findAvailableByMedicationNameContaining(@Param("keyword") String keyword,
                                                           @Param("today") LocalDate today,
                                                           Pageable pageable);

    @Query("SELECT p FROM Pharmacy p WHERE p.purchaseDate >= :startDate")
    List<Pharmacy> findBoughtAfter(@Param("startDate") LocalDate startDate);

    // Партии с остатком срока годности до 7 дней (не включая уже просроченные ранее, но включая истекающие сегодня)
    @Query("SELECT p FROM Pharmacy p WHERE p.expirationDate >= :startDate AND p.expirationDate <= :endDate")
    List<Pharmacy> findExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Pharmacy p WHERE p.expirationDate >= :startDate AND p.expirationDate <= :endDate")
    Page<Pharmacy> findExpiringSoonPage(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        Pageable pageable);

    @Query("SELECT p FROM Pharmacy p WHERE p.expirationDate <= :now")
    Page<Pharmacy> findExpired(@Param("now") LocalDate now, Pageable pageable);

    @Query("SELECT p FROM Pharmacy p WHERE p.expirationDate <= :now")
    List<Pharmacy> findAllExpired(@Param("now") LocalDate now);

    @Query("SELECT p FROM Pharmacy p " +
            "JOIN FETCH p.medication m " +
            "LEFT JOIN FETCH m.diseases " +
            "LEFT JOIN FETCH m.activeIngredients " +
            "WHERE p.id = :id")
    Optional<Pharmacy> findWithMedicationById(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Pharmacy p " +
            "WHERE p.medication.id = :medicationId AND p.expirationDate > :today")
    Long sumAvailableQuantity(@Param("medicationId") Long medicationId, @Param("today") LocalDate today);

    @Query("SELECT p FROM Pharmacy p " +
            "WHERE p.medication.id = :medicationId AND p.quantity > 0 AND p.expirationDate > :today " +
            "ORDER BY p.expirationDate ASC, p.purchaseDate ASC")
    List<Pharmacy> findAvailableBatches(@Param("medicationId") Long medicationId, @Param("today") LocalDate today);
}