package com.adelok.wallet_service.repository;

import com.adelok.wallet_service.entity.Transaction;
import com.adelok.wallet_service.entity.enumerated.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
