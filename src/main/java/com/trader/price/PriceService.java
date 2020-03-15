package com.trader.price;

import javafx.beans.property.SimpleStringProperty;

import java.math.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class PriceService {

    private double askPrice;
    private double bidPrice;
    private double spread;
    private PriceFeed priceFeed;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private boolean started = false;
    private List<PriceObserver> observers = new LinkedList<>();
    private SimpleStringProperty statusProperty = new SimpleStringProperty();

    public PriceService(double spread, PriceFeed priceFeed) {
        this.spread = spread;
        this.priceFeed = priceFeed;
        update();
    }

    public void start() {
        CompletableFuture.runAsync(() -> {
            started = true;
            while(started) {
                update();
                notifyObservers();
                pause();
            }
        });
    }

    public void stop () {
        started = false;
    }

    public void addObserver(PriceObserver observer) {
        observers.add(observer);
    }

    public double getAskPrice() {
        return askPrice;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public SimpleStringProperty statusProperty() {
        return statusProperty;
    }

    private void pause() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void update() {
        statusProperty.setValue("Status: " + priceFeed.getStatus().name() + "\n" +
                priceFeed.getLastSuccessTime().format(dateFormatter));
        double midPrice = priceFeed.nextPrice();
        askPrice = round(midPrice + spread / 2);
        bidPrice = round(midPrice - spread / 2);
    }

    private void notifyObservers() {
        for(PriceObserver observer : observers) {
            observer.update(bidPrice, askPrice);
        }
    }

    private double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
