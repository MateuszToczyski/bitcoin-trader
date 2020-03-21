package com.trader.account;

import com.google.gson.reflect.TypeToken;
import com.trader.ApplicationRunner;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.json.*;

public class DataStorage {

    private String path;

    public DataStorage(String path) {
        this.path = path;
    }

    public Account retrieveAccount() throws IOException, NullPointerException {

        String contents = new String(Files.readAllBytes(Paths.get(path)));
        JSONObject jsonObject = new JSONObject(contents);

        double balance = Double.parseDouble(jsonObject.get("balance").toString());
        double margin = Double.parseDouble(jsonObject.get("margin").toString());

        Gson gson = new Gson();

        Type positionListType = new TypeToken<List<Position>>(){}.getType();
        Type orderListType = new TypeToken<List<Order>>(){}.getType();

        List<Position> openPositions = gson.fromJson(jsonObject.get("openPositions").toString(), positionListType);
        List<Position> closedPositions = gson.fromJson(jsonObject.get("closedPositions").toString(), positionListType);
        List<Order> orders = gson.fromJson(jsonObject.get("orders").toString(), orderListType);

        return new Account(balance, margin, openPositions, closedPositions, orders);
    }

    public void storeAccount(Account account) throws FileNotFoundException {

        Gson gson = new Gson();
        String jsonString = gson.toJson(account);

        try (PrintWriter writer = new PrintWriter(path)) {
            writer.println(jsonString);
        }
    }

    private List<Position> positionsFromLinkedTreeMapList(List<LinkedTreeMap<String, Object>> list) {

        if(list.size() == 0) {
            return null;
        }

        List<Position> positions = new ArrayList<>();

        for(LinkedTreeMap<String, Object> map : list) {

            Side side = Side.valueOf(map.get("side").toString());
            double nominal = (double)map.get("nominal");
            double openPrice = (double)map.get("openPrice");

            positions.add(new Position(side, nominal, openPrice, ApplicationRunner.getMarginRequirement()));
        }

        return positions;
    }

    private List<Order> ordersFromLinkedTreeMapList(List<LinkedTreeMap<String, Object>> list) {

        if(list.size() == 0) {
            return null;
        }

        List<Order> orders = new ArrayList<>();

        for(LinkedTreeMap<String, Object> map : list) {

            Side side = Side.valueOf(map.get("side").toString());
            double nominal = (double)map.get("nominal");
            double price = (double)map.get("price");

            orders.add(new Order(side, nominal, price));
        }

        return orders;
    }
}