package ru.home_pharmacy.home_pharmacy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
//@Table(name = "active_ingredient", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Table(name = "active_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Название действующего вещества обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "activeIngredients")
    private Set<Medication> medications = new HashSet<>();
}