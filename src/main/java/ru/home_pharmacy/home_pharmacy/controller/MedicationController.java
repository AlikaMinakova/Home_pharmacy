package ru.home_pharmacy.home_pharmacy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
import ru.home_pharmacy.home_pharmacy.service.MedicationService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/medication")
@RequiredArgsConstructor
public class MedicationController {

    @Autowired
    private final MedicationService medicationService;
    @Autowired
    private final DiseaseService diseaseService;

    @GetMapping()
    public String getAll(Model model) {
        model.addAttribute("medications", medicationService.getAll());
        return "medication/list";
    }

    // форма добавления лекарсва
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("medication", new MedicationDto());
        model.addAttribute("diseases", diseaseService.getAll());
        return "medication/create";
    }

    @PostMapping()
    public String createMedication(
            @Valid @ModelAttribute("medication") MedicationDto medicationDto,
            BindingResult bindingResult,
            Model model,
            @RequestParam("file") MultipartFile file) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("diseases", diseaseService.getAll());
            return "medication/create";
        }
        try {
            medicationService.create(medicationDto, file);
        } catch (Exception e) {
            bindingResult.rejectValue("name", "name", "Лекарство с таким названием уже существует");
            model.addAttribute("diseases", diseaseService.getAll());
            return "medication/create";
        }

        return "redirect:/medication";
    }

    // форма редактирования лекарсва
    @GetMapping("/{id}")
    public String showUpdateForm(@PathVariable Long id,
                                 Model model) {
        model.addAttribute("medication", medicationService.getById(id));
        model.addAttribute("diseases", diseaseService.getAll());
        return "medication/update";
    }

    // сохранение формы редоктирования лекарства
    @PostMapping("/{id}")
    public String updateMedication(@PathVariable Long id,
                                   @Valid @ModelAttribute("medication") MedicationDto medicationDto,
                                   BindingResult bindingResult,
                                   @RequestParam("file") MultipartFile file,
                                   Model model) {
        MedicationDto existingMedication = medicationService.getById(id);
        medicationDto.setImage(existingMedication.getImage());
        if (bindingResult.hasErrors()) {
            model.addAttribute("medication", medicationDto);
            model.addAttribute("diseases", diseaseService.getAll());
            return "medication/update";
        }
        try {
            medicationService.update(id, medicationDto, file);
        } catch (Exception e) {
            bindingResult.rejectValue("name", "name", "Лекарство с таким названием уже существует");
            model.addAttribute("medication", medicationDto);
            model.addAttribute("diseases", diseaseService.getAll());
            return "medication/update";
        }

        return "redirect:/medication";
    }

    @GetMapping("/{id}/disease")
    public String listMedicationsByDisease(@PathVariable("id") Long id,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "6") int size,
                                           Model model) {
        List<Medication> medications = medicationService.findByDiseaseId(id, page, size);
        Optional<Disease> disease = diseaseService.findById(id);

        model.addAttribute("disease", disease.get());
        model.addAttribute("medicationsPage", medications);
        model.addAttribute("size", medications.size());

        return "medication/listByDisease";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        medicationService.delete(id);
        return "redirect:/medication";
    }

}
