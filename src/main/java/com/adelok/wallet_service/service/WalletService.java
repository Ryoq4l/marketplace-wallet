package com.adelok.wallet_service.service;

import com.adelok.wallet_service.dto.requests.*;
import com.adelok.wallet_service.dto.responses.WalletResponse;
import com.adelok.wallet_service.entity.Transaction;
import com.adelok.wallet_service.entity.Wallet;
import com.adelok.wallet_service.entity.enumerated.TransactionStatus;
import com.adelok.wallet_service.entity.enumerated.TransactionType;
import com.adelok.wallet_service.entity.enumerated.WalletStatus;
import com.adelok.wallet_service.mapper.WalletMapper;
import com.adelok.wallet_service.repository.TransactionRepository;
import com.adelok.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.adelok.common.exception.InsufficientFundsException;
import org.adelok.common.exception.ResourceNotFoundException;
import org.adelok.common.exception.WalletNotActiveException;
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
            Wallet userWallet = walletRepository.findByUserId(request.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
            return walletMapper.toResponse(userWallet);
        }
        Wallet wallet = Wallet.builder()
                .userId(request.userId())
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

        if (depositRequest.dealId() != null &&
                transactionRepository.existsByDealIdAndType(depositRequest.dealId(), TransactionType.DEPOSIT)) {
            throw new IllegalStateException("Deposit already processed for this deal");
        }

        if (depositRequest.operationId() != null &&
                transactionRepository.existsByOperationIdAndType(depositRequest.operationId(), TransactionType.DEPOSIT)) {
            throw new IllegalStateException("Deposit already processed for this operation");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with userId " + userId));

        wallet.setAvailableBalance(wallet.getAvailableBalance().add(depositRequest.amount()));

        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.DEPOSIT)
                .amount(depositRequest.amount())
                .dealId(depositRequest.dealId())
                .operationId(depositRequest.operationId())
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse withdraw(UUID userId, WithdrawRequest withdrawRequest) {
        if (withdrawRequest.dealId() != null &&
                transactionRepository.existsByDealIdAndType(withdrawRequest.dealId(), TransactionType.DEPOSIT)) {
            throw new IllegalStateException("Withdraw already processed for this deal");
        }
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
                .dealId(withdrawRequest.dealId())
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse holdEscrow(UUID userId, HoldRequest holdRequest) {
        if (transactionRepository.existsByDealIdAndType(holdRequest.dealId(), TransactionType.HOLD)) {
            throw new IllegalStateException("Hold already exists for deal");
        }
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with userId " + userId));
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
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
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(wallet);
    }
    @Transactional
    public WalletResponse completeToSeller(UUID sellerId, CompleteRequest request) {
        if (transactionRepository.existsByDealIdAndType(request.dealId(), TransactionType.COMPLETE)) {
            throw new IllegalStateException("Complete already processed for this deal");
        }
        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("There's no seller with id " + sellerId));
        if (sellerWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException("Seems like sellers wallet was either blocked or closed, try again later: " + sellerWallet.getStatus());
        }
        if (sellerWallet.getHeldBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        sellerWallet.setHeldBalance(sellerWallet.getHeldBalance().subtract(request.amount()));
        sellerWallet.setAvailableBalance(sellerWallet.getAvailableBalance().add(request.amount()));
        Transaction transaction = Transaction.builder()
                .walletId(sellerWallet.getId())
                .type(TransactionType.COMPLETE)
                .amount(request.amount())
                .dealId(request.dealId())
                .status(TransactionStatus.COMPLETED)
                .build();
        walletRepository.save(sellerWallet);
        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(sellerWallet);
    }

}