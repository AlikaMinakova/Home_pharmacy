package ru.home_pharmacy.home_pharmacy.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomResponse {
    private Long id;
    private String name;
}
