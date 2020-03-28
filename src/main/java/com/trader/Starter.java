package com.trader;

import com.trader.account.DataStorage;
import com.trader.price.PriceFeed;
import com.trader.price.PriceService;
import com.trader.utils.PropertyLoader;

public class Starter {

    public static void main(String[] args) throws Exception {

        PropertyLoader propertyLoader = new PropertyLoader("app.properties");
        DataStorage dataStorage = new DataStorage(propertyLoader.get("accountDataFile"));
        PriceFeed priceFeed = new PriceFeed(propertyLoader.get("singlePriceUrl"), propertyLoader.get("priceSetUrl"));
        PriceService priceService = new PriceService(1, priceFeed);
        ApplicationRunner applicationRunner = new ApplicationRunner();

        applicationRunner.run(priceService, dataStorage, 0.01, 0.3);
    }
}