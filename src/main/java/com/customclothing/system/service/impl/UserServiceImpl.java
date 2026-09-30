package com.customclothing.system.service.impl;

import com.customclothing.system.dto.UserLoginDTO;
import com.customclothing.system.dto.UserRegisterDTO;
import com.customclothing.system.dto.UserResponseDTO;
import com.customclothing.system.entity.User;
import com.customclothing.system.enums.Role;
import com.customclothing.system.exception.BadRequestException;
import com.customclothing.system.exception.ResourceNotFoundException;
import com.customclothing.system.repository.UserRepository;
import com.customclothing.system.service.EmailService;
import com.customclothing.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO login(UserLoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadRequestException("Account is deactivated. Please contact support.");
        }

        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        return new UserResponseDTO(user);
    }

    @Override
    public UserResponseDTO register(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email is already registered: " + dto.getEmail());
        }

        Role assignedRole = dto.getRole() != null ? dto.getRole() : Role.CUSTOMER;

        User user = new User(
                dto.getFullName(),
                dto.getEmail(),
                dto.getPassword(), // Note: Advanced password encoding/JWT to be added in Auth step
                dto.getPhoneNumber(),
                dto.getAddress(),
                assignedRole
        );

        User savedUser = userRepository.save(user);
        UserResponseDTO responseDTO = new UserResponseDTO(savedUser);

        // Send welcome email when a new customer registers
        if (savedUser.getRole() == Role.CUSTOMER) {
            try {
                emailService.sendCustomerWelcomeEmail(responseDTO);
            } catch (Exception ex) {
                log.error("Failed to trigger welcome email for customer '{}': {}",
                        savedUser.getEmail(), ex.getMessage());
            }
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        User user = getUserEntity(id);
        return new UserResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return new UserResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role).stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
