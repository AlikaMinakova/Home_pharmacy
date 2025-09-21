package ru.home_pharmacy.home_pharmacy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название обязательно")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image")
    private String image;

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pharmacy> pharmacies = new HashSet<>();

    @NotNull(message = "Выберите хотя бы одну болезнь")
    @Size(min = 1, message = "Выберите хотя бы одну болезнь")
    @ManyToMany
    @JoinTable(
            name = "medication_disease",
            joinColumns = @JoinColumn(name = "medication_id"),
            inverseJoinColumns = @JoinColumn(name = "disease_id")
    )
    private Set<Disease> diseases = new HashSet<>();
}
