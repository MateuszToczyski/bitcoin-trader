package com.trader;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ApplicationRunner extends Application implements PriceObserver {

    private SimpleStringProperty bidPriceProperty = new SimpleStringProperty();
    private SimpleStringProperty askPriceProperty = new SimpleStringProperty();
    private static PriceService priceService;

    public void run(PriceService priceService) {
        ApplicationRunner.priceService = priceService;
        launch();
    }

    @Override
    public void start(Stage primaryStage) {

        priceService.addObserver(this);

        new Thread(() -> {
            try {
                ApplicationRunner.priceService.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        primaryStage.setTitle("Bitcoin Trader");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(500);
        primaryStage.setScene(generateScene());

        primaryStage.show();
    }

    private Scene generateScene() {

        GridPane upperGridPane = new GridPane();

        upperGridPane.setAlignment(Pos.TOP_CENTER);
        upperGridPane.setHgap(20);
        upperGridPane.setVgap(10);

        Text textBid = new Text("Bid");
        textBid.textProperty().bind(bidPriceProperty);
        upperGridPane.add(textBid, 2, 1);

        Text textAsk = new Text("Ask");
        textAsk.textProperty().bind(askPriceProperty);
        upperGridPane.add(textAsk, 4, 1);

        Button buttonSell = new Button("SELL");
        buttonSell.setMinWidth(60);
        upperGridPane.add(buttonSell, 2, 2);

        Button buttonBuy = new Button("BUY");
        buttonBuy.setMinWidth(60);
        upperGridPane.add(buttonBuy, 4, 2);

        TextField textFieldNominal = new TextField();
        textFieldNominal.setMaxWidth(70);
        textFieldNominal.setText(String.valueOf(0.01));
        upperGridPane.add(textFieldNominal, 3, 2);

        TabPane tabPane = new TabPane();

        Tab tabPositions = new Tab("Positions");
        tabPositions.closableProperty().setValue(false);

        Tab tabOrders = new Tab("Orders");
        tabOrders.closableProperty().setValue(false);

        tabPane.getTabs().addAll(tabPositions, tabOrders);

        VBox vBox = new VBox();
        vBox.setSpacing(30);
        vBox.getChildren().addAll(upperGridPane, tabPane);

        return new Scene(vBox);
    }

    @Override
    public void update(double bidPrice, double askPrice) {
        bidPriceProperty.setValue(String.valueOf(bidPrice));
        askPriceProperty.setValue(String.valueOf(askPrice));
    }
}