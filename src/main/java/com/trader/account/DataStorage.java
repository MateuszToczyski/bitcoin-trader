package com.trader.account;

import com.trader.ApplicationRunner;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class DataStorage {

    private String path;

    public DataStorage(String path) {
        this.path = path;
    }

    @SuppressWarnings("unchecked")
    public Account retrieveAccount() throws FileNotFoundException, NullPointerException {

        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(new FileReader(path), Map.class);

        List<Position> openPositions =
                positionsFromLinkedTreeMapList((List<LinkedTreeMap<String, Object>>) map.get("openPositions"));

        List<Position> closedPositions =
                positionsFromLinkedTreeMapList((List<LinkedTreeMap<String, Object>>) map.get("closedPositions"));

        List<Order> orders =
                ordersFromLinkedTreeMapList((List<LinkedTreeMap<String, Object>>) map.get("orders"));

        return new Account((double)map.get("balance"), (double)map.get("margin"),
                openPositions, closedPositions, orders);
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