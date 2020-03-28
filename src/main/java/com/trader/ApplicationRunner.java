package com.trader;

import com.trader.account.*;
import com.trader.exceptions.BalanceExceededException;
import com.trader.exceptions.InvalidNominalException;
import com.trader.exceptions.WrongSideException;
import com.trader.price.DatePricePair;
import com.trader.price.PriceObserver;
import com.trader.price.PriceService;
import com.trader.utils.MathOperations;
import com.trader.utils.NumberFormatter;
import com.trader.utils.TimeFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ApplicationRunner extends Application implements PriceObserver {

    private SimpleStringProperty bidPriceProperty = new SimpleStringProperty();
    private SimpleStringProperty askPriceProperty = new SimpleStringProperty();
    private SimpleStringProperty requiredMarginProperty = new SimpleStringProperty();
    private StringBuilder depositWithdrawalInput = new StringBuilder();
    private StringBuilder nominalInput = new StringBuilder();
    private TableView<Position> tableViewOpenPositions;
    private TextField textFieldNominal;
    private Stage primaryStage;

    private static boolean restarted;
    private static double marginRequirement;
    private static double stopOutLevel;
    private static PriceService priceService;
    private static DataStorage dataStorage;
    private static Account account;
    private static XYChart.Series<String, Number> priceDataSeries;
    private static NumberAxis yAxis;

    public void run(PriceService priceService, DataStorage dataStorage, double marginRequirement, double stopOutLevel)
            throws IOException {

        ApplicationRunner.priceService = priceService;
        ApplicationRunner.dataStorage = dataStorage;
        ApplicationRunner.marginRequirement = marginRequirement;
        ApplicationRunner.stopOutLevel = stopOutLevel;

        priceDataSeries = new XYChart.Series<>();
        loadInitialPriceSet();

        launch();
    }

    @Override
    public void start(Stage primaryStage) {

        retrieveOrCreateAccount();

        if(account == null) {
            return;
        }

        this.primaryStage = primaryStage;

        priceService.addObserver(this);
        priceService.addObserver(account);

        if(!priceService.isStarted()) {
            priceService.start();
        }

        primaryStage.setTitle("Bitcoin Trader");
        primaryStage.setWidth(900);
        primaryStage.setScene(generateScene());
        primaryStage.show();
    }

    @Override
    public void stop() throws FileNotFoundException {

        priceService.stop();

        if(account != null) {
            dataStorage.storeAccount(account);
        }
    }

    @Override
    public void update(double bid, double ask) {

        bidPriceProperty.setValue(String.valueOf(bid));
        askPriceProperty.setValue(String.valueOf(ask));

        updateMarginProperty();

        Platform.runLater(() -> {
            tableViewOpenPositions.refresh();
            appendPriceDataSeries(bid);
            updateChartNumberAxisBounds();
        });
    }

    public static double getStopOutLevel() {
        return stopOutLevel;
    }

    public static double getMarginRequirement() {
        return marginRequirement;
    }

    private Scene generateScene() {

        VBox upperVBox = new VBox();

        GridPane chartGridPane = new GridPane();
        chartGridPane.setAlignment(Pos.TOP_CENTER);
        chartGridPane.setMaxHeight(250);

        LineChart<String, Number> chart = generateChart();
        chartGridPane.add(chart, 0, 0);

        GridPane tradingGridPane = new GridPane();

        tradingGridPane.setAlignment(Pos.TOP_CENTER);
        tradingGridPane.setHgap(20);
        tradingGridPane.setVgap(10);
        tradingGridPane.setPadding(new Insets(0, 0, 30, 0));

        Text textBid = new Text("Bid");
        textBid.textProperty().bind(bidPriceProperty);
        tradingGridPane.add(textBid, 0, 1);

        Text textAsk = new Text("Ask");
        textAsk.textProperty().bind(askPriceProperty);
        tradingGridPane.add(textAsk, 2, 1);

        Button buttonPendingOrder = new Button("+");
        GridPane.setHalignment(buttonPendingOrder, HPos.CENTER);
        GridPane.setValignment(buttonPendingOrder, VPos.BOTTOM);
        buttonPendingOrder.setTooltip(new Tooltip("Pending order"));
        buttonPendingOrder.setPadding(new Insets(0, 4, 0, 4));
        buttonPendingOrder.setOnAction(event -> newOrderCreator());
        tradingGridPane.add(buttonPendingOrder, 1, 1);

        textFieldNominal = new TextField(NumberFormatter.priceFormat(0));
        textFieldNominal.setMaxWidth(70);
        textFieldNominal.setOnKeyPressed(event -> Platform.runLater(() -> {
            appendNominalInput(event);
            textFieldNominal.setText(formatNominalInput());
            updateMarginProperty();
        }));
        tradingGridPane.add(textFieldNominal, 1, 2);

        Button buttonSell = new Button("SELL");
        buttonSell.setMinWidth(60);
        buttonSell.setOnAction(event -> {
            double openNominal = nominalInput.toString().equals("") ? 0 : Double.parseDouble(nominalInput.toString());
            tryAddPosition(Side.SELL, openNominal);
        });
        tradingGridPane.add(buttonSell, 0, 2);

        Button buttonBuy = new Button("BUY");
        buttonBuy.setMinWidth(60);
        buttonBuy.setOnAction(event -> {
            double openNominal = nominalInput.toString().equals("") ? 0 : Double.parseDouble(nominalInput.toString());
            tryAddPosition(Side.BUY, openNominal);
        });
        tradingGridPane.add(buttonBuy, 2, 2);

        Text textMarginRequired = new Text();
        textMarginRequired.textProperty().bind(requiredMarginProperty);
        tradingGridPane.add(textMarginRequired, 1, 3);

        upperVBox.getChildren().addAll(chartGridPane, tradingGridPane);

        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(300);

        Tab tabPositions = new Tab("Positions");

        tableViewOpenPositions = new TableView<>();
        tableViewOpenPositions.setPlaceholder(new Label(""));

        TableColumn<Position, Integer> colOpenPositionId = new TableColumn<>("ID");
        colOpenPositionId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Position, Side> colOpenPositionSide = new TableColumn<>("Side");
        colOpenPositionSide.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Position, Double> colOpenPositionNominal = new TableColumn<>("Nominal");
        colOpenPositionNominal.setCellValueFactory(new PropertyValueFactory<>("nominal"));

        TableColumn<Position, Double> colOpenPositionOpenPrice = new TableColumn<>("Open price");
        colOpenPositionOpenPrice.setCellValueFactory(new PropertyValueFactory<>("openPrice"));

        TableColumn<Position, Double> colOpenPositionMargin = new TableColumn<>("Margin");
        colOpenPositionMargin.setCellValueFactory(new PropertyValueFactory<>("margin"));

        TableColumn<Position, Double> colOpenPositionProfit = new TableColumn<>("Profit");
        colOpenPositionProfit.setCellValueFactory(new PropertyValueFactory<>("profit"));

        TableColumn<Position, Double> colOpenPositionStopLoss = new TableColumn<>("Stop loss");
        colOpenPositionStopLoss.setCellValueFactory(new PropertyValueFactory<>("stopLoss"));

        TableColumn<Position, Double> colOpenPositionTakeProfit= new TableColumn<>("Take profit");
        colOpenPositionTakeProfit.setCellValueFactory(new PropertyValueFactory<>("takeProfit"));

        TableColumn<Position, String> colOpenPositionClose = new TableColumn<>("");
        colOpenPositionClose.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        colOpenPositionClose.setCellFactory(closePositionCellFactory());

        TableColumn<Position, String> colOpenPositionModify = new TableColumn<>("");
        colOpenPositionModify.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        colOpenPositionModify.setCellFactory(modifyPositionCellFactory());

        tableViewOpenPositions.setItems(account.openPositions());

        //noinspection unchecked
        tableViewOpenPositions.getColumns().addAll(colOpenPositionId, colOpenPositionSide, colOpenPositionNominal,
                colOpenPositionOpenPrice, colOpenPositionMargin, colOpenPositionProfit, colOpenPositionStopLoss,
                colOpenPositionTakeProfit, colOpenPositionClose, colOpenPositionModify);

        tableViewOpenPositions.getColumns().forEach(column -> column.setMinWidth(80));

        colOpenPositionClose.setMinWidth(45);
        colOpenPositionClose.setPrefWidth(45);

        colOpenPositionModify.setMinWidth(60);
        colOpenPositionModify.setPrefWidth(60);

        tabPositions.setContent(tableViewOpenPositions);

        Tab tabOrders = new Tab("Orders");

        TableView<Order> tableViewOrders = new TableView<>();
        tableViewOrders.setPlaceholder(new Label(""));

        TableColumn<Order, Integer> colOrderId = new TableColumn<>("ID");
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, Order.Type> colOrderType = new TableColumn<>("Type");
        colOrderType.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Order, Side> colOrderSide = new TableColumn<>("Side");
        colOrderSide.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Order, Double> colOrderNominal = new TableColumn<>("Nominal");
        colOrderNominal.setCellValueFactory(new PropertyValueFactory<>("nominal"));

        TableColumn<Order, Double> colOrderPrice = new TableColumn<>("Price");
        colOrderPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Order, String> colOrderModify = new TableColumn<>("");
        colOrderModify.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        colOrderModify.setCellFactory(cancelOrderCellFactory());

        tableViewOrders.setItems(account.orders());

        //noinspection unchecked
        tableViewOrders.getColumns().addAll(colOrderId, colOrderType, colOrderSide, colOrderNominal, colOrderPrice,
                colOrderModify);

        tableViewOrders.getColumns().forEach(column -> column.setMinWidth(80));

        tabOrders.setContent(tableViewOrders);

        Tab tabHistory = new Tab("History");

        TableView<Position> tableViewClosedPositions = new TableView<>();
        tableViewClosedPositions.setPlaceholder(new Label(""));

        TableColumn<Position, Integer> colClosedPositionId = new TableColumn<>("ID");
        colClosedPositionId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Position, Side> colClosedPositionSide = new TableColumn<>("Side");
        colClosedPositionSide.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Position, Double> colClosedPositionNominal = new TableColumn<>("Nominal");
        colClosedPositionNominal.setCellValueFactory(new PropertyValueFactory<>("nominal"));

        TableColumn<Position, Double> colClosedPositionOpenPrice = new TableColumn<>("Open price");
        colClosedPositionOpenPrice.setCellValueFactory(new PropertyValueFactory<>("openPrice"));

        TableColumn<Position, Double> colClosedPositionClosePrice = new TableColumn<>("Close price");
        colClosedPositionClosePrice.setCellValueFactory(new PropertyValueFactory<>("closePrice"));

        TableColumn<Position, Double> colClosedPositionProfit = new TableColumn<>("Profit");
        colClosedPositionProfit.setCellValueFactory(new PropertyValueFactory<>("profit"));

        tableViewClosedPositions.setItems(account.closedPositions());

        //noinspection unchecked
        tableViewClosedPositions.getColumns().addAll(colClosedPositionId, colClosedPositionSide,
                colClosedPositionNominal, colClosedPositionOpenPrice, colClosedPositionClosePrice,
                colClosedPositionProfit);

        tableViewClosedPositions.getColumns().forEach(column -> column.setMinWidth(80));

        tabHistory.setContent(tableViewClosedPositions);

        tabPane.getTabs().addAll(tabPositions, tabOrders, tabHistory);
        tabPane.getTabs().forEach(tab -> tab.closableProperty().setValue(false));

        GridPane bottomLeftGridPane = new GridPane();
        GridPane bottomRightGridPane = new GridPane();
        GridPane bottomCenterGridPane = new GridPane();

        GridPane[] bottomGridPanes = {bottomLeftGridPane, bottomRightGridPane, bottomCenterGridPane};

        for(GridPane gridPane : bottomGridPanes) {
            gridPane.setPadding(new Insets(15));
            gridPane.setHgap(20);
            gridPane.setVgap(5);
        }

        bottomCenterGridPane.setMinWidth(205);

        bottomRightGridPane.setAlignment(Pos.BOTTOM_RIGHT);

        Text textBalanceLabel = new Text("Balance");
        bottomLeftGridPane.add(textBalanceLabel, 1, 0);

        Text textBalanceAmount = new Text();
        textBalanceAmount.textProperty().bind(account.balanceProperty());
        bottomLeftGridPane.add(textBalanceAmount, 1, 1);

        Text textMarginLabel = new Text("Margin");
        bottomLeftGridPane.add(textMarginLabel, 2, 0);

        Text textMarginAmount = new Text();
        textMarginAmount.textProperty().bind(account.marginProperty());
        bottomLeftGridPane.add(textMarginAmount, 2, 1);

        Text textProfitLabel = new Text("Total profit");
        bottomLeftGridPane.add(textProfitLabel, 3, 0);

        Text textProfitAmount = new Text();
        textProfitAmount.textProperty().bind(account.openProfitProperty());
        bottomLeftGridPane.add(textProfitAmount, 3, 1);

        Text textMarginLevelLabel = new Text("Margin level");
        bottomLeftGridPane.add(textMarginLevelLabel, 4, 0);

        Text textMarginLevelAmount = new Text();
        textMarginLevelAmount.textProperty().bind(account.marginLevelProperty());
        bottomLeftGridPane.add(textMarginLevelAmount, 4, 1);

        Button buttonDepositWithdrawal = new Button("Deposit / Withdrawal");
        buttonDepositWithdrawal.setMinHeight(35);
        buttonDepositWithdrawal.setOnAction(event -> depositWithdrawal());
        bottomLeftGridPane.add(buttonDepositWithdrawal, 0, 0, 1, 2);

        Text textStatus = new Text();
        textStatus.textProperty().bind(priceService.statusProperty());
        bottomRightGridPane.add(textStatus, 1, 0);

        Button buttonNewGame = new Button("New game");
        buttonNewGame.setOnAction(event -> confirmNewGame());
        bottomRightGridPane.add(buttonNewGame, 0, 0);

        GridPane bottomGridPane = new GridPane();

        bottomGridPane.add(bottomLeftGridPane, 0, 0);
        bottomGridPane.add(bottomCenterGridPane, 1, 0);
        bottomGridPane.add(bottomRightGridPane, 2, 0);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(upperVBox, tabPane, bottomGridPane);

        return new Scene(vBox);
    }

    private LineChart<String, Number> generateChart() {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAnimated(false);

        yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        yAxis.setAutoRanging(false);

        final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        chart.getData().add(priceDataSeries);
        chart.setCreateSymbols(false);

        updateChartNumberAxisBounds();

        return chart;
    }

    private void updateChartNumberAxisBounds() {
        Platform.runLater(() -> {
            yAxis.setLowerBound(MathOperations.round(findLowestPriceInSeries() - 1, 0));
            yAxis.setUpperBound(MathOperations.round(findHighestPriceInSeries() + 1, 0));
            yAxis.setTickUnit(1);
        });
    }

    private double findLowestPriceInSeries() {

        double lowest = (double)priceDataSeries.getData().get(0).getYValue();

        for(XYChart.Data<String, Number> data : priceDataSeries.getData()) {
            if((double)data.getYValue() < lowest) {
                lowest = (double)data.getYValue();
            }
        }

        return lowest;
    }

    private double findHighestPriceInSeries() {

        double highest = (double)priceDataSeries.getData().get(0).getYValue();

        for(XYChart.Data<String, Number> data : priceDataSeries.getData()) {
            if((double)data.getYValue() > highest) {
                highest = (double)data.getYValue();
            }
        }

        return highest;
    }

    private void loadInitialPriceSet() throws IOException {

        List<DatePricePair> prices = priceService.getInitialPriceSet();

        for(int i = 19; i >= 0; i--) {
            Date date = prices.get(i).getDate();
            double price = prices.get(i).getPrice();
            priceDataSeries.getData().add(new XYChart.Data<>(TimeFormatter.format(date), price));
            System.out.println(date.toString() + ", " + price);
        }
    }

    private void appendPriceDataSeries(double value) {

        double lastValue = 0;

        if(priceDataSeries.getData().size() > 0) {
            lastValue = (double)priceDataSeries.getData().get(priceDataSeries.getData().size() - 1).getYValue();
        }

        if(value == lastValue) {
            return;
        }

        if(priceDataSeries.getData().size() >= 20) {
            priceDataSeries.getData().remove(0);
        }

        priceDataSeries.getData().add(new XYChart.Data<>(
                TimeFormatter.format(Calendar.getInstance().getTime()), value));
    }

    private void confirmNewAccount() {
        Alert alert = new Alert(Alert.AlertType.NONE,
                "Couldn't find a valid save file. Create a new account?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            generateNewAccount();
        }
    }

    private void newOrderCreator() {

        Stage stage = new Stage();
        stage.setWidth(140);
        stage.setHeight(250);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setPadding(new Insets(15));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        String[] orderSides = {"BUY", "SELL"};
        ComboBox<String> cmbOrderSides = new ComboBox<>(FXCollections.observableArrayList(orderSides));
        cmbOrderSides.getSelectionModel().selectFirst();
        cmbOrderSides.setMinWidth(80);
        gridPane.add(cmbOrderSides, 0, 0);

        String[] orderTypes = {"LIMIT", "STOP"};
        ComboBox<String> cmbOrderTypes = new ComboBox<>(FXCollections.observableArrayList(orderTypes));
        cmbOrderTypes.getSelectionModel().selectFirst();
        cmbOrderTypes.setMinWidth(80);
        gridPane.add(cmbOrderTypes, 0, 1);

        TextField textFieldNominal = new TextField();
        textFieldNominal.setPromptText("Nominal");
        textFieldNominal.setMaxWidth(80);
        gridPane.add(textFieldNominal, 0, 2);

        TextField textFieldPrice = new TextField();
        textFieldPrice.setPromptText("Price");
        textFieldPrice.setMaxWidth(80);
        gridPane.add(textFieldPrice, 0, 3);

        Button buttonCreate = new Button("Create");
        buttonCreate.setPrefWidth(80);
        buttonCreate.setOnAction(event -> {
            double nominal;
            double price;

            try {
                nominal = Double.parseDouble(textFieldNominal.getText());
                price = Double.parseDouble(textFieldPrice.getText());
            } catch(NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.NONE, "Invalid value", ButtonType.OK);
                alert.show();
                return;
            }

            Side side = Side.valueOf(cmbOrderSides.getValue());
            Order.Type type = Order.Type.valueOf(cmbOrderTypes.getValue());

            Order order = new Order(type, side, nominal, price);
            tryAddOrder(order);
            if(account.orders().contains(order)) {
                stage.close();
            }
        });
        gridPane.add(buttonCreate, 0, 5);

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.show();
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

        TextField textFieldAmount = new TextField(NumberFormatter.currencyFormat(0));
        textFieldAmount.setMinWidth(150);
        textFieldAmount.setOnKeyPressed(event -> Platform.runLater(() -> {
            appendDepositWithdrawalInput(event);
            textFieldAmount.setText(formatDepositWithdrawalInput());
        }));
        gridPane.add(textFieldAmount, 0, 0, 2, 1);

        Button buttonDeposit = new Button("Deposit");
        buttonDeposit.setMinWidth(70);
        buttonDeposit.setOnAction(event -> {
            account.amendBalance(Double.parseDouble(depositWithdrawalInput.toString()) / 100);
            depositWithdrawalInput.setLength(0);
            stage.close();
        });
        gridPane.add(buttonDeposit, 0, 1);

        Button buttonWithdraw = new Button("Withdraw");
        buttonWithdraw.setMinWidth(70);
        buttonWithdraw.setOnAction(event -> {
            if(depositWithdrawalInput.length() > 0 &&
                    Double.parseDouble(depositWithdrawalInput.toString()) / 100 > account.getBalance()) {
                depositWithdrawalInput = new StringBuilder(String.valueOf(account.getBalance() * 100));
                textFieldAmount.setText(formatDepositWithdrawalInput());
            } else {
                account.amendBalance( - Double.parseDouble(depositWithdrawalInput.toString()) / 100);
                depositWithdrawalInput.setLength(0);
                stage.close();
            }
        });
        gridPane.add(buttonWithdraw, 1, 1);

        Scene scene = new Scene(gridPane);

        stage.setScene(scene);
        stage.setOnCloseRequest(event -> depositWithdrawalInput.setLength(0));
        stage.show();
    }

    private void appendDepositWithdrawalInput(KeyEvent event) {
        if(event.getCode().equals(KeyCode.BACK_SPACE) || event.getCode().equals(KeyCode.DELETE)) {
            depositWithdrawalInput.setLength(0);
        } else if (
                event.getCode().toString().length() == 6 &&
                event.getCode().toString().substring(0, 5).equals("DIGIT")
                ||
                event.getCode().toString().length() == 7 &&
                event.getCode().toString().substring(0, 6).equals("NUMPAD")) {

            depositWithdrawalInput.append(event.getText().charAt(0));
        }
    }

    private void appendNominalInput(KeyEvent event) {
        if(event.getCode().equals(KeyCode.BACK_SPACE) || event.getCode().equals(KeyCode.DELETE)) {
            nominalInput.setLength(0);
        } else if (
                (nominalInput.toString().length() < 3 ||
                nominalInput.toString().charAt(nominalInput.length() - 3) != '.') &&
                ((event.getCode().toString().length() == 6 &&
                event.getCode().toString().substring(0, 5).equals("DIGIT"))
                ||
                (event.getCode().toString().length() == 7 &&
                event.getCode().toString().substring(0, 6).equals("NUMPAD"))
                ||
                event.getCode().equals(KeyCode.PERIOD))) {

            nominalInput.append(event.getText().charAt(0));
        }
    }

    private String formatDepositWithdrawalInput() {
        if(depositWithdrawalInput.length() == 0) {
            return NumberFormatter.currencyFormat(0);
        } else {
            return NumberFormatter.currencyFormat(Double.parseDouble(depositWithdrawalInput.toString()) / 100);
        }
    }

    private String formatNominalInput() {
        if(nominalInput.length() == 0) {
            return NumberFormatter.priceFormat(0);
        } else {
            return NumberFormatter.priceFormat(Double.parseDouble(nominalInput.toString()));
        }
    }

    private Callback<TableColumn<Position, String>, TableCell<Position, String>> closePositionCellFactory() {
        return new Callback<TableColumn<Position, String>, TableCell<Position, String>>() {
            @Override
            public TableCell<Position, String> call(final TableColumn<Position, String> param) {

                Button closeButton = new Button("close");
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
    }

    private Callback<TableColumn<Position, String>, TableCell<Position, String>> modifyPositionCellFactory() {
        return new Callback<TableColumn<Position, String>, TableCell<Position, String>>() {
            @Override
            public TableCell<Position, String> call(final TableColumn<Position, String> param) {

                Button modifyButton = new Button("modify");
                modifyButton.setPadding(new Insets(0, 4, 0, 4));

                return new TableCell<Position, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {

                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            modifyButton.setOnAction(event -> {
                                Position position = getTableView().getItems().get(getIndex());
                                modifyPosition(position);
                            });
                            setGraphic(modifyButton);
                        }

                        setText(null);
                        setPadding(new Insets(3));
                    }
                };
            }
        };
    }

    private Callback<TableColumn<Order, String>, TableCell<Order, String>> cancelOrderCellFactory() {
        return new Callback<TableColumn<Order, String>, TableCell<Order, String>>() {
            @Override
            public TableCell<Order, String> call(final TableColumn<Order, String> param) {

                Button modifyButton = new Button("cancel");
                modifyButton.setPadding(new Insets(0, 4, 0, 4));

                return new TableCell<Order, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {

                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            modifyButton.setOnAction(event -> {
                                Order order = getTableView().getItems().get(getIndex());
                                account.cancelOrder(order);
                            });
                            setGraphic(modifyButton);
                        }

                        setText(null);
                        setPadding(new Insets(3));
                    }
                };
            }
        };
    }

    private void tryAddPosition(Side side, double nominal) {
        try {
            account.addPosition(side, nominal);
        } catch(InvalidNominalException ex) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Nominal has to be greater than 0", ButtonType.OK);
            alert.show();
        } catch (BalanceExceededException ex) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Insufficient funds", ButtonType.OK);
            alert.show();
        }
    }

    private void tryAddOrder(Order order) {
        try {
            account.addOrder(order);
        } catch(BalanceExceededException ex) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Insufficient funds", ButtonType.OK);
            alert.show();
        } catch(WrongSideException ex) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Wrong side of the market", ButtonType.OK);
            alert.show();
        }
    }

    private void updateMarginProperty() {

        double margin = 0;

        if(nominalInput.length() > 0) {
            margin = Double.parseDouble(askPriceProperty.getValue()) *
                    Double.parseDouble(nominalInput.toString()) * ApplicationRunner.marginRequirement;
        }

        requiredMarginProperty.setValue("Margin:\n" + NumberFormatter.priceFormat(MathOperations.round(margin, 2)));
    }

    private void modifyPosition(Position position) {

        Stage stage = new Stage();
        stage.setWidth(205);
        stage.setHeight(160);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setPadding(new Insets(15));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        Text txtStopLoss = new Text("Stop loss");
        gridPane.add(txtStopLoss, 0, 0);

        String stopLoss = position.getStopLoss() == null ? "" : position.getStopLoss().toString();
        TextField txtFieldStopLoss = new TextField(stopLoss);
        gridPane.add(txtFieldStopLoss, 1, 0);

        Text txtTakeProfit = new Text("Take profit");
        gridPane.add(txtTakeProfit, 0, 1);

        String takeProfit = position.getTakeProfit() == null ? "" : position.getTakeProfit().toString();
        TextField txtFieldTakeProfit = new TextField(takeProfit);
        gridPane.add(txtFieldTakeProfit, 1, 1);

        Button btnApply = new Button("Apply");
        btnApply.setMinWidth(70);
        GridPane.setHalignment(btnApply, HPos.CENTER);
        btnApply.setOnAction(event -> {

            try {
                tryModifyPosition(position, txtFieldStopLoss.getText(), txtFieldTakeProfit.getText());
                stage.close();
            } catch(NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.NONE, "Invalid value(s)", ButtonType.OK);
                alert.show();
            } catch(WrongSideException ex) {
                Alert alert = new Alert(Alert.AlertType.NONE, "Wrong side of the market", ButtonType.OK);
                alert.show();
            }
        });
        gridPane.add(btnApply, 0, 2, 2, 1);

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.show();
    }

    private void tryModifyPosition(Position position, String stopLoss, String takeProfit) {

        if(stopLoss.equals("")) {
            position.setStopLoss(null);
        } else {
            position.setStopLoss(Double.parseDouble(stopLoss));
        }

        if(takeProfit.equals("")) {
            position.setTakeProfit(null);
        } else {
            position.setTakeProfit(Double.parseDouble(takeProfit));
        }
    }

    private void confirmNewGame() {
        Alert alert = new Alert(Alert.AlertType.NONE, "Start a new game?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            restarted = true;
            primaryStage.close();
            Platform.runLater(() -> start(new Stage()));
        }
    }

    private void retrieveOrCreateAccount() {

        if(restarted) {
            generateNewAccount();
        } else {
            try {
                ApplicationRunner.account = dataStorage.retrieveAccount();
            } catch(IOException | JSONException ex) {
                confirmNewAccount();
            }
        }
    }

    private void generateNewAccount() {
        account = new Account(10000, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}