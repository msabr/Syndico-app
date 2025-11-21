package com.syndico.syndicoapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model){

        if (error != null){
            model.addAttribute("error", "Incorrect email or password");
        }

        if (logout != null){
            model.addAttribute("message", "You have been successfully logged out");
        }

        return "auth/LoginPage";
    }

    @GetMapping("/register")
    public String register(){
        return "auth/RegisterPage";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/ForgotPasswordPage";
    }
}
