package com.example.bankcards.service;

import com.example.bankcards.dto.request.RegisterRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.UserRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User user;
    private User admin;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@email.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        user.setRoles(Set.of(Role.ROLE_USER));

        admin = new User();
        admin.setId(2L);
        admin.setRoles(Set.of(Role.ROLE_ADMIN));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createUser_AdminOnly() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(registerRequest, admin);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_NonAdmin_ThrowsAccessDenied() {
        assertThrows(AccessDeniedException.class, () -> userService.createUser(registerRequest, user));
    }

    @Test
    void getAllUsers_AdminOnly() {
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserResponse> response = userService.getAllUsers(admin, pageable);

        assertEquals(1, response.getContent().size());
    }

    @Test
    void getAllUsers_NonAdmin_ThrowsAccessDenied() {
        assertThrows(AccessDeniedException.class, () -> userService.getAllUsers(user, pageable));
    }

    @Test
    void getUserById_OwnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L, user);

        assertNotNull(response);
    }

    @Test
    void getUserById_AdminAnyUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L, admin);

        assertNotNull(response);
    }

    @Test
    void updateUser_OwnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, registerRequest, user);

        assertNotNull(response);
    }

    @Test
    void updateUser_AdminAnyUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, registerRequest, admin);

        assertNotNull(response);
    }

    @Test
    void updateUser_DuplicateUsername_ThrowsException() {
        registerRequest.setUsername("existing");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.updateUser(1L, registerRequest, user));
    }

    @Test
    void deleteUser_AdminOnly_NotSelf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.deleteUser(1L, admin));
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_NonAdmin_ThrowsAccessDenied() {
        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(1L, user));
    }

    @Test
    void deleteUser_Self_ThrowsException() {
        assertThrows(BusinessException.class, () -> userService.deleteUser(2L, admin));
    }
}