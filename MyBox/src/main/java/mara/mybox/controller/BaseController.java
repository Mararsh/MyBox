package mara.mybox.controller;

import java.awt.Toolkit;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mara.mybox.data.BaseTask;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.VisitHistory.FileType;
import mara.mybox.db.TableUserConf;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.getUserConfigValue;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.setUserConfigValue;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:50:43
 * @Description
 * @License Apache License Version 2.0
 */
public class BaseController implements Initializable {

    protected String TipsLabelKey, LastPathKey, targetPathKey, sourcePathKey, defaultPathKey, SaveAsOptionsKey;
    protected int SourceFileType, SourcePathType, TargetFileType, TargetPathType, AddFileType, AddPathType,
            operationType, dpi;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter, targetExtensionFilter;
    protected String myFxml, parentFxml, currentStatus, baseTitle, baseName, loadFxml;
    protected Stage myStage;
    protected Scene myScene;
    protected Alert loadingAlert;
    protected Task<Void> task, backgroundTask;
    protected BaseController parentController, myController;
    protected Timer popupTimer, timer;
    protected Popup popup;
    protected ContextMenu popMenu;
    protected MaximizedListener maximizedListener;
    protected FullscreenListener fullscreenListener;
    protected String targetFileType, targetNameAppend;
    protected ChangeListener<Number> leftDividerListener, rightDividerListener;
    protected boolean isSettingValues;
    protected File sourceFile, sourcePath, targetPath, targetFile;
    protected SaveAsType saveAsType;
    protected TargetExistType targetExistType;
    protected KeyEvent currentKeyEvent;

    protected enum SaveAsType {
        Load, Open, None
    }

    public static enum TargetExistType {
        Rename, Replace, Skip
    }

    @FXML
    protected Pane thisPane, mainMenu, operationBar;
    @FXML
    protected MainMenuController mainMenuController;
    @FXML
    protected TextField sourceFileInput, sourcePathInput, targetAppendInput,
            targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    protected OperationController operationBarController;
    @FXML
    protected Button allButton, clearButton, selectFileButton, createButton, copyButton, pasteButton, cancelButton,
            deleteButton, saveButton, infoButton, metaButton, setButton, addButton,
            okButton, startButton, firstButton, lastButton, previousButton, nextButton, goButton, previewButton,
            cropButton, saveAsButton, recoverButton, renameButton, tipsButton, viewButton, popButton, refButton,
            undoButton, redoButton, transparentButton, whiteButton, blackButton, playButton, stopButton,
            selectAllButton, selectNoneButton, withdrawButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected Label bottomLabel, tipsLabel;
    @FXML
    protected ImageView tipsView, linksView, leftPaneControl, rightPaneControl;
    @FXML
    protected ChoiceBox saveAsOptionsBox;
    @FXML
    protected Hyperlink regexLink;
    @FXML
    protected CheckBox topCheck, saveCloseCheck;
    @FXML
    protected ToggleGroup targetExistGroup, fileTypeGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected ScrollPane leftPane, rightPane;
    @FXML
    protected ComboBox<String> dpiSelector;

    public BaseController() {
        baseTitle = AppVariables.message("AppTitle");

        SourceFileType = FileType.All;
        SourcePathType = FileType.All;
        TargetPathType = FileType.All;
        TargetFileType = FileType.All;
        AddFileType = FileType.All;
        AddPathType = FileType.All;
        operationType = FileType.All;

        LastPathKey = "LastPathKey";
        targetPathKey = "targetPath";
        sourcePathKey = "sourcePath";
        defaultPathKey = null;
        SaveAsOptionsKey = "SaveAsOptionsKey";

        sourceExtensionFilter = CommonFxValues.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            baseName = FxmlControl.getFxmlName(url.getPath());

            initValues();
            initBaseControls();
            initControls();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initValues() {
        try {
            myFxml = "/fxml/" + baseName + ".fxml";
            myController = this;
            if (mainMenuController != null) {
                mainMenuController.parentFxml = myFxml;
                mainMenuController.parentController = this;
            }
            AppVariables.alarmClockController = null;

            setInterfaceStyle(AppVariables.getStyle());
            setSceneFontSize(AppVariables.sceneFontSize);
            if (thisPane != null) {
                thisPane.setStyle("-fx-font-size: " + AppVariables.sceneFontSize + "px;");
                thisPane.setOnKeyReleased((KeyEvent event) -> {
                    keyEventsHandler(event);
                });
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initBaseControls() {
        try {

            if (mainMenuController != null) {
                mainMenuController.sourceExtensionFilter = sourceExtensionFilter;
                mainMenuController.targetExtensionFilter = targetExtensionFilter;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.SourceFileType = SourceFileType;
                mainMenuController.SourcePathType = SourcePathType;
                mainMenuController.TargetPathType = TargetPathType;
                mainMenuController.TargetFileType = TargetFileType;
                mainMenuController.AddFileType = AddFileType;
                mainMenuController.AddPathType = AddPathType;
                mainMenuController.targetPathKey = targetPathKey;
                mainMenuController.LastPathKey = LastPathKey;
            }

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            checkSourceFileInput();
                        });
            }

            if (sourcePathInput != null) {
                sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        checkSourcetPathInput();
                    }
                });
                File sfile = AppVariables.getUserConfigPath(sourcePathKey);
                if (sfile != null) {
                    sourcePathInput.setText(sfile.getAbsolutePath());
                }
            }

            if (targetFileInput != null) {
                targetFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        checkTargetFileInput();
                    }
                });
            }

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        checkTargetPathInput();
                    }
                });
                File tfile = AppVariables.getUserConfigPath(targetPathKey);
                if (tfile != null) {
                    targetPathInput.setText(tfile.getAbsolutePath());
                }
            }

            if (operationBarController != null) {
                operationBarController.parentController = this;
                if (operationBarController.openTargetButton != null) {
                    if (targetFileInput != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetFileInput.textProperty())
                                .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                        );
                    } else if (targetPathInput != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                        );
                    }
                }
            }

            if (tipsLabel != null && TipsLabelKey != null) {
                FxmlControl.setTooltip(tipsLabel, new Tooltip(message(TipsLabelKey)));
            }

            if (tipsView != null && TipsLabelKey != null) {
                FxmlControl.setTooltip(tipsView, new Tooltip(message(TipsLabelKey)));
            }

            if (saveAsOptionsBox != null) {
                try {
                    String vv = AppVariables.getUserConfigValue(SaveAsOptionsKey, SaveAsType.Load + "");
                    if ((SaveAsType.Load + "").equals(vv)) {
                        saveAsType = SaveAsType.Load;

                    } else if ((SaveAsType.Open + "").equals(vv)) {
                        saveAsType = SaveAsType.Open;

                    } else if ((SaveAsType.None + "").equals(vv)) {
                        saveAsType = SaveAsType.None;
                    }

                } catch (Exception e) {
//                logger.error(e.toString());
                    saveAsType = SaveAsType.Load;
                }

                List<String> optionsList = Arrays.asList(message("LoadAfterSaveAs"),
                        message("OpenAfterSaveAs"), message("JustSaveAs"));
                saveAsOptionsBox.getItems().addAll(optionsList);
                saveAsOptionsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue,
                            Number newValue) {
                        checkSaveAsOption();
                    }
                });
                if (null != saveAsType) {
                    switch (saveAsType) {
                        case Load:
                            saveAsOptionsBox.getSelectionModel().select(0);
                            break;
                        case Open:
                            saveAsOptionsBox.getSelectionModel().select(1);
                            break;
                        case None:
                            saveAsOptionsBox.getSelectionModel().select(2);
                            break;
                        default:
                            break;
                    }
                }

            }

            if (topCheck != null) {
                topCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue,
                            Boolean newValue) {
                        if (!topCheck.isVisible()) {
                            return;
                        }
                        if (getMyStage() != null) {
                            myStage.setAlwaysOnTop(topCheck.isSelected());
                        }
                        AppVariables.setUserConfigValue(baseName + "Top", newValue);
                    }
                });
            }

            if (saveCloseCheck != null) {
                saveCloseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldVal, Boolean newVal) {
                        AppVariables.setUserConfigValue(baseName + "SaveClose", saveCloseCheck.isSelected());
                    }
                });
                saveCloseCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "SaveClose", false));
            }

            if (targetExistGroup != null) {
                targetExistGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends Toggle> ov,
                            Toggle old_toggle, Toggle new_toggle) {
                        checkTargetExistType();
                    }
                });
                isSettingValues = true;
                FxmlControl.setRadioSelected(targetExistGroup, getUserConfigValue("TargetExistType", message("Replace")));
                if (targetAppendInput != null) {
                    targetAppendInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> ov,
                                String oldv, String newv) {
                            checkTargetExistType();
                        }
                    });
                    targetAppendInput.setText(getUserConfigValue("TargetExistAppend", "_m"));
                }
                isSettingValues = false;
                checkTargetExistType();
            }

            dpi = 96;
            if (dpiSelector != null) {
                List<String> dpiValues = new ArrayList();
                dpiValues.addAll(Arrays.asList("96", "120", "160", "300"));
                String sValue = Toolkit.getDefaultToolkit().getScreenResolution() + "";
                if (dpiValues.contains(sValue)) {
                    dpiValues.remove(sValue);
                }
                dpiValues.add(0, sValue);
                sValue = (int) Screen.getPrimary().getDpi() + "";
                if (dpiValues.contains(sValue)) {
                    dpiValues.remove(sValue);
                }
                dpiValues.add(sValue);
                dpiSelector.getItems().addAll(dpiValues);
                dpiSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                dpi = Integer.parseInt(newValue);
                                AppVariables.setUserConfigValue(baseName + "DPI", dpi + "");
                            } catch (Exception e) {
                                dpi = 96;
                            }
                        });
                dpiSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "DPI", "96"));
            }

            if (splitPane != null && leftPane != null && leftPaneControl != null) {
                if (getUserConfigBoolean("ControlSplitPanesEntered", true)) {
                    leftPaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            controlLeftPane();
                        }
                    });
                } else {
                    leftPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            controlLeftPane();
                        }
                    });
                }
            }

            if (splitPane != null && rightPane != null && rightPaneControl != null) {
                if (getUserConfigBoolean("ControlSplitPanesEntered", true)) {
                    rightPaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            controlRightPane();
                        }
                    });
                } else {
                    rightPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            controlRightPane();
                        }
                    });
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initControls() {

    }

    // This is called automatically after window is opened
    public void afterStageShown() {

        getMyStage();
    }

    public boolean recordStageSizeChange(int minSize) {
        return !isSettingValues
                && !myStage.isMaximized()
                && !myStage.isFullScreen()
                && !myStage.isIconified()
                && (myStage.getWidth() > minSize);
    }

    // This is called automatically after TOP scene is loaded.
    // Notice embedded scenes will NOT call this automatically.
    public void afterSceneLoaded() {
        try {
            getMyScene();
            getMyStage();

            final String prefix = "Interface_" + baseName;

            int minSize = 200;
            myStage.setMinWidth(minSize);
            myStage.setMinHeight(minSize);

            if (AppVariables.restoreStagesSize) {
                if (AppVariables.getUserConfigBoolean(prefix + "FullScreen", false)) {
                    myStage.setFullScreen(true);

                } else if (AppVariables.getUserConfigBoolean(prefix + "Maximized", false)) {
                    FxmlControl.setMaximized(myStage, true);

                } else {

                    int w = AppVariables.getUserConfigInt(prefix + "StageWidth", (int) thisPane.getPrefWidth());
                    int h = AppVariables.getUserConfigInt(prefix + "StageHeight", (int) thisPane.getPrefHeight());
                    int x = AppVariables.getUserConfigInt(prefix + "StageX", (int) myStage.getX());
                    int y = AppVariables.getUserConfigInt(prefix + "StageY", (int) myStage.getY());
                    if (w >= minSize && h >= minSize) {
                        myStage.setWidth(w);
                        myStage.setHeight(h);
                    }
                    myStage.setX(x);
                    myStage.setY(y);
                }

                fullscreenListener = new FullscreenListener(prefix);
                myStage.fullScreenProperty().addListener(fullscreenListener);
                maximizedListener = new MaximizedListener(prefix);
                myStage.maximizedProperty().addListener(maximizedListener);

                myScene.xProperty().addListener(
                        (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
                            if (recordStageSizeChange(minSize)) {
                                AppVariables.setUserConfigInt(prefix + "StageX", (int) myStage.getX());
                            }
                        });
                myScene.yProperty().addListener(
                        (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
                            if (recordStageSizeChange(minSize)) {
                                AppVariables.setUserConfigInt(prefix + "StageY", (int) myStage.getY());
                            }
                        });
                myScene.widthProperty().addListener(
                        (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
                            if (!isSettingValues && !myStage.isMaximized() && !myStage.isFullScreen() && !myStage.isIconified()
                            && (myStage.getWidth() > minSize)) {
                                AppVariables.setUserConfigInt(prefix + "StageWidth", (int) myStage.getWidth());
                            }
                        });
                myScene.heightProperty().addListener(
                        (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
                            if (!isSettingValues && !myStage.isMaximized() && !myStage.isFullScreen() && !myStage.isIconified()
                            && (myStage.getHeight() > minSize)) {
                                AppVariables.setUserConfigInt(prefix + "StageHeight", (int) myStage.getHeight());
                            }
                        });

            } else {
                myStage.sizeToScene();
                myStage.centerOnScreen();
            }

            Rectangle2D screen = FxmlControl.getScreen();
            if (myStage.getHeight() > screen.getHeight()) {
                myStage.setHeight(screen.getHeight());
            }
            if (myStage.getWidth() > screen.getWidth()) {
                myStage.setWidth(screen.getWidth());
            }
            if (myStage.getX() < 0) {
                myStage.setX(0);
            }
            if (myStage.getY() < 0) {
                myStage.setY(0);
            }

            initSplitPanes();
            refreshStyle();
            myStage.toFront();
            myStage.requestFocus();
            if (mainMenuController != null) {
                FxmlControl.mouseCenter(myStage);
            }
            toFront();

            if (selectFileButton != null) {
                selectFileButton.requestFocus();
            } else if (tipsView != null) {
                tipsView.requestFocus();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void refreshStyle() {
        if (getMyScene() == null) {
            return;
        }
        Parent root = myScene.getRoot();
        FxmlControl.refreshStyle(root);
    }

    public void refreshCurrentStyle() {
        if (getMyScene() == null) {
            return;
        }
        Parent root = myScene.getRoot();
        root.applyCss();
        root.layout();
    }

    public void toFront() {
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        getMyStage().toFront();
                        if (topCheck != null) {
                            topCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Top", true));
                            if (topCheck.isVisible()) {
                                getMyStage().setAlwaysOnTop(topCheck.isSelected());
                            }
                        }
                        timer = null;
                    });
                }
            }, 1000);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initSplitPanes() {
        try {
            if (splitPane == null || splitPane.getDividers().isEmpty()) {
                return;
            }
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowRightControl", true)) {
                hideRightPane();
            }
            setSplitDividerPositions();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setSplitDividerPositions() {
        try {
            if (isSettingValues || splitPane == null) {
                return;
            }
            int dividersSize = splitPane.getDividers().size();
            if (dividersSize < 1) {
                return;
            }
            isSettingValues = true;
            try {
                splitPane.getDividers().get(0).positionProperty().removeListener(leftDividerListener);
                leftDividerListener = null;
            } catch (Exception e) {
            }
            try {
                splitPane.getDividers().get(dividersSize - 1).positionProperty().removeListener(rightDividerListener);
                rightDividerListener = null;
            } catch (Exception e) {
            }
            if (splitPane.getItems().contains(leftPane)) {
                double defaultv = dividersSize == 1 ? 0.35 : 0.15;
                try {
                    String v = AppVariables.getUserConfigValue(baseName + "LeftPanePosition", defaultv + "");
                    splitPane.setDividerPosition(0, Double.parseDouble(v));
                } catch (Exception e) {
                    splitPane.setDividerPosition(0, defaultv);
                }
                leftDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues) {
                        AppVariables.setUserConfigValue(baseName + "LeftPanePosition", newValue.doubleValue() + "");
                    }
                };
                splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
            }
            if (splitPane.getItems().contains(rightPane)) {
                int index = splitPane.getDividers().size() - 1;
                double defaultv = index > 0 ? 0.85 : 0.65;
                try {
                    String v = AppVariables.getUserConfigValue(baseName + "RightPanePosition", defaultv + "");
                    splitPane.setDividerPosition(index, Double.parseDouble(v));
                } catch (Exception e) {
                    splitPane.setDividerPosition(index, defaultv);
                }
                rightDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues) {
                        AppVariables.setUserConfigValue(baseName + "RightPanePosition", newValue.doubleValue() + "");
                    }
                };
                splitPane.getDividers().get(index).positionProperty().addListener(rightDividerListener);
            }
            splitPane.applyCss();
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void controlLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()) {
            return;
        }
        if (splitPane.getItems().contains(leftPane)) {
            hideLeftPane();
        } else {
            showLeftPane();
        }
    }

    public void hideLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()
                || !splitPane.getItems().contains(leftPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(leftPane);
        isSettingValues = false;
        ControlStyle.setIcon(leftPaneControl, ControlStyle.getIcon("iconDoubleRight.png"));
        setSplitDividerPositions();
        AppVariables.setUserConfigValue(baseName + "ShowLeftControl", false);
    }

    public void showLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()
                || splitPane.getItems().contains(leftPane)) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().add(0, leftPane);
        isSettingValues = false;
        ControlStyle.setIcon(leftPaneControl, ControlStyle.getIcon("iconDoubleLeft.png"));
        setSplitDividerPositions();
        AppVariables.setUserConfigValue(baseName + "ShowLeftControl", true);
    }

    @FXML
    public void controlRightPane() {
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()) {
            return;
        }
        if (splitPane.getItems().contains(rightPane)) {
            hideRightPane();
        } else {
            showRightPane();
        }
    }

    public void hideRightPane() {
        if (splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()
                || !splitPane.getItems().contains(rightPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(rightPane);
        isSettingValues = false;
        ControlStyle.setIcon(rightPaneControl, ControlStyle.getIcon("iconDoubleLeft.png"));
        setSplitDividerPositions();
        AppVariables.setUserConfigValue(baseName + "ShowRightControl", false);
    }

    public void showRightPane() {
        if (splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()
                || splitPane.getItems().contains(rightPane)) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().add(rightPane);
        isSettingValues = false;
        ControlStyle.setIcon(leftPaneControl, ControlStyle.getIcon("iconDoubleRight.png"));
        setSplitDividerPositions();
        AppVariables.setUserConfigValue(baseName + "ShowRightControl", true);
    }

    public class FullscreenListener implements ChangeListener<Boolean> {

        private final String prefix;

        public FullscreenListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
            AppVariables.setUserConfigValue(prefix + "FullScreen", getMyStage().isFullScreen());
        }
    }

    public class MaximizedListener implements ChangeListener<Boolean> {

        private final String prefix;

        public MaximizedListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
            AppVariables.setUserConfigValue(prefix + "Maximized", getMyStage().isMaximized());
        }
    }

    public void checkSaveAsOption() {
        switch (saveAsOptionsBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                AppVariables.setUserConfigValue(SaveAsOptionsKey, SaveAsType.Load + "");
                saveAsType = SaveAsType.Load;
                break;
            case 1:
                AppVariables.setUserConfigValue(SaveAsOptionsKey, SaveAsType.Open + "");
                saveAsType = SaveAsType.Open;
                break;
            case 2:
                AppVariables.setUserConfigValue(SaveAsOptionsKey, SaveAsType.None + "");
                saveAsType = SaveAsType.None;
                break;
            default:
                break;
        }
    }

    public void checkSourcetPathInput() {
        try {
            final File file = new File(sourcePathInput.getText());
            if (!file.exists() || !file.isDirectory()) {
                sourcePathInput.setStyle(badStyle);
                return;
            }
            sourcePath = file;
            sourcePathInput.setStyle(null);
            recordFileOpened(file);
        } catch (Exception e) {
        }
    }

    public void checkSourceFileInput() {
        String v = sourceFileInput.getText();
        if (v == null || v.isEmpty()) {
            sourceFileInput.setStyle(badStyle);
            return;
        }
        final File file = new File(v);
        if (!file.exists()) {
            sourceFileInput.setStyle(badStyle);
            return;
        }
        sourceFileInput.setStyle(null);
        sourceFileChanged(file);
        if (parentController != null) {
            parentController.sourceFileChanged(file);
        }
        if (file.isDirectory()) {
            AppVariables.setUserConfigValue(sourcePathKey, file.getPath());
        } else {
            AppVariables.setUserConfigValue(sourcePathKey, file.getParent());
            if (targetPrefixInput != null) {
                targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
            }
        }
    }

    public void checkTargetPathInput() {
        try {
            final File file = new File(targetPathInput.getText());
            if (!file.exists() || !file.isDirectory()) {
                targetPathInput.setStyle(badStyle);
                return;
            }
            targetPath = file;
            targetPathInput.setStyle(null);
            AppVariables.setUserConfigValue(targetPathKey, file.getPath());
            recordFileWritten(file);
        } catch (Exception e) {
        }
    }

    public void checkTargetFileInput() {
        try {
            String input = targetFileInput.getText();
            targetFile = new File(input);
            targetFileInput.setStyle(null);
            AppVariables.setUserConfigValue(targetPathKey, targetFile.getParent());
        } catch (Exception e) {
            targetFile = null;
            targetFileInput.setStyle(badStyle);
        }
    }

    public void checkTargetExistType() {
        if (isSettingValues) {
            return;
        }
        if (targetAppendInput != null) {
            targetAppendInput.setStyle(null);
        }
        RadioButton selected = (RadioButton) targetExistGroup.getSelectedToggle();
        if (selected.equals(targetReplaceRadio)) {
            targetExistType = TargetExistType.Replace;

        } else if (selected.equals(targetRenameRadio)) {
            targetExistType = TargetExistType.Rename;
            if (targetAppendInput != null) {
                if (targetAppendInput.getText() == null || targetAppendInput.getText().trim().isEmpty()) {
                    targetAppendInput.setStyle(badStyle);
                } else {
                    setUserConfigValue("TargetExistAppend", targetAppendInput.getText().trim());
                }
            }

        } else if (selected.equals(targetSkipRadio)) {
            targetExistType = TargetExistType.Skip;
        }
        setUserConfigValue("TargetExistType", selected.getText());
    }

    // Shortcuts like Ctrl-c/v/x/z/y/a may be for text editing
    public void keyEventsHandler(KeyEvent event) {
//        logger.debug(this.getClass().getName() + " " + event.isControlDown() + " text:" + event.getText()
//                + " code:" + event.getCode());
        currentKeyEvent = event;
//        logger.debug(currentKeyEvent.getSource().getClass());
        keyEventsHandlerDo(event);
    }

    public void keyEventsHandlerDo(KeyEvent event) {
        if (event.isControlDown()) {
            controlHandler(event);

        } else if (event.isAltDown()) {
            altHandler(event);

        } else if (event.getCode() != null) {
            keyHandler(event);

        }
    }

    public void controlHandler(KeyEvent event) {
        if (!event.isControlDown() || event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case E:
                if (startButton != null && !startButton.isDisabled()) {
                    startAction();
                } else if (okButton != null && !okButton.isDisabled()) {
                    okAction();
                }
                return;
            case N:
                if (createButton != null && !createButton.isDisabled()) {
                    createAction();
                } else if (addButton != null && !addButton.isDisabled()) {
                    addAction(null);
                }
                return;
            case C:
                if (copyButton != null && !copyButton.isDisabled()) {
                    copyAction();
                }
                return;
            case V:
                if (pasteButton != null && !pasteButton.isDisabled()) {
                    pasteAction();
                }
                return;
            case S:
                if (saveButton != null && !saveButton.isDisabled()) {
                    saveAction();
                }
                return;
            case I:
                if (infoButton != null && !infoButton.isDisabled()) {
                    infoAction();
                }
                return;
            case D:
                if (deleteButton != null && !deleteButton.isDisabled()) {
                    deleteAction();
                }
                return;
            case A:
                if (allButton != null && !allButton.isDisabled()) {
                    allAction();
                } else if (selectAllButton != null && !selectAllButton.isDisabled()) {
                    selectAllAction();
                }
                return;
            case O:
                if (selectNoneButton != null && !selectNoneButton.isDisabled()) {
                    selectNoneAction();
                }
                return;
            case X:
                if (cropButton != null && !cropButton.isDisabled()) {
                    cropAction();
                }
                return;
            case G:
                if (clearButton != null && !clearButton.isDisabled()) {
                    clearAction();
                }
                return;
            case R:
                if (recoverButton != null && !recoverButton.isDisabled()) {
                    recoverAction();
                }
                return;
            case Z:
                if (undoButton != null && !undoButton.isDisabled()) {
                    undoAction();
                }
                return;
            case Y:
                if (redoButton != null && !redoButton.isDisabled()) {
                    redoAction();
                }
                return;
            case P:
                if (popButton != null && !popButton.isDisabled()) {
                    popAction();
                }
                return;
            case W:
                if (cancelButton != null && !cancelButton.isDisabled()) {
                    cancelAction();
                } else if (withdrawButton != null && !withdrawButton.isDisabled()) {
                    withdrawAction();
                }
                return;
            case MINUS:
                setSceneFontSize(AppVariables.sceneFontSize - 1);
                return;
            case EQUALS:
                setSceneFontSize(AppVariables.sceneFontSize + 1);
        }

    }

    public void altHandler(KeyEvent event) {
        if (!event.isAltDown() || event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case HOME:
                if (firstButton != null && !firstButton.isDisabled()) {
                    firstAction();
                }
                return;
            case END:
                if (lastButton != null && !lastButton.isDisabled()) {
                    lastAction();
                }
                return;

            case PAGE_UP:
                if (previousButton != null && !previousButton.isDisabled()) {
                    previousAction();
                }
                return;
            case PAGE_DOWN:
                if (nextButton != null && !nextButton.isDisabled()) {
                    nextAction();
                }
                return;
            case E:
                if (startButton != null && !startButton.isDisabled()) {
                    startAction();
                } else if (okButton != null && !okButton.isDisabled()) {
                    okAction();
                }
                return;
            case N:
                if (createButton != null && !createButton.isDisabled()) {
                    createAction();
                } else if (addButton != null && !addButton.isDisabled()) {
                    addAction(null);
                }
                return;
            case C:
                if (copyButton != null && !copyButton.isDisabled()) {
                    copyAction();
                }
                return;
            case V:
                if (pasteButton != null && !pasteButton.isDisabled()) {
                    pasteAction();
                }
                return;
            case S:
                if (saveButton != null && !saveButton.isDisabled()) {
                    saveAction();
                }
                return;
            case D:
                if (deleteButton != null && !deleteButton.isDisabled()) {
                    deleteAction();
                }
                return;
            case A:
                if (allButton != null && !allButton.isDisabled()) {
                    allAction();
                } else if (selectAllButton != null && !selectAllButton.isDisabled()) {
                    selectAllAction();
                }
                return;
            case O:
                if (selectNoneButton != null && !selectNoneButton.isDisabled()) {
                    selectNoneAction();
                }
                return;
            case X:
                if (cropButton != null && !cropButton.isDisabled()) {
                    cropAction();
                }
                return;
            case G:
                if (clearButton != null && !clearButton.isDisabled()) {
                    clearAction();
                }
                return;
            case R:
                if (recoverButton != null && !recoverButton.isDisabled()) {
                    recoverAction();
                }
                break;
            case Z:
                if (undoButton != null && !undoButton.isDisabled()) {
                    undoAction();
                }
                return;
            case Y:
                if (redoButton != null && !redoButton.isDisabled()) {
                    redoAction();
                }
                break;
            case P:
                if (popButton != null && !popButton.isDisabled()) {
                    popAction();
                }
                return;
            case W:
                if (cancelButton != null && !cancelButton.isDisabled()) {
                    cancelAction();
                } else if (withdrawButton != null && !withdrawButton.isDisabled()) {
                    withdrawAction();
                }
                return;
            case MINUS:
                setSceneFontSize(AppVariables.sceneFontSize - 1);
                break;
            case EQUALS:
                setSceneFontSize(AppVariables.sceneFontSize + 1);
        }

    }

    public void keyHandler(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == null) {
            return;
        }
        switch (code) {
            case DELETE:
                if (deleteButton != null && !deleteButton.isDisabled()) {
                    deleteAction();
                }
                return;
            case PAGE_UP:
                if (previousButton != null && !previousButton.isDisabled()) {
                    previousAction();
                }
                return;
            case PAGE_DOWN:
                if (nextButton != null && !nextButton.isDisabled()) {
                    nextAction();
                }
                return;
            case F1:
                if (startButton != null && !startButton.isDisabled()) {
                    startAction();
                } else if (okButton != null && !okButton.isDisabled()) {
                    okAction();
                } else if (setButton != null && !setButton.isDisabled()) {
                    setAction();
                } else if (playButton != null && !playButton.isDisabled()) {
                    playAction();
                }
                return;
            case F2:
                if (saveButton != null && !saveButton.isDisabled()) {
                    saveAction();
                }
                return;
            case F3:
                if (recoverButton != null && !recoverButton.isDisabled()) {
                    recoverAction();
                }
                return;
            case F4:
                if (leftPaneControl != null && leftPaneControl.isVisible()) {
                    controlLeftPane();
                }
                return;
            case F5:
                if (rightPaneControl != null && rightPaneControl.isVisible()) {
                    controlRightPane();
                }
                return;
            case F6:
                closePopup(event);
                return;
            case F9:
                closeStage();
                return;
            case F10:
                refresh();
                return;
            case F11:
                if (saveAsButton != null && !saveAsButton.isDisabled()) {
                    saveAsAction();
                }
                return;
            case ESCAPE:
                if (cancelButton != null && !cancelButton.isDisabled()) {
                    cancelAction();
                } else if (withdrawButton != null && !withdrawButton.isDisabled()) {
                    withdrawAction();
                }
//                else if (stopButton != null && !stopButton.isDisabled()) {
//                    stopAction();
//                }
        }

    }

    public void setInterfaceStyle(Scene scene, String style) {
        try {
            if (scene != null && style != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void setInterfaceStyle(String style) {
        try {
            if (thisPane != null && style != null) {
                thisPane.getStylesheets().clear();
                if (!CommonValues.MyBoxStyle.equals(style)) {
                    thisPane.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
                }
                thisPane.getStylesheets().add(BaseController.class.getResource(CommonValues.MyBoxStyle).toExternalForm());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public boolean setSceneFontSize(int size) {
        if (thisPane == null) {
            return false;
        }
        AppVariables.setSceneFontSize(size);
        thisPane.setStyle("-fx-font-size: " + size + "px;");
        if (parentController != null) {
            parentController.setSceneFontSize(size);
        }
        return true;
    }

    public boolean setIconSize(int size) {
        if (thisPane == null) {
            return false;
        }
        AppVariables.setIconSize(size);
        if (parentController != null) {
            parentController.setIconSize(size);
        }
        refreshBase();
        return true;
    }

    public BaseController refresh() {
        return refreshBase();
    }

    public BaseController refreshBase() {
        try {
            if (getMyStage() == null || myFxml == null) {
                return null;
            }
            String title = myStage.getTitle();
            BaseController c, p = parentController;
            c = loadScene(myFxml);
            if (c == null) {
                return null;
            }
            if (p != null) {
                c.parentFxml = p.myFxml;
                c.parentController = p;
                p.refresh();
            }
            if (c.getMyStage() != null) {
                c.getMyStage().toFront();
                c.getMyStage().setTitle(title);
            }
            return c;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public List<MenuItem> getRecentMenu() {
        List<MenuItem> menus = new ArrayList();
        List<VisitHistory> his = VisitHistoryTools.getRecentMenu();
        if (his == null || his.isEmpty()) {
            return menus;
        }
        List<String> valid = new ArrayList();
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            final String fxml = h.getDataMore();
            if (valid.contains(fxml)) {
                continue;
            }
            valid.add(fxml);
            MenuItem menu = new MenuItem(message(fname));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    loadScene(fxml);
                }
            });
            menus.add(menu);
        }
        return menus;

    }

    @FXML
    public void selectSourceFile() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            final FileChooser fileChooser = new FileChooser();
            File path = AppVariables.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null || !file.exists()) {
                return;
            }

            selectSourceFileDo(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void selectSourceFile(File file) {
        if (file == null || !file.exists()) {
            selectSourceFile();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        selectSourceFileDo(file);
    }

    public void selectSourceFileDo(File file) {
        recordFileOpened(file);
        if (sourceFileInput != null) {
            sourceFileInput.setText(file.getAbsolutePath());
        } else {
            sourceFileChanged(file);
        }
    }

    public void sourceFileChanged(final File file) {
        sourceFile = file;

    }

    public void recordFileOpened(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileOpened(final File file) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            String path = file.getPath();
            AppVariables.setUserConfigValue(sourcePathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(sourcePathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.readPath(SourcePathType, path);
            VisitHistoryTools.readFile(SourceFileType, fname);
        }

    }

    public void recordFileOpened(final File file, int pathType, int fileType) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            String path = file.getPath();
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.readPath(pathType, path);
        } else if (file.isFile()) {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.readPath(pathType, path);
            VisitHistoryTools.readFile(fileType, fname);
        }

    }

    public void recordFileWritten(String file) {
        recordFileWritten(new File(file));
    }

    public void recordFileWritten(final File file) {
        recordFileWritten(file, targetPathKey, TargetPathType, TargetFileType);
    }

    public void recordFileWritten(final File file, int fileType) {
        recordFileWritten(file, VisitHistoryTools.getPathKey(fileType), fileType, fileType);
    }

    public void recordFileWritten(final File file,
            String targetPathKey, int TargetPathType, int TargetFileType) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            String path = file.getPath();
            AppVariables.setUserConfigValue(targetPathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.writePath(TargetPathType, path);
        } else if (file.isFile()) {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(targetPathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.writePath(TargetPathType, path);
            VisitHistoryTools.writeFile(TargetFileType, fname);
        }
    }

    public void recordFileAdded(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileAdded(final File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            String path = file.getPath();
            AppVariables.setUserConfigValue(sourcePathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(sourcePathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistoryTools.readPath(SourcePathType, path);
            VisitHistoryTools.readFile(AddFileType, fname);
        }

    }

    @FXML
    public void selectTargetPath() {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            selectTargetPath(directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectTargetPath(File directory) {
        targetPathInput.setText(directory.getPath());

        recordFileWritten(directory);
        targetPathChanged();
    }

    public void targetPathChanged() {

    }

    @FXML
    public void selectTargetFile() {
        File path = AppVariables.getUserConfigPath(targetPathKey);
        selectTargetFileFromPath(path);
    }

    public void selectTargetFileFromPath(File path) {
        try {
            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(path, name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void selectTargetFile(File file) {
        try {
            if (file == null) {
                return;
            }
            targetFile = file;
            recordFileWritten(file);

            if (targetFileInput != null) {
                targetFileInput.setText(targetFile.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void selectSourcePath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(sourcePathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            selectSourcePath(directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectSourcePath(File directory) {
        if (sourcePathInput != null) {
            sourcePathInput.setText(directory.getPath());
        }
        recordFileWritten(directory);
    }

    public void openTarget(ActionEvent event) {

    }

    @FXML
    public void addFilesAction() {

    }

    public void addFile(File file) {

    }

    @FXML
    public void insertFilesAction() {

    }

    public void insertFile(File file) {

    }

    @FXML
    public void addDirectoryAction() {

    }

    public void addDirectory(File directory) {

    }

    @FXML
    public void insertDirectoryAction() {

    }

    public void insertDirectory(File directory) {

    }

    @FXML
    public void saveAsAction() {

    }

    @FXML
    public void popSourceFile(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {

            @Override
            public void handleSelect() {
                selectSourceFile();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSourceFile();
                    return;
                }
                selectSourceFile(file);
            }

        }.pop();
    }

    @FXML
    public void popFileAdd(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentAddFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                addFilesAction();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSourceFile();
                    return;
                }
                addFile(file);
            }

        }.pop();
    }

    @FXML
    public void popFileInsert(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentAddFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                insertFilesAction();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSourceFile();
                    return;
                }
                insertFile(file);
            }

        }.pop();
    }

    @FXML
    public void popDirectoryAdd(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                addDirectoryAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                addDirectory(file);
            }

        }.pop();
    }

    @FXML
    public void popDirectoryInsert(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
                if (controller.getAddPathType() <= 0) {
                    controller.AddPathType = controller.SourcePathType;
                }
                return VisitHistoryTools.getRecentPath(controller.getAddPathType(), pathNumber);
            }

            @Override
            public void handleSelect() {
                insertDirectoryAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                insertDirectory(file);
            }

        }.pop();
    }

    @FXML
    public void popSourcePath(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePaths();
            }

            @Override
            public void handleSelect() {
                selectSourcePath();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectSourcePath(file);
            }

        }.pop();
    }

    @FXML
    public void popTargetPath(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                selectTargetPath();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectTargetPath(file);
            }

        }.pop();
    }

    @FXML
    public void popTargetFile(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                selectTargetFile();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                selectTargetFileFromPath(file);
            }

        }.pop();
    }

    @FXML
    public void popSaveAs(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                saveAsAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    public File makeTargetFile(File sourceFile, File targetPath) {
        if (sourceFile.isFile()) {
            return makeTargetFile(sourceFile.getName(), targetPath);
        } else {
            return makeTargetFile(sourceFile.getName(), "", targetPath);
        }
    }

    public File makeTargetFile(String sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile);
            String nameSuffix = FileTools.getFileSuffix(sourceFile);
            if (targetFileType != null) {
                nameSuffix = "." + targetFileType;
            } else if (!nameSuffix.isEmpty()) {
                nameSuffix = "." + nameSuffix;
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

    public File makeTargetFile(String namePrefix, String nameSuffix,
            File targetPath) {
        try {
            String targetPrefix = targetPath.getAbsolutePath() + File.separator + namePrefix;
            File target = new File(targetPrefix + nameSuffix);
            if (target.exists()) {
                if (targetExistType == TargetExistType.Skip) {
                    target = null;
                } else if (targetExistType == TargetExistType.Rename) {
                    if (targetAppendInput != null) {
                        targetNameAppend = targetAppendInput.getText().trim();
                    }
                    if (targetNameAppend == null || targetNameAppend.isEmpty()) {
                        targetNameAppend = "-m";
                    }
                    while (true) {
                        targetPrefix = targetPrefix + targetNameAppend;
                        target = new File(targetPrefix + nameSuffix);
                        if (!target.exists()) {
                            break;
                        }
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    @FXML
    public void link(ActionEvent event) {
        try {
            Hyperlink link = (Hyperlink) event.getSource();
            link(link.getText());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void link(String urlString) {
        try {
            URL url = new URL(urlString);
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
            browseURI(uri);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void regexHelp() {
        try {
            String link;
            switch (AppVariables.getLanguage()) {
                case "zh":
                    link = "https://baike.baidu.com/item/%E6%AD%A3%E5%88%99%E8%A1%A8%E8%BE%BE%E5%BC%8F/1700215";
                    break;
                default:
                    link = "https://en.wikipedia.org/wiki/Regular_expression";
            }
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void derbyHelp() {
        try {
            browseURI(new URI("http://db.apache.org/derby/docs/10.15/ref/index.html"));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void okAction() {

    }

    @FXML
    public void startAction() {

    }

    @FXML
    public void playAction() {

    }

    @FXML
    public void stopAction() {

    }

    @FXML
    public void createAction() {

    }

    @FXML
    public void addAction(ActionEvent event) {

    }

    @FXML
    public void copyAction() {

    }

    @FXML
    public void pasteAction() {

    }

    @FXML
    public void saveAction() {

    }

    @FXML
    public void deleteAction() {

    }

    @FXML
    public void cropAction() {

    }

    @FXML
    public void recoverAction() {

    }

    @FXML
    public void redoAction() {

    }

    @FXML
    public void undoAction() {

    }

    @FXML
    public void allAction() {

    }

    @FXML
    public void clearAction() {

    }

    @FXML
    public void cancelAction() {

    }

    @FXML
    public void closeAction() {
        closeStage();
    }

    @FXML
    public void infoAction() {

    }

    @FXML
    public void setAction() {

    }

    @FXML
    public void selectAllAction() {

    }

    @FXML
    public void selectNoneAction() {

    }

    @FXML
    public void nextAction() {

    }

    @FXML
    public void previousAction() {

    }

    @FXML
    public void firstAction() {

    }

    @FXML
    public void lastAction() {

    }

    @FXML
    public void popAction() {

    }

    @FXML
    public void withdrawAction() {

    }

    @FXML
    public void closePopup(KeyEvent event) {
        if (popMenu != null) {
            popMenu.hide();
        }
        if (popup != null) {
            popup.hide();
        }
    }

    @FXML
    public void mybox(ActionEvent event) {
        openStage(CommonValues.MyboxFxml);
    }

    public void clearUserSettings() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setHeaderText(AppVariables.message("ClearPersonalSettings"));
        alert.setContentText(AppVariables.message("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonSure) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        new TableUserConf().clear();
                        AppVariables.initAppVaribles();
                        return true;
                    } catch (Exception e) {
                        logger.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    refresh();
                    popSuccessful();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void cleanAppPath() {
        try {
            File userPath = new File(MyboxDataPath);
            if (userPath.exists()) {
                File[] files = userPath.listFiles();
                if (files == null) {
                    return;
                }
                for (File f : files) {
                    if (f.isFile() && !f.equals(AppVariables.MyboxConfigFile)) {
                        f.delete();
                    } else if (f.isDirectory() && !AppVariables.MyBoxReservePaths.contains(f)) {
                        FileTools.deleteDir(f);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void view(File file) {
        FxmlStage.openTarget(null, file.getAbsolutePath());
    }

    public void view(String file) {
        FxmlStage.openTarget(null, file);
    }

    public boolean browseURI(URI uri) {
        return FxmlStage.browseURI(getMyStage(), uri);
    }

    @FXML
    public void openUserPath(ActionEvent event) {
        try {
            browseURI(new File(MyboxDataPath).toURI());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public BaseController loadScene(String newFxml) {
        try {
            if (!leavingScene()) {
                return null;
            }
            return FxmlStage.openScene(getMyStage(), newFxml);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public BaseController openStage(String newFxml) {
        return openStage(newFxml, false);
    }

    public BaseController openStage(String newFxml, boolean isOwned) {
        return FxmlStage.openStage(getMyStage(), newFxml, isOwned);
    }

    public boolean closeStage() {
        if (leavingScene()) {
            FxmlStage.closeStage(getMyStage());
            return true;
        } else {
            return false;
        }
    }

    public boolean leavingScene() {
        try {
            if (!checkBeforeNextAction()) {
                return false;
            }

            if (mainMenuController != null) {
                mainMenuController.stopMemoryMonitorTimer();
                mainMenuController.stopCpuMonitorTimer();
            }

            if (maximizedListener != null) {
                getMyStage().maximizedProperty().removeListener(maximizedListener);
            }
            if (fullscreenListener != null) {
                getMyStage().fullScreenProperty().removeListener(fullscreenListener);
            }

            hidePopup();
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null && task.isRunning()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(AppVariables.message("TaskRunning"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                alert.getButtonTypes().setAll(buttonSure, buttonCancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonSure && task != null) {
                    task.cancel();
                    task = null;
                } else {
                    return false;
                }
            }

            if (backgroundTask != null && backgroundTask.isRunning()) {
                backgroundTask.cancel();
                backgroundTask = null;
            }

//            System.gc();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public boolean checkBeforeNextAction() {
        return true;
    }

    public File chooseSaveFile(File defaultPath, String defaultName,
            List<FileChooser.ExtensionFilter> filters) {
        return chooseSaveFile(null, defaultPath, defaultName, filters, true);
    }

    public File chooseSaveFile(File defaultPath, String defaultName,
            List<FileChooser.ExtensionFilter> filters, boolean mustHaveExtension) {
        return chooseSaveFile(null, defaultPath, defaultName, filters, mustHaveExtension);
    }

    public File chooseSaveFile(String title, File defaultPath,
            String defaultName,
            List<FileChooser.ExtensionFilter> filters, boolean mustHaveExtension) {
        try {
            FileChooser fileChooser = new FileChooser();
            if (title != null) {
                fileChooser.setTitle(title);
            }
            if (defaultPath != null && defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            String name = defaultName;
            String suffix = null;
            if (filters != null) {
                suffix = FileTools.getFileSuffix(filters.get(0).getExtensions().get(0));
                fileChooser.getExtensionFilters().addAll(filters);
            }
            if (suffix != null) {
                if (name == null) {
                    name = "." + suffix;
                } else {
                    if (FileTools.getFileSuffix(name).isEmpty()) {
                        name += "." + suffix;
                    }
                }
            }
            if (name != null) {
                name = name.replaceAll("\\\"|\n|:", "");
                fileChooser.setInitialFileName(name);
            }

            File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return null;
            }
            // https://stackoverflow.com/questions/20637865/javafx-2-2-get-selected-file-extension
            // This is a pretty annoying thing in JavaFX - they will automatically append the extension on Windows, but not on Linux or Mac.
            if (mustHaveExtension && FileTools.getFileSuffix(file.getName()).isEmpty()) {
                if (suffix == null) {
                    popError(message("NoFileExtension"), 3000);
                    return null;
                }
                file = new File(file.getAbsolutePath() + "." + suffix);
            }
            return file;

        } catch (Exception e) {
            return null;
        }

    }

    public void alertError(String information) {
        FxmlStage.alertError(getMyStage(), information);
    }

    public void alertWarning(String information) {
        FxmlStage.alertError(getMyStage(), information);
    }

    public void alertInformation(String information) {
        FxmlStage.alertInformation(getMyStage(), information);
    }

    public Popup getPopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        popup = new Popup();
        popup.setAutoHide(true);
        return popup;
    }

    public void popText(String text, int delay, String color) {
        popText(text, delay, color, "1.1em", null);
    }

    public void popText(String text, int delay, String color, String size,
            Region attach) {
        try {
            if (popup != null) {
                popup.hide();
            }
            popup = getPopup();
            popup.setAutoFix(true);
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:black;"
                    + " -fx-text-fill: " + color + ";"
                    + " -fx-font-size: " + size + ";"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.setAutoFix(true);
            popup.getContent().add(popupLabel);

            if (delay > 0) {
                if (popupTimer != null) {
                    popupTimer.cancel();
                }
                popupTimer = getPopupTimer();
                popupTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            hidePopup();
                        });
                    }
                }, delay);
            }

            if (attach != null) {
                FxmlControl.locateUp(attach, popup);
            } else {
                popup.show(getMyStage());
            }
        } catch (Exception e) {

        }
    }

    public void popSuccessful() {
        popBigInformation(message("Successful"));
    }

    public void popFailed() {
        popError(message("Failed"));
    }

    public void popInsertSuccessful() {
        popBigInformation(message("InsertSuccessfully"));
    }

    public void popUpdateSuccessful() {
        popBigInformation(message("UpdateSuccessfully"));
    }

    public void popInformation(String text) {
        popInformation(text, AppVariables.getCommentsDelay());
    }

    public void popBigInformation(String text) {
        popInformation(text, "1.5em");
    }

    public void popInformation(String text, String size) {
        popText(text, AppVariables.getCommentsDelay(), "white", size, bottomLabel);
    }

    public void popInformation(String text, int delay) {
        popText(text, delay, "white");
    }

    public void popError(String text) {
        popError(text, AppVariables.getCommentsDelay());
    }

    public void popError(String text, int delay) {
        popText(text, delay, "red", "1.5em", null);
    }

    public void popWarn(String text) {
        popError(text, AppVariables.getCommentsDelay());
    }

    public void popWarn(String text, int delay) {
        popText(text, delay, "orange", "1.5em", null);
    }

    public void hidePopup() {
        if (popup != null) {
            popup.hide();
        }
        if (popupTimer != null) {
            popupTimer.cancel();
        }
        popup = null;
        popupTimer = null;
    }

    public Stage getMyStage() {
        if (myStage == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
                if (myScene != null) {
                    myStage = (Stage) myScene.getWindow();
                }
            }
        }
        return myStage;
    }

    public LoadingController openHandlingStage(Modality block) {
        return openHandlingStage(block, null);
    }

    public LoadingController openHandlingStage(Modality block, String info) {
        try {
            final LoadingController controller
                    = FxmlStage.openLoadingStage(getMyStage(), block, info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public LoadingController openHandlingStage(final Task<?> task,
            Modality block) {
        return openHandlingStage(task, block, null);
    }

    public LoadingController openHandlingStage(final Task<?> task,
            Modality block, String info) {
        try {
            final LoadingController controller
                    = FxmlStage.openLoadingStage(getMyStage(), block, task, info);
            controller.parentController = myController;

            task.setOnSucceeded((WorkerStateEvent event) -> {
                controller.closeStage();
            });
            task.setOnCancelled((WorkerStateEvent event) -> {
                popInformation(AppVariables.message("Canceled"));
                controller.closeStage();
            });
            task.setOnFailed((WorkerStateEvent event) -> {
                popError(AppVariables.message("Error"));
                controller.closeStage();
            });
            return controller;

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public void taskCanceled(Task task) {

    }

    public String getBaseTitle() {
        if (baseTitle == null && myStage != null) {
            baseTitle = myStage.getTitle();
            if (baseTitle == null) {
                baseTitle = AppVariables.message("AppTitle");
            }
        }
        return baseTitle;
    }

    public Timer getPopupTimer() {
        if (popupTimer != null) {
            popupTimer.cancel();

        }
        popupTimer = new Timer();
        return popupTimer;
    }

    public Scene getMyScene() {
        if (myScene == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
            } else if (myStage != null) {
                myScene = myStage.getScene();
            }
        }
        return myScene;
    }

    public void multipleFilesGenerated(final List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            String path = new File(fileNames.get(0)).getParent();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(getMyStage().getTitle());
            String info = MessageFormat.format(AppVariables.message("GeneratedFilesResult"),
                    fileNames.size(), "\"" + path + "\"");
            int num = fileNames.size();
            if (num > 10) {
                num = 10;
            }
            for (int i = 0; i < num; ++i) {
                info += "\n    " + fileNames.get(i);
            }
            if (fileNames.size() > num) {
                info += "\n    ......";
            }
            alert.setContentText(info);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonOpen = new ButtonType(AppVariables.message("OpenTargetPath"));
            ButtonType buttonBrowse = new ButtonType(AppVariables.message("Browse"));
            ButtonType buttonBrowseNew = new ButtonType(AppVariables.message("BrowseInNew"));
            ButtonType buttonClose = new ButtonType(AppVariables.message("Close"));
            alert.getButtonTypes().setAll(buttonBrowseNew, buttonBrowse, buttonOpen, buttonClose);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonOpen) {
                browseURI(new File(path).toURI());
                recordFileOpened(path);
            } else if (result.get() == buttonBrowse) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(getMyStage());
                if (controller != null && sourceFile != null) {
                    controller.loadFiles(fileNames);
                }
            } else if (result.get() == buttonBrowseNew) {
                final ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
                if (controller != null && sourceFile != null) {
                    controller.loadFiles(fileNames);
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void dataChanged() {

    }

    // pick coordinate from outside
    public void setCoordinate(double longitude, double latitude) {
    }

    // pick GeographyCode from outside
    public void setGeographyCode(GeographyCode code) {
    }

    public void restoreCheckingSSL() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureRestoreCheckingSSL"));
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonSure) {
            return;
        }
        NetworkTools.myBoxSSL();
        popSuccessful();
    }

    @FXML
    public void myboxInternetDataPath() {
        link(CommonValues.MyBoxInternetDataPath);
    }

    /*
        Task
     */
    public class SingletonTask<Void> extends BaseTask<Void> {

        @Override
        protected void whenSucceeded() {
            popSuccessful();
        }

        @Override
        protected void whenFailed() {
            if (error != null) {
                popError(AppVariables.message(error));
            } else {
                popFailed();
            }
        }

        @Override
        protected void taskQuit() {
            endTime = new Date();
            task = null;    // Notice: This must be done in each task!! Replace as real task name!
        }

    };

    /*
        get/set
     */
    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
    }

    public void setMyScene(Scene myScene) {
        this.myScene = myScene;
    }

    public Pane getThisPane() {
        return thisPane;
    }

    public String getLastPathKey() {
        return LastPathKey;
    }

    public String getTargetPathKey() {
        return targetPathKey;
    }

    public String getSourcePathKey() {
        return sourcePathKey;
    }

    public String getDefaultPathKey() {
        return defaultPathKey;
    }

    public int getSourceFileType() {
        return SourceFileType;
    }

    public int getSourcePathType() {
        return SourcePathType;
    }

    public int getTargetFileType() {
        return TargetFileType;
    }

    public int getTargetPathType() {
        return TargetPathType;
    }

    public int getAddFileType() {
        return AddFileType;
    }

    public int getAddPathType() {
        return AddPathType;
    }

    public void setLoadFxml(String loadFxml) {
        this.loadFxml = loadFxml;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public BaseController getParentController() {
        return parentController;
    }

    public void setParentController(BaseController parentController) {
        this.parentController = parentController;
    }

    public MainMenuController getMainMenuController() {
        return mainMenuController;
    }

    public ContextMenu getPopMenu() {
        return popMenu;
    }

    public void setPopMenu(ContextMenu popMenu) {
        this.popMenu = popMenu;
    }

    public Task<Void> getTask() {
        return task;
    }

    public void setTask(Task<Void> task) {
        this.task = task;
    }

}
