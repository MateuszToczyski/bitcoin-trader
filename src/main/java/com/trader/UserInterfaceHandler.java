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

public class UserInterfaceHandler extends Application implements PriceObserver {

    private SimpleStringProperty bidPrice = new SimpleStringProperty();
    private SimpleStringProperty askPrice = new SimpleStringProperty();

    public void initiate() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {

        GridPane upperGridPane = generateUpperGridPaneWithElements();
        TabPane tabPane = generateTabPaneWithElements();

        VBox vBox = new VBox();
        vBox.setSpacing(30);
        vBox.getChildren().addAll(upperGridPane, tabPane);

        Scene scene = new Scene(vBox);

        primaryStage.setTitle("Bitcoin Trader");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(500);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    @Override
    public void update(double bidPrice, double askPrice) {
        this.bidPrice.setValue(String.valueOf(bidPrice));
        this.askPrice.setValue(String.valueOf(askPrice));
    }

    private GridPane generateUpperGridPaneWithElements() {

        GridPane gridPane = new GridPane();

        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setHgap(20);
        gridPane.setVgap(20);

        Text textBid = new Text();
        textBid.textProperty().bind(bidPrice);
        gridPane.add(textBid, 2, 1);

        Text textAsk = new Text("Ask:");
        gridPane.add(textAsk, 4, 1);

        Button buttonSell = new Button("SELL");
        buttonSell.setMinWidth(60);
        gridPane.add(buttonSell, 2, 2);

        Button buttonBuy = new Button("BUY");
        buttonBuy.setMinWidth(60);
        gridPane.add(buttonBuy, 4, 2);

        TextField textFieldNominal = new TextField();
        textFieldNominal.setMaxWidth(70);
        textFieldNominal.setText(String.valueOf(0.01));
        gridPane.add(textFieldNominal, 3, 2);

        return gridPane;
    }

    private TabPane generateTabPaneWithElements() {

        TabPane tabPane = new TabPane();

        Tab tabPositions = new Tab("Positions");
        tabPositions.closableProperty().setValue(false);

        Tab tabOrders = new Tab("Orders");
        tabOrders.closableProperty().setValue(false);

        tabPane.getTabs().addAll(tabPositions, tabOrders);

        return tabPane;
    }
}
