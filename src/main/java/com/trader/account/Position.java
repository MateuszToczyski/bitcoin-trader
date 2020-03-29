package com.trader.account;

import com.trader.exceptions.InvalidNominalException;
import com.trader.exceptions.WrongSideException;
import com.trader.price.*;
import com.trader.utils.*;
import java.util.Objects;

public class Position implements PriceObserver {

    private static int maxId;

    private int id;
    private boolean open = true;
    private Side side;
    private double nominal;
    private double openPrice;
    private double margin;
    private double closePrice;
    private double profit;
    private Double stopLoss;
    private Double takeProfit;
    private boolean stopLossActivated;
    private boolean takeProfitActivated;

    public Position(Side side, double nominal, double openPrice, double closePrice, double marginRequirement) {

        if(nominal <= 0) {
            throw new InvalidNominalException();
        }

        this.side = side;
        this.nominal = nominal;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.margin = MathOperations.round(marginRequirement * nominal * openPrice, 2);

        maxId++;
        id = maxId;
    }

    @Override
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

        checkIfActivateStopLoss();
        checkIfActivateTakeProfit();
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

    public Double getStopLoss() {
        return stopLoss;
    }

    public Double getTakeProfit() {
        return takeProfit;
    }

    public boolean isStopLossActivated() {
        return stopLossActivated;
    }

    public boolean isTakeProfitActivated() {
        return takeProfitActivated;
    }

    public static void setMaxId(int value) {
        Position.maxId = value;
    }

    public void setStopLoss(Double stopLoss) {

        if(stopLoss == null) {
            this.stopLoss = null;
            return;
        }

        if(side.equals(Side.BUY) && stopLoss >= closePrice ||
                side.equals(Side.SELL) && stopLoss <= closePrice) {
            throw new WrongSideException();
        }

        this.stopLoss = stopLoss;
    }

    public void setTakeProfit(Double takeProfit) {

        if(takeProfit == null) {
            this.takeProfit = null;
            return;
        }

        if(side.equals(Side.BUY) && takeProfit <= closePrice ||
                side.equals(Side.SELL) && takeProfit >= closePrice) {
            throw new WrongSideException();
        }

        this.takeProfit = takeProfit;
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

    private void checkIfActivateStopLoss() {

        if(stopLoss == null) {
            return;
        }

        if (side.equals(Side.BUY) && closePrice <= stopLoss ||
                side.equals(Side.SELL) && closePrice >= stopLoss) {
            stopLossActivated = true;
        }
    }

    private void checkIfActivateTakeProfit() {

        if(takeProfit == null) {
            return;
        }

        if (side.equals(Side.BUY) && closePrice >= takeProfit ||
                side.equals(Side.SELL) && closePrice <= takeProfit) {
            takeProfitActivated = true;
        }
    }
}