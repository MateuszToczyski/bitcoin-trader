package com.trader.account;

import com.trader.price.*;
import com.trader.exceptions.*;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

import com.trader.utils.MathOperations;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Account implements PriceObserver {

    private double balance;
    private double margin;
    private double openProfit;
    private ObservableList<Position> openPositions;
    private ObservableList<Position> closedPositions;
    private ObservableList<Order> orders;
    private transient SimpleStringProperty balanceProperty;
    private transient SimpleStringProperty marginProperty;
    private transient SimpleStringProperty openProfitProperty;
    private transient NumberFormat currencyFormatter;

    public Account(double balance, double margin, List<Position> openPositions,
                   List<Position> closedPositions, List<Order> orders) {

        this.balance = balance;
        this.margin = margin;

        if(openPositions == null) {
            this.openPositions = FXCollections.observableArrayList();
        } else {
            this.openPositions = FXCollections.observableArrayList(openPositions);
            openPositions.forEach(position -> openProfit += position.getProfit());
        }

        if(closedPositions == null) {
            this.closedPositions = FXCollections.observableArrayList();
        } else {
            this.closedPositions = FXCollections.observableArrayList(closedPositions);
        }

        if(orders == null) {
            this.orders = FXCollections.observableArrayList();
        } else {
            this.orders = FXCollections.observableArrayList(orders);
        }

        currencyFormatter = NumberFormat.getCurrencyInstance();
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setGroupingUsed(true);

        balanceProperty = new SimpleStringProperty(currencyFormatter.format(balance));
        marginProperty = new SimpleStringProperty(currencyFormatter.format(margin));
        openProfitProperty = new SimpleStringProperty(currencyFormatter.format(openProfit));
    }

    @Override
    public void update(double bidPrice, double askPrice) {
        openPositions.forEach(position -> position.update(bidPrice, askPrice));
        updateProfit();
    }

    public void addPosition(Position position) {

        if(position.getMargin() > balance) {
            throw new BalanceExceededException();
        }

        amendBalance(-position.getMargin());
        amendMargin(position.getMargin());
        openPositions.add(position);
        updateProfit();
    }

    public ObservableList<Position> openPositions() {
        return openPositions;
    }

    public ObservableList<Position> closedPositions() {
        return closedPositions;
    }

    public void amendBalance(double value) {

        if(balance + MathOperations.round(value, 2) < 0) {
            throw new BalanceExceededException();
        }

        this.balance += MathOperations.round(value, 2);
        balanceProperty.setValue(currencyFormatter.format(balance));
    }

    public void amendMargin(double value) {
        this.margin += MathOperations.round(value, 2);
        marginProperty.setValue(currencyFormatter.format(margin));
    }

    public SimpleStringProperty balanceProperty() {
        return balanceProperty;
    }

    public SimpleStringProperty marginProperty() {
        return marginProperty;
    }

    public SimpleStringProperty openProfitProperty() {
        return openProfitProperty;
    }

    public void closePosition(Position position) {
        position.close();
        amendBalance(position.getProfit() + position.getMargin());
        amendMargin(-position.getMargin());
        openPositions.remove(position);
        closedPositions.add(position);
        updateProfit();
    }

    public double getBalance() {
        return balance;
    }

    private void updateProfit() {
        openProfit = 0;
        openPositions.forEach(position -> openProfit += position.getProfit());
        openProfitProperty.setValue(currencyFormatter.format(openProfit));
    }
}