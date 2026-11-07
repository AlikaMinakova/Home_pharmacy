package ru.home_pharmacy.home_pharmacy.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import ru.home_pharmacy.home_pharmacy.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PharmacyServiceTest {

    @Autowired
    private PharmacyService pharmacyService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private DiseaseService diseaseService;

    @Autowired
    private ActiveIngredientService activeIngredientService;

    private Long testMedicationId;

    @BeforeEach
    void setUp() {
        // Создаём болезнь и вещество для лекарства
        DiseaseDto disease = diseaseService.createDisease(DiseaseDto.builder()
                .name("Тестовая болезнь_Pharmacy_" + System.currentTimeMillis())
                .symptomIds(List.of())
                .build());
        ActiveIngredientDto ingredient = new ActiveIngredientDto(null,
                "Тестовое вещество_Pharmacy_" + System.currentTimeMillis(), "Описание");
        activeIngredientService.createActiveIngredient(ingredient);
        Long ingredientId = activeIngredientService.getAll().stream()
                .filter(i -> i.getName().startsWith("Тестовое вещество_Pharmacy_"))
                .findFirst().get().getId();

        // Создаём лекарство
        MedicationDto medicationDto = new MedicationDto();
        medicationDto.setName("Тест-лекарство_Pharmacy_" + System.currentTimeMillis());
        medicationDto.setDescription("Описание");
        medicationDto.setDiseases(Set.of(ru.home_pharmacy.home_pharmacy.entity.Disease.builder().id(disease.getId()).build()));
        medicationDto.setActiveIngredients(Set.of(ru.home_pharmacy.home_pharmacy.entity.ActiveIngredient.builder().id(ingredientId).build()));
        medicationService.create(medicationDto, null);
        testMedicationId = medicationService.getAll().stream()
                .filter(m -> m.getName().startsWith("Тест-лекарство_Pharmacy_"))
                .findFirst().get().getId();
    }


    @Test
    void delete_ShouldRemove() {
        PharmacyDto dto = PharmacyDto.builder()
                .medication(ru.home_pharmacy.home_pharmacy.dto.MedicationDto.builder().id(testMedicationId).build())
                .quantity(1)
                .purchaseDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(1))
                .build();
        PharmacyDto created = pharmacyService.create(dto);

        pharmacyService.delete(created.getId());
        assertThrows(RuntimeException.class, () -> pharmacyService.getById(created.getId()));
    }

    @Test
    void getAllMedications_ShouldReturnPage() {
        // Создаём несколько записей в аптеке
        for (int i = 0; i < 5; i++) {
            PharmacyDto dto = PharmacyDto.builder()
                    .medication(ru.home_pharmacy.home_pharmacy.dto.MedicationDto.builder().id(testMedicationId).build())
                    .quantity(i + 1)
                    .purchaseDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusYears(1))
                    .build();
            pharmacyService.create(dto);
        }

        Page<PharmacyDto> page = pharmacyService.getAllMedications(0, 3);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void getPharmacyOverview_ShouldWork() {
        // Создаём запись, купленную на этой неделе
        PharmacyDto dto = PharmacyDto.builder()
                .medication(ru.home_pharmacy.home_pharmacy.dto.MedicationDto.builder().id(testMedicationId).build())
                .quantity(10)
                .purchaseDate(LocalDate.now().minusDays(2))
                .expirationDate(LocalDate.now().plusMonths(6))
                .build();
        pharmacyService.create(dto);

        PharmacyOverviewResponse overview = pharmacyService.getPharmacyOverview(0, 10, null, "purchaseDate");
        assertThat(overview.getAll().getContent()).isNotEmpty();
        assertThat(overview.getRecentlyBought()).isNotEmpty();
    }
}