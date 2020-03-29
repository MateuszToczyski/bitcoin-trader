package com.trader.account;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DataStorage {

    private String path;

    public DataStorage(String path) {
        this.path = path;
    }

    public Account retrieveAccount() throws IOException {

        File file = new File(path);
        File parent = new File(file.getParent());

        if(!parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdir();
        }

        String contents = new String(Files.readAllBytes(Paths.get(path)));
        JSONObject jsonObject = new JSONObject(contents);

        double marginRequirement = Double.parseDouble(jsonObject.get("marginRequirement").toString());
        double stopOutLevel = Double.parseDouble(jsonObject.get("stopOutLevel").toString());
        double balance = Double.parseDouble(jsonObject.get("balance").toString());
        double margin = Double.parseDouble(jsonObject.get("margin").toString());

        Gson gson = new Gson();

        Type positionListType = new TypeToken<List<Position>>(){}.getType();
        Type orderListType = new TypeToken<List<Order>>(){}.getType();

        List<Position> openPositions = gson.fromJson(jsonObject.get("openPositions").toString(), positionListType);
        List<Position> closedPositions = gson.fromJson(jsonObject.get("closedPositions").toString(), positionListType);
        List<Order> orders = gson.fromJson(jsonObject.get("orders").toString(), orderListType);

        return new Account(marginRequirement, stopOutLevel, balance, margin, openPositions, closedPositions, orders);
    }

    public void storeAccount(Account account) throws FileNotFoundException {

        Gson gson = new Gson();
        String jsonString = gson.toJson(account);

        try (PrintWriter writer = new PrintWriter(path)) {
            writer.println(jsonString);
        }
    }
}