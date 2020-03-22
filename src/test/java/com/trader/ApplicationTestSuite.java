package com.trader;

import com.trader.account.DataStorage;
import com.trader.price.PriceFeed;
import com.trader.price.PriceService;
import org.junit.jupiter.api.Test;

public class ApplicationTestSuite {

    @Test
    public void testApplicationBasicStartup() {

        DataStorage dataStorage = new DataStorage("src/test/resources/Account.json");
        PriceFeed priceFeed = new PriceFeedStub(PriceFeedStub.Direction.DOWN, 0.01);
        PriceService priceService = new PriceService(1, priceFeed);
        ApplicationRunner applicationRunner = new ApplicationRunner();

        try {
            applicationRunner.run(priceService, dataStorage, 0.05, 0.3);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}