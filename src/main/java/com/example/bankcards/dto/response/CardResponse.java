package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardResponse {
    private Long id;
    private String maskedCardNumber;
    private String cardNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private LocalDateTime createdAt;

}
