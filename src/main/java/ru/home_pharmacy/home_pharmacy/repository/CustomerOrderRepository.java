package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.home_pharmacy.home_pharmacy.entity.CustomerOrder;
import ru.home_pharmacy.home_pharmacy.entity.OrderStatus;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long>,
        JpaSpecificationExecutor<CustomerOrder> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    List<CustomerOrder> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<CustomerOrder> findAllByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    long countByStatus(OrderStatus status);

    long countByCustomerPhoneAndBuyerNotifiedFalse(String customerPhone);

    boolean existsByCustomerPhoneAndMedicationIdAndStatus(String customerPhone,
                                                          Long medicationId,
                                                          OrderStatus status);

    @Modifying
    @Query("UPDATE CustomerOrder o SET o.buyerNotified = true " +
            "WHERE o.customerPhone = :phone AND o.buyerNotified = false")
    void markAllNotified(@Param("phone") String customerPhone);

    void deleteByMedication_Id(Long medicationId);
}