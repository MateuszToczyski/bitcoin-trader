package com.trader;

import javafx.collections.*;
import org.junit.Test;

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
        Account account = new Account(dataStorageMock);
        ApplicationRunner applicationRunner = new ApplicationRunner();

        applicationRunner.run(priceService, account);
    }
}
