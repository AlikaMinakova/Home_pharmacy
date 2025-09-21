package ru.home_pharmacy.home_pharmacy.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseRequest;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyRequest;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyResponse;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
import ru.home_pharmacy.home_pharmacy.service.PharmacyService;

import java.util.List;

@Controller
@RequestMapping("/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final DiseaseService diseaseService;

    // список лекарств
    @GetMapping
    public String listMedications(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "8") int size,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "expirationDate") String sort,
                                  Model model) {
        model.addAttribute("pharmacyPage", pharmacyService.getPharmacyOverview(page, size, keyword, sort));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "pharmacy/list";
    }

    @GetMapping("/expired")
    public String listMedicationsForDelete(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "8") int size,
                                           Model model) {
        Page<PharmacyResponse> pharmacyPage = pharmacyService.getExpiredPharmacies(page, size);
        model.addAttribute("pharmacyPage", pharmacyPage);
        model.addAttribute("size", pharmacyPage.getTotalElements());
        return "pharmacy/listForDelete";
    }

    @GetMapping("/{id}/medications")
    public String listMedicationsByDisease(@PathVariable("id") Long id,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "6") int size,
                                           Model model) {
        Page<Medication> medications = pharmacyService.findByDiseaseId(id, page, size);
        Disease disease = pharmacyService.findDiseaseById(id);

        model.addAttribute("disease", disease);
        model.addAttribute("medicationsPage", medications);
        model.addAttribute("size", medications.getSize());

        return "pharmacy/listByDisease";
    }


    // страниуца просмотра
    @GetMapping("/detail/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        PharmacyResponse pharmacy = pharmacyService.getById(id);

        Medication medication = pharmacy.getMedication();

        model.addAttribute("medication", medication);
        model.addAttribute("pharmacy", pharmacy);

        return "pharmacy/detail";
    }

    // форма добавления лекарсва
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pharmacy", new PharmacyRequest());
        model.addAttribute("diseases", diseaseService.getAll());
        return "pharmacy/create";
    }

    @PostMapping()
    public String createMedication(
            @Valid @ModelAttribute("pharmacy") PharmacyRequest pharmacyRequest,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("diseases", diseaseService.getAll());
            return "pharmacy/create";
        }

        pharmacyService.create(pharmacyRequest);
        return "redirect:/pharmacies";
    }

    // форма редактирования лекарсва
    @GetMapping("/{id}")
    public String showUpdateForm(@PathVariable Long id,
                                 Model model) {
        PharmacyResponse p = pharmacyService.getById(id);
        model.addAttribute("pharmacy", pharmacyService.getById(id));
        model.addAttribute("diseases", diseaseService.getAll());
        return "pharmacy/update";
    }

    // сохранение формы редоктирования лекарства
    @PostMapping("/{id}")
    public String updateMedication(@PathVariable Long id,
                                   @Valid @ModelAttribute("pharmacy") PharmacyRequest pharmacyRequest,
                                   BindingResult bindingResult,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pharmacy", pharmacyRequest);
            model.addAttribute("diseases", diseaseService.getAll());
            return "pharmacy/update";
        }
        pharmacyService.update(id, pharmacyRequest);
        return "redirect:/pharmacies";
    }

    // удаление болезни
    @PostMapping("/{id}/delete")
    public String deleteDisease(@PathVariable Long id) {
        pharmacyService.deleteMedication(id);
        return "redirect:/pharmacies";
    }

    @PostMapping("/expired/delete-all")
    public String deleteAllExpired() {
        pharmacyService.deleteAllExpired();
        return "redirect:/pharmacies/expired";
    }
}
