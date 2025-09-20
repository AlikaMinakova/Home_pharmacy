package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home_pharmacy.home_pharmacy.entity.Disease;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    @Query("SELECT d FROM Disease d JOIN d.symptoms s WHERE s.id = :symptomId")
    Page<Disease> findBySymptomId(@Param("symptomId") Long symptomId, Pageable pageable);
}