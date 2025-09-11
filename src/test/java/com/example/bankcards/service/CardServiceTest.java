package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    private User user;
    private User admin;
    private Card card;
    private CardCreateRequest cardCreateRequest;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "password", "John", "Doe", Set.of(Role.ROLE_USER));
        user.setId(1L);

        admin = new User("admin", "admin@example.com", "password", "Admin", "User", Set.of(Role.ROLE_ADMIN));
        admin.setId(2L);

        card = new Card();
        card.setId(1L);
        card.setCardNumber("encrypted_card_number");
        card.setMaskedCardNumber("**** **** **** 1234");
        card.setOwner(user);
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setBalance(BigDecimal.valueOf(1000.00));
        card.setStatus(CardStatus.ACTIVE);

        cardCreateRequest = new CardCreateRequest();
        cardCreateRequest.setInitialBalance(BigDecimal.valueOf(500.00));
        cardCreateRequest.setExpiryDate(LocalDate.now().plusYears(2));
    }

    @Test
    void createCard_Success() {
        // Given
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted_card_number");
        when(encryptionService.maskCardNumber(anyString())).thenReturn("**** **** **** 1234");
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        CardResponse result = cardService.createCard(cardCreateRequest, user);

        // Then
        assertNotNull(result);
        assertEquals("**** **** **** 1234", result.getMaskedCardNumber());
        assertEquals("John Doe", result.getOwnerName());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_AdminCanCreateForOtherUser() {
        // Given
        cardCreateRequest.setOwnerId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted_card_number");
        when(encryptionService.maskCardNumber(anyString())).thenReturn("**** **** **** 1234");
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        CardResponse result = cardService.createCard(cardCreateRequest, admin);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_UserCannotCreateForOtherUser() {
        // Given
        cardCreateRequest.setOwnerId(2L);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                cardService.createCard(cardCreateRequest, user));
    }

    @Test
    void transferFunds_Success() {
        // Given
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setOwner(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));
        fromCard.setMaskedCardNumber("**** **** **** 1234");

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setOwner(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));
        toCard.setMaskedCardNumber("**** **** **** 5678");

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
        transferRequest.setDescription("Test transfer");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any())).thenReturn(new Transfer());

        // When
        TransferResponse result = cardService.transferFunds(transferRequest, user);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(900.00), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(600.00), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferFunds_InsufficientFunds() {
        // Given
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(50.00));
        fromCard.setOwner(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setOwner(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(BusinessException.class, () ->
                cardService.transferFunds(transferRequest, user));
    }

    @Test
    void transferFunds_UserCannotTransferBetweenOtherUsersCards() {
        // Given
        User otherUser = new User("other", "other@example.com", "password", "Other", "User", Set.of(Role.ROLE_USER));
        otherUser.setId(3L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setOwner(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setOwner(otherUser); // Different owner
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                cardService.transferFunds(transferRequest, user));
    }

    @Test
    void blockCard_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        CardResponse result = cardService.blockCard(1L, user);

        // Then
        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_UserCannotBlockOtherUsersCard() {
        // Given
        User otherUser = new User("other", "other@example.com", "password", "Other", "User", Set.of(Role.ROLE_USER));
        otherUser.setId(3L);
        card.setOwner(otherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                cardService.blockCard(1L, user));
    }
}
