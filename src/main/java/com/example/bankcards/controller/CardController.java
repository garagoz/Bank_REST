package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardBlockRequest;
import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.request.CreditDebitRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.ApiResponse;
import com.example.bankcards.dto.response.CardBlockResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Card Management", description = "Card management operations")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @Operation(summary = "Create a new card")
    public ResponseEntity<ApiResponse<CardResponse>> createCard(
            @Valid @RequestBody CardCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        CardResponse card = cardService.createCard(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Card created successfully", card));
    }

//    @GetMapping
//    @Operation(summary = "Get user cards with pagination and filtering")
//    public ResponseEntity<ApiResponse<Page<CardResponse>>> getUserCards(@AuthenticationPrincipal User currentUser, Pageable pageable) {
//        Page<CardResponse> cards = cardService.getUserCards(currentUser, pageable);
//        return ResponseEntity.ok(ApiResponse.success(cards));
//    }

    @GetMapping
    @Operation(summary = "Get cards with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<CardResponse>>> getCards(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardResponse>  cards = cardService.getCards(currentUser, userId, cardNumber, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card by ID")
    public ResponseEntity<ApiResponse<CardResponse>> getCardById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        CardResponse card = cardService.getCardById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Block a card (Admin only)")
    public ResponseEntity<ApiResponse<CardResponse>> blockCard(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        CardResponse card = cardService.blockCard(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Card blocked successfully", card));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Activate a card (Admin only)")
    public ResponseEntity<ApiResponse<CardResponse>> activateCard(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        CardResponse card = cardService.activateCard(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Card activated successfully", card));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Delete a card (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        cardService.deleteCard(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully", null));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between cards")
    public ResponseEntity<ApiResponse<TransferResponse>> transferFunds(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal User currentUser) {
        TransferResponse transfer = cardService.transferFunds(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed successfully", transfer));
    }

    @PostMapping("/credit")
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Credit funds to a card (Admin only)")
    public ResponseEntity<ApiResponse<CardResponse>> creditCard(
            @Valid @RequestBody CreditDebitRequest request,
            @AuthenticationPrincipal User currentUser) {
        CardResponse card = cardService.creditCard(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Credit funded to a card successfully", card));
    }

    @PostMapping("/debit")
    @Operation(summary = "Debit funds from a card")
    public ResponseEntity<ApiResponse<CardResponse>> debitCard(
            @Valid @RequestBody CreditDebitRequest request,
            @AuthenticationPrincipal User currentUser) {
        CardResponse card = cardService.debitCard(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Debit funded from a card successfully", card));
    }

    @PostMapping("/block/request")
    @Operation(summary = "Create card block request by user")
    public ResponseEntity<ApiResponse<CardBlockResponse>> cardBlockRequest(
            @Valid @RequestBody CardBlockRequest request,
            @AuthenticationPrincipal User currentUser) {
        CardBlockResponse cardBlockResponse = cardService.createCardBlockRequest(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Card block request created successfully", cardBlockResponse));
    }

    @GetMapping("/block/request")
    @PreAuthorize("hasRole(T(com.example.bankcards.entity.enums.Role).ROLE_ADMIN.name())")
    @Operation(summary = "Get all card block requests with pagination and filtering (Admin only)")
    public ResponseEntity<ApiResponse<Page<CardBlockResponse>>> getCardBlockRequests(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(cardService.getCardBlocks(pageable)));
    }
}
