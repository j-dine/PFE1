package com.example.user.service;

import com.example.user.dto.LoginResponse;
import com.example.user.entity.User;

public interface AuthService {
    LoginResponse login(String username, String password);
    User register(User user);
}
