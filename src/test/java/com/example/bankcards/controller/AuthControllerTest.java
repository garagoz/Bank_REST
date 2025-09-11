package com.example.bankcards.controller;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegisterRequest;
import com.example.bankcards.dto.response.ApiResponse;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        loginRequest = new LoginRequest();
        authResponse = new AuthResponse();
    }

    @Test
    void register_ShouldReturnSuccessResponse() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.register(registerRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered successfully", response.getBody().getMessage());
        assertEquals(authResponse, response.getBody().getData());
    }

    @Test
    void login_ShouldReturnSuccessResponse() {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(authResponse, response.getBody().getData());
    }
}