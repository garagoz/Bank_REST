package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardBlockRequest;
import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.request.CreditDebitRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.ApiResponse;
import com.example.bankcards.dto.response.CardBlockResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
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
public class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private CardCreateRequest cardCreateRequest;
    private CardBlockRequest cardBlockRequest;
    private CreditDebitRequest creditDebitRequest;
    private TransferRequest transferRequest;
    private CardResponse cardResponse;
    private CardBlockResponse cardBlockResponse;
    private TransferResponse transferResponse;
    private User currentUser;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        cardCreateRequest = new CardCreateRequest();
        cardBlockRequest = new CardBlockRequest();
        creditDebitRequest = new CreditDebitRequest();
        transferRequest = new TransferRequest();
        cardResponse = new CardResponse();
        cardBlockResponse = new CardBlockResponse();
        transferResponse = new TransferResponse();
        currentUser = new User();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createCard_ShouldReturnSuccessResponse() {
        when(cardService.createCard(any(CardCreateRequest.class), any(User.class))).thenReturn(cardResponse);

        ResponseEntity<ApiResponse<CardResponse>> response = cardController.createCard(cardCreateRequest, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Card created successfully", response.getBody().getMessage());
        assertEquals(cardResponse, response.getBody().getData());
    }

    @Test
    void getUserCards_ShouldReturnSuccessResponse() {
        Page<CardResponse> cardPage = new PageImpl<>(Collections.singletonList(cardResponse));
        when(cardService.getUserCards(any(User.class), any(Pageable.class))).thenReturn(cardPage);

        ResponseEntity<ApiResponse<Page<CardResponse>>> response = cardController.getUserCards(currentUser, pageable);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(cardPage, response.getBody().getData());
    }

    @Test
    void getCardById_ShouldReturnSuccessResponse() {
        when(cardService.getCardById(anyLong(), any(User.class))).thenReturn(cardResponse);

        ResponseEntity<ApiResponse<CardResponse>> response = cardController.getCardById(1L, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(cardResponse, response.getBody().getData());
    }

    @Test
    void blockCard_ShouldReturnSuccessResponse() {
        when(cardService.blockCard(anyLong(), any(User.class))).thenReturn(cardResponse);

        ResponseEntity<ApiResponse<CardResponse>> response = cardController.blockCard(1L, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Card blocked successfully", response.getBody().getMessage());
        assertEquals(cardResponse, response.getBody().getData());
    }

    @Test
    void activateCard_ShouldReturnSuccessResponse() {
        when(cardService.activateCard(anyLong(), any(User.class))).thenReturn(cardResponse);

        ResponseEntity<ApiResponse<CardResponse>> response = cardController.activateCard(1L, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Card activated successfully", response.getBody().getMessage());
        assertEquals(cardResponse, response.getBody().getData());
    }

    @Test
    void deleteCard_ShouldReturnSuccessResponse() {
        ResponseEntity<ApiResponse<Void>> response = cardController.deleteCard(1L, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Card deleted successfully", response.getBody().getMessage());
        assertEquals(null, response.getBody().getData());
    }

    @Test
    void transferFunds_ShouldReturnSuccessResponse() {
        when(cardService.transferFunds(any(TransferRequest.class), any(User.class))).thenReturn(transferResponse);

        ResponseEntity<ApiResponse<TransferResponse>> response = cardController.transferFunds(transferRequest, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Transfer completed successfully", response.getBody().getMessage());
        assertEquals(transferResponse, response.getBody().getData());
    }

    @Test
    void creditCard_ShouldReturnSuccessResponse() {
        when(cardService.creditCard(any(CreditDebitRequest.class), any(User.class))).thenReturn(cardResponse);

        ResponseEntity<ApiResponse<CardResponse>> response = cardController.creditCard(creditDebitRequest, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Credit funded to a card successfully", response.getBody().getMessage());
        assertEquals(cardResponse, response.getBody().getData());
    }

    @Test
    void debitCard_ShouldReturnSuccessResponse() {
        when(cardService.debitCard(any(CreditDebitRequest.class), any(User.class))).thenReturn(cardResponse);

        ResponseEntity<ApiResponse<CardResponse>> response = cardController.debitCard(creditDebitRequest, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Debit funded from a card successfully", response.getBody().getMessage());
        assertEquals(cardResponse, response.getBody().getData());
    }

    @Test
    void cardBlockRequest_ShouldReturnSuccessResponse() {
        when(cardService.createCardBlockRequest(any(CardBlockRequest.class), any(User.class))).thenReturn(cardBlockResponse);

        ResponseEntity<ApiResponse<CardBlockResponse>> response = cardController.cardBlockRequest(cardBlockRequest, currentUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Card block request created successfully", response.getBody().getMessage());
        assertEquals(cardBlockResponse, response.getBody().getData());
    }

    @Test
    void getCardBlockRequests_ShouldReturnSuccessResponse() {
        Page<CardBlockResponse> blockPage = new PageImpl<>(Collections.singletonList(cardBlockResponse));
        when(cardService.getCardBlocks(any(Pageable.class))).thenReturn(blockPage);

        ResponseEntity<ApiResponse<Page<CardBlockResponse>>> response = cardController.getCardBlockRequests(pageable);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(blockPage, response.getBody().getData());
    }
}