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

public class Starter extends Application implements PriceObserver {

    private SimpleStringProperty bidPrice = new SimpleStringProperty();
    private SimpleStringProperty askPrice = new SimpleStringProperty();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Bitcoin Trader");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(500);
        primaryStage.setScene(generateScene());

        primaryStage.show();

        PriceService service = new PriceService(5);
        service.addObserver(this);

        new Thread(() -> {
            try {
                service.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Scene generateScene() {

        GridPane upperGridPane = new GridPane();

        upperGridPane.setAlignment(Pos.TOP_CENTER);
        upperGridPane.setHgap(20);
        upperGridPane.setVgap(10);

        Text textBid = new Text("Bid");
        textBid.textProperty().bind(bidPrice);
        upperGridPane.add(textBid, 2, 1);

        Text textAsk = new Text("Ask");
        textAsk.textProperty().bind(askPrice);
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
        this.bidPrice.setValue(String.valueOf(bidPrice));
        this.askPrice.setValue(String.valueOf(askPrice));
    }
}