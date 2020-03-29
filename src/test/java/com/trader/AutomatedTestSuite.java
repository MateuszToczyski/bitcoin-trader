package com.trader;

import com.trader.account.*;
import com.trader.exceptions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class AutomatedTestSuite {

    @Test
    public void testOpenPosition() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        account.addPosition(Side.BUY, 10);
        account.update(5002, 5003);

        Position position = account.openPositions().get(0);
        Assertions.assertEquals(10, position.getProfit());

        Assertions.assertEquals(1, account.openPositions().size());
        Assertions.assertEquals(0, account.closedPositions().size());
        Assertions.assertEquals(0, account.orders().size());

        Assertions.assertEquals(9499.9, account.getBalance(), 0.0001);
        Assertions.assertEquals(500.1, account.getMargin(), 0.0001);
        Assertions.assertEquals(10, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(20.016, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testOpenAndClosePosition() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        account.addPosition(Side.BUY, 10);
        account.update(5002, 5003);

        Position position = account.openPositions().get(0);
        account.closePosition(position);

        Assertions.assertEquals(0, account.openPositions().size());
        Assertions.assertEquals(1, account.closedPositions().size());
        Assertions.assertEquals(0, account.orders().size());

        Assertions.assertEquals(10010, account.getBalance(), 0.0001);
        Assertions.assertEquals(0, account.getMargin(), 0.0001);
        Assertions.assertEquals(0, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(0, account.getMarginLevel(), 0.0001);
    }
}