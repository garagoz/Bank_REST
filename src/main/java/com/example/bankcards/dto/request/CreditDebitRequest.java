package com.example.bankcards.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreditDebitRequest {

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private BigDecimal amount;

}
