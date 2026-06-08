package com.nik07roxx.apexPay.exceptions;

public class PaymentStrategyNotFound extends RuntimeException {
    public PaymentStrategyNotFound(String message) {
        super(message);
    }
}
