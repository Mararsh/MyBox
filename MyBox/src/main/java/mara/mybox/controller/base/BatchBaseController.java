package mara.mybox.controller.base;

import mara.mybox.controller.FilesTableController;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.FileInformation;
import mara.mybox.data.ProcessParameters;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.ValueTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BatchBaseController extends FilesTableController {

    public List<File> sourceFiles, targetFiles;
    public TargetExistType targetExistType;
    public int dirTotal, dirOk;
    public List<Integer> sourcesIndice;
    public String creatSubdirKey, previewKey, fillZeroKey;
    public String appendColorKey, appendCompressionTypeKey;
    public String appendDensityKey, appendQualityKey, appendSizeKey;
    public boolean allowPaused, browseTargets;
    public boolean isPreview, paused;

    public ProcessParameters actualParameters, previewParameters, currentParameters;

    public static enum TargetExistType {
        Rename, Replace, Skip
    }

    @FXML
    public FilesTableController filesTableController;
    @FXML
    public ToggleGroup targetExistGroup, fileTypeGroup;
    @FXML
    public RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    public TextField targetSuffixInput;
    @FXML
    public CheckBox fillZero, subdirCheck, filesNameCheck;
    @FXML
    public TextField filesNameInput;
    @FXML
    public TextField previewInput, acumFromInput, digitInput;
    @FXML
    public CheckBox appendDensity, appendColor, appendCompressionType, appendQuality, appendSize;

    public BatchBaseController() {
        creatSubdirKey = "creatSubdir";
        fillZeroKey = "fillZero";
        previewKey = "preview";
        appendColorKey = "appendColor";
        appendCompressionTypeKey = "appendCompressionType";
        appendDensityKey = "appendDensity";
        appendQualityKey = "appendQuality";
        appendSizeKey = "appendSize";

        browseTargets = false;

        sourcePathKey = "sourcePath";
        fileExtensionFilter = CommonValues.AllExtensionFilter;
    }

    /**
     * Methods to be implemented
     */
    // "operationBarController.startButton.disableProperty()" should be defined by subClass
    public void initializeNext2() {
        try {

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void initOptionsSection() {

    }

    // SubClass should use either "makeSingleParameters()" or "makeBatchParameters()"
    public void makeMoreParameters() {

    }

    // "targetFiles" and "actualParameters.finalTargetName" should be written by subClass
    public void handleCurrentFile() {
        FileInformation d = sourceFilesInformation.get(sourcesIndice.get(currentParameters.currentIndex));
        if (d == null) {
            return;
        }
        File file = d.getFile();
        currentParameters.sourceFile = file;
        String result;
        if (!file.exists()) {
            result = AppVaribles.getMessage("NotFound");
        } else if (file.isFile()) {
            result = handleCurrentFile(d);
        } else {
            result = handleCurrentDirectory(d);
        }
        d.setHandled(result);
        filesTableView.refresh();
        currentParameters.currentTotalHandled++;
    }

    public String handleCurrentFile(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    public String handleCurrentDirectory(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    public void donePost() {
        if (operationBarController != null) {
            if (operationBarController.miaoCheck.isSelected()) {
                FxmlControl.miao3();
            }
            if (!isPreview && !operationBarController.openCheck.isSelected()) {
                return;
            }
        }
        if (targetFiles == null || targetFiles.size() == 1) {
            if (actualParameters.finalTargetName == null
                    || !new File(actualParameters.finalTargetName).exists()) {
                alertInformation(AppVaribles.getMessage("NoDataNotSupported"));
            } else {
                viewTarget(new File(actualParameters.finalTargetName));
            }
        } else if (targetFiles.size() > 1) {
            if (browseTargets) {
                browseAction();
            } else {
                openTarget(null);
            }
        } else {
            popInformation(AppVaribles.getMessage("NoFileGenerated"));
        }
    }

    public void viewTarget(File file) {
        if (file == null) {
            return;
        }
        FxmlStage.openTarget(getClass(), null, file.getAbsolutePath());
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {
            File path = null;
            if (targetFiles != null && !targetFiles.isEmpty()) {
                path = targetFiles.get(0).getParentFile();
            } else if (actualParameters != null && actualParameters.finalTargetName != null) {
                path = new File(actualParameters.finalTargetName).getParentFile();
            } else if (actualParameters != null && actualParameters.targetPath != null) {
                path = new File(actualParameters.targetPath);
            } else if (targetPathInput != null) {
                String p = targetPathInput.getText();
                if (targetPrefixInput != null && subdirCheck != null && subdirCheck.isSelected()) {
                    if (p.endsWith("/") || p.endsWith("\\")) {
                        p += targetPrefixInput.getText();
                    } else {
                        p += "/" + targetPrefixInput.getText();
                    }
                    if (!new File(p).exists()) {
                        p = targetPathInput.getText();
                    }
                }
                path = new File(p);
            }

            if (path != null && path.exists()) {
                FxmlStage.openTarget(getClass(), null, path.getAbsolutePath());
                recordFileOpened(path);
            } else {
                popInformation(AppVaribles.getMessage("NoFileGenerated"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void browseAction() {
        try {
            if (targetFiles == null || targetFiles.isEmpty()) {
                return;
            }
            final ImagesBrowserController controller = FxmlStage.openImagesBrowser(getClass(), null);
            if (controller != null) {
                controller.loadImages(targetFiles.subList(0, Math.min(9, targetFiles.size())), 3);
            }
        } catch (Exception e) {
        }
    }


    /* ----------------------------------------------------- */
    @Override
    public void initControls() {
        try {
            super.initControls();

            if (subdirCheck != null) {
                subdirCheck.setSelected(AppVaribles.getUserConfigBoolean(creatSubdirKey));
            }

            if (filesTableController != null) {
                filesTableController.fileExtensionFilter = fileExtensionFilter;
                filesTableController.sourcePathKey = sourcePathKey;
                filesTableController.sourcePathKey = sourcePathKey;
                filesTableController.SourceFileType = SourceFileType;
                filesTableController.SourcePathType = SourcePathType;
                filesTableController.TargetPathType = TargetPathType;
                filesTableController.TargetFileType = TargetFileType;
                filesTableController.AddFileType = AddFileType;
                filesTableController.AddPathType = AddPathType;
                filesTableController.targetPathKey = targetPathKey;
                filesTableController.LastPathKey = LastPathKey;
            }

            if (fillZero != null) {
                fillZero.setSelected(AppVaribles.getUserConfigBoolean(fillZeroKey));
            }

            if (acumFromInput != null) {
                FxmlControl.setNonnegativeValidation(acumFromInput);
                acumFromInput.setText("1");
            }

            if (previewInput != null) {
                previewInput.setText(AppVaribles.getUserConfigValue(previewKey, "1"));
                FxmlControl.setPositiveValidation(previewInput);
                previewInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        AppVaribles.setUserConfigValue(previewKey, newValue);
                    }
                });
            }

            if (appendSize != null) {
                appendSize.setSelected(AppVaribles.getUserConfigBoolean(appendSizeKey));
            }
            if (appendColor != null) {
                appendColor.setSelected(AppVaribles.getUserConfigBoolean(appendColorKey));
            }
            if (appendCompressionType != null) {
                appendCompressionType.setSelected(AppVaribles.getUserConfigBoolean(appendCompressionTypeKey));
            }
            if (appendQuality != null) {
                appendQuality.setSelected(AppVaribles.getUserConfigBoolean(appendQualityKey));
            }
            if (appendDensity != null) {
                appendDensity.setSelected(AppVaribles.getUserConfigBoolean(appendDensityKey));
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initializeNext() {
        try {
            initSourceSection();
            initOptionsSection();
            initTargetSection();
            if (filesTableController != null) {
                sourceFilesInformation = filesTableController.sourceFilesInformation;
                filesTableView = filesTableController.filesTableView;
            }

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void initTargetSection() {
        try {

            if (targetExistGroup != null) {
                targetExistGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends Toggle> ov,
                            Toggle old_toggle, Toggle new_toggle) {
                        checkTargetExistType();
                    }
                });
                checkTargetExistType();
            }

            if (previewButton != null) {
                if (operationBarController.startButton != null) {
                    previewButton.disableProperty().bind(
                            operationBarController.startButton.disableProperty()
                                    .or(operationBarController.startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
                    );
                }
                FxmlControl.quickTooltip(previewButton, new Tooltip(getMessage("PreviewComments")));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void checkTargetExistType() {
        targetSuffixInput.setStyle(null);
        RadioButton selected = (RadioButton) targetExistGroup.getSelectedToggle();
        if (selected.equals(targetReplaceRadio)) {
            targetExistType = TargetExistType.Replace;

        } else if (selected.equals(targetRenameRadio)) {
            targetExistType = TargetExistType.Rename;
            if (targetSuffixInput.getText() == null || targetSuffixInput.getText().trim().isEmpty()) {
                targetSuffixInput.setStyle(badStyle);
            }

        } else if (selected.equals(targetSkipRadio)) {
            targetExistType = TargetExistType.Skip;
        }
    }

    @FXML
    @Override
    public void startAction() {
        isPreview = false;
        if (!makeActualParameters()) {
            popError(getMessage("Invalid"));
            actualParameters = null;
            return;
        }
        currentParameters = actualParameters;
        paused = false;
        doCurrentProcess();
    }

    @FXML
    public void preview(ActionEvent event) {
        isPreview = true;
        if (!makeActualParameters()) {
            popError(getMessage("Invalid"));
            actualParameters = null;
            return;
        }
        previewParameters = copyParameters(actualParameters);
        previewParameters.status = "start";
        currentParameters = previewParameters;
        doCurrentProcess();
    }

    public boolean makeActualParameters() {
        if (actualParameters != null && paused) {
            actualParameters.startIndex = actualParameters.currentIndex;
            actualParameters.startPage = actualParameters.currentPage;
            actualParameters.acumStart = actualParameters.currentNameNumber + 1;
            return true;
        }

        actualParameters = new ProcessParameters();
        actualParameters.currentIndex = 0;
        targetPath = null;

        if (targetFileInput != null) {
            actualParameters.finalTargetName = targetFileInput.getText();
            try {
                targetFile = new File(actualParameters.finalTargetName);
                targetPath = new File(targetFileInput.getText()).getParentFile();
            } catch (Exception e) {
            }
        }
        if (targetPathInput != null) {
            targetPath = new File(targetPathInput.getText());
        }
        if (targetPath != null) {
            actualParameters.targetRootPath = targetPath.getAbsolutePath();
            actualParameters.targetPath = actualParameters.targetRootPath;
        }

        if (targetPrefixInput != null) {
            actualParameters.targetPrefix = targetPrefixInput.getText();
        } else {
            actualParameters.targetPrefix = "";
        }
        if (subdirCheck != null) {
            actualParameters.createSubDir = subdirCheck.isSelected();
            AppVaribles.setUserConfigValue(creatSubdirKey, actualParameters.createSubDir);
            if (subdirCheck.isSelected()) {
                File finalPath = new File(actualParameters.targetPath + "/" + actualParameters.targetPrefix + "/");
                if (!finalPath.exists()) {
                    finalPath.mkdirs();
                }
                actualParameters.targetPath += "/" + actualParameters.targetPrefix;
            }
        }

        if (fillZero != null) {
            actualParameters.fill = fillZero.isSelected();
            AppVaribles.setUserConfigValue(fillZeroKey, actualParameters.fill);
        }

        actualParameters.fromPage = 0;
        actualParameters.toPage = 100;
        actualParameters.acumFrom = 1;
        actualParameters.currentNameNumber = 1;
        actualParameters.password = "";
        actualParameters.startPage = 0;
        actualParameters.acumStart = 1;
        actualParameters.acumDigit = 0;

        sourcesIndice = new ArrayList();
        sourceFiles = new ArrayList();
        targetFiles = new ArrayList();

        makeMoreParameters();
        return true;

    }

    public void makeSingleParameters() {
        actualParameters.isBatch = false;

        sourcesIndice = new ArrayList();
        sourceFiles = new ArrayList();

        sourceFiles.add(sourceFile);
        actualParameters.sourceFile = sourceFile;

    }

    public void makeBatchParameters() {
        actualParameters.isBatch = true;

        sourcesIndice = new ArrayList();
        sourceFiles = new ArrayList();
        if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
            actualParameters = null;
            return;
        }

        if (isPreview) {
            int index = 0;
            ObservableList<Integer> selected = filesTableView.getSelectionModel().getSelectedIndices();
            if (selected != null && !selected.isEmpty()) {
                index = selected.get(0);
            }
            sourceFiles.add(sourceFilesInformation.get(index).getFile());
            sourcesIndice.add(index);
        } else {
            for (int i = 0; i < sourceFilesInformation.size(); i++) {
                sourcesIndice.add(i);
                sourceFiles.add(sourceFilesInformation.get(i).getFile());
            }
        }

    }

    public ProcessParameters copyParameters(ProcessParameters theConversion) {
        ProcessParameters newConversion = new ProcessParameters();
        newConversion.aColor = theConversion.aColor;
        newConversion.aCompression = theConversion.aCompression;
        newConversion.aSize = theConversion.aSize;
        newConversion.aDensity = theConversion.aDensity;
        newConversion.aQuality = theConversion.aQuality;
        newConversion.acumDigit = theConversion.acumDigit;
        newConversion.acumFrom = theConversion.acumFrom;
        newConversion.acumStart = theConversion.acumStart;
        newConversion.currentNameNumber = theConversion.currentNameNumber;
        newConversion.currentPage = theConversion.currentPage;
        newConversion.currentIndex = theConversion.currentIndex;
        newConversion.startIndex = theConversion.startIndex;
        newConversion.currentTotalHandled = theConversion.currentTotalHandled;
        newConversion.sourceFile = theConversion.sourceFile;
        newConversion.targetRootPath = theConversion.targetRootPath;
        newConversion.targetPath = theConversion.targetPath;
        newConversion.targetPrefix = theConversion.targetPrefix;
        newConversion.fill = theConversion.fill;
        newConversion.createSubDir = theConversion.createSubDir;
        newConversion.fromPage = theConversion.fromPage;
        newConversion.password = theConversion.password;
        newConversion.startPage = theConversion.startPage;
        newConversion.status = theConversion.status;
        newConversion.toPage = theConversion.toPage;
        newConversion.startTime = theConversion.startTime;
        newConversion.isBatch = theConversion.isBatch;
        return newConversion;
    }

    public void doCurrentProcess() {
        try {
            if (currentParameters == null || sourceFiles.isEmpty()) {
                return;
            }
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                private boolean ok;

                @Override
                public Void call() {
                    for (; currentParameters.currentIndex < sourceFiles.size();) {
                        if (isCancelled()) {
                            break;
                        }
                        handleCurrentFile();

                        currentParameters.currentIndex++;
                        updateProgress(currentParameters.currentIndex, sourceFiles.size());
                        updateMessage(currentParameters.currentIndex + "/" + sourceFiles.size());

                        if (isCancelled() || isPreview) {
                            break;
                        }

                    }
                    ok = true;

                    return null;
                }

                @Override
                public void succeeded() {
                    super.succeeded();
                    updateInterface("Done");
                }

                @Override
                public void cancelled() {
                    super.cancelled();
                    updateInterface("Canceled");
                }

                @Override
                public void failed() {
                    super.failed();
                    updateInterface("Failed");
                }
            };
            operationBarController.progressValue.textProperty().bind(task.messageProperty());
            operationBarController.progressBar.progressProperty().bind(task.progressProperty());
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    @FXML
    public void pauseProcess(ActionEvent event) {
        paused = true;
        if (task != null && task.isRunning()) {
            task.cancel();
        } else {
            updateInterface("Canceled");
        }
    }

    public void cancelProcess(ActionEvent event) {
        paused = false;
        if (task != null && task.isRunning()) {
            task.cancel();
        } else {
            updateInterface("Canceled");
        }
    }

    public void updateInterface(final String newStatus) {
        currentParameters.status = newStatus;
        if (operationBarController == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (operationBarController.fileProgressBar != null) {
                    operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourcesIndice.size());
                    operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesIndice.size());
                }
                switch (newStatus) {
                    case "StartFile":
                        operationBarController.statusLabel.setText(currentParameters.sourceFile.getName() + " "
                                + getMessage("Handling...") + " "
                                + getMessage("StartTime")
                                + ": " + DateTools.datetimeToString(currentParameters.startTime));
                        if (operationBarController.fileProgressBar != null) {
                            operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourcesIndice.size());
                            operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesIndice.size());
                        }
                        break;

                    case "Started":
                        operationBarController.startButton.setText(AppVaribles.getMessage("Cancel"));
                        operationBarController.startButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                cancelProcess(event);
                            }
                        });
                        if (allowPaused) {
                            operationBarController.pauseButton.setVisible(true);
                            operationBarController.pauseButton.setDisable(false);
                            operationBarController.pauseButton.setText(AppVaribles.getMessage("Pause"));
                            operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pauseProcess(event);
                                }
                            });
                        }
                        paraBox.setDisable(true);
                        break;

                    case "CompleteFile":
                        showCost();
                        if (operationBarController.fileProgressBar != null) {
                            operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourcesIndice.size());
                            operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesIndice.size());
                        }
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
                            paraBox.setDisable(true);
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
                            paraBox.setDisable(false);
                            donePost();
                        }
                        showCost();

                }

            }
        });

    }

    public double countAverageTime(long cost) {
        double avg = 0;
        if (currentParameters.currentTotalHandled != 0) {
            avg = ValueTools.roundDouble3((double) cost / currentParameters.currentTotalHandled);
        }
        return avg;
    }

    public void showCost() {
        if (operationBarController.statusLabel == null) {
            return;
        }
        long cost = (new Date().getTime() - currentParameters.startTime.getTime()) / 1000;
        double avg = countAverageTime(cost);
        String s;
        if (paused) {
            s = getMessage("Paused");
        } else {
            s = getMessage(currentParameters.status);
        }
        s += ". " + getMessage("HandledThisTime") + ": " + currentParameters.currentTotalHandled + " ";
        int count = 0;
        if (targetFiles != null) {
            count = targetFiles.size();
            popInformation(MessageFormat.format(AppVaribles.getMessage("FilesGenerated"), targetFiles.size()));
        }
        s += MessageFormat.format(AppVaribles.getMessage("FilesGenerated"), count) + " ";
        s += getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(currentParameters.startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

    public void markFileHandled(int index) {
        if (filesTableController == null) {
            return;
        }
        FileInformation d = filesTableController.sourceFilesInformation.get(index);
        if (d == null) {
            return;
        }
        d.setHandled(getMessage("Yes"));
        filesTableController.filesTableView.refresh();
    }

    public void markFileHandled(int index, String message) {
        if (filesTableController == null) {
            return;
        }
        FileInformation d = filesTableController.sourceFilesInformation.get(index);
        if (d == null) {
            return;
        }
        d.setHandled(message);
        filesTableController.filesTableView.refresh();
    }

    public FileInformation findFileInformation(String filename) {
        for (FileInformation d : sourceFilesInformation) {
            if (d.getFileName().equals(filename)) {
                return d;
            }
        }
        return null;
    }

    public FileInformation findFileInformation(File file) {
        for (FileInformation d : sourceFilesInformation) {
            if (d.getFile().equals(file)) {
                return d;
            }
        }
        return null;
    }

    public int findFileIndex(String filename) {
        for (int i = 0; i < sourceFilesInformation.size(); i++) {
            FileInformation d = sourceFilesInformation.get(i);
            if (d.getFileName().equals(filename)) {
                return i;
            }
        }
        return -1;
    }

}
