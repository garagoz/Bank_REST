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
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    private User adminUser;
    private User regularUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRoles(Set.of(Role.ROLE_ADMIN));

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("testuser");
        regularUser.setEmail("test@example.com");
        regularUser.setRoles(Set.of(Role.ROLE_USER));

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
    }

    @Test
    void createUser_AdminSuccess() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        UserResponse response = userService.createUser(registerRequest, adminUser);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_NonAdmin_ThrowsException() {
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.createUser(registerRequest, regularUser));

        assertEquals("Only administrators can create users", exception.getMessage());
    }

    @Test
    void getAllUsers_AdminSuccess() {
        Page<User> userPage = new PageImpl<>(List.of(regularUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserResponse> response = userService.getAllUsers(adminUser, mock(Pageable.class));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void getAllUsers_NonAdmin_ThrowsException() {
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.getAllUsers(regularUser, mock(Pageable.class)));

        assertEquals("Only administrators can view all users", exception.getMessage());
    }

    @Test
    void getUserById_OwnProfile_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        UserResponse response = userService.getUserById(2L, regularUser);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getUserById_OtherUserProfile_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setRoles(Set.of(Role.ROLE_USER));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.getUserById(2L, otherUser));

        assertEquals("Access denied to this user", exception.getMessage());
    }

    @Test
    void updateUser_Success() {
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");

        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        UserResponse response = userService.updateUser(2L, updateRequest, regularUser);

        assertNotNull(response);
        verify(userRepository).save(regularUser);
    }

    @Test
    void deleteUser_AdminDeletesSelf_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.deleteUser(1L, adminUser));

        assertEquals("Cannot delete your own account", exception.getMessage());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        userService.deleteUser(2L, adminUser);

        verify(userRepository).delete(regularUser);
    }
}
