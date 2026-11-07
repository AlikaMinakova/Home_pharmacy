package ru.home_pharmacy.home_pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medication_name", nullable = false, length = 150)
    private String medicationName;

    @Column(name = "desired_quantity", nullable = false)
    private Integer desiredQuantity;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Builder.Default
    @Column(name = "comment_read", nullable = false)
    private Boolean commentRead = true;

    /** Прочитал ли аптекарь новые сообщения покупателя. */
    @Builder.Default
    @Column(name = "pharmacist_read", nullable = false)
    private Boolean pharmacistRead = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.NEW;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}