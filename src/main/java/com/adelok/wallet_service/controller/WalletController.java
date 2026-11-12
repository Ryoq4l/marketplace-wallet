package com.adelok.wallet_service.controller;

import com.adelok.wallet_service.dto.requests.*;
import com.adelok.wallet_service.dto.responses.WalletResponse;
import com.adelok.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> create(@AuthenticationPrincipal String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.create(new CreateWalletRequest(userId)));
    }

    @GetMapping
    public ResponseEntity<WalletResponse> getMyWallet(@AuthenticationPrincipal String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        return ResponseEntity.ok(walletService.getByUserId(userId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        return ResponseEntity.ok(walletService.deposit(userId, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        return ResponseEntity.ok(walletService.withdraw(userId, request));
    }

    @PostMapping("/hold")
    public ResponseEntity<WalletResponse> hold(
            @Valid @RequestBody HoldRequest request,
            @AuthenticationPrincipal String tokenUserId) {
        UUID userId = UUID.fromString(tokenUserId);
        return ResponseEntity.ok(walletService.holdEscrow(userId, request));
    }

    @PostMapping("/complete/{sellerId}")
    public ResponseEntity<WalletResponse> complete(
            @Valid @RequestBody CompleteRequest request,
            @PathVariable UUID sellerId,
            @AuthenticationPrincipal String tokenUserId) {
        return ResponseEntity.ok(walletService.completeToSeller(sellerId, request));
    }
}