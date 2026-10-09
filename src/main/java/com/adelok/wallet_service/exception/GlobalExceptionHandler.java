package com.adelok.wallet_service.exception;

import com.adelok.wallet_service.dto.error.ErrorCode;
import com.adelok.wallet_service.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handle(InsufficientFundsException exception, HttpServletRequest request) {
        log.warn("INSUFFICIENT FUNDS");
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ErrorCode.INSUFFICIENT_FUNDS,
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage(),
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errorResponse);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ResourceNotFoundException exception, HttpServletRequest request) {
        log.warn("RESOURCE NOT FOUND");
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ErrorCode.NOT_FOUND,
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handle(WalletAlreadyExistsException exception, HttpServletRequest request) {
        log.warn("WALLET ALREADY EXISTS");
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ErrorCode.WALLET_ALREADY_EXISTS,
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    @ExceptionHandler(WalletNotActiveException.class)
    public ResponseEntity<ErrorResponse> handle(WalletNotActiveException exception, HttpServletRequest request) {
        log.warn("WALLET NOT ACTIVE");
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ErrorCode.WALLET_NOT_ACTIVE,
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.warn("ARGUMENT NOT VALID");
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ErrorCode.VALIDATION_FAILED,
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception exception, HttpServletRequest request) {
        log.error("UNEXPECTED ERROR AT {}", request.getRequestURI(), exception);
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                ErrorCode.INTERNAL_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "UNEXPECTED ERROR",
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
