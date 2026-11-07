package ru.home_pharmacy.home_pharmacy.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class DiseaseServiceTest {

    @Autowired
    private DiseaseService diseaseService;

    @Test
    void createAndGet_ShouldWork() {
        DiseaseDto dto = DiseaseDto.builder()
                .name("Тестовая болезнь_" + System.currentTimeMillis())
                .description("Описание")
                .symptomIds(List.of())
                .build();
        DiseaseDto created = diseaseService.createDisease(dto);

        DiseaseDto found = diseaseService.getDisease(created.getId());
        assertThat(found.getName()).isEqualTo(dto.getName());
    }

    @Test
    void getAll_ShouldReturnList() {
        String prefix = "Список_" + System.currentTimeMillis();
        diseaseService.createDisease(DiseaseDto.builder().name(prefix + "_1").symptomIds(List.of()).build());
        diseaseService.createDisease(DiseaseDto.builder().name(prefix + "_2").symptomIds(List.of()).build());

        List<DiseaseDto> all = diseaseService.getAll();
        assertThat(all.stream().filter(d -> d.getName().startsWith(prefix))).hasSize(2);
    }

    @Test
    void update_ShouldChangeName() {
        DiseaseDto dto = DiseaseDto.builder()
                .name("Старая болезнь_" + System.currentTimeMillis())
                .symptomIds(List.of())
                .build();
        DiseaseDto created = diseaseService.createDisease(dto);

        DiseaseDto updateDto = DiseaseDto.builder()
                .id(created.getId())
                .name("Обновлённая болезнь_" + System.currentTimeMillis())
                .symptomIds(List.of())
                .build();
        diseaseService.updateDisease(created.getId(), updateDto);

        DiseaseDto updated = diseaseService.getDisease(created.getId());
        assertThat(updated.getName()).isEqualTo(updateDto.getName());
    }

    @Test
    void delete_ShouldRemove() {
        DiseaseDto dto = DiseaseDto.builder()
                .name("Для удаления_" + System.currentTimeMillis())
                .symptomIds(List.of())
                .build();
        DiseaseDto created = diseaseService.createDisease(dto);

        diseaseService.deleteDisease(created.getId());
        assertThrows(RuntimeException.class, () -> diseaseService.getDisease(created.getId()));
    }

    @Test
    void paginationAndSearch_ShouldWork() {
        String prefix = "Болезнь_" + System.currentTimeMillis();
        for (int i = 0; i < 12; i++) {
            diseaseService.createDisease(DiseaseDto.builder().name(prefix + "_" + i).symptomIds(List.of()).build());
        }

        Page<DiseaseDto> page = diseaseService.getAllDiseases(0, 5);
        assertThat(page.getContent()).hasSize(5);

        Page<DiseaseDto> searchResult = diseaseService.searchByName(prefix, 0, 10);
        assertThat(searchResult.getTotalElements()).isGreaterThanOrEqualTo(12);
    }
}