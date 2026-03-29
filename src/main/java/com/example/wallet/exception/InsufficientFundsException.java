package com.example.wallet.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Insufficient balance for this transaction");
    }
}
