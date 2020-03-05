package com.trader;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        UserInterfaceHandler userInterfaceHandler = new UserInterfaceHandler();
        PriceService priceService = new PriceService(5);

        new AppStarter().start(userInterfaceHandler, priceService);
    }
}