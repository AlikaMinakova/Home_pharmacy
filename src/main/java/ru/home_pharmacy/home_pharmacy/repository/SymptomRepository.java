package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.home_pharmacy.home_pharmacy.entity.Symptom;


public interface SymptomRepository extends JpaRepository<Symptom, Long> {

}