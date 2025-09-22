package ru.home_pharmacy.home_pharmacy.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseDto;
import ru.home_pharmacy.home_pharmacy.dto.SymptomDto;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
import org.springframework.ui.Model;
import ru.home_pharmacy.home_pharmacy.service.SymptomService;


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
        Page<DiseaseDto> diseasesPage = diseaseService.getAllDiseases(page, size);
        model.addAttribute("diseasesPage", diseasesPage);
        return "disease/list";
    }

    @GetMapping("/{id}/diseases")
    public String diseasesBySymptom(@PathVariable Long id,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Disease> diseasesPage = diseaseService.findBySymptomId(id, pageable);

        SymptomDto symptom = symptomService.getSymptom(id);

        model.addAttribute("diseasesPage", diseasesPage);
        model.addAttribute("symptom", symptom);
        model.addAttribute("size", diseasesPage.getTotalElements());

        return "disease/listBySymptom";
    }

    // форма добавления болезни
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("disease", new DiseaseDto());
        model.addAttribute("symptoms", symptomService.getAllSymptoms());
        return "disease/create";
    }

    // сохранение формы болезни
    @PostMapping
    public String createDisease(@Valid @ModelAttribute("disease") DiseaseDto diseaseDto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            return "disease/create";
        }

        try {
            diseaseService.createDisease(diseaseDto);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.disease", "Болезнь с таким названием уже существует");
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            return "disease/create";
        }

        return "redirect:/diseases";
    }

    // форма редактирования и просмотра болезни
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("disease", diseaseService.getDisease(id));
        model.addAttribute("symptoms", symptomService.getAllSymptoms());
        return "disease/update";
    }

    // обновление формы редактирования болезни
    @PostMapping("/{id}")
    public String updateDisease(@PathVariable Long id,
                                @Valid @ModelAttribute("disease") DiseaseDto diseaseDto,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            model.addAttribute("disease", diseaseDto);
            return "disease/update";
        }

        try {
            diseaseService.updateDisease(id, diseaseDto);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.disease", "Болезнь с таким названием уже существует");
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            model.addAttribute("disease", diseaseDto);
            return "disease/update";
        }

        return "redirect:/diseases";
    }

    // удаление болезни
    @PostMapping("/{id}/delete")
    public String deleteDisease(@PathVariable Long id) {
        diseaseService.deleteDisease(id);
        return "redirect:/diseases";
    }
}
