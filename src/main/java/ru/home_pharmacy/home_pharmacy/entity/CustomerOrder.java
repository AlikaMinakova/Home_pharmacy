package ru.home_pharmacy.home_pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    /** Общий идентификатор заказа из корзины: несколько позиций объединяются в один заказ. */
    @Column(name = "order_group", length = 40)
    private String orderGroup;

    /**
     * Флаг уведомления покупателя: становится false, когда сотрудник меняет статус
     * (подтвердил/отклонил/выкупил), и покупатель видит счётчик в «Моих заказах».
     */
    @Builder.Default
    @Column(name = "buyer_notified")
    private Boolean buyerNotified = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}