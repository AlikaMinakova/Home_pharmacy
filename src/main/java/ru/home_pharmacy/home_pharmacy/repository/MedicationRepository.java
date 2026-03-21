package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home_pharmacy.home_pharmacy.entity.Medication;

import java.util.List;


public interface MedicationRepository extends JpaRepository<Medication, Long> {
    @Query("SELECT m FROM Medication m JOIN m.diseases d WHERE d.id = :diseaseId")
    List<Medication> findByDiseaseId(@Param("diseaseId") Long diseaseId);

    @Query("SELECT m FROM Medication m JOIN m.activeIngredients ai WHERE ai.id = :ingredientId")
    Page<Medication> findByActiveIngredientId(@Param("ingredientId") Long ingredientId, Pageable pageable);
}