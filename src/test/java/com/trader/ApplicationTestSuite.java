package com.trader;

import com.trader.account.DataStorage;
import com.trader.price.PriceFeed;
import com.trader.price.PriceService;
import org.junit.jupiter.api.Test;

public class ApplicationTestSuite {

    @Test
    public void testApplicationBasicStartup() {
        DataStorage dataStorage = new DataStorage("src/test/resources/Account.json");
        PriceFeed priceFeed = new PriceFeedStub(PriceFeedStub.Direction.RANDOM, 0.0001, "https://example.com/");
        PriceService priceService = new PriceService(1, priceFeed);
        ApplicationRunner applicationRunner = new ApplicationRunner();

        try {
            applicationRunner.run(priceService, dataStorage);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}