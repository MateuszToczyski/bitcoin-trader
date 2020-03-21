package com.trader.account;

public class Order {

    public enum Type {
        LIMIT,
        STOP
    }

    private Side side;
    private double nominal;
    private double price;

    public Order(Side side, double nominal, double price) {
        this.side = side;
        this.nominal = nominal;
        this.price = price;
    }
}
