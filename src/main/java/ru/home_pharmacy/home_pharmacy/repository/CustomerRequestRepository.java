package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.home_pharmacy.home_pharmacy.entity.CustomerRequest;
import ru.home_pharmacy.home_pharmacy.entity.RequestStatus;

import java.util.List;

public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, Long>,
        JpaSpecificationExecutor<CustomerRequest> {
    List<CustomerRequest> findAllByOrderByCreatedAtDesc();

    List<CustomerRequest> findAllByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    long countByStatus(RequestStatus status);

    long countByCustomerPhoneAndCommentReadFalse(String customerPhone);

    long countByPharmacistReadFalse();
}