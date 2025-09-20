package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home_pharmacy.home_pharmacy.entity.Medication;


public interface MedicationRepository extends JpaRepository<Medication, Long> {
    @Query("SELECT m FROM Medication m JOIN m.diseases d WHERE d.id = :diseaseId")
    Page<Medication> findByDiseaseId(@Param("diseaseId") Long diseaseId, Pageable pageable);
}