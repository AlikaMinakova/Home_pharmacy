package ru.home_pharmacy.home_pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Избранный товар покупателя. Поле {@code notified} хранит, уведомили ли уже
 * покупателя о том, что товар снова появился в наличии. Когда товар пропадает
 * со склада, флаг сбрасывается, чтобы при следующем поступлении уведомить снова.
 */
@Entity
@Table(name = "favorite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Builder.Default
    @Column(nullable = false)
    private Boolean notified = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}