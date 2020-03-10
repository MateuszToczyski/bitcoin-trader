package com.trader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ApplicationRunner extends Application implements PriceObserver {

    private SimpleStringProperty bidPriceProperty = new SimpleStringProperty();
    private SimpleStringProperty askPriceProperty = new SimpleStringProperty();
    private TableView<Position> tableViewPositions;
    private double marginRequirement = 0.05;

    private static PriceService priceService;
    private static Account account;

    public void run(PriceService priceService, Account account) {
        ApplicationRunner.priceService = priceService;
        ApplicationRunner.account = account;
        launch();
    }

    @Override
    public void start(Stage primaryStage) {

        priceService.addObserver(this);
        priceService.addObserver(account);
        priceService.start();

        primaryStage.setTitle("Bitcoin Trader");
        primaryStage.setWidth(900);
        primaryStage.setScene(generateScene());
        primaryStage.show();
    }

    @Override
    public void stop() {
        priceService.stop();
        account.storeData();
    }

    private Scene generateScene() {

        GridPane upperGridPane = new GridPane();

        upperGridPane.setAlignment(Pos.TOP_CENTER);
        upperGridPane.setHgap(20);
        upperGridPane.setVgap(10);
        upperGridPane.setPadding(new Insets(10, 0, 30, 0));

        Text textBid = new Text("Bid");
        textBid.textProperty().bind(bidPriceProperty);
        upperGridPane.add(textBid, 2, 1);

        Text textAsk = new Text("Ask");
        textAsk.textProperty().bind(askPriceProperty);
        upperGridPane.add(textAsk, 4, 1);

        TextField textFieldNominal = new TextField();
        textFieldNominal.setMaxWidth(70);
        textFieldNominal.setText(String.valueOf(0.01));
        upperGridPane.add(textFieldNominal, 3, 2);

        Button buttonSell = new Button("SELL");
        buttonSell.setMinWidth(60);
        buttonSell.setOnAction(event -> account.addPosition(new Position(Position.Side.SELL,
                Double.parseDouble(textFieldNominal.getText()), priceService.getBidPrice(), marginRequirement)));
        upperGridPane.add(buttonSell, 2, 2);

        Button buttonBuy = new Button("BUY");
        buttonBuy.setMinWidth(60);
        buttonBuy.setOnAction(event -> account.addPosition(new Position(Position.Side.BUY,
                Double.parseDouble(textFieldNominal.getText()), priceService.getAskPrice(), marginRequirement)));
        upperGridPane.add(buttonBuy, 4, 2);

        TabPane tabPane = new TabPane();
        tabPane.setMinHeight(300);

        Tab tabPositions = new Tab("Positions");
        tabPositions.closableProperty().setValue(false);

        tableViewPositions = new TableView<>();

        TableColumn<Position, Integer> columnPositionId = new TableColumn<>("ID");
        columnPositionId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Position, Position.Side> columnPositionSide = new TableColumn<>("Side");
        columnPositionSide.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Position, Integer> columnPositionNominal = new TableColumn<>("Nominal");
        columnPositionNominal.setCellValueFactory(new PropertyValueFactory<>("nominal"));

        TableColumn<Position, Double> columnPositionOpenPrice = new TableColumn<>("Open price");
        columnPositionOpenPrice.setCellValueFactory(new PropertyValueFactory<>("openPrice"));

        TableColumn<Position, Double> columnPositionProfit = new TableColumn<>("Profit");
        columnPositionProfit.setCellValueFactory(new PropertyValueFactory<>("profit"));

        TableColumn<Position, String> columnPositionAction = new TableColumn<>("");
        columnPositionAction.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<Position, String>, TableCell<Position, String>> cellFactory =
                new Callback<TableColumn<Position, String>, TableCell<Position, String>>() {
                    @Override
                    public TableCell<Position, String> call(final TableColumn<Position, String> param) {

                        Button closeButton = new Button("X");
                        closeButton.setPadding(new Insets(0, 4, 0, 4));

                        return new TableCell<Position, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {

                                super.updateItem(item, empty);

                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    closeButton.setOnAction(event -> {
                                        Position position = getTableView().getItems().get(getIndex());
                                        account.closePosition(position);
                                    });
                                    setGraphic(closeButton);
                                }

                                setText(null);
                                setPadding(new Insets(3));
                            }
                        };
                    }
                };

        columnPositionAction.setCellFactory(cellFactory);

        tableViewPositions.setItems(account.getOpenPositions());
        //noinspection unchecked
        tableViewPositions.getColumns().addAll(columnPositionId, columnPositionSide, columnPositionNominal,
                columnPositionOpenPrice, columnPositionProfit, columnPositionAction);

        tableViewPositions.getColumns().forEach(column -> column.setMinWidth(80));

        tabPositions.setContent(tableViewPositions);

        Tab tabOrders = new Tab("Orders");
        tabOrders.closableProperty().setValue(false);

        tabPane.getTabs().addAll(tabPositions, tabOrders);

        GridPane bottomGridPane = new GridPane();
        bottomGridPane.setAlignment(Pos.TOP_LEFT);
        bottomGridPane.setPadding(new Insets(15));
        bottomGridPane.setHgap(20);
        bottomGridPane.setVgap(5);
        bottomGridPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, Color.LIGHTGRAY, Color.LIGHTGRAY,
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
                BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));

        Text textBalanceLabel = new Text("Balance");
        bottomGridPane.add(textBalanceLabel, 1, 0);

        Text textBalanceAmount = new Text();
        textBalanceAmount.textProperty().bind(account.balanceProperty());
        bottomGridPane.add(textBalanceAmount, 1, 1);

        Button buttonDepositWithdrawal = new Button("Deposit / Withdrawal");
        buttonDepositWithdrawal.setMinHeight(35);
        buttonDepositWithdrawal.setOnAction(event -> depositWithdrawal());
        bottomGridPane.add(buttonDepositWithdrawal, 0, 0, 1, 2);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(upperGridPane, tabPane, bottomGridPane);

        return new Scene(vBox);
    }

    @Override
    public void update(double bidPrice, double askPrice) {
        bidPriceProperty.setValue(String.valueOf(bidPrice));
        askPriceProperty.setValue(String.valueOf(askPrice));
        Platform.runLater(() -> tableViewPositions.refresh());
    }

    private void depositWithdrawal() {

        Stage stage = new Stage();
        stage.setWidth(205);
        stage.setHeight(140);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(20);
        gridPane.setHgap(20);

        TextField textFieldAmount = new TextField();
        textFieldAmount.setMinWidth(150);
        gridPane.add(textFieldAmount, 0, 0, 2, 1);

        Button buttonDeposit = new Button("Deposit");
        buttonDeposit.setMinWidth(70);
        buttonDeposit.setOnAction(event -> {
            account.amendBalance(Double.parseDouble(textFieldAmount.getText()));
            stage.close();
        });
        gridPane.add(buttonDeposit, 0, 1);

        Button buttonWithdraw = new Button("Withdraw");
        buttonWithdraw.setMinWidth(70);
        buttonWithdraw.setOnAction(event -> {
            account.amendBalance( - Double.parseDouble(textFieldAmount.getText()));
            stage.close();
        });
        gridPane.add(buttonWithdraw, 1, 1);

        Scene scene = new Scene(gridPane);

        stage.setScene(scene);
        stage.show();
    }
}