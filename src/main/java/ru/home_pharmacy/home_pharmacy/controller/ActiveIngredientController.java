package ru.home_pharmacy.home_pharmacy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.ActiveIngredientDto;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.service.ActiveIngredientService;
import ru.home_pharmacy.home_pharmacy.service.MedicationService;

@Controller
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class ActiveIngredientController {

    @Autowired
    private MedicationService medicationService;
    private final ActiveIngredientService activeIngredientService;

    @GetMapping("/{id}/medications")
    public String medicationsByIngredient(@PathVariable Long id,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          Model model) {

        Page<MedicationDto> medicationsPage = medicationService.getMedicationsByActiveIngredientId(id, page, size);
        ActiveIngredientDto ingredient = activeIngredientService.getActiveIngredient(id);

        model.addAttribute("medicationsPage", medicationsPage);
        model.addAttribute("ingredient", ingredient);
        model.addAttribute("size", medicationsPage.getTotalElements());

        return "ingredient/medications";
    }
    // Список веществ с пагинацией
    @GetMapping
    public String listIngredients(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(required = false) String name,
                                  Model model) {

        Page<ActiveIngredientDto> ingredientsPage;

        if (name != null && !name.trim().isEmpty()) {
            ingredientsPage = activeIngredientService.searchByName(name, page, size);
        } else {
            ingredientsPage = activeIngredientService.getAllActiveIngredients(page, size);
        }

        model.addAttribute("ingredientsPage", ingredientsPage);
        model.addAttribute("currentName", name); // для сохранения поискового запроса в форме
        return "ingredient/list";
    }

    // Форма добавления
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("ingredient", new ActiveIngredientDto());
        return "ingredient/create";
    }

    // Сохранение нового вещества
    @PostMapping
    public String createIngredient(@Valid @ModelAttribute("ingredient") ActiveIngredientDto dto,
                                   BindingResult bindingResult,
                                   Model model) {

        if (bindingResult.hasErrors()) {
            return "ingredient/create";
        }

        try {
            activeIngredientService.createActiveIngredient(dto);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.ingredient", "Вещество с таким названием уже существует");
            return "ingredient/create";
        }

        return "redirect:/ingredients";
    }

    // Форма редактирования
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("ingredient", activeIngredientService.getActiveIngredient(id));
        return "ingredient/update";
    }

    // Обновление вещества
    @PostMapping("/{id}")
    public String updateIngredient(@PathVariable Long id,
                                   @Valid @ModelAttribute("ingredient") ActiveIngredientDto dto,
                                   BindingResult bindingResult,
                                   Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("ingredient", dto);
            return "ingredient/update";
        }

        try {
            activeIngredientService.updateActiveIngredient(id, dto);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.ingredient", "Вещество с таким названием уже существует");
            model.addAttribute("ingredient", dto);
            return "ingredient/update";
        }

        return "redirect:/ingredients";
    }

    // Удаление вещества
    @PostMapping("/{id}/delete")
    public String deleteIngredient(@PathVariable Long id) {
        activeIngredientService.deleteActiveIngredient(id);
        return "redirect:/ingredients";
    }
}
