package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.home_pharmacy.home_pharmacy.entity.PriceHistory;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findAllByMedicationIdOrderByChangedAtAsc(Long medicationId);
}