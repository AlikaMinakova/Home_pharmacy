package ru.home_pharmacy.home_pharmacy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.SymptomDto;
import ru.home_pharmacy.home_pharmacy.service.SymptomService;

@Controller
@RequestMapping("/symptoms")
@RequiredArgsConstructor
public class SymptomController {
    @Autowired
    private final SymptomService symptomService;

    // список симптомов
    @GetMapping
    public String listSymptoms(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        Page<SymptomDto> symptomsPage = symptomService.getPaginateSymptoms(page, size);
        model.addAttribute("symptomsPage", symptomsPage);
        return "symptom/list";
    }

    // форма добавления симптома
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("symptom", new SymptomDto());
        return "symptom/create";
    }

    // сохранение формы симптома
    @PostMapping()
    public String createSymptom(@Valid @ModelAttribute("symptom") SymptomDto symptomDto, BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "symptom/create";
        }
        try {
            symptomService.createSymptom(symptomDto);
        } catch (
                DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.symptom", "Симптом с таким названием уже существует");
            return "symptom/create";
        }
        return "redirect:/symptoms";
    }


    // форма редактирования и просмотра симптомов
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("symptom", symptomService.getSymptom(id));
        return "symptom/update";
    }

    // обновление формы редактирования симптомов
    @PostMapping("/{id}")
    public String updateSymptom(@PathVariable Long id,
                                @Valid @ModelAttribute("symptom") SymptomDto symptomDto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("symptom", symptomDto);
            return "symptom/update";
        }
        try {
            symptomService.updateSymptom(id, symptomDto);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.symptom", "Симптом с таким названием уже существует");
            model.addAttribute("symptom", symptomDto);
            return "symptom/update";
        }
        return "redirect:/symptoms";
    }

    // удаление симптомов
    @PostMapping("/{id}/delete")
    public String deleteSymptom(@PathVariable Long id) {
        symptomService.deleteSymptom(id);
        return "redirect:/symptoms";
    }
}
