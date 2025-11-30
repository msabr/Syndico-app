package com.syndico.syndicoapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}
