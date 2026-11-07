package ru.home_pharmacy.home_pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "archive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "retail_price", precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "written_off_at", nullable = false)
    private LocalDateTime writtenOffAt;

    /** Причина попадания в архив: WRITTEN_OFF (просрочено) или DELETED (удалено вручную). */
    @Builder.Default
    @Column(nullable = false, length = 20)
    private String reason = "WRITTEN_OFF";
}