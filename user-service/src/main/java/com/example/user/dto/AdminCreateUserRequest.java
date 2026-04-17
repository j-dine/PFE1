package com.example.user.dto;

import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String username;
    private String password;
    private String email;
    private String service;

    // Single role per user to keep "1 utilisateur = 1 interface".
    private String roleName;
}

