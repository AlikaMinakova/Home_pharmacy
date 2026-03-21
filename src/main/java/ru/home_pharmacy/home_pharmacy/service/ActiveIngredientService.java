package ru.home_pharmacy.home_pharmacy.service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.dto.ActiveIngredientDto;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseDto;
import ru.home_pharmacy.home_pharmacy.entity.ActiveIngredient;
import ru.home_pharmacy.home_pharmacy.mapper.ActiveIngredientMapper;
import ru.home_pharmacy.home_pharmacy.repository.ActiveIngredientRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ActiveIngredientService {

    private final ActiveIngredientRepository activeIngredientRepository;
    private final ActiveIngredientMapper activeIngredientMapper;

    public List<ActiveIngredientDto> getAll() {
        return activeIngredientRepository.findAll()
                .stream()
                .map(activeIngredientMapper::toDto)
                .collect(Collectors.toList());
    }

    // Получить страницу веществ
    public Page<ActiveIngredientDto> getAllActiveIngredients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activeIngredientRepository.findAll(pageable)
                .map(activeIngredientMapper::toDto);
    }

    // Поиск по названию
    public Page<ActiveIngredientDto> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activeIngredientRepository.findByNameContaining(name, pageable)
                .map(activeIngredientMapper::toDto);
    }

    // Получить по ID
    public ActiveIngredientDto getActiveIngredient(Long id) {
        ActiveIngredient entity = activeIngredientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Действующее вещество с ID " + id + " не найдено"));
        return activeIngredientMapper.toDto(entity);
    }

    // Создать новое вещество
    public void createActiveIngredient(ActiveIngredientDto dto) {
        if (activeIngredientRepository.existsByName(dto.getName())) {
            throw new DataIntegrityViolationException("Вещество с таким названием уже существует");
        }
        ActiveIngredient entity = activeIngredientMapper.toEntity(dto);
        activeIngredientRepository.save(entity);
    }

    // Обновить вещество
    public void updateActiveIngredient(Long id, ActiveIngredientDto dto) {
        ActiveIngredient existing = activeIngredientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Вещество не найдено"));

        if (!existing.getName().equalsIgnoreCase(dto.getName()) &&
                activeIngredientRepository.existsByName(dto.getName())) {
            throw new DataIntegrityViolationException("Вещество с таким названием уже существует");
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        activeIngredientRepository.save(existing);
    }

    // Удалить вещество
    public void deleteActiveIngredient(Long id) {
        if (!activeIngredientRepository.existsById(id)) {
            throw new NoSuchElementException("Вещество не найдено");
        }
        activeIngredientRepository.deleteById(id);
    }
}