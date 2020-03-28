package com.trader.utils;

import java.text.NumberFormat;
import java.util.Currency;

public class NumberFormatter {

    private static NumberFormat currencyFormatter;
    private static NumberFormat priceFormatter;
    private static NumberFormat percentFormatter;

    static {
        currencyFormatter = NumberFormat.getCurrencyInstance();
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setGroupingUsed(true);

        priceFormatter = NumberFormat.getNumberInstance();
        priceFormatter.setMinimumFractionDigits(2);
        priceFormatter.setGroupingUsed(true);

        percentFormatter = NumberFormat.getPercentInstance();
        percentFormatter.setMinimumFractionDigits(2);
    }

    public static String currencyFormat(double value) {
        return currencyFormatter.format(value);
    }

    public static String priceFormat(double value) {
        return priceFormatter.format(value);
    }

    public static String percentFormat(double value) {
        return percentFormatter.format(value);
    }
}