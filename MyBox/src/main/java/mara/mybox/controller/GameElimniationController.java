package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2019-12-30
 * @License Apache License Version 2.0
 */
public class GameElimniationController extends BaseController {

    protected int boardSize, chessSize, eliminationSize = 3, totalScore;
    protected Map<String, VBox> chessBoard;
    protected List<Integer> chessImageIndice, countedChesses;
    protected List<String> imageNames, imageComments;
    protected IntPoint firstClick;
    protected boolean countScore, isEliminating;
    protected Random random;
    protected ObservableList<ScoreRuler> scoreRulersData;
    protected Map<Integer, Integer> scoreRulers;
    protected Map<Integer, Integer> scoreRecord;
    protected Date startTime;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab playTab, chessesTab, rulersTab, scoresTab;
    @FXML
    protected VBox chessboardPane;
    @FXML
    protected Label chessesLabel, scoreLabel;
    @FXML
    protected ListView<String> chessesListView;
    @FXML
    protected FlowPane chessImagesPane, countedImagesPane;
    @FXML
    protected CheckBox viewImageCheck, shadowCheck, arcCheck;
    @FXML
    protected ComboBox<String> chessSizeSelector;
    @FXML
    protected TableView<ScoreRuler> rulersTable;
    @FXML
    protected TableColumn<ScoreRuler, Integer> linksColumn, scoreColumn;
    @FXML
    protected RadioButton guaiRadio, benRadio, guaiBenRadio, muteRadio;

    public GameElimniationController() {
        baseTitle = AppVariables.message("GameElimniation");

        TipsLabelKey = "GameEliminationComments";
    }

    @Override
    public void initializeNext() {
        try {
            chessBoard = new HashMap();
            scoreRulers = new HashMap();
            scoreRecord = new HashMap();
            scoreRulersData = FXCollections.observableArrayList();
            chessImageIndice = new ArrayList();
            countedChesses = new ArrayList();
            random = new Random();

            initChessesTab();
            initRulersTab();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initChessesTab() {
        try {
            isSettingValues = true;
            imageNames = Arrays.asList(
                    "img/About.png", "img/DataTools.png", "img/Settings.png",
                    "img/RecentAccess.png", "img/FileTools.png", "img/ImageTools.png",
                    "img/PdfTools.png", "img/MediaTools.png", "img/NetworkTools.png",
                    "img/ww1.png", "img/ww2.png", "img/ww5.png",
                    "img/ww3.png", "img/ww4.png", "img/ww6.png",
                    "img/ww7.png", "img/ww8.png", "img/ww9.png",
                    "img/jade.png", "img/zz1.png", "img/MyBox.png"
            );
            imageComments = Arrays.asList(
                    "AboutImageTips", "DataToolsImageTips", "SettingsImageTips",
                    "RecentAccessImageTips", "FileToolsImageTips", "ImageToolsImageTips",
                    "PdfToolsImageTips", "MediaToolsImageTips", "NetworkToolsImageTips",
                    "ww1ImageTips", "ww2ImageTips", "ww5ImageTips",
                    "ww3ImageTips", "ww4ImageTips", "ww6ImageTips",
                    "ww7ImageTips", "ww8ImageTips", "ww9ImageTips",
                    "jadeImageTips", "zz1ImageTips", ""
            );

            for (int i = 0; i < imageNames.size(); i++) {
                String name = imageNames.get(i);
                CheckBox cbox = new CheckBox();
                ImageView view = new ImageView(name);
                view.setPreserveRatio(true);
                view.setFitWidth(40);
                view.setUserData(i);
                cbox.setGraphic(view);
                cbox.setUserData(i);
                cbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                        setChessImagesLabel();
                    }
                });
                chessImagesPane.getChildren().add(cbox);
            }

            viewImageCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    checkChessImagesTips();
                }
            });
            viewImageCheck.setSelected(AppVariables.getUserConfigBoolean("GameEliminationChessImagesShow", true));
            try {
                List<Integer> selectedIndice = new ArrayList();
                String selected = AppVariables.getUserConfigValue("GameEliminationChessImages", "0,1,2,3,4,5,6,7");
                for (String s : selected.split(",")) {
                    selectedIndice.add(Integer.valueOf(s));
                }
                int index = 0;
                for (Node node : chessImagesPane.getChildren()) {
                    CheckBox cbox = (CheckBox) node;
                    cbox.setSelected(selectedIndice.contains(index++));
                }
            } catch (Exception e) {
            }

            shadowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
//                    makeChessBoard();
                }
            });

            arcCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
//                    makeChessBoard();
                }
            });

            chessSize = 50;
            chessSizeSelector.getItems().addAll(Arrays.asList(
                    "50", "40", "60", "30", "80"
            ));
            chessSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v < 20) {
                            v = 20;
                        }
                        chessSize = v;
                    } catch (Exception e) {
                        chessSize = 50;
                    }
                    AppVariables.setUserConfigValue("GameEliminationChessImageSize", chessSize + "");
//                    makeChessBoard();
                }
            });

            shadowCheck.setSelected(AppVariables.getUserConfigBoolean("GameEliminationShadow", false));
            arcCheck.setSelected(AppVariables.getUserConfigBoolean("GameEliminationArc", false));
            chessSizeSelector.getSelectionModel().select(AppVariables.getUserConfigValue("GameEliminationChessImageSize", "50"));
            isSettingValues = false;

            checkChessImagesTips();
            setChessImagesLabel();
            okChessesAction();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initRulersTab() {
        try {
            rulersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            rulersTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {

                    }
                }
            });

            rulersTable.setItems(scoreRulersData);

            linksColumn.setCellValueFactory(new PropertyValueFactory<>("linksNumber"));
            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
            scoreColumn.setCellFactory(new Callback<TableColumn<ScoreRuler, Integer>, TableCell<ScoreRuler, Integer>>() {
                @Override
                public TableCell<ScoreRuler, Integer> call(TableColumn<ScoreRuler, Integer> param) {
                    TableAutoCommitCell<ScoreRuler, Integer> cell
                            = new TableAutoCommitCell<ScoreRuler, Integer>(new IntegerStringConverter()) {
                        @Override
                        public void commitEdit(Integer val) {
                            if (val < 0) {
                                cancelEdit();
                            } else {
                                super.commitEdit(val);
                            }
                        }
                    };
                    return cell;
                }
            });
            scoreColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ScoreRuler, Integer>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<ScoreRuler, Integer> t) {
                    if (t == null) {
                        return;
                    }
                    if (t.getNewValue() >= 0) {
                        ScoreRuler row = t.getRowValue();
                        row.score = t.getNewValue();
                    }
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void setChessImagesLabel() {
        if (isSettingValues) {
            return;
        }
        int count = 0;
        for (Node node : chessImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            if (cbox.isSelected()) {
                count++;
            }
        }
        chessesLabel.setText(MessageFormat.format(message("SelectChesses"), count));
    }

    public void checkChessImagesTips() {
        if (isSettingValues) {
            return;
        }
        boolean show = viewImageCheck.isSelected();
        AppVariables.setUserConfigValue("GameEliminationChessImagesShow", show);
        for (Node node : chessImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            int index = (Integer) cbox.getUserData();
            if (show) {
                VBox vbox = new VBox();
                vbox.setPrefWidth(600);
                vbox.setPrefHeight(600);
                vbox.setAlignment(Pos.CENTER);
                vbox.setStyle("-fx-background-color: white;");
                ImageView tipView = new ImageView(imageNames.get(index));
                tipView.setPreserveRatio(true);
                tipView.setFitWidth(500);
                vbox.getChildren().add(tipView);
                Text text = new Text();
                String com = imageComments.get(index);
                if (!com.isBlank()) {
                    text.setText(message(com));
                } else {
                    text.setText("");
                }
                text.setStyle("-fx-font-size: 1.2em;");
                vbox.getChildren().add(text);
                vbox.setPadding(new Insets(10, 10, 10, 10));
                FxmlControl.setTooltip(cbox, vbox);
            } else {
                FxmlControl.removeTooltip(cbox);
            }
        }
    }

    @FXML
    protected void settingsAction() {
        tabPane.getSelectionModel().select(chessesTab);
    }

    @FXML
    protected void okChessesAction() {
        if (isSettingValues) {
            return;
        }
        chessImageIndice.clear();
        String s = "";
        for (Node node : chessImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            if (cbox.isSelected()) {
                int index = (Integer) cbox.getUserData();
                chessImageIndice.add(index);
                if (s.isBlank()) {
                    s += index + "";
                } else {
                    s += "," + index;
                }
            }
        }
        AppVariables.setUserConfigValue("GameEliminationChessImages", s);
        boardSize = chessImageIndice.size();
        tabPane.getSelectionModel().select(playTab);
        makeChessBoard();
        makeRulers();
        newGame();
    }

    @FXML
    protected void okRulersAction() {
        try {
            countedChesses.clear();
            String s = "";
            for (Node node : countedImagesPane.getChildren()) {
                CheckBox cbox = (CheckBox) node;
                if (cbox.isSelected()) {
                    int index = (Integer) cbox.getUserData();
                    countedChesses.add(index);
                    if (s.isBlank()) {
                        s += index + "";
                    } else {
                        s += "," + index;
                    }
                }
            }
            if (countedChesses.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getBaseTitle());
                alert.setContentText(AppVariables.message("SureNoScore"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() != ButtonType.OK) {
                    return;
                }
            }
            AppVariables.setUserConfigValue("GameEliminationCountedImages", s);

            scoreRulers.clear();
            s = "";
            for (int i = 0; i < scoreRulersData.size(); i++) {
                ScoreRuler r = scoreRulersData.get(i);
                scoreRulers.put(r.linksNumber, r.score);
                if (!s.isEmpty()) {
                    s += ",";
                }
                s += r.linksNumber + "," + r.score;
            }
            AppVariables.setUserConfigValue("GameElimniationScoreRulers", s);

            tabPane.getSelectionModel().select(playTab);
            newGame();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    protected void clearChessSelectionAction() {
        isSettingValues = true;
        for (Node node : chessImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            cbox.setSelected(false);
        }
        isSettingValues = false;
        chessesLabel.setText(MessageFormat.format(message("SelectChesses"), 0));
    }

    @FXML
    protected void clearCountedImagesAction() {
        for (Node node : countedImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            cbox.setSelected(false);
        }
    }

    @FXML
    protected void allCountedImagesAction() {
        for (Node node : countedImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            cbox.setSelected(true);
        }
    }

    protected void makeChessBoard() {
        if (isSettingValues) {
            return;
        }
        try {
            chessBoard.clear();
            chessboardPane.getChildren().clear();
            chessboardPane.setPrefWidth((chessSize + 15) * boardSize);
            chessboardPane.setPrefHeight((chessSize + 20) * boardSize);
            DropShadow effect = new DropShadow();
            boolean shadow = shadowCheck.isSelected();
            boolean arc = arcCheck.isSelected();
            for (int i = 1; i <= boardSize; i++) {
                HBox line = new HBox();
                line.setAlignment(Pos.CENTER);
                line.setSpacing(10);
                chessboardPane.getChildren().add(line);
                VBox.setVgrow(line, Priority.NEVER);
                HBox.setHgrow(line, Priority.NEVER);
                for (int j = 1; j <= boardSize; j++) {
                    VBox vbox = new VBox();
                    vbox.setAlignment(Pos.CENTER);
                    VBox.setVgrow(vbox, Priority.NEVER);
                    HBox.setHgrow(vbox, Priority.NEVER);
                    vbox.setSpacing(10);
                    vbox.setUserData(i + "-" + j);
                    if (shadow) {
                        vbox.setEffect(effect);
                    }
                    if (arc) {
                        vbox.setStyle("-fx-background-color: white; -fx-border-radius: 10;-fx-background-radius: 10");
                    } else {
                        if (shadow) {
                            vbox.setStyle("-fx-background-color: white;");
                        }
                    }
                    final int x = i, y = j;
                    vbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            chessClicked(x, y);
                        }
                    });
                    line.getChildren().add(vbox);
                    chessBoard.put(i + "-" + j, vbox);
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void newGame() {
        if (isSettingValues) {
            return;
        }
        try {
            for (int i = 1; i <= boardSize; i++) {
                for (int j = 1; j <= boardSize; j++) {
                    setRandomImage(i, j);
                }
            }
            totalScore = 0;
            scoreLabel.setText("");
            countScore = false;
            eliminate();
            startTime = new Date();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void makeRulers() {
        if (isSettingValues) {
            return;
        }
        try {
            scoreRulers.clear();
            scoreRulersData.clear();
            try {
                String s = AppVariables.getUserConfigValue("GameElimniationScoreRulers", "");
                if (s != null && !s.isEmpty()) {
                    String[] ss = s.split(",");
                    for (int i = 1; i < ss.length; i = i + 2) {
                        scoreRulers.put(Integer.parseInt(ss[i - 1]), Integer.parseInt(ss[i]));
                    }
                }
            } catch (Exception e) {
            }
            for (int i = 3; i <= boardSize; i++) {
                if (scoreRulers.get(i) == null) {
                    scoreRulers.put(i, (int) Math.pow(10, i - 3));
                }
                ScoreRuler r = new ScoreRuler(i, scoreRulers.get(i));
                scoreRulersData.add(r);
            }
            rulersTable.refresh();

            countedChesses.clear();
            countedImagesPane.getChildren().clear();
            for (int i = 0; i < chessImageIndice.size(); i++) {
                int index = chessImageIndice.get(i);
                CheckBox cbox = new CheckBox();
                ImageView view = new ImageView(imageNames.get(index));
                view.setPreserveRatio(true);
                view.setFitWidth(40);
                view.setUserData(index);
                cbox.setGraphic(view);
                cbox.setUserData(index);
                countedImagesPane.getChildren().add(cbox);
                countedChesses.add(index);
                cbox.setSelected(true);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void chessClicked(int x, int y) {
        try {
//            if (isEliminating) {
//                return;
//            }
            if (firstClick == null) {
                firstClick = new IntPoint(x, y);
                return;
            }
//            isEliminating = true;
            countScore = true;
            chessClicked(x, y, firstClick.getX(), firstClick.getY());
            firstClick = null;
//            isEliminating = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void chessClicked(int i1, int j1, int i2, int j2) {
        try {
            if (i1 == i2 && j1 == j2) {
                return;
            }
            if (Math.abs(i1 - i2) > 1
                    || Math.abs(j1 - j2) > 1) {
                return;
            }

            VBox vbox1 = chessBoard.get(i1 + "-" + j1);
            ImageView view1 = (ImageView) (vbox1.getChildren().get(0));
            VBox vbox2 = chessBoard.get(i2 + "-" + j2);
            ImageView view2 = (ImageView) (vbox2.getChildren().get(0));
            vbox1.getChildren().clear();
            vbox1.getChildren().add(view2);
            vbox2.getChildren().clear();
            vbox2.getChildren().add(view1);

            IntRange range = horizontalSame(i1, j1);
            if (range != null && range.getLength() >= eliminationSize) {
                horizontalEliminate(i1, range.start, range.end);
                return;
            }

            range = verticalSame(i1, j1);
            if (range != null && range.getLength() >= eliminationSize) {
                verticalEliminate(range.start, range.end, j1);
                return;
            }

            range = horizontalSame(i2, j2);
            if (range != null && range.getLength() >= eliminationSize) {
                horizontalEliminate(i2, range.start, range.end);
                return;
            }

            range = verticalSame(i2, j2);
            if (range != null && range.getLength() >= eliminationSize) {
                verticalEliminate(range.start, range.end, j2);
                return;
            }

            vbox1.getChildren().clear();
            vbox1.getChildren().add(view1);
            vbox2.getChildren().clear();
            vbox2.getChildren().add(view2);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected ImageView getImageView(int x, int y) {
        try {
            VBox vbox = chessBoard.get(x + "-" + y);
            return (ImageView) (vbox.getChildren().get(0));
        } catch (Exception e) {
            return null;
        }
    }

    protected int getImageIndex(int x, int y) {
        try {
            ImageView view = getImageView(x, y);
            return (int) (view.getUserData());
        } catch (Exception e) {
            return -1;
        }
    }

    protected void setRandomImage(int i, int j) {
        try {
            int index = chessImageIndice.get(random.nextInt(boardSize));
//            logger.debug(x + "," + y + ":" + index);
            ImageView view = new ImageView(imageNames.get(index));
            view.setPreserveRatio(true);
            view.setFitWidth(chessSize);
            view.setUserData(index);
            VBox vbox = chessBoard.get(i + "-" + j);
            vbox.getChildren().clear();
            vbox.getChildren().add(view);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void lightFlush(int i, int j) {
        flush(i, j, 0.2f, 500, 1);
    }

    protected void darkFlush(int i, int j) {
        flush(i, j, 0f, 200, 3);
    }

    protected void flush(int i, int j, float opacity, int duration, int times) {
        try {
            FadeTransition fade = new FadeTransition(Duration.millis(duration));
            fade.setFromValue(1.0);
            fade.setToValue(opacity);
            fade.setCycleCount(times * 2);
            fade.setAutoReverse(true);
            fade.setNode(chessBoard.get(i + "-" + j));
            fade.play();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void move(int i1, int j1, int i2, int j2) {
        try {
            ImageView view = getImageView(i1, j1);
            logger.debug(view != null);
            VBox vbox = chessBoard.get(i2 + "-" + j2);
            logger.debug(vbox != null);
            Path path = new Path();
            logger.debug(FxmlControl.getX(vbox) + ", " + FxmlControl.getY(vbox));
            path.getElements().add(new MoveTo(FxmlControl.getX(vbox), FxmlControl.getY(vbox)));

            PathTransition pathTransition = new PathTransition(Duration.millis(2000), path, view);
//        pathTransition.setOrientation(OrientationType.ORTHOGONAL_TO_TANGENT);
            pathTransition.play();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected IntRange verticalSame(int i, int j) {
        try {
            int imageIndex = getImageIndex(i, j);
            int i1 = i;
            while (i1 > 1) {
                int moveIndex = getImageIndex(i1 - 1, j);
                if (moveIndex != imageIndex) {
                    break;
                }
                i1--;
            }
            int i2 = i;
            while (i2 < boardSize) {
                int moveIndex = getImageIndex(i2 + 1, j);
                if (moveIndex != imageIndex) {
                    break;
                }
                i2++;
            }
            return new IntRange(i1, i2);
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    protected IntRange horizontalSame(int i, int j) {
        try {
            int imageIndex = getImageIndex(i, j);
            int j1 = j;
            while (j1 > 1) {
                int preIndex = getImageIndex(i, j1 - 1);
                if (preIndex != imageIndex) {
                    break;
                }
                j1--;
            }
            int j2 = j;
            while (j2 < boardSize) {
                int nextIndex = getImageIndex(i, j2 + 1);
                if (nextIndex != imageIndex) {
                    break;
                }
                j2++;
            }
            return new IntRange(j1, j2);
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

    protected void verticalEliminate(int i1, int i2, int j) {
        if (isSettingValues) {
            return;
        }
        if (i1 < 1 || i1 > boardSize
                || i2 < 1 || i2 > boardSize
                || j < 1 || j > boardSize
                || i1 > i2) {
            return;
        }
        if (countScore) {
            for (int row = i1; row <= i2; row++) {
                darkFlush(row, j);
            }
            Timer vtimer = new Timer();
            vtimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            verticalEliminateDo(i1, i2, j);
                        }
                    });
                }
            }, 1500);
        } else {
            verticalEliminateDo(i1, i2, j);
        }
    }

    protected void verticalEliminateDo(int i1, int i2, int j) {
        try {
            ImageView view = getImageView(i1, j);
            int imageIndex = (int) view.getUserData();
            int offset = 1;
            for (int row = i2; row >= i1; row--, offset++) {
                if (i1 - offset < 1) {
                    setRandomImage(row, j);
//                    if (countScore) {
//                        lightFlush(row, j);
//                    }
                } else {
                    view = getImageView(i1 - offset, j);
                    VBox vbox = chessBoard.get(row + "-" + j);
                    vbox.getChildren().clear();
                    vbox.getChildren().add(view);
//                    if (countScore) {
//                        lightFlush(row, j);
//                    }
                    setRandomImage(i1 - offset, j);
//                    if (countScore) {
//                        lightFlush(i1 - offset, j);
//                    }
                }
            }
            recordScore(imageIndex, i2 - i1 + 1);
            eliminate();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void horizontalEliminate(int i, int j1, int j2) {
        if (isSettingValues) {
            return;
        }
        if (i < 1 || i > boardSize
                || j1 < 1 || j1 > boardSize
                || j2 < 1 || j2 > boardSize
                || j1 > j2) {
            return;
        }
        if (countScore) {
            for (int col = j1; col <= j2; col++) {
                darkFlush(i, col);
            }
            Timer htimer = new Timer();
            htimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            horizontalEliminateDo(i, j1, j2);
                        }
                    });
                }
            }, 1500);
        } else {
            horizontalEliminateDo(i, j1, j2);
        }
    }

    protected void horizontalEliminateDo(int i, int j1, int j2) {
        try {
            ImageView view = getImageView(i, j1);
            int imageIndex = (int) view.getUserData();
            for (int row = i; row > 1; row--) {
                for (int col = j1; col <= j2; col++) {
                    view = getImageView(row - 1, col);
                    VBox vbox = chessBoard.get(row + "-" + col);
                    vbox.getChildren().clear();
                    vbox.getChildren().add(view);
//                    if (countScore) {
//                        lightFlush(row, col);
//                    }
                }
            }
            for (int col = j1; col <= j2; col++) {
                setRandomImage(1, col);
//                if (countScore) {
//                    lightFlush(1, col);
//                }
            }
            recordScore(imageIndex, j2 - j1 + 1);
            eliminate();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void recordScore(int imageIndex, int size) {
        try {
            if (!countScore || !countedChesses.contains(imageIndex)) {
                return;
            }

            int add = scoreRulers.get(size);
            totalScore += add;
            long cost = new Date().getTime() - startTime.getTime();
            scoreLabel.setText(message("Score") + ": " + totalScore + "  " + message("Cost") + ": "
                    + DateTools.showSeconds(cost / 1000));

            if (add > 0) {
                Label popupLabel = new Label("+" + add);
                popupLabel.setStyle("-fx-background-color:black;"
                        + " -fx-text-fill: gold;"
                        + " -fx-font-size: 3em;"
                        + " -fx-padding: 10px;"
                        + " -fx-background-radius: 10;");
                final Popup scorePopup = new Popup();
                scorePopup.setAutoFix(true);
                scorePopup.setAutoHide(true);
                scorePopup.getContent().add(popupLabel);
                scorePopup.show(getMyStage());

                Timer stimer = new Timer();
                stimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                scorePopup.hide();
                            }
                        });
                    }
                }, 1500);

                if (guaiRadio.isSelected()) {
                    FxmlControl.GuaiAO();
                } else if (benRadio.isSelected()) {
                    FxmlControl.BenWu();
                } else if (guaiBenRadio.isSelected()) {
                    if (size == 3) {
                        FxmlControl.BenWu();
                    } else {
                        FxmlControl.GuaiAO();
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    protected void eliminate() {
        if (isSettingValues) {
            return;
        }
        try {
            for (int i = 1; i <= boardSize; i++) {
                int j = 1;
                while (j <= boardSize) {
                    IntRange range = horizontalSame(i, j);
                    if (range != null && range.getLength() >= eliminationSize) {
                        horizontalEliminate(i, range.start, range.end);
                        return;
                    }
                    if (range != null && range.getLength() > 0) {
                        j = j + range.getLength();
                    } else {
                        j = j + 1;
                    }
                }
            }
            for (int j = 1; j <= boardSize; j++) {
                int i = 1;
                while (i <= boardSize) {
                    IntRange range = verticalSame(i, j);
                    if (range != null && range.getLength() >= eliminationSize) {
                        verticalEliminate(range.start, range.end, j);
                        return;
                    }
                    if (range != null && range.getLength() > 0) {
                        i = i + range.getLength();
                    } else {
                        i = i + 1;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        newGame();
    }

    public class IntRange {

        protected int start, end; // both include

        public IntRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getLength() {
            return end - start + 1;
        }

    }

    public class ScoreRuler {

        protected int linksNumber, score;

        public ScoreRuler(int linksNumber, int score) {
            this.linksNumber = linksNumber;
            this.score = score;
        }

        public int getLinksNumber() {
            return linksNumber;
        }

        public void setLinksNumber(int linksNumber) {
            this.linksNumber = linksNumber;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

    }

}
