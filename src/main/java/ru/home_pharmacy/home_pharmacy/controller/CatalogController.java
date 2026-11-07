package ru.home_pharmacy.home_pharmacy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.home_pharmacy.home_pharmacy.dto.CatalogMedicationDto;
import ru.home_pharmacy.home_pharmacy.dto.MedicationDto;
import ru.home_pharmacy.home_pharmacy.dto.OrderDto;
import ru.home_pharmacy.home_pharmacy.dto.RequestDto;
import ru.home_pharmacy.home_pharmacy.entity.Role;
import ru.home_pharmacy.home_pharmacy.entity.User;
import ru.home_pharmacy.home_pharmacy.service.ActiveIngredientService;
import ru.home_pharmacy.home_pharmacy.service.CatalogService;
import ru.home_pharmacy.home_pharmacy.service.DiseaseService;
import ru.home_pharmacy.home_pharmacy.service.MedicationService;
import ru.home_pharmacy.home_pharmacy.service.OrderService;
import ru.home_pharmacy.home_pharmacy.service.RequestService;
import ru.home_pharmacy.home_pharmacy.service.SymptomService;
import ru.home_pharmacy.home_pharmacy.service.UserService;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final MedicationService medicationService;
    private final DiseaseService diseaseService;
    private final SymptomService symptomService;
    private final ActiveIngredientService activeIngredientService;
    private final OrderService orderService;
    private final RequestService requestService;
    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "8") int size,
                       @RequestParam(required = false) String name,
                       Model model) {
        Page<CatalogMedicationDto> medications;
        if (name != null && !name.trim().isEmpty()) {
            medications = catalogService.searchByName(name, page, size);
        } else {
            medications = catalogService.findAll(page, size);
        }
        model.addAttribute("medicationsPage", medications);
        model.addAttribute("currentName", name);
        return "catalog/list";
    }

    @GetMapping("/medication/{id}")
    public String detail(@PathVariable Long id, Model model) {
        MedicationDto medication = medicationService.getById(id);
        if (medication == null) {
            return "redirect:/catalog";
        }
        model.addAttribute("medication", medication);
        model.addAttribute("availableQuantity", catalogService.getAvailableQuantity(id));
        return "catalog/detail";
    }

    // ==== Просмотр каталога по справочникам ====

    @GetMapping("/diseases")
    public String diseases(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String name,
                           Model model) {
        var result = (name != null && !name.trim().isEmpty())
                ? diseaseService.searchByName(name, page, size)
                : diseaseService.getAllDiseases(page, size);
        model.addAttribute("diseasesPage", result);
        model.addAttribute("currentName", name);
        return "catalog/diseases";
    }

    @GetMapping("/disease/{id}")
    public String byDisease(@PathVariable Long id,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "8") int size,
                            Model model) {
        model.addAttribute("medicationsPage", catalogService.findByDisease(id, page, size));
        model.addAttribute("disease", diseaseService.findById(id).orElse(null));
        model.addAttribute("baseUrl", "/catalog/disease/" + id);
        return "catalog/medicationsBy";
    }

    @GetMapping("/symptoms")
    public String symptoms(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String name,
                           Model model) {
        var result = (name != null && !name.trim().isEmpty())
                ? symptomService.searchByName(name, page, size)
                : symptomService.getPaginateSymptoms(page, size);
        model.addAttribute("symptomsPage", result);
        model.addAttribute("currentName", name);
        return "catalog/symptoms";
    }

    @GetMapping("/symptom/{id}")
    public String bySymptom(@PathVariable Long id,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "8") int size,
                            Model model) {
        model.addAttribute("medicationsPage", catalogService.findBySymptom(id, page, size));
        model.addAttribute("symptom", symptomService.getSymptom(id));
        model.addAttribute("baseUrl", "/catalog/symptom/" + id);
        return "catalog/medicationsBy";
    }

    @GetMapping("/ingredients")
    public String ingredients(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String name,
                              Model model) {
        var result = (name != null && !name.trim().isEmpty())
                ? activeIngredientService.searchByName(name, page, size)
                : activeIngredientService.getAllActiveIngredients(page, size);
        model.addAttribute("ingredientsPage", result);
        model.addAttribute("currentName", name);
        return "catalog/ingredients";
    }

    @GetMapping("/ingredient/{id}")
    public String byIngredient(@PathVariable Long id,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               Model model) {
        model.addAttribute("medicationsPage", catalogService.findByIngredient(id, page, size));
        model.addAttribute("ingredient", activeIngredientService.getActiveIngredient(id));
        model.addAttribute("baseUrl", "/catalog/ingredient/" + id);
        return "catalog/medicationsBy";
    }

    // ==== Оформление заказа ====

    @GetMapping("/order")
    public String orderForm(@RequestParam(required = false) Long medicationId, Model model) {
        OrderDto dto = new OrderDto();
        dto.setMedicationId(medicationId);
        prefillCustomer(dto);
        model.addAttribute("order", dto);
        CatalogMedicationDto medication = medicationId != null ? catalogService.getById(medicationId) : null;
        model.addAttribute("medication", medication);
        model.addAttribute("availableQuantity",
                medication != null ? catalogService.getAvailableQuantity(medicationId) : null);
        model.addAttribute("isCustomer", currentCustomer() != null);
        return "catalog/order";
    }

    @PostMapping("/order")
    public String submitOrder(@Valid @ModelAttribute("order") OrderDto orderDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        User customer = currentCustomer();
        if (customer != null) {
            orderDto.setCustomerName(customer.getName());
            orderDto.setCustomerPhone(customer.getUsername());
        }

        // нельзя заказать больше, чем есть на складе
        if (!bindingResult.hasErrors() && orderDto.getMedicationId() != null && orderDto.getQuantity() != null) {
            Long available = catalogService.getAvailableQuantity(orderDto.getMedicationId());
            if (orderDto.getQuantity() > available) {
                bindingResult.rejectValue("quantity", "quantity",
                        "На складе доступно только " + available + " шт");
            }
        }

        if (bindingResult.hasErrors()) {
            CatalogMedicationDto medication =
                    orderDto.getMedicationId() != null ? catalogService.getById(orderDto.getMedicationId()) : null;
            model.addAttribute("medication", medication);
            model.addAttribute("availableQuantity",
                    medication != null ? catalogService.getAvailableQuantity(orderDto.getMedicationId()) : null);
            return "catalog/order";
        }
        orderService.createOrder(orderDto);
        redirectAttributes.addFlashAttribute("successMessage", "Заказ оформлен! Заберите лекарство в аптечном пункте.");
        return "redirect:/catalog";
    }

    // ==== Заявка на отсутствующее лекарство ====

    @GetMapping("/request")
    public String requestForm(Model model) {
        RequestDto dto = new RequestDto();
        User customer = currentCustomer();
        if (customer != null) {
            dto.setCustomerName(customer.getName());
            dto.setCustomerPhone(customer.getUsername());
        }
        model.addAttribute("request", dto);
        model.addAttribute("isCustomer", customer != null);
        return "catalog/request";
    }

    @PostMapping("/request")
    public String submitRequest(@Valid @ModelAttribute("request") RequestDto requestDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User customer = currentCustomer();
        if (customer != null) {
            requestDto.setCustomerName(customer.getName());
            requestDto.setCustomerPhone(customer.getUsername());
        }
        if (bindingResult.hasErrors()) {
            return "catalog/request";
        }
        requestService.createRequest(requestDto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Заявка отправлена! Мы учтём её при следующей поставке.");
        return "redirect:/catalog";
    }

    // ==== Авторизованный покупатель ====

    private void prefillCustomer(OrderDto dto) {
        User customer = currentCustomer();
        if (customer != null) {
            dto.setCustomerName(customer.getName());
            dto.setCustomerPhone(customer.getUsername());
        }
    }

    private User currentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        User user = userService.findByUsername(username).orElse(null);
        return user != null && user.getRole() == Role.CUSTOMER ? user : null;
    }
}