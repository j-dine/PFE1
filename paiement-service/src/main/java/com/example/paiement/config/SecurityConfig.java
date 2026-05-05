package com.example.paiement.config;

import com.example.paiement.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        // Read access: financier/responsable/admin.
                        .requestMatchers(HttpMethod.GET, "/api/paiements/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_AGENT_FINANCIER", "ROLE_RESPONSABLE")
                        // Create/update/delete: financier/admin (responsable allowed to create to keep current UI behavior).
                        .requestMatchers(HttpMethod.POST, "/api/paiements/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_AGENT_FINANCIER", "ROLE_RESPONSABLE")
                        .requestMatchers(HttpMethod.PATCH, "/api/paiements/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_AGENT_FINANCIER")
                        .requestMatchers(HttpMethod.DELETE, "/api/paiements/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_AGENT_FINANCIER")
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
