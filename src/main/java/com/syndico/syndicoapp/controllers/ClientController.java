package com.syndico.syndicoapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/client")
public class ClientController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "client/dashboard";
    }

}
