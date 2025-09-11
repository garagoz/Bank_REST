package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.request.CreditDebitRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlock;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardBlockRepository;
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
import java.util.Date;
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
    private CardBlockRepository cardBlockRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    private User adminUser;
    private User regularUser;
    private Card card;
    private CardCreateRequest cardCreateRequest;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRoles(Set.of(Role.ROLE_ADMIN));

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRoles(Set.of(Role.ROLE_USER));

        card = new Card();
        card.setId(1L);
        card.setOwner(regularUser);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setMaskedCardNumber("**** **** **** 1234");
        card.setExpiryDate(LocalDate.now().plusYears(1));

        cardCreateRequest = new CardCreateRequest();
        cardCreateRequest.setInitialBalance(BigDecimal.valueOf(500));
    }

    @Test
    void createCard_AdminForOtherUser_Success() {
        cardCreateRequest.setOwnerId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");
        when(encryptionService.maskCardNumber(anyString())).thenReturn("**** **** **** 1234");
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardResponse response = cardService.createCard(cardCreateRequest, adminUser);

        assertNotNull(response);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_RegularUserForOtherUser_ThrowsException() {
        cardCreateRequest.setOwnerId(3L);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> cardService.createCard(cardCreateRequest, regularUser));

        assertEquals("Only administrators can create cards for other users", exception.getMessage());
    }

    @Test
    void blockCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardBlock cardBlock = new CardBlock();
        when(cardBlockRepository.findByCard(card)).thenReturn(cardBlock);

        CardResponse response = cardService.blockCard(1L, regularUser);

        assertNotNull(response);
        verify(cardRepository).save(card);
        verify(cardBlockRepository).save(cardBlock);
    }

    @Test
    void blockCard_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.blockCard(1L, regularUser));

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    void blockCard_AccessDenied_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setRoles(Set.of(Role.ROLE_USER));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> cardService.blockCard(1L, otherUser));

        assertEquals("Access denied to this card", exception.getMessage());
    }

    @Test
    void creditCard_Success() {
        CreditDebitRequest request = new CreditDebitRequest();
        request.setCardId(1L);
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardResponse response = cardService.creditCard(request, regularUser);

        assertNotNull(response);
        verify(cardRepository).save(card);
    }

//    @Test
//    void debitCard_InsufficientFunds_ThrowsException() {
//        CreditDebitRequest request = new CreditDebitRequest();
//        request.setCardId(1L);
//        request.setAmount(BigDecimal.valueOf(2000)); // More than card balance
//
//        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
//
//        BusinessException exception = assertThrows(BusinessException.class,
//                () -> cardService.debitCard(request, regularUser));
//
//        assertEquals("Insufficient funds", exception.getMessage());
//    }

    @Test
    void transferFunds_Success() {
        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setOwner(regularUser);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setExpiryDate(LocalDate.now().plusYears(1));
        toCard.setMaskedCardNumber("**** **** **** 5678");

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Test transfer");

        Transfer transfer = new Transfer();
        transfer.setId(1L);
        transfer.setFromCard(card);
        transfer.setToCard(toCard);


        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        TransferResponse response = cardService.transferFunds(request, regularUser);

        assertNotNull(response);
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void deleteCard_WithPositiveBalance_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.deleteCard(1L, adminUser));

        assertEquals("Cannot delete card with positive balance", exception.getMessage());
    }
}
