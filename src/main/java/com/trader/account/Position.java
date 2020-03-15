package com.trader.account;

import com.trader.exceptions.InvalidNominalException;
import com.trader.price.*;
import com.trader.utils.*;

import java.util.Objects;

public class Position implements PriceObserver {

    public enum Side {
        BUY,
        SELL
    }

    private static int idMax;

    private int id;
    private boolean open = true;
    private Side side;
    private double nominal;
    private double openPrice;
    private double margin;
    private double closePrice;
    private double profit;
    private Order stopLossOrder;
    private Order takeProfitOrder;

    public Position(Side side, double nominal, double openPrice, double marginRequirement) {

        if(nominal <= 0) {
            throw new InvalidNominalException();
        }

        this.side = side;
        this.nominal = nominal;
        this.openPrice = openPrice;
        this.margin = MathOperations.round(marginRequirement * nominal * openPrice, 2);

        idMax++;
        id = idMax;
    }

    public void update(double bidPrice, double askPrice) {

        if(!open) {
            return;
        }

        if(side.equals(Side.BUY)) {
            closePrice = bidPrice;
            profit = MathOperations.round(nominal * (closePrice - openPrice), 2);
        } else {
            closePrice = askPrice;
            profit = MathOperations.round(nominal * (openPrice - closePrice), 2);
        }
    }

    public void close() {
        open = false;
    }

    public int getId() {
        return id;
    }

    public boolean isOpen() {
        return open;
    }

    public Side getSide() {
        return side;
    }

    public double getNominal() {
        return nominal;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getProfit() {
        return profit;
    }

    public double getMargin() {
        return margin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return id == position.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
