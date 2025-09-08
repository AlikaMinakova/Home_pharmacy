package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;

import java.time.LocalDate;
import java.util.List;


public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    @Query("SELECT p FROM Pharmacy p WHERE p.purchaseDate >= :startDate")
    List<Pharmacy> findBoughtAfter(@Param("startDate") LocalDate startDate);


    @Query("SELECT p FROM Pharmacy p WHERE p.expirationDate <= :endDate")
    List<Pharmacy> findExpiringSoon(@Param("endDate") LocalDate endDate);
}