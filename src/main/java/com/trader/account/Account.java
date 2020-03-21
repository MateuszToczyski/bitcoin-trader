package com.trader.account;

import com.trader.price.*;
import com.trader.exceptions.*;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Account implements PriceObserver {

    private double balance;
    private double margin;
    private ObservableList<Position> openPositions;
    private ObservableList<Position> closedPositions;
    private ObservableList<Order> orders;
    private transient SimpleStringProperty balanceProperty;
    private transient SimpleStringProperty marginProperty;
    private transient NumberFormat currencyFormatter;

    public Account(double balance, double margin, List<Position> openPositions,
                   List<Position> closedPositions, List<Order> orders) {

        this.balance = balance;
        this.margin = margin;

        if(openPositions == null) {
            this.openPositions = FXCollections.observableArrayList();
        } else {
            this.openPositions = FXCollections.observableArrayList(openPositions);
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
    }

    @Override
    public void update(double bidPrice, double askPrice) {
        openPositions.forEach(position -> position.update(bidPrice, askPrice));
    }

    public void addPosition(Position position) {

        if(position.getMargin() > balance) {
            throw new BalanceExceededException();
        }

        amendBalance(-position.getMargin());
        amendMargin(position.getMargin());
        openPositions.add(position);
    }

    public ObservableList<Position> openPositions() {
        return openPositions;
    }

    public void amendBalance(double value) {

        if(balance + value < 0) {
            throw new BalanceExceededException();
        }

        this.balance += value;
        balanceProperty.setValue(currencyFormatter.format(balance));
    }

    public void amendMargin(double value) {
        this.margin += value;
        marginProperty.setValue(currencyFormatter.format(margin));
    }

    public SimpleStringProperty balanceProperty() {
        return balanceProperty;
    }

    public SimpleStringProperty marginProperty() {
        return marginProperty;
    }

    public void closePosition(Position position) {
        position.close();
        amendBalance(position.getProfit() + position.getMargin());
        amendMargin(-position.getMargin());
        openPositions.remove(position);
        closedPositions.add(position);
    }

    public double getBalance() {
        return balance;
    }
}
