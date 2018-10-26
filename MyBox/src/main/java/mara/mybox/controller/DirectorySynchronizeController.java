package mara.mybox.controller;

import java.awt.Desktop;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileSynchronizeAttributes;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-8
 * @Description
 * @License Apache License Version 2.0
 */
public class DirectorySynchronizeController extends BaseController {

    protected boolean isConditional, startHandle;
    protected String lastFileName;
    protected Date startTime;
    protected FileSynchronizeAttributes copyAttr;
    protected StringBuffer newLogs;
    protected int newlines, maxLines, totalLines, cacheLines = 200;
    protected String strFailedCopy, strCreatedSuccessfully, strCopySuccessfully, strFailedDelete;
    protected String strDeleteSuccessfully, strFileDeleteSuccessfully, strDirectoryDeleteSuccessfully;
    protected File sourcePath;

    @FXML
    protected VBox dirsBox, conditionsBox, condBox, logsBox;
    @FXML
    protected TextField sourcePathInput, maxLinesinput, notCopyInput;
    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected CheckBox copySubdirCheck, copyEmptyCheck, copyNewCheck, copyHiddenCheck, copyReadonlyCheck, verboseCheck;
    @FXML
    protected CheckBox copyExistedCheck, copyModifiedCheck, deleteNonExistedCheck, notCopyCheck, copyAttrCheck, continueCheck;
    @FXML
    protected DatePicker modifyAfterInput;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected Button clearButton;

    public DirectorySynchronizeController() {
        targetPathKey = "DirectorySynchronizeTargetPath";
        sourcePathKey = "DirectorySynchronizeSourcePath";

        fileExtensionFilter = new ArrayList();
        fileExtensionFilter.add(new FileChooser.ExtensionFilter("*", "*.*"));
    }

    @Override
    protected void initializeNext() {
        try {

            sourcePathInput.setText(AppVaribles.getConfigValue(sourcePathKey, CommonValues.UserFilePath));
            sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    final File file = new File(newValue);
                    if (!file.exists() || !file.isDirectory()) {
                        sourcePathInput.setStyle(badStyle);
                        return;
                    }
                    sourcePathInput.setStyle(null);
                    AppVaribles.setConfigValue(LastPathKey, newValue);
                    AppVaribles.setConfigValue(sourcePathKey, newValue);
                }
            });

            targetPathInput.setText(AppVaribles.getConfigValue(targetPathKey, CommonValues.UserFilePath));
            targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    final File file = new File(newValue);
                    if (!file.isDirectory()) {
                        targetPathInput.setStyle(badStyle);
                        return;
                    }
                    targetPathInput.setStyle(null);
                    AppVaribles.setConfigValue(LastPathKey, newValue);
                    AppVaribles.setConfigValue(targetPathKey, newValue);
                }
            });

            FxmlTools.setNonnegativeValidation(maxLinesinput);

            checkIsConditional();
            copyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkIsConditional();
                }
            });

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

    @FXML
    protected void selectSourcePath(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setConfigValue(sourcePathKey, directory.getPath());

            sourcePathInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void selectTargetPath(ActionEvent event) {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setConfigValue(targetPathKey, directory.getPath());

            targetPathInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void openTarget(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new File(targetPathInput.getText()).toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void clearLogs(ActionEvent event) {
        logsTextArea.setText("");
    }

    protected boolean initAttributes() {
        try {
            sourcePath = new File(sourcePathInput.getText());
            if (!paused || lastFileName == null) {
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
                List<String> notCopy = new ArrayList();
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
                    alertInformation(getMessage("NothingCopy"));
                    return false;
                }
                // In case that the source path itself is in blacklist
                if (copyAttr.isNotCopySome()) {
                    List<String> keys = copyAttr.getNotCopyNames();
                    String srcName = sourcePath.getName();
                    for (String key : keys) {
                        if (srcName.contains(key)) {
                            alertInformation(getMessage("NothingCopy"));
                            return false;
                        }
                    }
                }

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
                strFileDeleteSuccessfully = AppVaribles.getMessage("FileDeletedSuccessfully") + ": ";
                strDirectoryDeleteSuccessfully = AppVaribles.getMessage("DirectoryDeletedSuccessfully") + ": ";

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

            startTime = new Date();

            return true;

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @FXML
    @Override
    protected void startProcess(ActionEvent event) {
        try {
            if (!initAttributes()) {
                return;
            }

            updateInterface("Started");
            task = new Task<Void>() {

                @Override
                protected Void call() {
                    if (copyAttr.isConditionalCopy()) {
                        paused = false;
                        conditionalCopy(sourcePath, targetPath);
                    } else {
                        if (!paused && targetPath.exists()) {
                            updateLogs(AppVaribles.getMessage("ClearingTarget"), true);
                            if (clearDir(targetPath, false)) {
                                updateLogs(AppVaribles.getMessage("TargetCleared"), true);
                            } else if (!copyAttr.isContinueWhenError()) {
                                updateLogs(AppVaribles.getMessage("FailClearTarget"), true);
                                return null;
                            }
                        }
                        paused = false;
                        copyWholeDirectory(sourcePath, targetPath);
                    }
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
    protected void updateInterface(final String newStatus) {
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
                                        startProcess(event);
                                    }
                                });
                            } else {
                                operationBarController.startButton.setText(AppVaribles.getMessage("Start"));
                                operationBarController.startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startProcess(event);
                                    }
                                });
                                operationBarController.pauseButton.setVisible(false);
                                operationBarController.pauseButton.setDisable(true);
                                operationBarController.progressBar.setProgress(1);
                                dirsBox.setDisable(false);
                                conditionsBox.setDisable(false);
                            }
                            showCost();
                            updateLogs(getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime) + "   "
                                    + AppVaribles.getMessage("Cost") + ": " + DateTools.showTime(new Date().getTime() - startTime.getTime()), false, true);
                            updateLogs(AppVaribles.getMessage("TotalCheckedFiles") + ": " + copyAttr.getTotalFilesNumber() + "   "
                                    + AppVaribles.getMessage("TotalCheckedDirectories") + ": " + copyAttr.getTotalDirectoriesNumber() + "   "
                                    + AppVaribles.getMessage("TotalCheckedSize") + ": " + FileTools.showFileSize(copyAttr.getTotalSize()), false, true);
                            updateLogs(AppVaribles.getMessage("TotalCopiedFiles") + ": " + copyAttr.getCopiedFilesNumber() + "   "
                                    + AppVaribles.getMessage("TotalCopiedDirectories") + ": " + copyAttr.getCopiedDirectoriesNumber() + "   "
                                    + AppVaribles.getMessage("TotalCopiedSize") + ": " + FileTools.showFileSize(copyAttr.getCopiedSize()), false, true);
                            if (copyAttr.isConditionalCopy() && copyAttr.isDeleteNotExisteds()) {
                                updateLogs(AppVaribles.getMessage("TotalDeletedFiles") + ": " + copyAttr.getDeletedFiles() + "   "
                                        + AppVaribles.getMessage("TotalDeletedDirectories") + ": " + copyAttr.getDeletedDirectories() + "   "
                                        + AppVaribles.getMessage("TotalDeletedSize") + ": " + FileTools.showFileSize(copyAttr.getDeletedSize()), false, true);
                            }

                    }

                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        });

    }

    protected void showCost() {
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

    protected void checkIsConditional() {
        RadioButton sort = (RadioButton) copyGroup.getSelectedToggle();
        if (!getMessage("CopyConditionally").equals(sort.getText())) {
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
            if (copyAttr.isDeleteNotExisteds()
                    && !deleteNonExisted(sourcePath, targetPath) && !copyAttr.isContinueWhenError()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
            String srcFileName;
            long len;
            for (File srcFile : files) {
                if (task.isCancelled()) {
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
                File targetFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyAttr.isOnlyCopyModified()) {
                        if (srcFile.lastModified() <= copyAttr.getModifyAfter()) {
                            continue;
                        }
                    }
                    if (targetFile.exists()) {
                        if (!copyAttr.isCopyExisted()) {
                            continue;
                        }
                        if (copyAttr.isOnlyCopyModified()) {
                            if (srcFile.lastModified() <= targetFile.lastModified()) {
                                continue;
                            }
                        }
                    } else if (!copyAttr.isCopyNew()) {
                        continue;
                    }
                    if (copyFile(srcFile, targetFile)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + targetFile.getAbsolutePath());
                        }
                        lastFileName = srcFileName;
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + targetFile.getAbsolutePath());
                        }
                        return false;
                    }
                } else if (copyAttr.isCopySubdir()) {
                    if (verboseCheck.isSelected()) {
                        updateLogs(getMessage("HandlingDirectory") + " " + srcFileName, true);
                    }
                    if (srcFile.listFiles() == null && !copyAttr.isCopyEmpty()) {
                        continue;
                    }
                    if (startHandle && !targetFile.exists()) {
                        targetFile.mkdirs();
                        if (verboseCheck.isSelected()) {
                            updateLogs(strCreatedSuccessfully + targetFile.getAbsolutePath());
                        }
                    }
                    if (conditionalCopy(srcFile, targetFile)) {
                        if (startHandle) {
                            copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                            copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                            if (verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getCopiedDirectoriesNumber() + "  " + strCopySuccessfully
                                        + srcFileName + " -> " + targetFile.getAbsolutePath());
                            }
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            updateLogs(strFailedCopy + sourcePath.getAbsolutePath() + "\n" + e.toString());
            return false;
        }
    }

    protected boolean copyWholeDirectory(File sourcePath, File targetPath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
            String srcFileName;
            long len;
            for (File srcFile : files) {
                if (task.isCancelled()) {
                    return false;
                }
                srcFileName = srcFile.getAbsolutePath();
                len = srcFile.length();
                if (!startHandle) {
                    if (lastFileName.equals(srcFileName)) {
                        startHandle = true;
                        if (verboseCheck.isSelected()) {
                            updateLogs(getMessage("ReachFile") + " " + lastFileName, true);
                        }
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
                File targetFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    if (copyFile(srcFile, targetFile)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                        if (verboseCheck.isSelected()) {
                            updateLogs(copyAttr.getCopiedFilesNumber() + "  " + strCopySuccessfully
                                    + srcFileName + " -> " + targetFile.getAbsolutePath());
                        }
                        lastFileName = srcFileName;
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFileName + " -> " + targetFile.getAbsolutePath());
                        }
                        return false;
                    }
                } else {
                    if (verboseCheck.isSelected()) {
                        updateLogs(getMessage("HandlingDirectory") + " " + srcFileName, true);
                    }
                    if (startHandle) {
                        targetFile.mkdirs();
                        if (verboseCheck.isSelected()) {
                            updateLogs(strCreatedSuccessfully + targetFile.getAbsolutePath());
                        }
                    }
                    if (copyWholeDirectory(srcFile, targetFile)) {
                        if (startHandle) {
                            copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                            copyAttr.setCopiedSize(copyAttr.getCopiedSize() + len);
                            if (verboseCheck.isSelected()) {
                                updateLogs(copyAttr.getCopiedDirectoriesNumber() + "  " + strCopySuccessfully
                                        + srcFileName + " -> " + targetFile.getAbsolutePath());
                            }
                        }
                    } else if (!copyAttr.isContinueWhenError()) {
                        if (verboseCheck.isSelected()) {
                            updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
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
                        file.delete();
                        if (record) {
                            copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                            copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                            if (verboseCheck.isSelected()) {
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
                        if (verboseCheck.isSelected()) {
                            updateLogs(strFailedDelete + filename);
                        }
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
            try {
                file.delete();
                if (record) {
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck.isSelected()) {
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
            if (task.isCancelled() || sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
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
            logger.error(e.toString());
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
        for (File targetFile : files) {
            if (task.isCancelled()) {
                return false;
            }
            File srcFile = new File(sourcePath + File.separator + targetFile.getName());
            if (srcFile.exists()) {
                continue;
            }
            long len = targetFile.length();
            String filename = targetFile.getAbsolutePath();
            if (targetFile.isDirectory()) {
                if (clearDir(targetFile, true)) {
                    try {
                        targetFile.delete();
                        copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                        copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                        if (verboseCheck.isSelected()) {
                            updateLogs(strDirectoryDeleteSuccessfully + filename);
                        }
                    } catch (Exception e) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        if (verboseCheck.isSelected()) {
                            updateLogs(strFailedDelete + filename);
                        }
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    if (verboseCheck.isSelected()) {
                        updateLogs(strFailedDelete + filename);
                    }
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            } else {
                try {
                    targetFile.delete();
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    if (verboseCheck.isSelected()) {
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
