package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.home_pharmacy.home_pharmacy.entity.Archive;

import java.util.List;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    List<Archive> findAllByOrderByWrittenOffAtDesc();

    Page<Archive> findAllByOrderByWrittenOffAtDesc(Pageable pageable);

    void deleteByMedication_Id(Long medicationId);
}