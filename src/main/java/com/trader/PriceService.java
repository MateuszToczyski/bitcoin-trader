package com.trader;

import javafx.beans.property.SimpleStringProperty;
import sun.java2d.pipe.SpanShapeRenderer;

import java.math.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PriceService {

    private double midPrice = 5000;
    private double askPrice;
    private double bidPrice;
    private double spread;
    private List<PriceObserver> observers = new LinkedList<>();

    public PriceService(double spread) {
        this.spread = spread;
        updateBidAndAsk();
    }

    public void start() throws InterruptedException {

        while(true) {
            updatePrices();
            notifyObservers();
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public void addObserver(PriceObserver observer) {
        observers.add(observer);
    }

    public SimpleStringProperty bidPriceProperty() {
        return new SimpleStringProperty(String.valueOf(bidPrice));
    }

    public SimpleStringProperty askPriceProperty() {
        return new SimpleStringProperty(String.valueOf(askPrice));
    }

    private void updatePrices() { //randomly increases or decreases prices

        int randomNumber = new Random().nextInt(2);

        if(randomNumber == 0) {
            midPrice *= 0.999;
        } else {
            midPrice *= 1.001;
        }

        updateBidAndAsk();
    }

    private void updateBidAndAsk() {
        askPrice = round(midPrice + spread / 2);
        bidPrice = round(midPrice - spread / 2);
    }

    private void notifyObservers() {
        for(PriceObserver observer : observers) {
            observer.update(bidPrice, askPrice);
        }
    }

    private double round(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
