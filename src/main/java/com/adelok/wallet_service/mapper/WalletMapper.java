package com.adelok.wallet_service.mapper;

import com.adelok.wallet_service.dto.WalletResponse;
import com.adelok.wallet_service.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {
    public WalletResponse toResponse(Wallet wallet){
        if (wallet==null) return null;
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .availableBalance(wallet.getAvailableBalance())
                .heldBalance(wallet.getHeldBalance())
                .totalBalance(wallet.getTotalBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}
