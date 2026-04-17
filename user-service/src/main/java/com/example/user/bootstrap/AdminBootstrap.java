package com.example.user.bootstrap;

import com.example.user.entity.Role;
import com.example.user.entity.User;
import com.example.user.repository.RoleRepository;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates an initial admin account for local/dev environments.
 * This is disabled by default and must be explicitly enabled via configuration:
 * - APP_BOOTSTRAP_ADMIN_ENABLED=true
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.bootstrap-admin", name = "enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:admin}")
    private String username;

    @Value("${app.bootstrap-admin.password:Admin@123}")
    private String password;

    @Value("${app.bootstrap-admin.email:admin@bo.local}")
    private String email;

    @Value("${app.bootstrap-admin.role-name:ROLE_ADMIN}")
    private String roleName;

    @Override
    public void run(ApplicationArguments args) {
        Role adminRole = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            // Email is optional (unique). Only set it if not already used.
            if (email != null && !email.isBlank() && !userRepository.existsByEmail(email)) {
                user.setEmail(email);
            }
        }

        // Ensure the admin role is present (idempotent).
        user.getRoles().add(adminRole);
        userRepository.save(user);
    }
}

