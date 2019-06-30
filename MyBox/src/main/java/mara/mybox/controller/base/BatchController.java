package mara.mybox.controller.base;

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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ProcessParameters;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BatchController<T> extends TableController<T> {

    public List<File> sourceFiles, targetFiles;
    public TargetExistType targetExistType;
    public int dirTotal, dirOk;
    public List<Integer> sourcesIndice;
    public List<String> filesPassword;
    public String creatSubdirKey, previewKey, fillZeroKey;
    public String appendColorKey, appendCompressionTypeKey;
    public String appendDensityKey, appendQualityKey, appendSizeKey;
    public boolean allowPaused, browseTargets;
    public boolean isPreview, paused;

    public ProcessParameters actualParameters, previewParameters, currentParameters;

    public static enum TargetExistType {
        Rename, Replace, Skip
    }

    /**
     * Methods to be implemented
     */
    // "operationBarController.startButton.disableProperty()" should be defined by subClass
    public abstract void initializeNext2();

    // SubClass should use either "makeSingleParameters()" or "makeBatchParameters()"
    public abstract void makeMoreParameters();

    // "targetFiles" and "actualParameters.finalTargetName" should be written by subClass
    public abstract boolean handleCurrentFile();

    public abstract File getTableFile(int index);

    @FXML
    public TableController tableController;
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
    @FXML
    public Button pauseButton, openTargetButton;
    @FXML
    public ProgressBar progressBar, fileProgressBar;
    @FXML
    public Label progressValue, fileProgressValue;
    @FXML
    public CheckBox miaoCheck, openCheck;

    public BatchController() {
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

    /* ----Method may need updated ------------------------------------------------- */
    public void initOptionsSection() {

    }

    public void donePost() {
        if (miaoCheck != null && miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }
        if (!isPreview && openCheck != null && !openCheck.isSelected()) {
            return;
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
        view(file);
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
                view(path);
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
            final ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
            if (controller != null) {
                controller.loadImages(targetFiles.subList(0, Math.min(9, targetFiles.size())), 3);
            }
        } catch (Exception e) {
        }
    }


    /* ------Method need not updated commonly ----------------------------------------------- */
    @Override
    public void initControls() {
        try {
            super.initControls();

            if (tableController != null) {
                tableController.fileExtensionFilter = fileExtensionFilter;
                tableController.sourcePathKey = sourcePathKey;
                tableController.sourcePathKey = sourcePathKey;
                tableController.SourceFileType = SourceFileType;
                tableController.SourcePathType = SourcePathType;
                tableController.TargetPathType = TargetPathType;
                tableController.TargetFileType = TargetFileType;
                tableController.AddFileType = AddFileType;
                tableController.AddPathType = AddPathType;
                tableController.targetPathKey = targetPathKey;
                tableController.LastPathKey = LastPathKey;
                tableController.operationType = operationType;
            }

            // Work for either embedded fxml or direct controls with same names
            if (tableController != null) {
                tableData = tableController.tableData;
                tableView = tableController.tableView;
            }
            if (operationBarController != null) {
                startButton = operationBarController.startButton;
                pauseButton = operationBarController.pauseButton;
                openTargetButton = operationBarController.openTargetButton;
                progressBar = operationBarController.progressBar;
                fileProgressBar = operationBarController.fileProgressBar;
                progressValue = operationBarController.progressValue;
                fileProgressValue = operationBarController.fileProgressValue;
                miaoCheck = operationBarController.miaoCheck;
                openCheck = operationBarController.openCheck;
                statusLabel = operationBarController.statusLabel;
            }

            if (subdirCheck != null) {
                subdirCheck.setSelected(AppVaribles.getUserConfigBoolean(creatSubdirKey));
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
            if (tableController == null) {
                initTable();
            }
            initOptionsSection();
            initTargetSection();

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
                if (startButton != null) {
                    previewButton.disableProperty().bind(
                            startButton.disableProperty()
                                    .or(startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
                    );
                }
            }

            if (operationBarController == null) {
                if (miaoCheck != null) {
                    miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                            AppVaribles.setUserConfigValue("Miao", newValue);

                        }
                    });
                    miaoCheck.setSelected(AppVaribles.getUserConfigBoolean("Miao"));
                }

                if (openCheck != null) {
                    openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                            AppVaribles.setUserConfigValue("OpenWhenComplete", newValue);

                        }
                    });
                    openCheck.setSelected(AppVaribles.getUserConfigBoolean("OpenWhenComplete"));
                }
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
    public void previewAction() {
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
        sourcesIndice.add(0);
        actualParameters.sourceFile = sourceFile;

    }

    public void makeBatchParameters() {
        actualParameters.isBatch = true;

        sourcesIndice = new ArrayList();
        sourceFiles = new ArrayList();
        if (tableData == null || tableData.isEmpty()) {
            actualParameters = null;
            return;
        }

        if (isPreview) {
            int index = 0;
            ObservableList<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (selected != null && !selected.isEmpty()) {
                index = selected.get(0);
            }
            sourceFiles.add(getTableFile(index));
            sourcesIndice.add(index);
        } else {
            for (int i = 0; i < tableData.size(); i++) {
                sourcesIndice.add(i);
                sourceFiles.add(getTableFile(i));
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
            progressValue.textProperty().bind(task.messageProperty());
            progressBar.progressProperty().bind(task.progressProperty());
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (fileProgressBar != null) {
                    fileProgressBar.setProgress(currentParameters.currentIndex / sourcesIndice.size());
                    fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesIndice.size());
                }
                switch (newStatus) {
                    case "StartFile":
                        statusLabel.setText(currentParameters.sourceFile.getName() + " "
                                + getMessage("Handling...") + " "
                                + getMessage("StartTime")
                                + ": " + DateTools.datetimeToString(currentParameters.startTime));
                        if (fileProgressBar != null) {
                            fileProgressBar.setProgress(currentParameters.currentIndex / sourcesIndice.size());
                            fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesIndice.size());
                        }
                        break;

                    case "Started":
                        startButton.setText(AppVaribles.getMessage("Cancel"));
                        startButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                cancelProcess(event);
                            }
                        });
                        if (allowPaused) {
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVaribles.getMessage("Pause"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
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
                        if (fileProgressBar != null) {
                            fileProgressBar.setProgress(currentParameters.currentIndex / sourcesIndice.size());
                            fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesIndice.size());
                        }
                        break;

                    case "Done":
                    default:
                        if (paused) {
                            startButton.setText(AppVaribles.getMessage("Cancel"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelProcess(event);
                                }
                            });
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVaribles.getMessage("Continue"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startAction();
                                }
                            });
                            paraBox.setDisable(true);
                        } else {
                            startButton.setText(AppVaribles.getMessage("Start"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startAction();
                                }
                            });
                            pauseButton.setVisible(false);
                            pauseButton.setDisable(true);
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
            avg = DoubleTools.scale3((double) cost / currentParameters.currentTotalHandled);
        }
        return avg;
    }

    public void showCost() {
        if (statusLabel == null) {
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
        statusLabel.setText(s);
    }

}
