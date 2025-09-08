package ru.home_pharmacy.home_pharmacy.controller;

import jakarta.persistence.EntityNotFoundException;
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
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
import ru.home_pharmacy.home_pharmacy.service.PharmacyService;

import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final DiseaseService diseaseService;

    // список болезней
    @GetMapping
    public String listDiseases(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        model.addAttribute("pharmacyPage", pharmacyService.getPharmacyOverview(page, size));
        return "pharmacy/list";
    }


    @GetMapping("/detail/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        PharmacyResponse pharmacy = pharmacyService.getById(id);

        Medication medication = pharmacy.getMedication();

        model.addAttribute("medication", medication);
        model.addAttribute("pharmacy", pharmacy);

        return "pharmacy/detail";
    }

    // форма добавления болезни
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pharmacy", new PharmacyRequest());
        model.addAttribute("diseases", diseaseService.getAll());
        return "pharmacy/create";
    }

    // сохранение формы болезни
    @PostMapping()
    public String createDisease(@ModelAttribute("disease") PharmacyRequest pharmacyRequest) {
        pharmacyService.create(pharmacyRequest);
        return "redirect:/pharmacies";
    }
//
//
//    // обновление формы редактирования болезни
//    @PostMapping("/{id}")
//    public String updateDisease(@PathVariable Long id,
//                                @ModelAttribute DiseaseRequest diseaseRequest) {
//        diseaseService.updateDisease(id, diseaseRequest);
//        return "redirect:/diseases";
//    }

    // удаление болезни
    @PostMapping("/{id}/delete")
    public String deleteDisease(@PathVariable Long id) {
        pharmacyService.deleteMedication(id);
        return "redirect:/pharmacies";
    }
}
