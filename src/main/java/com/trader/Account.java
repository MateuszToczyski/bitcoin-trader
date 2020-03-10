package com.trader;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.text.NumberFormat;
import java.util.Currency;

public class Account implements PriceObserver {

    private DataStorage dataStorage;
    private double balance;
    private ObservableList<Position> openPositions;
    private ObservableList<Position> closedPositions;
    private ObservableList<Order> orders;
    private SimpleStringProperty balanceProperty;
    private NumberFormat formatter;

    public Account(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        balance = dataStorage.getBalance();
        openPositions = dataStorage.getPositions();
        closedPositions = FXCollections.observableArrayList();
        orders = dataStorage.getOrders();

        formatter = NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance("USD"));
        balanceProperty = new SimpleStringProperty(formatter.format(balance));
    }

    @Override
    public void update(double bidPrice, double askPrice) {
        openPositions.forEach(position -> position.update(bidPrice, askPrice));
    }

    public void addPosition(Position position) {
        openPositions.add(position);
    }

    public ObservableList<Position> getOpenPositions() {
        return openPositions;
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

    public void closePosition(Position position) {
        position.close();
        amendBalance(position.getProfit());
        openPositions.remove(position);
        closedPositions.add(position);
    }
}
