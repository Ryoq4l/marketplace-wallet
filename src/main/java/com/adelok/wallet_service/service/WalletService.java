package com.adelok.wallet_service.service;

import com.adelok.wallet_service.dto.*;
import com.adelok.wallet_service.entity.Transaction;
import com.adelok.wallet_service.entity.Wallet;
import com.adelok.wallet_service.entity.enumerated.TransactionStatus;
import com.adelok.wallet_service.entity.enumerated.TransactionType;
import com.adelok.wallet_service.entity.enumerated.WalletStatus;
import com.adelok.wallet_service.exception.InsufficientFundsException;
import com.adelok.wallet_service.exception.ResourceNotFoundException;
import com.adelok.wallet_service.exception.WalletAlreadyExistsException;
import com.adelok.wallet_service.exception.WalletNotActiveException;
import com.adelok.wallet_service.mapper.WalletMapper;
import com.adelok.wallet_service.repository.TransactionRepository;
import com.adelok.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletResponse create(CreateWalletRequest request) {
        if (walletRepository.existsByUserId(request.userId())) {
            throw new WalletAlreadyExistsException("Wallet already exists");
        }
        Wallet wallet = Wallet.builder()
                .userId(request.userId())
                .currency(request.currency())
                .build();
        Wallet saved = walletRepository.saveAndFlush(wallet);
        return walletMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WalletResponse getByUserId(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with userId " + userId));
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse deposit(UUID userId, DepositRequest depositRequest) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with userId " + userId));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(depositRequest.amount()));
        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.DEPOSIT)
                .amount(depositRequest.amount())
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse withdraw(UUID userId, WithdrawRequest withdrawRequest) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with userId " + userId));
        if (wallet.getAvailableBalance().compareTo(withdrawRequest.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(withdrawRequest.amount()));

        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.WITHDRAW)
                .amount(withdrawRequest.amount())
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(wallet);
    }
    // TODO: refactor hold/release/refund after order-service is implemented
    @Transactional
    public WalletResponse holdEscrow(UUID userId, HoldRequest holdRequest) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with userId " + userId));
        if(wallet.getStatus() != WalletStatus.ACTIVE){
            throw new WalletNotActiveException("Wallet is unavailable");
        }
        if (wallet.getAvailableBalance().compareTo(holdRequest.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(holdRequest.amount()));
        wallet.setHeldBalance(wallet.getHeldBalance().add(holdRequest.amount()));
        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.HOLD)
                .amount(holdRequest.amount())
                .dealId(holdRequest.dealId())
                .status(TransactionStatus.PENDING)
                .build();
        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(wallet);

    }
}

