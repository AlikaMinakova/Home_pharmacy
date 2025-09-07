package ru.home_pharmacy.home_pharmacy.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class BaseController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/medicine")
    public String create_medicine() {
        return "medicine";
    }

    @GetMapping("/add-medicine")
    public String add_medicine() {
        return "add_medicine";
    }

    @GetMapping("/diseases")
    public String diseases() {
        return "diseases";
    }

    @GetMapping("/update-medicine")
    public String edit_medicine() {
        return "edit_medicine";
    }
}
