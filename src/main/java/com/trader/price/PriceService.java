package com.trader.price;

import com.trader.utils.MathOperations;
import javafx.beans.property.SimpleStringProperty;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class PriceService {

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

    public SimpleStringProperty statusProperty() {
        return statusProperty;
    }

    public boolean isStarted() {
        return started;
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
        double mid = priceFeed.nextPrice();
        double ask = MathOperations.round(mid + spread / 2, 2);
        double bid = MathOperations.round(mid - spread / 2, 2);

        notifyObservers(bid, ask);
    }

    private void notifyObservers(double bid, double ask) {
        for(PriceObserver observer : observers) {
            observer.update(bid, ask);
        }
    }
}
