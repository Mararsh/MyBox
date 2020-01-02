package mara.mybox.controller;

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
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import mara.mybox.data.BaseTask;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.VisitHistory.FileType;
import mara.mybox.db.DerbyBase;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
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
            operationType;
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

    protected ColorPaletteController paletteController;
    protected SimpleBooleanProperty isPickingColor;

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
    protected TextField sourceFileInput, sourcePathInput,
            targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    protected OperationController operationBarController;
    @FXML
    protected ToggleButton moreButton;
    @FXML
    protected Button allButton, clearButton, selectFileButton, createButton, copyButton, pasteButton, cancelButton,
            deleteButton, saveButton, infoButton, metaButton, selectAllButton, setButton,
            okButton, startButton, firstButton, lastButton, previousButton, nextButton, goButton, previewButton,
            cropButton, saveAsButton, recoverButton, renameButton, tipsButton, viewButton, popButton, refButton,
            undoButton, redoButton, transparentButton, whiteButton, blackButton, playButton, stopButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected Label bottomLabel, tipsLabel;
    @FXML
    protected ImageView tipsView, linksView;
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
    protected TextField targetAppendInput;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected ScrollPane leftPane, rightPane;
    @FXML
    protected ImageView leftPaneControl, rightPaneControl;

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
            initControls();
            initializeNext();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initValues() {
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
            thisPane.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    keyEventsHandler(event);
                }
            });
        }

        isPickingColor = new SimpleBooleanProperty();

    }

    public void initControls() {
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
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkSourceFileInput();
                    }
                });
            }

            if (sourcePathInput != null) {
                sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable,
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
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkTargetFileInput();
                    }
                });
            }

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
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

            if (saveAsOptionsBox != null) {
                List<String> optionsList = Arrays.asList(message("LoadAfterSaveAs"),
                        message("OpenAfterSaveAs"), message("JustSaveAs"));
                saveAsOptionsBox.getItems().addAll(optionsList);
                saveAsOptionsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
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

            if (moreButton != null) {
                moreButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        moreAction();
                    }
                });
            }

            if (topCheck != null) {
                topCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
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
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
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
                        public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                            checkTargetExistType();
                        }
                    });
                    targetAppendInput.setText(getUserConfigValue("TargetExistAppend", "_m"));
                }
                isSettingValues = false;
                checkTargetExistType();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void afterStageShown() {

        getMyStage();
    }

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
                    if (w >= minSize && h >= minSize) {
                        myStage.setWidth(w);
                        myStage.setHeight(h);
                    }
                    myStage.centerOnScreen();
                }

                fullscreenListener = new FullscreenListener(prefix);
                myStage.fullScreenProperty().addListener(fullscreenListener);
                maximizedListener = new MaximizedListener(prefix);
                myStage.maximizedProperty().addListener(maximizedListener);

                myScene.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        if (!myStage.isMaximized() && !myStage.isFullScreen() && !myStage.isIconified()
                                && myStage.getWidth() > minSize) {
                            AppVariables.setUserConfigInt(prefix + "StageWidth", (int) myStage.getWidth());
                        }
                    }
                });
                myScene.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        if (!myStage.isMaximized() && !myStage.isFullScreen() && !myStage.isIconified()
                                && myStage.getHeight() > minSize) {
                            AppVariables.setUserConfigInt(prefix + "StageHeight", (int) myStage.getHeight());
                        }
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

            toFront();

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

    public void toFront() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        getMyStage().toFront();
                        if (topCheck != null) {
                            topCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Top", true));
                            if (topCheck.isVisible()) {
                                myStage.setAlwaysOnTop(topCheck.isSelected());
                            }
                        }
                        timer = null;
                    }
                });
            }
        }, 1000);
    }

    public void initSplitPanes() {
        try {
            if (splitPane == null) {
                return;
            }
            if (leftPane != null && rightPane == null) {
                try {
                    String lv = AppVariables.getUserConfigValue(baseName + "LeftPanePosition", "0.35");
                    splitPane.setDividerPositions(Double.parseDouble(lv));
                } catch (Exception e) {
                    splitPane.setDividerPositions(0.35);
                }
                leftDividerListener = new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (!isSettingValues) {
                            if (splitPane.getItems().contains(leftPane)) {
                                AppVariables.setUserConfigValue(baseName + "LeftPanePosition", newValue.doubleValue() + "");
                            }
                        }
                    }
                };
                splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);

            } else if (leftPane == null && rightPane != null) {
                try {
                    String rv = AppVariables.getUserConfigValue(baseName + "RightPanePosition", "0.65");
                    splitPane.setDividerPositions(Double.parseDouble(rv));
                } catch (Exception e) {
                    splitPane.setDividerPositions(0.65);
                }
                rightDividerListener = new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (!isSettingValues) {
                            AppVariables.setUserConfigValue(baseName + "RightPanePosition", newValue.doubleValue() + "");
                        }
                    }
                };
                splitPane.getDividers().get(0).positionProperty().addListener(rightDividerListener);

            } else if (leftPane != null && rightPane != null) {
                try {
                    String lv = AppVariables.getUserConfigValue(baseName + "LeftPanePosition", "0.15");
                    String rv = AppVariables.getUserConfigValue(baseName + "RightPanePosition", "0.85");
                    splitPane.setDividerPositions(Double.parseDouble(lv), Double.parseDouble(rv));
                } catch (Exception e) {
                    splitPane.setDividerPositions(0.15, 0.85);
                }
                leftDividerListener = new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (!isSettingValues) {
                            if (splitPane.getItems().contains(leftPane)) {
                                AppVariables.setUserConfigValue(baseName + "LeftPanePosition", newValue.doubleValue() + "");
                            } else {
                                AppVariables.setUserConfigValue(baseName + "RightPanePosition", newValue.doubleValue() + "");
                            }
                        }
                    }
                };
                rightDividerListener = new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (!isSettingValues) {
                            AppVariables.setUserConfigValue(baseName + "RightPanePosition", newValue.doubleValue() + "");
                        }
                    }
                };
                splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
                splitPane.getDividers().get(1).positionProperty().addListener(rightDividerListener);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void controlLeftPane() {
        if (splitPane == null || leftPane == null || leftPaneControl == null) {
            return;
        }
        isSettingValues = true;
        if (splitPane.getItems().contains(leftPane)) {
            double[] positions = splitPane.getDividerPositions();
            splitPane.getItems().remove(leftPane);
            ControlStyle.setIcon(leftPaneControl, ControlStyle.getIcon("iconDoubleRight.png"));
            if (positions.length == 2) {
                splitPane.setDividerPosition(0, positions[1]);
            }
        } else {
            splitPane.getItems().add(0, leftPane);
            try {
                String v = AppVariables.getUserConfigValue(baseName + "LeftPanePosition", "0.15");
                splitPane.setDividerPosition(0, Double.parseDouble(v));
            } catch (Exception e) {
                splitPane.setDividerPosition(0, 0.15);
            }
            ControlStyle.setIcon(leftPaneControl, ControlStyle.getIcon("iconDoubleLeft.png"));
        }
        splitPane.applyCss();
        if (splitPane.getDividers().size() == 1) {
            splitPane.getDividers().get(0).positionProperty().removeListener(leftDividerListener);
            splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);

        } else if (splitPane.getDividers().size() == 2) {
            splitPane.getDividers().get(0).positionProperty().removeListener(leftDividerListener);
            splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
            splitPane.getDividers().get(1).positionProperty().removeListener(rightDividerListener);
            splitPane.getDividers().get(1).positionProperty().addListener(rightDividerListener);
        }
        isSettingValues = false;
    }

    @FXML
    public void controlRightPane() {
        if (splitPane == null || rightPane == null || rightPaneControl == null) {
            return;
        }
        isSettingValues = true;
        if (splitPane.getItems().contains(rightPane)) {
            double[] positions = splitPane.getDividerPositions();
            splitPane.getItems().remove(rightPane);
            ControlStyle.setIcon(rightPaneControl, ControlStyle.getIcon("iconDoubleLeft.png"));
            if (positions.length == 2) {
                splitPane.setDividerPosition(0, positions[0]);
            }
        } else {
            splitPane.getItems().add(rightPane);
            try {
                String v = AppVariables.getUserConfigValue(baseName + "RightPanePosition", "0.85");
                splitPane.setDividerPosition(splitPane.getItems().size() - 2, Double.parseDouble(v));
            } catch (Exception e) {
                splitPane.setDividerPosition(splitPane.getItems().size() - 2, 0.85);
            }
            ControlStyle.setIcon(rightPaneControl, ControlStyle.getIcon("iconDoubleRight.png"));
        }
        splitPane.applyCss();
        if (splitPane.getDividers().size() == 1) {
            if (leftDividerListener != null) {
                splitPane.getDividers().get(0).positionProperty().removeListener(leftDividerListener);
                splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
            } else if (rightDividerListener != null) {
                splitPane.getDividers().get(0).positionProperty().removeListener(rightDividerListener);
                splitPane.getDividers().get(0).positionProperty().addListener(rightDividerListener);
            }
        } else if (splitPane.getDividers().size() == 2) {
            splitPane.getDividers().get(0).positionProperty().removeListener(leftDividerListener);
            splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
            splitPane.getDividers().get(1).positionProperty().removeListener(rightDividerListener);
            splitPane.getDividers().get(1).positionProperty().addListener(rightDividerListener);
        }
        isSettingValues = false;
    }

    public class FullscreenListener implements ChangeListener<Boolean> {

        private final String prefix;

        public FullscreenListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
            AppVariables.setUserConfigValue(prefix + "FullScreen", getMyStage().isFullScreen());
        }
    }

    public class MaximizedListener implements ChangeListener<Boolean> {

        private final String prefix;

        public MaximizedListener(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
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

    public void keyEventsHandler(KeyEvent event) {
//        logger.debug(event.getCode() + " " + event.getText());
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
        if (!event.isControlDown()) {
            return;
        }
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        switch (key) {
            case "e":
            case "E":
                if (startButton != null && !startButton.isDisabled()) {
                    startAction();
                } else if (okButton != null && !okButton.isDisabled()) {
                    okAction();
                }
                return;
            case "n":
            case "N":
                if (createButton != null && !createButton.isDisabled()) {
                    createAction();
                }
                return;
            case "c":
            case "C":
                if (copyButton != null && !copyButton.isDisabled()) {
                    copyAction();
                }
                return;
            case "v":
            case "V":
                if (pasteButton != null && !pasteButton.isDisabled()) {
                    pasteAction();
                }
                return;
            case "s":
            case "S":
                if (saveButton != null && !saveButton.isDisabled()) {
                    saveAction();
                }
                return;
            case "f":
            case "F":
                if (saveAsButton != null && !saveAsButton.isDisabled()) {
                    saveAsAction();
                }
                return;
            case "i":
            case "I":
                if (infoButton != null && !infoButton.isDisabled()) {
                    infoAction();
                }
                return;
            case "d":
            case "D":
                if (deleteButton != null && !deleteButton.isDisabled()) {
                    deleteAction();
                }
                return;
            case "a":
            case "A":
                if (allButton != null && !allButton.isDisabled()) {
                    allAction();
                } else if (selectAllButton != null && !selectAllButton.isDisabled()) {
                    selectAllAction();
                }
                return;
            case "x":
            case "X":
                if (cropButton != null && !cropButton.isDisabled()) {
                    cropAction();
                }
                return;
            case "g":
            case "G":
                if (clearButton != null && !clearButton.isDisabled()) {
                    clearAction();
                }
                return;
            case "r":
            case "R":
                if (recoverButton != null && !recoverButton.isDisabled()) {
                    recoverAction();
                }
                return;
            case "z":
            case "Z":
                if (undoButton != null && !undoButton.isDisabled()) {
                    undoAction();
                }
                return;
            case "y":
            case "Y":
                if (redoButton != null && !redoButton.isDisabled()) {
                    redoAction();
                }
                return;
            case "m":
            case "M":
                if (moreButton != null && !moreButton.isDisabled()) {
                    moreButton.fire();
                }
                return;
            case "-":
                setSceneFontSize(AppVariables.sceneFontSize - 1);
                return;
            case "=":
                setSceneFontSize(AppVariables.sceneFontSize + 1);
        }

    }

    public void altHandler(KeyEvent event) {
        if (!event.isAltDown()) {
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
        }
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        switch (key) {
            case "e":
            case "E":
                if (startButton != null && !startButton.isDisabled()) {
                    startAction();
                } else if (okButton != null && !okButton.isDisabled()) {
                    okAction();
                }
                break;
            case "c":
            case "C":
                if (copyButton != null && !copyButton.isDisabled()) {
                    copyAction();
                }
                break;
            case "v":
            case "V":
                if (pasteButton != null && !pasteButton.isDisabled()) {
                    pasteAction();
                }
                break;
            case "s":
            case "S":
                if (saveButton != null && !saveButton.isDisabled()) {
                    saveAction();
                }
                break;
            case "f":
            case "F":
                if (saveAsButton != null && !saveAsButton.isDisabled()) {
                    saveAsAction();
                }
                break;
            case "d":
            case "D":
                if (deleteButton != null && !deleteButton.isDisabled()) {
                    deleteAction();
                }
                break;
            case "a":
            case "A":
                if (allButton != null && !allButton.isDisabled()) {
                    allAction();
                } else if (selectAllButton != null && !selectAllButton.isDisabled()) {
                    selectAllAction();
                }
                break;
            case "x":
            case "X":
                if (cropButton != null && !cropButton.isDisabled()) {
                    cropAction();
                }
                break;
            case "g":
            case "G":
                if (clearButton != null && !clearButton.isDisabled()) {
                    clearAction();
                }
                return;
            case "r":
            case "R":
                if (recoverButton != null && !recoverButton.isDisabled()) {
                    recoverAction();
                }
                break;
            case "z":
            case "Z":
                if (undoButton != null && !undoButton.isDisabled()) {
                    undoAction();
                }
                break;
            case "y":
            case "Y":
                if (redoButton != null && !redoButton.isDisabled()) {
                    redoAction();
                }
                break;
            case "m":
            case "M":
                if (moreButton != null && !moreButton.isDisabled()) {
                    moreButton.fire();
                }
                break;
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
                break;

            case PAGE_UP:
                if (previousButton != null && !previousButton.isDisabled()) {
                    previousAction();
                }
                break;
            case PAGE_DOWN:
                if (nextButton != null && !nextButton.isDisabled()) {
                    nextAction();
                }
                break;
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
                break;
            case F2:
                if (saveButton != null && !saveButton.isDisabled()) {
                    saveAction();
                }
                break;
            case F3:
                if (recoverButton != null && !recoverButton.isDisabled()) {
                    recoverAction();
                }
                break;
            case F4:
                closeStage();
                break;
            case F5:
                refresh();
                break;
            case F11:
                if (saveAsButton != null && !saveAsButton.isDisabled()) {
                    saveAsAction();
                }
                break;
            case F12:
                if (moreButton != null && !moreButton.isDisabled()) {
                    moreButton.fire();
                }
                break;
            case ESCAPE:
                if (cancelButton != null && !cancelButton.isDisabled()) {
                    cancelAction();
                }
//                else if (stopButton != null && !stopButton.isDisabled()) {
//                    stopAction();
//                }
                break;
        }

//        String text = event.getText();
//        if (text == null || text.isEmpty()) {
//            return;
//        }
//        switch (text) {
//            case "e":
//            case "E":
//
//        }
    }

    public void initializeNext() {

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
                thisPane.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
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
        List<VisitHistory> his = VisitHistory.getRecentMenu();
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
            VisitHistory.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(sourcePathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
            VisitHistory.readFile(SourceFileType, fname);
        }

    }

    public void recordFileOpened(final File file, int pathType, int fileType) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            String path = file.getPath();
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(pathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(pathType, path);
            VisitHistory.readFile(fileType, fname);
        }

    }

    public void recordFileWritten(String file) {
        recordFileWritten(new File(file));
    }

    public void recordFileWritten(final File file) {
        recordFileWritten(file, targetPathKey, TargetPathType, TargetFileType);
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
            VisitHistory.writePath(TargetPathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(targetPathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistory.writePath(TargetPathType, path);
            VisitHistory.writeFile(TargetFileType, fname);
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
            VisitHistory.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVariables.setUserConfigValue(sourcePathKey, path);
            AppVariables.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
            VisitHistory.readFile(AddFileType, fname);
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
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

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

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
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
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
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

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
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
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
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

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
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
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
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
                    controller.setAddPathType(controller.getSourcePathType());
                }
                return VisitHistory.getRecentPath(controller.getAddPathType(), pathNumber);
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

    public File makeTargetFile(String namePrefix, String nameSuffix, File targetPath) {
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
            URL url = new URL(link.getText());
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
    public void moreAction() {

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

    public boolean clearSettings() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return false;
        }
        DerbyBase.clearData();
        cleanAppPath();
        AppVariables.initAppVaribles();
        return true;
    }

    public void cleanAppPath() {
        try {
            File userPath = new File(MyboxDataPath);
            if (userPath.exists()) {
                File[] files = userPath.listFiles();
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
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK && task != null) {
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

    public File chooseSaveFile(String title, File defaultPath, String defaultName,
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
        }
        popup = new Popup();
        popup.setAutoHide(true);
        return popup;
    }

    public void popText(String text, int delay, String color) {
        popText(text, delay, color, "1.1em", null);
    }

    public void popText(String text, int delay, String color, String size, Region attach) {
        try {
            if (popup != null) {
                popup.hide();
            }
            popup = getPopup();
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
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                hidePopup();
                            }
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

    public void popSuccessul() {
        popBigInformation(message("Successful"));
    }

    public void popFailed() {
        popError(message("Failed"));
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

    public LoadingController openHandlingStage(final Task<?> task, Modality block) {
        return openHandlingStage(task, block, null);
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block, String info) {
        try {
            final LoadingController controller
                    = FxmlStage.openLoadingStage(getMyStage(), block, task, info);

            controller.parentController = myController;

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    controller.closeStage();
                }
            });
            task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popInformation(AppVariables.message("Canceled"));
                    controller.closeStage();
                }
            });
            task.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popError(AppVariables.message("Error"));
                    controller.closeStage();
                }
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

    public void setMaskStroke() {

    }

    public void drawMaskRulerX() {

    }

    public void drawMaskRulerY() {

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
            for (int i = 0; i < num; i++) {
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

    @FXML
    public void showPalette(ActionEvent event) {
        showPalette((Control) event.getSource(), null, false);
    }

    public void showPalette(Control control, String title) {
        showPalette(control, title, false);

    }

    public void showPalette(Control control, String title, boolean pickColor) {
        if (paletteController == null || !paletteController.getMyStage().isShowing()) {
            paletteController = (ColorPaletteController) openStage(CommonValues.ColorPaletteFxml);
        }
        paletteController.init(this, control, title, pickColor);

    }

    // pick color from outside
    public boolean setColor(Control control, Color color) {
        return true;
    }

    /*
        Task
     */
    public class SingletonTask<Void> extends BaseTask<Void> {

        @Override
        protected void whenSucceeded() {
            popSuccessul();
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
            endTime = new Date().getTime();
            task = null;    // Notice: This must be done in each task!!
        }

    };

    /*
        Static methods
     */
    public static void openImageViewer(File file) {
        FxmlStage.openImageViewer(null, file);
    }

    public static void openImageViewer(String file) {
        FxmlStage.openImageViewer(null, new File(file));
    }

    public static void openImageViewer(Image image) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
            if (controller != null) {
                controller.loadImage(image);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void openImageViewer(ImageInformation info) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
            if (controller != null) {
                controller.loadImage(info);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void openImageManufacture(String filename) {
        FxmlStage.openImageManufacture(null, new File(filename));
    }

    public static void showImageInformation(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageInformation(null, info);
    }

    public static void showImageMetaData(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageMetaData(null, info);
    }

    /*
        get/set
     */
    public String getTipsLabelKey() {
        return TipsLabelKey;
    }

    public void setTipsLabelKey(String TipsLabelKey) {
        this.TipsLabelKey = TipsLabelKey;
    }

    public String getLastPathKey() {
        return LastPathKey;
    }

    public void setLastPathKey(String LastPathKey) {
        this.LastPathKey = LastPathKey;
    }

    public String getTargetPathKey() {
        return targetPathKey;
    }

    public void setTargetPathKey(String targetPathKey) {
        this.targetPathKey = targetPathKey;
    }

    public String getSourcePathKey() {
        return sourcePathKey;
    }

    public void setSourcePathKey(String sourcePathKey) {
        this.sourcePathKey = sourcePathKey;
    }

    public String getDefaultPathKey() {
        return defaultPathKey;
    }

    public void setDefaultPathKey(String defaultPathKey) {
        this.defaultPathKey = defaultPathKey;
    }

    public String getSaveAsOptionsKey() {
        return SaveAsOptionsKey;
    }

    public void setSaveAsOptionsKey(String SaveAsOptionsKey) {
        this.SaveAsOptionsKey = SaveAsOptionsKey;
    }

    public int getSourceFileType() {
        return SourceFileType;
    }

    public void setSourceFileType(int SourceFileType) {
        this.SourceFileType = SourceFileType;
    }

    public int getSourcePathType() {
        return SourcePathType;
    }

    public void setSourcePathType(int SourcePathType) {
        this.SourcePathType = SourcePathType;
    }

    public int getTargetFileType() {
        return TargetFileType;
    }

    public void setTargetFileType(int TargetFileType) {
        this.TargetFileType = TargetFileType;
    }

    public int getTargetPathType() {
        return TargetPathType;
    }

    public void setTargetPathType(int TargetPathType) {
        this.TargetPathType = TargetPathType;
    }

    public int getAddFileType() {
        return AddFileType;
    }

    public void setAddFileType(int AddFileType) {
        this.AddFileType = AddFileType;
    }

    public int getAddPathType() {
        return AddPathType;
    }

    public void setAddPathType(int AddPathType) {
        this.AddPathType = AddPathType;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public List<FileChooser.ExtensionFilter> getFileExtensionFilter() {
        return sourceExtensionFilter;
    }

    public void setFileExtensionFilter(List<FileChooser.ExtensionFilter> fileExtensionFilter) {
        this.sourceExtensionFilter = fileExtensionFilter;
    }

    public String getMyFxml() {
        return myFxml;
    }

    public void setMyFxml(String myFxml) {
        this.myFxml = myFxml;
    }

    public String getParentFxml() {
        return parentFxml;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getLoadFxml() {
        return loadFxml;
    }

    public void setLoadFxml(String loadFxml) {
        this.loadFxml = loadFxml;
    }

    public Alert getLoadingAlert() {
        return loadingAlert;
    }

    public void setLoadingAlert(Alert loadingAlert) {
        this.loadingAlert = loadingAlert;
    }

    public Task<Void> getTask() {
        return task;
    }

    public void setTask(Task<Void> task) {
        this.task = task;
    }

    public Task<Void> getBackgroundTask() {
        return backgroundTask;
    }

    public void setBackgroundTask(Task<Void> backgroundTask) {
        this.backgroundTask = backgroundTask;
    }

    public BaseController getParentController() {
        return parentController;
    }

    public void setParentController(BaseController parentController) {
        this.parentController = parentController;
    }

    public BaseController getMyController() {
        return myController;
    }

    public void setMyController(BaseController myController) {
        this.myController = myController;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public ContextMenu getPopMenu() {
        return popMenu;
    }

    public void setPopMenu(ContextMenu popMenu) {
        this.popMenu = popMenu;
    }

    public boolean isIsSettingValues() {
        return isSettingValues;
    }

    public void setIsSettingValues(boolean isSettingValues) {
        this.isSettingValues = isSettingValues;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public SaveAsType getSaveAsType() {
        return saveAsType;
    }

    public void setSaveAsType(SaveAsType saveAsType) {
        this.saveAsType = saveAsType;
    }

    public Pane getThisPane() {
        return thisPane;
    }

    public void setThisPane(Pane thisPane) {
        this.thisPane = thisPane;
    }

    public Pane getMainMenu() {
        return mainMenu;
    }

    public void setMainMenu(Pane mainMenu) {
        this.mainMenu = mainMenu;
    }

    public Pane getOperationBar() {
        return operationBar;
    }

    public void setOperationBar(Pane operationBar) {
        this.operationBar = operationBar;
    }

    public MainMenuController getMainMenuController() {
        return mainMenuController;
    }

    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    public TextField getSourceFileInput() {
        return sourceFileInput;
    }

    public void setSourceFileInput(TextField sourceFileInput) {
        this.sourceFileInput = sourceFileInput;
    }

    public TextField getSourcePathInput() {
        return sourcePathInput;
    }

    public void setSourcePathInput(TextField sourcePathInput) {
        this.sourcePathInput = sourcePathInput;
    }

    public TextField getTargetPathInput() {
        return targetPathInput;
    }

    public void setTargetPathInput(TextField targetPathInput) {
        this.targetPathInput = targetPathInput;
    }

    public TextField getTargetPrefixInput() {
        return targetPrefixInput;
    }

    public void setTargetPrefixInput(TextField targetPrefixInput) {
        this.targetPrefixInput = targetPrefixInput;
    }

    public TextField getTargetFileInput() {
        return targetFileInput;
    }

    public void setTargetFileInput(TextField targetFileInput) {
        this.targetFileInput = targetFileInput;
    }

    public TextField getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(TextField statusLabel) {
        this.statusLabel = statusLabel;
    }

    public OperationController getOperationBarController() {
        return operationBarController;
    }

    public void setOperationBarController(OperationController operationBarController) {
        this.operationBarController = operationBarController;
    }

    public Button getselectFileButton() {
        return selectFileButton;
    }

    public void setselectFileButton(Button selectFileButton) {
        this.selectFileButton = selectFileButton;
    }

    public Button getCreateButton() {
        return createButton;
    }

    public void setCreateButton(Button createButton) {
        this.createButton = createButton;
    }

    public Button getCopyButton() {
        return copyButton;
    }

    public void setCopyButton(Button copyButton) {
        this.copyButton = copyButton;
    }

    public Button getPasteButton() {
        return pasteButton;
    }

    public void setPasteButton(Button pasteButton) {
        this.pasteButton = pasteButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public void setSaveButton(Button saveButton) {
        this.saveButton = saveButton;
    }

    public Button getInfoButton() {
        return infoButton;
    }

    public void setInfoButton(Button infoButton) {
        this.infoButton = infoButton;
    }

    public Button getMetaButton() {
        return metaButton;
    }

    public void setMetaButton(Button metaButton) {
        this.metaButton = metaButton;
    }

    public Button getSelectAllButton() {
        return selectAllButton;
    }

    public void setSelectAllButton(Button selectAllButton) {
        this.selectAllButton = selectAllButton;
    }

    public Button getOkButton() {
        return okButton;
    }

    public void setOkButton(Button okButton) {
        this.okButton = okButton;
    }

    public Button getStartButton() {
        return startButton;
    }

    public void setStartButton(Button startButton) {
        this.startButton = startButton;
    }

    public Button getFirstButton() {
        return firstButton;
    }

    public void setFirstButton(Button firstButton) {
        this.firstButton = firstButton;
    }

    public Button getLastButton() {
        return lastButton;
    }

    public void setLastButton(Button lastButton) {
        this.lastButton = lastButton;
    }

    public Button getPreviousButton() {
        return previousButton;
    }

    public void setPreviousButton(Button previousButton) {
        this.previousButton = previousButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public void setNextButton(Button nextButton) {
        this.nextButton = nextButton;
    }

    public Button getGoButton() {
        return goButton;
    }

    public void setGoButton(Button goButton) {
        this.goButton = goButton;
    }

    public Button getPreviewButton() {
        return previewButton;
    }

    public void setPreviewButton(Button previewButton) {
        this.previewButton = previewButton;
    }

    public Button getCropButton() {
        return cropButton;
    }

    public void setCropButton(Button cropButton) {
        this.cropButton = cropButton;
    }

    public Button getSaveAsButton() {
        return saveAsButton;
    }

    public void setSaveAsButton(Button saveAsButton) {
        this.saveAsButton = saveAsButton;
    }

    public Button getRecoverButton() {
        return recoverButton;
    }

    public void setRecoverButton(Button recoverButton) {
        this.recoverButton = recoverButton;
    }

    public Button getRenameButton() {
        return renameButton;
    }

    public void setRenameButton(Button renameButton) {
        this.renameButton = renameButton;
    }

    public Button getTipsButton() {
        return tipsButton;
    }

    public void setTipsButton(Button tipsButton) {
        this.tipsButton = tipsButton;
    }

    public Button getViewButton() {
        return viewButton;
    }

    public void setViewButton(Button viewButton) {
        this.viewButton = viewButton;
    }

    public Button getPopButton() {
        return popButton;
    }

    public void setPopButton(Button popButton) {
        this.popButton = popButton;
    }

    public Button getRefButton() {
        return refButton;
    }

    public void setRefButton(Button refButton) {
        this.refButton = refButton;
    }

    public Button getUndoButton() {
        return undoButton;
    }

    public void setUndoButton(Button undoButton) {
        this.undoButton = undoButton;
    }

    public Button getRedoButton() {
        return redoButton;
    }

    public void setRedoButton(Button redoButton) {
        this.redoButton = redoButton;
    }

    public Button getTransparentButton() {
        return transparentButton;
    }

    public void setTransparentButton(Button transparentButton) {
        this.transparentButton = transparentButton;
    }

    public Button getWhiteButton() {
        return whiteButton;
    }

    public void setWhiteButton(Button whiteButton) {
        this.whiteButton = whiteButton;
    }

    public Button getBlackButton() {
        return blackButton;
    }

    public void setBlackButton(Button blackButton) {
        this.blackButton = blackButton;
    }

    public VBox getParaBox() {
        return paraBox;
    }

    public void setParaBox(VBox paraBox) {
        this.paraBox = paraBox;
    }

    public Label getBottomLabel() {
        return bottomLabel;
    }

    public void setBottomLabel(Label bottomLabel) {
        this.bottomLabel = bottomLabel;
    }

    public Label getTipsLabel() {
        return tipsLabel;
    }

    public void setTipsLabel(Label tipsLabel) {
        this.tipsLabel = tipsLabel;
    }

    public ImageView getTipsView() {
        return tipsView;
    }

    public void setTipsView(ImageView tipsView) {
        this.tipsView = tipsView;
    }

    public ImageView getLinksView() {
        return linksView;
    }

    public void setLinksView(ImageView linksView) {
        this.linksView = linksView;
    }

    public ChoiceBox getSaveAsOptionsBox() {
        return saveAsOptionsBox;
    }

    public void setSaveAsOptionsBox(ChoiceBox saveAsOptionsBox) {
        this.saveAsOptionsBox = saveAsOptionsBox;
    }

    public void setBaseTitle(String baseTitle) {
        this.baseTitle = baseTitle;
    }

    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
    }

    public void setMyScene(Scene myScene) {
        this.myScene = myScene;
    }

    public void setPopupTimer(Timer popupTimer) {
        this.popupTimer = popupTimer;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    public File getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(File sourcePath) {
        this.sourcePath = sourcePath;
    }

    public List<FileChooser.ExtensionFilter> getSourceExtensionFilter() {
        return sourceExtensionFilter;
    }

    public void setSourceExtensionFilter(List<FileChooser.ExtensionFilter> sourceExtensionFilter) {
        this.sourceExtensionFilter = sourceExtensionFilter;
    }

    public List<FileChooser.ExtensionFilter> getTargetExtensionFilter() {
        return targetExtensionFilter;
    }

    public void setTargetExtensionFilter(List<FileChooser.ExtensionFilter> targetExtensionFilter) {
        this.targetExtensionFilter = targetExtensionFilter;
    }

    public MaximizedListener getMaximizedListener() {
        return maximizedListener;
    }

    public void setMaximizedListener(MaximizedListener maximizedListener) {
        this.maximizedListener = maximizedListener;
    }

    public FullscreenListener getFullscreenListener() {
        return fullscreenListener;
    }

    public void setFullscreenListener(FullscreenListener fullscreenListener) {
        this.fullscreenListener = fullscreenListener;
    }

    public ColorPaletteController getPaletteController() {
        return paletteController;
    }

    public void setPaletteController(ColorPaletteController paletteController) {
        this.paletteController = paletteController;
    }

    public SimpleBooleanProperty getIsPickingColor() {
        return isPickingColor;
    }

    public void setIsPickingColor(SimpleBooleanProperty isPickingColor) {
        this.isPickingColor = isPickingColor;
    }

}
