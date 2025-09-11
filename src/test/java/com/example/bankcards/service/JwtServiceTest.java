package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Set test values using reflection
        try {
            var secretField = JwtService.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(jwtService, "myJwtSecretKeyThatIsLongEnoughForHS256Algorithm");

            var expirationField = JwtService.class.getDeclaredField("jwtExpiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtService, 3600000L); // 1 hour
        } catch (Exception e) {
            throw new RuntimeException("Failed to set JWT properties", e);
        }

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRoles(Set.of(Role.ROLE_USER));
    }

    @Test
    void generateToken_Success() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_Success() {
        String token = jwtService.generateToken(testUser);

        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WrongUser_ReturnsFalse() {
        String token = jwtService.generateToken(testUser);

        User differentUser = new User();
        differentUser.setUsername("differentuser");

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void generateTokenWithExtraClaims_Success() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");

        String token = jwtService.generateToken(extraClaims, testUser);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }
}
