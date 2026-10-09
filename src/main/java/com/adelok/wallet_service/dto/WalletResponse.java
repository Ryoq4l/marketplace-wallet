package com.adelok.wallet_service.dto;

import com.adelok.wallet_service.entity.enumerated.Currency;
import com.adelok.wallet_service.entity.enumerated.WalletStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Builder
public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal availableBalance,
        BigDecimal heldBalance,
        BigDecimal totalBalance,
        Currency currency,
        WalletStatus status,
        LocalDateTime createdAt
) {
}
