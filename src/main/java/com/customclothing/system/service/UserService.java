package com.customclothing.system.service;

import com.customclothing.system.dto.UserRegisterDTO;
import com.customclothing.system.dto.UserResponseDTO;
import com.customclothing.system.entity.User;
import com.customclothing.system.enums.Role;

import java.util.List;

public interface UserService {

    UserResponseDTO register(UserRegisterDTO registerDTO);

    UserResponseDTO login(com.customclothing.system.dto.UserLoginDTO loginDTO);

    UserResponseDTO findById(Long id);

    UserResponseDTO findByEmail(String email);

    List<UserResponseDTO> findAll();

    List<UserResponseDTO> findByRole(Role role);

    User getUserEntity(Long id);
}
