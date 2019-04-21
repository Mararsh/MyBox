package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mara.mybox.controller.base.BatchBaseController;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ValueTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-7-9
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesArrangeController extends BatchBaseController {

    protected String lastFileName;
    protected Date startTime;
    private boolean startHandle, isCopy, byModifyTime;
    private int dirType, replaceType;
    protected File sourcePath;
    protected String renameAppdex = "-m";
    protected StringBuffer newLogs;
    protected int newlines, maxLines, totalLines, cacheLines = 200;
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
    protected TextField maxLinesinput;
    @FXML
    protected VBox dirsBox, conditionsBox, logsBox;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    private RadioButton copyRadio, moveRadio, replaceModifiedRadio, replaceRadio, renameRadio, notCopyRadio;
    @FXML
    private RadioButton modifiyTimeRadio, createTimeRadio, monthRadio, dayRadio, yearRadio;
    @FXML
    private CheckBox verboseCheck;

    public FilesArrangeController() {
        baseTitle = AppVaribles.getMessage("FilesArrangement");

        targetPathKey = "FilesArrageTargetPath";
        sourcePathKey = "FilesArrageSourcePath";
        FileArrangeSubdirKey = "FileArrangeSubdirKey";
        FileArrangeCopyKey = "FileArrangeCopyKey";
        FileArrangeExistedKey = "FileArrangeExistedKey";
        FileArrangeModifyTimeKey = "FileArrangeModifyTimeKey";
        FileArrangeCategoryKey = "FileArrangeCategoryKey";

        fileExtensionFilter = new ArrayList();
        fileExtensionFilter.add(new FileChooser.ExtensionFilter("*", "*.*"));
    }

    @Override
    public void initializeNext() {
        try {
            initDirTab();
            initConditionTab();

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(sourcePathInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
            );

            operationBarController.openTargetButton.disableProperty().bind(
                    operationBarController.startButton.disableProperty()
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initDirTab() {

    }

    private void initConditionTab() {

        subdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_toggle, Boolean new_toggle) {
                AppVaribles.setUserConfigValue(FileArrangeSubdirKey, isCopy);
            }
        });
        subdirCheck.setSelected(AppVaribles.getUserConfigBoolean(FileArrangeSubdirKey, true));

        filesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton selected = (RadioButton) filesGroup.getSelectedToggle();
                isCopy = getMessage("Copy").equals(selected.getText());
                AppVaribles.setUserConfigValue(FileArrangeCopyKey, isCopy);
            }
        });
        if (AppVaribles.getUserConfigBoolean(FileArrangeCopyKey, true)) {
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
        String replaceSelect = AppVaribles.getUserConfigValue(FileArrangeExistedKey, "ReplaceModified");
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
                byModifyTime = getMessage("ModifyTime").equals(selected.getText());
                AppVaribles.setUserConfigValue(FileArrangeModifyTimeKey, byModifyTime);
            }
        });
        if (AppVaribles.getUserConfigBoolean(FileArrangeModifyTimeKey, true)) {
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
        String dirSelect = AppVaribles.getUserConfigValue(FileArrangeCategoryKey, "Month");
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
        if (getMessage("ReplaceModified").equals(selected.getText())) {
            replaceType = ReplaceType.ReplaceModified;
            AppVaribles.setUserConfigValue(FileArrangeExistedKey, "ReplaceModified");
        } else if (getMessage("NotCopy").equals(selected.getText())) {
            replaceType = ReplaceType.NotCopy;
            AppVaribles.setUserConfigValue(FileArrangeExistedKey, "NotCopy");
        } else if (getMessage("Replace").equals(selected.getText())) {
            replaceType = ReplaceType.Replace;
            AppVaribles.setUserConfigValue(FileArrangeExistedKey, "Replace");
        } else if (getMessage("Rename").equals(selected.getText())) {
            replaceType = ReplaceType.Rename;
            AppVaribles.setUserConfigValue(FileArrangeExistedKey, "Rename");
        } else {
            replaceType = ReplaceType.ReplaceModified;
            AppVaribles.setUserConfigValue(FileArrangeExistedKey, "ReplaceModified");
        }

    }

    private void checkDirType() {
        RadioButton selected = (RadioButton) dirGroup.getSelectedToggle();
        if (getMessage("Year").equals(selected.getText())) {
            dirType = DirType.Year;
            AppVaribles.setUserConfigValue(FileArrangeCategoryKey, "Year");
        } else if (getMessage("Month").equals(selected.getText())) {
            dirType = DirType.Month;
            AppVaribles.setUserConfigValue(FileArrangeCategoryKey, "Month");
        } else if (getMessage("Day").equals(selected.getText())) {
            dirType = DirType.Day;
            AppVaribles.setUserConfigValue(FileArrangeCategoryKey, "Day");
        } else {
            dirType = DirType.Month;
            AppVaribles.setUserConfigValue(FileArrangeCategoryKey, "Month");
        }
    }

    protected boolean initAttributes() {
        try {
            sourcePath = new File(sourcePathInput.getText());
            if (!paused || lastFileName == null) {
                copyAttr = new FileSynchronizeAttributes();

                logsTextArea.setText(AppVaribles.getMessage("SourcePath") + ": " + sourcePathInput.getText() + "\n");
                logsTextArea.appendText(AppVaribles.getMessage("TargetPath") + ": " + targetPathInput.getText() + "\n");
                newLogs = new StringBuffer();
                newlines = 0;
                totalLines = 0;

                try {
                    maxLines = Integer.parseInt(maxLinesinput.getText());
                } catch (Exception e) {
                    maxLines = 5000;
                }

                strFailedCopy = AppVaribles.getMessage("FailedCopy") + ": ";
                strCreatedSuccessfully = AppVaribles.getMessage("CreatedSuccessfully") + ": ";
                strCopySuccessfully = AppVaribles.getMessage("CopySuccessfully") + ": ";
                strDeleteSuccessfully = AppVaribles.getMessage("DeletedSuccessfully") + ": ";
                strFailedDelete = AppVaribles.getMessage("FailedDelete") + ": ";

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
                updateLogs(getMessage("LastHanldedFile") + " " + lastFileName, true);
            }

            paused = false;
            startTime = new Date();

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
            task = new Task<Void>() {

                @Override
                protected Void call() {
                    arranegFiles(sourcePath);
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
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
                        updateLogs(AppVaribles.getMessage("Paused"), true);
                    } else {
                        updateLogs(AppVaribles.getMessage(newStatus), true);
                    }
                    switch (newStatus) {
                        case "Started":
                            operationBarController.statusLabel.setText(getMessage("Handling...") + " "
                                    + getMessage("StartTime")
                                    + ": " + DateTools.datetimeToString(startTime));
                            operationBarController.startButton.setText(AppVaribles.getMessage("Cancel"));
                            operationBarController.startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelProcess(event);
                                }
                            });
                            operationBarController.pauseButton.setVisible(true);
                            operationBarController.pauseButton.setDisable(false);
                            operationBarController.pauseButton.setText(AppVaribles.getMessage("Pause"));
                            operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pauseProcess(event);
                                }
                            });
                            operationBarController.progressBar.setProgress(-1);
                            dirsBox.setDisable(true);
                            conditionsBox.setDisable(true);
                            break;

                        case "Done":
                        default:
                            if (paused) {
                                operationBarController.startButton.setText(AppVaribles.getMessage("Cancel"));
                                operationBarController.startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        cancelProcess(event);
                                    }
                                });
                                operationBarController.pauseButton.setVisible(true);
                                operationBarController.pauseButton.setDisable(false);
                                operationBarController.pauseButton.setText(AppVaribles.getMessage("Continue"));
                                operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startAction();
                                    }
                                });

                            } else {
                                operationBarController.startButton.setText(AppVaribles.getMessage("Start"));
                                operationBarController.startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startAction();
                                    }
                                });
                                operationBarController.pauseButton.setVisible(false);
                                operationBarController.pauseButton.setDisable(true);
                                operationBarController.progressBar.setProgress(1);
                                dirsBox.setDisable(false);
                                conditionsBox.setDisable(false);
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
    public void showCost() {
        if (operationBarController.statusLabel == null) {
            return;
        }
        long cost = (new Date().getTime() - startTime.getTime()) / 1000;
        double avg = 0;
        if (copyAttr.getCopiedFilesNumber() != 0) {
            avg = ValueTools.roundDouble3((double) cost / copyAttr.getCopiedFilesNumber());
        }
        String s;
        if (paused) {
            s = getMessage("Paused");
        } else {
            s = getMessage(currentStatus);
        }
        s += ". " + getMessage("HandledThisTime") + ": " + copyAttr.getCopiedFilesNumber() + " "
                + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

    @Override
    public void donePost() {
        showCost();
        updateLogs(getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime) + "   "
                + AppVaribles.getMessage("Cost") + ": " + DateTools.showTime(new Date().getTime() - startTime.getTime()), false, true);
        updateLogs(AppVaribles.getMessage("TotalCheckedFiles") + ": " + copyAttr.getTotalFilesNumber() + "   "
                + AppVaribles.getMessage("TotalCheckedDirectories") + ": " + copyAttr.getTotalDirectoriesNumber() + "   "
                + AppVaribles.getMessage("TotalCheckedSize") + ": " + FileTools.showFileSize(copyAttr.getTotalSize()), false, true);
        updateLogs(AppVaribles.getMessage("TotalCopiedFiles") + ": " + copyAttr.getCopiedFilesNumber() + "   "
                + AppVaribles.getMessage("TotalCopiedDirectories") + ": " + copyAttr.getCopiedDirectoriesNumber() + "   "
                + AppVaribles.getMessage("TotalCopiedSize") + ": " + FileTools.showFileSize(copyAttr.getCopiedSize()), false, true);
        if (!isCopy) {
            updateLogs(AppVaribles.getMessage("TotalDeletedFiles") + ": " + copyAttr.getDeletedFiles() + "   "
                    + AppVaribles.getMessage("TotalDeletedDirectories") + ": " + copyAttr.getDeletedDirectories() + "   "
                    + AppVaribles.getMessage("TotalDeletedSize") + ": " + FileTools.showFileSize(copyAttr.getDeletedSize()), false, true);
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
            Desktop.getDesktop().browse(new File(targetPathInput.getText()).toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected boolean arranegFiles(File sourcePath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
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
                        updateLogs(getMessage("ReachFile") + " " + lastFileName, true);
                    }
                    if (srcFile.isFile()) {
                        continue;
                    }
                } else {
                    if (srcFile.isFile()) {
                        copyAttr.setTotalFilesNumber(copyAttr.getTotalFilesNumber() + 1);
                    } else {
                        copyAttr.setTotalDirectoriesNumber(copyAttr.getTotalDirectoriesNumber() + 1);
                    }
                    copyAttr.setTotalSize(copyAttr.getTotalSize() + srcFile.length());
                }
                if (srcFile.isDirectory()) {
                    if (subdirCheck.isSelected()) {
                        if (verboseCheck.isSelected()) {
                            updateLogs(getMessage("HandlingDirectory") + " " + srcFileName, true);
                        }
                        arranegFiles(srcFile);
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
                                if (verboseCheck.isSelected()) {
                                    updateLogs(strCreatedSuccessfully + path.getAbsolutePath());
                                }
                            }
                            break;
                        case DirType.Day:
                            path = new File(targetPath + File.separator + c.get(Calendar.YEAR)
                                    + File.separator + c.get(Calendar.YEAR) + "-" + month
                                    + File.separator + c.get(Calendar.YEAR) + "-" + month + "-" + day);
                            if (!path.exists()) {
                                path.mkdirs();
                                if (verboseCheck.isSelected()) {
                                    updateLogs(strCreatedSuccessfully + path.getAbsolutePath());
                                }
                            }
                            break;
                        case DirType.Month:
                        default:
                            path = new File(targetPath + File.separator + c.get(Calendar.YEAR)
                                    + File.separator + c.get(Calendar.YEAR) + "-" + month);
                            if (!path.exists()) {
                                path.mkdirs();
                                if (verboseCheck.isSelected()) {
                                    updateLogs(strCreatedSuccessfully + path.getAbsolutePath());
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
                        if (verboseCheck.isSelected()) {
                            updateLogs(strDeleteSuccessfully + srcFileName);
                        }
                    }
                    copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                    copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                    if (verboseCheck.isSelected()) {
                        updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                + srcFileName + " -> " + newFile.getAbsolutePath());
                    }
                    lastFileName = srcFileName;

                } catch (Exception e) {
                    updateLogs(strFailedCopy + srcFileName + "\n" + e.toString());
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

    protected void updateLogs(final String line) {
        updateLogs(line, true, false);
    }

    protected void updateLogs(final String line, boolean immediate) {
        updateLogs(line, true, immediate);
    }

    protected void updateLogs(final String line, boolean showTime, boolean immediate) {
        try {
            if (showTime) {
                newLogs.append(DateTools.datetimeToString(new Date())).append("  ");
            }
            newLogs.append(line).append("\n");
            long past = new Date().getTime() - startTime.getTime();
            if (immediate || newlines++ > cacheLines || past > 5000) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        logsTextArea.appendText(newLogs.toString());
                        totalLines += newlines;
                        if (totalLines > maxLines + cacheLines) {
                            logsTextArea.deleteText(0, newLogs.length());
                        }
                        newLogs = new StringBuffer();
                        newlines = 0;
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    @FXML
    protected void clearLogs(ActionEvent event) {
        logsTextArea.setText("");
    }

}
