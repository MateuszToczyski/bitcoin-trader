package com.trader;

import com.squareup.okhttp.OkHttpClient;
import com.trader.price.PriceFeed;

import java.util.Random;

public class PriceFeedStub extends PriceFeed {

    private double currentPrice = 5000;

    public PriceFeedStub(String url, OkHttpClient httpClient) {
        super(url, httpClient);
    }

    @Override
    public double nextPrice() {

        Random random = new Random();

        if(random.nextInt(2) == 0) {
            currentPrice *= 0.999;
        } else {
            currentPrice *= 1.001;
        }

        return currentPrice;
    }
}
