package com.revature.revshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

        @Bean
        @Order(0)
        public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/actuator/**")
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/actuator/health").permitAll()
                                                .requestMatchers("/actuator/**").hasRole("ADMIN"))
                                .httpBasic(httpBasic -> {
                                });
                return http.build();
        }

        @Bean
        public InMemoryUserDetailsManager actuatorUserDetailsService() {
                var admin = User.withDefaultPasswordEncoder()
                                .username("admin")
                                .password("admin123")
                                .roles("ADMIN")
                                .build();
                return new InMemoryUserDetailsManager(admin);
        }
}
