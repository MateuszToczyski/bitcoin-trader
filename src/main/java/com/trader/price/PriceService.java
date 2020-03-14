package com.trader.price;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

public class PriceService {

    private double askPrice;
    private double bidPrice;
    private double spread;
    private boolean started = false;
    private PriceFeed priceFeed;
    private List<PriceObserver> observers = new LinkedList<>();

    public PriceService(double spread, PriceFeed priceFeed) {
        this.spread = spread;
        this.priceFeed = priceFeed;
        updatePrices();
    }

    public void start() {

        CompletableFuture.runAsync(() -> {

            started = true;

            while(started) {
                updatePrices();
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

    private void pause() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updatePrices() {
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
