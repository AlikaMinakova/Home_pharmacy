package ru.home_pharmacy.home_pharmacy.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT p FROM Pharmacy p WHERE p.purchaseDate >= :startDate")
    List<Pharmacy> findBoughtAfter(@Param("startDate") LocalDate startDate);


    @Query("SELECT p FROM Pharmacy p WHERE p.expirationDate <= :endDate")
    List<Pharmacy> findExpiringSoon(@Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Pharmacy p WHERE p.expirationDate <= :now")
    Page<Pharmacy> findExpired(@Param("now") LocalDate now, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Pharmacy p WHERE p.expirationDate <= :today")
    void deleteAllExpired(@Param("today") LocalDate today);

    @Query("SELECT p FROM Pharmacy p " +
            "JOIN FETCH p.medication m " +
            "LEFT JOIN FETCH m.diseases " +
            "LEFT JOIN FETCH m.activeIngredients " +
            "WHERE p.id = :id")
    Optional<Pharmacy> findWithMedicationById(@Param("id") Long id);
}