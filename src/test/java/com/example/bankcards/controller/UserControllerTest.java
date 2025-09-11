package com.example.bankcards.controller;

import com.example.bankcards.dto.request.RegisterRequest;
import com.example.bankcards.dto.response.ApiResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private RegisterRequest registerRequest;
    private UserResponse userResponse;
    private User currentUser;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        userResponse = new UserResponse();
        currentUser = new User();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllUsers_ShouldReturnSuccessResponse() {
        Page<UserResponse> userPage = new PageImpl<>(Collections.singletonList(userResponse));
        when(userService.getAllUsers(any(User.class), any(Pageable.class))).thenReturn(userPage);

        ResponseEntity<ApiResponse<Page<UserResponse>>> response = userController.getAllUsers(currentUser, pageable);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(userPage, response.getBody().getData());
    }

    @Test
    void getUserById_ShouldReturnSuccessResponse() {
        when(userService.getUserById(anyLong(), any(User.class))).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponse>> response = userController.getUserById(1L, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(userResponse, response.getBody().getData());
    }

    @Test
    void createUser_ShouldReturnSuccessResponse() {
        when(userService.createUser(any(RegisterRequest.class), any(User.class))).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponse>> response = userController.createUser(registerRequest, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User created successfully", response.getBody().getMessage());
        assertEquals(userResponse, response.getBody().getData());
    }

    @Test
    void updateUser_ShouldReturnSuccessResponse() {
        when(userService.updateUser(anyLong(), any(RegisterRequest.class), any(User.class))).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponse>> response = userController.updateUser(1L, registerRequest, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User updated successfully", response.getBody().getMessage());
        assertEquals(userResponse, response.getBody().getData());
    }

    @Test
    void deleteUser_ShouldReturnSuccessResponse() {
        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(1L, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User deleted successfully", response.getBody().getMessage());
        assertEquals(null, response.getBody().getData());
    }
}