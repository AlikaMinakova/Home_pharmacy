package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.home_pharmacy.home_pharmacy.entity.CustomerOrder;
import ru.home_pharmacy.home_pharmacy.entity.OrderStatus;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long>,
        JpaSpecificationExecutor<CustomerOrder> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    List<CustomerOrder> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<CustomerOrder> findAllByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    long countByStatus(OrderStatus status);

    void deleteByMedication_Id(Long medicationId);
}