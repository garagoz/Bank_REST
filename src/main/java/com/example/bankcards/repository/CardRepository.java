package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByOwner(User owner, Pageable pageable);
    boolean existsByCardNumber(String cardNumber);

    Page<Card> findByOwnerId(Long userId, Pageable pageable);
    Page<Card> findByCardNumberContaining(String cardNumber, Pageable pageable);
    Page<Card> findByStatus(CardStatus status, Pageable pageable);
    Page<Card> findByCardNumberContainingAndStatus(String cardNumber, CardStatus status, Pageable pageable);
    Page<Card> findByCardNumberContainingAndOwnerId(String cardNumber, Long userId, Pageable pageable);
    Page<Card> findByStatusAndOwnerId(CardStatus status, Long userId, Pageable pageable);
    Page<Card> findByCardNumberContainingAndStatusAndOwnerId(String cardNumber, CardStatus status, Long userId, Pageable pageable);
}
