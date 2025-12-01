package com.syndico.syndicoapp.controllers;

import com.syndico.syndicoapp.dto.UserRegistrationDto;
import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

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
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/RegisterPage";
    }

    @PostMapping("/perform_register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) {

        // validation errors
        if (result.hasErrors()) {
            return "auth/RegisterPage";
        }

        // password match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user",
                    "Passwords do not match");
            return "auth/RegisterPage";
        }

        // email exists
        if (userService.existsByEmail(registrationDto.getEmail())) {
            result.rejectValue("email", "error.user",
                    "Email already registered");
            return "auth/RegisterPage";
        }

        try {
            // register user
            User newUser = userService.registerNewUser(registrationDto);

            // auto-login
            autoLogin(registrationDto.getEmail(), registrationDto.getPassword(), request, response);

            return "redirect:/redirect";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Registration failed: " + e.getMessage());
            return "redirect:/register?error";
        }
    }

    private void autoLogin(String email, String password,
                           HttpServletRequest request,
                           HttpServletResponse response) {

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, password);

            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            securityContextRepository.saveContext(securityContext, request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/ForgotPasswordPage";
    }
}
