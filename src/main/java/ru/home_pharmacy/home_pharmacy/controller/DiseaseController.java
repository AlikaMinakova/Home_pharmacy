package ru.home_pharmacy.home_pharmacy.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseRequest;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseResponse;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
import org.springframework.ui.Model;
import ru.home_pharmacy.home_pharmacy.service.SymptomService;

import java.security.Principal;


@Controller
@RequestMapping("/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    @Autowired
    private final DiseaseService diseaseService;
    @Autowired
    private final SymptomService symptomService;

    // список болезней
    @GetMapping
    public String listDiseases(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        Page<DiseaseResponse> diseasesPage = diseaseService.getAllDiseases(page, size);
        model.addAttribute("diseasesPage", diseasesPage);
        return "disease/list";
    }

    // форма добавления болезни
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("disease", new DiseaseRequest());
        model.addAttribute("symptoms", symptomService.getAllSymptoms());
        return "disease/create";
    }

    // сохранение формы болезни
    @PostMapping()
    public String createDisease(@ModelAttribute("disease") DiseaseRequest diseaseRequest) {
        diseaseService.createDisease(diseaseRequest);
        return "redirect:/diseases";
    }

    // форма редактирования и просмотра болезни
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("disease", diseaseService.getDisease(id));
        return "disease/list"; // diseases/edit.html
    }
    // обновление формы редактирования болезни
    @PostMapping("/{id}")
    public String updateDisease(@PathVariable Long id,
                                @ModelAttribute DiseaseRequest diseaseRequest) {
        diseaseService.updateDisease(id, diseaseRequest);
        return "redirect:/diseases";
    }

    // удаление болезни
    @PostMapping("/{id}/delete")
    public String deleteDisease(@PathVariable Long id) {
        diseaseService.deleteDisease(id);
        return "redirect:/diseases";
    }
}
