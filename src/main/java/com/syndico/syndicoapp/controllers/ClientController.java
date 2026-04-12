package com.syndico.syndicoapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client")
public class ClientController {

    @GetMapping("")
    public String redirectToDashboard() {
        return "redirect:/client/dashboard";
    }
}

