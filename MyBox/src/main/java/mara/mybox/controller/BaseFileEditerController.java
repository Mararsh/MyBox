package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
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
    protected String filterConditions = "";
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
    protected VBox editBox, pairBox, filtersTypeBox, findOptionsBox;
    @FXML
    protected TitledPane filePane, savePane, saveAsPane, bytesPane, findPane, filterPane,
            locatePane, encodePane, breakLinePane, paginatePane;
    @FXML
    protected TextArea mainArea, lineArea, pairArea;
    @FXML
    protected ComboBox<String> encodeBox, targetBox, pageSelector;
    @FXML
    protected ToggleGroup filterGroup, lineBreakGroup;
    @FXML
    protected CheckBox targetBomCheck, confirmCheck, autoSaveCheck, filterLineNumberCheck;
    @FXML
    protected ControlTimeLength autoSaveDurationController;
    @FXML
    protected Label editLabel, bomLabel, numbersLabel,
            pageLabel, charsetLabel, selectionLabel, filterCommentsLabel, savedLabel;
    @FXML
    protected Button panesMenuButton, charactersButton, linesButton, exampleFilterButton,
            filterButton, locateObjectButton, locateLineButton;
    @FXML
    protected TextField fromInput, toInput, pageSizeInput, filterInput,
            currentLineBreak, objectNumberInput, lineInput;
    @FXML
    protected RadioButton crlfRadio, lfRadio, crRadio;
    @FXML
    protected HBox pageBox, findBox, filterBox;
    @FXML
    protected ControlFindReplace findReplaceController;

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

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);

        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    public final void setBytesType() {
        editType = Edit_Type.Bytes;

        SourceFileType = VisitHistory.FileType.Bytes;
        SourcePathType = VisitHistory.FileType.Bytes;
        TargetPathType = VisitHistory.FileType.Bytes;
        TargetFileType = VisitHistory.FileType.Bytes;
        AddFileType = VisitHistory.FileType.Bytes;
        AddPathType = VisitHistory.FileType.Bytes;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Bytes);
        sourceExtensionFilter = CommonFxValues.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    public final void setMarkdownType() {
        editType = Edit_Type.Markdown;

        SourceFileType = VisitHistory.FileType.Markdown;
        SourcePathType = VisitHistory.FileType.Markdown;
        TargetPathType = VisitHistory.FileType.Markdown;
        TargetFileType = VisitHistory.FileType.Markdown;
        AddFileType = VisitHistory.FileType.Markdown;
        AddPathType = VisitHistory.FileType.Markdown;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Markdown);

        sourceExtensionFilter = CommonFxValues.MarkdownExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
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
            initLineBreakTab();
            initLocateTab();
            initDisplayTab();
            initFilterTab();
            initFindTab();
            initPageinateTab();
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
        if (okButton != null) {
            FxmlControl.setTooltip(okButton, new Tooltip(message("OK") + "\nF1 / CTRL+g"));
        }
        if (filtersTypeBox != null) {
            FxmlControl.setTooltip(filtersTypeBox, new Tooltip(message("FilterTypesComments")));
        }
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        if (event.getCode() != null
                && (event.isControlDown() || event.isAltDown())) {
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
        }
        if (findReplaceController != null && findPane.isExpanded()) {
            findReplaceController.keyEventsHandler(event);
        }
        super.keyEventsHandler(event);
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
            }
            sourceFile = file;
            sourceInformation = FileEditInformation.newEditInformation(editType, file);
            sourceInformation.setPageSize(AppVariables.getUserConfigInt(baseName + "PageSize", defaultPageSize));
            sourceInformation.setCurrentPage(pageNumber);
            targetInformation = FileEditInformation.newEditInformation(editType);

            mainArea.clear();
            lineArea.clear();
            clearPairArea();
            sourceInformation.setFindReplace(null);

            bottomLabel.setText("");
            numbersLabel.setText("");
            selectionLabel.setText("");
            if (pageBox != null) {
                pageBox.setVisible(false);
            }

            if (bomLabel != null) {
                bomLabel.setText("");
            }
            if (findBox != null) {
                findBox.setDisable(false);
            }
            if (filterBox != null) {
                filterBox.setDisable(false);
            }
//            if (recoverButton != null) {
//                recoverButton.setDisable(file == null);
//            }
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

            if (encodeBox != null) {
                if (charsetByUser) {
                    sourceInformation.setCharset(Charset.forName(encodeBox.getSelectionModel().getSelectedItem()));
                    if (targetBox == null) {
                        targetInformation.setCharset(sourceInformation.getCharset());
                    }
                } else {
                    encodeBox.getSelectionModel().select(defaultCharset().name());
                    encodeBox.setDisable(false);
                    if (targetBox != null) {
                        targetBox.getSelectionModel().select(defaultCharset().name());
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
                confirmCheck.setDisable(!autoSaveCheck.isDisabled() && autoSaveCheck.isSelected());
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

    protected void initDisplayTab() {
        try {
            if (bytesPane != null) {
                bytesPane.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "BytesPane", bytesPane.isExpanded());
                        });
                bytesPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "BytesPane", false));
            }
            if (encodeBox == null) {
                return;
            }
            Tooltip tips = new Tooltip(AppVariables.message("EncodeComments"));
            tips.setFont(new Font(16));
            FxmlControl.setTooltip(encodeBox, tips);

            List<String> setNames = TextTools.getCharsetNames();
            encodeBox.getItems().addAll(setNames);
            encodeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue,
                        String newValue) {
                    changeCurrentCharset();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void changeCurrentCharset() {

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
        boolean invalid = f.isEmpty() || sourceFile == null || mainArea.getText().isEmpty();
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
        popMenu = FxmlControl.popRegexExample(this, popMenu, filterInput, mouseEvent);
    }

    protected void initPageinateTab() {
        try {
            if (paginatePane != null) {
                paginatePane.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "PaginatePane", paginatePane.isExpanded());
                        });
                paginatePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "PaginatePane", false));
            }
            if (pageSizeInput != null) {
                pageSizeInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkPageSize();
                    }
                });
                int pageSize = AppVariables.getUserConfigInt(baseName + "PageSize", defaultPageSize);
                if (pageSize <= 0) {
                    pageSize = defaultPageSize;
                }
                pageSizeInput.setText(pageSize + "");
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
                    String pageText = mainArea.getText();
                    updateNumbers(pageText, true);
                    updatePairArea();
                    updateControls(true);
                }

            });
            mainArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (AppVariables.getUserConfigBoolean(baseName + "ScrollSynchronously", false)) {
                        scrollTopPairArea(newValue.doubleValue());
                    }
                    isSettingValues = true;
                    lineArea.setScrollTop(newValue.doubleValue());
                    isSettingValues = false;
                }
            });
            mainArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (AppVariables.getUserConfigBoolean(baseName + "ScrollSynchronously", false)) {
                        scrollLeftPairArea(newValue.doubleValue());
                    }
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

    protected void initPairBox() {
        try {
            if (pairArea == null) {
                return;
            }
            pairArea.setStyle("-fx-highlight-fill: black; -fx-highlight-text-fill: palegreen;");
            pairArea.setEditable(false);
            pairArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue,
                        Number newValue) {
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
                public void changed(ObservableValue ov, Number oldValue,
                        Number newValue) {
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
        long fileStart = 0, fileEnd = 0;
        if (sourceInformation != null) {
            fileStart = sourceInformation.getCurrentPageObjectStart();
            fileEnd = fileStart;
        }
        // end of range is *excluded* when handled internally, while it is *included* when displayed
        if (editType == Edit_Type.Bytes) {
            pageStart = currentSelection.getStart() / 3 + 1;
            pageEnd = currentSelection.getLength() == 0 ? pageStart : currentSelection.getEnd() / 3;
            fileStart += pageStart;
            fileEnd += pageEnd;

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
            fileStart += pageStart;
            fileEnd += pageEnd;
        }
        selectionLabel.setText(AppVariables.message("Selection") + ":"
                + (pageEnd - pageStart + 1) + "(" + pageStart + "-" + pageEnd + ")/"
                + (fileEnd - fileStart + 1) + "(" + fileStart + "-" + fileEnd + ")");

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
            if (pageSelector == null) {
                return;
            }
            pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues || !checkBeforeNextAction()) {
                        return;
                    }
                    if (pageBox.isDisabled() || !pageBox.isVisible() || pageSelector.getValue() == null) {
                        pageSelector.getSelectionModel().select(null);
                        pageSelector.getEditor().setStyle(null);
                        return;
                    }
                    try {
                        int v = Integer.valueOf(pageSelector.getValue());
                        if (v > 0 && v <= sourceInformation.getPagesNumber()) {
                            if (sourceInformation.getCurrentPage() != v) {
                                sourceInformation.setCurrentPage(v);
                                if (findReplaceController != null) {
                                    findReplaceController.lastFileRange = null;
                                }
                                loadPage();
                            }
                            pageSelector.getEditor().setStyle(null);
                        } else {
                            pageSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        pageSelector.getEditor().setStyle(badStyle);
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private int checkPageSize() {
        try {
            int v = Integer.valueOf(pageSizeInput.getText());
            if (v > 0) {
                pageSizeInput.setStyle(null);
                okButton.setDisable(false);
                return v;
            } else {
                pageSizeInput.setStyle(badStyle);
                okButton.setDisable(true);
            }
        } catch (Exception e) {
            pageSizeInput.setStyle(badStyle);
            okButton.setDisable(true);
        }
        return -1;
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
                || (pairArea != null && pairArea.getText().isBlank())) {
            refreshPairAction();
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

    protected void setMainArea(String text) {
        if (isSettingValues) {
            return;
        }
        mainArea.setText(text);
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
    @Override
    public void okAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        int pageSize = checkPageSize();
        if (pageSize <= 0) {
            return;
        }
        AppVariables.setUserConfigInt(baseName + "PageSize", pageSize);
        popInformation(AppVariables.message("Saved"), 3000);
        sourceInformation.setPageSize(pageSize);
        sourceInformation.setCurrentPage(1);
        if (sourceInformation.getLineBreak() == Line_Break.Width) {
            sourceInformation.setTotalNumberRead(false);
            openFile(sourceFile);
        } else {
            loadPage();
        }
    }

    @FXML
    protected void filterAction() {
        if (isSettingValues || filterButton.isDisabled()) {
            return;
        }
        if (!checkBeforeNextAction() || sourceFile == null
                || filterStrings == null || filterStrings.length == 0) {
            return;
        }
        sourceInformation.setFilterStrings(filterStrings);
        sourceInformation.setFilterType(filterType);
        final FileFilterController controller = (FileFilterController) openStage(CommonValues.FileFilterFxml);
        if (controller != null) {
            controller.filterFile(sourceInformation, filterConditions, filterLineNumberCheck.isSelected());
        }
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
    protected void locateLine() {
        sourceInformation.setCurrentLine(-1);
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            String[] lines = mainArea.getText().split("\n");
            if (lineLocation > lines.length) {
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
                    thread.setDaemon(true);
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
                    if (encodeBox != null) {
                        encodeBox.getSelectionModel().select(sourceInformation.getCharset().name());
                        if (targetBox != null) {
                            targetBox.getSelectionModel().select(sourceInformation.getCharset().name());
                        } else {
                            targetInformation.setCharset(sourceInformation.getCharset());
                        }
                        if (sourceInformation.isWithBom()) {
                            encodeBox.setDisable(true);
                            bomLabel.setText(AppVariables.message("WithBom"));
                            if (targetBomCheck != null) {
                                targetBomCheck.setSelected(true);
                            }
                        } else {
                            encodeBox.setDisable(false);
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
            thread.setDaemon(true);
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
                    updateNumbers(mainArea.getText(), false);
                }

            };
            backgroundTask.setSelf(backgroundTask);
            Thread thread = new Thread(backgroundTask);
            thread.setDaemon(true);
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

                        updateInterface(false);
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
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void updateInterface(boolean changed) {
        String pageText = mainArea.getText();

        updateNumbers(pageText, changed);

        updateCursor(pageText);

        updateControls(changed);

        updatePairArea();

    }

    protected void updateControls(boolean changed) {
        fileChanged.set(changed);
        bottomLabel.setText("");
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
        if (!formatMainArea()) {
            if (editLabel != null) {
                editLabel.setText(AppVariables.message("InvalidData"));
            }
            mainArea.setStyle(badStyle);
            return;
        } else if (editLabel != null) {
            editLabel.setText("");
        }
        mainArea.setStyle(null);
        if (okButton != null) {
            okButton.setDisable(changed);
        }
        if (autoSaveCheck != null) {
            autoSaveCheck.setDisable(sourceFile == null);
        }
        if (!changed && savedLabel != null) {
            savedLabel.setText(message("Load") + ": " + DateTools.nowString());
        }
    }

    protected synchronized void updateCursor(String pageText) {
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

    protected void updateNumbers(String pageText, boolean changed) {
        int pageObjectsNumber = pageText.length();
        int linesNumber = FindReplaceString.count(pageText, "\n") + 1;
        String objectNumberName, objectName;
        if (editType == Edit_Type.Bytes) {
            pageObjectsNumber = pageText.length() / 3;
            objectName = AppVariables.message("Bytes");
            objectNumberName = AppVariables.message("BytesNumber");
        } else {
            if (sourceInformation.getLineBreak().equals(Line_Break.CRLF)) {
                pageObjectsNumber += linesNumber - 1;
            }
            objectName = AppVariables.message("Characters");
            objectNumberName = AppVariables.message("CharactersNumber");
        }
        saveButton.setDisable(false);
        if (saveAsButton != null) {
            saveAsButton.setDisable(false);
        }
        if (pageBox != null) {
            pageBox.setVisible(false);
        }
        if (sourceFile == null) {
            if (pageLabel != null) {
                pageLabel.setText("");
            }
            setLines(1, linesNumber);
            sourceInformation.setObjectsNumber(pageObjectsNumber);
            sourceInformation.setLinesNumber(linesNumber);
            numbersLabel.setText(objectNumberName + ":" + pageObjectsNumber + "\n"
                    + AppVariables.message("LinesNumber") + ":" + linesNumber);
        } else {
            if (!sourceInformation.isTotalNumberRead()) {
                saveButton.setDisable(true);
                if (saveAsButton != null) {
                    saveAsButton.setDisable(true);
                }
                setLines(sourceInformation.getCurrentPageLineStart(), sourceInformation.getCurrentPageLineEnd());
                numbersLabel.setText(objectName + ":"
                        + (sourceInformation.getCurrentPageObjectEnd() - sourceInformation.getCurrentPageObjectStart()) + "("
                        + (sourceInformation.getCurrentPageObjectStart() + 1) + "-" + sourceInformation.getCurrentPageObjectEnd() + ")  "
                        + AppVariables.message("CountingTotalNumber"));
                if (locateObjectButton != null) {
                    locateObjectButton.setDisable(true);
                    locateLineButton.setDisable(true);
                }
            } else {
                int pagesNumber = (int) (sourceInformation.getObjectsNumber() / sourceInformation.getPageSize());
                if (sourceInformation.getObjectsNumber() % sourceInformation.getPageSize() > 0) {
                    pagesNumber++;
                }
                sourceInformation.setPagesNumber(pagesNumber);
                if (pagesNumber > 1) {
                    if (filterBox != null) {
                        filterBox.setDisable(changed);
                    }
                    if (encodeBox != null) {
                        encodeBox.setDisable(changed || sourceInformation.isWithBom());
                    }
                    if (locateObjectButton != null) {
                        locateObjectButton.setDisable(changed);
                        locateLineButton.setDisable(changed);
                    }
                    if (pageBox != null) {
                        pageBox.setVisible(true);
                        pageBox.setDisable(changed);
                        pagePreviousButton.setDisable(sourceInformation.getCurrentPage() <= 1);
                        pageNextButton.setDisable(sourceInformation.getObjectsNumber() <= sourceInformation.getCurrentPageObjectEnd());
                        List<String> pages = new ArrayList<>();
                        for (int i = 1; i <= pagesNumber; i++) {
                            pages.add(i + "");
                        }
                        isSettingValues = true;
                        pageSelector.getItems().clear();
                        pageSelector.getItems().setAll(pages);
                        pageLabel.setText("/" + pagesNumber);
                        pageSelector.setValue(sourceInformation.getCurrentPage() + "");
                        isSettingValues = false;
                    }
                }

                if (!changed) {
                    setLines(sourceInformation.getCurrentPageLineStart(), sourceInformation.getCurrentPageLineEnd());
                    numbersLabel.setText(objectName + ":"
                            + (sourceInformation.getCurrentPageObjectEnd() - sourceInformation.getCurrentPageObjectStart()) + "("
                            + (sourceInformation.getCurrentPageObjectStart() + 1) + "-" + sourceInformation.getCurrentPageObjectEnd() + ")/"
                            + sourceInformation.getObjectsNumber() + "\n"
                            + AppVariables.message("Lines") + ":"
                            + (sourceInformation.getCurrentPageLineEnd() - sourceInformation.getCurrentPageLineStart() + 1) + "("
                            + sourceInformation.getCurrentPageLineStart() + "-" + sourceInformation.getCurrentPageLineEnd() + ")/"
                            + sourceInformation.getLinesNumber());
                } else {
                    long charsTo = sourceInformation.getCurrentPageObjectStart() + pageObjectsNumber;
                    long charsTotal = sourceInformation.getObjectsNumber()
                            + (pageObjectsNumber - (sourceInformation.getCurrentPageObjectEnd() - sourceInformation.getCurrentPageObjectStart()));
                    long linesTo = sourceInformation.getCurrentPageLineStart() + linesNumber - 1;
                    long linesTotal = sourceInformation.getLinesNumber()
                            + (linesNumber - (sourceInformation.getCurrentPageLineEnd() - sourceInformation.getCurrentPageLineStart() + 1));
                    setLines(sourceInformation.getCurrentPageLineStart(), linesTo);
                    numbersLabel.setText(objectName + ":"
                            + pageObjectsNumber + "(" + (sourceInformation.getCurrentPageObjectStart() + 1) + "-" + charsTo + ")/"
                            + charsTotal + "  " + AppVariables.message("Lines") + ":" + linesNumber + "("
                            + sourceInformation.getCurrentPageLineStart() + "-" + linesTo + ")/" + linesTotal);
                }
            }
        }
    }

    protected boolean formatMainArea() {
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

    @FXML
    @Override
    public void saveAction() {
        recordCursor();
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
        recordFileWritten(file);
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
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    charsetByUser = false;
                    openFile(file);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void saveExisted() {
        if (!confirmCheck.isDisabled() && confirmCheck.isSelected() && (autoSaveTimer == null)) {
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
                    ok = targetInformation.writePage(sourceInformation, mainArea.getText());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    openFile(sourceFile);
                }

                @Override
                protected void whenFailed() {
                    updateInterface(false);
                    super.whenFailed();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        String name = null;
        if (sourceFile != null) {
            name = FileTools.getFilePrefix(sourceFile.getName());
        }
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);

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
                    ok = targetInformation.writePage(sourceInformation, mainArea.getText());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (saveAsType == SaveAsType.Load) {
                        openFile(file);
                    } else if (saveAsType == SaveAsType.Open) {
                        BaseFileEditerController controller = openNewStage();
                        controller.openFile(file);
                    }
                    popSuccessful();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            initPage(null);
            updateInterface(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

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
