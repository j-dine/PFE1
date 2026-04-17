package com.example.user.service.impl;

import com.example.user.dto.AdminCreateUserRequest;
import com.example.user.dto.AdminUpdateRoleRequest;
import com.example.user.dto.UserDTO;
import com.example.user.entity.Role;
import com.example.user.entity.User;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.repository.RoleRepository;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        return toDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + username));
        return toDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id);
        }
        userRepository.deleteById(id);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────
    @Override
    public UserDTO adminCreateUser(AdminCreateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid request");
        }

        final String username = request.getUsername() != null ? request.getUsername().trim() : "";
        final String password = request.getPassword() != null ? request.getPassword() : "";
        final String roleName = request.getRoleName() != null ? request.getRoleName().trim() : "";
        final String email = request.getEmail() != null ? request.getEmail().trim() : null;

        if (username.isBlank() || password.isBlank() || roleName.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "username, password and roleName are required");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(CONFLICT, "Username already exists");
        }
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setService(request.getService());
        user.setActive(true);
        user.setRoles(Set.of(role));

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO adminUpdateUserRole(Long userId, AdminUpdateRoleRequest request) {
        if (userId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "userId is required");
        }
        if (request == null || request.getRoleName() == null || request.getRoleName().trim().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "roleName is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String roleName = request.getRoleName().trim();
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));

        user.setRoles(Set.of(role));
        return toDTO(userRepository.save(user));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setService(user.getService());
        dto.setActive(user.getActive() == null ? Boolean.TRUE : user.getActive());
        dto.setRoles(
                user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toSet()));
        return dto;
    }
}
