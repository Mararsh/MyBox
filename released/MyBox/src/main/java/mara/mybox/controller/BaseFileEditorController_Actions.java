package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditorController_Actions extends BaseFileEditorController_File {

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
    public void refreshAction() {
        try {
            if (!isSettingValues && sourceFile != null) {
                sourceInformation.setCharsetDetermined(true);
                sourceInformation.setCharset(Charset.forName(charsetSelector.getSelectionModel().getSelectedItem()));
                openFile(sourceFile);
            };
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
        sourceInformation.setFile(file);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    ok = sourceInformation.writeObject(mainArea.getText());
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
    }

    private void saveExisted() {
        if (confirmCheck.isVisible() && confirmCheck.isSelected() && (autoSaveTimer == null)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("SureOverrideFile"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
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
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    if (backupController != null && backupController.isBack()) {
                        backupController.addBackup(task, sourceFile);
                    }
                    return sourceInformation.writePage(sourceInformation, mainArea.getText());
                }

                @Override
                protected void whenSucceeded() {
                    recordFileWritten(sourceFile);
                    if (getMyWindow() != null && myWindow.isFocused()) {
                        popSaved();
                    }
                    sourceInformation.setTotalNumberRead(false);
                    String pageText = mainArea.getText();
                    sourceInformation.setCurrentPageLineEnd(sourceInformation.getCurrentPageLineStart() + pageLinesNumber(pageText));
                    sourceInformation.setCurrentPageObjectEnd(sourceInformation.getCurrentPageObjectStart() + pageObjectsNumber(pageText));
                    updateInterface(false);
                    loadTotalNumbers();
                }

            };
            start(task, getMyWindow() == null || myWindow.isFocused());
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!validateMainArea()) {
            return;
        }
        final File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            FileEditInformation targetInformation = FileEditInformation.create(editType, file);
            targetInformation.setFile(file);
            targetInformation.setCharset(Charset.forName(targetCharsetSelector.getSelectionModel().getSelectedItem()));
            targetInformation.setPageSize(sourceInformation.getPageSize());
            targetInformation.setCurrentPage(sourceInformation.getCurrentPage());
            if (targetBomCheck != null) {
                targetInformation.setWithBom(targetBomCheck.isSelected());
            } else {
                targetInformation.setWithBom(sourceInformation.isWithBom());
            }
            targetInformation.setLineBreak(lineBreak);
            targetInformation.setLineBreakValue(TextTools.lineBreakValue(lineBreak));
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return targetInformation.writePage(sourceInformation, mainArea.getText());
                }

                @Override
                protected void whenSucceeded() {
                    recordFileWritten(file);
                    BaseFileEditorController controller = null;
                    if (saveAsType == SaveAsType.Load) {
                        controller = (BaseFileEditorController) myController;
                    } else if (saveAsType == SaveAsType.Open) {
                        controller = openNewStage();
                    }
                    if (controller != null) {
                        controller.editType = editType;
                        controller.sourceInformation = targetInformation;
                        controller.sourceInformation.setCharsetDetermined(true);
                        controller.openFile(file);
                    }
                    popSaved();
                }

            };
            start(task);
        }
    }

    public BaseFileEditorController openNewStage() {
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
                synchronized (this) {
                    if (task != null && !task.isQuit()) {
                        return;
                    }
                    task = new SingletonTask<Void>(this) {

                        String text;

                        @Override
                        protected boolean handle() {
                            text = sourceInformation.readLine(locateLine);
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
                synchronized (this) {
                    if (task != null && !task.isQuit()) {
                        return;
                    }
                    task = new SingletonTask<Void>(this) {

                        String text;

                        @Override
                        protected boolean handle() {
                            text = sourceInformation.readObject(locateObject);
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
    }

    @FXML
    protected void locateLinesRange() {
        long from, to;  // 0-based, exlcuded end
        try {
            from = Long.valueOf(lineFromInput.getText()) - 1;
            if (from < 0 || from >= sourceInformation.getLinesNumber()) {
                popError(message("InvalidParameters") + ": " + message("From"));
                return;
            }
            to = Long.valueOf(lineToInput.getText());
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
                synchronized (this) {
                    if (task != null && !task.isQuit()) {
                        return;
                    }
                    task = new SingletonTask<Void>(this) {

                        String text;

                        @Override
                        protected boolean handle() {
                            text = sourceInformation.readLines(from, number);
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
    }

    @FXML
    protected void locateObjectsRange() {
        long from, to;  // 0-based, exlcuded end
        try {
            from = Long.valueOf(objectFromInput.getText()) - 1;
            if (from < 0 || from >= sourceInformation.getObjectsNumber()) {
                popError(message("InvalidParameters") + ": " + message("From"));
                return;
            }
            to = Long.valueOf(objectToInput.getText());
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
                synchronized (this) {
                    if (task != null && !task.isQuit()) {
                        return;
                    }
                    task = new SingletonTask<Void>(this) {

                        String text;

                        @Override
                        protected boolean handle() {
                            text = sourceInformation.readObjects(from, len);
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
        synchronized (this) {
            SingletonTask filterTask = new SingletonTask<Void>(this) {

                private File filteredFile;
                private String finalCondition;

                @Override
                protected boolean handle() {
                    FileEditInformation filterInfo;
                    if (sourceFile != null) {
                        filterInfo = sourceInformation;
                    } else {
                        File tmpfile = TextFileTools.writeFile(TmpFileTools.getTempFile(".txt"), mainArea.getText(), Charset.forName("utf-8"));
                        filterInfo = FileEditInformation.create(editType, tmpfile);
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
                        finalCondition = filterConditionsString + "\n" + message("And") + conditions;
                    }
                    filteredFile = filterInfo.filter(filterController.filterLineNumberCheck.isSelected());
                    return filteredFile != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (filteredFile.length() == 0) {
                        popInformation(message("NoData"));
                        return;
                    }
                    TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
                    controller.sourceFileChanged(filteredFile);
                    controller.filterConditionsLabel.setText(finalCondition);
                    controller.filterConditionsString = finalCondition;
                    controller.filterPane.setExpanded(true);
                }
            };
            start(filterTask, false, null);
        }
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
    public void myBoxClipBoard() {
        TextClipboardPopController.open(this, mainArea);
    }

    @FXML
    @Override
    public boolean menuAction() {
        Point2D localToScreen = mainArea.localToScreen(mainArea.getWidth() - 80, 80);
        MenuTextEditController.open(myController, mainArea, localToScreen.getX(), localToScreen.getY());
        return true;
    }

}
