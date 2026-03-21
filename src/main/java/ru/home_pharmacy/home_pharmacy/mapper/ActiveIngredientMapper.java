package ru.home_pharmacy.home_pharmacy.mapper;

import org.springframework.stereotype.Component;
import ru.home_pharmacy.home_pharmacy.entity.ActiveIngredient;
import ru.home_pharmacy.home_pharmacy.dto.ActiveIngredientDto;

@Component
public class ActiveIngredientMapper {

    public ActiveIngredientDto toDto(ActiveIngredient entity) {
        if (entity == null) return null;

        return ActiveIngredientDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public ActiveIngredient toEntity(ActiveIngredientDto dto) {
        if (dto == null) return null;

        ActiveIngredient entity = new ActiveIngredient();
        entity.setId(dto.getId()); // может быть null при создании
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
