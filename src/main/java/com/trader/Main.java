package com.trader;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Bitcoin Trader");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(500);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(20);
        grid.setVgap(20);

        Text textBid = new Text("Bid: 4.1590");
        grid.add(textBid, 2, 1);

        Text textAsk = new Text("Ask: 4.1610");
        grid.add(textAsk, 4, 1);

        Button buttonSell = new Button("SELL");
        buttonSell.setMinWidth(60);
        grid.add(buttonSell, 2, 2);

        Button buttonBuy = new Button("BUY");
        buttonBuy.setMinWidth(60);
        grid.add(buttonBuy, 4, 2);

        TextField textFieldNominal = new TextField();
        textFieldNominal.setMaxWidth(70);
        textFieldNominal.setText(String.valueOf(0.01));
        grid.add(textFieldNominal, 3, 2);

        Tab tabPositions = new Tab("Positions");
        tabPositions.closableProperty().setValue(false);

        Tab tabOrders = new Tab("Orders");
        tabOrders.closableProperty().setValue(false);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(tabPositions, tabOrders);

        VBox vBox = new VBox();
        vBox.setSpacing(30);
        vBox.getChildren().addAll(grid, tabPane);

        //scene
        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}