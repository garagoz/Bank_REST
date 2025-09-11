package com.example.bankcards.service;

import com.example.bankcards.dto.request.RegisterRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(RegisterRequest request, User currentUser) {

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can create users");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoles(Set.of(Role.ROLE_USER)); // every new user gets ROLE_USER role
        user.setIsActive(true);

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(User currentUser, Pageable pageable) {

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));

        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can view all users");
        }

        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId, User currentUser) {

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));

        if (!isAdmin && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to this user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        return mapToResponse(user);
    }

    public UserResponse updateUser(Long userId, RegisterRequest request, User currentUser) {

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));

        if (!isAdmin && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to this user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        // Check for duplicate username/email
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        // Only admin can change roles
        if (request.getRoles() != null && isAdmin) {
            Set<Role> roles = request.getRoles().stream()
                    .map(roleName -> {
                        try {
                            return Role.valueOf(roleName);
                        } catch (IllegalArgumentException e) {
                            throw new BusinessException("Invalid role: " + roleName);
                        }
                    })
                    .collect(Collectors.toSet());

            user.setRoles(roles);
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    public void deleteUser(Long userId, User currentUser) {

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));

        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can delete users");
        }

        if (currentUser.getId().equals(userId)) {
            throw new BusinessException("Cannot delete your own account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

//        // Check if user has active cards with balance
//        boolean hasActiveCardsWithBalance = user.getCards().stream()
//                .anyMatch(card -> card.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0);
//
//        if (hasActiveCardsWithBalance) {
//            throw new BusinessException("Cannot delete user with cards having positive balance");
//        }

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRoles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
