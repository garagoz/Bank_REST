package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.TransferStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse {
    private Long id;
    private String fromCardMasked;
    private String toCardMasked;
    private BigDecimal amount;
    private TransferStatus status;
    private String description;
    private LocalDateTime processedAt;
}
