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

    private boolean isConditional;
    protected String currentStatus, lastFileName;
    protected Date startTime;
    protected FileSynchronizeAttributes copyAttr;
    protected StringBuffer newLogs;
    protected int newlines, maxLines, totalLines, cacheLines = 200;
    protected String strFailedCopy, strCreatedSuccessfully, strCopySuccessfully, strDeleteSuccessfully, strFailedDelete;
    protected File sourcePath;

    @FXML
    private VBox dirsBox, conditionsBox, condBox, logsBox;
    @FXML
    private TextField sourcePathInput, maxLinesinput, notCopyInput;
    @FXML
    private ToggleGroup copyGroup;
    @FXML
    private CheckBox copySubdirCheck, copyEmptyCheck, copyNewCheck, copyHiddenCheck, copyReadonlyCheck;
    @FXML
    private CheckBox copyExistedCheck, copyModifiedCheck, deleteNonExistedCheck, notCopyCheck, copyAttrCheck, continueCheck;
    @FXML
    private DatePicker modifyAfterInput;
    @FXML
    private TextArea logsTextArea;
    @FXML
    private Button clearButton;

    public DirectorySynchronizeController() {
        targetPathKey = "FileTargetPath";
        sourcePathKey = "FileSourcePath";

        fileExtensionFilter = new ArrayList();
        fileExtensionFilter.add(new FileChooser.ExtensionFilter("*", "*.*"));
    }

    @Override
    protected void initializeNext() {
        try {

            FxmlTools.setPathExistedValidation(sourcePathInput);
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
            File path = new File(AppVaribles.getConfigValue(sourcePathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", directory.getPath());
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
            File path = new File(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", directory.getPath());
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

    private boolean initAttributes() {
        try {
            sourcePath = new File(sourcePathInput.getText());

            if (!"Paused".equals(currentStatus)) {
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
                lastFileName = null;
            }

            if (!copyAttr.isCopyNew() && !copyAttr.isCopyExisted() && !copyAttr.isCopySubdir()) {
                popInformation(getMessage("NothingCopy"));
                return false;
            }
            // In case that the source path itself is in blacklist
            if (copyAttr.isNotCopySome()) {
                List<String> keys = copyAttr.getNotCopyNames();
                String srcName = sourcePath.getName();
                for (String key : keys) {
                    if (srcName.contains(key)) {
                        popInformation(getMessage("NothingCopy"));
                        return false;
                    }
                }
            }

            strFailedCopy = AppVaribles.getMessage("FailedCopy") + ": ";
            strCreatedSuccessfully = AppVaribles.getMessage("CreatedSuccessfully") + ": ";
            strCopySuccessfully = AppVaribles.getMessage("CopySuccessfully") + ": ";
            strDeleteSuccessfully = AppVaribles.getMessage("DeletedSuccessfully") + ": ";
            strFailedDelete = AppVaribles.getMessage("FailedDelete") + ": ";

            targetPath = new File(targetPathInput.getText());
            if (!targetPath.exists()) {
                targetPath.mkdir();
                logsTextArea.appendText(strCreatedSuccessfully + targetPath.getAbsolutePath() + "\n");
            }
            targetPath.setWritable(true);
            targetPath.setExecutable(true);

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

            startTime = new Date();
            updateInterface("Started");
            task = new Task<Void>() {

                @Override
                protected Void call() {
                    if (copyAttr.isConditionalCopy()) {
                        conditionalCopy(sourcePath, targetPath);
                    } else {
                        updateLogs(AppVaribles.getMessage("ClearingTarget"));
                        if (!clearDir(targetPath, false)) {
                            updateLogs(AppVaribles.getMessage("FailClearTarget"));
                        } else {
                            updateLogs(AppVaribles.getMessage("TargetCleared"));
                            copyWholeDirectory(sourcePath, targetPath);
                        }
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

    @FXML
    @Override
    protected void pauseProcess(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Paused");
    }

    protected void cancelProcess(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Canceled");
    }

    protected void updateInterface(final String newStatus) {
        currentStatus = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    updateLogs(AppVaribles.getMessage(newStatus));
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

                        case "Paused":
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
                            showCost();
                            dirsBox.setDisable(false);
                            conditionsBox.setDisable(false);
                            condBox.setDisable(false);
                            break;

                        case "Done":
                        default:
                            operationBarController.startButton.setText(AppVaribles.getMessage("Start"));
                            operationBarController.startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startProcess(event);
                                }
                            });
                            operationBarController.pauseButton.setVisible(false);
                            operationBarController.pauseButton.setDisable(true);
                            showCost();
                            operationBarController.progressBar.setProgress(1);
                            dirsBox.setDisable(false);
                            conditionsBox.setDisable(false);
                            logsTextArea.appendText(newLogs.toString());
                            logsTextArea.appendText(AppVaribles.getMessage("TotalCheckedFiles") + ": " + copyAttr.getTotalFilesNumber() + "   ");
                            logsTextArea.appendText(AppVaribles.getMessage("TotalCheckedDirectories") + ": " + copyAttr.getTotalDirectoriesNumber() + "   ");
                            logsTextArea.appendText(AppVaribles.getMessage("TotalCheckedSize") + ": " + FileTools.showFileSize2(copyAttr.getTotalSize()) + "\n");
                            logsTextArea.appendText(AppVaribles.getMessage("TotalCopiedFiles") + ": " + copyAttr.getCopiedFilesNumber() + "   ");
                            logsTextArea.appendText(AppVaribles.getMessage("TotalCopiedDirectories") + ": " + copyAttr.getCopiedDirectoriesNumber() + "   ");
                            logsTextArea.appendText(AppVaribles.getMessage("TotalCopiedSize") + ": " + FileTools.showFileSize2(copyAttr.getCopiedSize()) + "\n");
                            if (copyAttr.isConditionalCopy() && copyAttr.isDeleteNotExisteds()) {
                                logsTextArea.appendText(AppVaribles.getMessage("TotalDeletedFiles") + ": " + copyAttr.getDeletedFiles() + "   ");
                                logsTextArea.appendText(AppVaribles.getMessage("TotalDeletedDirectories") + ": " + copyAttr.getDeletedDirectories() + "   ");
                                logsTextArea.appendText(AppVaribles.getMessage("TotalDeletedSize") + ": " + FileTools.showFileSize2(copyAttr.getDeletedSize()) + "\n");
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
        String s = getMessage(currentStatus) + ". "
                + getMessage("HandledThisTime") + ": " + copyAttr.getCopiedFilesNumber() + " "
                + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

    private void updateLogs(final String line) {
        newLogs.append(DateTools.datetimeToString(new Date())).append("  ").append(line).append("\n");
        long past = new Date().getTime() - startTime.getTime();
        if (newlines++ > cacheLines || past > 5000) {
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
    }

    private void checkIsConditional() {
        RadioButton sort = (RadioButton) copyGroup.getSelectedToggle();
        if (!getMessage("CopyConditionally").equals(sort.getText())) {
            condBox.setDisable(true);
            isConditional = false;
        } else {
            condBox.setDisable(false);
            isConditional = true;
        }
    }

    private boolean conditionalCopy(File sourcePath, File targetPath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (copyAttr.isDeleteNotExisteds()
                    && !deleteNonExisted(sourcePath, targetPath) && !copyAttr.isContinueWhenError()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
            for (File srcFile : files) {
                if (task.isCancelled()) {
                    return false;
                }
                copyAttr.setTotalSize(copyAttr.getTotalSize() + srcFile.length());
                if (srcFile.isFile()) {
                    copyAttr.setTotalFilesNumber(copyAttr.getTotalFilesNumber() + 1);
                } else {
                    copyAttr.setTotalDirectoriesNumber(copyAttr.getTotalDirectoriesNumber() + 1);
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
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + srcFile.length());
                        updateLogs(strCopySuccessfully + srcFile.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                    } else if (!copyAttr.isContinueWhenError()) {
                        updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                        return false;
                    }
                } else if (copyAttr.isCopySubdir()) {
                    if (srcFile.listFiles() == null && !copyAttr.isCopyEmpty()) {
                        continue;
                    }
                    if (!targetFile.exists()) {
                        targetFile.mkdir();
                        updateLogs(strCreatedSuccessfully + targetFile.getAbsolutePath());
                    }
                    if (conditionalCopy(srcFile, targetFile)) {
                        copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                        copyAttr.setCopiedSize(copyAttr.getCopiedSize() + srcFile.length());
                    } else if (!copyAttr.isContinueWhenError()) {
                        updateLogs(strFailedCopy + srcFile.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    private boolean copyWholeDirectory(File sourcePath, File targetPath) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            File[] files = sourcePath.listFiles();
            for (File file : files) {
                if (task.isCancelled()) {
                    return false;
                }
                copyAttr.setTotalSize(copyAttr.getTotalSize() + file.length());
                File targetFile = new File(targetPath + File.separator + file.getName());
                if (file.isFile()) {
                    copyAttr.setTotalFilesNumber(copyAttr.getTotalFilesNumber() + 1);
                    if (copyFile(file, targetFile)) {
                        copyAttr.setCopiedFilesNumber(copyAttr.getCopiedFilesNumber() + 1);
                    } else if (!copyAttr.isContinueWhenError()) {
                        updateLogs(strFailedCopy + file.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                        return false;
                    }
                } else {
                    copyAttr.setTotalDirectoriesNumber(copyAttr.getTotalDirectoriesNumber() + 1);
                    targetFile.mkdir();
                    updateLogs(strCreatedSuccessfully + targetFile.getAbsolutePath());
                    if (copyWholeDirectory(file, targetFile)) {
                        copyAttr.setCopiedDirectoriesNumber(copyAttr.getCopiedDirectoriesNumber() + 1);
                    } else if (!copyAttr.isContinueWhenError()) {
                        updateLogs(strFailedCopy + file.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                        return false;
                    }
                }
                updateLogs(strCopySuccessfully + file.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                copyAttr.setCopiedSize(copyAttr.getCopiedSize() + file.length());
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    private boolean clearDir(File dir, boolean record) {
        if (task.isCancelled() || dir.isFile()) {
            return false;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (task.isCancelled()) {
                return false;
            }
            long len = file.length();
            String filename = file.getAbsolutePath();
            if (file.isDirectory()) {
                if (clearDir(file, record)) {
                    try {
                        file.delete();
                        if (record) {
                            copyAttr.setDeletedDirectories(copyAttr.getDeletedDirectories() + 1);
                            copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                            updateLogs(strDeleteSuccessfully + filename);
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
                        updateLogs(strFailedDelete + filename);
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
                    updateLogs(strDeleteSuccessfully + filename);
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

    private boolean copyFile(File sourceFile, File targetFile) {
        try {
            if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
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

    private boolean deleteNonExisted(File sourcePath, File targetPath) {
        if (!copyAttr.isDeleteNotExisteds() || !targetPath.isDirectory()) {
            return true;
        }
        File[] files = targetPath.listFiles();
        if (files == null) {
            return true;
        }
        for (File targetFile : files) {
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
                        updateLogs(strDeleteSuccessfully + filename);
                    } catch (Exception e) {
                        copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                        copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                        updateLogs(strFailedDelete + filename);
                        if (!copyAttr.isContinueWhenError()) {
                            return false;
                        }
                    }
                } else {
                    copyAttr.setFailedDeletedDirectories(copyAttr.getFailedDeletedDirectories() + 1);
                    copyAttr.setFailedDeletedSize(copyAttr.getFailedDeletedSize() + len);
                    updateLogs(strFailedDelete + filename);
                    if (!copyAttr.isContinueWhenError()) {
                        return false;
                    }
                }
            } else {
                try {
                    targetFile.delete();
                    copyAttr.setDeletedFiles(copyAttr.getDeletedFiles() + 1);
                    copyAttr.setDeletedSize(copyAttr.getDeletedSize() + len);
                    updateLogs(strDeleteSuccessfully + filename);
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
