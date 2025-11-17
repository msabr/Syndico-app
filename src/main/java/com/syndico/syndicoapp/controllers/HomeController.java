package com.syndico.syndicoapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";  // Retourne index.html
    }

    @GetMapping("/home")
    public String home() {
        return "index";  // Même page
    }

    @GetMapping("/about")
    public String about() {
        return "AboutUs";  // Retourne AboutUs.html
    }

    @GetMapping("/services")
    public String services() {
        return "Services";
    }

    @GetMapping("/faq")
    public String faq() {
        return "Faq";
    }

    @GetMapping("/contact")
    public String contact() {
        return "Contact";
    }
}
