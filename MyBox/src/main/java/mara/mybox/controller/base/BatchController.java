package mara.mybox.controller.base;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.data.ProcessParameters;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getUserConfigValue;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.AppVaribles.setUserConfigValue;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BatchController<T> extends BaseController {

    protected String targetSubdirKey, previewKey, targetExistKey, targetAppendKey;

    protected ObservableList<T> tableData;
    protected TableView<T> tableView;

    protected List<File> sourceFiles, targetFiles;
    protected TargetExistType targetExistType;
    protected List<Integer> sourcesIndice;
    protected List<String> filesPassword;

    protected boolean sourceCheckSubdir, allowPaused, browseTargets;
    protected boolean isPreview, paused;
    protected String targetFileType, targetNameAppend;
    protected int dirFilesNumber, dirFilesHandled;
    protected String[] sourceFilesfilters;

    protected ProcessParameters actualParameters, previewParameters, currentParameters;

    public static enum TargetExistType {
        Rename, Replace, Skip
    }

    @FXML
    protected TableController<T> tableController;
    @FXML
    protected ToggleGroup targetExistGroup, fileTypeGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected TextField targetAppendInput, previewInput, acumFromInput, digitInput;
    @FXML
    protected CheckBox targetSubdirCheck, miaoCheck, openCheck;
    @FXML
    protected Button pauseButton, openTargetButton;
    @FXML
    protected ProgressBar progressBar, fileProgressBar;
    @FXML
    protected Label progressValue, fileProgressValue;

    public BatchController() {
        targetSubdirKey = "targetSubdirKey";
        previewKey = "previewKey";
        targetExistKey = "targetExistKey";
        targetAppendKey = "targetAppendKey";

        browseTargets = false;

        sourcePathKey = "sourcePath";
        sourceExtensionFilter = CommonValues.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    /* ----Method may need override ------------------------------------------------- */
    public void initOptionsSection() {

    }

    // "targetFiles" and "actualParameters.finalTargetName" should be written by this method
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return AppVaribles.message("Skip");
        }
        currentParameters.finalTargetName = target.getAbsolutePath();
        targetFiles.add(target);
        return AppVaribles.message("Successful");
    }

    public void donePost() {
        tableView.refresh();
        if (miaoCheck != null && miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }
        if (!isPreview && openCheck != null && !openCheck.isSelected()) {
            return;
        }
        if (targetFiles == null || targetFiles.size() == 1) {
            if (actualParameters.finalTargetName == null
                    || !new File(actualParameters.finalTargetName).exists()) {
                alertInformation(AppVaribles.message("NoDataNotSupported"));
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
            popInformation(AppVaribles.message("NoFileGenerated"));
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
                if (targetPrefixInput != null && targetSubdirCheck != null && targetSubdirCheck.isSelected()) {
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
                popInformation(AppVaribles.message("NoFileGenerated"));
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


    /* ------Method need not override commonly ----------------------------------------------- */
    @Override
    public void initControls() {
        try {
            super.initControls();

            if (tableController != null) {
                tableController.parentController = this;
                tableController.parentFxml = myFxml;

                tableController.sourceExtensionFilter = sourceExtensionFilter;
                tableController.targetExtensionFilter = targetExtensionFilter;
                tableController.SourceFileType = SourceFileType;
                tableController.SourcePathType = SourcePathType;
                tableController.TargetPathType = TargetPathType;
                tableController.TargetFileType = TargetFileType;
                tableController.AddFileType = AddFileType;
                tableController.AddPathType = AddPathType;
                tableController.sourcePathKey = sourcePathKey;
                tableController.targetPathKey = targetPathKey;
                tableController.LastPathKey = LastPathKey;
                tableController.operationType = operationType;

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

            if (targetSubdirCheck != null) {
                targetSubdirCheck.setSelected(AppVaribles.getUserConfigBoolean(targetSubdirKey));
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

            initControlsMore();

            initOptionsSection();
            initTargetSection();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void initControlsMore() {

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
                targetAppendInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                        checkTargetExistType();
                    }
                });
                isSettingValues = true;
                FxmlControl.setRadioSelected(targetExistGroup, getUserConfigValue(targetExistKey, message("Replace")));
                targetAppendInput.setText(getUserConfigValue(targetAppendKey, null));
                isSettingValues = false;
                checkTargetExistType();
            }

            if (startButton != null) {
                if (targetPathInput != null) {
                    startButton.disableProperty().bind(
                            Bindings.isEmpty(tableView.getItems())
                                    .or(Bindings.isEmpty(targetPathInput.textProperty()))
                                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    );
                }
                if (previewButton != null) {
                    previewButton.disableProperty().bind(startButton.disableProperty()
                            .or(startButton.textProperty().isNotEqualTo(AppVaribles.message("Start")))
                    );
                }
            }

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

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void checkTargetExistType() {
        if (isSettingValues) {
            return;
        }
        targetAppendInput.setStyle(null);
        RadioButton selected = (RadioButton) targetExistGroup.getSelectedToggle();
        if (selected.equals(targetReplaceRadio)) {
            targetExistType = TargetExistType.Replace;

        } else if (selected.equals(targetRenameRadio)) {
            targetExistType = TargetExistType.Rename;
            if (targetAppendInput.getText() == null || targetAppendInput.getText().trim().isEmpty()) {
                targetAppendInput.setStyle(badStyle);
            } else {
                setUserConfigValue(targetAppendKey, targetAppendInput.getText().trim());
            }

        } else if (selected.equals(targetSkipRadio)) {
            targetExistType = TargetExistType.Skip;
        }
        setUserConfigValue(targetExistKey, selected.getText());
    }

    @FXML
    @Override
    public void startAction() {
        isPreview = false;
        if (!makeActualParameters()) {
//            popError(message("Invalid"));
            actualParameters = null;
            return;
        }
        currentParameters = actualParameters;
        paused = false;
        doCurrentProcess();
    }

    @FXML
    public void previewAction() {
        if (!makePreviewParameters()) {
            return;
        }
        doCurrentProcess();
    }

    public boolean makeActualParameters() {
        if (actualParameters != null && paused) {
            actualParameters.startIndex = actualParameters.currentIndex;
            actualParameters.startPage = actualParameters.currentPage;
            return true;
        }
        actualParameters = new ProcessParameters();
        actualParameters.currentIndex = 0;
        targetPath = null;

        sourceCheckSubdir = true;
        if (tableController.tableSubdirCheck != null) {
            sourceCheckSubdir = tableController.tableSubdirCheck.isSelected();
        }
        sourceFilesfilters = null;
        if (tableController.tableFiltersInput != null) {
            sourceFilesfilters = tableController.tableFiltersInput.getText().trim().split("\\s+");
            if (sourceFilesfilters.length == 0) {
                sourceFilesfilters = null;
            }
        }

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

        if (targetSubdirCheck != null) {
            actualParameters.targetSubDir = targetSubdirCheck.isSelected();
            AppVaribles.setUserConfigValue(targetSubdirKey, actualParameters.targetSubDir);
        }

        actualParameters.fromPage = 1;
        actualParameters.toPage = 0;
        actualParameters.startPage = 1;
        actualParameters.password = "";
        actualParameters.acumFrom = 1;
        actualParameters.acumStart = 1;
        actualParameters.acumDigit = 0;

        sourcesIndice = new ArrayList();
        sourceFiles = new ArrayList();
        targetFiles = new ArrayList();

        return makeMoreParameters();

    }

    public boolean makeMoreParameters() {
        return makeBatchParameters();
    }

    public boolean makeBatchParameters() {

        if (tableData == null || tableData.isEmpty()) {
            actualParameters = null;
            return false;
        }

        if (isPreview) {
            int index = 0;
            ObservableList<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (selected != null && !selected.isEmpty()) {
                index = selected.get(0);
            }
            sourceFiles.add(tableController.file(index));
            sourcesIndice.add(index);
        } else {
            for (int i = 0; i < tableData.size(); i++) {
                sourcesIndice.add(i);
                sourceFiles.add(tableController.file(i));
            }
        }
        return true;
    }

    public boolean makePreviewParameters() {
        if (!makeActualParameters()) {
            popError(message("Invalid"));
            actualParameters = null;
            return false;
        }
        previewParameters = copyParameters(actualParameters);
        previewParameters.status = "start";
        currentParameters = previewParameters;
        isPreview = true;
        return true;
    }

    public ProcessParameters copyParameters(ProcessParameters theConversion) {
        ProcessParameters newParameters = new ProcessParameters();
        newParameters.acumDigit = theConversion.acumDigit;
        newParameters.acumFrom = theConversion.acumFrom;
        newParameters.acumStart = theConversion.acumStart;
        newParameters.currentPage = theConversion.currentPage;
        newParameters.currentIndex = theConversion.currentIndex;
        newParameters.startIndex = theConversion.startIndex;
        newParameters.currentTotalHandled = theConversion.currentTotalHandled;
        newParameters.currentSourceFile = theConversion.currentSourceFile;
        newParameters.targetRootPath = theConversion.targetRootPath;
        newParameters.targetPath = theConversion.targetPath;
        newParameters.targetSubDir = theConversion.targetSubDir;
        newParameters.fromPage = theConversion.fromPage;
        newParameters.password = theConversion.password;
        newParameters.startPage = theConversion.startPage;
        newParameters.status = theConversion.status;
        newParameters.toPage = theConversion.toPage;
        newParameters.startTime = theConversion.startTime;
        newParameters.isBatch = theConversion.isBatch;
        return newParameters;
    }

    public File getCurrentFile() {
        return sourceFiles.get(currentParameters.currentIndex);
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
                    updateTaskProgress(currentParameters.currentIndex, sourceFiles.size());
                    for (; currentParameters.currentIndex < sourceFiles.size(); currentParameters.currentIndex++) {
                        if (isCancelled()) {
                            break;
                        }
                        currentParameters.currentSourceFile = sourceFiles.get(currentParameters.currentIndex);
                        if (statusLabel != null) {
                            statusLabel.setText(currentParameters.currentSourceFile.getName() + " "
                                    + message("Handling...") + " "
                                    + message("StartTime")
                                    + ": " + DateTools.datetimeToString(new Date()));
                        }

                        handleCurrentFile();

                        updateTaskProgress(currentParameters.currentIndex + 1, sourceFiles.size());

                        if (isCancelled() || isPreview) {
                            break;
                        }

                    }
                    updateTaskProgress(currentParameters.currentIndex, sourceFiles.size());
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
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    public void updateTaskProgress(long number, long total) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                double p = (number * 1d) / total;
                String s = number + "/" + total;
                if (fileProgressBar != null) {
                    fileProgressBar.setProgress(p);
                    fileProgressValue.setText(s);
                } else if (progressBar != null) {
                    progressBar.setProgress(p);
                    progressValue.setText(s);
                }
            }
        });
    }

    public void updateFileProgress(long number, long total) {
        if (progressBar != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    double p = (number * 1d) / total;
                    String s = number + "/" + total;
                    progressBar.setProgress(p);
                    progressValue.setText(s);
                }
            });
        }
    }

    public void handleCurrentFile() {
        currentParameters.currentSourceFile = getCurrentFile();
        String result;
        if (!currentParameters.currentSourceFile.exists()) {
            result = AppVaribles.message("NotFound");
        } else if (currentParameters.currentSourceFile.isFile()) {
            result = handleFile(currentParameters.currentSourceFile);
        } else if (currentParameters.currentSourceFile.isDirectory()) {
            result = handleDirectory(currentParameters.currentSourceFile);
        } else {
            return;
        }
        currentParameters.currentTotalHandled++;
        tableController.markFileHandled(currentParameters.currentIndex, result);
    }

    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = "." + FileTools.getFileSuffix(sourceFile.getName());
                if (targetFileType != null) {
                    nameSuffix = "." + targetFileType;
                }
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

    public File makeTargetFile(String namePrefix, String nameSuffix, File targetPath) {
        try {

            String targetPrefix = targetPath.getAbsolutePath() + File.separator + namePrefix;
            File target = new File(targetPrefix + nameSuffix);
            if (target.exists()) {
                if (targetExistType == TargetExistType.Skip) {
                    target = null;
                } else if (targetExistType == TargetExistType.Rename) {
                    if (targetAppendInput != null) {
                        targetNameAppend = targetAppendInput.getText().trim();
                    }
                    if (targetNameAppend == null || targetNameAppend.isEmpty()) {
                        targetNameAppend = "-m";
                    }
                    while (true) {
                        targetPrefix = targetPrefix + targetNameAppend;
                        target = new File(targetPrefix + nameSuffix);
                        if (!target.exists()) {
                            break;
                        }
                    }
                }
            }
            return target;
        } catch (Exception e) {
            return null;
        }
    }

    public String handleFile(File file) {
        try {
            if (!matchFilters(file)) {
                return AppVaribles.message("Skip");
            }
            if (currentParameters.targetPath != null) {
                return handleFile(file, new File(currentParameters.targetPath));
            } else {
                return handleFile(file, null);
            }
        } catch (Exception e) {
            return AppVaribles.message("Failed");
        }
    }

    public boolean matchFilters(File file) {
        if (sourceFilesfilters == null) {
            return true;
        }
        String fname = file.getName();
        switch (tableController.getFilterType()) {
            case None:
                return true;

            case IncludeOne:
                for (String name : sourceFilesfilters) {
                    if (fname.contains(name)) {
                        return true;
                    }
                }
                return false;

            case IncludeAll:
                for (String name : sourceFilesfilters) {
                    if (!fname.contains(name)) {
                        return false;
                    }
                }
                return true;

            case NotIncludeAny:
                for (String name : sourceFilesfilters) {
                    if (fname.contains(name)) {
                        return false;
                    }
                }
                return true;

            case NotIncludeAll:
                for (String name : sourceFilesfilters) {
                    if (!fname.contains(name)) {
                        return true;
                    }
                }
                return false;

        }

        return true;
    }

    public String handleDirectory(File dir) {
        try {
            dirFilesNumber = dirFilesHandled = 0;
            if (currentParameters.targetPath != null) {
                handleDirectory(dir, new File(currentParameters.targetPath));
            } else {
                handleDirectory(dir, null);
            }
            return MessageFormat.format(AppVaribles.message("DirHandledSummary"), dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            logger.debug(e.toString());
            return AppVaribles.message("Failed");
        }
    }

    protected void handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()
                || (isPreview && dirFilesHandled > 0)) {
            return;
        }
        try {
            File[] files = sourcePath.listFiles();
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                if (srcFile.isFile()) {
                    dirFilesNumber++;
                    if (isPreview && dirFilesHandled > 0) {
                        return;
                    }
                    if (!matchFilters(srcFile)) {
                        continue;
                    }
                    String result = handleFile(srcFile, targetPath);
                    if (!AppVaribles.message("Failed").equals(result)
                            && !AppVaribles.message("Skip").equals(result)) {
                        dirFilesHandled++;
                    }
                } else if (srcFile.isDirectory() && sourceCheckSubdir) {
                    if (targetPath != null) {
                        File subPath = makeTargetFile(srcFile, targetPath);
                        if (!subPath.exists()) {
                            subPath.mkdirs();
                        }
                        handleDirectory(srcFile, subPath);
                    } else {
                        handleDirectory(srcFile, targetPath);
                    }
                }
            }
        } catch (Exception e) {
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

                switch (newStatus) {

                    case "Started":
                        startButton.setText(AppVaribles.message("Cancel"));
                        startButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                cancelProcess(event);
                            }
                        });
                        if (allowPaused) {
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVaribles.message("Pause"));
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
                        break;

                    case "Done":
                    default:
                        if (paused) {
                            startButton.setText(AppVaribles.message("Cancel"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelProcess(event);
                                }
                            });
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVaribles.message("Continue"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    startAction();
                                }
                            });
                            paraBox.setDisable(true);
                        } else {
                            startButton.setText(AppVaribles.message("Start"));
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
            s = message("Paused");
        } else {
            s = message(currentParameters.status);
        }
        String space = "   ";
        s += ". " + message("HandledThisTime") + ":" + currentParameters.currentTotalHandled + space;
        int count = 0;
        if (targetFiles != null) {
            count = targetFiles.size();
            popInformation(MessageFormat.format(AppVaribles.message("FilesGenerated"), count));
        }
        s += MessageFormat.format(AppVaribles.message("FilesGenerated"), count) + space;
        s += message("Cost") + ": " + cost + " " + message("Seconds") + "." + space
                + message("Average") + ":" + avg + " " + message("SecondsPerItem") + "." + space
                + message("StartTime") + ":" + DateTools.datetimeToString(currentParameters.startTime) + space
                + message("EndTime") + ":" + DateTools.datetimeToString(new Date());
        statusLabel.setText(s);
    }

}
