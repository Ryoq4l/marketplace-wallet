package com.adelok.wallet_service.entity;

import com.adelok.wallet_service.entity.enumerated.Currency;
import com.adelok.wallet_service.entity.enumerated.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Builder.Default
    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "held_balance", nullable = false)
    private BigDecimal heldBalance = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name= "currency", nullable = false)
    private Currency currency = Currency.USD;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name= "status", nullable = false)
    private WalletStatus status = WalletStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BigDecimal getTotalBalance() {
        return availableBalance.add(heldBalance);
    }
}





