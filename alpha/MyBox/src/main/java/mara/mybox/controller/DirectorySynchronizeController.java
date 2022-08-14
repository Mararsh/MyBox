package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-8
 * @Description
 * @License Apache License Version 2.0
 */
public class DirectorySynchronizeController extends BaseBatchFileController {

    protected boolean isConditional, startHandle;
    protected String lastFileName;
    protected FileSynchronizeAttributes copyAttr;
    protected String strFailedCopy, strCreatedSuccessfully, strCopySuccessfully, strFailedDelete;
    protected String strDeleteSuccessfully, strFileDeleteSuccessfully, strDirectoryDeleteSuccessfully;

    @FXML
    protected ControlPathInput targetPathInputController;
    @FXML
    protected VBox dirsBox, conditionsBox, condBox, logsBox;
    @FXML
    protected TextField notCopyInput;
    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected CheckBox copySubdirCheck, copyEmptyCheck, copyNewCheck, copyHiddenCheck, copyReadonlyCheck;
    @FXML
    protected CheckBox copyExistedCheck, copyModifiedCheck, deleteNonExistedCheck, notCopyCheck, copyAttrCheck, continueCheck,
            deleteSourceCheck;
    @FXML
    protected DatePicker modifyAfterInput;

    public DirectorySynchronizeController() {
        baseTitle = Languages.message("DirectorySynchronize");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            deleteNonExistedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    if (deleteNonExistedCheck.isSelected()) {
                        deleteNonExistedCheck.setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    } else {
                        deleteNonExistedCheck.setStyle(null);
                    }
                }
            });

            deleteSourceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    if (deleteSourceCheck.isSelected()) {
                        deleteSourceCheck.setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    } else {
                        deleteSourceCheck.setStyle(null);
                    }
                }
            });

            ValidationTools.setNonnegativeValidation(maxLinesinput);

            checkIsConditional();
            copyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkIsConditional();
                }
            });

            targetPathInputController.baseName(baseName).init();

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(sourcePathInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(targetPathInputController.valid.not())
            );

            operationBarController.openTargetButton.disableProperty().bind(
                    startButton.disableProperty()
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    protected boolean initAttributes() {
        try {
            sourcePath = new File(sourcePathInput.getText());
            targetPath = targetPathInputController.file();
            if (targetPath == null) {
                popError(message("Invlid") + ": " + message("TargetPath"));
                return false;
            }
            if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
                popError(message("TargetPathShouldNotSourceSub"));
                return false;
            }

            if (!paused || lastFileName == null) {
                if (!targetPath.exists()) {
                    targetPath.mkdirs();
                    updateLogs(strCreatedSuccessfully + targetPath.getAbsolutePath(), true);
                }
                targetPath.setWritable(true);
                targetPath.setExecutable(true);

                copyAttr = new FileSynchronizeAttributes();
                copyAttr.setContinueWhenError(continueCheck.isSelected());
                copyAttr.setCopyAttrinutes(copyAttrCheck.isSelected());
                copyAttr.setCopyEmpty(copyEmptyCheck.isSelected());
                copyAttr.setConditionalCopy(isConditional);
                copyAttr.setCopyExisted(copyExistedCheck.isSelected());
                copyAttr.setCopyHidden(copyHiddenCheck.isSelected());
                copyAttr.setCopyNew(copyNewCheck.isSelected());
                copyAttr.setCopySubdir(copySubdirCheck.isSelected());
                copyAttr.setNotCopySome(notCopyCheck.isSelected());
                copyAttr.setOnlyCopyReadonly(copyReadonlyCheck.isSelected());
                List<String> notCopy = new ArrayList<>();
                if (copyAttr.isNotCopySome() && notCopyInput.getText() != null && !notCopyInput.getText().trim().isEmpty()) {
                    String[] s = notCopyInput.getText().split(",");
                    notCopy.addAll(Arrays.asList(s));
                }
                copyAttr.setNotCopyNames(notCopy);
                copyAttr.setOnlyCopyModified(copyModifiedCheck.isSelected());
                copyAttr.setModifyAfter(0);
                if (copyAttr.isOnlyCopyModified() && modifyAfterInput.getValue() != null) {
                    copyAttr.setModifyAfter(DateTools.localDate2Date(modifyAfterInput.getValue()).getTime());
                }
                copyAttr.setDeleteNotExisteds(deleteNonExistedCheck.isSelected());

                if (!copyAttr.isCopyNew() && !copyAttr.isCopyExisted() && !copyAttr.isCopySubdir()) {
                    alertInformation(Languages.message("NothingCopy"));
                    return false;
                }
                // In case that the source path itself is in blacklist
                if (copyAttr.isNotCopySome()) {
                    List<String> keys = copyAttr.getNotCopyNames();
                    String srcName = sourcePath.getName();
                    for (String key : keys) {
                        if (srcName.contains(key)) {
                            alertInformation(Languages.message("NothingCopy"));
                            return false;
                        }
                    }
                }

                initLogs();
                logsTextArea.setText(Languages.message("SourcePath") + ": " + sourcePathInput.getText() + "\n");
                logsTextArea.appendText(Languages.message("TargetPath") + ": " + targetPath.getAbsolutePath() + "\n");

                strFailedCopy = Languages.message("FailedCopy") + ": ";
                strCreatedSuccessfully = Languages.message("CreatedSuccessfully") + ": ";
                strCopySuccessfully = Languages.message("CopySuccessfully") + ": ";
                strDeleteSuccessfully = Languages.message("DeletedSuccessfully") + ": ";
                strFailedDelete = Languages.message("FailedDelete") + ": ";
                strFileDeleteSuccessfully = Languages.message("FileDeletedSuccessfully") + ": ";
                strDirectoryDeleteSuccessfully = Languages.message("DirectoryDeletedSuccessfully") + ": ";

                startHandle = true;
                lastFileName = null;

            } else {
                startHandle = false;
                updateLogs(Languages.message("LastHanldedFile") + " " + lastFileName, true);
            }

            processStartTime = new Date();

            return true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @FXML
    @Override
    public void startAction() {
        try {
            if (!initAttributes()) {
                return;
            }
            updateInterface("Started");
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    @Override
                    protected boolean handle() {
                        boolean done = false;
                        if (copyAttr.isConditionalCopy()) {
                            paused = false;
                            done = conditionalCopy(sourcePath, targetPath);
                        } else {
                            if (!paused && targetPath.exists()) {
                                updateLogs(Languages.message("ClearingTarget"), true);
                                if (clearDir(targetPath, false)) {
                                    updateLogs(Languages.message("TargetCleared"), true);
                                } else if (!copyAttr.isContinueWhenError()) {
                                    updateLogs(Languages.message("FailClearTarget"), true);
                                    return false;
                                }
                            }
                            paused = false;
                            done = copyWholeDirectory(sourcePath, targetPath);
                        }
                        if (!done || task == null || task.isCancelled()) {
                            return false;
                        }
                        if (deleteSourceCheck.isSelected()) {
                            done = FileDeleteTools.deleteDir(sourcePath);
                            updateLogs(Languages.message("SourcePathCleared"), true);
                        }
                        return done;
                    }

                    @Override
                    protected void whenSucceeded() {
                        updateInterface("Done");
                    }

                    @Override
                    protected void cancelled() {
                        super.cancelled();
                        updateInterface("Canceled");
                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        updateInterface("Failed");
                    }
                };
                start(task, false);
            }

        } catch (Exception e) {
            updateInterface("Failed");
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void updateInterface(final String newStatus) {
        currentStatus = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (paused) {
                    updateLogs(Languages.message("Paused"), true);
                } else {
                    updateLogs(Languages.message(newStatus), true);
                }
                switch (newStatus) {
                    case "Started":
                        operationBarController.statusLabel.setText(Languages.message("Handling...") + " "
                                + Languages.message("StartTime")
                                + ": " + DateTools.datetimeToString(processStartTime));
                        StyleTools.setNameIcon(startButton, Languages.message("Stop"), "iconStop.png");
                        startButton.applyCss();
                        startButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                cancelProcess(event);
                            }
                        });
                        operationBarController.pauseButton.setVisible(true);
                        operationBarController.pauseButton.setDisable(false);
                        StyleTools.setNameIcon(operationBarController.pauseButton, Languages.message("Pause"), "iconPause.png");
                        operationBarController.pauseButton.applyCss();
                        operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                pauseProcess(event);
                            }
                        });
                        operationBarController.progressBar.setProgress(-1);
                        disableControls(true);
                        break;

                    case "Done":
                    default:
                        if (paused) {
                            StyleTools.setNameIcon(startButton, Languages.message("Stop"), "iconStop.png");
                            startButton.applyCss();
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelProcess(event);
                                }
                            });
                            operationBarController.pauseButton.setVisible(true);
                            operationBarController.pauseButton.setDisable(false);
                            StyleTools.setNameIcon(operationBarController.pauseButton, Languages.message("Start"), "iconStart.png");
                            operationBarController.pauseButton.applyCss();
                            operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startAction();
                                }
                            });
                            disableControls(true);
                        } else {
                            StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
                            startButton.applyCss();
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startAction();
                                }
                            });
                            operationBarController.pauseButton.setVisible(false);
                            operationBarController.pauseButton.setDisable(true);
                            operationBarController.progressBar.setProgress(1);
                            disableControls(false);
                        }
                        donePost();

                }
            }
        });
    }

    @Override
    public void disableControls(boolean disable) {
        paraBox.setDisable(disable);
        batchTabPane.getSelectionModel().select(logsTab);
    }

    @Override
    public void showCost() {
        if (operationBarController.statusLabel == null) {
            return;
        }
        long cost = new Date().getTime() - processStartTime.getTime();
        double avg = 0;
        if (copyAttr.getCopiedFilesNumber() != 0) {
            avg = DoubleTools.scale3((double) cost / copyAttr.getCopiedFilesNumber());
        }
        String s;
        if (paused) {
            s = Languages.message("Paused");
        } else {
            s = Languages.message(currentStatus);
        }
        s += ". " + Languages.message("HandledFiles") + ": " + copyAttr.getCopiedFilesNumber() + " "
                + Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime) + ". "
                + Languages.message("Average") + ": " + avg + " " + Languages.message("SecondsPerItem") + ". "
                + Languages.message("StartTime") + ": " + DateTools.datetimeToString(processStartTime) + ", "
                + Languages.message("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

    @Override
    public void donePost() {
        showCost();
        updateLogs(Languages.message("StartTime") + ": " + DateTools.datetimeToString(processStartTime) + "   "
                + Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime), false, true);
        updateLogs(Languages.message("TotalCheckedFiles") + ": " + copyAttr.getTotalFilesNumber() + "   "
                + Languages.message("TotalCheckedDirectories") + ": " + copyAttr.getTotalDirectoriesNumber() + "   "
                + Languages.message("TotalCheckedSize") + ": " + FileTools.showFileSize(copyAttr.getTotalSize()), false, true);
        updateLogs(Languages.message("TotalCopiedFiles") + ": " + copyAttr.getCopiedFilesNumber() + "   "
                + Languages.message("TotalCopiedDirectories") + ": " + copyAttr.getCopiedDirectoriesNumber() + "   "
                + Languages.message("TotalCopiedSize") + ": " + FileTools.showFileSize(copyAttr.getCopiedSize()), false, true);
        if (copyAttr.isConditionalCopy() && copyAttr.isDeleteNotExisteds()) {
            updateLogs(Languages.message("TotalDeletedFiles") + ": " + copyAttr.getDeletedFiles() + "   "
                    + Languages.message("TotalDeletedDirectories") + ": " + copyAttr.getDeletedDirectories() + "   "
                    + Languages.message("TotalDeletedSize") + ": " + FileTools.showFileSize(copyAttr.getDeletedSize()), false, true);
        }

        if (operationBarController.miaoCheck.isSelected()) {
            SoundTools.miao3();
        }

        if (operationBarController.openCheck.isSelected()) {
            openTarget(null);
        }

    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {
            browseURI(targetPathInputController.file().toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public double countAverageTime(long cost) {
        double avg = 0;
        if (copyAttr.getCopiedFilesNumber() != 0) {
            avg = DoubleTools.scale3((double) cost / copyAttr.getCopiedFilesNumber());
        }
        return avg;
    }

    protected void checkIsConditional() {
        RadioButton sort = (RadioButton) copyGroup.getSelectedToggle();
        if (!Languages.message("CopyConditionally").equals(sort.getText())) {
            condBox.setDisable(true);
            isConditional = false;
        } else {
            condBox.setDisable(false);
            isConditional = true;
        }
    }

    protected boolean conditionalCopy(File sourcePath, File targetPath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
                updateLogs(message("TargetPathShouldNotSourceSub") + ": " + targetPath);
                return false;
            }
            if (copyAttr.isDeleteNotExisteds()
                    && !deleteNonExisted(sourcePath, targetPath) && !copyAttr.isContinueWhenError()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
            if (files == null) {
                return false;
            }
            String srcFileName;
            long len;
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                srcFileName = srcFile.getAbsolutePath();
                len = srcFile.length();
                if (!startHandle) {
                    if (lastFileName.equals(srcFileName)) {
                        startHandle = true;
                        updateLogs(Languages.message("ReachFile") + " " + lastFileName, true);
                    }
                    if (srcFile.isFile()) {
                        continue;
                    }
                } else {
                    if (srcFile.isFile()) {
                        copyAttr.setTotalFilesNumber(copyAttr.getTotalFilesNumber() + 1);
                    } else if (srcFile.isDirectory()) {
                        copyAttr.setTotalDirectoriesNumber(copyAttr.getTotalDirectoriesNumber() + 1);
                    }
                    copyAttr.setTotalSize(copyAttr.getTotalSize() + srcFile.length());
                }
                if (srcFile.isHidden() && !copyAttr.isCopyHidden()) {
                    continue;
                }
                if (srcFile.canWrite() && copyAttr.isOnlyCopyReadonly()) {
                    continue;
                }
                if (copyAttr.isNotCopySome()) {
                    List<String> blacks = copyAttr.getNotCopyNames();
                    String srcName = srcFile.getName();
                    boolean black = false;
                    for (String b : blacks) {
                        if (srcName.contains(b)) {
                            black = true;
                            break;
                        }
                    }
                    if (black) {
                        continue;
                    }
                }
                File tFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyAttr.isOnlyCopyModified()) {
                        if (srcFile.lastModified() <= copyAttr.getModifyAfter()) {
                            continue;
                        }
                    }
                    if (tFile.exists()) {
                        if (!copyAttr.isCopyExisted()) {
                            continue;
                        }
                        if (copyAttr.isOnlyCopyModified()) {
                            if (srcFile.lastModified() <= tFile.lastModified()) {
                                continue;
                            }
                        }
                    } else if (!copyAttr.isCopyNew()) {
                        continue;
                    }
                    if (copyFile(srcFile, tFile)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                        lastFileName = srcFileName;
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory() && copyAttr.isCopySubdir()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(Languages.message("HandlingDirectory") + " " + srcFileName, true);
                    }
                    if (srcFile.listFiles() == null && !copyAttr.isCopyEmpty()) {
                        continue;
                    }
                    if (startHandle && !tFile.exists()) {
                        tFile.mkdirs();
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCreatedSuccessfully + tFile.getAbsolutePath());
                        }
                    }
                    if (conditionalCopy(srcFile, tFile)) {
                        if (startHandle) {
                            copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                            copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getCopiedDirectoriesNumber() + "  " + strCopySuccessfully
                                        + srcFileName + " -> " + tFile.getAbsolutePath());
                            }
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + tFile.getAbsolutePath());
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            updateLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    protected boolean copyWholeDirectory(File sourcePath, File targetPath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
                updateLogs(message("TargetPathShouldNotSourceSub") + ": " + targetPath);
                return false;
            }
            File[] files = sourcePath.listFiles();
            if (files == null) {
                return false;
            }
            String srcFileName;
            long len;
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                srcFileName = srcFile.getAbsolutePath();
                len = srcFile.length();
                if (!startHandle) {
                    if (lastFileName.equals(srcFileName)) {
                        startHandle = true;
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(Languages.message("ReachFile") + " " + lastFileName, true);
                        }
                    }
                    if (srcFile.isFile()) {
                        continue;
                    }
                } else {
                    if (srcFile.isFile()) {
                        copyAttr.setTotalFilesNumber(copyAttr.getTotalFilesNumber() + 1);
                    } else if (srcFile.isDirectory()) {
                        copyAttr.setTotalDirectoriesNumber(copyAttr.getTotalDirectoriesNumber() + 1);
                    }
                    copyAttr.setTotalSize(copyAttr.getTotalSize() + srcFile.length());
                }
                File tFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyFile(srcFile, tFile)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                        lastFileName = srcFileName;
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + tFile.getAbsolutePath());
                        }
                        return false;
                    }
                } else if (srcFile.isDirectory()) {
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(Languages.message("HandlingDirectory") + " " + srcFileName, true);
                    }
                    if (startHandle) {
                        tFile.mkdirs();
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strCreatedSuccessfully + tFile.getAbsolutePath());
                        }
                    }
                    if (copyWholeDirectory(srcFile, tFile)) {
                        if (startHandle) {
                            copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                            copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getCopiedDirectoriesNumber() + "  " + strCopySuccessfully
                                        + srcFileName + " -> " + tFile.getAbsolutePath());
                            }
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + tFile.getAbsolutePath());
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            updateLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    // clearDir can not be paused to avoid logic messed.
    protected boolean clearDir(File dir, boolean record) {
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        for (File file : files) {
            long len = file.length();
            String filename = file.getAbsolutePath();
            if (file.isDirectory()) {
                if (clearDir(file, record)) {
                    try {
                        FileDeleteTools.delete(file);
                        if (record) {
                            copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                            copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getDeletedDirectories() + "  " + strDirectoryDeleteSuccessfully + filename);
                            }
                        }
                    } catch (Exception e) {
                        if (record) {
                            copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                            copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                            updateLogs(strFailedDelete + filename);
                        }
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    if (record) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedDelete + filename);
                        }
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
            try {
                FileDeleteTools.delete(file);
                if (record) {
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(copyAttr.getDeletedFiles() + "  " + strFileDeleteSuccessfully + filename);
                    }
                }
            } catch (Exception e) {
                if (record) {
                    copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    updateLogs(strFailedDelete + filename);
                }
                if (!copyAttr.isContinueWhenError()) {
                    return false;
                }
            }
        }
        return true; // When return true, it is not necessary that the dir is cleared.
    }

    protected boolean copyFile(File sourceFile, File targetFile) {
        try {
            if (task == null || task.isCancelled()
                    || sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            if (!targetFile.exists()) {
                if (copyAttr.isCopyAttrinutes()) {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()));
                }
            } else if (!copyAttr.isCanReplace() || targetFile.isDirectory()) {
                return false;
            } else if (copyAttr.isCopyAttrinutes()) {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected boolean deleteNonExisted(File sourcePath, File targetPath) {
        if (!copyAttr.isDeleteNotExisteds() || !targetPath.isDirectory()) {
            return true;
        }
        File[] files = targetPath.listFiles();
        if (files == null) {
            return true;
        }
        for (File tFile : files) {
            if (task == null || task.isCancelled()) {
                return false;
            }
            File srcFile = new File(sourcePath + File.separator + tFile.getName());
            if (srcFile.exists()) {
                continue;
            }
            long len = tFile.length();
            String filename = tFile.getAbsolutePath();
            if (tFile.isDirectory()) {
                if (clearDir(tFile, true)) {
                    try {
                        FileDeleteTools.delete(tFile);
                        copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                        copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strDirectoryDeleteSuccessfully + filename);
                        }
                    } catch (Exception e) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strFailedDelete + filename);
                        }
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strFailedDelete + filename);
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            } else {
                try {
                    FileDeleteTools.delete(tFile);
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(strFileDeleteSuccessfully + filename);
                    }
                } catch (Exception e) {
                    copyAttr.setFailedDeletedFiles(copyAttr.getFailedDeletedFiles() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    updateLogs(strFailedDelete + filename);
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
        }
        return true; // When return true, it is not necessary that all things are good.
    }

}
