package com.example.bankcards.service;


import com.example.bankcards.dto.request.CardBlockRequest;
import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.request.CreditDebitRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardBlockResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlock;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardBlockStatus;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardBlockRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    private final TransferRepository transferRepository;

    private final CardBlockRepository cardBlockRepository;

    private final EncryptionService encryptionService;

    private static final SecureRandom random = new SecureRandom();

    public CardService(CardRepository cardRepository, UserRepository userRepository, TransferRepository transferRepository, CardBlockRepository cardBlockRepository, EncryptionService encryptionService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transferRepository = transferRepository;
        this.cardBlockRepository = cardBlockRepository;
        this.encryptionService = encryptionService;
    }

    public CardResponse createCard(CardCreateRequest request, User currentUser) {

        // Only admin can create cards for other users
        User owner = currentUser;
        if (request.getOwnerId() != null) {
            boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
            if (!isAdmin) {
                throw new AccessDeniedException("Only administrators can create cards for other users");
            }
            owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new BusinessException("User not found"));
        }

        String cardNumber = generateCardNumber();
        String encryptedCardNumber = encryptionService.encrypt(cardNumber);
        String maskedCardNumber = encryptionService.maskCardNumber(cardNumber);

        Card card = new Card();
        card.setCardNumber(encryptedCardNumber);
        card.setMaskedCardNumber(maskedCardNumber);
        card.setOwner(owner);
        card.setExpiryDate(request.getExpiryDate() != null ? request.getExpiryDate() : LocalDate.now().plusYears(3));
        card.setBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);
        card.setCreatedAt(LocalDateTime.now());

        card = cardRepository.save(card);
        return mapToResponse(card);
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(User user, Pageable pageable) {
        Page<Card> cards;

        boolean isAdmin = user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (isAdmin) {
            cards = cardRepository.findAll(pageable);
        } else {
            cards = cardRepository.findByOwner(user, pageable);
        }

        return cards.map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public CardResponse getCardById(Long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Card not found"));

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to this card");
        }

        card.updateStatus();
        cardRepository.save(card);

        return mapToResponse(card);
    }

    public CardResponse blockCard(Long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Card not found"));

        // admins can block any card
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to this card");
        }

        if (!card.isActive()) {
            throw new BusinessException("Card is not active");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new BusinessException("Cannot block an expired card");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());
        card = cardRepository.save(card);

        // Update any pending block requests to APPROVED or REJECTED
        CardBlock cardBlock = cardBlockRepository.findByCard(card);
        cardBlock.setStatus(CardBlockStatus.APPROVED);
        cardBlock.setUpdatedAt(LocalDateTime.now());
        cardBlockRepository.save(cardBlock);

        return mapToResponse(card);
    }

    public CardResponse activateCard(Long cardId, User currentUser) {
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can activate cards");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Card not found"));

        if (card.isExpired()) {
            throw new BusinessException("Cannot activate an expired card");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setUpdatedAt(LocalDateTime.now());
        card = cardRepository.save(card);

        return mapToResponse(card);
    }

    public void deleteCard(Long cardId, User currentUser) {

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can delete cards");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Card not found"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot delete card with positive balance");
        }

        cardRepository.delete(card);
    }

    public CardResponse creditCard(CreditDebitRequest request, User currentUser) {

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new BusinessException("Card not found"));

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to this card");
        }

        if (!card.isActive()) {
            throw new BusinessException("Card is not active");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Credit amount must be positive");
        }

        card.setBalance(card.getBalance().add(request.getAmount()));
        card.setUpdatedAt(LocalDateTime.now());
        card = cardRepository.save(card);

        return mapToResponse(card);
    }

    public CardResponse debitCard(CreditDebitRequest request, User currentUser) {

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new BusinessException("Card not found"));

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to this card");
        }

        if (!card.isActive()) {
            throw new BusinessException("Card is not active");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Debit amount must be positive");
        }

        if (card.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Insufficient funds");
        }

        card.setBalance(card.getBalance().subtract(request.getAmount()));
        card.setUpdatedAt(LocalDateTime.now());
        card = cardRepository.save(card);

        return mapToResponse(card);
    }

    public TransferResponse transferFunds(TransferRequest request, User currentUser) {

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new BusinessException("Source card not found"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new BusinessException("Destination card not found"));

        // Validate ownership - users can only transfer between their own cards
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin) {
            if (!fromCard.getOwner().getId().equals(currentUser.getId()) ||
                    !toCard.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only transfer between your own cards");
            }
        }

        // Validate cards status
        if (!fromCard.isActive()) {
            throw new BusinessException("Source card is not active");
        }

        if (!toCard.isActive()) {
            throw new BusinessException("Destination card is not active");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be positive");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Insufficient funds");
        }

        // Create transfer record
        Transfer transfer = new Transfer(fromCard, toCard, request.getAmount(), request.getDescription());

        try {
            // Perform the transfer
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));
            fromCard.setUpdatedAt(LocalDateTime.now());
            toCard.setUpdatedAt(LocalDateTime.now());

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setProcessedAt(LocalDateTime.now());

        } catch (Exception e) {
            transfer.setStatus(TransferStatus.FAILED);
            throw new BusinessException("Transfer failed: " + e.getMessage());
        } finally {
            transfer = transferRepository.save(transfer);
        }

        return mapTransferToResponse(transfer);
    }

    public CardBlockResponse createCardBlockRequest(CardBlockRequest request, User currentUser) {

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new BusinessException("Card not found"));

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name()));
        if (!isAdmin && !card.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to this card");
        }

        // Validate cards status
        if (!card.isActive()) {
            throw new BusinessException("Card is not active");
        }

        CardBlock cardBlock = new CardBlock();
        cardBlock.setCard(card);
        cardBlock.setStatus(CardBlockStatus.PENDING);
        cardBlock.setDescription(request.getDescription());
        cardBlock.setCreatedAt(LocalDateTime.now());
        cardBlockRepository.save(cardBlock);

        return mapCardBlockToResponse(cardBlock);

    }

    @Transactional(readOnly = true)
    public Page<CardBlockResponse> getCardBlocks(Pageable pageable) {
        Page<CardBlock> cardBlocks = cardBlockRepository.findAll(pageable);

        return cardBlocks.map(this::mapCardBlockToResponse);
    }

    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();

        // Generate 16-digit card number
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }

        // Ensure uniqueness
        String generated = cardNumber.toString();
        while (cardRepository.existsByCardNumber(encryptionService.encrypt(generated))) {
            cardNumber = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                cardNumber.append(random.nextInt(10));
            }
            generated = cardNumber.toString();
        }

        return generated;
    }

    private CardResponse mapToResponse(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setMaskedCardNumber(card.getMaskedCardNumber());
        response.setOwnerName(card.getOwner().getFirstName() + " " + card.getOwner().getLastName());
        response.setExpiryDate(card.getExpiryDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        response.setCreatedAt(card.getCreatedAt());
        return response;
    }

    private TransferResponse mapTransferToResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setFromCardMasked(transfer.getFromCard().getMaskedCardNumber());
        response.setToCardMasked(transfer.getToCard().getMaskedCardNumber());
        response.setAmount(transfer.getAmount());
        response.setStatus(transfer.getStatus());
        response.setDescription(transfer.getDescription());
        response.setProcessedAt(transfer.getProcessedAt());
        return response;
    }

    private CardBlockResponse mapCardBlockToResponse(CardBlock cardBlock) {
        CardBlockResponse response = new CardBlockResponse();
        response.setId(cardBlock.getId());
        response.setCardMasked(cardBlock.getCard().getMaskedCardNumber());
        response.setStatus(cardBlock.getStatus());
        response.setDescription(cardBlock.getDescription());
        response.setCreatedAt(cardBlock.getCreatedAt());
        return response;
    }


}
