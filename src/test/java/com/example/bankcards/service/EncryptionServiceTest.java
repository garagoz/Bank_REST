package com.example.bankcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "secretKey", "MySecretKey12345");
    }

    @Test
    void encrypt_Decrypt_Success() {
        // Given
        String plainText = "1234567890123456";

        // When
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotEquals(plainText, encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void maskCardNumber_Success() {
        // Given
        String cardNumber = "1234567890123456";

        // When
        String masked = encryptionService.maskCardNumber(cardNumber);

        // Then
        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void maskCardNumber_InvalidLength() {
        // Given
        String cardNumber = "123456";

        // When
        String masked = encryptionService.maskCardNumber(cardNumber);

        // Then
        assertEquals(cardNumber, masked); // Should return original if invalid
    }

    @Test
    void maskCardNumber_Null() {
        // Given
        String cardNumber = null;

        // When
        String masked = encryptionService.maskCardNumber(cardNumber);

        // Then
        assertNull(masked);
    }
}
