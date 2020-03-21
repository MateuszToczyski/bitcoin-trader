package com.trader;

import com.squareup.okhttp.OkHttpClient;
import com.trader.account.DataStorage;
import com.trader.price.PriceFeed;
import com.trader.price.PriceService;
import org.junit.jupiter.api.Test;

public class ApplicationTestSuite {

    @Test
    public void testApplicationBasicStartup() {
        DataStorage dataStorage = new DataStorage("src/test/resources/Account.json");
        PriceFeed priceFeed = new PriceFeed("https://www.bitstamp.net/api/v2/ticker/btcusd", new OkHttpClient());
        PriceService priceService = new PriceService(1, priceFeed);
        ApplicationRunner applicationRunner = new ApplicationRunner();
    }
}