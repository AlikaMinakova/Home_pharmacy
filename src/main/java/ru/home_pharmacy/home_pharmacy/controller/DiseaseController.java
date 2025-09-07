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


@Controller
@RequestMapping("/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    @Autowired
    private final DiseaseService diseaseService;

    @GetMapping
    public String listDiseases(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        Page<DiseaseResponse> diseasesPage = diseaseService.getAllDiseases(page, size);
        model.addAttribute("diseasesPage", diseasesPage);
        return "disease/list";
    }
}
