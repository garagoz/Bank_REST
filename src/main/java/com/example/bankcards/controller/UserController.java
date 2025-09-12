package com.example.bankcards.controller;

import com.example.bankcards.dto.request.RegisterRequest;
import com.example.bankcards.dto.response.ApiResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "User management operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Get users with filtering and pagination (Admin only)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getUsers(currentUser, username, firstName, lastName, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        UserResponse user = userService.getUserById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Create new user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal User currentUser) {
        UserResponse user = userService.createUser(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal User currentUser) {
        UserResponse user = userService.updateUser(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        userService.deleteUser(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
