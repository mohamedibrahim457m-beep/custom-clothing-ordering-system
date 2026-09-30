package com.customclothing.system.controller;

import com.customclothing.system.dto.ApiResponse;
import com.customclothing.system.dto.UserLoginDTO;
import com.customclothing.system.dto.UserRegisterDTO;
import com.customclothing.system.dto.UserResponseDTO;
import com.customclothing.system.enums.Role;
import com.customclothing.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserResponseDTO response = userService.register(registerDTO);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDTO>> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserResponseDTO response = userService.login(loginDTO);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.findAll();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/tailors")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getTailors() {
        List<UserResponseDTO> tailors = userService.findByRole(Role.TAILOR);
        return ResponseEntity.ok(ApiResponse.success("Tailors retrieved successfully", tailors));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getCustomers() {
        List<UserResponseDTO> customers = userService.findByRole(Role.CUSTOMER);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }
}
