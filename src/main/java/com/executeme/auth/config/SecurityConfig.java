package com.executeme.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_DOCS = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AdminProperties adminProperties) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers(
                "/broker/angel/callback",
                "/broker/kite/login",
                "/broker/kite/callback",
                "/admin/**"
        ));

        if (adminProperties.enabled()) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_DOCS).permitAll()
                    .requestMatchers("/broker/angel/callback", "/broker/kite/login",
                            "/broker/kite/callback", "/actuator/health").permitAll()
                    .requestMatchers("/admin/**").authenticated()
                    .anyRequest().denyAll()
            );
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_DOCS).permitAll()
                    .requestMatchers("/broker/angel/callback", "/broker/kite/login",
                            "/broker/kite/callback", "/actuator/health").permitAll()
                    .requestMatchers("/admin/**").denyAll()
                    .anyRequest().denyAll()
            );
        }

        http.httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService users(AdminProperties adminProperties, PasswordEncoder passwordEncoder) {
        if (!adminProperties.enabled()) {
            return new InMemoryUserDetailsManager();
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(adminProperties.username())
                        .password(passwordEncoder.encode(adminProperties.password()))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
