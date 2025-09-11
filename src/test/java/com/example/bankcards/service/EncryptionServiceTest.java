package com.example.bankcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension .class)
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        // Using reflection to set the secret key for testing
        try {
            var field = EncryptionService.class.getDeclaredField("secretKey");
            field.setAccessible(true);
            field.set(encryptionService, "mySecretKey12345"); // 16 bytes key for AES
        } catch (Exception e) {
            throw new RuntimeException("Failed to set secret key", e);
        }
    }

    @Test
    void encrypt_Success() {
        String plainText = "1234567890123456";

        String encrypted = encryptionService.encrypt(plainText);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
    }

    @Test
    void decrypt_Success() {
        String plainText = "1234567890123456";
        String encrypted = encryptionService.encrypt(plainText);

        String decrypted = encryptionService.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void maskCardNumber_Success() {
        String cardNumber = "1234567890123456";

        String masked = encryptionService.maskCardNumber(cardNumber);

        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void maskCardNumber_InvalidLength_ReturnsOriginal() {
        String invalidCardNumber = "123456";

        String masked = encryptionService.maskCardNumber(invalidCardNumber);

        assertEquals(invalidCardNumber, masked);
    }

    @Test
    void maskCardNumber_Null_ReturnsNull() {
        String masked = encryptionService.maskCardNumber(null);

        assertNull(masked);
    }
}
