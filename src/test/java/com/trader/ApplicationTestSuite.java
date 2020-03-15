package com.trader;

import com.squareup.okhttp.OkHttpClient;
import com.trader.account.*;
import com.trader.price.*;
import javafx.collections.*;
import org.junit.Test;

import java.text.NumberFormat;
import java.util.Currency;

import static org.mockito.Mockito.*;

public class ApplicationTestSuite {

    @Test
    public void testApplicationBasicStartup() {

        DataStorage dataStorageMock = mock(DataStorage.class);
        when(dataStorageMock.getPositions()).thenReturn(FXCollections.observableArrayList());
        when(dataStorageMock.getOrders()).thenReturn(FXCollections.observableArrayList());
        when(dataStorageMock.getBalance()).thenReturn(10000.0);
        doNothing().when(dataStorageMock).storeData();

        PriceFeed priceFeed = new PriceFeed("https://www.bitstamp.net/api/v2/ticker/btcusd", new OkHttpClient());
        PriceService priceService = new PriceService(1, priceFeed);

        ApplicationRunner applicationRunner = new ApplicationRunner();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setGroupingUsed(true);

        NumberFormat priceFormatter = NumberFormat.getNumberInstance();
        priceFormatter.setMinimumFractionDigits(2);
        priceFormatter.setGroupingUsed(true);

        Account account = new Account(dataStorageMock, currencyFormatter);

        applicationRunner.run(priceService, account, currencyFormatter, priceFormatter, 0.05);
    }
}
