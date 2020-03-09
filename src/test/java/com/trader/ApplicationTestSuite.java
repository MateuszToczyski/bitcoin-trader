package com.trader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;

import java.util.*;

import static org.mockito.Mockito.*;

public class ApplicationTestSuite {

    @Test
    public void testApplicationBasicStartup() {

        ObservableList<Position> positions = FXCollections.observableArrayList();
        positions.add(new Position(Position.Side.BUY, 5, 4800, 0.10));
        positions.add(new Position(Position.Side.BUY, 10, 4900, 0.10));
        positions.add(new Position(Position.Side.BUY, 15, 5000, 0.10));

        DataStorage dataStorageMock = mock(DataStorage.class);
        when(dataStorageMock.getPositions()).thenReturn(positions);
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
