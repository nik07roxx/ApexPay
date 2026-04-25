package com.nik07roxx.apexPay.config;

import com.nik07roxx.apexPay.Service.Implementation.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Automatically injects UserDetailsServiceImpl
public class SpringSecurity {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Registration/Login public
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/accounts/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/customers/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/transactions/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/transactions/**").hasAuthority("USER")
                        .anyRequest().authenticated() // "Secure by default"
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions/cookies
                )
                .authenticationProvider(authenticationProvider()) // Tell Spring to use your DB
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // 1. THE PASSWORD ENCRYPTOR
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. THE AUTHENTICATION PROVIDER (The Matcher)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Uses your custom DB logic
        authProvider.setUserDetailsService(userDetailsService);
        // Uses your BCrypt bean, injection internally managed by Spring
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 2. THE AUTHENTICATION MANAGER (Login Checker)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}