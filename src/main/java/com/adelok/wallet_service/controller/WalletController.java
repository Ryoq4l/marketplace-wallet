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
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody CreateWalletRequest request, @AuthenticationPrincipal String tokenUserId) {
        if (!request.userId().toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.create(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getByUserId(
            @PathVariable UUID userId,
            @AuthenticationPrincipal String tokenUserId) {
        if (!userId.toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(walletService.getByUserId(userId));
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @PathVariable UUID userId,
            @AuthenticationPrincipal String tokenUserId) {
        if (!userId.toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(walletService.deposit(userId, request));
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @PathVariable UUID userId,
            @AuthenticationPrincipal String tokenUserId) {
        if (!userId.toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(walletService.withdraw(userId, request));
    }

    @PostMapping("/{userId}/hold")
    public ResponseEntity<WalletResponse> hold(
            @Valid @RequestBody HoldRequest request,
            @PathVariable UUID userId,
            @AuthenticationPrincipal String tokenUserId) {
        if (!userId.toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(walletService.holdEscrow(userId, request));
    }

    @PostMapping("/{sellerId}/complete")
    public ResponseEntity<WalletResponse> complete(
            @Valid @RequestBody CompleteRequest request,
            @PathVariable UUID sellerId,
            @AuthenticationPrincipal String tokenUserId) {
        if (!sellerId.toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(walletService.completeToSeller(sellerId, request));
    }

    @PostMapping("/{buyerId}/refund")
    public ResponseEntity<WalletResponse> refund(
            @Valid @RequestBody RefundRequest request,
            @PathVariable UUID buyerId,
            @AuthenticationPrincipal String tokenUserId) {
        if (!buyerId.toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(walletService.refundToBuyer(buyerId, request));
    }

    @PostMapping("/{buyerId}/release")
    public ResponseEntity<WalletResponse> release(
            @Valid @RequestBody ReleaseRequest request,
            @PathVariable UUID buyerId,
            @AuthenticationPrincipal String tokenUserId) {
        if (!buyerId.toString().equals(tokenUserId)) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(walletService.releaseFunds(buyerId, request));
    }
}