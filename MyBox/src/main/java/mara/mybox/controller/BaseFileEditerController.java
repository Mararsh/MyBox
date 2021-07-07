package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.FileEditInformation.StringFilterType;
import static mara.mybox.data.FileEditInformation.defaultCharset;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.FindReplaceString.Operation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FileTools.getTempFile;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditerController extends BaseController {

    protected Edit_Type editType;
    protected long lineLocation, objectLocation;
    protected SimpleBooleanProperty fileChanged;
    protected boolean charsetByUser;
    protected FileEditInformation sourceInformation, targetInformation;
    protected String filterConditionsString = "";
    protected StringFilterType filterType;
    protected Line_Break lineBreak;
    protected int defaultPageSize, lineBreakWidth, lastCursor, lastCaret, currentLine;
    protected double lastScrollTop, lastScrollLeft;
    protected String lineBreakValue;
    protected String[] filterStrings;
    protected Timer autoSaveTimer;

    protected enum Action {
        None, FindFirst, FindNext, FindPrevious, FindLast, Replace, ReplaceAll,
        Filter, SetPageSize, NextPage, PreviousPage, FirstPage, LastPage, GoPage
    }

    @FXML
    protected VBox filtersTypeBox;
    @FXML
    protected TitledPane filePane, savePane, saveAsPane, findPane, filterPane,
            locatePane, encodePane, breakLinePane, backupPane;
    @FXML
    protected TextArea mainArea, lineArea, pairArea;
    @FXML
    protected ComboBox<String> encodeSelector, targetEncodeSelector, pageSelector, pageSizeSelector;
    @FXML
    protected ToggleGroup filterGroup, lineBreakGroup;
    @FXML
    protected CheckBox targetBomCheck, confirmCheck, autoSaveCheck, filterLineNumberCheck;
    @FXML
    protected ControlTimeLength autoSaveDurationController;
    @FXML
    protected Label editLabel, bomLabel, fileLabel, pageLabel, charsetLabel, selectionLabel,
            filterConditionsLabel;
    @FXML
    protected Button panesMenuButton, charactersButton, linesButton, exampleFilterButton,
            filterButton, locateObjectButton, locateLineButton;
    @FXML
    protected TextField fromInput, toInput, filterInput, currentLineBreak, objectNumberInput, lineInput;
    @FXML
    protected RadioButton crlfRadio, lfRadio, crRadio;
    @FXML
    protected HBox pageBox;
    @FXML
    protected ControlFindReplace findReplaceController;
    @FXML
    protected ControlFileBackup backupController;

    public BaseFileEditerController() {
        baseTitle = AppVariables.message("FileEditer");
        defaultPageSize = 10000;
    }

    public BaseFileEditerController(Edit_Type editType) {
        baseTitle = AppVariables.message("FileEditer");
        if (null != editType) {
            switch (editType) {
                case Text:
                    setTextType();
                    break;
                case Markdown:
                    setMarkdownType();
                    break;
                case Bytes:
                    setBytesType();
                    break;
                default:
                    break;
            }
        }
    }

    public final void setTextType() {
        editType = Edit_Type.Text;

        setFileType(VisitHistory.FileType.Text);
    }

    public final void setBytesType() {
        editType = Edit_Type.Bytes;

        setFileType(VisitHistory.FileType.Bytes);
    }

    public final void setMarkdownType() {
        editType = Edit_Type.Markdown;

        setFileType(VisitHistory.FileType.Markdown);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (findReplaceController != null) {
                findReplaceController.setValues(this);
            }

            initPage(null);

            initFileTab();
            initSaveTab();
            initBackupsTab();
            initSaveAsTab();
            initLineBreakTab();
            initLocateTab();
            initCharsetTab();
            initFilterTab();
            initFindTab();
            initMainBox();
            initPairBox();
            initPageBar();
            initToolBar();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        if (filtersTypeBox != null) {
            FxmlControl.setTooltip(filtersTypeBox, new Tooltip(message("FilterTypesComments")));
        }
    }

    @Override
    public void controlAltHandler(KeyEvent event) {
        if (event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case F:
                if (leftPaneControl != null) {
                    showLeftPane();
                }
                if (findPane != null) {
                    findPane.setExpanded(true);
                }
                if (findReplaceController != null && findReplaceController.findArea != null) {
                    findReplaceController.findArea.requestFocus();
                }
                return;
        }
        if (findReplaceController != null && findPane.isExpanded()) {
            findReplaceController.keyEventsHandler(event);
        }
        super.controlAltHandler(event);
    }

    protected void initPage(File file) {
        try {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }

            isSettingValues = true;
            fileChanged = new SimpleBooleanProperty(false);
            long pageNumber;
            if (sourceFile != null && sourceFile == file) {
                pageNumber = sourceInformation.getCurrentPage();
            } else {
                pageNumber = 1;
                resetCursor();
            }
            sourceFile = file;
            sourceInformation = FileEditInformation.newEditInformation(editType, file);
            sourceInformation.setPageSize(AppVariables.getUserConfigInt(baseName + "PageSize", defaultPageSize));
            sourceInformation.setCurrentPage(pageNumber);
            targetInformation = FileEditInformation.newEditInformation(editType);
            targetInformation.setPageSize(sourceInformation.getPageSize());
            targetInformation.setCurrentPage(sourceInformation.getPagesNumber());

            if (backupController != null) {
                backupController.loadBackups(sourceFile);
            }

            recoverButton.setDisable(file == null);
            mainArea.clear();
            lineArea.clear();
            clearPairArea();
            sourceInformation.setFindReplace(null);

            bottomLabel.setText("");
            fileLabel.setText("");
            selectionLabel.setText("");
            if (filterConditionsLabel != null) {
                filterConditionsLabel.setText("");
            }
            filterConditionsString = "";

            if (bomLabel != null) {
                bomLabel.setText("");
            }
            sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
            targetInformation.setLineBreak(sourceInformation.getLineBreak());
            targetInformation.setLineBreakValue(sourceInformation.getLineBreakValue());
            if (currentLineBreak != null) {
                switch (System.lineSeparator()) {
                    case "\r\n":
                        crlfRadio.fire();
                        currentLineBreak.setText("CRLF");
                        break;
                    case "\r":
                        crRadio.fire();
                        currentLineBreak.setText("CR");
                        break;
                    default:
                        lfRadio.fire();
                        currentLineBreak.setText("LF");
                        break;
                }
            }

            if (encodeSelector != null) {
                if (charsetByUser) {
                    sourceInformation.setCharset(Charset.forName(encodeSelector.getSelectionModel().getSelectedItem()));
                    if (targetEncodeSelector == null) {
                        targetInformation.setCharset(sourceInformation.getCharset());
                    }
                } else {
                    encodeSelector.getSelectionModel().select(defaultCharset().name());
                    encodeSelector.setDisable(false);
                    if (targetEncodeSelector != null) {
                        targetEncodeSelector.getSelectionModel().select(defaultCharset().name());
                    } else {
                        targetInformation.setCharset(defaultCharset());
                    }
                }
            }

            isSettingValues = false;
            mainArea.requestFocus();

            if (findReplaceController != null) {
                findReplaceController.lastFileRange = null;
                findReplaceController.lastStringRange = null;
                findReplaceController.findReplace = null;
                if (sourceInformation.getEditType() == Edit_Type.Bytes) {
                    FxmlControl.setTooltip(findReplaceController.tipsView, new Tooltip(message("FindReplaceBytesTips")));
                } else {
                    FxmlControl.setTooltip(findReplaceController.tipsView, new Tooltip(message("FindReplaceTextsTips")));
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            isSettingValues = false;
        }
    }

    protected void initFileTab() {
        try {
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSaveTab() {
        try {
            if (savePane != null) {
                savePane.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "SavePane", savePane.isExpanded());
                        });
                savePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "SavePane", false));
            }
            if (confirmCheck != null) {
                confirmCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ConfirmSave", true));
                confirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "ConfirmSave", newValue);
                    }
                });
            }

            if (autoSaveCheck != null) {
                autoSaveCheck.setDisable(true);
                autoSaveCheck.disabledProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldValue, Boolean newValue) {
                        checkAutoSave();
                    }
                });
                autoSaveCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "AutoSave", true));
                autoSaveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldValue, Boolean newValue) {
                        checkAutoSave();
                    }
                });

                autoSaveDurationController
                        .permitInvalid(!autoSaveCheck.isSelected())
                        .init(baseName + "AutoSaveDuration", 300);
                autoSaveDurationController.notify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                        checkAutoSave();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBackupsTab() {
        try {
            if (backupPane == null) {
                return;
            }
            backupPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "BackupPane", false));
            backupPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "BackupPane", backupPane.isExpanded());
                    });

            backupController.setControls(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSaveAsTab() {
        try {
            if (saveAsPane != null) {
                saveAsPane.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "SaveAsPane", saveAsPane.isExpanded());
                        });
                saveAsPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "SaveAsPane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkAutoSave() {
        try {
            if (autoSaveCheck == null) {
                return;
            }
            AppVariables.setUserConfigValue(baseName + "AutoSave", autoSaveCheck.isSelected());
            autoSaveDurationController.permitInvalid(autoSaveCheck.isDisabled() || !autoSaveCheck.isSelected());
            if (confirmCheck != null) {
                confirmCheck.setVisible(autoSaveCheck.isDisabled() || !autoSaveCheck.isSelected());
            }
            if (autoSaveTimer != null) {
                autoSaveTimer.cancel();
                autoSaveTimer = null;
            }
            if (sourceFile == null || autoSaveCheck.isDisabled()
                    || !autoSaveCheck.isSelected() || autoSaveDurationController.value <= 0) {
                return;
            }
            long interval = autoSaveDurationController.value * 1000;
            autoSaveTimer = new Timer();
            autoSaveTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        popInformation(message("Saving"));
                        saveAction();
                    });
                }
            }, interval, interval);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initLineBreakTab() {
        try {
            if (breakLinePane == null) {
                return;
            }
            breakLinePane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "BreakLinePane", breakLinePane.isExpanded());
                    });
            breakLinePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "BreakLinePane", false));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initCharsetTab() {
        try {
            if (encodePane == null) {
                encodePane.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "EncodePane", encodePane.isExpanded());
                        });
                encodePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "EncodePane", true));
            }
            if (encodeSelector != null) {
                Tooltip tips = new Tooltip(AppVariables.message("EncodeComments"));
                tips.setFont(new Font(16));
                FxmlControl.setTooltip(encodeSelector, tips);

                encodeSelector.getItems().addAll(TextTools.getCharsetNames());
                encodeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        sourceInformation.setCharset(Charset.forName(encodeSelector.getSelectionModel().getSelectedItem()));
                        charsetByUser = !isSettingValues;
                        if (!isSettingValues && sourceFile != null) {
                            openFile(sourceFile);
                        };
                    }
                });
            }
            if (targetEncodeSelector != null) {
                targetEncodeSelector.getItems().addAll(TextTools.getCharsetNames());
                targetEncodeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        targetInformation.setCharset(Charset.forName(newValue));
                        if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                                || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                                || "UTF-32LE".equals(newValue)) {
                            targetBomCheck.setDisable(false);
                        } else {
                            targetBomCheck.setDisable(true);
                            if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                                targetBomCheck.setSelected(true);
                            } else {
                                targetBomCheck.setSelected(false);
                            }
                        }
                    }
                });
                Tooltip tips = new Tooltip(AppVariables.message("BOMcomments"));
                tips.setFont(new Font(16));
                FxmlControl.setTooltip(targetBomCheck, tips);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFilterTab() {
        try {
            if (filterPane != null) {
                filterPane.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "FilterPane", filterPane.isExpanded());
                        });
                filterPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "FilterPane", false));
            }

            if (filterGroup != null) {
                filterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends Toggle> ov,
                            Toggle old_toggle, Toggle new_toggle) {
                        checkFilterType();
                    }
                });
                checkFilterType();
            }

            if (filterInput != null) {
                filterInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (!isSettingValues) {
                            checkFilterStrings();
                        }
                    }
                });
                checkFilterStrings();
            }

            if (filterLineNumberCheck != null) {
                filterLineNumberCheck.setSelected(AppVariables.getUserConfigBoolean("FilterRecordLineNumber", true));
                filterLineNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("FilterRecordLineNumber", filterLineNumberCheck.isSelected());
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initLocateTab() {
        try {
            if (locatePane != null) {
                locatePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "LocatePane", false));
                locatePane.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "LocatePane", locatePane.isExpanded());
                        });
            }
            if (lineInput != null) {
                lineInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            int v = Integer.valueOf(lineInput.getText());
                            if (v > 0 && v <= sourceInformation.getLinesNumber()) {
                                lineLocation = v;
                                lineInput.setStyle(null);
                                locateLineButton.setDisable(false);
                            } else {
                                lineInput.setStyle(badStyle);
                                locateLineButton.setDisable(true);
                            }
                        } catch (Exception e) {
                            lineInput.setStyle(badStyle);
                            locateLineButton.setDisable(true);
                        }
                    }
                });
            }

            if (objectNumberInput != null) {
                objectNumberInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            int v = Integer.valueOf(objectNumberInput.getText());
                            if (v > 0 && v <= sourceInformation.getObjectsNumber()) {
                                objectLocation = v;
                                objectNumberInput.setStyle(null);
                                locateObjectButton.setDisable(false);
                            } else {
                                objectNumberInput.setStyle(badStyle);
                                locateObjectButton.setDisable(true);
                            }
                        } catch (Exception e) {
                            objectNumberInput.setStyle(badStyle);
                            locateObjectButton.setDisable(true);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkFilterType() {
        if (filterGroup == null) {
            return;
        }
        String selected = ((RadioButton) filterGroup.getSelectedToggle()).getText();
        for (StringFilterType type : StringFilterType.values()) {
            if (message(type.name()).equals(selected)) {
                filterType = type;
                break;
            }
        }
        if (filterType == StringFilterType.MatchRegularExpression
                || filterType == StringFilterType.NotMatchRegularExpression
                || filterType == StringFilterType.IncludeRegularExpression
                || filterType == StringFilterType.NotIncludeRegularExpression) {
            if (exampleFilterButton != null) {
                exampleFilterButton.setVisible(true);
            }
            FxmlControl.removeTooltip(filterInput);
        } else {
            if (exampleFilterButton != null) {
                exampleFilterButton.setVisible(false);
            }
            FxmlControl.setTooltip(filterInput, new Tooltip(message("SeparateByCommaBlanksInvolved")));
        }
    }

    protected void checkFilterStrings() {
        if (filterInput == null) {
            return;
        }
        String f = filterInput.getText();
        boolean invalid = f.isEmpty() || mainArea.getText().isEmpty();
        if (!invalid) {
            if (sourceInformation != null && f.length() >= sourceInformation.getPageSize()) {
                popError(AppVariables.message("FindStringLimitation"));
                invalid = true;
            } else {
                if (filterType == StringFilterType.MatchRegularExpression
                        || filterType == StringFilterType.NotMatchRegularExpression
                        || filterType == StringFilterType.IncludeRegularExpression
                        || filterType == StringFilterType.NotIncludeRegularExpression) {
                    filterStrings = new String[1];
                    filterStrings[0] = filterInput.getText();
                } else {
                    filterStrings = StringTools.splitByComma(filterInput.getText());
                }
                invalid = filterStrings.length == 0;
            }
        }
        filterButton.setDisable(invalid);
    }

    protected void initFindTab() {
        try {
            if (findReplaceController == null) {
                return;
            }
            findPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "FindPane", false));
            findPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "FindPane", findPane.isExpanded());
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popFilterExample(MouseEvent mouseEvent) {
        FxmlControl.popRegexExample(this, filterInput, mouseEvent);
    }

    protected void initMainBox() {
        try {
            if (mainArea == null) {
                return;
            }
            mainArea.setStyle("-fx-highlight-fill: dodgerblue; -fx-highlight-text-fill: white;");

            mainArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    updateInterface(true);
                }

            });
            mainArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    scrollTopPairArea(newValue.doubleValue());
                    isSettingValues = true;
                    lineArea.setScrollTop(newValue.doubleValue());
                    isSettingValues = false;
                }
            });
            mainArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    scrollLeftPairArea(newValue.doubleValue());
                }
            });
            mainArea.selectionProperty().addListener(new ChangeListener<IndexRange>() {
                @Override
                public void changed(ObservableValue ov, IndexRange oldValue, IndexRange newValue) {
                    checkMainAreaSelection();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void makeEditContextMenu(PopNodesController controller, TextInputControl node) {
        try {
            if (node.equals(mainArea)) {
                makeMainAreaContextMenu(controller);

            } else {
                super.makeEditContextMenu(controller, node);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeMainAreaContextMenu(PopNodesController controller) {
        try {

            controller.addEditPane(mainArea);
            controller.addNode(new Separator());

            List<Node> editNodes = new ArrayList<>();
            Button save = new Button(message("Save"));
            save.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    saveAction();
                }
            });
            editNodes.add(save);

            Button recover = new Button(message("Recover"));
            recover.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    recoverAction();
                }
            });
            recover.setDisable(recoverButton.isDisable());
            editNodes.add(recover);

            Button pop = new Button(message("Pop"));
            pop.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popAction();
                }
            });
            editNodes.add(pop);

            controller.addFlowPane(editNodes);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void scrollTopPairArea(double value) {
        if (isSettingValues || pairArea == null || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        isSettingValues = true;
        pairArea.setScrollTop(value);
        isSettingValues = false;
    }

    protected void scrollLeftPairArea(double value) {
        if (isSettingValues || pairArea == null || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        isSettingValues = true;
        pairArea.setScrollLeft(value);
        isSettingValues = false;
    }

    protected void initPairBox() {
        try {
            if (pairArea == null) {
                return;
            }
            pairArea.setStyle("-fx-highlight-fill: black; -fx-highlight-text-fill: palegreen;");
            pairArea.setEditable(false);
            pairArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (AppVariables.getUserConfigBoolean(baseName + "ScrollSynchronously", false)) {
                        isSettingValues = true;
                        mainArea.setScrollTop(newValue.doubleValue());
                        isSettingValues = false;
                    }
                }
            });
            pairArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (AppVariables.getUserConfigBoolean(baseName + "ScrollSynchronously", false)) {
                        isSettingValues = true;
                        mainArea.setScrollLeft(newValue.doubleValue());
                        isSettingValues = false;
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkMainAreaSelection() {
        if (isSettingValues) {
            return;
        }
        setPairAreaSelection();
        IndexRange currentSelection = mainArea.getSelection();
        long pageStart = 0, pageEnd;
        // end of range is *excluded* when handled internally, while it is *included* when displayed
        if (editType == Edit_Type.Bytes) {
            pageStart = currentSelection.getStart() / 3 + 1;
            pageEnd = currentSelection.getLength() == 0 ? pageStart : currentSelection.getEnd() / 3;

        } else {
            pageStart = currentSelection.getStart() + 1;
            pageEnd = currentSelection.getLength() == 0 ? pageStart : currentSelection.getEnd();
            if (sourceInformation != null && sourceInformation.getLineBreak().equals(Line_Break.CRLF)) {
                String sub = mainArea.getText(0, currentSelection.getStart());
                int startLinesNumber = FindReplaceString.count(sub, "\n");
                pageStart += startLinesNumber;
                sub = mainArea.getText(currentSelection.getStart(), currentSelection.getEnd());
                int linesNumber = FindReplaceString.count(sub, "\n");
                pageEnd += startLinesNumber + linesNumber;
            }
        }
        String info = AppVariables.message("SelectionInPage") + ": "
                + StringTools.format(pageStart) + " - " + StringTools.format(pageEnd)
                + " (" + StringTools.format(currentSelection.getLength() == 0 ? 0 : pageEnd - pageStart + 1) + ")";
        if (sourceInformation != null
                && sourceInformation.getPagesNumber() > 1 && sourceInformation.getCurrentPage() > 1) {
            long fileStart = sourceInformation.getCurrentPageObjectStart() + pageStart;
            long fileEnd = sourceInformation.getCurrentPageObjectStart() + pageEnd;
            info += "  " + AppVariables.message("SelectionInFile") + ": "
                    + StringTools.format(fileStart) + " - " + StringTools.format(fileEnd);
        }
        selectionLabel.setText(info);
    }

    protected void initToolBar() {
        try {
            if (topCheck != null) {
                topCheck.setVisible(false);
                topCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Top", true));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initPageBar() {
        try {
            List<String> values = new ArrayList();
            values.addAll(Arrays.asList("100,000", "500,000", "50,000", "10,000", "20,000",
                    "200,000", "1,000,000", "2,000,000", "20,000,000", "200,000,000"));
            pageSizeSelector.getItems().addAll(values);
            int pageSize = AppVariables.getUserConfigInt(baseName + "PageSize", defaultPageSize);
            if (pageSize <= 0) {
                pageSize = defaultPageSize;
            }
            pageSizeSelector.setValue(StringTools.format(pageSize));
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    Platform.runLater(() -> {
                        setPageSize();
                    });
                }
            });

            pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkCurrentPage();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setPageSize() {
        try {
            if (isSettingValues || !checkBeforeNextAction()) {
                return;
            }
            int v = Integer.valueOf(pageSizeSelector.getValue().replaceAll(",", ""));
            int available = (int) (SystemTools.freeBytes() / 4);
            if (v > available) {
                popError(message("MayOutOfMemory"));
                v = available;
            } else if (v <= 0) {
                pageSizeSelector.getEditor().setStyle(badStyle);
                popError(message("InvalidParameters"));
                return;
            }
            pageSizeSelector.getEditor().setStyle(null);
            AppVariables.setUserConfigInt(baseName + "PageSize", v);
            sourceInformation.setPageSize(v);
            sourceInformation.setCurrentPage(1);
            if (sourceInformation.getLineBreak() == Line_Break.Width) {
                sourceInformation.setTotalNumberRead(false);
                openFile(sourceFile);
            } else {
                loadPage();
            }
        } catch (Exception e) {
            pageSizeSelector.getEditor().setStyle(badStyle);
            popError(message("InvalidParameters"));
        }
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null || !checkBeforeNextAction()) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.valueOf(value);
            if (v > 0 && v <= sourceInformation.getPagesNumber()) {
                sourceInformation.setCurrentPage(v);
                if (findReplaceController != null) {
                    findReplaceController.lastFileRange = null;
                }
                pageSelector.getEditor().setStyle(null);
                loadPage();
                return true;
            } else {
                pageSelector.getEditor().setStyle(badStyle);
                return false;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(badStyle);
            return false;
        }
    }

    @FXML
    public void refreshPairAction() {
        if (isSettingValues || pairArea == null
                || !splitPane.getItems().contains(rightPane)) {
            return;
        }
    }

    @Override
    public void checkRightPaneClose() {
        super.checkRightPaneClose();
        if (isSettingValues || splitPane == null || rightPane == null
                || closeRightPaneCheck == null || rightPaneControl == null) {
            return;
        }
        if (!closeRightPaneCheck.isSelected()) {
            refreshPairAction();
        }
    }

    @FXML
    @Override
    public void controlRightPane() {
        if (splitPane == null || rightPane == null || rightPaneControl == null) {
            return;
        }
        super.controlRightPane();
        refreshPairAction();
    }

    @Override
    public void sourceFileChanged(final File file) {
        super.sourceFileChanged(file);
        sourceFile = null;
        openFile(file);
    }

    public void openFile(File file) {
        if (editType == Edit_Type.Bytes) {
            openBytesFile(file);
        } else {
            openTextFile(file);
        }
    }

    public void openTextFile(File file) {
        if (file == null) {
            return;
        }
        initPage(file);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            bottomLabel.setText(AppVariables.message("CheckingEncoding"));
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (sourceInformation == null) {
                        return false;
                    }
                    sourceInformation.setLineBreak(TextTools.checkLineBreak(sourceFile));
                    sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
                    targetInformation.setLineBreak(sourceInformation.getLineBreak());
                    targetInformation.setLineBreakValue(sourceInformation.getLineBreakValue());
                    if (charsetByUser) {
                        return true;
                    } else {
                        return sourceInformation.checkCharset();
                    }
                }

                @Override
                protected void whenSucceeded() {
                    bottomLabel.setText("");
                    isSettingValues = true;
                    if (currentLineBreak != null) {
                        currentLineBreak.setText(sourceInformation.getLineBreak().toString());
                        switch (sourceInformation.getLineBreak()) {
                            case CRLF:
                                crlfRadio.fire();
                                break;
                            case CR:
                                crRadio.fire();
                                break;
                            default:
                                lfRadio.fire();
                                break;
                        }
                    }
                    if (encodeSelector != null) {
                        encodeSelector.getSelectionModel().select(sourceInformation.getCharset().name());
                        if (targetEncodeSelector != null) {
                            targetEncodeSelector.getSelectionModel().select(sourceInformation.getCharset().name());
                        } else {
                            targetInformation.setCharset(sourceInformation.getCharset());
                        }
                        if (sourceInformation.isWithBom()) {
                            encodeSelector.setDisable(true);
                            bomLabel.setText(AppVariables.message("WithBom"));
                            if (targetBomCheck != null) {
                                targetBomCheck.setSelected(true);
                            }
                        } else {
                            encodeSelector.setDisable(false);
                            bomLabel.setText("");
                            if (targetBomCheck != null) {
                                targetBomCheck.setSelected(false);
                            }
                        }
                    } else {
                        targetInformation.setCharset(sourceInformation.getCharset());
                    }
                    isSettingValues = false;
                    loadPage();
                }

                @Override
                protected void whenFailed() {
                    bottomLabel.setText("");
                    super.whenFailed();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public void openBytesFile(File file) {
        if (file == null) {
            return;
        }
        if (lineBreak == Line_Break.Value && lineBreakValue == null
                || lineBreak == Line_Break.Width && lineBreakWidth <= 0) {
            popError(AppVariables.message("WrongLineBreak"));
            breakLinePane.setExpanded(true);
            return;
        }
        initPage(file);
        sourceInformation.setLineBreak(lineBreak);
        sourceInformation.setLineBreakValue(lineBreakValue);
        sourceInformation.setLineBreakWidth(lineBreakWidth);
        loadPage();

    }

    protected void loadTotalNumbers() {
        if (sourceInformation == null || sourceFile == null
                || sourceInformation.isTotalNumberRead()) {
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }
            backgroundTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ok = sourceInformation.readTotalNumbers();
                    return ok;
                }

                @Override
                protected void whenSucceeded() {
                    updateNumbers(false);
                }

            };
            backgroundTask.setSelf(backgroundTask);
            Thread thread = new Thread(backgroundTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void loadPage() {
        if (sourceInformation == null || sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            bottomLabel.setText(AppVariables.message("ReadingFile"));
            task = new SingletonTask<Void>() {

                private String text;

                @Override
                protected boolean handle() {
                    text = sourceInformation.readPage();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    bottomLabel.setText("");
                    if (text != null) {
                        isSettingValues = true;
                        mainArea.setText(text);
                        isSettingValues = false;
                        formatMainArea();
                        updateInterface(false);
                        updateCursor();
                        if (!sourceInformation.isTotalNumberRead()) {
                            loadTotalNumbers();
                        }

                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    taskCanceled();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void updateInterface(boolean changed) {
        updateControls(changed);
        updateNumbers(changed);
        updatePairArea();
    }

    protected void updateControls(boolean changed) {
        fileChanged.set(changed);
        bottomLabel.setText("");
        selectionLabel.setText("");
        if (getMyStage() == null || sourceInformation == null) {
            return;
        }
        String t = getBaseTitle();
        if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (fileChanged.getValue()) {
            getMyStage().setTitle(t + "*");
        } else {
            getMyStage().setTitle(t);
        }

        if (changed && !validMainArea()) {
            if (editLabel != null) {
                editLabel.setText(message("InvalidData"));
            }
            mainArea.setStyle(badStyle);
//            popError(message("InvalidData"));
        } else if (editLabel != null) {
            editLabel.setText("");
            mainArea.setStyle(null);
        }

        if (okButton != null) {
            okButton.setDisable(changed);
        }
        if (autoSaveCheck != null) {
            autoSaveCheck.setDisable(sourceFile == null);
        }
    }

    protected synchronized void updateCursor() {
        String pageText = mainArea.getText();
        if (lastCursor >= 0) {
            mainArea.requestFocus();
            mainArea.deselect();
            mainArea.selectRange(lastCursor, lastCaret > lastCursor ? lastCaret : lastCursor);

        } else if (sourceInformation.getCurrentLine() >= 1) {
            if (sourceInformation.getCurrentPageLineStart() <= sourceInformation.getCurrentLine()
                    && sourceInformation.getCurrentPageLineEnd() >= sourceInformation.getCurrentLine()) {
                String[] lines = pageText.split("\n");
                int index = 0, end = (int) (sourceInformation.getCurrentLine() - sourceInformation.getCurrentPageLineStart());
                for (int i = 0; i < end; ++i) {
                    index += lines[i].length() + 1;
                }
                mainArea.selectRange(index, index);
            }

        } else if (findReplaceController != null && sourceInformation.getFindReplace() != null
                && sourceInformation.getFindReplace().getFileRange() != null) {
            FindReplaceFile findReplaceFile = sourceInformation.getFindReplace();
            findReplaceController.lastFileRange = findReplaceFile.getFileRange();
            String info = message("RangeInFile") + ":"
                    + (findReplaceController.lastFileRange.getStart() / findReplaceFile.getUnit() + 1) + "-"
                    + (findReplaceController.lastFileRange.getEnd() / findReplaceFile.getUnit());

            if (findReplaceFile.getStringRange() == null) {
                if (sourceInformation.getEditType() != Edit_Type.Bytes) {
                    FindReplaceFile.stringRange(findReplaceFile, pageText);
                } else {
                    FindReplaceFile.bytesRange(findReplaceFile, pageText);
                }
            }
            IndexRange range = findReplaceFile.getStringRange();
            if (range != null) {
                mainArea.requestFocus();
                mainArea.deselect();
//                MyBoxLog.debug("pageText.length():" + pageText.length() + " range.getEnd():" + range.getEnd());
                int start = range.getStart(), end = pageText.length();
                if (findReplaceFile.getOperation() == Operation.ReplaceFirst) {
                    end = Math.min(end, start + findReplaceFile.getReplaceString().length());
                } else {
                    end = Math.min(end, range.getEnd());
                }
                mainArea.selectRange(start, end);
                findReplaceController.lastStringRange = range;
                info += "\n" + message("RangeInPage") + ":" + (range.getStart() + 1) + "-" + (range.getEnd());
            }
            findReplaceController.findLabel.setText(info);
        }
        recoverCursor();
    }

    protected void updateNumbers(boolean changed) {
        saveButton.setDisable(false);
        if (saveAsButton != null) {
            saveAsButton.setDisable(false);
        }
        String pageText = mainArea.getText();
        int pageObjectsNumber;
        int pageLinesNumber = FindReplaceString.count(pageText, "\n") + 1;
        if (editType == Edit_Type.Bytes) {
            pageObjectsNumber = pageText.replaceAll("\\s+|\n", "").length() / 2;
        } else {
            pageObjectsNumber = pageText.length();
            if (sourceInformation.getLineBreak().equals(Line_Break.CRLF)) {
                pageObjectsNumber += pageLinesNumber - 1;
            }
        }
        long pageObjectStart = 1;
        long pageObjectEnd = pageObjectsNumber;
        long pageLineStart = 1, pageLineEnd = pageLinesNumber, pagesNumber = 1;
        long fileObjectNumber = pageObjectsNumber;
        long fileLinesNumber = pageLinesNumber;
        int pageSize = sourceInformation.getPageSize();
        long currentPage = sourceInformation.getCurrentPage();
        String fileInfo = "";
        if (sourceFile == null) {
            if (pageLabel != null) {
                pageLabel.setText("");
            }
            sourceInformation.setObjectsNumber(pageObjectsNumber);
            sourceInformation.setLinesNumber(pageLinesNumber);
            setLines(1, pageLinesNumber);
        } else {
            pageLineStart = sourceInformation.getCurrentPageLineStart();
            pageLineEnd = pageLineStart + pageLinesNumber - 1;
            setLines(pageLineStart, pageLineEnd);
            pageObjectStart = sourceInformation.getCurrentPageObjectStart() + 1;
            pageObjectEnd = sourceInformation.getCurrentPageObjectStart() + pageObjectsNumber;
            if (!sourceInformation.isTotalNumberRead()) {
                saveButton.setDisable(true);
                if (saveAsButton != null) {
                    saveAsButton.setDisable(true);
                }

                if (locateObjectButton != null) {
                    locateObjectButton.setDisable(true);
                    locateLineButton.setDisable(true);
                }
                fileInfo = message("CountingTotalNumber") + "\n\n";
            } else {
                fileObjectNumber = sourceInformation.getObjectsNumber();
                fileLinesNumber = sourceInformation.getLinesNumber();

                pagesNumber = fileObjectNumber / pageSize;
                if (fileObjectNumber % pageSize > 0) {
                    pagesNumber++;
                }
                sourceInformation.setPagesNumber(pagesNumber);
            }
            fileInfo += message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + (editType == Edit_Type.Bytes ? message("BytesNumberInFile") : message("CharactersNumberInFile"))
                    + ": " + StringTools.format(fileObjectNumber) + "\n"
                    + message("LinesNumberInFile") + ": " + StringTools.format(fileLinesNumber) + "\n"
                    + (editType == Edit_Type.Bytes ? message("PageSizeBytes") : message("PageSizeCharacters"))
                    + ": " + StringTools.format(pageSize) + "\n"
                    + message("CurrentPage") + ": " + StringTools.format(currentPage)
                    + " / " + StringTools.format(pagesNumber) + "\n";
        }

        String objectInfo, lineInfo;
        if (pagesNumber > 1) {
            objectInfo = editType == Edit_Type.Bytes ? message("BytesRangeInPage") : message("CharactersRangeInPage");
            objectInfo += ": " + StringTools.format(pageObjectStart) + " - " + StringTools.format(pageObjectEnd) + "\n"
                    + " ( " + StringTools.format(pageObjectsNumber) + " )\n";
            lineInfo = message("LinesRangeInPage")
                    + ": " + StringTools.format(pageLineStart) + " - " + StringTools.format(pageLineEnd)
                    + " ( " + StringTools.format(pageLinesNumber) + " )\n";
        } else {
            objectInfo = editType == Edit_Type.Bytes ? message("BytesNumberInPage") : message("CharactersNumberInPage");
            objectInfo += ": " + StringTools.format(pageObjectsNumber) + "\n";
            lineInfo = message("LinesNumberInPage") + ": " + StringTools.format(pageLinesNumber) + "\n";
        }
        fileInfo += message("CurrentFileLineBreak") + ": " + sourceInformation.lineBreakName() + "\n"
                + message("CurrentFileCharset") + ": " + sourceInformation.getCharset().name() + "\n"
                + objectInfo + lineInfo
                + message("PageModifyTime") + ": " + DateTools.nowString();
        fileLabel.setText(fileInfo);

        pageBox.setDisable(changed);
        pagePreviousButton.setDisable(currentPage <= 1 || pagesNumber < 2);
        pageNextButton.setDisable(currentPage >= pagesNumber || pagesNumber < 2);
        List<String> pages = new ArrayList<>();
        for (int i = 1; i <= pagesNumber; i++) {
            pages.add(i + "");
        }
        isSettingValues = true;
        pageSelector.getItems().clear();
        pageSelector.getItems().setAll(pages);
        pageLabel.setText("/" + pagesNumber);
        pageSelector.setValue(currentPage + "");
        pageSelector.getEditor().setStyle(null);
        pageSizeSelector.setValue(StringTools.format(pageSize));
        isSettingValues = false;

        if (pagesNumber > 1) {
            locatePane.getContent().setDisable(changed);
            filterPane.getContent().setDisable(changed);
        }
        if (encodePane != null) {
            encodePane.getContent().setDisable(changed);
        }
        if (editType != Edit_Type.Bytes) {
            encodeSelector.setDisable(sourceInformation.isWithBom());
        }
    }

    protected boolean validMainArea() {
        return true;
    }

    protected void recordCursor() {
        lastScrollLeft = mainArea.getScrollLeft();
        lastScrollTop = mainArea.getScrollTop();
        lastCursor = mainArea.getAnchor();
        lastCaret = mainArea.getCaretPosition();
    }

    protected void resetCursor() {
        lastScrollLeft = -1;
        lastScrollTop = -1;
        lastCursor = -1;
        lastCaret = -1;
    }

    protected void recoverCursor() {
        int delay = Math.min(2000, mainArea.getLength() / 10);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (lastScrollLeft >= 0) {
                        mainArea.setScrollLeft(lastScrollLeft);
                    }
                    if (lastScrollTop >= 0) {
                        mainArea.setScrollTop(lastScrollTop);
                    }
                    resetCursor();
                });
            }
        }, delay);
    }

    protected boolean formatMainArea() {
        return true;
    }

    protected void setMainArea(String text) {
        if (isSettingValues) {
            return;
        }
        mainArea.setText(text);
    }

    // include "to"
    protected void setLines(long from, long to) {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        if (from < 0 || to <= 0 || from > to) {
            lineArea.clear();
        } else {
            StringBuilder lines = new StringBuilder();
            for (long i = from; i <= to; ++i) {
                lines.append(i).append("\n");
            }
            lineArea.setText(lines.toString());
        }
        lineArea.setScrollTop(mainArea.getScrollTop());
        AnchorPane.setLeftAnchor(mainArea, (to + "").length() * AppVariables.sceneFontSize + 20d);
        isSettingValues = false;
    }

    protected void updatePairArea() {
        if (isSettingValues || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        if (AppVariables.getUserConfigBoolean(baseName + "UpdateSynchronously", false)
                || (pairArea != null && pairArea.getText().isEmpty())) {
            refreshPairAction();
        }
    }

    protected void setPairAreaSelection() {
        if (isSettingValues || pairArea == null || !splitPane.getItems().contains(rightPane)) {
            return;
        }
    }

    protected void clearPairArea() {
        if (pairArea == null) {
            return;
        }
        pairArea.clear();
    }

    @FXML
    @Override
    public void recoverAction() {
        try {
            if (!recoverButton.isDisabled() && sourceInformation.getFile() != null) {
                loadPage();
            }
        } catch (Exception e) {

        }
    }

    @FXML
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (sourceInformation.getObjectsNumber() <= sourceInformation.getCurrentPageObjectEnd()) {
            pageNextButton.setDisable(true);
        } else {
            sourceInformation.setCurrentPage(sourceInformation.getCurrentPage() + 1);
            if (findReplaceController != null) {
                findReplaceController.lastFileRange = null;
            }
            loadPage();
        }
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (sourceInformation.getCurrentPage() <= 1) {
            pagePreviousButton.setDisable(true);
        } else {
            sourceInformation.setCurrentPage(sourceInformation.getCurrentPage() - 1);
            if (findReplaceController != null) {
                findReplaceController.lastFileRange = null;
            }
            loadPage();
        }
    }

    @FXML
    @Override
    public void pageFirstAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceInformation.setCurrentPage(1);
        if (findReplaceController != null) {
            findReplaceController.lastFileRange = null;
        }
        loadPage();
    }

    @FXML
    @Override
    public void pageLastAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceInformation.setCurrentPage(sourceInformation.getPagesNumber());
        if (findReplaceController != null) {
            findReplaceController.lastFileRange = null;
        }
        loadPage();
    }

    @FXML
    @Override
    public void saveAction() {
        recordCursor();
        if (!formatMainArea()) {
            return;
        }
        if (sourceFile == null) {
            saveNew();
        } else {
            saveExisted();
        }
    }

    protected void saveNew() {
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                null, targetExtensionFilter);
        if (file == null) {
            return;
        }
        targetInformation.setFile(file);
        if (targetBomCheck != null) {
            targetInformation.setWithBom(targetBomCheck.isSelected());
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ok = targetInformation.writeObject(mainArea.getText());
                    return ok;
                }

                @Override
                protected void whenSucceeded() {
                    recordFileWritten(file);
                    popSaved();
                    charsetByUser = false;
                    sourceFile = file;
                    sourceInformation = targetInformation;
                    sourceInformation.setTotalNumberRead(false);
                    updateInterface(false);
                    loadTotalNumbers();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void saveExisted() {
        if (confirmCheck.isVisible() && confirmCheck.isSelected() && (autoSaveTimer == null)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("SureOverrideFile"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVariables.message("SaveAs"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAsAction();
                return;
            }
        }
        targetInformation.setFile(sourceFile);
        if (targetBomCheck != null) {
            targetInformation.setWithBom(targetBomCheck.isSelected());
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (backupController != null && backupController.isBack()) {
                        backupController.addBackup(sourceFile);
                    }
                    return targetInformation.writePage(sourceInformation, mainArea.getText());
                }

                @Override
                protected void whenSucceeded() {
                    recordFileWritten(sourceFile);
                    popSaved();
                    if (sourceInformation.getCharset().equals(targetInformation.getCharset())
                            && sourceInformation.getLineBreak().name().equals(targetInformation.getLineBreak().name())
                            && sourceInformation.isWithBom() == targetInformation.isWithBom()) {
                        sourceInformation.setTotalNumberRead(false);
                        updateInterface(false);
                        loadTotalNumbers();
                    } else {
                        openFile(sourceFile);
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        recordCursor();
        if (!formatMainArea()) {
            return;
        }
        String name;
        if (sourceFile != null) {
            name = FileTools.getFilePrefix(sourceFile.getName());
        } else {
            name = new Date().getTime() + "";
        }
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        targetInformation.setFile(file);
        if (targetBomCheck != null) {
            targetInformation.setWithBom(targetBomCheck.isSelected());
        } else {
            targetInformation.setWithBom(sourceInformation.isWithBom());
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return targetInformation.writePage(sourceInformation, mainArea.getText());
                }

                @Override
                protected void whenSucceeded() {
                    recordFileWritten(file);
                    if (saveAsType == SaveAsType.Load) {
                        openFile(file);
                    } else if (saveAsType == SaveAsType.Open) {
                        BaseFileEditerController controller = openNewStage();
                        controller.openFile(file);
                    }
                    popSaved();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void locateLine() {
        sourceInformation.setCurrentLine(-1);
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            String[] lines = mainArea.getText().split("\n");
            if (lineLocation > lines.length) {
                popError(message("NoData"));
                return;
            }
            mainArea.requestFocus();
            mainArea.deselect();
            int index = 0;
            for (int i = 0; i < lineLocation - 1; ++i) {
                index += lines[i].length() + 1;
            }
            mainArea.selectRange(index, index);

        } else {
            if (lineLocation > sourceInformation.getLinesNumber()) {
                popError(message("NoData"));
                return;
            }
            if (sourceInformation.getCurrentPageLineStart() <= lineLocation
                    && sourceInformation.getCurrentPageLineEnd() > lineLocation) {
                String[] lines = mainArea.getText().split("\n");
                mainArea.requestFocus();
                mainArea.deselect();
                int index = 0, end = (int) (lineLocation - sourceInformation.getCurrentPageLineStart());
                for (int i = 0; i < end; ++i) {
                    index += lines[i].length() + 1;
                }
                mainArea.selectRange(index, index);

            } else {
                sourceInformation.setCurrentLine(lineLocation);
                synchronized (this) {
                    if (task != null && !task.isQuit()) {
                        return;
                    }
                    task = new SingletonTask<Void>() {

                        private String text;

                        @Override
                        protected boolean handle() {
                            text = sourceInformation.locateLine();
                            return text != null;
                        }

                        @Override
                        protected void whenSucceeded() {
                            isSettingValues = true;
                            mainArea.setText(text);
                            isSettingValues = false;

                            sourceInformation.setCurrentLine(lineLocation);
                            updateInterface(false);
                        }
                    };
                    openHandlingStage(task, Modality.WINDOW_MODAL);
                    task.setSelf(task);
                    Thread thread = new Thread(task);
                    thread.setDaemon(false);
                    thread.start();
                }
            }
        }
    }

    @FXML
    protected void locateObject() {
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            mainArea.requestFocus();
            mainArea.deselect();
            int start = (int) ((objectLocation - 1) * sourceInformation.getObjectUnit());
            mainArea.selectRange(start, start);
            lastCursor = start;

        } else {
            long pageSize = sourceInformation.getPageSize();
            if (sourceInformation.getCurrentPageObjectStart() <= objectLocation - 1
                    && sourceInformation.getCurrentPageObjectEnd() > objectLocation) {
                mainArea.requestFocus();
                mainArea.deselect();
                int pLocation = (int) ((objectLocation - 1 - sourceInformation.getCurrentPageObjectStart())
                        * sourceInformation.getObjectUnit());
                mainArea.selectRange(pLocation, pLocation);

            } else {
                int page = (int) ((objectLocation - 1) / pageSize + 1);
                int pLocation = (int) ((objectLocation - 1) % pageSize);
                sourceInformation.setCurrentPage(page);
                lastCursor = pLocation * sourceInformation.getObjectUnit();
                loadPage();
            }
        }
    }

    @FXML
    protected void filterAction() {
        if (isSettingValues || filterButton.isDisabled() || sourceInformation == null) {
            return;
        }
        if (filterStrings == null || filterStrings.length == 0) {
            popError(message("EmptyData"));
            return;
        }
        if (fileChanged.get() && sourceInformation.getPagesNumber() > 1
                && !checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            SingletonTask filterTask = new SingletonTask<Void>() {

                private File filteredFile;
                private String finalCondition;

                @Override
                protected boolean handle() {
                    FileEditInformation filterInfo;
                    if (sourceFile != null) {
                        filterInfo = sourceInformation;
                    } else {
                        File tmpfile = FileTools.writeFile(getTempFile(".txt"), mainArea.getText(), Charset.forName("utf-8"));
                        filterInfo = FileEditInformation.newEditInformation(editType, tmpfile);
                        filterConditionsString = "";
                        if (editType != Edit_Type.Bytes) {
                            filterInfo.setLineBreak(TextTools.checkLineBreak(tmpfile));
                            filterInfo.setLineBreakValue(TextTools.lineBreakValue(filterInfo.getLineBreak()));
                        } else {
                            filterInfo.setLineBreak(sourceInformation.getLineBreak());
                            filterInfo.setLineBreakValue(sourceInformation.getLineBreakValue());
                        }
                        filterInfo.setCharset(Charset.forName("utf-8"));
                        filterInfo.setPageSize(sourceInformation.getPageSize());
                    }
                    filterInfo.setFilterStrings(filterStrings);
                    filterInfo.setFilterType(filterType);
                    String conditions = " (" + filterInfo.filterTypeName() + ": "
                            + Arrays.asList(filterInfo.getFilterStrings()) + ") ";
                    if (filterConditionsString == null || filterConditionsString.isEmpty()) {
                        finalCondition = filterInfo.getFile().getAbsolutePath() + "\n" + conditions;
                    } else {
                        finalCondition = filterConditionsString + "\n" + message("And") + conditions;
                    }
                    filteredFile = filterInfo.filter(filterLineNumberCheck.isSelected());
                    return filteredFile != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (filteredFile.length() == 0) {
                        popInformation(AppVariables.message("NoData"));
                        return;
                    }
                    TextEditerController controller = (TextEditerController) openStage(CommonValues.TextEditerFxml);
                    controller.openTextFile(filteredFile);
                    controller.filterConditionsLabel.setText(finalCondition);
                    controller.filterConditionsString = finalCondition;
                    controller.filterPane.setExpanded(true);
                }
            };
            filterTask.setSelf(filterTask);
            Thread thread = new Thread(filterTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public BaseFileEditerController openNewStage() {
        if (null == editType) {
            return null;
        } else {
            switch (editType) {
                case Text:
                    return (BaseFileEditerController) openStage(CommonValues.TextEditerFxml);
                case Bytes:
                    return (BaseFileEditerController) openStage(CommonValues.BytesEditerFxml);
                case Markdown:
                    return (MarkdownEditerController) openStage(CommonValues.MarkdownEditorFxml);
                default:
                    return null;
            }
        }
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        initPage(null);
        updateInterface(false);
    }

    public void loadContexts(String contents) {
        createAction();
        mainArea.setText(contents);
        updateInterface(true);
    }

    @FXML
    @Override
    public void popAction() {
        BaseFileEditerController controller;
        switch (editType) {
            case Text:
                controller = (BaseFileEditerController) openStage(CommonValues.TextEditerFxml);
                break;
            case Bytes:
                controller = (BaseFileEditerController) openStage(CommonValues.BytesEditerFxml);
                break;
            case Markdown:
                controller = (MarkdownEditerController) openStage(CommonValues.MarkdownEditorFxml);
                break;
            default:
                return;
        }
        controller.setAsPopup();
        if (sourceFile != null) {
            controller.openFile(sourceFile);
        } else {
            controller.mainArea.setText(mainArea.getText());
        }
    }

    protected void setAsPopup() {
        baseName = baseName + "Popup";
        topCheck.setVisible(true);
        hideLeftPane();
        hideRightPane();
        autoSaveCheck.setSelected(false);
        checkAlwaysTop();
    }

    @FXML
    public void popPanesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            CheckMenuItem updateMenu = new CheckMenuItem(message("UpdateSynchronously"));
            updateMenu.setSelected(AppVariables.getUserConfigBoolean(baseName + "UpdateSynchronously", false));
            updateMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.setUserConfigValue(baseName + "UpdateSynchronously", updateMenu.isSelected());
                    if (updateMenu.isSelected()) {
                        updatePairArea();
                    }
                }
            });
            popMenu.getItems().add(updateMenu);

            CheckMenuItem scrollMenu = new CheckMenuItem(message("ScrollSynchronously"));
            scrollMenu.setSelected(AppVariables.getUserConfigBoolean(baseName + "ScrollSynchronously", false));
            scrollMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.setUserConfigValue(baseName + "ScrollSynchronously", scrollMenu.isSelected());
                    if (scrollMenu.isSelected()) {
                        pairArea.setScrollLeft(mainArea.getScrollLeft());
                        pairArea.setScrollTop(mainArea.getScrollTop());
                    }
                }
            });
            popMenu.getItems().add(scrollMenu);

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVariables.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    @Override
    public void taskCanceled(Task task) {
        taskCanceled();
    }

    public void taskCanceled() {
        bottomLabel.setText("");
        if (backgroundTask != null && !backgroundTask.isQuit()) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
    }

    @Override
    public boolean leavingScene() {
        try {
            if (autoSaveTimer != null) {
                autoSaveTimer.cancel();
                autoSaveTimer = null;
            }

        } catch (Exception e) {
        }
        return super.leavingScene();
    }

}
