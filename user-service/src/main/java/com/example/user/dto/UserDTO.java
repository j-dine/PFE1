package com.example.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String service;
    private Boolean active;
    private Set<String> roles;
}
