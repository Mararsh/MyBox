package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.FindReplaceString;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditorController extends BaseFileEditorController_Assist {

    public BaseFileEditorController() {
        baseTitle = Languages.message("FileEditer");
    }

    public BaseFileEditorController(Edit_Type editType) {
        baseTitle = Languages.message("FileEditer");
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
                findReplaceController.setEditor(this);
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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (findReplaceController != null) {
                if (sourceInformation != null && sourceInformation.getEditType() == Edit_Type.Bytes) {
                    NodeStyleTools.setTooltip(findReplaceController.tipsView, new Tooltip(Languages.message("FindReplaceBytesTips")));
                } else {
                    NodeStyleTools.setTooltip(findReplaceController.tipsView, new Tooltip(Languages.message("FindReplaceTextsTips")));
                }
            }

            if (encodeSelector != null) {
                NodeStyleTools.setTooltip(encodeSelector, new Tooltip(Languages.message("EncodeComments")));
            }
            if (targetBomCheck != null) {
                NodeStyleTools.setTooltip(targetBomCheck, new Tooltip(Languages.message("BOMcomments")));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean controlAltFilter(KeyEvent event) {
        if (event.getCode() == null) {
            return false;
        }
        if (findReplaceController != null && findPane != null) {
            switch (event.getCode()) {
                case DIGIT1:
                case DIGIT2:
                case F:
                case H:
                case W:
                    if (leftPaneControl != null) {
                        showLeftPane();
                    }
                    if (findPane.isExpanded()) {
                        findReplaceController.keyEventsFilter(event);
                    } else {
                        findPane.setExpanded(true);
                        findReplaceController.findArea.requestFocus();
                    }
                    return true;
            }
        }
        return super.controlAltFilter(event);
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
                savePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "SavePane", savePane.isExpanded());
                });
                savePane.setExpanded(UserConfig.getBoolean(baseName + "SavePane", false));
            }
            if (confirmCheck != null) {
                confirmCheck.setSelected(UserConfig.getBoolean(baseName + "ConfirmSave", true));
                confirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ConfirmSave", newValue);
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
                autoSaveCheck.setSelected(UserConfig.getBoolean(baseName + "AutoSave", true));
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
            backupPane.setExpanded(UserConfig.getBoolean(baseName + "BackupPane", false));
            backupPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "BackupPane", backupPane.isExpanded());
            });

            backupController.setControls(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSaveAsTab() {
        try {
            if (saveAsPane != null) {
                saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "SaveAsPane", saveAsPane.isExpanded());
                });
                saveAsPane.setExpanded(UserConfig.getBoolean(baseName + "SaveAsPane", false));
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
            UserConfig.setBoolean(baseName + "AutoSave", autoSaveCheck.isSelected());
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
                        popInformation(Languages.message("Saving"));
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
            breakLinePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "BreakLinePane", breakLinePane.isExpanded());
            });
            breakLinePane.setExpanded(UserConfig.getBoolean(baseName + "BreakLinePane", false));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initCharsetTab() {
        try {
            if (encodePane == null) {
                encodePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "EncodePane", encodePane.isExpanded());
                });
                encodePane.setExpanded(UserConfig.getBoolean(baseName + "EncodePane", true));
            }
            if (encodeSelector != null) {
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
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFilterTab() {
        try {
            if (filterPane != null) {
                filterPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "FilterPane", filterPane.isExpanded());
                });
                filterPane.setExpanded(UserConfig.getBoolean(baseName + "FilterPane", false));
            }

            if (filterButton != null && filterController != null) {
                filterButton.disableProperty().bind(filterController.valid.not());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initLocateTab() {
        try {
            if (locatePane != null) {
                locatePane.setExpanded(UserConfig.getBoolean(baseName + "LocatePane", false));
                locatePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "LocatePane", locatePane.isExpanded());
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
                                lineInput.setStyle(NodeStyleTools.badStyle);
                                locateLineButton.setDisable(true);
                            }
                        } catch (Exception e) {
                            lineInput.setStyle(NodeStyleTools.badStyle);
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
                                objectNumberInput.setStyle(NodeStyleTools.badStyle);
                                locateObjectButton.setDisable(true);
                            }
                        } catch (Exception e) {
                            objectNumberInput.setStyle(NodeStyleTools.badStyle);
                            locateObjectButton.setDisable(true);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initFindTab() {
        try {
            if (findReplaceController == null) {
                return;
            }
            findPane.setExpanded(UserConfig.getBoolean(baseName + "FindPane", false));
            findPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "FindPane", findPane.isExpanded());
            });

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
                    if (UserConfig.getBoolean(baseName + "ScrollSynchronously", false)) {
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
                    if (UserConfig.getBoolean(baseName + "ScrollSynchronously", false)) {
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
        String info = Languages.message("SelectionInPage") + ": "
                + StringTools.format(pageStart) + " - " + StringTools.format(pageEnd)
                + " (" + StringTools.format(currentSelection.getLength() == 0 ? 0 : pageEnd - pageStart + 1) + ")";
        if (sourceInformation != null
                && sourceInformation.getPagesNumber() > 1 && sourceInformation.getCurrentPage() > 1) {
            long fileStart = sourceInformation.getCurrentPageObjectStart() + pageStart;
            long fileEnd = sourceInformation.getCurrentPageObjectStart() + pageEnd;
            info += "  " + Languages.message("SelectionInFile") + ": "
                    + StringTools.format(fileStart) + " - " + StringTools.format(fileEnd);
        }
        selectionLabel.setText(info);
    }

    protected void initToolBar() {
        try {
            if (topCheck != null) {
                topCheck.setVisible(false);
                topCheck.setSelected(UserConfig.getBoolean(baseName + "Top", true));
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
            int pageSize = UserConfig.getInt(baseName + "PageSize", defaultPageSize);
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
                popError(Languages.message("MayOutOfMemory"));
                v = available;
            } else if (v <= 0) {
                pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                popError(Languages.message("InvalidParameters"));
                return;
            }
            pageSizeSelector.getEditor().setStyle(null);
            UserConfig.setInt(baseName + "PageSize", v);
            sourceInformation.setPageSize(v);
            sourceInformation.setCurrentPage(1);
            if (sourceInformation.getLineBreak() == Line_Break.Width) {
                sourceInformation.setTotalNumberRead(false);
                openFile(sourceFile);
            } else {
                loadPage();
            }
        } catch (Exception e) {
            pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            popError(Languages.message("InvalidParameters"));
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

    @FXML
    @Override
    public void myBoxClipBoard() {
        TextClipboardPopController.open(this, mainArea);
    }

    @FXML
    public void popPanesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            CheckMenuItem updateMenu = new CheckMenuItem(Languages.message("UpdateSynchronously"));
            updateMenu.setSelected(UserConfig.getBoolean(baseName + "UpdateSynchronously", false));
            updateMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "UpdateSynchronously", updateMenu.isSelected());
                    if (updateMenu.isSelected()) {
                        updatePairArea();
                    }
                }
            });
            popMenu.getItems().add(updateMenu);

            CheckMenuItem scrollMenu = new CheckMenuItem(Languages.message("ScrollSynchronously"));
            scrollMenu.setSelected(UserConfig.getBoolean(baseName + "ScrollSynchronously", false));
            scrollMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ScrollSynchronously", scrollMenu.isSelected());
                    if (scrollMenu.isSelected()) {
                        pairArea.setScrollLeft(mainArea.getScrollLeft());
                        pairArea.setScrollTop(mainArea.getScrollTop());
                    }
                }
            });
            popMenu.getItems().add(scrollMenu);

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
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
    @Override
    public void menuAction() {
        Point2D localToScreen = mainArea.localToScreen(mainArea.getWidth() - 80, 80);
        MenuTextEditController.open(myController, mainArea, localToScreen.getX(), localToScreen.getY());
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (isPop || fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
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
    public void cleanPane() {
        try {
            if (autoSaveTimer != null) {
                autoSaveTimer.cancel();
                autoSaveTimer = null;
            }

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
