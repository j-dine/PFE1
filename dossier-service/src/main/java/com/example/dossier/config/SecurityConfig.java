package com.example.dossier.config;

import com.example.dossier.security.JwtAuthFilter;
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
                        // Read access: all authenticated roles (filtered in UI / business logic).
                        .requestMatchers(HttpMethod.GET, "/api/dossiers/**").authenticated()

                        // BO: create/assign/archive/expedier.
                        .requestMatchers(HttpMethod.POST, "/api/dossiers").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE")
                        .requestMatchers(HttpMethod.POST, "/api/dossiers/*/assign").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE")
                        .requestMatchers(HttpMethod.POST, "/api/dossiers/*/archive").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE")
                        .requestMatchers(HttpMethod.POST, "/api/dossiers/*/expedier").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE")

                        // Comments: any authenticated user can add a trace.
                        .requestMatchers(HttpMethod.POST, "/api/dossiers/*/comments").authenticated()

                        // Updates: restricted by role (fine-grained statut rules are handled in workflow later).
                        .requestMatchers(HttpMethod.PATCH, "/api/dossiers/*").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE", "ROLE_AGENT_SERVICE", "ROLE_RESPONSABLE")
                        .requestMatchers(HttpMethod.PUT, "/api/dossiers/*/statut").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE", "ROLE_AGENT_SERVICE", "ROLE_RESPONSABLE", "ROLE_AGENT_FINANCIER")

                        .requestMatchers(HttpMethod.DELETE, "/api/dossiers/*").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
