package com.adelok.wallet_service.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawRequest (
        @Positive
        @NotNull
        BigDecimal amount,

        String description
) {
}
