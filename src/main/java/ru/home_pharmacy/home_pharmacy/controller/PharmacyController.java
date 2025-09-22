package ru.home_pharmacy.home_pharmacy.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.dto.PharmacyDto;
import ru.home_pharmacy.home_pharmacy.service.MedicationService;
import ru.home_pharmacy.home_pharmacy.service.PharmacyService;

@Controller
@RequestMapping("/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final MedicationService medicationService;

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
        Page<PharmacyDto> pharmacyPage = pharmacyService.getExpiredPharmacies(page, size);
        model.addAttribute("pharmacyPage", pharmacyPage);
        model.addAttribute("size", pharmacyPage.getTotalElements());
        return "pharmacy/listForDelete";
    }


    // страниуца просмотра
    @GetMapping("/detail/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        PharmacyDto pharmacy = pharmacyService.getById(id);

        MedicationDto medication = pharmacy.getMedication();

        model.addAttribute("medication", medication);
        model.addAttribute("pharmacy", pharmacy);

        return "pharmacy/detail";
    }

    // форма добавления лекарсва
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        PharmacyDto dto = new PharmacyDto();
        dto.setMedication(new MedicationDto());
        model.addAttribute("pharmacy", dto);
        model.addAttribute("medications", medicationService.getAll());
        return "pharmacy/create";
    }

    @PostMapping()
    public String createMedication(
            @Valid @ModelAttribute("pharmacy") PharmacyDto pharmacyDto,
            BindingResult bindingResult,
            @RequestParam Long medicationId,
            Model model) {

        pharmacyDto.setMedication(medicationService.getById(medicationId));
        if (bindingResult.hasErrors()) {
            model.addAttribute("pharmacy", pharmacyDto);
            model.addAttribute("medications", medicationService.getAll());
            return "pharmacy/create";
        }
        pharmacyService.create(pharmacyDto);
        return "redirect:/pharmacies";
    }

    // форма редактирования лекарсва
    @GetMapping("/{id}")
    public String showUpdateForm(@PathVariable Long id,
                                 Model model) {
        model.addAttribute("pharmacy", pharmacyService.getById(id));
        model.addAttribute("medications", medicationService.getAll());
        return "pharmacy/update";
    }

    // сохранение формы редоктирования лекарства
    @PostMapping("/{id}")
    public String updateMedication(@PathVariable Long id,
                                   @Valid @ModelAttribute("pharmacy") PharmacyDto pharmacyDto,
                                   BindingResult bindingResult,
                                   @RequestParam Long medicationId,
                                   Model model) {
        pharmacyDto.setMedication(medicationService.getById(medicationId));
        if (bindingResult.hasErrors()) {
            model.addAttribute("pharmacy", pharmacyDto);
            model.addAttribute("medications", medicationService.getAll());
            return "pharmacy/update";
        }
        pharmacyService.update(id, pharmacyDto);
        return "redirect:/pharmacies";
    }

    // удаление болезни
    @PostMapping("/{id}/delete")
    public String deleteDisease(@PathVariable Long id) {
        pharmacyService.delete(id);
        return "redirect:/pharmacies";
    }

    @PostMapping("/expired/delete-all")
    public String deleteAllExpired() {
        pharmacyService.deleteAllExpired();
        return "redirect:/pharmacies/expired";
    }
}
