package com.adelok.wallet_service.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWalletRequest (
        @NotNull (message = "required userId")
        UUID userId
) {
}
