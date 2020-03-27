package com.trader;

import com.squareup.okhttp.OkHttpClient;
import com.trader.account.*;
import com.trader.price.*;

public class Starter {

    public static void main(String[] args) {

        DataStorage dataStorage = new DataStorage("src/main/resources/Account.json");
        PriceFeed priceFeed = new PriceFeed("https://www.bitstamp.net/api/v2/ticker/btcusd", new OkHttpClient());
        PriceService priceService = new PriceService(1, priceFeed);
        ApplicationRunner applicationRunner = new ApplicationRunner();

        try {
            applicationRunner.run(priceService, dataStorage, 0.01, 0.3);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}