package com.trader.price;

import com.squareup.okhttp.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

public class PriceFeed {

    public enum Status {
        OK,
        ERROR,
        LOADING
    }

    private Status status;
    private OkHttpClient httpClient;
    private Request singlePriceRequest;
    private Request priceSetRequest;
    private double currentPrice;
    private LocalTime lastSuccessTime;

    public PriceFeed(String singlePriceUrl, String priceSetUrl) {
        httpClient = new OkHttpClient();
        singlePriceRequest = new Request.Builder().url(singlePriceUrl).build();
        priceSetRequest = new Request.Builder().url(priceSetUrl).build();
        status = Status.LOADING;
        lastSuccessTime = LocalTime.now();
    }

    public double getNextPrice() {
        try {
            currentPrice = getNextPriceFromProvider();
            status = Status.OK;
            lastSuccessTime = LocalTime.now();
        } catch(Exception ex) {
            status = Status.ERROR;
        }

        return currentPrice;
    }

    public List<DatePricePair> getInitialPriceSet() throws IOException {

        Response response = httpClient.newCall(priceSetRequest).execute();
        String responseText = response.body().string();
        JSONArray jsonArray = new JSONArray(responseText);

        List<DatePricePair> prices = new LinkedList<>();

        for(int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Date date = new Date(Long.parseLong(jsonObject.getString("date")) * 1000);
            double price = Double.parseDouble(jsonObject.getString("price"));

            prices.add(new DatePricePair(date, price));
        }

        removeDuplicates(prices);

        return prices;
    }

    public Status getStatus() {
        return status;
    }

    public LocalTime getLastSuccessTime() {
        return lastSuccessTime;
    }

    private void removeDuplicates(List<DatePricePair> list) {

        for(int i = list.size() - 1; i > 0; i--) {
            if(list.get(i).getDate().equals(list.get(i - 1).getDate())) {
                list.remove(i);
            }
        }

        for(int i = list.size() - 1; i > 0; i--) {
            if(list.get(i).getPrice() == list.get(i - 1).getPrice()) {
                list.remove(i);
            }
        }
    }

    private double getNextPriceFromProvider() throws IOException {
        Response response = httpClient.newCall(singlePriceRequest).execute();
        String responseText = response.body().string();
        JSONObject jsonObject = new JSONObject(responseText);
        String priceString = jsonObject.getString("last");
        return Double.parseDouble(priceString);
    }
}