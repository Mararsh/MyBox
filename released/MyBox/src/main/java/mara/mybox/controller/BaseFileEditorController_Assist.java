package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import static mara.mybox.data.FileEditInformation.defaultCharset;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.FindReplaceString.Operation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 *
 * Attributes and methods about file
 */
public abstract class BaseFileEditorController_Assist extends BaseController {

    protected Edit_Type editType;
    protected long lineLocation, objectLocation;
    protected SimpleBooleanProperty fileChanged;
    protected boolean charsetByUser;
    protected FileEditInformation sourceInformation, targetInformation;
    protected String filterConditionsString = "";
    protected Line_Break lineBreak;
    protected int defaultPageSize, lineBreakWidth, lastCursor, lastCaret, currentLine;
    protected double lastScrollTop, lastScrollLeft;
    protected String lineBreakValue;
    protected Timer autoSaveTimer;

    protected enum Action {
        None, FindFirst, FindNext, FindPrevious, FindLast, Replace, ReplaceAll,
        Filter, SetPageSize, NextPage, PreviousPage, FirstPage, LastPage, GoPage
    }

    @FXML
    protected TitledPane filePane, savePane, saveAsPane, findPane, filterPane,
            locatePane, encodePane, breakLinePane, backupPane;
    @FXML
    protected TextArea mainArea, lineArea, pairArea;
    @FXML
    protected ComboBox<String> encodeSelector, targetEncodeSelector, pageSelector, pageSizeSelector;
    @FXML
    protected ToggleGroup lineBreakGroup;
    @FXML
    protected CheckBox targetBomCheck, confirmCheck, autoSaveCheck;
    @FXML
    protected ControlTimeLength autoSaveDurationController;
    @FXML
    protected Label editLabel, bomLabel, fileLabel, pageLabel, charsetLabel, selectionLabel,
            filterConditionsLabel;
    @FXML
    protected Button panesMenuButton, charactersButton, linesButton, exampleFilterButton,
            filterButton, locateObjectButton, locateLineButton;
    @FXML
    protected TextField fromInput, toInput, currentLineBreak, objectNumberInput, lineInput;
    @FXML
    protected RadioButton crlfRadio, lfRadio, crRadio;
    @FXML
    protected HBox pageBox;
    @FXML
    protected ControlFindReplace findReplaceController;
    @FXML
    protected ControlTextFilter filterController;
    @FXML
    protected ControlFileBackup backupController;

    public BaseFileEditorController_Assist() {
        defaultPageSize = 50000;
    }

    @Override
    public void sourceFileChanged(File file) {
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
            bottomLabel.setText(Languages.message("CheckingEncoding"));
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
                            bomLabel.setText(Languages.message("WithBom"));
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
            handling(task);
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
            popError(Languages.message("WrongLineBreak"));
            breakLinePane.setExpanded(true);
            return;
        }
        initPage(file);
        sourceInformation.setLineBreak(lineBreak);
        sourceInformation.setLineBreakValue(lineBreakValue);
        sourceInformation.setLineBreakWidth(lineBreakWidth);
        loadPage();

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
            sourceInformation.setPageSize(UserConfig.getInt(baseName + "PageSize", defaultPageSize));
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
                setControlsStyle();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            isSettingValues = false;
        }
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
            bottomLabel.setText(Languages.message("ReadingFile"));
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
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
                pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                return false;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            return false;
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
                editLabel.setText(Languages.message("InvalidData"));
            }
            mainArea.setStyle(NodeStyleTools.badStyle);
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
            String info = Languages.message("RangeInFile") + ":"
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
                info += "\n" + Languages.message("RangeInPage") + ":" + (range.getStart() + 1) + "-" + (range.getEnd());
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
                fileInfo = Languages.message("CountingTotalNumber") + "\n\n";
            } else {
                fileObjectNumber = sourceInformation.getObjectsNumber();
                fileLinesNumber = sourceInformation.getLinesNumber();

                pagesNumber = fileObjectNumber / pageSize;
                if (fileObjectNumber % pageSize > 0) {
                    pagesNumber++;
                }
                sourceInformation.setPagesNumber(pagesNumber);
            }
            fileInfo += Languages.message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + Languages.message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + (editType == Edit_Type.Bytes ? Languages.message("BytesNumberInFile") : Languages.message("CharactersNumberInFile"))
                    + ": " + StringTools.format(fileObjectNumber) + "\n"
                    + Languages.message("LinesNumberInFile") + ": " + StringTools.format(fileLinesNumber) + "\n"
                    + (editType == Edit_Type.Bytes ? Languages.message("PageSizeBytes") : Languages.message("PageSizeCharacters"))
                    + ": " + StringTools.format(pageSize) + "\n"
                    + Languages.message("CurrentPage") + ": " + StringTools.format(currentPage)
                    + " / " + StringTools.format(pagesNumber) + "\n";
        }

        String objectInfo, lineInfo;
        if (pagesNumber > 1) {
            objectInfo = editType == Edit_Type.Bytes ? Languages.message("BytesRangeInPage") : Languages.message("CharactersRangeInPage");
            objectInfo += ": " + StringTools.format(pageObjectStart) + " - " + StringTools.format(pageObjectEnd)
                    + " ( " + StringTools.format(pageObjectsNumber) + " )\n";
            lineInfo = Languages.message("LinesRangeInPage")
                    + ": " + StringTools.format(pageLineStart) + " - " + StringTools.format(pageLineEnd)
                    + " ( " + StringTools.format(pageLinesNumber) + " )\n";
        } else {
            objectInfo = editType == Edit_Type.Bytes ? Languages.message("BytesNumberInPage") : Languages.message("CharactersNumberInPage");
            objectInfo += ": " + StringTools.format(pageObjectsNumber) + "\n";
            lineInfo = Languages.message("LinesNumberInPage") + ": " + StringTools.format(pageLinesNumber) + "\n";
        }
        fileInfo += Languages.message("CurrentFileLineBreak") + ": " + sourceInformation.lineBreakName() + "\n"
                + Languages.message("CurrentFileCharset") + ": " + sourceInformation.getCharset().name() + "\n"
                + objectInfo + lineInfo
                + Languages.message("PageModifyTime") + ": " + DateTools.nowString();
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
        if (filterController != null) {
            filterController.isBytes = editType == Edit_Type.Bytes;
            filterController.maxLen = pageSize;
            filterController.sourceLen = pageObjectsNumber;
            filterController.checkFilterStrings();
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
        if (UserConfig.getBoolean(baseName + "UpdateSynchronously", false)
                || (pairArea != null && pairArea.getText().isEmpty())) {
            refreshPairAction();
        }
    }

    @FXML
    public void refreshPairAction() {
    }

    protected void setPairAreaSelection() {

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
        final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
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
            handling(task);
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
            alert.setContentText(Languages.message("SureOverrideFile"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonSaveAs = new ButtonType(Languages.message("SaveAs"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
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
            handling(task);
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
        final File file = chooseSaveFile();
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
                        BaseFileEditorController controller = openNewStage();
                        controller.openFile(file);
                    }
                    popSaved();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public BaseFileEditorController openNewStage() {
        if (null == editType) {
            return null;
        } else {
            switch (editType) {
                case Text:
                    return (BaseFileEditorController) openStage(Fxmls.TextEditorFxml);
                case Bytes:
                    return (BaseFileEditorController) openStage(Fxmls.BytesEditorFxml);
                case Markdown:
                    return (MarkdownEditorController) openStage(Fxmls.MarkdownEditorFxml);
                default:
                    return null;
            }
        }
    }

    @FXML
    protected void locateLine() {
        sourceInformation.setCurrentLine(-1);
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            String[] lines = mainArea.getText().split("\n");
            if (lineLocation > lines.length) {
                popError(Languages.message("NoData"));
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
                popError(Languages.message("NoData"));
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
                    handling(task);
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
        if (isSettingValues || filterButton.isDisabled()
                || sourceInformation == null || filterController == null) {
            return;
        }
        if (filterController.filterStrings == null || filterController.filterStrings.length == 0) {
            popError(Languages.message("InvalidParameters"));
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
                        File tmpfile = TextFileTools.writeFile(TmpFileTools.getTempFile(".txt"), mainArea.getText(), Charset.forName("utf-8"));
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
                    filterInfo.setFilterStrings(filterController.filterStrings);
                    filterInfo.setFilterType(filterController.filterType);
                    String conditions = " (" + filterInfo.filterTypeName() + ": "
                            + Arrays.asList(filterInfo.getFilterStrings()) + ") ";
                    if (filterConditionsString == null || filterConditionsString.isEmpty()) {
                        finalCondition = filterInfo.getFile().getAbsolutePath() + "\n" + conditions;
                    } else {
                        finalCondition = filterConditionsString + "\n" + Languages.message("And") + conditions;
                    }
                    filteredFile = filterInfo.filter(filterController.filterLineNumberCheck.isSelected());
                    return filteredFile != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (filteredFile.length() == 0) {
                        popInformation(Languages.message("NoData"));
                        return;
                    }
                    TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
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

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        initPage(null);
        updateInterface(false);
    }

    public void loadContents(String contents) {
        createAction();
        mainArea.setText(contents);
        updateInterface(true);
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

}
