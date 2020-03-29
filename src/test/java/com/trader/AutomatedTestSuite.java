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

    @Test
    public void testCreateLimitOrder() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        Order order = new Order(Order.Type.LIMIT, Side.BUY, 10, 4900, account.getMarginRequirement());
        account.addOrder(order);

        Assertions.assertEquals(0, account.openPositions().size());
        Assertions.assertEquals(0, account.closedPositions().size());
        Assertions.assertEquals(1, account.orders().size());

        Assertions.assertEquals(9510, account.getBalance(), 0.0001);
        Assertions.assertEquals(490, account.getMargin(), 0.0001);
        Assertions.assertEquals(0, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(0, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testCreateAndExecuteLimitOrder() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        Order order = new Order(Order.Type.LIMIT, Side.BUY, 10, 4990, account.getMarginRequirement());
        account.addOrder(order);
        account.update(4989, 4990);

        Assertions.assertEquals(1, account.openPositions().size());
        Assertions.assertEquals(0, account.closedPositions().size());
        Assertions.assertEquals(0, account.orders().size());

        Assertions.assertEquals(9501, account.getBalance(), 0.0001);
        Assertions.assertEquals(499, account.getMargin(), 0.0001);
        Assertions.assertEquals(-10, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(20.02, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testCreateStopOrder() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        Order order = new Order(Order.Type.STOP, Side.BUY, 10, 5100, account.getMarginRequirement());
        account.addOrder(order);

        Assertions.assertEquals(0, account.openPositions().size());
        Assertions.assertEquals(0, account.closedPositions().size());
        Assertions.assertEquals(1, account.orders().size());

        Assertions.assertEquals(9490, account.getBalance(), 0.0001);
        Assertions.assertEquals(510, account.getMargin(), 0.0001);
        Assertions.assertEquals(0, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(0, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testCreateAndExecuteStopOrder() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        Order order = new Order(Order.Type.STOP, Side.BUY, 10, 5010, account.getMarginRequirement());
        account.addOrder(order);
        account.update(5009, 5010);

        Assertions.assertEquals(1, account.openPositions().size());
        Assertions.assertEquals(0, account.closedPositions().size());
        Assertions.assertEquals(0, account.orders().size());

        Assertions.assertEquals(9499, account.getBalance(), 0.0001);
        Assertions.assertEquals(501, account.getMargin(), 0.0001);
        Assertions.assertEquals(-10, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(19.9401, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testExecuteStopLoss() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        account.addPosition(Side.BUY, 10);
        Position position = account.openPositions().get(0);
        position.setStopLoss(4990.0);
        account.update(4990, 4991);

        Assertions.assertEquals(0, account.openPositions().size());
        Assertions.assertEquals(1, account.closedPositions().size());
        Assertions.assertEquals(0, account.orders().size());

        Assertions.assertEquals(9890, account.getBalance(), 0.0001);
        Assertions.assertEquals(0, account.getMargin(), 0.0001);
        Assertions.assertEquals(0, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(0, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testExecuteTakeProfit() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        account.addPosition(Side.BUY, 10);
        Position position = account.openPositions().get(0);
        position.setTakeProfit(5010.0);
        account.update(5010, 5011);

        Assertions.assertEquals(0, account.openPositions().size());
        Assertions.assertEquals(1, account.closedPositions().size());
        Assertions.assertEquals(0, account.orders().size());

        Assertions.assertEquals(10090, account.getBalance(), 0.0001);
        Assertions.assertEquals(0, account.getMargin(), 0.0001);
        Assertions.assertEquals(0, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(0, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testStopOut() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        account.addPosition(Side.BUY, 10);
        account.update(4010, 4011);

        Assertions.assertEquals(0, account.openPositions().size());
        Assertions.assertEquals(1, account.closedPositions().size());
        Assertions.assertEquals(0, account.orders().size());

        Assertions.assertEquals(90, account.getBalance(), 0.0001);
        Assertions.assertEquals(0, account.getMargin(), 0.0001);
        Assertions.assertEquals(0, account.getOpenProfit(), 0.0001);
        Assertions.assertEquals(0, account.getMarginLevel(), 0.0001);
    }

    @Test
    public void testBalanceExceededWhenOpeningPosition() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        Assertions.assertThrows(BalanceExceededException.class, () -> account.addPosition(Side.BUY, 200));
    }

    @Test
    public void textBalanceExceededWhenCreatingOrder() {
        Account account = new Account(0.01, 0.3, 10000, 0,
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

        account.update(5000, 5001);
        Order order = new Order(Order.Type.LIMIT, Side.BUY, 201, 4990, account.getMarginRequirement());
        Assertions.assertThrows(BalanceExceededException.class, () -> account.addOrder(order));
    }
}