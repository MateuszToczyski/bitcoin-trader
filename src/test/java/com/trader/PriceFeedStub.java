package com.trader;

import com.squareup.okhttp.OkHttpClient;
import com.trader.price.PriceFeed;

import java.util.Random;

public class PriceFeedStub extends PriceFeed {

    public enum Direction {
        UP,
        DOWN,
        RANDOM
    }

    private double currentPrice = 5000;
    private Random random = new Random();
    private double step;
    private Direction direction;

    public PriceFeedStub(Direction direction, double step) {
        super("http://www.example.com", new OkHttpClient());
        this.direction = direction;
        this.step = step;
    }

    @Override
    public double nextPrice() {

        if(direction.equals(Direction.UP)) {
            currentPrice *= (1 + step);
        } else if(direction.equals(Direction.DOWN)) {
            currentPrice *= (1 - step);
        } else if(direction.equals(Direction.RANDOM)) {
            currentPrice *= upOrDown();
        }

        return currentPrice;
    }

    public double upOrDown() {

        if(random.nextInt(2) == 0) {
            return 1 + step;
        } else {
            return 1 - step;
        }
    }
}
