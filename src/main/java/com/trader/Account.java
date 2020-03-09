package com.trader;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

import java.text.NumberFormat;
import java.util.*;

public class Account {

    private DataStorage dataStorage;
    private double balance;
    private ObservableList<Position> positions;
    private Set<Order> orders;
    private SimpleStringProperty balanceProperty;
    private NumberFormat formatter;

    public Account(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        balance = dataStorage.getBalance();
        positions = dataStorage.getPositions();
        orders = dataStorage.getOrders();

        formatter = NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance("USD"));
        balanceProperty = new SimpleStringProperty(formatter.format(balance));
    }

    public ObservableList<Position> getPositions() {
        return positions;
    }

    public void amendBalance(double value) {
        this.balance += value;
        balanceProperty.setValue(formatter.format(balance));
    }

    public void storeData() {
        dataStorage.storeData();
    }

    public SimpleStringProperty balanceProperty() {
        return balanceProperty;
    }
}
