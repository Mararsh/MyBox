package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.input.Clipboard;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.db.data.FileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseTextController_Actions extends BaseTextController_File {

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
    public void refreshAction() {
        try {
            if (!isSettingValues && sourceFile != null) {
                sourceInformation.setCharsetDetermined(true);
                sourceInformation.setCharset(Charset.forName(UserConfig.getString(baseName + "SourceCharset", "utf-8")));
                openFile(sourceFile);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (sourceFile != null && !sourceInformation.isTotalNumberRead()) {
            popError(message("CountingTotalNumber"));
            return;
        }
        if (!validateMainArea()) {
            popError(message("InvalidData"));
            return;
        }
        if (sourceFile == null) {
            saveNew();
        } else {
            saveExisted();
        }
    }

    private void saveNew() {
        final File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        sourceInformation.setFile(file);
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                ok = sourceInformation.writeObject(this, mainArea.getText());
                return ok;
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file);
                popSaved();
                sourceFile = file;
                sourceInformation.setTotalNumberRead(false);
                String pageText = mainArea.getText();
                sourceInformation.setCurrentPageLineStart(0);
                sourceInformation.setCurrentPageLineEnd(pageLinesNumber(pageText));
                sourceInformation.setCurrentPageObjectStart(0);
                sourceInformation.setCurrentPageObjectEnd(pageObjectsNumber(pageText));
                updateInterface(false);
                loadTotalNumbers();
            }

        };
        start(task);
    }

    private void saveExisted() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                needBackup = sourceFile != null && UserConfig.getBoolean(baseName + "BackupWhenSave", true);
                if (sourceFile != null && UserConfig.getBoolean(baseName + "BackupWhenSave", true)) {
                    backup = addBackup(this, sourceFile);
                }
                return sourceInformation.writePage(this, sourceInformation, mainArea.getText());
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(sourceFile);
                if (getMyWindow() != null && myWindow.isFocused()) {
                    if (needBackup) {
                        if (backup != null && backup.getBackup() != null) {
                            popInformation(message("SavedAndBacked"));
                            FileBackupController.updateList(sourceFile);
                        } else {
                            popError(message("FailBackup"));
                        }
                    } else {
                        popInformation(sourceFile + "   " + message("Saved"));
                    }
                }
                sourceInformation.setTotalNumberRead(false);
                String pageText = mainArea.getText();
                sourceInformation.setCurrentPageLineEnd(
                        sourceInformation.getCurrentPageLineStart() + pageLinesNumber(pageText));
                sourceInformation.setCurrentPageObjectEnd(
                        sourceInformation.getCurrentPageObjectStart() + pageObjectsNumber(pageText));
                updateInterface(false);
                loadTotalNumbers();
            }

        };
        start(task, getMyWindow() == null || myWindow.isFocused());
    }

    public void saveAs() {
        if (!validateMainArea()) {
            return;
        }
        final File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        FileEditInformation targetInformation = FileEditInformation.create(editType, file);
        targetInformation.setFile(file);
        targetInformation.setCharset(Charset.forName(UserConfig.getString(baseName + "TargetCharset", "utf-8")));
        targetInformation.setPageSize(sourceInformation.getPageSize());
        targetInformation.setCurrentPage(sourceInformation.getCurrentPage());
        targetInformation.setWithBom(UserConfig.getBoolean(baseName + "TargetBOM", sourceInformation.isWithBom()));
        Line_Break tlk = sourceInformation.getLineBreak();
        if (tlk == null) {
            tlk = Line_Break.LF;
        }
        tlk = Line_Break.valueOf(UserConfig.getString(baseName + "TargetLineBreak", tlk.toString()));
        targetInformation.setLineBreak(tlk);
        targetInformation.setLineBreakValue(TextTools.lineBreakValue(tlk));
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return targetInformation.writePage(this, sourceInformation, mainArea.getText());
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file);
                BaseTextController editor = null;
                if (saveAsType == SaveAsType.Load) {
                    editor = (BaseTextController) myController;
                } else if (saveAsType == SaveAsType.Open) {
                    editor = openNewStage();
                }
                if (editor != null) {
                    editor.editType = editType;
                    editor.sourceInformation = targetInformation;
                    editor.sourceInformation.setCharsetDetermined(true);
                    editor.openFile(file);
                }
                popSaved();
            }

        };
        start(task);
    }

    public BaseTextController openNewStage() {
        if (null == editType) {
            return null;
        } else {
            switch (editType) {
                case Text:
                    return (TextEditorController) openStage(Fxmls.TextEditorFxml);
                case Bytes:
                    return (BytesEditorController) openStage(Fxmls.BytesEditorFxml);
                case Markdown:
                    return (MarkdownEditorController) openStage(Fxmls.MarkdownEditorFxml);
                default:
                    return null;
            }
        }
    }

    @FXML
    protected void locateLine() {
        if (locateLine < 0 || locateLine >= sourceInformation.getLinesNumber()) {
            popError(message("InvalidParameters"));
            return;
        }
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectLine(locateLine);
        } else {
            if (locateLine >= sourceInformation.getCurrentPageLineStart()
                    && locateLine < sourceInformation.getCurrentPageLineEnd()) {
                selectLine(locateLine - sourceInformation.getCurrentPageLineStart());
            } else {
                if (!checkBeforeNextAction()) {
                    return;
                }
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readLine(this, locateLine);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectLine(locateLine - sourceInformation.getCurrentPageLineStart());
                    }

                };
                start(task);
            }
        }
    }

    @FXML
    protected void locateObject() {
        if (locateObject < 0 || locateObject >= sourceInformation.getObjectsNumber()) {
            popError(message("InvalidParameters"));
            return;
        }
        int unit = sourceInformation.getObjectUnit();
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectObjects(locateObject * unit, unit);

        } else {
            if (locateObject >= sourceInformation.getCurrentPageObjectStart()
                    && locateObject < sourceInformation.getCurrentPageObjectEnd()) {
                selectObjects((locateObject - sourceInformation.getCurrentPageObjectStart()) * unit, unit);
            } else {
                if (!checkBeforeNextAction()) {
                    return;
                }
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readObject(this, locateObject);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectObjects((locateObject - sourceInformation.getCurrentPageObjectStart()) * unit, unit);
                    }

                };
                start(task);
            }
        }
    }

    @FXML
    protected void locateLinesRange() {
        long from, to;  // 0-based, exlcuded end
        try {
            from = Long.parseLong(lineFromInput.getText()) - 1;
            if (from < 0 || from >= sourceInformation.getLinesNumber()) {
                popError(message("InvalidParameters") + ": " + message("From"));
                return;
            }
            to = Long.parseLong(lineToInput.getText());
            if (to < 0 || to > sourceInformation.getLinesNumber() || from > to) {
                popError(message("InvalidParameters") + ": " + message("To"));
                return;
            }
        } catch (Exception e) {
            popError(e.toString());
            return;
        }
        int number = (int) (to - from);
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectLines(from, number);
        } else {
            if (from >= sourceInformation.getCurrentPageLineStart() && to <= sourceInformation.getCurrentPageLineEnd()) {
                selectLines(from - sourceInformation.getCurrentPageLineStart(), number);
            } else {
                if (!checkBeforeNextAction()) {
                    return;
                }
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readLines(this, from, number);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectLines(from - sourceInformation.getCurrentPageLineStart(), number);
                    }

                };
                start(task);
            }
        }
    }

    @FXML
    protected void locateObjectsRange() {
        long from, to;  // 0-based, exlcuded end
        try {
            from = Long.parseLong(objectFromInput.getText()) - 1;
            if (from < 0 || from >= sourceInformation.getObjectsNumber()) {
                popError(message("InvalidParameters") + ": " + message("From"));
                return;
            }
            to = Long.parseLong(objectToInput.getText());
            if (to < 0 || to > sourceInformation.getObjectsNumber() || from > to) {
                popError(message("InvalidParameters") + ": " + message("To"));
                return;
            }
        } catch (Exception e) {
            popError(e.toString());
            return;
        }
        int len = (int) (to - from), unit = sourceInformation.getObjectUnit();
        if (sourceFile == null || sourceInformation.getPagesNumber() <= 1) {
            selectObjects(from * unit, len * unit);

        } else {
            if (from >= sourceInformation.getCurrentPageObjectStart() && to <= sourceInformation.getCurrentPageObjectEnd()) {
                selectObjects((from - sourceInformation.getCurrentPageObjectStart()) * unit, len * unit);

            } else {
                if (!checkBeforeNextAction()) {
                    return;
                }
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new FxSingletonTask<Void>(this) {

                    String text;

                    @Override
                    protected boolean handle() {
                        text = sourceInformation.readObjects(this, from, len);
                        return text != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        loadText(text, false);
                        selectObjects((from - sourceInformation.getCurrentPageObjectStart()) * unit, len * unit);
                    }

                };
                start(task);
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
            popError(message("InvalidParameters"));
            return;
        }
        if (fileChanged.get() && sourceInformation.getPagesNumber() > 1
                && !checkBeforeNextAction()) {
            return;
        }
        FxTask filterTask = new FxTask<Void>(this) {

            private File filteredFile;
            private String finalCondition;

            @Override
            protected boolean handle() {
                FileEditInformation filterInfo;
                if (sourceFile != null) {
                    filterInfo = sourceInformation;
                } else {
                    File tmpfile = TextFileTools.writeFile(FileTmpTools.getTempFile(".txt"), mainArea.getText(), Charset.forName("utf-8"));
                    filterInfo = FileEditInformation.create(editType, tmpfile);
                    filterConditionsString = "";
                    if (editType != Edit_Type.Bytes) {
                        filterInfo.setLineBreak(TextTools.checkLineBreak(this, tmpfile));
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
                    finalCondition = filterConditionsString + "\n" + message("And") + conditions;
                }
                filteredFile = filterInfo.filter(this, filterController.filterLineNumberCheck.isSelected());
                return filteredFile != null;
            }

            @Override
            protected void whenSucceeded() {
                if (filteredFile.length() == 0) {
                    popInformation(message("NoData"));
                    return;
                }
                TextEditorController c = (TextEditorController) openStage(Fxmls.TextEditorFxml);
                c.sourceFileChanged(filteredFile);
                c.filterConditionsLabel.setText(finalCondition);
                c.filterConditionsString = finalCondition;
                c.filterPane.setExpanded(true);
            }
        };
        start(filterTask, false, null);
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceInformation = null;
        initPage(null);
        updateInterface(false);
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            loadText(text, true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void editTexts() {
        if (sourceFile == null) {
            String txt = mainArea.getText();
            if (txt == null || txt.isBlank()) {
                popError(message("NoData"));
                return;
            }
            TextEditorController.edit(txt);
        } else {
            TextEditorController.open(sourceFile);
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        TextClipboardPopController.open(this, mainArea);
    }

    @FXML
    @Override
    public boolean menuAction() {
        Point2D localToScreen = mainArea.localToScreen(mainArea.getWidth() - 80, 80);
        MenuTextEditController.textMenu(myController, mainArea, localToScreen.getX(), localToScreen.getY());
        return true;
    }

}