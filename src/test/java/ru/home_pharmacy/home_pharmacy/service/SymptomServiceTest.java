package ru.home_pharmacy.home_pharmacy.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import ru.home_pharmacy.home_pharmacy.dto.SymptomDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class SymptomServiceTest {

    @Autowired
    private SymptomService symptomService;

    @Test
    void createAndGet_ShouldWork() {
        SymptomDto dto = new SymptomDto(null, "Тестовый симптом_" + System.currentTimeMillis());
        SymptomDto created = symptomService.createSymptom(dto);

        SymptomDto found = symptomService.getSymptom(created.getId());
        assertThat(found.getName()).isEqualTo(dto.getName());
    }

    @Test
    void update_ShouldChangeName() {
        SymptomDto dto = new SymptomDto(null, "Старый симптом_" + System.currentTimeMillis());
        SymptomDto created = symptomService.createSymptom(dto);

        SymptomDto updateDto = new SymptomDto(created.getId(), "Новый симптом_" + System.currentTimeMillis());
        symptomService.updateSymptom(created.getId(), updateDto);

        SymptomDto updated = symptomService.getSymptom(created.getId());
        assertThat(updated.getName()).isEqualTo(updateDto.getName());
    }

    @Test
    void delete_ShouldRemove() {
        SymptomDto dto = new SymptomDto(null, "Для удаления_" + System.currentTimeMillis());
        SymptomDto created = symptomService.createSymptom(dto);

        symptomService.deleteSymptom(created.getId());
        assertThrows(RuntimeException.class, () -> symptomService.getSymptom(created.getId()));
    }

    @Test
    void paginationAndSearch_ShouldWork() {
        String prefix = "Симптом_" + System.currentTimeMillis();
        for (int i = 0; i < 12; i++) {
            symptomService.createSymptom(new SymptomDto(null, prefix + "_" + i));
        }

        Page<SymptomDto> page = symptomService.getPaginateSymptoms(0, 5);
        assertThat(page.getContent()).hasSize(5);

        Page<SymptomDto> searchResult = symptomService.searchByName(prefix, 0, 10);
        assertThat(searchResult.getTotalElements()).isGreaterThanOrEqualTo(12);
    }
}