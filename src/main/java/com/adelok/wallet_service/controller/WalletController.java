package com.adelok.wallet_service.controller;

import com.adelok.wallet_service.dto.requests.*;
import com.adelok.wallet_service.dto.responses.WalletResponse;
import com.adelok.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.create(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.getByUserId(userId));
    }

    @PostMapping("/user/{userId}/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @Valid
            @RequestBody DepositRequest request,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.deposit(userId, request));
    }

    @PostMapping("/user/{userId}/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            @Valid
            @RequestBody WithdrawRequest request,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.withdraw(userId, request));
    }

    @PostMapping("/user/{userId}/hold")
    public ResponseEntity<WalletResponse> hold(
            @Valid
            @RequestBody HoldRequest request,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.holdEscrow(userId, request));
    }
    @PostMapping("/user/{sellerId}/complete")
    public ResponseEntity<WalletResponse> complete(
            @Valid
            @RequestBody CompleteRequest request,
            @PathVariable UUID sellerId) {
        return ResponseEntity.ok(walletService.completeToSeller(sellerId, request));
    }
    @PostMapping("/user/{buyerId}/refund")
    public ResponseEntity<WalletResponse> refund(
            @Valid
            @RequestBody RefundRequest request,
            @PathVariable UUID buyerId) {
        return ResponseEntity.ok(walletService.refundToBuyer(buyerId, request));
    }
    @PostMapping("/user/{buyerId}/release")
    public ResponseEntity<WalletResponse> release(
            @Valid
            @RequestBody ReleaseRequest request,
            @PathVariable UUID buyerId) {
        return ResponseEntity.ok(walletService.releaseFunds(buyerId, request));
    }
}
