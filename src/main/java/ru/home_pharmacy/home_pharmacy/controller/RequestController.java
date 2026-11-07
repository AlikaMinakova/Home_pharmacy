package ru.home_pharmacy.home_pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.home_pharmacy.home_pharmacy.entity.RequestStatus;
import ru.home_pharmacy.home_pharmacy.service.RequestService;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public String listRequests(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String status,
                               @RequestParam(defaultValue = "createdAt") String sortBy,
                               @RequestParam(defaultValue = "desc") String sortDir,
                               Model model) {
        model.addAttribute("requests", requestService.search(search, parseStatus(status), sortBy, sortDir));
        model.addAttribute("newCount", requestService.countNew());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        return "requests/list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        requestService.approve(id);
        return "redirect:/requests";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id) {
        requestService.reject(id);
        return "redirect:/requests";
    }

    private RequestStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return RequestStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}