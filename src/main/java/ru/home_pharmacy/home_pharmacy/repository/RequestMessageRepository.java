package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.home_pharmacy.home_pharmacy.entity.RequestMessage;

import java.util.List;

public interface RequestMessageRepository extends JpaRepository<RequestMessage, Long> {

    List<RequestMessage> findAllByRequestIdOrderByCreatedAtAsc(Long requestId);
}