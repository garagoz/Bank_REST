package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardBlockRequest {

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @NotNull(message = "Description is required")
    private String description;
}
