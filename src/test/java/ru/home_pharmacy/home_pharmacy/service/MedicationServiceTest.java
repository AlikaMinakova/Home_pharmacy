package ru.home_pharmacy.home_pharmacy.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import ru.home_pharmacy.home_pharmacy.dto.ActiveIngredientDto;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseDto;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.entity.ActiveIngredient;
import ru.home_pharmacy.home_pharmacy.entity.Disease;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MedicationServiceTest {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private DiseaseService diseaseService;

    @Autowired
    private ActiveIngredientService activeIngredientService;

    private Long testDiseaseId;
    private Long testIngredientId;

    @BeforeEach
    void setUp() {
        // Создаём тестовую болезнь
        DiseaseDto diseaseDto = DiseaseDto.builder()
                .name("Тестовая болезнь_MedService_" + System.currentTimeMillis())
                .symptomIds(List.of())
                .build();
        testDiseaseId = diseaseService.createDisease(diseaseDto).getId();

        // Создаём тестовое вещество
        ActiveIngredientDto ingredientDto = new ActiveIngredientDto(null,
                "Тестовое вещество_MedService_" + System.currentTimeMillis(), "Описание");
        activeIngredientService.createActiveIngredient(ingredientDto);
        testIngredientId = activeIngredientService.getAll().stream()
                .filter(i -> i.getName().startsWith("Тестовое вещество_MedService_"))
                .findFirst().get().getId();
    }

    @Test
    void createAndGet_ShouldWork() {
        MedicationDto dto = new MedicationDto();
        dto.setName("Тест-лекарство_" + System.currentTimeMillis());
        dto.setDescription("Описание");
        dto.setDiseases(Set.of(Disease.builder().id(testDiseaseId).build()));
        dto.setActiveIngredients(Set.of(ActiveIngredient.builder().id(testIngredientId).build()));

        medicationService.create(dto, null);

        MedicationDto created = medicationService.getAll().stream()
                .filter(m -> m.getName().equals(dto.getName()))
                .findFirst().get();
        assertThat(created.getName()).isEqualTo(dto.getName());
        assertThat(created.getDiseases()).hasSize(1);
        assertThat(created.getActiveIngredients()).hasSize(1);
    }

    @Test
    void update_ShouldChangeName() {
        String originalName = "Старое лекарство_" + System.currentTimeMillis();
        MedicationDto dto = new MedicationDto();
        dto.setName(originalName);
        dto.setDiseases(Set.of(Disease.builder().id(testDiseaseId).build()));
        dto.setActiveIngredients(Set.of(ActiveIngredient.builder().id(testIngredientId).build()));
        medicationService.create(dto, null);

        Long id = medicationService.getAll().stream()
                .filter(m -> m.getName().equals(originalName))
                .findFirst().get().getId();

        String updatedName = "Обновлённое лекарство_" + System.currentTimeMillis();
        MedicationDto updateDto = new MedicationDto();
        updateDto.setName(updatedName);
        updateDto.setDiseases(Set.of(Disease.builder().id(testDiseaseId).build()));
        updateDto.setActiveIngredients(Set.of(ActiveIngredient.builder().id(testIngredientId).build()));
        medicationService.update(id, updateDto, null);

        MedicationDto updated = medicationService.getById(id);
        assertThat(updated.getName()).isEqualTo(updatedName);
    }

    @Test
    void delete_ShouldRemove() {
        String name = "Для удаления_" + System.currentTimeMillis();
        MedicationDto dto = new MedicationDto();
        dto.setName(name);
        dto.setDiseases(Set.of(Disease.builder().id(testDiseaseId).build()));
        dto.setActiveIngredients(Set.of(ActiveIngredient.builder().id(testIngredientId).build()));
        medicationService.create(dto, null);

        Long id = medicationService.getAll().stream()
                .filter(m -> m.getName().equals(name))
                .findFirst().get().getId();

        medicationService.delete(id);
        assertThat(medicationService.getById(id)).isNull();
    }

    @Test
    void searchByName_ShouldReturnPage() {
        String unique = "ПоискЛекарство_" + System.currentTimeMillis();
        MedicationDto dto = new MedicationDto();
        dto.setName(unique);
        dto.setDiseases(Set.of(Disease.builder().id(testDiseaseId).build()));
        dto.setActiveIngredients(Set.of(ActiveIngredient.builder().id(testIngredientId).build()));
        medicationService.create(dto, null);

        Page<MedicationDto> result = medicationService.searchByName(unique, 0, 10);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(unique);
    }

    @Test
    void pagination_ShouldWork() {
        String prefix = "ПагинацияЛекарств_" + System.currentTimeMillis();
        for (int i = 0; i < 15; i++) {
            MedicationDto dto = new MedicationDto();
            dto.setName(prefix + "_" + i);
            dto.setDiseases(Set.of(Disease.builder().id(testDiseaseId).build()));
            dto.setActiveIngredients(Set.of(ActiveIngredient.builder().id(testIngredientId).build()));
            medicationService.create(dto, null);
        }

        Page<MedicationDto> page = medicationService.getAllMedications(0, 10);
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(15);
    }
}