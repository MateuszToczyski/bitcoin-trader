package com.trader;

import java.util.Objects;

public class Position implements PriceObserver {

    enum Side {
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
        this.side = side;
        this.nominal = nominal;
        this.openPrice = openPrice;
        this.margin = marginRequirement * nominal * openPrice;

        idMax++;
        id = idMax;
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

    @Override
    public void update(double bidPrice, double askPrice) {

        if(!open) {
            return;
        }

        if(side.equals(Side.BUY)) {
            closePrice = bidPrice;
            profit = nominal * (closePrice - openPrice);
        } else {
            closePrice = askPrice;
            profit = nominal * (openPrice - closePrice);
        }
    }
}
