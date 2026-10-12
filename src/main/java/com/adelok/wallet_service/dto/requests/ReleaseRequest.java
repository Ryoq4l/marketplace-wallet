package com.adelok.wallet_service.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ReleaseRequest(
        @Positive
        @NotNull
        BigDecimal amount,

        @NotNull
        UUID sellerId,

        @NotNull
        UUID dealId,

        String description
) {
}
