package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseRequest;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseResponse;
import ru.home_pharmacy.home_pharmacy.dto.SymptomRequest;
import ru.home_pharmacy.home_pharmacy.dto.SymptomResponse;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
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
        Page<SymptomResponse> symptomsPage = symptomService.getPaginateSymptoms(page, size);
        model.addAttribute("symptomsPage", symptomsPage);
        return "symptom/list";
    }

    // форма добавления симптома
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("symptom", new SymptomRequest());
        return "symptom/create";
    }

    // сохранение формы симптома
    @PostMapping()
    public String createSymptom(@ModelAttribute("symptom") SymptomRequest symptomRequest) {
        symptomService.createSymptom(symptomRequest);
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
                                @ModelAttribute SymptomRequest symptomRequest) {
        symptomService.updateSymptom(id, symptomRequest);
        return "redirect:/symptoms";
    }

    // удаление симптомов
    @PostMapping("/{id}/delete")
    public String deleteSymptom(@PathVariable Long id) {
        symptomService.deleteSymptom(id);
        return "redirect:/symptoms";
    }
}
