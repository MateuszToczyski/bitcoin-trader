package com.trader;

public interface PriceObserver {
    void update(double bidPrice, double askPrice);
}
