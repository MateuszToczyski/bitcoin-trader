package com.trader;

import org.junit.Test;

import java.util.*;

import static org.mockito.Mockito.*;

public class ApplicationTestSuite {

    @Test
    public void testApplicationBasicStartup() {

        DataStorage dataStorageMock = mock(DataStorage.class);
        when(dataStorageMock.getPositions()).thenReturn(new HashSet<>());
        when(dataStorageMock.getOrders()).thenReturn(new HashSet<>());
        when(dataStorageMock.getBalance()).thenReturn(10000.0);
        doNothing().when(dataStorageMock).storeData();

        PriceFeedStub priceFeedStub = new PriceFeedStub();
        PriceService priceService = new PriceService(5, priceFeedStub);
        Account account = new Account(dataStorageMock);
        ApplicationRunner applicationRunner = new ApplicationRunner();

        applicationRunner.run(priceService, account);
    }
}
