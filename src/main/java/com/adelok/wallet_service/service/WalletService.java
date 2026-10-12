package com.adelok.wallet_service.service;

import com.adelok.wallet_service.dto.requests.*;
import com.adelok.wallet_service.dto.responses.WalletResponse;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;

    //TODO: DISPUTE REVERSE
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
    public WalletResponse releaseFunds(UUID buyerId, ReleaseRequest request) {
        if (transactionRepository.existsByDealIdAndType(request.dealId(), TransactionType.RELEASE)) {
            throw new IllegalStateException("Funds already been released");
        }
        Wallet buyerWallet = walletRepository.findByUserIdForUpdate(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("There's no buyer with id " + buyerId));
        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(request.sellerId())
                .orElseThrow(() -> new ResourceNotFoundException("There's no seller with id " + request.sellerId()));
        if (buyerWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException("Seems like your wallet was either blocked or closed, try again later: " + buyerWallet.getStatus());
        }
        if (buyerWallet.getHeldBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        buyerWallet.setHeldBalance(buyerWallet.getHeldBalance().subtract(request.amount()));
        sellerWallet.setHeldBalance(sellerWallet.getHeldBalance().add(request.amount()));
        Transaction transaction = Transaction.builder()
                .walletId(buyerWallet.getId())
                .type(TransactionType.RELEASE)
                .amount(request.amount())
                .dealId(request.dealId())
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.saveAndFlush(transaction);
        walletRepository.save(sellerWallet);
        walletRepository.save(buyerWallet);
        return walletMapper.toResponse(buyerWallet);
    }

    @Transactional
    public WalletResponse refundToBuyer(UUID buyerId, RefundRequest request) {
        if (transactionRepository.existsByDealIdAndType(request.dealId(), TransactionType.REFUND)) {
            throw new IllegalStateException("Refund has already been processed");
        }
        Wallet buyerWallet = walletRepository.findByUserIdForUpdate(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("There's no buyer with id " + buyerId));
        if (buyerWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException("Seems like your wallet was either blocked or closed, try again later: " + buyerWallet.getStatus());
        }
        if (buyerWallet.getHeldBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        buyerWallet.setHeldBalance(buyerWallet.getHeldBalance().subtract(request.amount()));
        buyerWallet.setAvailableBalance(buyerWallet.getAvailableBalance().add(request.amount()));
        Transaction transaction = Transaction.builder()
                .walletId(buyerWallet.getId())
                .type(TransactionType.REFUND)
                .amount(request.amount())
                .dealId(request.dealId())
                .status(TransactionStatus.COMPLETED)
                .build();
        walletRepository.save(buyerWallet);
        transactionRepository.saveAndFlush(transaction);
        return walletMapper.toResponse(buyerWallet);
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