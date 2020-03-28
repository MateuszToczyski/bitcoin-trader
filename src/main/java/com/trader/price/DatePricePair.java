package com.trader.price;

import java.util.Date;

public class DatePricePair {
    private Date date;
    private double price;

    public DatePricePair(Date date, double price) {
        this.date = date;
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public double getPrice() {
        return price;
    }
}
