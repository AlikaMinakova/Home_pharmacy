package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseRequest;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseResponse;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyRequest;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyResponse;
import ru.home_pharmacy.home_pharmacy.service.PharmacyService;

import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;

    // список болезней
    @GetMapping
    public String listDiseases(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        model.addAttribute("pharmacyPage", pharmacyService.getPharmacyOverview(page, size));
        return "pharmacy/list";
    }
//
//    // форма добавления болезни
//    @GetMapping("/new")
//    public String showCreateForm(Model model) {
//        model.addAttribute("disease", new DiseaseRequest());
//        model.addAttribute("symptoms", symptomService.getAllSymptoms());
//        return "disease/create";
//    }
//
//    // сохранение формы болезни
//    @PostMapping()
//    public String createDisease(@ModelAttribute("disease") DiseaseRequest diseaseRequest) {
//        diseaseService.createDisease(diseaseRequest);
//        return "redirect:/diseases";
//    }
//
//    // форма редактирования и просмотра болезни
//    @GetMapping("/{id}/edit")
//    public String showEditForm(@PathVariable Long id, Model model) {
//        model.addAttribute("disease", diseaseService.getDisease(id));
//        model.addAttribute("symptoms", symptomService.getAllSymptoms());
//        return "disease/update";
//    }
//
//    // обновление формы редактирования болезни
//    @PostMapping("/{id}")
//    public String updateDisease(@PathVariable Long id,
//                                @ModelAttribute DiseaseRequest diseaseRequest) {
//        diseaseService.updateDisease(id, diseaseRequest);
//        return "redirect:/diseases";
//    }
//
//    // удаление болезни
//    @PostMapping("/{id}/delete")
//    public String deleteDisease(@PathVariable Long id) {
//        diseaseService.deleteDisease(id);
//        return "redirect:/diseases";
//    }
}
