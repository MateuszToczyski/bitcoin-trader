package com.trader.price;

public interface PriceObserver {
    void update(double bidPrice, double askPrice);
}