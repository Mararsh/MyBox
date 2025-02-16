package mara.mybox.controller;

import java.io.File;
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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Path;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import mara.mybox.data.ImageItem;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.cell.ListImageCheckBoxCell;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.converter.IntegerStringFromatConverter;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-30
 * @License Apache License Version 2.0
 */
public class GameEliminationController extends BaseController {

    protected int boardSize, chessSize, minimumAdjacent, totalScore, autoSpeed,
            flushDuration, eliminateDelay, flushTimes;
    protected Map<String, VBox> chessBoard;
    protected List<Integer> selectedChesses, countedChesses;
    protected IntPoint firstClick;
    protected boolean countScore, isEliminating, autoPlaying, stopped;
    protected Random random;
    protected ObservableList<ScoreRuler> scoreRulersData;
    protected Map<Integer, Integer> scoreRulers;
    protected Map<Integer, Integer> scoreRecord;
    protected Date startTime;
    protected Adjacent lastElimination, lastRandom;
    protected String currentStyle, focusStyle, defaultStyle, arcStyle, shadowStyle;
    protected File soundFile;

    @FXML
    protected Tab playTab, chessesTab, rulersTab, settingsTab;
    @FXML
    protected VBox chessboardPane;
    @FXML
    protected Label chessesLabel, scoreLabel, autoLabel, soundFileLabel;
    @FXML
    protected ListView<ImageItem> imagesListview;
    @FXML
    protected FlowPane countedImagesPane;
    @FXML
    protected CheckBox shadowCheck, arcCheck, scoreCheck;
    @FXML
    protected ComboBox<String> chessSizeSelector;
    @FXML
    protected TableView<ScoreRuler> rulersTable;
    @FXML
    protected TableColumn<ScoreRuler, Integer> numberColumn, scoreColumn;
    @FXML
    protected RadioButton guaiRadio, benRadio, guaiBenRadio, muteRadio, customizedSoundRadio,
            deadRenewRadio, deadChanceRadio, deadPromptRadio,
            speed1Radio, speed2Radio, speed3Radio, speed5Radio,
            flush0Radio, flush1Radio, flush2Radio, flush3Radio;
    @FXML
    protected HBox selectSoundBox;
    @FXML
    protected Button helpMeButton;
    @FXML
    protected ToggleButton catButton;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected ControlWebView imageInfoController;
    @FXML
    protected ControlColorSet colorSetController;

    public GameEliminationController() {
        baseTitle = message("GameElimniation");
        TipsLabelKey = "GameEliminationComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            chessBoard = new HashMap();
            scoreRulers = new HashMap();
            scoreRecord = new HashMap();
            scoreRulersData = FXCollections.observableArrayList();
            selectedChesses = new ArrayList();
            countedChesses = new ArrayList();
            random = new Random();
            defaultStyle = "-fx-background-color: transparent; -fx-border-style: solid inside;"
                    + "-fx-border-width: 2; -fx-border-radius: 3; -fx-border-color: transparent;";
            arcStyle = "-fx-background-color: white; -fx-border-style: solid inside;"
                    + "-fx-border-width: 2; -fx-border-radius: 10;-fx-background-radius: 10; -fx-border-color: transparent;";
            shadowStyle = "-fx-background-color: white;-fx-border-style: solid inside;"
                    + "-fx-border-width: 2; -fx-border-radius: 3; -fx-border-color: white;";
            focusStyle = "-fx-border-style: solid inside;-fx-border-width: 2;"
                    + "-fx-border-radius: 3;" + "-fx-border-color: blue;";

            flushDuration = 200;
            minimumAdjacent = 3;

            colorSetController.init(this, baseName + "Color", Color.RED);
            colorSetController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    String name = "color:" + colorSetController.name();
                    addImageItem(name);
                }
            });

            catButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    if (firstClick != null) {
                        recoverStyle(firstClick.getX(), firstClick.getY());
                        firstClick = null;
                    }
                    autoPlaying = catButton.isSelected();
                    if (autoPlaying) {
                        autoLabel.setText(message("Autoplaying"));
                        findAdjacentAndEliminate();
                    } else {
                        autoLabel.setText("");
                    }
                }
            });

            imageInfoController.setParent(this);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(createButton, message("NewGame") + "\nn / Ctrl+n");
            NodeStyleTools.setTooltip(helpMeButton, message("HelpMeFindExchange") + "\nh / Ctrl+h");
            NodeStyleTools.setTooltip(catButton, message("AutoPlayGame") + "\np / Ctrl+p");
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private List<ImageItem> items;
            private String defaultSelected;

            @Override
            protected boolean handle() {
                try {
                    items = new ArrayList<>();
                    List<ImageItem> predefinedItems = ImageItem.predefined();
                    List<String> saved = TableStringValues.read("GameEliminationImage");
                    if (saved != null) {
                        for (String address : saved) {
                            boolean predefined = false;
                            for (ImageItem item : predefinedItems) {
                                if (address.equals(item.getAddress())) {
                                    predefined = true;
                                    break;
                                }
                            }
                            if (!predefined) {
                                if (!address.startsWith("color:")) {
                                    File file = new File(address);
                                    if (!file.exists()) {
                                        TableStringValues.delete("GameEliminationImage", address);
                                        continue;
                                    }
                                }
                                items.add(new ImageItem().setAddress(address));
                            }
                        }
                    }
                    items.addAll(predefinedItems);
                    defaultSelected = items.get(0).getAddress();
                    for (int i = 0; i < items.size(); ++i) {
                        ImageItem item = items.get(i);
                        item.getSelected().addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue ov, Boolean t, Boolean t1) {
                                if (!isSettingValues) {
                                    setChessImagesLabel();
                                }
                            }
                        });
                        item.setIndex(i);
                        if (i > 0 && i < 8) {
                            defaultSelected += "," + item.getAddress();
                        }
                    }
                    return true;

                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                initChessesTab(items, defaultSelected);
                initRulersTab();
                initSettingsTab();

            }

        };
        start(task);
    }

    protected void initChessesTab(List<ImageItem> items, String defaultSelected) {
        try {
            isSettingValues = true;
            imagesListview.setCellFactory((ListView<ImageItem> param) -> {
                ListImageCheckBoxCell cell = new ListImageCheckBoxCell();
                return cell;
            });
            imagesListview.getItems().addAll(items);

            List<String> selected = Arrays.asList(UserConfig.getString("GameEliminationChessImages",
                    defaultSelected).split(","));
            if (selected.isEmpty()) {
                selected = Arrays.asList(defaultSelected.split(","));
            }
            for (int i = 0; i < imagesListview.getItems().size(); ++i) {
                ImageItem item = imagesListview.getItems().get(i);
                if (selected.contains(item.getAddress())) {
                    item.setSelected(true);
                }
            }

            imagesListview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            imagesListview.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ImageItem>() {
                @Override
                public void changed(ObservableValue ov, ImageItem oldVal, ImageItem newVal) {
                    viewImage();
                }
            });

            chessSize = 50;
            chessSizeSelector.getItems().addAll(Arrays.asList(
                    "50", "40", "60", "30", "80"
            ));

            shadowCheck.setSelected(UserConfig.getBoolean("GameEliminationShadow", false));
            arcCheck.setSelected(UserConfig.getBoolean("GameEliminationArc", false));
            chessSizeSelector.getSelectionModel().select(UserConfig.getString("GameEliminationChessImageSize", "50"));
            isSettingValues = false;

            setChessImagesLabel();
            imagesListview.getSelectionModel().select(0);
            okChessesAction();

        } catch (Exception e) {
            MyBoxLog.debug(e);
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

            numberColumn.setCellValueFactory(new PropertyValueFactory<>("adjacentNumber"));

            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
            scoreColumn.setCellFactory(new Callback<TableColumn<ScoreRuler, Integer>, TableCell<ScoreRuler, Integer>>() {
                @Override
                public TableCell<ScoreRuler, Integer> call(TableColumn<ScoreRuler, Integer> param) {
                    TableAutoCommitCell<ScoreRuler, Integer> cell
                            = new TableAutoCommitCell<ScoreRuler, Integer>(new IntegerStringFromatConverter()) {

                        @Override
                        public boolean valid(Integer value) {
                            return value != null && value >= 0;
                        }

                        @Override
                        public boolean setCellValue(Integer value) {
                            try {
                                if (!valid(value) || !isEditingRow()) {
                                    cancelEdit();
                                    return false;
                                }
                                ScoreRuler row = scoreRulersData.get(editingRow);
                                if (row == null || value == null || value < 0) {
                                    cancelEdit();
                                    return false;
                                }
                                row.score = value;
                                return super.setCellValue(value);
                            } catch (Exception e) {
                                MyBoxLog.debug(e);
                                return false;
                            }
                        }
                    };
                    return cell;
                }
            });
            scoreColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void initSettingsTab() {
        try {
            isSettingValues = true;

            guaiRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    if (newVal) {
                        UserConfig.setString("GameEliminationSound", "Guai");
                    }
                }
            });
            benRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    if (newVal) {
                        UserConfig.setString("GameEliminationSound", "Ben");
                    }
                }
            });
            guaiBenRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    if (newVal) {
                        UserConfig.setString("GameEliminationSound", "GuaiBen");
                    }
                }
            });
            muteRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    if (newVal) {
                        UserConfig.setString("GameEliminationSound", "Mute");
                    }
                }
            });
            customizedSoundRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    if (newVal) {
                        UserConfig.setString("GameEliminationSound", "Customized");
                    }
                }
            });
            String sound = UserConfig.getString("GameEliminationSound", "Guai");
            switch (sound) {
                case "Ben":
                    benRadio.setSelected(true);
                    break;
                case "GuaiBen":
                    guaiBenRadio.setSelected(true);
                    break;
                case "Mute":
                    muteRadio.setSelected(true);
                    break;
                case "Customized":
                    customizedSoundRadio.setSelected(true);
                    break;
                default:
                    guaiRadio.setSelected(true);
                    break;
            }

            deadRenewRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setString("GameEliminationDead", "Renew");
                }
            });
            deadChanceRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setString("GameEliminationDead", "Chance");
                }
            });
            deadPromptRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setString("GameEliminationDead", "Prompt");
                }
            });
            String dead = UserConfig.getString("GameEliminationDead", "Renew");
            switch (dead) {
                case "Chance":
                    deadChanceRadio.setSelected(true);
                    break;
                case "Prompt":
                    deadPromptRadio.setSelected(true);
                    break;
                default:
                    deadRenewRadio.setSelected(true);
                    break;
            }

            speed1Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    autoSpeed = 1000;
                    UserConfig.setString("GameEliminationAutoSpeed", "1");
                }
            });
            speed2Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    autoSpeed = 2000;
                    UserConfig.setString("GameEliminationAutoSpeed", "2");
                }
            });
            speed3Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    autoSpeed = 3000;
                    UserConfig.setString("GameEliminationAutoSpeed", "3");
                }
            });
            speed5Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    autoSpeed = 5000;
                    UserConfig.setString("GameEliminationAutoSpeed", "5");
                }
            });
            autoSpeed = 2000;
            String speed = UserConfig.getString("GameEliminationAutoSpeed", "2");
            switch (speed) {
                case "1":
                    speed1Radio.setSelected(true);
                    break;
                case "3":
                    speed3Radio.setSelected(true);
                    break;
                case "5":
                    speed5Radio.setSelected(true);
                    break;
                default:
                    speed2Radio.setSelected(true);
                    break;
            }

            flush0Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    flushTimes = 0;
                    eliminateDelay = flushDuration * (2 * flushTimes + 1);
                    UserConfig.setString("GameEliminationFlushTime", "0");
                }
            });
            flush1Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    flushTimes = 1;
                    eliminateDelay = flushDuration * (2 * flushTimes + 1);
                    UserConfig.setString("GameEliminationFlushTime", "1");
                }
            });
            flush2Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    flushTimes = 2;
                    eliminateDelay = flushDuration * (2 * flushTimes + 1);
                    UserConfig.setString("GameEliminationFlushTime", "2");
                }
            });
            flush3Radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    flushTimes = 3;
                    eliminateDelay = flushDuration * (2 * flushTimes + 1);
                    UserConfig.setString("GameEliminationFlushTime", "3");
                }
            });
            flushTimes = 2;
            eliminateDelay = flushDuration * (2 * flushTimes + 1);
            String flush = UserConfig.getString("GameEliminationFlushTime", "2");
            switch (flush) {
                case "1":
                    flush1Radio.setSelected(true);
                    break;
                case "3":
                    flush3Radio.setSelected(true);
                    break;
                case "0":
                    flush0Radio.setSelected(true);
                    break;
                default:
                    flush2Radio.setSelected(true);
                    break;
            }

            scoreCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    UserConfig.setBoolean("GameEliminationPopScores", scoreCheck.isSelected());
                }
            });
            scoreCheck.setSelected(UserConfig.getBoolean("GameEliminationPopScores", true));

            selectSoundBox.disableProperty().bind(customizedSoundRadio.selectedProperty().not());

            String sfile = UserConfig.getString("GameEliminationSoundFile", null);
            if (sfile != null) {
                soundFile = new File(sfile);
                if (soundFile.exists()) {
                    soundFileLabel.setText(soundFile.getAbsolutePath());
                } else {
                    soundFile = null;
                }
            }

            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setChessImagesLabel() {
        if (isSettingValues) {
            return;
        }
        int count = 0;
        for (ImageItem item : imagesListview.getItems()) {
            if (item.isSelected()) {
                count++;
            }
        }
        chessesLabel.setText(MessageFormat.format(message("SelectChesses"), count));
    }

    public void viewImage() {
        if (isSettingValues) {
            return;
        }
        imageInfoController.clear();
        ImageItem selected = imagesListview.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isColor()) {
            return;
        }
        File file = selected.getFile();
        if (file == null || !file.exists()) {
            return;
        }
        String body = "<Img src='" + file.toURI().toString() + "' width=" + selected.getWidth() + ">\n";
        String comments = selected.getComments();
        if (comments != null && !comments.isBlank()) {
            body += "<BR>" + message(comments);
        }
        imageInfoController.loadContent(HtmlWriteTools.html(body));
    }

    @Override
    public void selectSourceFileDo(File file) {
        if (file == null) {
            return;
        }
        recordFileOpened(file);
        addImageItem(file.getAbsolutePath());
    }

    @FXML
    protected void okChessesAction() {
        makeChesses();
    }

    @FXML
    protected void okRulersAction() {
        if (isSettingValues) {
            return;
        }
        catButton.setSelected(false);
        try {
            countedChesses.clear();
            String s = "";
            for (Node node : countedImagesPane.getChildren()) {
                CheckBox cbox = (CheckBox) node;
                if (cbox.isSelected()) {
                    int index = (int) cbox.getUserData();
                    countedChesses.add(index);
                    ImageItem item = getImageItem(index);
                    if (s.isBlank()) {
                        s += item.getAddress();
                    } else {
                        s += "," + item.getAddress();
                    }
                }
            }
            if (countedChesses.isEmpty()) {
                if (!PopTools.askSure(getTitle(), message("SureNoScore"))) {
                    return;
                }
            }
            UserConfig.setString("GameEliminationCountedImages", s);

            scoreRulers.clear();
            s = "";
            for (int i = 0; i < scoreRulersData.size(); ++i) {
                ScoreRuler r = scoreRulersData.get(i);
                scoreRulers.put(r.adjacentNumber, r.score);
                if (!s.isEmpty()) {
                    s += ",";
                }
                s += r.adjacentNumber + "," + r.score;
            }
            UserConfig.setString("GameElimniationScoreRulers", s);

            tabPane.getSelectionModel().select(playTab);
            newGame(true);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    protected void clearChessSelectionAction() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        for (ImageItem item : imagesListview.getItems()) {
            item.setSelected(false);
        }
        isSettingValues = false;
        chessesLabel.setText(MessageFormat.format(message("SelectChesses"), 0));
        imagesListview.refresh();
    }

    @FXML
    protected void deleteChessesAction() {
        if (isSettingValues) {
            return;
        }
        catButton.setSelected(false);
        isSettingValues = true;
        List<ImageItem> selected = new ArrayList();
        selected.addAll(imagesListview.getSelectionModel().getSelectedItems());
        List<ImageItem> predefined = ImageItem.predefined();
        for (int i = 0; i < selected.size(); ++i) {
            ImageItem item = selected.get(i);
            if (item.getAddress() == null || !predefined.contains(item)) {
                imagesListview.getItems().remove(item);
                TableStringValues.delete("GameEliminationImage", item.getAddress());
            }
        }
        isSettingValues = false;
        setChessImagesLabel();
    }

    @FXML
    protected void clearCountedImagesAction() {
        if (isSettingValues) {
            return;
        }
        for (Node node : countedImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            cbox.setSelected(false);
        }
    }

    @FXML
    protected void allCountedImagesAction() {
        if (isSettingValues) {
            return;
        }
        for (Node node : countedImagesPane.getChildren()) {
            CheckBox cbox = (CheckBox) node;
            cbox.setSelected(true);
        }
    }

    @FXML
    @Override
    public void createAction() {
        if (isSettingValues) {
            return;
        }
        catButton.setSelected(false);
        newGame(true);
    }

    @FXML
    protected void settingsAction() {
        tabPane.getSelectionModel().select(settingsTab);
    }

    @FXML
    public void helpMeAction() {
        if (isSettingValues || autoPlaying) {
            return;
        }
        Adjacent adjacent = findValidExchange();
        if (adjacent != null) {
            flush(adjacent.exchangei1, adjacent.exchangej1);
            flush(adjacent.exchangei2, adjacent.exchangej2);
        } else {
            promptDeadlock();
        }
    }

    protected void setAutoplay() {
        catButton.setSelected(!catButton.isSelected());
    }

    @FXML
    public void selectSoundFile() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            File file = mara.mybox.fxml.FxFileTools.selectFile(this, UserConfig.getPath("MusicPath"),
                    FileFilters.Mp3WavExtensionFilter);
            if (file == null) {
                return;
            }
            selectSoundFile(file);
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    public void selectSoundFile(File file) {
        recordFileOpened(file);
        String suffix = FileNameTools.ext(file.getName());
        if (suffix == null
                || (!"mp3".equals(suffix.toLowerCase()) && !"wav".equals(suffix.toLowerCase()))) {
            alertError(message("OnlySupportMp3Wav"));
            return;
        }
        soundFile = file;
        UserConfig.setString("MusicPath", file.getParent());
        UserConfig.setString("GameEliminationSoundFile", file.getAbsolutePath());
        soundFileLabel.setText(file.getAbsolutePath());
        SoundTools.mp3(soundFile);
    }

    @FXML
    public void showSoundFileMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        RecentVisitMenu menu = new RecentVisitMenu(this, event, false) {

            @Override
            public void handleSelect() {
                selectSoundFile();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSoundFile();
                    return;
                }
                selectSoundFile(file);
            }

        };
        menu.setSourceFileType(VisitHistory.FileType.Media)
                .setSourcePathType(VisitHistory.FileType.Media)
                .setSourceExtensionFilter(FileFilters.Mp3WavExtensionFilter);
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            menu.setDefaultPath("C:\\Windows\\media");
        }
        menu.pop();
    }

    @FXML
    public void pickSoundFile(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectSoundFile();
        } else {
            showSoundFileMenu(event);
        }
    }

    @FXML
    public void popSoundFile(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showSoundFileMenu(event);
        }
    }

    public boolean addImageItem(String address) {
        if (isSettingValues || address == null) {
            return false;
        }
        for (ImageItem item : imagesListview.getItems()) {
            if (item.getAddress().equals(address)) {
                return false;
            }
        }
        catButton.setSelected(false);
        isSettingValues = true;
        ImageItem item = new ImageItem().setAddress(address);
        item.getSelected().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean t, Boolean t1) {
                if (!isSettingValues) {
                    setChessImagesLabel();
                }
            }
        });
        imagesListview.getItems().add(0, item);
        imagesListview.scrollTo(0);
        TableStringValues.add("GameEliminationImage", address);
        isSettingValues = false;
        return true;
    }

    protected void makeChesses() {
        if (isSettingValues) {
            return;
        }
        catButton.setSelected(false);
        try {
            chessSize = Integer.parseInt(chessSizeSelector.getValue());
            if (chessSize < 20) {
                chessSize = 20;
            }
        } catch (Exception e) {
            chessSize = 50;
        }
        UserConfig.setString("GameEliminationChessImageSize", chessSize + "");
        UserConfig.setBoolean("GameEliminationShadow", shadowCheck.isSelected());
        UserConfig.setBoolean("GameEliminationArc", arcCheck.isSelected());

        selectedChesses.clear();
        String s = "";
        for (int i = 0; i < imagesListview.getItems().size(); ++i) {
            ImageItem item = imagesListview.getItems().get(i);
            item.setIndex(i);
            if (item.isSelected()) {
                selectedChesses.add(i);
                if (s.isBlank()) {
                    s += item.getAddress() + "";
                } else {
                    s += "," + item.getAddress();
                }
            }
        }
        if (selectedChesses.size() <= minimumAdjacent) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(getBaseTitle());
            alert.setContentText(MessageFormat.format(message("ChessesNumberTooSmall"), minimumAdjacent + ""));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            alert.showAndWait();
            return;
        }
        UserConfig.setString("GameEliminationChessImages", s);
        boardSize = selectedChesses.size();
        tabPane.getSelectionModel().select(playTab);
        makeChessBoard();
        makeRulers();
        newGame(true);
    }

    protected void makeChessBoard() {
        if (isSettingValues) {
            return;
        }
        try {
            chessBoard.clear();
            chessboardPane.getChildren().clear();
            chessboardPane.setPrefWidth((chessSize + 20) * boardSize);
            chessboardPane.setPrefHeight((chessSize + 20) * boardSize);
            DropShadow effect = new DropShadow();
            boolean shadow = shadowCheck.isSelected();
            boolean arc = arcCheck.isSelected();
            currentStyle = defaultStyle;
            if (arc) {
                currentStyle = arcStyle;
            } else if (shadow) {
                currentStyle = shadowStyle;
            }
            for (int i = 1; i <= boardSize; ++i) {
                HBox line = new HBox();
                line.setAlignment(Pos.CENTER);
                line.setSpacing(10);
                chessboardPane.getChildren().add(line);
                VBox.setVgrow(line, Priority.NEVER);
                HBox.setHgrow(line, Priority.NEVER);
                for (int j = 1; j <= boardSize; ++j) {
                    VBox vbox = new VBox();
                    vbox.setAlignment(Pos.CENTER);
                    VBox.setVgrow(vbox, Priority.NEVER);
                    HBox.setHgrow(vbox, Priority.NEVER);
                    vbox.setSpacing(6);
                    if (shadow) {
                        vbox.setEffect(effect);
                    }
                    vbox.setStyle(currentStyle);
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
            MyBoxLog.debug(e);
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
                String s = UserConfig.getString("GameElimniationScoreRulers", "");
                if (s != null && !s.isEmpty()) {
                    String[] ss = s.split(",");
                    for (int i = 1; i < ss.length; i = i + 2) {
                        scoreRulers.put(Integer.valueOf(ss[i - 1]), Integer.parseInt(ss[i]));
                    }
                }
            } catch (Exception e) {
            }
            for (int i = 3; i <= boardSize; ++i) {
                if (scoreRulers.get(i) == null) {
                    scoreRulers.put(i, (int) Math.pow(10, i - 3));
                }
                ScoreRuler r = new ScoreRuler(i, scoreRulers.get(i));
                scoreRulersData.add(r);
            }
            rulersTable.refresh();

            countedChesses.clear();
            countedImagesPane.getChildren().clear();
            for (int i = 0; i < selectedChesses.size(); ++i) {
                int index = selectedChesses.get(i);
                ImageItem item = getImageItem(index);
                Node node = item.makeNode(50);
                CheckBox cbox = new CheckBox();
                cbox.setGraphic(node);
                cbox.setUserData(index);
                countedImagesPane.getChildren().add(cbox);
                countedChesses.add(index);
                cbox.setSelected(true);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void newGame(boolean reset) {
        if (isSettingValues) {
            return;
        }
        try {
            if (firstClick != null) {
                recoverStyle(firstClick.getX(), firstClick.getY());
                firstClick = null;
            }
            for (int i = 1; i <= boardSize; ++i) {
                for (int j = 1; j <= boardSize; ++j) {
                    setRandomImage(i, j);
                }
            }
            if (reset) {
                totalScore = 0;
                scoreLabel.setText("");
                startTime = new Date();
            }
            countScore = false;
            findAdjacentAndEliminate();
            countScore = true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void chessClicked(int i, int j) {
        try {
            if (isSettingValues || isEliminating || autoPlaying) {
                return;
            }
            if (firstClick == null) {
                firstClick = new IntPoint(i, j);
                focusStyle(i, j);
                return;
            }
            isEliminating = true;
            countScore = true;
            exchange(i, j, firstClick.getX(), firstClick.getY(), false);
            firstClick = null;
            isEliminating = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected Adjacent exchange(int i1, int j1, int i2, int j2, boolean justCheck) {
        if (isSettingValues) {
            return null;
        }
        try {
            recoverStyle(i1, j1);
            recoverStyle(i2, j2);
            if (i1 == i2 && j1 == j2) {
                return null;
            }
            if (Math.abs(i1 - i2) > 1
                    || Math.abs(j1 - j2) > 1) {
                return null;
            }

            VBox vbox1 = chessBoard.get(i1 + "-" + j1);
            Node node1 = vbox1.getChildren().get(0);
            VBox vbox2 = chessBoard.get(i2 + "-" + j2);
            Node node2 = vbox2.getChildren().get(0);
            vbox1.getChildren().clear();
            vbox1.getChildren().add(node2);
            vbox2.getChildren().clear();
            vbox2.getChildren().add(node1);

            Adjacent adjacent;
            adjacent = verticalCheck(i1, j1);
            if (adjacent != null && adjacent.getLength() >= minimumAdjacent) {
                adjacent.setExchange(i1, j1, i2, j2);
                if (!justCheck) {
                    verticalEliminate(adjacent);
                } else {
                    vbox1.getChildren().clear();
                    vbox1.getChildren().add(node1);
                    vbox2.getChildren().clear();
                    vbox2.getChildren().add(node2);
                }
                return adjacent;
            }

            adjacent = horizontalCheck(i1, j1);
            if (adjacent != null && adjacent.getLength() >= minimumAdjacent) {
                adjacent.setExchange(i1, j1, i2, j2);
                if (!justCheck) {
                    horizontalEliminate(adjacent);
                } else {
                    vbox1.getChildren().clear();
                    vbox1.getChildren().add(node1);
                    vbox2.getChildren().clear();
                    vbox2.getChildren().add(node2);
                }
                return adjacent;
            }

            adjacent = verticalCheck(i2, j2);
            if (adjacent != null && adjacent.getLength() >= minimumAdjacent) {
                adjacent.setExchange(i1, j1, i2, j2);
                if (!justCheck) {
                    verticalEliminate(adjacent);
                } else {
                    vbox1.getChildren().clear();
                    vbox1.getChildren().add(node1);
                    vbox2.getChildren().clear();
                    vbox2.getChildren().add(node2);
                }
                return adjacent;
            }

            adjacent = horizontalCheck(i2, j2);
            if (adjacent != null && adjacent.getLength() >= minimumAdjacent) {
                adjacent.setExchange(i1, j1, i2, j2);
                if (!justCheck) {
                    horizontalEliminate(adjacent);
                } else {
                    vbox1.getChildren().clear();
                    vbox1.getChildren().add(node1);
                    vbox2.getChildren().clear();
                    vbox2.getChildren().add(node2);
                }
                return adjacent;
            }

            // No elimination, and reback.
            vbox1.getChildren().clear();
            vbox1.getChildren().add(node1);
            vbox2.getChildren().clear();
            vbox2.getChildren().add(node2);
            return null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    protected Adjacent exchange(Adjacent adjacent) {
        try {
            if (isSettingValues || adjacent == null
                    || !adjacent.isValid() || adjacent.getLength() < minimumAdjacent) {
                return null;
            }

            VBox vbox1 = chessBoard.get(adjacent.exchangei1 + "-" + adjacent.exchangej1);
            Node node1 = vbox1.getChildren().get(0);
            VBox vbox2 = chessBoard.get(adjacent.exchangei2 + "-" + adjacent.exchangej2);
            Node node2 = vbox2.getChildren().get(0);
            vbox1.getChildren().clear();
            vbox1.getChildren().add(node2);
            vbox2.getChildren().clear();
            vbox2.getChildren().add(node1);

            if (adjacent.isVertical()) {
                verticalEliminate(adjacent);
            } else {
                horizontalEliminate(adjacent);
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    protected Adjacent verticalCheck(int i, int j) {
        if (isSettingValues) {
            return null;
        }
        try {
            int imageIndex = getImageIndex(i, j);
            int i1 = i;
            while (i1 > 1) {
                int moveIndex = getImageIndex(i1 - 1, j);
                if (imageIndex != moveIndex) {
                    break;
                }
                i1--;
            }
            int i2 = i;
            while (i2 < boardSize) {
                int moveIndex = getImageIndex(i2 + 1, j);
                if (imageIndex != moveIndex) {
                    break;
                }
                i2++;
            }
            return new Adjacent(i1, j, i2, j);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    protected Adjacent horizontalCheck(int i, int j) {
        if (isSettingValues) {
            return null;
        }
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
            return new Adjacent(i, j1, i, j2);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }

    }

    protected void verticalEliminate(Adjacent adjacent) {
        if (isSettingValues || !adjacent.isValid() || !adjacent.isVertical()) {
            return;
        }

        if (countScore && flushTimes > 0) {
            for (int row = adjacent.starti; row <= adjacent.endi; row++) {
                flush(row, adjacent.startj);
            }
            Timer vtimer = new Timer();
            vtimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            verticalEliminateDo(adjacent);
                        }
                    });
                }
            }, eliminateDelay);
        } else {
            verticalEliminateDo(adjacent);
        }
    }

    protected void verticalEliminateDo(Adjacent adjacent) {
        if (isSettingValues) {
            return;
        }
        try {
            Node node = getImageNode(adjacent.starti, adjacent.startj);
            int len = adjacent.getLength();
            for (int row = adjacent.starti - 1; row > 0; row--) {
                node = getImageNode(row, adjacent.startj);
                VBox vbox = chessBoard.get((row + len) + "-" + adjacent.startj);
                vbox.getChildren().clear();
                vbox.getChildren().add(node);
            }
            for (int row = 1; row <= len; row++) {
                setRandomImage(row, adjacent.startj);
            }
            lastElimination = adjacent;
            lastRandom = new Adjacent(1, adjacent.startj, len, adjacent.startj);
            afterElimination((int) node.getUserData(), adjacent.getLength());
            findAdjacentAndEliminate();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void horizontalEliminate(Adjacent adjacent) {
        if (isSettingValues || !adjacent.isValid() || adjacent.isVertical()) {
            return;
        }
        if (countScore && flushTimes > 0) {
            for (int col = adjacent.startj; col <= adjacent.endj; col++) {
                flush(adjacent.starti, col);
            }
            Timer htimer = new Timer();
            htimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            horizontalEliminateDo(adjacent);
                        }
                    });
                }
            }, eliminateDelay);
        } else {
            horizontalEliminateDo(adjacent);
        }
    }

    protected void horizontalEliminateDo(Adjacent adjacent) {
        if (isSettingValues) {
            return;
        }
        try {
            Node node = getImageNode(adjacent.starti, adjacent.startj);
            for (int row = adjacent.starti; row > 1; row--) {
                for (int col = adjacent.startj; col <= adjacent.endj; col++) {
                    node = getImageNode(row - 1, col);
                    VBox vbox = chessBoard.get(row + "-" + col);
                    vbox.getChildren().clear();
                    vbox.getChildren().add(node);
                }
            }
            for (int col = adjacent.startj; col <= adjacent.endj; col++) {
                setRandomImage(1, col);
            }
            lastElimination = adjacent;
            lastRandom = new Adjacent(1, adjacent.startj, 1, adjacent.endj);
            afterElimination((int) node.getUserData(), adjacent.getLength());
            findAdjacentAndEliminate();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void findAdjacentAndEliminate() {
        if (isSettingValues) {
            return;
        }
        try {
            // Eliminate bottom in priority
            for (int i = 1; i <= boardSize; ++i) {
                int j = boardSize;
                while (j > 0) {
                    Adjacent adjacent = horizontalCheck(i, j);
                    if (adjacent != null && adjacent.getLength() >= minimumAdjacent) {
                        horizontalEliminate(adjacent);
                        return;
                    }
                    if (adjacent != null && adjacent.getLength() > 0) {
                        j = j - adjacent.getLength();
                    } else {
                        j = j - 1;
                    }
                }
            }
            for (int j = boardSize; j > 0; --j) {
                int i = 1;
                while (i <= boardSize) {
                    Adjacent adjacent = verticalCheck(i, j);
                    if (adjacent != null && adjacent.getLength() >= minimumAdjacent) {
                        verticalEliminate(adjacent);
                        return;
                    }
                    if (adjacent != null && adjacent.getLength() > 0) {
                        i = i + adjacent.getLength();
                    } else {
                        i = i + 1;
                    }
                }
            }
            checkDeadlock();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void afterElimination(int imageIndex, int size) {
        try {
            if (isSettingValues || !countScore || !countedChesses.contains(imageIndex)) {
                return;
            }
            int add = scoreRulers.get(size);
            totalScore += add;
            long cost = new Date().getTime() - startTime.getTime();
            scoreLabel.setText(message("Score") + ": " + totalScore + "  " + message("Cost") + ": "
                    + DateTools.timeDuration(cost));

            if (add > 0) {
                if (scoreCheck.isSelected()) {
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
                    LocateTools.locateUp(scoreLabel, scorePopup);

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
                    }, 1000);

                }
                if (guaiRadio.isSelected()) {
                    SoundTools.GuaiAO();
                } else if (benRadio.isSelected()) {
                    SoundTools.BenWu();
                } else if (guaiBenRadio.isSelected()) {
                    if (size == 3) {
                        SoundTools.BenWu();
                    } else {
                        SoundTools.GuaiAO();
                    }
                } else if (customizedSoundRadio.isSelected() && soundFile != null && soundFile.exists()) {
                    SoundTools.mp3(soundFile);
                }
            }
        } catch (Exception e) {

        }
    }

    protected Adjacent findValidExchange() {
        if (isSettingValues) {
            return null;
        }
        try {
            for (int i = boardSize; i > 0; --i) {
                for (int j = 1; j <= boardSize; ++j) {
                    if (j < boardSize) {
                        Adjacent adjacent = exchange(i, j, i, j + 1, true);
                        if (adjacent != null) {
                            return adjacent;
                        }
                    }
                    if (i < boardSize) {
                        Adjacent adjacent = exchange(i, j, i + 1, j, true);
                        if (adjacent != null) {
                            return adjacent;
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return null;
    }

    protected void checkDeadlock() {
        if (isSettingValues) {
            return;
        }
        try {
            firstClick = null;
            Adjacent adjacent = findValidExchange();
            if (adjacent != null) {
                if (autoPlaying) {
                    focusStyle(adjacent.exchangei1, adjacent.exchangej1);
                    focusStyle(adjacent.exchangei2, adjacent.exchangej2);
                    Timer vtimer = new Timer();
                    vtimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    recoverStyle(adjacent.exchangei1, adjacent.exchangej1);
                                    recoverStyle(adjacent.exchangei2, adjacent.exchangej2);
                                    exchange(adjacent);
                                }
                            });
                        }
                    }, autoSpeed);
                }
                return;
            }

            if (autoPlaying || deadRenewRadio.isSelected()) {
                newGame(false);
                popInformation(message("DeadlockDetectRenew"));
            } else if (lastRandom == null || deadPromptRadio.isSelected()) {
                promptDeadlock();
            } else {
                makeChance();
                popInformation(message("DeadlockDetectChance"));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void makeChance() {
        if (isSettingValues) {
            return;
        }
        try {
            int imagei = 1, imagej = 1, seti1 = 1, seti2 = 1, setj1 = 1, setj2 = 1;
            if (lastRandom.isVertical()) {
                if (lastRandom.endi < boardSize) {
                    imagei = lastRandom.endi + 1;
                    imagej = lastRandom.startj;
                    seti1 = lastRandom.endi - 1;
                    seti2 = lastRandom.endi - 2;
                    setj1 = setj2 = lastRandom.startj;
                } else {
                    imagei = lastRandom.starti - 1;
                    imagej = lastRandom.startj;
                    seti1 = lastRandom.starti + 1;
                    seti2 = lastRandom.starti + 2;
                    setj1 = setj2 = lastRandom.startj;
                }
            } else {
                if (lastRandom.startj > 1) {
                    imagei = lastRandom.starti;
                    imagej = lastRandom.startj - 1;
                    seti1 = seti2 = lastRandom.starti;
                    setj1 = lastRandom.startj + 1;
                    setj2 = lastRandom.startj + 2;

                } else {
                    imagei = lastRandom.starti;
                    imagej = lastRandom.endj + 1;
                    seti1 = seti2 = lastRandom.starti;
                    setj1 = lastRandom.endj - 1;
                    setj2 = lastRandom.endj - 2;
                }
            }
            ImageItem item = getImageItem(imagei, imagej);
            setImageNode(seti1, setj1, item);
            setImageNode(seti2, setj2, item);
            findAdjacentAndEliminate();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void promptDeadlock() {
        try {
            SoundTools.BenWu2();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(getBaseTitle());
            alert.setContentText(message("NoValidElimination"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonRenew = new ButtonType(message("RenewGame"));
            ButtonType buttonChance = new ButtonType(message("MakeChance"));
            alert.getButtonTypes().setAll(buttonRenew, buttonChance);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return;
            }
            if (result.get() == buttonRenew) {
                newGame(false);
                popInformation(message("DeadlockDetectRenew"));
            } else if (result.get() == buttonChance) {
                makeChance();
                popInformation(message("DeadlockDetectChance"));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean inputFilter(String input, boolean omit) {
        if (input != null) {
            switch (input.toUpperCase()) {
                case "H":
                    helpMeAction();
                    return true;
                case "N":
                    createAction();
                    return true;
                case "P":
                    setAutoplay();
                    return true;
            }
        }
        return super.inputFilter(input, omit);
    }

    @Override
    public boolean controlAltP() {
        if (targetIsTextInput()) {
            return false;
        }
        setAutoplay();
        return true;
    }

    @Override
    public boolean controlAltH() {
        if (targetIsTextInput()) {
            return false;
        }
        helpMeAction();
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

    @Override
    public void cleanPane() {
        try {
            catButton.setSelected(false);
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        utilities
     */
    protected VBox getBox(int i, int j) {
        try {
            VBox vbox = chessBoard.get(i + "-" + j);
            return vbox;
        } catch (Exception e) {
            return null;
        }
    }

    protected Node getImageNode(int i, int j) {
        try {
            VBox vbox = getBox(i, j);
            return vbox.getChildren().get(0);
        } catch (Exception e) {
            return null;
        }
    }

    protected int getImageIndex(int i, int j) {
        try {
            Node node = getImageNode(i, j);
            return (int) (node.getUserData());
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return -1;
        }
    }

    protected ImageItem getImageItem(int index) {
        try {
            return imagesListview.getItems().get(index);
        } catch (Exception e) {
            return null;
        }
    }

    protected ImageItem getImageItem(int i, int j) {
        try {
            int index = getImageIndex(i, j);
            return getImageItem(index);
        } catch (Exception e) {
            return null;
        }
    }

    protected void setRandomImage(int i, int j) {
        try {
            int index = selectedChesses.get(random.nextInt(boardSize));
            setImageNode(i, j, imagesListview.getItems().get(index));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void setImageNode(int i, int j, ImageItem item) {
        try {
            Node node = item.makeNode(chessSize);
            VBox vbox = getBox(i, j);
            vbox.getChildren().clear();
            vbox.getChildren().add(node);
            vbox.setStyle(currentStyle);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void flush(int i, int j) {
        if (flushTimes < 1) {
            return;
        }
        try {
            FadeTransition fade = new FadeTransition(Duration.millis(flushDuration));
            fade.setFromValue(1.0);
            fade.setToValue(0f);
            fade.setCycleCount(flushTimes * 2);
            fade.setAutoReverse(true);
            fade.setNode(chessBoard.get(i + "-" + j));
            fade.play();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void shake(int i1, int j1) {
        try {
            VBox vbox = chessBoard.get(i1 + "-" + j1);
            double x = vbox.getLayoutX();
            double y = vbox.getLayoutY();
            Path path = new Path();
            path.getElements().add(new LineTo(x - 20, y + 18));
            path.getElements().add(new LineTo(x + 20, y + 18));
            path.getElements().add(new LineTo(x - 20, y + 18));
            path.getElements().add(new LineTo(x + 20, y + 18));
            path.getElements().add(new LineTo(x, y));

            PathTransition pathTransition = new PathTransition(Duration.millis(200), path, vbox);
            pathTransition.setCycleCount(3);
            pathTransition.setAutoReverse(true);
            pathTransition.play();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void focusStyle(int i, int j) {
        try {
            VBox vbox = getBox(i, j);
            vbox.setStyle(focusStyle);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void recoverStyle(int i, int j) {
        try {
            VBox vbox = getBox(i, j);
            vbox.setStyle(currentStyle);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }


    /*
        data
     */
    // Assume only veritical and horizontal
    public class Adjacent {

        protected int exchangei1, exchangej1, exchangei2, exchangej2;
        protected int starti, startj, endi, endj;

        public Adjacent(int starti, int startj, int endi, int endj) {
            this.starti = starti;
            this.startj = startj;
            this.endi = endi;
            this.endj = endj;
        }

        public void setExchange(int exchangei1, int exchangej1, int exchangei2, int exchangej2) {
            this.exchangei1 = exchangei1;
            this.exchangej1 = exchangej1;
            this.exchangei2 = exchangei2;
            this.exchangej2 = exchangej2;
        }

        public int getLength() {
            if (starti == endi) {
                return endj - startj + 1;
            } else {
                return endi - starti + 1;
            }
        }

        public boolean isValid() {
            if (starti < 1 || starti > boardSize
                    || endi < 1 || endi > boardSize
                    || startj < 1 || startj > boardSize
                    || endj < 1 || endj > boardSize) {
                return false;
            }
            return starti == endi || startj == endj;
        }

        public boolean isVertical() {
            return starti != endi;
        }

        public void print() {
            MyBoxLog.debug(starti + "," + startj + " -- " + endi + "," + endj);
        }

    }

    public class ScoreRuler {

        protected int adjacentNumber, score;

        public ScoreRuler(int linksNumber, int score) {
            this.adjacentNumber = linksNumber;
            this.score = score;
        }

        public int getAdjacentNumber() {
            return adjacentNumber;
        }

        public void setAdjacentNumber(int adjacentNumber) {
            this.adjacentNumber = adjacentNumber;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

    }

}
