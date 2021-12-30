package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.StringValues;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.HtmlStyles;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-10-3
 * @License Apache License Version 2.0
 */
public class GameMineController extends BaseWebViewController {

    protected int chessSize, vNumber, hNumber, spacing, minesNumber,
            total, disclosed, historiesNumber;
    protected AnchorPane[][] chessBoard;
    protected int[][] chessValue;
    protected ChessStatus[][] chessStatus;
    protected Random random;
    protected DropShadow dropShadow;
    protected String mineImage;
    protected long startTime, cost;

    protected enum ChessStatus {
        Disclosed, Closed, Marked, Suspected
    }

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab playTab, optionsTab;
    @FXML
    protected VBox chessboardPane;
    @FXML
    protected Label chessesLabel, scoreLabel, imageLabel;
    @FXML
    protected ComboBox<String> chessSizeSelector, historiesNumberSelector;
    @FXML
    protected RadioButton guaiRadio, benRadio, guaiBenRadio, muteRadio, customizedSoundRadio,
            deadRenewRadio, deadChanceRadio, deadPromptRadio,
            speed1Radio, speed2Radio, speed3Radio, speed5Radio,
            flush0Radio, flush1Radio, flush2Radio, flush3Radio;
    @FXML
    protected Button helpMeButton, okHistoriesNumberButton;
    @FXML
    protected TextField boardWidthInput, boardHeightInput, boardMinesInput;
    @FXML
    protected Label timeLabel, minesLabel;
    @FXML
    protected CheckBox miaowCheck;

    public GameMineController() {
        baseTitle = message("GameMine");
        TipsLabelKey = "GameMineComments";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            random = new Random();
            dropShadow = new DropShadow();
            mineImage = StyleTools.getIconPath(AppVariables.ControlColor) + "iconClear.png";

            spacing = 0;
            chessSize = UserConfig.getInt(baseName + "ChessSize", 20);
            vNumber = UserConfig.getInt(baseName + "BoardHeight", 16);
            hNumber = UserConfig.getInt(baseName + "BoardWidth", 30);
            minesNumber = UserConfig.getInt(baseName + "MinesNumber", 99);
            historiesNumber = UserConfig.getInt(baseName + "HistoriesNumber", 50);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            minesLabel.setStyle("-fx-background-color: black ; -fx-text-fill: lightgreen;");
            timeLabel.setStyle("-fx-background-color: black ; -fx-text-fill: lightgreen;");

            chessSizeSelector.getItems().addAll(Arrays.asList("20", "15", "10", "18", "25", "30"));

            boardWidthInput.setText(hNumber + "");
            boardHeightInput.setText(vNumber + "");
            boardMinesInput.setText(minesNumber + "");
            chessSizeSelector.setValue(chessSize + "");

            historiesNumberSelector.getItems().addAll(Arrays.asList("50", "100", "200", "20", "10", "300", "500"));
            historiesNumberSelector.getSelectionModel().select(UserConfig.getString(baseName + "HistoriesNumber", "50"));

            miaowCheck.setSelected(UserConfig.getBoolean(baseName + "Miaow", true));
            miaowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "Miaow", miaowCheck.isSelected());
                }
            });

            loadRecords();
            createAction();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(undoButton, message("Undo") + "\nz / Ctrl+z");
            NodeStyleTools.setTooltip(recoverButton, message("Replay") + "\nr / Ctrl+r");
            NodeStyleTools.setTooltip(createButton, message("NewGame") + "\nn / Ctrl+n");
            NodeStyleTools.setTooltip(helpMeButton, message("HelpMe") + "\nh / Ctrl+h");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        if (isSettingValues) {
            return;
        }
        try {
            chessValue = new int[vNumber][hNumber];
            chessBoard = new AnchorPane[vNumber][hNumber];
            chessStatus = new ChessStatus[vNumber][hNumber];
            total = vNumber * hNumber - minesNumber;
            resetPanes();

            chessboardPane.getChildren().clear();
            chessboardPane.setPrefWidth((chessSize + spacing * 2) * hNumber);
            chessboardPane.setPrefHeight((chessSize + spacing * 2) * vNumber);
            chessboardPane.setSpacing(spacing);

            for (int v = 0; v < vNumber; ++v) {
                HBox line = new HBox();
                line.setAlignment(Pos.CENTER);
                line.setSpacing(spacing);
                chessboardPane.getChildren().add(line);
                VBox.setVgrow(line, Priority.NEVER);
                HBox.setHgrow(line, Priority.NEVER);
                for (int h = 0; h < hNumber; ++h) {
                    chessValue[v][h] = 0;
                    chessStatus[v][h] = ChessStatus.Closed;
                    VBox vbox = new VBox();
                    vbox.setAlignment(Pos.CENTER);
                    VBox.setVgrow(vbox, Priority.NEVER);
                    HBox.setHgrow(vbox, Priority.NEVER);
                    vbox.setSpacing(spacing);
                    vbox.setPrefWidth(chessSize);
                    vbox.setPrefHeight(chessSize);
                    line.getChildren().add(vbox);
                    AnchorPane apane = new AnchorPane();
                    apane.setPrefWidth(chessSize);
                    apane.setPrefHeight(chessSize);
                    vbox.getChildren().add(apane);
                    chessBoard[v][h] = apane;
                    displayChess(v, h);
                    final int i = v, j = h;
                    vbox.setPickOnBounds(false);
                    vbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (disclosed <= 0) {
                                timing();
                            }
                            if (event.getClickCount() > 1) {
                                discloseAll(i, j);
                            } else {
                                if (chessStatus[i][j] == ChessStatus.Disclosed) {
                                    return;
                                }
                                if (event.getButton() == MouseButton.PRIMARY) {
                                    if (chessValue[i][j] < 0) {
                                        failed();
                                    } else {
                                        disclose(i, j);
                                    }
                                } else if (event.getButton() == MouseButton.SECONDARY) {
                                    switch (chessStatus[i][j]) {
                                        case Closed:
                                            chessStatus[i][j] = ChessStatus.Marked;
                                            break;
                                        case Marked:
                                            chessStatus[i][j] = ChessStatus.Suspected;
                                            break;
                                        case Suspected:
                                            chessStatus[i][j] = ChessStatus.Closed;
                                            break;
                                    }
                                    displayChess(i, j);
                                }
                            }
                        }
                    });

                }
            }

            int mines = 0;
            while (mines < minesNumber) {
                int h = random.nextInt(hNumber);
                int v = random.nextInt(vNumber);
                if (chessValue[v][h] >= 0) {
                    chessValue[v][h] = -1;
                    mines++;
                }
            }

            for (int v = 0; v < vNumber; ++v) {
                for (int h = 0; h < hNumber; ++h) {
                    int n = 0;
                    if (chessValue[v][h] < 0) {
                        continue;
                    }
                    if (h - 1 >= 0 && chessValue[v][h - 1] < 0) {
                        n++;
                    }
                    if (h + 1 < hNumber && chessValue[v][h + 1] < 0) {
                        n++;
                    }
                    if (v - 1 >= 0) {
                        if (h - 1 >= 0 && chessValue[v - 1][h - 1] < 0) {
                            n++;
                        }
                        if (chessValue[v - 1][h] < 0) {
                            n++;
                        }
                        if (h + 1 < hNumber && chessValue[v - 1][h + 1] < 0) {
                            n++;
                        }
                    }
                    if (v + 1 < vNumber) {
                        if (h - 1 >= 0 && chessValue[v + 1][h - 1] < 0) {
                            n++;
                        }
                        if (chessValue[v + 1][h] < 0) {
                            n++;
                        }
                        if (h + 1 < hNumber && chessValue[v + 1][h + 1] < 0) {
                            n++;
                        }
                    }
                    chessValue[v][h] = n;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void timing() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (startTime <= 0) {
            startTime = new Date().getTime();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (timer == null) {
                            return;
                        }
                        cost = new Date().getTime() - startTime;
                        timeLabel.setText(DateTools.timeMsDuration(cost) + "");
                    }
                });
            }
        }, 100, 100);
    }

    // This is called after already determined this grid is not a mine.
    protected void disclose(int v, int h) {
        if (!valid(v, h)) {
            return;
        }
        int value = chessValue[v][h];
        ChessStatus status = chessStatus[v][h];
        if (status == ChessStatus.Disclosed || value < 0) {
            return;
        }
        chessStatus[v][h] = ChessStatus.Disclosed;
        displayChess(v, h);
        ++disclosed;
        minesLabel.setText(disclosed + "/" + total);
        if (startTime > 0) {
            cost = new Date().getTime() - startTime;
            timeLabel.setText(DateTools.timeMsDuration(cost));
        }
        if (disclosed == total) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            popInformation(message("Congratulations"));
            if (miaowCheck.isSelected()) {
                SoundTools.miao3();
            }
            TableStringValues.add("GameMineHistory", vNumber + "-" + hNumber + "-" + minesNumber + "-" + cost);
            loadRecords();
            return;
        }
        if (value > 0) {
            return;
        }
        disclose(v, h - 1);
        disclose(v, h + 1);
        disclose(v - 1, h - 1);
        disclose(v - 1, h);
        disclose(v - 1, h + 1);
        disclose(v + 1, h - 1);
        disclose(v + 1, h);
        disclose(v + 1, h + 1);
    }

    protected boolean valid(int v, int h) {
        return v >= 0 && v < vNumber && h >= 0 && h < hNumber;
    }

    protected void discloseAll(int v, int h) {
        if (!valid(v, h)) {
            return;
        }
        if (chessValue[v][h] < 0) {
            failed();
            return;
        }
        if (chessStatus[v][h] != ChessStatus.Disclosed) {
            disclose(v, h);
        }
        discloseUnmarked(v, h - 1);
        discloseUnmarked(v, h + 1);
        discloseUnmarked(v - 1, h - 1);
        discloseUnmarked(v - 1, h);
        discloseUnmarked(v - 1, h + 1);
        discloseUnmarked(v + 1, h - 1);
        discloseUnmarked(v + 1, h);
        discloseUnmarked(v + 1, h + 1);
    }

    protected void discloseUnmarked(int v, int h) {
        if (!valid(v, h)) {
            return;
        }
        if (chessStatus[v][h] != ChessStatus.Disclosed && chessStatus[v][h] != ChessStatus.Marked) {
            if (chessValue[v][h] < 0) {
                failed();
                return;
            }
            disclose(v, h);
        }
    }

    protected void loadRecords() {
        webViewController.loadContents(null);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private String html;

                @Override
                protected boolean handle() {
                    List<String> names = new ArrayList<>();
                    names.addAll(Arrays.asList(message("Height"), message("Width"), message("MinesNumber"),
                            message("Cost"), message("Time")
                    ));
                    StringTable table = new StringTable(names);
                    List<StringValues> records = TableStringValues.values("GameMineHistory");
                    for (StringValues record : records) {
                        String[] values = record.getValue().split("-");
                        if (values.length != 4) {
                            continue;
                        }
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(
                                values[0], values[1], values[2],
                                DateTools.timeMsDuration(Long.parseLong(values[3])),
                                DateTools.datetimeToString(record.getTime())
                        ));
                        table.add(row);
                    }
                    html = HtmlWriteTools.html(null, HtmlStyles.styleValue("Default"), StringTable.tableDiv(table));
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    webViewController.loadContents(html);
                }
            };
            start(task);
        }
    }

    @FXML
    public void clearHistories() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    TableStringValues.clear("GameMineHistory");
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    webViewController.loadContents(null);
                }
            };
            start(task);
        }
    }

    public void failed() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (miaowCheck.isSelected()) {
            SoundTools.miao6();
        }
        for (int v = 0; v < vNumber; ++v) {
            for (int h = 0; h < hNumber; ++h) {
                displayChess(v, h, ChessStatus.Disclosed, chessValue[v][h]);
            }
        }
        undoButton.setDisable(false);
    }

    @FXML
    public void helpMe() {
        for (int v = 0; v < vNumber; ++v) {
            for (int h = 0; h < hNumber; ++h) {
                displayChess(v, h, ChessStatus.Disclosed, chessValue[v][h]);
            }
        }
        undoButton.setDisable(false);
    }

    @FXML
    @Override
    public void undoAction() {
        for (int v = 0; v < vNumber; ++v) {
            for (int h = 0; h < hNumber; ++h) {
                displayChess(v, h);
            }
        }
        undoButton.setDisable(true);
        if (disclosed > 0) {
            timing();
        }
    }

    protected void displayChess(int v, int h) {
        displayChess(v, h, chessStatus[v][h], chessValue[v][h]);
    }

    protected void displayChess(int v, int h, ChessStatus status, int value) {
        AnchorPane apane = chessBoard[v][h];
        apane.getChildren().clear();
        Rectangle rect = new Rectangle();
        rect.setStroke(Color.GRAY);
        rect.setStrokeType(StrokeType.CENTERED);
        rect.setStrokeWidth(0.5);
        rect.setWidth(chessSize);
        rect.setHeight(chessSize);
        apane.getChildren().add(rect);
        AnchorPane.setTopAnchor(rect, 0.0);
        AnchorPane.setLeftAnchor(rect, 0.0);
        String labelStyle = " -fx-font-size: " + Math.max(6, Math.min(28, chessSize - 7)) + "px;";
        switch (status) {
            case Disclosed:
                if (value < 0) {
                    ImageView view = new ImageView(mineImage);
                    view.setFitWidth(chessSize - 1);
                    view.setFitHeight(chessSize - 1);
                    apane.getChildren().add(view);
                    AnchorPane.setTopAnchor(view, 0.5);
                    AnchorPane.setLeftAnchor(view, 0.5);
                } else if (value == 0) {
                    rect.setFill(Color.WHITE);
                } else {
                    rect.setFill(Color.WHITE);
                    Label label = new Label(value + "");
                    label.setStyle(labelStyle);
                    label.setPrefWidth(chessSize - 1);
                    label.setPrefHeight(chessSize - 1);
                    label.setAlignment(Pos.CENTER);
                    if (value == 1) {
                        label.setTextFill(Color.BLUE);
                    } else if (value == 2) {
                        label.setTextFill(Color.GREEN);
                    } else if (value == 3) {
                        label.setTextFill(Color.RED);
                    } else if (value == 4) {
                        label.setTextFill(Color.PURPLE);
                    } else if (value > 4) {
                        label.setTextFill(Color.GOLD);
                    }
                    apane.getChildren().addAll(label);
                    AnchorPane.setTopAnchor(label, 0.5);
                    AnchorPane.setLeftAnchor(label, 0.5);
                }
                break;
            case Closed:
                rect.setFill(Color.LIGHTGRAY);
//                rect.setEffect(dropShadow);
                break;
            case Marked:
                rect.setFill(Color.LIGHTGRAY);
//                rect.setEffect(dropShadow);
                ImageView view = new ImageView(mineImage);
                view.setFitWidth(chessSize - 1);
                view.setFitHeight(chessSize - 1);
                apane.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.5);
                AnchorPane.setLeftAnchor(view, 0.5);
                break;
            case Suspected:
                rect.setFill(Color.LIGHTGRAY);
//                rect.setEffect(dropShadow);
                Label label = new Label("?");
                label.setStyle(labelStyle);
                label.setPrefWidth(chessSize - 1);
                label.setPrefHeight(chessSize - 1);
                label.setAlignment(Pos.CENTER);
                apane.getChildren().add(label);
                AnchorPane.setTopAnchor(label, 0.5);
                AnchorPane.setLeftAnchor(label, 0.5);
                break;
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        if (chessValue == null) {
            createAction();
            return;
        }
        resetPanes();
        for (int v = 0; v < vNumber; ++v) {
            for (int h = 0; h < hNumber; ++h) {
                chessStatus[v][h] = ChessStatus.Closed;
                displayChess(v, h);
            }
        }
    }

    public void resetPanes() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        startTime = 0;
        disclosed = 0;
        minesLabel.setText(0 + "/" + total);
        timeLabel.setText("");
        undoButton.setDisable(true);
    }

    @FXML
    public void popBoardMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;
            menu = new MenuItem(message("Easy"));
            menu.setOnAction((ActionEvent event) -> {
                boardWidthInput.setText("9");
                boardHeightInput.setText("9");
                boardMinesInput.setText("10");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Medium"));
            menu.setOnAction((ActionEvent event) -> {
                boardWidthInput.setText("16");
                boardHeightInput.setText("16");
                boardMinesInput.setText("40");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Hard"));
            menu.setOnAction((ActionEvent event) -> {
                boardWidthInput.setText("30");
                boardHeightInput.setText("16");
                boardMinesInput.setText("99");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void okOptionsAction() {
        try {
            int v = Integer.parseInt(chessSizeSelector.getValue());
            if (v < 10) {
                chessSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("TooSmall"));
                return;
            }
            chessSize = v;
            chessSizeSelector.getEditor().setStyle(null);
        } catch (Exception e) {
            chessSizeSelector.getEditor().setStyle(UserConfig.badStyle());
            return;
        }
        try {
            int v = Integer.parseInt(boardWidthInput.getText());
            if (v < 2) {
                popError(message("TooSmall"));
                boardWidthInput.setStyle(UserConfig.badStyle());
                return;
            }
            hNumber = v;
            boardWidthInput.setStyle(null);
        } catch (Exception e) {
            boardWidthInput.setStyle(UserConfig.badStyle());
            return;
        }
        try {
            int v = Integer.parseInt(boardHeightInput.getText());
            if (v < 2) {
                popError(message("TooSmall"));
                boardHeightInput.setStyle(UserConfig.badStyle());
                return;
            }
            vNumber = v;
            boardHeightInput.setStyle(null);
        } catch (Exception e) {
            boardHeightInput.setStyle(UserConfig.badStyle());
            return;
        }
        try {
            int v = Integer.parseInt(boardMinesInput.getText());
            if (v <= 0 || v >= hNumber * vNumber) {
                popError(message("InvalidData"));
                boardMinesInput.setStyle(UserConfig.badStyle());
                return;
            }
            minesNumber = v;
            boardMinesInput.setStyle(null);
        } catch (Exception e) {
            boardMinesInput.setStyle(UserConfig.badStyle());
        }
        UserConfig.setInt(baseName + "ChessSize", chessSize);
        UserConfig.setInt(baseName + "BoardHeight", vNumber);
        UserConfig.setInt(baseName + "BoardWidth", hNumber);
        UserConfig.setInt(baseName + "MinesNumber", minesNumber);
        createAction();
        tabPane.getSelectionModel().select(playTab);
    }

    @FXML
    protected void okHistoriesNumber() {
        try {
            int v = Integer.parseInt(historiesNumberSelector.getValue());
            if (v < 1) {
                historiesNumberSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("TooSmall"));
                return;
            }
            historiesNumber = v;
            historiesNumberSelector.getEditor().setStyle(null);
            UserConfig.setInt(baseName + "HistoriesNumber", historiesNumber);
        } catch (Exception e) {
            historiesNumberSelector.getEditor().setStyle(UserConfig.badStyle());
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    TableStringValues.max("GameMineHistory", historiesNumber);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadRecords();
                }
            };
            start(task);
        }

    }

    @Override
    public boolean keyFilter(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != null) {
            switch (code) {
                case H:
                    helpMe();
                    return true;
                case N:
                    createAction();
                    return true;
                case R:
                    recoverAction();
                    return true;
                case Z:
                    undoAction();
                    return true;
            }
        }
        return super.keyFilter(event);
    }

    @Override
    public boolean controlAltR() {
        if (targetIsTextInput()) {
            return false;
        }
        recoverAction();
        return true;
    }

    @Override
    public boolean controlAltZ() {
        if (targetIsTextInput()) {
            return false;
        }
        undoAction();
        return true;
    }

    @Override
    public boolean controlAltH() {
        if (targetIsTextInput()) {
            return false;
        }
        helpMe();
        return true;
    }

    @Override
    public boolean controlAltN() {
        if (targetIsTextInput()) {
            return false;
        }
        createAction();
        return true;
    }

}
