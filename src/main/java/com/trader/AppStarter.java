package com.trader;

import javafx.stage.Stage;

public class AppStarter {

    public void start(UserInterfaceHandler userInterfaceHandler, PriceService priceService) throws InterruptedException {

        priceService.addObserver(userInterfaceHandler);

        new Thread(() -> {
            try {
                priceService.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        userInterfaceHandler.initiate();
    }
}