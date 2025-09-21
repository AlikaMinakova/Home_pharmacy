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
import ru.home_pharmacy.home_pharmacy.dto.DiseaseRequest;
import ru.home_pharmacy.home_pharmacy.dto.DiseaseResponse;
import ru.home_pharmacy.home_pharmacy.dto.SymptomResponse;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Symptom;
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
        Page<DiseaseResponse> diseasesPage = diseaseService.getAllDiseases(page, size);
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

        SymptomResponse symptom = symptomService.getSymptom(id);

        model.addAttribute("diseasesPage", diseasesPage);
        model.addAttribute("symptom", symptom);
        model.addAttribute("size", diseasesPage.getTotalElements());

        return "disease/listBySymptom";
    }

    // форма добавления болезни
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("disease", new DiseaseRequest());
        model.addAttribute("symptoms", symptomService.getAllSymptoms());
        return "disease/create";
    }

    // сохранение формы болезни
    @PostMapping
    public String createDisease(@Valid @ModelAttribute("disease") DiseaseRequest diseaseRequest,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            return "disease/create";
        }

        try {
            diseaseService.createDisease(diseaseRequest);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.disease", "Болезнь с таким названием уже существует");
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            return "disease/create";
        }

        return "redirect:/diseases";
    }

    // сохранение формы болезни
//    @PostMapping()
//    public String createDisease(@ModelAttribute("disease") DiseaseRequest diseaseRequest) {
//        diseaseService.createDisease(diseaseRequest);
//        return "redirect:/diseases";
//    }

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
                                @Valid @ModelAttribute("disease") DiseaseRequest diseaseRequest,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            model.addAttribute("disease", diseaseRequest);
            return "disease/update";
        }

        try {
            diseaseService.updateDisease(id, diseaseRequest);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "error.disease", "Болезнь с таким названием уже существует");
            model.addAttribute("symptoms", symptomService.getAllSymptoms());
            model.addAttribute("disease", diseaseRequest);
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
