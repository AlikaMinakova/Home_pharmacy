package ru.home_pharmacy.home_pharmacy.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import ru.home_pharmacy.home_pharmacy.dto.ActiveIngredientDto;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ActiveIngredientServiceTest {

    @Autowired
    private ActiveIngredientService service;

    @Test
    void createAndGet_ShouldWork() {
        ActiveIngredientDto dto = new ActiveIngredientDto(null, "Тестовое вещество_" + System.currentTimeMillis(), "Описание");
        service.createActiveIngredient(dto);

        // Получаем все и ищем созданное
        var all = service.getAll();
        assertThat(all).anyMatch(i -> i.getName().equals(dto.getName()));
    }

    @Test
    void createDuplicate_ShouldThrow() {
        String uniqueName = "Уникальное_" + System.currentTimeMillis();
        ActiveIngredientDto dto = new ActiveIngredientDto(null, uniqueName, "Описание");
        service.createActiveIngredient(dto);

        ActiveIngredientDto duplicate = new ActiveIngredientDto(null, uniqueName, "Другое описание");
        assertThrows(DataIntegrityViolationException.class, () -> service.createActiveIngredient(duplicate));
    }

    @Test
    void update_ShouldChangeName() {
        String originalName = "Старое_" + System.currentTimeMillis();
        ActiveIngredientDto dto = new ActiveIngredientDto(null, originalName, "Описание");
        service.createActiveIngredient(dto);
        Long id = service.getAll().stream().filter(i -> i.getName().equals(originalName)).findFirst().get().getId();

        ActiveIngredientDto updateDto = new ActiveIngredientDto(id, "Новое_" + System.currentTimeMillis(), "Описание");
        service.updateActiveIngredient(id, updateDto);

        ActiveIngredientDto updated = service.getActiveIngredient(id);
        assertThat(updated.getName()).isEqualTo(updateDto.getName());
    }

    @Test
    void delete_ShouldRemove() {
        String name = "Для удаления_" + System.currentTimeMillis();
        ActiveIngredientDto dto = new ActiveIngredientDto(null, name, "Описание");
        service.createActiveIngredient(dto);
        Long id = service.getAll().stream().filter(i -> i.getName().equals(name)).findFirst().get().getId();

        service.deleteActiveIngredient(id);
        assertThrows(NoSuchElementException.class, () -> service.getActiveIngredient(id));
    }

    @Test
    void pagination_ShouldWork() {
        // Создаём несколько записей (имена с префиксом для поиска)
        String prefix = "Пагинация_" + System.currentTimeMillis();
        for (int i = 0; i < 15; i++) {
            service.createActiveIngredient(new ActiveIngredientDto(null, prefix + "_" + i, "Описание"));
        }

        Page<ActiveIngredientDto> page = service.getAllActiveIngredients(0, 10);
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(15);
    }

    @Test
    void searchByName_ShouldReturnFiltered() {
        String unique = "Поиск_" + System.currentTimeMillis();
        service.createActiveIngredient(new ActiveIngredientDto(null, unique + "_A", "Описание"));
        service.createActiveIngredient(new ActiveIngredientDto(null, unique + "_B", "Описание"));

        Page<ActiveIngredientDto> result = service.searchByName(unique, 0, 10);
        assertThat(result.getContent()).hasSize(2);
    }
}