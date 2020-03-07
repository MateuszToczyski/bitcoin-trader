package com.trader;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Starter {

    public static void main(String[] args) {

        ApplicationRunner applicationRunner = new ApplicationRunner();
        PriceService priceService = new PriceService(5);

        applicationRunner.run(priceService);
    }
}