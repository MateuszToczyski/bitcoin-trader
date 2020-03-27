package com.trader.account;

import com.trader.ApplicationRunner;
import com.trader.exceptions.BalanceExceededException;
import com.trader.exceptions.WrongSideException;
import com.trader.price.PriceObserver;
import com.trader.utils.MathOperations;
import com.trader.utils.NumberFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Account implements PriceObserver {

    private double balance;
    private double margin;
    private double openProfit;
    private ObservableList<Position> openPositions;
    private ObservableList<Position> closedPositions;
    private ObservableList<Order> orders;
    private transient double currentBid;
    private transient double currentAsk;
    private transient SimpleStringProperty balanceProperty;
    private transient SimpleStringProperty marginProperty;
    private transient SimpleStringProperty openProfitProperty;
    private transient SimpleStringProperty marginLevelProperty;

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

        balanceProperty = new SimpleStringProperty(NumberFormatter.currencyFormat(balance));
        marginProperty = new SimpleStringProperty(NumberFormatter.currencyFormat(margin));
        openProfitProperty = new SimpleStringProperty();
        marginLevelProperty = new SimpleStringProperty();

        Position.setMaxId(findMaxPositionId());
        Order.setMaxId(findMaxOrderId());

        updateProfitAndMarginLevel();
    }

    @Override
    public void update(double bid, double ask) {

        currentBid = bid;
        currentAsk = ask;

        for(int i = orders.size() - 1; i >= 0; i--) {
            Order order = orders.get(i);
            order.update(bid, ask);
            if(order.isActivated()) {
                addPosition(order.getSide(), order.getNominal());
                cancelOrder(order);
            }
        }

        for(int i = openPositions.size() - 1; i >= 0; i--) {
            Position position = openPositions.get(i);
            position.update(currentBid, currentAsk);
            if(position.isStopLossActivated() || position.isTakeProfitActivated()) {
                closePosition(position);
            }
        }

        updateProfitAndMarginLevel();
    }

    public void addPosition(Side side, double nominal) {

        double openPrice;

        if(side.equals(Side.BUY)) {
            openPrice = currentAsk;
        } else {
            openPrice = currentBid;
        }

        Position position = new Position(side, nominal, openPrice, ApplicationRunner.getMarginRequirement());

        if(position.getMargin() > balance) {
            throw new BalanceExceededException();
        }

        amendBalance(-position.getMargin());
        amendMargin(position.getMargin());
        openPositions.add(position);
        updateProfitAndMarginLevel();
    }

    public void closePosition(Position position) {
        position.close();
        amendBalance(position.getProfit() + position.getMargin());
        amendMargin(-position.getMargin());
        openPositions.remove(position);
        closedPositions.add(position);
        updateProfitAndMarginLevel();
    }

    public void addOrder(Order order) {

        if(order.getMargin() > balance) {
            throw new BalanceExceededException();
        } else if(!orderPriceValid(order)) {
            throw new WrongSideException();
        } else {
            orders.add(order);
            amendBalance(-order.getMargin());
            amendMargin(order.getMargin());
        }
    }

    public void cancelOrder(Order order) {
        orders.remove(order);
        amendBalance(order.getMargin());
        amendMargin(-order.getMargin());
    }

    public ObservableList<Position> openPositions() {
        return openPositions;
    }

    public ObservableList<Position> closedPositions() {
        return closedPositions;
    }

    public ObservableList<Order> orders() {
        return orders;
    }

    public void amendBalance(double value) {

        if(balance + MathOperations.round(value, 2) < 0) {
            throw new BalanceExceededException();
        }

        balance += MathOperations.round(value, 2);

        if(Math.abs(balance) < 0.001) {
            balance = 0;
        }

        balanceProperty.setValue(NumberFormatter.currencyFormat(balance));
    }

    public void amendMargin(double value) {

        margin += MathOperations.round(value, 2);

        if(Math.abs(margin) < 0.001) {
            margin = 0;
        }

        marginProperty.setValue(NumberFormatter.currencyFormat(margin));
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

    public SimpleStringProperty marginLevelProperty() {
        return marginLevelProperty;
    }

    public double getBalance() {
        return balance;
    }

    private void updateProfitAndMarginLevel() {

        openProfit = 0;
        openPositions.forEach(position -> openProfit += position.getProfit());
        openProfitProperty.setValue(NumberFormatter.currencyFormat(openProfit));

        double marginLevel;

        if(Math.abs(margin) > 0.001) {
            marginLevel = (balance + margin + openProfit) / margin;
        } else {
            marginLevel = 0;
        }

        if(marginLevel != 0) {
            marginLevelProperty.setValue(NumberFormatter.percentFormat(marginLevel));
            if(marginLevel < ApplicationRunner.getStopOutLevel()) {
                executeStopOut();
                updateProfitAndMarginLevel();
            }
        } else {
            marginLevelProperty.setValue("-");
        }
    }

    private int findMaxPositionId() {

        List<Position> positions = Stream
                .concat(openPositions.stream(), closedPositions.stream())
                .collect(Collectors.toList());

        return positions.stream()
                .map(Position::getId)
                .max(Comparator.comparing(id -> id))
                .orElse(0);
    }

    private int findMaxOrderId() {
        return orders.stream()
                .map(Order::getId)
                .max(Comparator.comparing(id -> id))
                .orElse(0);
    }

    private void executeStopOut() {
        openPositions.stream()
                .min(Comparator.comparing(Position::getProfit))
                .ifPresent(this::closePosition);
    }

    private boolean orderPriceValid(Order order) {
        return order.getType().equals(Order.Type.LIMIT) ||
                order.getSide().equals(Side.BUY) && order.getPrice() > currentAsk ||
                order.getSide().equals(Side.SELL) && order.getPrice() < currentBid;
    }
}