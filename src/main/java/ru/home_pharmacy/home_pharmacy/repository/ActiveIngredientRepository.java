package ru.home_pharmacy.home_pharmacy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.home_pharmacy.home_pharmacy.entity.ActiveIngredient;

@Repository
public interface ActiveIngredientRepository extends JpaRepository<ActiveIngredient, Long> {

    // Поиск по названию (частичное совпадение, регистронезависимое)
    @Query("SELECT ai FROM ActiveIngredient ai WHERE LOWER(ai.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ActiveIngredient> findByNameContaining(@Param("name") String name, Pageable pageable);

    // Проверка уникальности имени (опционально, можно использовать на уровне сервиса)
    boolean existsByName(String name);
}