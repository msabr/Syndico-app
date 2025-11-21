package com.syndico.syndicoapp.config;

import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.models.enums.UserRole;
import com.syndico.syndicoapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Admin Creation
            User admin = User.builder()
                    .email("admin@syndico.ma")
                    .password(passwordEncoder.encode("admin@123"))
                    .firstName("Admin")
                    .lastName("Syndico")
                    .role(UserRole.ADMIN)
                    .isEmailVerified(true)
                    .preferredLanguage("EN")
                    .build();

            userRepository.save(admin);

            // Resident Creation
            User resident = User.builder()
                    .email("resident@test.ma")
                    .password(passwordEncoder.encode("resident@123"))
                    .firstName("Mohammed")
                    .lastName("Alami")
                    .role(UserRole.RESIDENT)
                    .isEmailVerified(true)
                    .preferredLanguage("EN")
                    .build();

            userRepository.save(resident);

            System.out.println("✅ Test User created :");
            System.out.println("   Admin: admin@syndico.ma / admin@123");
            System.out.println("   Resident: resident@test.ma / resident@123");
        }
    }
}
