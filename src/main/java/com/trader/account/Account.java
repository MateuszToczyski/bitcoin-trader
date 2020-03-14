package com.trader.account;

import com.trader.price.*;
import com.trader.exceptions.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.NumberFormat;

public class Account implements PriceObserver {

    private double balance;
    private double margin;
    private SimpleStringProperty balanceProperty;
    private SimpleStringProperty marginProperty;
    private DataStorage dataStorage;
    private ObservableList<Position> openPositions;
    private ObservableList<Position> closedPositions;
    private ObservableList<Order> orders;
    private NumberFormat currencyFormatter;

    public Account(DataStorage dataStorage, NumberFormat currencyFormatter) {
        this.dataStorage = dataStorage;
        this.currencyFormatter = currencyFormatter;
        balance = dataStorage.getBalance();
        closedPositions = FXCollections.observableArrayList();
        orders = dataStorage.getOrders();

        openPositions = FXCollections.observableArrayList();
        openPositions.addAll(dataStorage.getPositions());

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

    public void storeData() {
        dataStorage.storeData();
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
