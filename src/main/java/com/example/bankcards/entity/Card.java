package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true, length = 500)
    private String cardNumber; // Encrypted card number

    @Column(name = "masked_card_number", nullable = false, length = 19)
    private String maskedCardNumber; // **** **** **** 1234

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull
    private User owner;

    @Column(name = "expiry_date", nullable = false)
    @NotNull
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(precision = 15, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal balance = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Card(String cardNumber, String maskedCardNumber, User owner, LocalDate expiryDate, BigDecimal balance) {
        this.cardNumber = cardNumber;
        this.maskedCardNumber = maskedCardNumber;
        this.owner = owner;
        this.expiryDate = expiryDate;
        this.balance = balance;
    }

    // Business methods
    public boolean isExpired() {
        if (expiryDate == null) {
            return true; // null means expired
        }
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isActive() {
        return status == CardStatus.ACTIVE && !isExpired();
    }

    public void updateStatus() {
        if (isExpired() && status == CardStatus.ACTIVE) {
            this.status = CardStatus.EXPIRED;
        }
    }

}
