package com.adelok.wallet_service.repository;

import com.adelok.wallet_service.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    @Query("select w from Wallet w where w.userId = :userId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUserIdForUpdate(UUID userId);
}