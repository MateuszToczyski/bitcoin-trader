package com.trader.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {

    private Properties properties;

    public PropertyLoader(String path) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(path));
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
