package ru.home_pharmacy.home_pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private CustomerRequest request;

    /** Отправитель: CUSTOMER (покупатель) или PHARMACIST (аптекарь). */
    @Column(nullable = false, length = 20)
    private String sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}