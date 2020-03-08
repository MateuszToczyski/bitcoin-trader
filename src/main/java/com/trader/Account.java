package com.trader;

import java.util.*;

public class Account {

    private double balance;
    private Set<Position> positions;
    private Set<Order> orders;
    private DataStorage dataStorage;

    public Account(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        balance = dataStorage.getBalance();
        positions = dataStorage.getPositions();
        orders = dataStorage.getOrders();
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void storeData() {
        dataStorage.storeData();
    }
}
