package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-9
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesArrangeController extends FilesBatchController {

    protected String lastFileName;
    private boolean startHandle, isCopy, byModifyTime;
    private int dirType, replaceType;
    protected String renameAppdex = "-m";
    protected String strFailedCopy, strCreatedSuccessfully, strCopySuccessfully, strDeleteSuccessfully, strFailedDelete;
    protected FileSynchronizeAttributes copyAttr;
    private final String FileArrangeSubdirKey, FileArrangeCopyKey, FileArrangeExistedKey, FileArrangeModifyTimeKey, FileArrangeCategoryKey;

    private class DirType {

        private static final int Year = 0;
        private static final int Month = 1;
        private static final int Day = 2;
    }

    private class ReplaceType {

        private static final int ReplaceModified = 0;
        private static final int Replace = 1;
        private static final int NotCopy = 2;
        private static final int Rename = 3;
    }

    @FXML
    private ToggleGroup filesGroup, byGroup, dirGroup, replaceGroup;
    @FXML
    protected VBox dirsBox, conditionsBox, logsBox;
    @FXML
    private RadioButton copyRadio, moveRadio, replaceModifiedRadio, replaceRadio, renameRadio, notCopyRadio;
    @FXML
    private RadioButton modifiyTimeRadio, createTimeRadio, monthRadio, dayRadio, yearRadio;
    @FXML
    private CheckBox handleSubdirCheck;

    public FilesArrangeController() {
        baseTitle = AppVariables.message("FilesArrangement");

        targetPathKey = "FilesArrageTargetPath";
        sourcePathKey = "FilesArrageSourcePath";
        FileArrangeSubdirKey = "FileArrangeSubdirKey";
        FileArrangeCopyKey = "FileArrangeCopyKey";
        FileArrangeExistedKey = "FileArrangeExistedKey";
        FileArrangeModifyTimeKey = "FileArrangeModifyTimeKey";
        FileArrangeCategoryKey = "FileArrangeCategoryKey";

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initDirTab();
            initConditionTab();

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(sourcePathInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
            );

            operationBarController.openTargetButton.disableProperty().bind(
                    startButton.disableProperty()
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initDirTab() {

    }

    private void initConditionTab() {

        handleSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_toggle, Boolean new_toggle) {
                AppVariables.setUserConfigValue(FileArrangeSubdirKey, isCopy);
            }
        });
        handleSubdirCheck.setSelected(AppVariables.getUserConfigBoolean(FileArrangeSubdirKey, true));

        filesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton selected = (RadioButton) filesGroup.getSelectedToggle();
                isCopy = message("Copy").equals(selected.getText());
                AppVariables.setUserConfigValue(FileArrangeCopyKey, isCopy);
            }
        });
        if (AppVariables.getUserConfigBoolean(FileArrangeCopyKey, true)) {
            copyRadio.setSelected(true);
            isCopy = true;
        } else {
            moveRadio.setSelected(true);
            isCopy = false;
        }

        replaceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkReplaceType();
            }
        });
        String replaceSelect = AppVariables.getUserConfigValue(FileArrangeExistedKey, "ReplaceModified");
        switch (replaceSelect) {
            case "ReplaceModified":
                replaceModifiedRadio.setSelected(true);
                break;
            case "Replace":
                replaceRadio.setSelected(true);
                break;
            case "Rename":
                renameRadio.setSelected(true);
                break;
            case "NotCopy":
                notCopyRadio.setSelected(true);
                break;
        }
        checkReplaceType();

        byGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton selected = (RadioButton) byGroup.getSelectedToggle();
                byModifyTime = message("ModifyTime").equals(selected.getText());
                AppVariables.setUserConfigValue(FileArrangeModifyTimeKey, byModifyTime);
            }
        });
        if (AppVariables.getUserConfigBoolean(FileArrangeModifyTimeKey, true)) {
            modifiyTimeRadio.setSelected(true);
            byModifyTime = true;
        } else {
            createTimeRadio.setSelected(true);
            byModifyTime = false;
        }

        dirGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkDirType();
            }
        });
        String dirSelect = AppVariables.getUserConfigValue(FileArrangeCategoryKey, "Month");
        switch (dirSelect) {
            case "Year":
                yearRadio.setSelected(true);
                break;
            case "Month":
                monthRadio.setSelected(true);
                break;
            case "Day":
                dayRadio.setSelected(true);
                break;
        }
        checkDirType();

    }

    private void checkReplaceType() {
        RadioButton selected = (RadioButton) replaceGroup.getSelectedToggle();
        if (message("ReplaceModified").equals(selected.getText())) {
            replaceType = ReplaceType.ReplaceModified;
            AppVariables.setUserConfigValue(FileArrangeExistedKey, "ReplaceModified");
        } else if (message("NotCopy").equals(selected.getText())) {
            replaceType = ReplaceType.NotCopy;
            AppVariables.setUserConfigValue(FileArrangeExistedKey, "NotCopy");
        } else if (message("Replace").equals(selected.getText())) {
            replaceType = ReplaceType.Replace;
            AppVariables.setUserConfigValue(FileArrangeExistedKey, "Replace");
        } else if (message("Rename").equals(selected.getText())) {
            replaceType = ReplaceType.Rename;
            AppVariables.setUserConfigValue(FileArrangeExistedKey, "Rename");
        } else {
            replaceType = ReplaceType.ReplaceModified;
            AppVariables.setUserConfigValue(FileArrangeExistedKey, "ReplaceModified");
        }

    }

    private void checkDirType() {
        RadioButton selected = (RadioButton) dirGroup.getSelectedToggle();
        if (message("Year").equals(selected.getText())) {
            dirType = DirType.Year;
            AppVariables.setUserConfigValue(FileArrangeCategoryKey, "Year");
        } else if (message("Month").equals(selected.getText())) {
            dirType = DirType.Month;
            AppVariables.setUserConfigValue(FileArrangeCategoryKey, "Month");
        } else if (message("Day").equals(selected.getText())) {
            dirType = DirType.Day;
            AppVariables.setUserConfigValue(FileArrangeCategoryKey, "Day");
        } else {
            dirType = DirType.Month;
            AppVariables.setUserConfigValue(FileArrangeCategoryKey, "Month");
        }
    }

    protected boolean initAttributes() {
        try {
            sourcePath = new File(sourcePathInput.getText());
            if (!paused || lastFileName == null) {
                copyAttr = new FileSynchronizeAttributes();

                initLogs();
                logsTextArea.setText(AppVariables.message("SourcePath") + ": " + sourcePathInput.getText() + "\n");
                logsTextArea.appendText(AppVariables.message("TargetPath") + ": " + targetPathInput.getText() + "\n");

                strFailedCopy = AppVariables.message("FailedCopy") + ": ";
                strCreatedSuccessfully = AppVariables.message("CreatedSuccessfully") + ": ";
                strCopySuccessfully = AppVariables.message("CopySuccessfully") + ": ";
                strDeleteSuccessfully = AppVariables.message("DeletedSuccessfully") + ": ";
                strFailedDelete = AppVariables.message("FailedDelete") + ": ";

                targetPath = new File(targetPathInput.getText());
                if (!targetPath.exists()) {
                    targetPath.mkdirs();
                    updateLogs(strCreatedSuccessfully + targetPath.getAbsolutePath(), true);
                }
                targetPath.setWritable(true);
                targetPath.setExecutable(true);

                startHandle = true;
                lastFileName = null;

            } else {
                startHandle = false;
                updateLogs(message("LastHanldedFile") + " " + lastFileName, true);
            }

            paused = false;
            processStartTime = new Date();

            return true;

        } catch (Exception e) {
            logger.error(e.toString());
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
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        return arrangeFiles(sourcePath);
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
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }

    }

    @Override
    public void updateInterface(final String newStatus) {
        currentStatus = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (paused) {
                        updateLogs(AppVariables.message("Paused"), true, true);
                    } else {
                        updateLogs(AppVariables.message(newStatus), true, true);
                    }
                    switch (newStatus) {
                        case "Started":
                            operationBarController.statusLabel.setText(message("Handling...") + " "
                                    + message("StartTime")
                                    + ": " + DateTools.datetimeToString(processStartTime));
                            startButton.setText(AppVariables.message("Cancel"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelProcess(event);
                                }
                            });
                            operationBarController.pauseButton.setVisible(true);
                            operationBarController.pauseButton.setDisable(false);
                            operationBarController.pauseButton.setText(AppVariables.message("Pause"));
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
                                startButton.setText(AppVariables.message("Cancel"));
                                startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        cancelProcess(event);
                                    }
                                });
                                operationBarController.pauseButton.setVisible(true);
                                operationBarController.pauseButton.setDisable(false);
                                operationBarController.pauseButton.setText(AppVariables.message("Continue"));
                                operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startAction();
                                    }
                                });
                                disableControls(true);
                            } else {
                                startButton.setText(AppVariables.message("Start"));
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

                } catch (Exception e) {
                    logger.error(e.toString());
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
            s = message("Paused");
        } else {
            s = message(currentStatus);
        }
        s += ". " + message("HandledFiles") + ": " + copyAttr.getCopiedFilesNumber() + " "
                + message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime) + ". "
                + message("Average") + ": " + avg + " " + message("SecondsPerItem") + ". "
                + message("StartTime") + ": " + DateTools.datetimeToString(processStartTime) + ", "
                + message("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

    @Override
    public void donePost() {
        showCost();
        updateLogs(message("StartTime") + ": " + DateTools.datetimeToString(processStartTime) + "   "
                + AppVariables.message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime), false, true);
        updateLogs(AppVariables.message("TotalCheckedFiles") + ": " + copyAttr.getTotalFilesNumber() + "   "
                + AppVariables.message("TotalCheckedDirectories") + ": " + copyAttr.getTotalDirectoriesNumber() + "   "
                + AppVariables.message("TotalCheckedSize") + ": " + FileTools.showFileSize(copyAttr.getTotalSize()), false, true);
        updateLogs(AppVariables.message("TotalCopiedFiles") + ": " + copyAttr.getCopiedFilesNumber() + "   "
                + AppVariables.message("TotalCopiedDirectories") + ": " + copyAttr.getCopiedDirectoriesNumber() + "   "
                + AppVariables.message("TotalCopiedSize") + ": " + FileTools.showFileSize(copyAttr.getCopiedSize()), false, true);
        if (!isCopy) {
            updateLogs(AppVariables.message("TotalDeletedFiles") + ": " + copyAttr.getDeletedFiles() + "   "
                    + AppVariables.message("TotalDeletedDirectories") + ": " + copyAttr.getDeletedDirectories() + "   "
                    + AppVariables.message("TotalDeletedSize") + ": " + FileTools.showFileSize(copyAttr.getDeletedSize()), false, true);
        }

        if (operationBarController.miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }

        if (operationBarController.openCheck.isSelected()) {
            openTarget(null);
        }

    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {
            browseURI(new File(targetPathInput.getText()).toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected boolean arrangeFiles(File sourcePath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
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
                        updateLogs(message("ReachFile") + " " + lastFileName, true, true);
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
                if (srcFile.isDirectory()) {
                    if (handleSubdirCheck.isSelected()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(message("HandlingDirectory") + " " + srcFileName, true, true);
                        }
                        arrangeFiles(srcFile);
                    }
                    continue;
                } else if (!startHandle) {
                    continue;
                }
                try {
                    Calendar c = Calendar.getInstance();
                    if (byModifyTime) {
                        c.setTimeInMillis(srcFile.lastModified());
                    } else {
                        c.setTimeInMillis(FileTools.getFileCreateTime(srcFileName));
                    }
                    File path;
                    String month, day;
                    if (c.get(Calendar.MONTH) > 8) {
                        month = (c.get(Calendar.MONTH) + 1) + "";
                    } else {
                        month = "0" + (c.get(Calendar.MONTH) + 1);
                    }
                    if (c.get(Calendar.DAY_OF_MONTH) > 9) {
                        day = c.get(Calendar.DAY_OF_MONTH) + "";
                    } else {
                        day = "0" + c.get(Calendar.DAY_OF_MONTH);
                    }
                    switch (dirType) {
                        case DirType.Year:
                            path = new File(targetPath + File.separator + c.get(Calendar.YEAR));
                            if (!path.exists()) {
                                path.mkdirs();
                                if (verboseCheck == null || verboseCheck.isSelected()) {
                                    updateLogs(strCreatedSuccessfully + path.getAbsolutePath(), true, true);
                                }
                            }
                            break;
                        case DirType.Day:
                            path = new File(targetPath + File.separator + c.get(Calendar.YEAR)
                                    + File.separator + c.get(Calendar.YEAR) + "-" + month
                                    + File.separator + c.get(Calendar.YEAR) + "-" + month + "-" + day);
                            if (!path.exists()) {
                                path.mkdirs();
                                if (verboseCheck == null || verboseCheck.isSelected()) {
                                    updateLogs(strCreatedSuccessfully + path.getAbsolutePath(), true, true);
                                }
                            }
                            break;
                        case DirType.Month:
                        default:
                            path = new File(targetPath + File.separator + c.get(Calendar.YEAR)
                                    + File.separator + c.get(Calendar.YEAR) + "-" + month);
                            if (!path.exists()) {
                                path.mkdirs();
                                if (verboseCheck == null || verboseCheck.isSelected()) {
                                    updateLogs(strCreatedSuccessfully + path.getAbsolutePath(), true, true);
                                }
                            }
                            break;
                    }
                    File newFile = new File(path.getAbsolutePath() + File.separator + srcFile.getName());
                    if (newFile.exists()) {
                        switch (replaceType) {
                            case ReplaceType.NotCopy:
                                continue;
                            case ReplaceType.Rename:
                                newFile = renameExistedFile(newFile);
                                break;
                            case ReplaceType.Replace:
                                break;
                            case ReplaceType.ReplaceModified:
                                if (srcFile.lastModified() <= newFile.lastModified()) {
                                    continue;
                                }
                        }
                    }
                    Files.copy(Paths.get(srcFileName), Paths.get(newFile.getAbsolutePath()),
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

                    if (!isCopy) {
                        srcFile.delete();
                        copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(strDeleteSuccessfully + srcFileName, true, true);
                        }
                    }
                    copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                    copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                + srcFileName + " -> " + newFile.getAbsolutePath(), true, true);
                    }
                    lastFileName = srcFileName;

                } catch (Exception e) {
                    updateLogs(strFailedCopy + srcFileName + "\n" + e.toString(), true, true);
                }

            }

            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }

    }

    private File renameExistedFile(File file) {
        if (!file.exists()) {
            return file;
        }
        String newName = FileTools.getFilePrefix(file.getName()) + renameAppdex + "." + FileTools.getFileSuffix(file.getName());
        File newFile = new File(file.getParent() + File.separator + newName);
        if (!newFile.exists()) {
            return newFile;
        }
        return renameExistedFile(newFile);
    }

}
