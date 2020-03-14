package com.trader;

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

        PriceFeedStub priceFeedStub = new PriceFeedStub();
        PriceService priceService = new PriceService(1, priceFeedStub);

        ApplicationRunner applicationRunner = new ApplicationRunner();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setGroupingUsed(true);

        NumberFormat priceFormatter = NumberFormat.getNumberInstance();
        priceFormatter.setMinimumFractionDigits(2);
        priceFormatter.setGroupingUsed(true);

        Account account = new Account(dataStorageMock, currencyFormatter);

        applicationRunner.run(priceService, account, currencyFormatter, priceFormatter);
    }
}
