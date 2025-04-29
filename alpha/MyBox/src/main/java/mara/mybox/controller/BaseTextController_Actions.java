package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import javafx.event.Event;
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
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
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
                goPage();
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
        final File file = saveCurrentFile();
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
                sourceInformation.setStartRowOfCurrentPage(0);
                sourceInformation.setEndRowOfCurrentPage(pageLinesNumber(pageText));
                sourceInformation.setStartObjectOfCurrentPage(0);
                sourceInformation.setEndObjectOfCurrentPage(pageObjectsNumber(pageText));
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
                sourceInformation.setEndRowOfCurrentPage(
                        sourceInformation.getStartRowOfCurrentPage() + pageLinesNumber(pageText));
                sourceInformation.setEndObjectOfCurrentPage(
                        sourceInformation.getStartObjectOfCurrentPage() + pageObjectsNumber(pageText));
                updateInterface(false);
                loadTotalNumbers();
            }

        };
        start(task, getMyWindow() == null || myWindow.isFocused());
    }

    public void saveAs(File file) {
        if (file == null || !validateMainArea()) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        FileEditInformation targetInformation = FileEditInformation.create(editType, file, pagination);
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

    protected boolean filter(ControlTextFilter filterController) {
        if (sourceInformation == null || filterController == null || !filterController.valid.get()) {
            popError(message("InvalidParameters"));
            return false;
        }
        if (filterController.filterStrings == null || filterController.filterStrings.length == 0) {
            popError(message("InvalidParameters"));
            return false;
        }
        if (sourceInformation.getPagesNumber() > 1 && !checkBeforeNextAction()) {
            return false;
        }
        FxTask filterTask = new FxTask<Void>(this) {

            private File filteredFile;

            @Override
            protected boolean handle() {
                FileEditInformation filterInfo;
                if (sourceFile != null) {
                    filterInfo = sourceInformation;
                } else {
                    File tmpfile = TextFileTools.writeFile(FileTmpTools.getTempFile(".txt"), mainArea.getText(), Charset.forName("utf-8"));
                    filterInfo = FileEditInformation.create(editType, tmpfile, null);
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
                File tmpFile = filterInfo.filter(this, filterController.filterLineNumberCheck.isSelected());
                if (tmpFile != null && tmpFile.exists()) {
                    String prefix = null;
                    if (filterInfo.getFile() != null) {
                        prefix = FileNameTools.prefix(filterInfo.getFile().getName());
                    }
                    if (prefix == null) {
                        prefix = baseTitle;
                    }
                    prefix += "_" + filterInfo.filterTypeName() + "_" + Arrays.asList(filterInfo.getFilterStrings());
                    filteredFile = FileTmpTools.generateFile(prefix, "txt");
                    FileTools.override(tmpFile, filteredFile);
                }
                return filteredFile != null;
            }

            @Override
            protected void whenSucceeded() {
                if (filteredFile.length() == 0) {
                    popInformation(message("NoData"));
                    return;
                }
                TextEditorController.open(filteredFile);
            }
        };
        start(filterTask, false);
        return true;
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
    public boolean menuAction(Event event) {
        Point2D localToScreen = mainArea.localToScreen(mainArea.getWidth() - 80, 80);
        MenuTextEditController.textMenu(myController, mainArea, localToScreen.getX(), localToScreen.getY());
        return true;
    }

}
