package com.syndico.syndicoapp.config;

import com.syndico.syndicoapp.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ================= AUTHORIZATION =================
                .authorizeHttpRequests(auth -> auth

                        // ----------- PUBLIC PAGES -----------
                        .requestMatchers(
                                "/", "/home", "/about", "/services",
                                "/contact", "/faq", "/privacy", "/terms",
                                "/login", "/register", "/forgot-password",
                                "/verify-email", "/access-denied"
                        ).permitAll()

                        // ----------- STATIC RESOURCES -----------
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**",
                                "/assets/**", "/favicon.ico"
                        ).permitAll()

                        // ----------- PUBLIC APIs -----------
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/chatbot/**",
                                "/api/contact/**"
                        ).permitAll()

                        // ----------- ADMIN SPACE -----------
                        .requestMatchers("/admin/**", "/api/admin/**")
                        .hasRole("ADMIN")

                        // ----------- RESIDENT SPACE -----------
                        .requestMatchers("/client/**", "/api/client/**")
                        .hasRole("RESIDENT")

                        // ----------- EVERYTHING ELSE -----------
                        .anyRequest().authenticated()
                )

                // ================= LOGIN =================
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/redirect", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // ================= LOGOUT =================
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // ================= ACCESS DENIED =================
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )

                // ================= CSRF =================
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                );

        return http.build();
    }

    // ================= AUTH MANAGER =================
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder passwordEncoder) throws Exception {

        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        builder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return builder.build();
    }

    // ================= PASSWORD ENCODER =================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
