package com.example.user.service;

import com.example.user.dto.UserDTO;
import com.example.user.dto.AdminCreateUserRequest;
import com.example.user.dto.AdminUpdateRoleRequest;
import com.example.user.entity.User;

import java.util.List;

public interface UserService {

    User registerUser(User user);

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    List<UserDTO> getAllUsers();

    void deleteUser(Long id);

    UserDTO adminCreateUser(AdminCreateUserRequest request);

    UserDTO adminUpdateUserRole(Long userId, AdminUpdateRoleRequest request);
}
