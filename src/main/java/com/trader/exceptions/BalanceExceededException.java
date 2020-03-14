package com.trader.exceptions;

public class BalanceExceededException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Balance exceeded";
    }
}