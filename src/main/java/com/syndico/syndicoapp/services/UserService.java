package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.dto.UserRegistrationDto;
import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.models.enums.UserRole;
import com.syndico.syndicoapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //@Autowired
    //private EmailService emailService;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) {
        // Créer le nouvel utilisateur
        User user = User.builder()
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .email(registrationDto.getEmail())
                .phoneNumber(registrationDto.getPhoneNumber())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .role(UserRole.RESIDENT)
                .isEmailVerified(true)
                .emailVerificationToken(UUID.randomUUID().toString())
                .preferredLanguage("EN")
                .build();

        // Sauvegarder
        user = userRepository.save(user);

        // Envoyer email de vérification (optionnel)
        //emailService.sendVerificationEmail(user);

        return user;
    }
}
