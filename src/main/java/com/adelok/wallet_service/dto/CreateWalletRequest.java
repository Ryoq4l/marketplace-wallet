package com.adelok.wallet_service.dto;

import com.adelok.wallet_service.entity.enumerated.Currency;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWalletRequest (
        @NotNull (message = "required userId")
        UUID userId,
        @NotNull (message = "currency is required")
        Currency currency
) {
}
