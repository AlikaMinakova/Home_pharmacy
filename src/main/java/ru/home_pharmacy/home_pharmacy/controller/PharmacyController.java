package ru.home_pharmacy.home_pharmacy.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyRequest;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyResponse;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
import ru.home_pharmacy.home_pharmacy.service.PharmacyService;

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

    // сохранение формы лекарства
    @PostMapping()
    public String createMedication(@ModelAttribute("pharmacy") PharmacyRequest pharmacyRequest) {
        pharmacyService.create(pharmacyRequest);
        return "redirect:/pharmacies";
    }


    // форма редактирования лекарсва
    @GetMapping("/{id}")
    public String showUpdateForm(@PathVariable Long id,
                                 Model model) {
        PharmacyResponse p = pharmacyService.getById(id);
        model.addAttribute("pharmacy", pharmacyService.getById(id));
        System.out.println(p.getExpirationDate());
        System.out.println(p.getPurchaseDate());

        model.addAttribute("diseases", diseaseService.getAll());
        return "pharmacy/update";
    }

    // сохранение формы редоктирования лекарства
    @PostMapping("/{id}")
    public String updateMedication(@PathVariable Long id,
                                   @ModelAttribute("pharmacy") PharmacyRequest pharmacyRequest) {
        pharmacyService.update(id, pharmacyRequest);
        return "redirect:/pharmacies";
    }

    // удаление болезни
    @PostMapping("/{id}/delete")
    public String deleteDisease(@PathVariable Long id) {
        pharmacyService.deleteMedication(id);
        return "redirect:/pharmacies";
    }
}
