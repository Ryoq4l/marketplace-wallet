package com.adelok.wallet_service.exception;

public class WalletNotActiveException extends RuntimeException {
    public WalletNotActiveException(String message) {
        super(message);
    }
}
