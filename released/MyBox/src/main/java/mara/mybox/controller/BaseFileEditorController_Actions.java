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
                recordCursor();
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
        final File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        sourceInformation.setFile(file);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

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
                    updateInterface(false);
                    loadTotalNumbers();
                }

            };
            start(task);
        }
    }

    protected void saveExisted() {
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
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (backupController != null && backupController.isBack()) {
                        backupController.addBackup(sourceFile);
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
        if (!formatMainArea()) {
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
            FileEditInformation targetInformation = FileEditInformation.newEditInformation(editType, file);
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
            task = new SingletonTask<Void>() {

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
                    start(task);
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
            popError(message("InvalidParameters"));
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
