package com.revature.revshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

        @Bean
        @Order(0)
        public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {

                // Create in-memory user only for actuator endpoints
                InMemoryUserDetailsManager actuatorUsers = new InMemoryUserDetailsManager(
                                User.withUsername("admin")
                                                .password("admin123")
                                                .roles("ADMIN")
                                                .build());

                DaoAuthenticationProvider actuatorAuthProvider = new DaoAuthenticationProvider();
                actuatorAuthProvider.setUserDetailsService(actuatorUsers);
                actuatorAuthProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());

                AuthenticationManager actuatorAuthManager = new ProviderManager(actuatorAuthProvider);

                http
                                .securityMatcher("/actuator/**")
                                .authenticationManager(actuatorAuthManager)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/actuator/health").permitAll()
                                                .requestMatchers("/actuator/**").hasRole("ADMIN"))
                                .httpBasic(httpBasic -> {
                                });
                return http.build();
        }
}
