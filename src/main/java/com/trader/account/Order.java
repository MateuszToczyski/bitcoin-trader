package com.trader.account;

import com.trader.ApplicationRunner;

import java.util.Objects;

public class Order {

    public enum Type {
        LIMIT,
        STOP
    }

    private static int maxId;

    private int id;
    private Type type;
    private Side side;
    private double nominal;
    private double price;
    private boolean activated;
    private double margin;

    public Order(Type type, Side side, double nominal, double price) {
        this.type = type;
        this.side = side;
        this.nominal = nominal;
        this.price = price;
        margin = price * nominal * ApplicationRunner.getMarginRequirement();

        maxId++;
        id = maxId;
    }

    public void update(double bid, double ask) {

        if(activated) {
            return;
        }

        double activationPrice;

        if(side.equals(Side.BUY)) {
            activationPrice = ask;
        } else {
            activationPrice = bid;
        }

        if(
        ((type.equals(Type.LIMIT) && side.equals(Side.BUY) || type.equals(Type.STOP) && side.equals(Side.SELL)) && activationPrice <= price) ||
        ((type.equals(Type.LIMIT) && side.equals(Side.SELL) || type.equals(Type.STOP) && side.equals(Side.BUY)) && activationPrice >= price)) {

            activated = true;
        }
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Side getSide() {
        return side;
    }

    public double getNominal() {
        return nominal;
    }

    public double getPrice() {
        return price;
    }

    public double getMargin() {
        return margin;
    }

    public static void setMaxId(int maxId) {
        Order.maxId = maxId;
    }

    public boolean isActivated() {
        return activated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
