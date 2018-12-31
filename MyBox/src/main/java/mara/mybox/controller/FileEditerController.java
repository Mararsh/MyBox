package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileEditInformation;
import mara.mybox.objects.FileEditInformation.Line_Break;
import mara.mybox.objects.FileEditInformationFactory;
import mara.mybox.objects.FileEditInformationFactory.Edit_Type;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class FileEditerController extends BaseController {

    protected Edit_Type editType;
    protected String FilePathKey, DisplayKey, lineNumbersKey;
    protected int currentFound, currentPageTmp, pageSize, currentPage;
    protected SimpleBooleanProperty fileChanged;
    protected boolean isSettingValues, filterInclude, findWhole, charsetByUser;
    protected FileEditInformation sourceInformation, targetInformation;
    protected SaveAsType saveAsType;
    protected TextArea displayArea;
    protected String filterConditions = "";

    protected enum Action {
        None, FindFirst, FindNext, FindPrevious, FindLast, Replace, ReplaceAll, Filter,
        SetPageSize, NextPage, PreviousPage, FirstPage, LastPage, GoPage
    }

    public enum SaveAsType {
        Load, Open, None
    }

    @FXML
    protected AnchorPane mainPane;
    @FXML
    protected TextArea mainArea, lineArea;
    @FXML
    protected ComboBox<String> currentBox, targetBox;
    @FXML
    protected ToggleGroup saveAsGroup, filterGroup, targetLineGroup, findGroup;
    @FXML
    protected CheckBox displayCheck, targetBomCheck, confirmCheck, scrollCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected Label bomLabel, pageLabel, charsetLabel;
    @FXML
    protected Button openButton, createButton, saveButton, charactersButton, linesButton,
            redoButton, undoButton, deleteButton, cutButton, copyButton, pasteButton, selectAllButton,
            findFirstButton, findPreviousButton, findNextButton, findLastButton,
            replaceButton, replaceAllButton, filterButton, infoButton, pageSizeButton,
            firstPageButton, perviousPageButton, nextPageButton, lastPageButton, pageGoButton;
    @FXML
    protected TextField fromInput, toInput, pageSizeInput, pageInput, filterInput, findInput, replaceInput,
            currentLineBreak, bottomText;
    @FXML
    protected ToolBar pageBar;
    @FXML
    protected RadioButton crlfRadio, lfRadio, crRadio, wholeFileRadio, currentPageRadio;
    @FXML
    protected HBox saveAsBox, findBox, filterBox;

    public FileEditerController() {
        editType = Edit_Type.Text;

        FilePathKey = "FilePathKey";
        DisplayKey = "EditerDisplayHex";
        lineNumbersKey = "ShowLineNumbers";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*", "*.*"));
                add(new FileChooser.ExtensionFilter("txt", "*.txt"));
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };
    }

    @Override
    protected void initializeNext() {
        try {
            initPage(null);

            initFileTab();
            initEditBar();
            initLineBreakTab();
            initDisplayTab();
            initFilterTab();
            initReplaceTab();
            initPageinateTab();
            initMainArea();
            initPageBar();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initPage(File file) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        if (backgroundTask != null && backgroundTask.isRunning()) {
            backgroundTask.cancel();
        }
        isSettingValues = true;
        fileChanged = new SimpleBooleanProperty(false);
        int fileCurrentFound = -1;
        String findString = null;
        if (sourceFile != null && sourceFile == file) {
            fileCurrentFound = sourceInformation.getCurrentFound();
            findString = sourceInformation.getFindString();
            currentPage = sourceInformation.getCurrentPage();
        } else {
            currentPage = 1;
        }
        sourceFile = file;
        sourceInformation = FileEditInformationFactory.newEditInformation(editType, file);
        sourceInformation.setPageSize(AppVaribles.getUserConfigInt("TextPageSize", 100000));
        sourceInformation.setCurrentPage(currentPage);
        if (fileCurrentFound >= 0) {
            sourceInformation.setCurrentFound(fileCurrentFound);
        }
        sourceInformation.setFindString(findString);
        targetInformation = FileEditInformationFactory.newEditInformation(editType);
        currentFound = -1;
        mainArea.clear();
        if (displayArea != null) {
            displayArea.clear();
        }
        lineArea.clear();
        bottomText.setText("");
        pageBar.setDisable(true);
        pageInput.setText("");
        pageInput.setStyle(null);
        pageLabel.setText("");
        if (bomLabel != null) {
            bomLabel.setText("");
        }
        if (infoButton != null) {
            infoButton.setDisable(true);
        }
        if (currentLineBreak != null) {
            switch (System.lineSeparator()) {
                case "\r\n":
                    crlfRadio.fire();
                    currentLineBreak.setText("CRLF");
                    sourceInformation.setLineBreak(Line_Break.CRLF);
                    break;
                case "\r":
                    crRadio.fire();
                    currentLineBreak.setText("CR");
                    sourceInformation.setLineBreak(Line_Break.CR);
                    break;
                default:
                    lfRadio.fire();
                    currentLineBreak.setText("LF");
                    sourceInformation.setLineBreak(Line_Break.LF);
                    break;
            }
        }
        if (currentBox != null) {
            if (charsetByUser) {
                sourceInformation.setCharset(Charset.forName(currentBox.getSelectionModel().getSelectedItem()));
                if (targetBox == null) {
                    targetInformation.setCharset(sourceInformation.getCharset());
                }
            } else {
                currentBox.getSelectionModel().select(Charset.defaultCharset().name());
                currentBox.setDisable(false);
                if (targetBox != null) {
                    targetBox.getSelectionModel().select(Charset.defaultCharset().name());
                } else {
                    targetInformation.setCharset(Charset.defaultCharset());
                }
            }
        }
        if (wholeFileRadio != null) {
            if (file == null) {
                wholeFileRadio.setDisable(true);
                currentPageRadio.fire();
            } else {
                wholeFileRadio.setDisable(false);
            }
        }
        isSettingValues = false;
        mainArea.requestFocus();
    }

    protected void initFileTab() {
        confirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                AppVaribles.setUserConfigValue("TextEditerConfirmSave", newValue);
            }
        });
        confirmCheck.setSelected(AppVaribles.getUserConfigBoolean("TextEditerConfirmSave", true));

        saveAsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSaveAsType();
            }
        });

        checkSaveAsType();
    }

    protected void checkSaveAsType() {
        try {
            RadioButton selected = (RadioButton) saveAsGroup.getSelectedToggle();
            if (AppVaribles.getMessage("LoadAfterSaveAs").equals(selected.getText())) {
                saveAsType = SaveAsType.Load;
            } else if (AppVaribles.getMessage("OpenAfterSaveAs").equals(selected.getText())) {
                saveAsType = SaveAsType.Open;
            } else if (AppVaribles.getMessage("JustSaveAs").equals(selected.getText())) {
                saveAsType = SaveAsType.None;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initEditBar() {
        Tooltip tips = new Tooltip("CTRL+c");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(copyButton, tips);

        tips = new Tooltip("CTRL+v");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(pasteButton, tips);

        tips = new Tooltip("CTRL+y");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(redoButton, tips);

        tips = new Tooltip("CTRL+z");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(undoButton, tips);

        tips = new Tooltip("CTRL+a");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(selectAllButton, tips);

        tips = new Tooltip("CTRL+x");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(cutButton, tips);

        tips = new Tooltip("CTRL+d");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(deleteButton, tips);

        tips = new Tooltip("CTRL+s");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(saveButton, tips);
    }

    protected void initLineBreakTab() {
        targetLineGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkTargetLineGroup();
            }
        });

    }

    protected void checkTargetLineGroup() {
        try {
            RadioButton selected = (RadioButton) targetLineGroup.getSelectedToggle();
            if (AppVaribles.getMessage("LF").equals(selected.getText())) {
                targetInformation.setLineBreak(Line_Break.LF);
            } else if (AppVaribles.getMessage("CR").equals(selected.getText())) {
                targetInformation.setLineBreak(Line_Break.CR);
            } else if (AppVaribles.getMessage("CRLF").equals(selected.getText())) {
                targetInformation.setLineBreak(Line_Break.CRLF);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initCharsetTab() {
    }

    protected void changeCurrentCharset() {

    }

    protected void initDisplayTab() {
        displayCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                checkDisplay();
            }
        });
        isSettingValues = true;
        displayCheck.setSelected(AppVaribles.getUserConfigBoolean(DisplayKey, true));
        isSettingValues = false;
        checkDisplay();

        scrollCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (displayArea != null && newValue) {
                    displayArea.setScrollLeft(mainArea.getScrollLeft());
                    displayArea.setScrollTop(mainArea.getScrollTop());
                }
            }
        });

    }

    protected void checkDisplay() {
        setDisplayPane();
        scrollCheck.setDisable(!displayCheck.isSelected());
        AppVaribles.setUserConfigValue(DisplayKey, displayCheck.isSelected());
    }

    protected void initFilterTab() {

        Tooltip tips = new Tooltip(AppVaribles.getMessage("SeparateBySpace"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(filterInput, tips);

        filterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFilter();
            }
        });

        filterInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkFilter();
            }
        });
        checkFilter();

    }

    protected void checkFilter() {
        RadioButton selected = (RadioButton) filterGroup.getSelectedToggle();
        filterInclude = AppVaribles.getMessage("IncludeOneOf").equals(selected.getText());
        String f = filterInput.getText().trim();
        boolean invalid = f.isEmpty() || sourceFile == null || mainArea.getText().isEmpty();
        if (!invalid && f.length() >= sourceInformation.getPageSize()) {
            popError(AppVaribles.getMessage("FindStringLimitation"));
            invalid = true;
        }
        filterButton.setDisable(invalid);
    }

    protected void initReplaceTab() {

        Tooltip tips = new Tooltip("CTRL+f");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(findFirstButton, tips);

        tips = new Tooltip("CTRL+n");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(findNextButton, tips);

        tips = new Tooltip("CTRL+p");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(findPreviousButton, tips);

        tips = new Tooltip("CTRL+r");
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(replaceButton, tips);

        findGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFindType();
            }
        });
        checkFindType();

        findInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                boolean invalid = newValue.isEmpty() || mainArea.getText().isEmpty();
                if (newValue.length() >= pageSize) {
                    popError(AppVaribles.getMessage("FindStringLimitation"));
                    invalid = true;
                }
                findFirstButton.setDisable(invalid);
                findLastButton.setDisable(invalid);
                findPreviousButton.setDisable(true);
                findNextButton.setDisable(true);
                replaceButton.setDisable(true);
                replaceAllButton.setDisable(invalid);
                if (!invalid) {
                    sourceInformation.setFindString(newValue);
                }
                currentFound = -1;
                sourceInformation.setCurrentFound(-1);
            }
        });
    }

    protected void checkFindType() {
        RadioButton selected = (RadioButton) findGroup.getSelectedToggle();
        findWhole = AppVaribles.getMessage("WholeFile").equals(selected.getText());
    }

    protected void initPageinateTab() {

    }

    protected void initMainArea() {
        mainArea.setStyle("-fx-highlight-fill: dodgerblue; -fx-highlight-text-fill: white;");

        mainArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                if (!isSettingValues) {
                    updateInterface(true);
                }
            }
        });
        mainArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                if (isSettingValues) {
                    return;
                }
                if (displayArea != null && scrollCheck.isSelected()) {
                    isSettingValues = true;
                    displayArea.setScrollTop(newValue.doubleValue());
                    isSettingValues = false;
                }
                isSettingValues = true;
                lineArea.setScrollTop(newValue.doubleValue());
                isSettingValues = false;
            }
        });
        mainArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                if (!isSettingValues && displayArea != null && scrollCheck.isSelected()) {
                    isSettingValues = true;
                    displayArea.setScrollLeft(newValue.doubleValue());
                    isSettingValues = false;
                }
            }
        });
        mainArea.selectionProperty().addListener(new ChangeListener<IndexRange>() {
            @Override
            public void changed(ObservableValue ov, IndexRange oldValue, IndexRange newValue) {
                if (!isSettingValues) {
                    setSecondAreaSelection();
                    boolean none = (newValue.getLength() == 0);
                    deleteButton.setDisable(none);
                    cutButton.setDisable(none);
                    copyButton.setDisable(none);
                }
            }
        });

    }

    protected void initPageBar() {
        pageSizeInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkPageSize();
            }
        });
        pageSize = AppVaribles.getUserConfigInt("TextPageSize", 100000);
        if (pageSize <= 0) {
            pageSize = 100000;
        }
        pageSizeInput.setText(pageSize + "");

        pageInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkCurrentPage();
            }
        });

    }

    private void checkPageSize() {
        try {
            int v = Integer.valueOf(pageSizeInput.getText());
            if (v > 0) {
                pageSize = v;
                pageSizeInput.setStyle(null);
            } else {
                pageSizeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pageSizeInput.setStyle(badStyle);
        }
    }

    protected void checkCurrentPage() {
        if (pageBar.isDisabled()) {
            currentPageTmp = 0;
            pageInput.setStyle(null);
            return;
        }
        try {
            int v = Integer.valueOf(pageInput.getText());
            if (v > 0 && v <= sourceInformation.getPagesNumber()) {
                currentPageTmp = v;
                pageInput.setStyle(null);
                pageGoButton.setDisable(false);
            } else {
                pageInput.setStyle(badStyle);
                pageGoButton.setDisable(true);
            }
        } catch (Exception e) {
            pageInput.setStyle(badStyle);
            pageGoButton.setDisable(true);
        }
    }

    protected void setDisplayPane() {
        if (displayCheck.isSelected()) {
            if (displayArea == null) {
                displayArea = new TextArea();
                displayArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(displayArea, Priority.ALWAYS);
                HBox.setHgrow(displayArea, Priority.ALWAYS);
                displayArea.setStyle("-fx-highlight-fill: black; -fx-highlight-text-fill: palegreen;");
                displayArea.setEditable(false);
                displayArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        if (!isSettingValues && scrollCheck.isSelected()) {
                            isSettingValues = true;
                            mainArea.setScrollTop(newValue.doubleValue());
                            isSettingValues = false;
                        }
                    }
                });
            }
            if (!splitPane.getItems().contains(displayArea)) {
                splitPane.getItems().add(displayArea);
            }

            setSecondArea(mainArea.getText());

        } else {
            if (displayArea != null && splitPane.getItems().contains(displayArea)) {
                splitPane.getItems().remove(displayArea);
            }
        }

        switch (splitPane.getItems().size()) {
            case 3:
                splitPane.getDividers().get(0).setPosition(0.33333);
                splitPane.getDividers().get(1).setPosition(0.66666);
//                splitPane.setDividerPositions(0.33, 0.33, 0.33); // This way not work!
                break;
            case 2:
                splitPane.getDividers().get(0).setPosition(0.5);
//               splitPane.setDividerPositions(0.5, 0.5); // This way not work!
                break;
            default:
                splitPane.setDividerPositions(1);
                break;
        }
        splitPane.layout();
    }

    protected void setLines(int from, int to) {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        if (from < 0 || to <= 0 || from > to) {
            lineArea.clear();
        } else {
            StringBuilder lines = new StringBuilder();
            for (int i = from; i <= to; i++) {
                lines.append(i).append("\n");
            }
            lineArea.setText(lines.toString());
        }
        isSettingValues = false;
    }

    protected void setSecondArea(String contents) {

    }

    protected void setSecondAreaSelection() {

    }

    @FXML
    protected void copyAction() {
        if (!copyButton.isDisabled()) {
            mainArea.copy();
        }
    }

    @FXML
    protected void pasteAction() {
        if (!pasteButton.isDisabled()) {
            mainArea.paste();
        }
    }

    @FXML
    protected void cutAction() {
        if (!cutButton.isDisabled()) {
            mainArea.cut();
        }
    }

    @FXML
    protected void deleteAction() {
        if (!deleteButton.isDisabled()) {
            mainArea.deleteText(mainArea.getSelection());
        }
    }

    @FXML
    protected void selectAllAction() {
        if (!selectAllButton.isDisabled()) {
            mainArea.selectAll();
        }
    }

    @FXML
    protected void redoAction() {
        if (!redoButton.isDisabled()) {
            mainArea.redo();
        }
    }

    @FXML
    protected void undoAction() {
        if (!undoButton.isDisabled()) {
            mainArea.undo();
        }
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        if (event.isControlDown()) {
            switch (key) {
                case "c":
                case "C":
                    if (mainArea.isFocused() && !copyButton.isDisabled()) {
                        copyAction();
                    }
                    break;
                case "v":
                case "V":
                    if (mainArea.isFocused() && !pasteButton.isDisabled()) {
                        pasteAction();
                    }
                    break;
                case "z":
                case "Z":
                    if (mainArea.isFocused() && !undoButton.isDisabled()) {
                        undoAction();
                    }
                    break;
                case "y":
                case "Y":
                    if (mainArea.isFocused() && !redoButton.isDisabled()) {
                        redoAction();
                    }
                    break;
                case "a":
                case "A":
                    if (mainArea.isFocused() && !selectAllButton.isDisabled()) {
                        selectAllAction();
                    }
                    break;
                case "x":
                case "X":
                    if (mainArea.isFocused() && !cutButton.isDisabled()) {
                        cutAction();
                    }
                    break;
                case "d":
                case "D":
                    if (mainArea.isFocused() && !deleteButton.isDisabled()) {
                        deleteAction();
                    }
                    break;
                case "s":
                case "S":
                    if (!saveButton.isDisabled()) {
                        saveAction();
                    }
                    break;
                case "f":
                case "F":
                    if (!findFirstButton.isDisabled()) {
                        findFirstAction();
                    }
                    break;
                case "n":
                case "N":
                    if (!findNextButton.isDisabled()) {
                        findNextAction();
                    }
                    break;
                case "p":
                case "P":
                    if (!findPreviousButton.isDisabled()) {
                        findPreviousAction();
                    }
                    break;
                case "r":
                case "R":
                    if (!replaceButton.isDisabled()) {
                        replaceAction();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @FXML
    protected void pageSizeAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        AppVaribles.setUserConfigInt("TextPageSize", pageSize);
        popInformation(AppVaribles.getMessage("Saved"), 3000);
        sourceInformation.setPageSize(pageSize);
        sourceInformation.setCurrentPage(1);
        loadPage();
    }

    @FXML
    protected void findFirstAction() {
        if (findFirstButton.isDisabled()) {
            return;
        }
        final String findString = findInput.getText();
        if (findString.isEmpty()) {
            return;
        }
        final boolean whole = findWhole && (sourceInformation.getPagesNumber() > 1);
        if (whole) {
            if (!checkSavingForNextAction()) {
                return;
            }
        }
        sourceInformation.setFindString(findString);
        currentFound = -1;
        final String areaText = mainArea.getText();
        task = new Task<Void>() {
            private String text;

            @Override
            protected Void call() throws Exception {
                if (whole) {
                    text = sourceInformation.findFirst();
                } else if (areaText != null) {
                    text = areaText;
                    currentFound = text.indexOf(findString);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (text == null || text.isEmpty()) {
                            findFirstButton.setDisable(true);
                        } else {
                            if (whole) {
                                currentPage = sourceInformation.getCurrentPage();
                                currentFound = sourceInformation.getCurrentFound() % sourceInformation.getPageSize();
                                if (currentFound > 0 && sourceInformation.getLineBreak() == Line_Break.CRLF) {
                                    currentFound -= TextTools.countNumber(text.substring(0, currentFound), "\n");
                                }
                                mainArea.setText(text);
                                updateInterface(false);
                            }
                        }
                        if (currentFound >= 0) {
                            mainArea.requestFocus();
                            mainArea.deselect();
                            mainArea.selectRange(currentFound, Math.min(text.length(), currentFound + findString.length()));
                            findPreviousButton.setDisable(true);
                            findNextButton.setDisable(false);
                            findLastButton.setDisable(false);
                            replaceButton.setDisable(false);
                            replaceAllButton.setDisable(false);
                        } else {
                            popInformation(AppVaribles.getMessage("NotFound"));
                            findPreviousButton.setDisable(true);
                            findNextButton.setDisable(true);
                            findLastButton.setDisable(true);
                            replaceButton.setDisable(true);
                            replaceAllButton.setDisable(true);
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void findNextAction() {
        if (findNextButton.isDisabled()) {
            return;
        }
        final String findString = findInput.getText();
        if (findString.isEmpty()) {
            return;
        }
        sourceInformation.setFindString(findString);
        final boolean whole = findWhole && (sourceInformation.getPagesNumber() > 1);
        if (whole) {
            if (!checkSavingForNextAction()) {
                return;
            }
            if (sourceInformation.getCurrentFound() < 0) {
                findFirstAction();
                return;
            }
        } else {
            if (currentFound < 0) {
                findFirstAction();
                return;
            }
        }
        final String areaText = mainArea.getText();
        task = new Task<Void>() {
            private String text;
            private int pos = -1;

            @Override
            protected Void call() throws Exception {
                if (whole) {
                    text = sourceInformation.findNext();
                } else if (areaText != null) {
                    text = areaText;
                    pos = text.indexOf(findString, currentFound + 1);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (text == null || text.isEmpty()) {
                            findNextButton.setDisable(true);
                        } else {
                            if (whole) {
                                mainArea.setText(text);
                                currentPage = sourceInformation.getCurrentPage();
                                pos = sourceInformation.getCurrentFound() % sourceInformation.getPageSize();
                                if (pos > 0 && sourceInformation.getLineBreak() == Line_Break.CRLF) {
                                    pos -= TextTools.countNumber(text.substring(0, pos), "\n");
                                }
                                updateInterface(false);
                            }
                        }
                        if (pos >= 0) {
                            currentFound = pos;
                            findPreviousButton.setDisable(false);
                            mainArea.deselect();
                            mainArea.selectRange(currentFound, Math.min(text.length(), currentFound + findString.length()));
                            replaceButton.setDisable(false);
                            replaceAllButton.setDisable(false);
                        } else {
                            findNextButton.setDisable(true);
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void findPreviousAction() {
        if (findPreviousButton.isDisabled()) {
            return;
        }
        final String findString = findInput.getText();
        if (findString.isEmpty()) {
            return;
        }
        final boolean whole = findWhole && (sourceInformation.getPagesNumber() > 1);
        if (whole) {
            if (!checkSavingForNextAction()) {
                return;
            }
            if (sourceInformation.getCurrentFound() < 0) {
                findFirstAction();
                return;
            }
        } else {
            if (currentFound < 0) {
                findFirstAction();
                return;
            }
        }
        sourceInformation.setFindString(findString);
        final String areaText = mainArea.getText();
        task = new Task<Void>() {
            private String text;
            private int pos = -1;

            @Override
            protected Void call() throws Exception {
                if (whole) {
                    text = sourceInformation.findPrevious();
                } else if (areaText != null) {
                    text = areaText;
                    text = text.substring(0, currentFound + findString.length() - 1);
                    pos = text.lastIndexOf(findString);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (text == null || text.isEmpty()) {
                            findPreviousButton.setDisable(true);
                        } else {
                            if (whole) {
                                mainArea.setText(text);
                                currentPage = sourceInformation.getCurrentPage();
                                pos = sourceInformation.getCurrentFound() % sourceInformation.getPageSize();
                                if (pos > 0 && sourceInformation.getLineBreak() == Line_Break.CRLF) {
                                    pos -= TextTools.countNumber(text.substring(0, pos), "\n");
                                }
                                updateInterface(false);
                            }
                        }
                        if (pos >= 0) {
                            currentFound = pos;
                            findNextButton.setDisable(false);
                            mainArea.deselect();
                            mainArea.selectRange(currentFound, Math.min(text.length(), currentFound + findString.length()));
                            replaceButton.setDisable(false);
                            replaceAllButton.setDisable(false);
                        } else {
                            findPreviousButton.setDisable(true);
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void findLastAction() {
        if (findLastButton.isDisabled()) {
            return;
        }
        final String findString = findInput.getText();
        if (findString.isEmpty()) {
            return;
        }
        final boolean whole = findWhole && (sourceInformation.getPagesNumber() > 1);
        if (whole) {
            if (!checkSavingForNextAction()) {
                return;
            }
        }
        sourceInformation.setFindString(findString);
        final String areaText = mainArea.getText();
        task = new Task<Void>() {
            private String text;
            private int pos = -1;

            @Override
            protected Void call() throws Exception {
                if (whole) {
                    text = sourceInformation.findLast();
                } else if (areaText != null) {
                    text = areaText;
                    pos = text.lastIndexOf(findString);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (text == null || text.isEmpty()) {
                            findLastButton.setDisable(true);
                        } else {
                            if (whole) {
                                mainArea.setText(text);
                                currentPage = sourceInformation.getCurrentPage();
                                pos = sourceInformation.getCurrentFound() % sourceInformation.getPageSize();
                                if (pos > 0 && sourceInformation.getLineBreak() == Line_Break.CRLF) {
                                    pos -= TextTools.countNumber(text.substring(0, pos), "\n");
                                }
                                updateInterface(false);
                            }
                        }
                        if (pos >= 0) {
                            currentFound = pos;
                            findPreviousButton.setDisable(false);
                            findNextButton.setDisable(true);
                            findFirstButton.setDisable(false);
                            mainArea.deselect();
                            mainArea.selectRange(currentFound, Math.min(text.length(), currentFound + findString.length()));
                            replaceButton.setDisable(false);
                            replaceAllButton.setDisable(false);
                        } else {
                            popInformation(AppVaribles.getMessage("NotFound"));
                            findPreviousButton.setDisable(true);
                            findNextButton.setDisable(true);
                            findFirstButton.setDisable(true);
                            findLastButton.setDisable(true);
                            replaceButton.setDisable(true);
                            replaceAllButton.setDisable(true);
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void replaceAction() {
        if (replaceButton.isDisabled()) {
            return;
        }
        final String findString = findInput.getText();
        if (findString.isEmpty() || currentFound < 0) {
            return;
        }
        final String replaceString = replaceInput.getText();
        sourceInformation.setFindString(findString);
        sourceInformation.setReplaceString(replaceString);
        boolean found = false;
        final boolean whole = findWhole && (sourceInformation.getPagesNumber() > 1);
        if (whole) {
            if (sourceInformation.getCurrentFound() >= 0) {
                currentFound = sourceInformation.getCurrentFound() % sourceInformation.getPageSize();
                int foundPage = sourceInformation.getCurrentFound() / sourceInformation.getPageSize();
                if (currentFound > 0) {
                    foundPage++;
                }
                if (foundPage == currentPage) {
                    found = true;
                }
            }
        } else {
            if (currentFound >= 0) {
                found = true;
            }
        }
        if (found) {
            final String text = mainArea.getText();
            task = new Task<Void>() {
                private String replaced;

                @Override
                protected Void call() throws Exception {
                    String bstr = text.substring(0, currentFound);
                    String astr = text.substring(currentFound + findString.length(), text.length());
                    replaced = bstr + replaceString + astr;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            mainArea.setText(replaced);
                            updateInterface(true);
                            mainArea.deselect();
                            mainArea.selectRange(currentFound, currentFound + replaceString.length());
                            replaceButton.setDisable(true);
                        }
                    });
                    return null;
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } else {
            replaceButton.setDisable(true);
        }
    }

    @FXML
    protected void replaceAllAction() {
        if (replaceAllButton.isDisabled()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVaribles.getMessage("SureReplaceAll"));
        ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonCancel) {
            return;
        }
        final String findString = findInput.getText();
        if (findString.isEmpty()) {
            return;
        }
        final String replaceString = replaceInput.getText();
        sourceInformation.setFindString(findString);
        sourceInformation.setReplaceString(replaceString);
        final boolean whole = findWhole && (sourceInformation.getPagesNumber() > 1);
        final String text;
        if (whole) {
            text = null;
            if (!checkSavingForNextAction()) {
                return;
            }
        } else {
            text = mainArea.getText();
        }
        task = new Task<Void>() {
            private String replaced;
            private int num;

            @Override
            protected Void call() throws Exception {
                if (whole) {
                    num = sourceInformation.replaceAll();
                } else {
                    num = TextTools.countNumber(text, findString);
                    if (num > 0) {
                        replaced = text.replaceAll(findString, replaceString);
                    }
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (num > 0) {
                            if (whole) {
                                loadPage();
                            } else {
                                mainArea.setText(replaced);
                                updateInterface(true);
                            }
                            popInformation(MessageFormat.format(AppVaribles.getMessage("ReplaceAllOk"), num));
                        } else {
                            popInformation(AppVaribles.getMessage("NotFound"));
                            findPreviousButton.setDisable(true);
                            findNextButton.setDisable(true);
                            findLastButton.setDisable(true);
                            replaceButton.setDisable(true);
                            replaceAllButton.setDisable(true);
                            currentFound = -1;
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void filterAction() {
        if (isSettingValues || filterButton.isDisabled()) {
            return;
        }
        if (!checkSavingForNextAction() || sourceFile == null) {
            return;
        }
        String[] filterStrings = StringTools.splitBySpace(filterInput.getText());
        if (filterStrings.length == 0) {
            return;
        }
        sourceInformation.setFilterStrings(filterStrings);
        sourceInformation.setFilterInclude(filterInclude);
        final FileFilterController controller = (FileFilterController) openStage(CommonValues.FileFilterFxml,
                AppVaribles.getMessage("FileFilter"), false, true);
        if (controller != null) {
            controller.filterFile(sourceInformation, filterConditions);
        }

    }

    @FXML
    protected void nextPageAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        if (sourceInformation.getObjectsNumber() <= sourceInformation.getCurrentPageObjectEnd()) {
            nextPageButton.setDisable(true);
        } else {
            sourceInformation.setCurrentPage(sourceInformation.getCurrentPage() + 1);
            loadPage();
        }
    }

    @FXML
    protected void previousPageAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        if (sourceInformation.getCurrentPage() <= 1) {
            perviousPageButton.setDisable(true);
        } else {
            sourceInformation.setCurrentPage(sourceInformation.getCurrentPage() - 1);
            loadPage();
        }
    }

    @FXML
    protected void firstPageAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        sourceInformation.setCurrentPage(1);
        loadPage();
    }

    @FXML
    protected void lastPageAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        sourceInformation.setCurrentPage(sourceInformation.getPagesNumber());
        loadPage();
    }

    @FXML
    protected void goPageAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        if (currentPageTmp > sourceInformation.getPagesNumber()) {
            sourceInformation.setCurrentPage(sourceInformation.getPagesNumber());
        } else {
            sourceInformation.setCurrentPage(currentPageTmp);
        }
        loadPage();
    }

    @FXML
    protected void infoAction() {

    }

    @FXML
    protected void openAction() {
        try {
            if (!checkSavingForNextAction()) {
                return;
            }

            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(FilePathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
            AppVaribles.setUserConfigValue(FilePathKey, file.getParent());

            openFile(file);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void openFile(File file) {
        if (file == null) {
            return;
        }

        initPage(file);
        bottomText.setText(AppVaribles.getMessage("CheckingEncoding"));
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                sourceInformation.setLineBreak(TextTools.checkLineBreak(sourceFile));
                if (charsetByUser) {
                    ok = true;
                } else {
                    ok = sourceInformation.checkCharset();
                }
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        bottomText.setText("");
                        if (!ok || sourceInformation == null) {
                            popError(AppVaribles.getMessage("Failed"));
                            return;
                        }
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
                        if (currentBox != null) {
                            currentBox.getSelectionModel().select(sourceInformation.getCharset().name());
                            if (targetBox != null) {
                                targetBox.getSelectionModel().select(sourceInformation.getCharset().name());
                            } else {
                                targetInformation.setCharset(sourceInformation.getCharset());
                            }
                            if (sourceInformation.isWithBom()) {
                                currentBox.setDisable(true);
                                bomLabel.setText(AppVaribles.getMessage("WithBom"));
                                if (targetBomCheck != null) {
                                    targetBomCheck.setSelected(true);
                                }
                            } else {
                                currentBox.setDisable(false);
                                bomLabel.setText("");
                                if (targetBomCheck != null) {
                                    targetBomCheck.setSelected(false);
                                }
                            }
                        }
                        if (infoButton != null) {
                            infoButton.setDisable(false);
                        }
                        isSettingValues = false;

                        loadPage();
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void loadTotalNumbers() {
        if (sourceInformation == null || sourceFile == null
                || sourceInformation.getObjectsNumber() >= 0) {
            return;
        }
        backgroundTask = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = sourceInformation.readTotalNumbers();
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            updateInterface(false);
                        }
                    }
                });
                return null;
            }
        };
        Thread thread = new Thread(backgroundTask);
        thread.setDaemon(true);
        thread.start();
    }

    protected void loadPage() {
        if (sourceInformation == null || sourceFile == null) {
            return;
        }
        currentFound = -1;
        bottomText.setText(AppVaribles.getMessage("ReadingFile"));
        task = new Task<Void>() {
            private String text;

            @Override
            protected Void call() throws Exception {
                text = sourceInformation.readPage();
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (text != null) {
                            isSettingValues = true;
                            mainArea.setText(text);
                            isSettingValues = false;

                            updateInterface(false);
                            if (sourceInformation.getObjectsNumber() < 0) {
                                loadTotalNumbers();
                            }
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    protected void updateInterface(boolean changed) {
        fileChanged.set(changed);
        if (getMyStage() == null) {
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
        String text = mainArea.getText();
        int chars = text.length();
        int lines = TextTools.countNumber(text, "\n") + 1;
        if (sourceInformation.getLineBreak().equals(Line_Break.CRLF)) {
            chars += lines - 1;
        }
        sourceInformation.setEditerCharactersNumber(chars);
        sourceInformation.setEditerLinesNumber(lines);
        saveButton.setDisable(false);
        if (saveAsBox != null) {
            saveAsBox.setDisable(false);
        }
        if (sourceFile == null) {
            pageBar.setDisable(true);
            setLines(1, lines);
            pageLabel.setText("");
            bottomText.setText(AppVaribles.getMessage("CharactersNumber") + ": " + sourceInformation.getEditerCharactersNumber() + "  "
                    + AppVaribles.getMessage("LinesNumber") + ": " + sourceInformation.getEditerLinesNumber());
        } else {
            if (sourceInformation.getObjectsNumber() < 0) {
                pageBar.setDisable(true);
                saveButton.setDisable(true);
                if (saveAsBox != null) {
                    saveAsBox.setDisable(true);
                }
                setLines(sourceInformation.getCurrentPageLineStart(), sourceInformation.getCurrentPageLineEnd());
                bottomText.setText(
                        AppVaribles.getMessage("Characters") + ": "
                        + (sourceInformation.getCurrentPageObjectStart() + 1) + "-" + sourceInformation.getCurrentPageObjectEnd() + " "
                        + AppVaribles.getMessage("CountingTotalNumber"));
            } else {
                if (sourceInformation.getObjectsNumber() <= sourceInformation.getPageSize()) {
                    pageBar.setDisable(true);
                    sourceInformation.setPagesNumber(1);
                } else {
                    pageBar.setDisable(changed);
                    if (findBox != null) {
                        findBox.setDisable(changed);
                    }
                    if (filterBox != null) {
                        filterBox.setDisable(changed);
                    }
                    if (currentBox != null) {
                        currentBox.setDisable(changed || sourceInformation.isWithBom());
                    }
                    perviousPageButton.setDisable(sourceInformation.getCurrentPage() <= 1);
                    nextPageButton.setDisable(sourceInformation.getObjectsNumber() <= sourceInformation.getCurrentPageObjectEnd());
                    if (sourceInformation.getObjectsNumber() % sourceInformation.getPageSize() == 0) {
                        sourceInformation.setPagesNumber(sourceInformation.getObjectsNumber() / sourceInformation.getPageSize());
                    } else {
                        sourceInformation.setPagesNumber(sourceInformation.getObjectsNumber() / sourceInformation.getPageSize() + 1);
                    }
                    pageInput.setText(sourceInformation.getCurrentPage() + "");
                    currentPage = sourceInformation.getCurrentPage();
                    currentFound = -1;
                    if (sourceInformation.getCurrentFound() >= 0) {
                        int pos = sourceInformation.getCurrentFound() % sourceInformation.getPageSize();
                        int p = sourceInformation.getCurrentFound() / sourceInformation.getPageSize();
                        if (pos > 0) {
                            p++;
                        }
                        if (p == currentPage) {
                            currentFound = pos;
                        }
                    }
                }
                isSettingValues = false;

                pageLabel.setText("/" + sourceInformation.getPagesNumber());
                if (!changed) {
                    setLines(sourceInformation.getCurrentPageLineStart(), sourceInformation.getCurrentPageLineEnd());
                    bottomText.setText(
                            AppVaribles.getMessage("Characters") + ": "
                            + (sourceInformation.getCurrentPageObjectStart() + 1) + "-" + sourceInformation.getCurrentPageObjectEnd() + "/"
                            + sourceInformation.getObjectsNumber() + " "
                            + AppVaribles.getMessage("Lines") + ": "
                            + sourceInformation.getCurrentPageLineStart() + "-" + sourceInformation.getCurrentPageLineEnd() + "/"
                            + sourceInformation.getLinesNumber());
                } else {
                    int charsTo = sourceInformation.getCurrentPageObjectStart() + chars;
                    int charsTotal = sourceInformation.getObjectsNumber()
                            + (chars - (sourceInformation.getCurrentPageObjectEnd() - sourceInformation.getCurrentPageObjectStart()));
                    int linesTo = sourceInformation.getCurrentPageLineStart() + lines - 1;
                    int linesTotal = sourceInformation.getLinesNumber()
                            + (lines - (sourceInformation.getCurrentPageLineEnd() - sourceInformation.getCurrentPageLineStart() + 1));
                    setLines(sourceInformation.getCurrentPageLineStart(), linesTo);
                    bottomText.setText(
                            AppVaribles.getMessage("Characters") + ": "
                            + (sourceInformation.getCurrentPageObjectStart() + 1) + "-" + charsTo + "/"
                            + charsTotal + " "
                            + AppVaribles.getMessage("Lines") + ": "
                            + sourceInformation.getCurrentPageLineStart() + "-" + linesTo + "/"
                            + linesTotal);
                }
            }
        }
        if (pageSizeButton != null) {
            pageSizeButton.setDisable(changed);
        }

        setSecondArea(text);
        if (currentFound >= 0) {
            mainArea.deselect();
            mainArea.selectRange(currentFound, Math.min(text.length(), currentFound + sourceInformation.getFindString().length()));
            if (findNextButton != null) {
                findNextButton.setDisable(false);
                findPreviousButton.setDisable(false);
                replaceButton.setDisable(false);
            }
        }

    }

    @FXML
    protected void saveAction() {
        if (sourceFile == null) {
            saveNew();
        } else {
            saveExisted();
        }
    }

    protected void saveNew() {
        final FileChooser fileChooser = new FileChooser();
        File path = new File(AppVaribles.getUserConfigValue(FilePathKey, CommonValues.UserFilePath));
        fileChooser.setInitialDirectory(path);
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
        AppVaribles.setUserConfigValue(FilePathKey, file.getParent());
        targetInformation.setFile(file);
        targetInformation.setWithBom(targetBomCheck.isSelected());
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = targetInformation.writeObject(mainArea.getText());
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            popInformation(AppVaribles.getMessage("Successful"));
                            charsetByUser = false;
                            openFile(file);
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void saveExisted() {
        if (confirmCheck.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAsAction();
                return;
            }
        }
        targetInformation.setFile(sourceFile);
        targetInformation.setWithBom(targetBomCheck.isSelected());
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = targetInformation.writePage(sourceInformation, mainArea.getText());
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            popInformation(AppVaribles.getMessage("Successful"));
                            openFile(sourceFile);
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                        updateInterface(false);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    protected void saveAsAction() {
        final FileChooser fileChooser = new FileChooser();
        File path = new File(AppVaribles.getUserConfigValue(FilePathKey, CommonValues.UserFilePath));
        fileChooser.setInitialDirectory(path);
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
        AppVaribles.setUserConfigValue(FilePathKey, file.getParent());

        targetInformation.setFile(file);
        if (targetBomCheck != null) {
            targetInformation.setWithBom(targetBomCheck.isSelected());
        } else {
            targetInformation.setCharset(sourceInformation.getCharset());
            targetInformation.setLineBreak(sourceInformation.getLineBreak());
            targetInformation.setWithBom(sourceInformation.isWithBom());
        }
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = targetInformation.writePage(sourceInformation, mainArea.getText());
                if (task.isCancelled()) {
                    return null;
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            if (saveAsType == SaveAsType.Load) {
                                openFile(file);
                            } else if (saveAsType == SaveAsType.Open) {
                                final FileEditerController controller
                                        = (FileEditerController) openStage(CommonValues.TextEditerFxml,
                                                AppVaribles.getMessage("TextEncoding"), false, true);
                                sourceInformation.setCurrentPage(1);
                                controller.openFile(file);
                            }
                            popInformation(AppVaribles.getMessage("Successful"));
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    protected void createAction() {
        try {
            if (!checkSavingForNextAction()) {
                return;
            }
            initPage(null);
            updateInterface(false);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public boolean stageReloading() {
//        logger.debug("stageReloading");
        return checkSavingForNextAction();
    }

    @Override
    public boolean stageClosing() {
//        logger.debug("stageClosing");
        if (!checkSavingForNextAction()) {
            return false;
        }
        return super.stageClosing();
    }

    public boolean checkSavingForNextAction() {
        if (fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("NeedSaveBeforeAction"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void taskCanceled() {
        bottomText.setText("");
        if (backgroundTask != null && backgroundTask.isRunning()) {
            backgroundTask.cancel();
        }
    }

}
