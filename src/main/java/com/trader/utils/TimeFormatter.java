package com.trader.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatter {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public static String format(Date date) {
        return simpleDateFormat.format(date);
    }
}