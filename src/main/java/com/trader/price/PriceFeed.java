package com.trader.price;

import com.squareup.okhttp.*;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalTime;

public class PriceFeed {

    public enum Status {
        OK,
        ERROR,
        LOADING
    }

    private Status status;
    private OkHttpClient httpClient;
    private Request request;
    private double currentPrice;
    private LocalTime lastSuccessTime;

    public PriceFeed(String url, OkHttpClient httpClient) {
        this.httpClient = httpClient;
        request = new Request.Builder().url(url).build();
        status = Status.LOADING;
        lastSuccessTime = LocalTime.now();
    }

    public double nextPrice() {
        try {
            currentPrice = getPriceFromProvider();
            status = Status.OK;
            lastSuccessTime = LocalTime.now();
            return currentPrice;
        } catch(Exception ex) {
            status = Status.ERROR;
            return currentPrice;
        }
    }

    public Status getStatus() {
        return status;
    }

    public LocalTime getLastSuccessTime() {
        return lastSuccessTime;
    }

    private double getPriceFromProvider() throws IOException {
        Response response = httpClient.newCall(request).execute();
        String responseText = response.body().string();
        JSONObject jsonObject = new JSONObject(responseText);
        String priceString = jsonObject.getString("last");
        return Double.parseDouble(priceString);
    }
}