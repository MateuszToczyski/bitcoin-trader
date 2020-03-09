package com.trader;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import java.text.NumberFormat;
import java.util.Currency;

public class Account implements PriceObserver {

    private DataStorage dataStorage;
    private double balance;
    private ObservableList<Position> positions;
    private  ObservableList<Order> orders;
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

    @Override
    public void update(double bidPrice, double askPrice) {
        positions.forEach(position -> position.update(bidPrice, askPrice));
    }

    public void addPosition(Position position) {
        positions.add(position);
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
