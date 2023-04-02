package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-9
 * @License Apache License Version 2.0
 */
public class FilesArrangeController extends BaseBatchFileController {

    protected String lastFileName;
    private boolean startHandle, isCopy, byModifyTime;
    private int dirType, replaceType;
    protected String renameAppdex = "-m";
    protected String strFailedCopy, strCreatedSuccessfully, strCopySuccessfully, strDeleteSuccessfully, strFailedDelete;
    protected FileSynchronizeAttributes copyAttr;

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
    protected ControlPathInput targetPathInputController;
    @FXML
    protected ToggleGroup filesGroup, byGroup, dirGroup, replaceGroup;
    @FXML
    protected VBox dirsBox, conditionsBox, logsBox;
    @FXML
    protected RadioButton copyRadio, moveRadio, replaceModifiedRadio, replaceRadio, renameRadio, notCopyRadio;
    @FXML
    protected RadioButton modifiyTimeRadio, createTimeRadio, monthRadio, dayRadio, yearRadio;
    @FXML
    protected CheckBox handleSubdirCheck;

    public FilesArrangeController() {
        baseTitle = Languages.message("FilesArrangement");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initDirTab();
            initConditionTab();

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

    private void initDirTab() {

    }

    private void initConditionTab() {

        handleSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_toggle, Boolean new_toggle) {
                UserConfig.setBoolean(baseName + "Subdir", isCopy);
            }
        });
        handleSubdirCheck.setSelected(UserConfig.getBoolean(baseName + "Subdir", true));

        filesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton selected = (RadioButton) filesGroup.getSelectedToggle();
                isCopy = Languages.message("Copy").equals(selected.getText());
                UserConfig.setBoolean(baseName + "Copy", isCopy);
            }
        });
        if (UserConfig.getBoolean(baseName + "Copy", true)) {
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
        String replaceSelect = UserConfig.getString(baseName + "Exist", "ReplaceModified");
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
                byModifyTime = Languages.message("ModifyTime").equals(selected.getText());
                UserConfig.setBoolean(baseName + "ModifyTime", byModifyTime);
            }
        });
        if (UserConfig.getBoolean(baseName + "ModifyTime", true)) {
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
        String dirSelect = UserConfig.getString(baseName + "Category", "Month");
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
        if (Languages.message("ReplaceModified").equals(selected.getText())) {
            replaceType = ReplaceType.ReplaceModified;
            UserConfig.setString(baseName + "Exist", "ReplaceModified");
        } else if (Languages.message("NotCopy").equals(selected.getText())) {
            replaceType = ReplaceType.NotCopy;
            UserConfig.setString(baseName + "Exist", "NotCopy");
        } else if (Languages.message("Replace").equals(selected.getText())) {
            replaceType = ReplaceType.Replace;
            UserConfig.setString(baseName + "Exist", "Replace");
        } else if (Languages.message("Rename").equals(selected.getText())) {
            replaceType = ReplaceType.Rename;
            UserConfig.setString(baseName + "Exist", "Rename");
        } else {
            replaceType = ReplaceType.ReplaceModified;
            UserConfig.setString(baseName + "Exist", "ReplaceModified");
        }

    }

    private void checkDirType() {
        RadioButton selected = (RadioButton) dirGroup.getSelectedToggle();
        if (Languages.message("Year").equals(selected.getText())) {
            dirType = DirType.Year;
            UserConfig.setString(baseName + "Category", "Year");
        } else if (Languages.message("Month").equals(selected.getText())) {
            dirType = DirType.Month;
            UserConfig.setString(baseName + "Category", "Month");
        } else if (Languages.message("Day").equals(selected.getText())) {
            dirType = DirType.Day;
            UserConfig.setString(baseName + "Category", "Day");
        } else {
            dirType = DirType.Month;
            UserConfig.setString(baseName + "Category", "Month");
        }
    }

    protected boolean initAttributes() {
        try {
            sourcePath = new File(sourcePathInput.getText());
            if (!paused || lastFileName == null) {
                copyAttr = new FileSynchronizeAttributes();

                targetPath = targetPathInputController.file;
                if (!targetPath.exists()) {
                    targetPath.mkdirs();
                    updateLogs(strCreatedSuccessfully + targetPath.getAbsolutePath(), true);
                }
                targetPath.setWritable(true);
                targetPath.setExecutable(true);

                initLogs();
                logsTextArea.setText(Languages.message("SourcePath") + ": " + sourcePathInput.getText() + "\n");
                logsTextArea.appendText(Languages.message("TargetPath") + ": " + targetPath.getAbsolutePath() + "\n");

                strFailedCopy = Languages.message("FailedCopy") + ": ";
                strCreatedSuccessfully = Languages.message("CreatedSuccessfully") + ": ";
                strCopySuccessfully = Languages.message("CopySuccessfully") + ": ";
                strDeleteSuccessfully = Languages.message("DeletedSuccessfully") + ": ";
                strFailedDelete = Languages.message("FailedDelete") + ": ";

                startHandle = true;
                lastFileName = null;

            } else {
                startHandle = false;
                updateLogs(Languages.message("LastHanldedFile") + " " + lastFileName, true);
            }

            paused = false;
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
            targetFilesCount = 0;
            targetFiles = new LinkedHashMap<>();
            updateInterface("Started");
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

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

                    @Override
                    protected void finalAction() {
                        super.finalAction();
                        task = null;
                        afterTask();
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
                try {
                    if (paused) {
                        updateLogs(Languages.message("Paused"), true, true);
                    } else {
                        updateLogs(Languages.message(newStatus), true, true);
                    }
                    switch (newStatus) {
                        case "Started":
                            operationBarController.statusInput.setText(Languages.message("Handling...") + " "
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
                    }

                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            }
        });

    }

    @Override
    public void disableControls(boolean disable) {
        paraBox.setDisable(disable);
        tabPane.getSelectionModel().select(logsTab);
    }

    @Override
    public void showCost() {
        if (operationBarController.statusInput == null) {
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
        operationBarController.statusInput.setText(s);
    }

    @Override
    public void afterTask() {
        recordTargetFiles();
        updateLogs(Languages.message("StartTime") + ": " + DateTools.datetimeToString(processStartTime) + "   "
                + Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime), false, true);
        updateLogs(Languages.message("TotalCheckedFiles") + ": " + copyAttr.getTotalFilesNumber() + "   "
                + Languages.message("TotalCheckedDirectories") + ": " + copyAttr.getTotalDirectoriesNumber() + "   "
                + Languages.message("TotalCheckedSize") + ": " + FileTools.showFileSize(copyAttr.getTotalSize()), false, true);
        updateLogs(Languages.message("TotalCopiedFiles") + ": " + copyAttr.getCopiedFilesNumber() + "   "
                + Languages.message("TotalCopiedDirectories") + ": " + copyAttr.getCopiedDirectoriesNumber() + "   "
                + Languages.message("TotalCopiedSize") + ": " + FileTools.showFileSize(copyAttr.getCopiedSize()), false, true);
        if (!isCopy) {
            updateLogs(Languages.message("TotalDeletedFiles") + ": " + copyAttr.getDeletedFiles() + "   "
                    + Languages.message("TotalDeletedDirectories") + ": " + copyAttr.getDeletedDirectories() + "   "
                    + Languages.message("TotalDeletedSize") + ": " + FileTools.showFileSize(copyAttr.getDeletedSize()), false, true);
        }
        showCost();
        if (operationBarController.miaoCheck.isSelected()) {
            SoundTools.miao3();
        }

        if (operationBarController.openCheck.isSelected()) {
            openTarget();
        }

    }

    @FXML
    @Override
    public void openTarget() {
        try {
            browseURI(targetPathInputController.file.toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                        updateLogs(Languages.message("ReachFile") + " " + lastFileName, true, true);
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
                            updateLogs(Languages.message("HandlingDirectory") + " " + srcFileName, true, true);
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
                        c.setTimeInMillis(FileTools.createTime(srcFileName));
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
                        FileDeleteTools.delete(srcFile);
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
            MyBoxLog.error(e.toString());
            return false;
        }

    }

    private File renameExistedFile(File file) {
        if (!file.exists()) {
            return file;
        }
        String newName = FileNameTools.prefix(file.getName()) + renameAppdex + "." + FileNameTools.suffix(file.getName());
        File newFile = new File(file.getParent() + File.separator + newName);
        if (!newFile.exists()) {
            return newFile;
        }
        return renameExistedFile(newFile);
    }

}
