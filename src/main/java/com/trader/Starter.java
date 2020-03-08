package com.trader;

public class Starter {

    public static void main(String[] args) {

        ApplicationRunner applicationRunner = new ApplicationRunner();
        PriceService priceService = new PriceService(5);

        applicationRunner.run(priceService, new Account(new DataStorage()));
    }
}