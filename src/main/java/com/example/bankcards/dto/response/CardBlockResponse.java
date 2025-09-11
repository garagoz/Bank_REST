package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.CardBlockStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardBlockResponse {
    private Long id;
    private String cardMasked;
    private CardBlockStatus status;
    private String description;
    private LocalDateTime createdAt;

}
