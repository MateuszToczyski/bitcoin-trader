package com.trader;

import com.trader.price.DatePricePair;
import com.trader.price.PriceFeed;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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

    public PriceFeedStub(Direction direction, double step, String dummyUrl) {
        super(dummyUrl, dummyUrl);
        this.direction = direction;
        this.step = step;
    }

    @Override
    public double getNextPrice() {
        if(direction.equals(Direction.UP)) {
            currentPrice *= (1 + step);
        } else if(direction.equals(Direction.DOWN)) {
            currentPrice *= (1 - step);
        } else if(direction.equals(Direction.RANDOM)) {
            currentPrice *= upOrDown();
        }

        return currentPrice;
    }

    @Override
    public List<DatePricePair> getInitialPriceSet() {

        List<DatePricePair> list = new LinkedList<>();

        for(int i = 0; i < 50; i++) {
            DatePricePair pair = new DatePricePair(new Date(), 5000);
            list.add(pair);
        }

        return list;
    }

    public double upOrDown() {
        if(random.nextInt(2) == 0) {
            return 1 + step;
        } else {
            return 1 - step;
        }
    }
}
