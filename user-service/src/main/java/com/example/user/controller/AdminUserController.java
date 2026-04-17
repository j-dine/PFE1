package com.example.user.controller;

import com.example.user.dto.AdminCreateUserRequest;
import com.example.user.dto.AdminUpdateRoleRequest;
import com.example.user.dto.UserDTO;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> list() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.ok(userService.adminCreateUser(request));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDTO> updateRole(@PathVariable("id") Long id, @RequestBody AdminUpdateRoleRequest request) {
        return ResponseEntity.ok(userService.adminUpdateUserRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

