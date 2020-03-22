package com.trader;

import com.trader.account.*;
import com.trader.exceptions.*;
import com.trader.price.*;
import com.trader.utils.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONException;

public class ApplicationRunner extends Application implements PriceObserver {

    private SimpleStringProperty bidPriceProperty = new SimpleStringProperty();
    private SimpleStringProperty askPriceProperty = new SimpleStringProperty();
    private SimpleStringProperty marginProperty = new SimpleStringProperty();
    private StringBuilder depositWithdrawalInput = new StringBuilder();
    private StringBuilder nominalInput = new StringBuilder();
    private TableView<Position> tableViewOpenPositions;
    private TextField textFieldNominal;

    private static double marginRequirement;
    private static double stopOutLevel;
    private static PriceService priceService;
    private static DataStorage dataStorage;
    private static Account account;
    private static NumberFormat currencyFormatter;
    private static NumberFormat priceFormatter;

    public void run(PriceService priceService, DataStorage dataStorage, double marginRequirement, double stopOutLevel) {

        ApplicationRunner.priceService = priceService;
        ApplicationRunner.dataStorage = dataStorage;
        ApplicationRunner.marginRequirement = marginRequirement;
        ApplicationRunner.stopOutLevel = stopOutLevel;

        launch();
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            ApplicationRunner.account = dataStorage.retrieveAccount();
        } catch(IOException | JSONException ex) {
            confirmCreateNewAccount();
        }

        if(account == null) {
            return;
        }

        currencyFormatter = NumberFormat.getCurrencyInstance();
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setGroupingUsed(true);

        priceFormatter = NumberFormat.getNumberInstance();
        priceFormatter.setMinimumFractionDigits(2);
        priceFormatter.setGroupingUsed(true);

        priceService.addObserver(this);
        priceService.addObserver(account);
        priceService.start();

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
    public void update(double bidPrice, double askPrice) {
        bidPriceProperty.setValue(String.valueOf(bidPrice));
        askPriceProperty.setValue(String.valueOf(askPrice));
        updateMarginProperty();
        Platform.runLater(() -> tableViewOpenPositions.refresh());
    }

    public static double getStopOutLevel() {
        return stopOutLevel;
    }

    private void confirmCreateNewAccount() {

        Alert alert = new Alert(Alert.AlertType.NONE, "Create a new account?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            account = new Account(10000, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
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

        textFieldNominal = new TextField(priceFormatter.format(0));
        textFieldNominal.setMaxWidth(70);
        textFieldNominal.setOnKeyPressed(event -> Platform.runLater(() -> {
            appendNominalInput(event);
            textFieldNominal.setText(formatNominalInput(priceFormatter));
            updateMarginProperty();
        }));
        upperGridPane.add(textFieldNominal, 3, 2);

        Button buttonSell = new Button("SELL");
        buttonSell.setMinWidth(60);
        buttonSell.setOnAction(event -> {
            double openNominal = nominalInput.toString().equals("") ? 0 : Double.parseDouble(nominalInput.toString());
            tryAddPosition(Side.SELL, openNominal, priceService.getBidPrice(), marginRequirement);
        });
        upperGridPane.add(buttonSell, 2, 2);

        Button buttonBuy = new Button("BUY");
        buttonBuy.setMinWidth(60);
        buttonBuy.setOnAction(event -> {
            double openNominal = nominalInput.toString().equals("") ? 0 : Double.parseDouble(nominalInput.toString());
            tryAddPosition(Side.BUY, openNominal, priceService.getAskPrice(), marginRequirement);
        });
        upperGridPane.add(buttonBuy, 4, 2);

        Text textMarginRequired = new Text();
        textMarginRequired.textProperty().bind(marginProperty);
        upperGridPane.add(textMarginRequired, 3, 3);

        TabPane tabPane = new TabPane();
        tabPane.setMinHeight(300);

        Tab tabPositions = new Tab("Positions");

        tableViewOpenPositions = new TableView<>();

        TableColumn<Position, Integer> colOpenPositionId = new TableColumn<>("ID");
        colOpenPositionId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Position, Side> colOpenPositionSide = new TableColumn<>("Side");
        colOpenPositionSide.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Position, Integer> colOpenPositionNominal = new TableColumn<>("Nominal");
        colOpenPositionNominal.setCellValueFactory(new PropertyValueFactory<>("nominal"));

        TableColumn<Position, Double> colOpenPositionOpenPrice = new TableColumn<>("Open price");
        colOpenPositionOpenPrice.setCellValueFactory(new PropertyValueFactory<>("openPrice"));

        TableColumn<Position, Double> colOpenPositionMargin = new TableColumn<>("Margin");
        colOpenPositionMargin.setCellValueFactory(new PropertyValueFactory<>("margin"));

        TableColumn<Position, Double> colOpenPositionProfit = new TableColumn<>("Profit");
        colOpenPositionProfit.setCellValueFactory(new PropertyValueFactory<>("profit"));

        TableColumn<Position, String> colOpenPositionAction = new TableColumn<>("");
        colOpenPositionAction.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        colOpenPositionAction.setCellFactory(generateCellFactory());

        tableViewOpenPositions.setItems(account.openPositions());

        //noinspection unchecked
        tableViewOpenPositions.getColumns().addAll(colOpenPositionId, colOpenPositionSide, colOpenPositionNominal,
                colOpenPositionOpenPrice, colOpenPositionMargin, colOpenPositionProfit, colOpenPositionAction);

        tableViewOpenPositions.getColumns().forEach(column -> column.setMinWidth(80));

        tabPositions.setContent(tableViewOpenPositions);

        Tab tabOrders = new Tab("Orders");

        Tab tabHistory = new Tab("History");

        TableView<Position> tableViewClosedPositions = new TableView<>();

        TableColumn<Position, Integer> colClosedPositionId = new TableColumn<>("ID");
        colClosedPositionId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Position, Side> colClosedPositionSide = new TableColumn<>("Side");
        colClosedPositionSide.setCellValueFactory(new PropertyValueFactory<>("side"));

        TableColumn<Position, Integer> colClosedPositionNominal = new TableColumn<>("Nominal");
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

        GridPane[] bottomGridPanes = {bottomLeftGridPane, bottomRightGridPane};

        for(GridPane gridPane : bottomGridPanes) {
            gridPane.setPadding(new Insets(15));
            gridPane.setHgap(20);
            gridPane.setVgap(5);
        }

        bottomLeftGridPane.setAlignment(Pos.TOP_LEFT);

        bottomRightGridPane.setAlignment(Pos.TOP_RIGHT);
        bottomRightGridPane.setMaxWidth(100);

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
        bottomRightGridPane.add(textStatus, 0, 0);

        GridPane bottomGridPane = new GridPane();

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(90);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(10);

        bottomGridPane.getColumnConstraints().addAll(column1, column2);

        bottomGridPane.add(bottomLeftGridPane, 0, 0);
        bottomGridPane.add(bottomRightGridPane, 1, 0);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(upperGridPane, tabPane, bottomGridPane);

        return new Scene(vBox);
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

        TextField textFieldAmount = new TextField(currencyFormatter.format(0));
        textFieldAmount.setMinWidth(150);
        textFieldAmount.setOnKeyPressed(event -> Platform.runLater(() -> {
            appendDepositWithdrawalInput(event);
            textFieldAmount.setText(formatDepositWithdrawalInput(currencyFormatter));
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
                textFieldAmount.setText(formatDepositWithdrawalInput(currencyFormatter));
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

    private String formatDepositWithdrawalInput(NumberFormat formatter) {
        if(depositWithdrawalInput.length() == 0) {
            return formatter.format(0);
        } else {
            return formatter.format(Double.parseDouble(depositWithdrawalInput.toString()) / 100);
        }
    }

    private String formatNominalInput(NumberFormat formatter) {
        if(nominalInput.length() == 0) {
            return formatter.format(0);
        } else {
            return formatter.format(Double.parseDouble(nominalInput.toString()));
        }
    }

    private Callback<TableColumn<Position, String>, TableCell<Position, String>> generateCellFactory() {
        return new Callback<TableColumn<Position, String>, TableCell<Position, String>>() {
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
    }

    private void tryAddPosition(Side side, double nominal, double openPrice, double marginRequirement) {
        try {
            Position position = new Position(side, nominal, openPrice, marginRequirement);
            account.addPosition(position);
        } catch(InvalidNominalException ex) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Nominal has to be greater than 0", ButtonType.OK);
            alert.show();
        } catch (BalanceExceededException ex) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Insufficient funds", ButtonType.OK);
            alert.show();
        }
    }

    private void updateMarginProperty() {

        double margin = 0;

        if(nominalInput.length() > 0) {
            margin = Double.parseDouble(askPriceProperty.getValue()) *
                    Double.parseDouble(nominalInput.toString()) * ApplicationRunner.marginRequirement;
        }

        marginProperty.setValue("Margin:\n" + priceFormatter.format(MathOperations.round(margin, 2)));
    }
}