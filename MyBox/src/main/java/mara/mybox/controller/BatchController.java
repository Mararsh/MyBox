package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.data.ProcessParameters;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BatchController<T> extends BaseController {

    protected String targetSubdirKey, previewKey;

    protected ObservableList<T> tableData;
    protected TableView<T> tableView;

    protected List<File> sourceFiles, targetFiles;

    protected List<String> filesPassword;

    protected boolean sourceCheckSubdir, allowPaused, browseTargets, viewTargetPath,
            createDirectories;
    protected boolean isPreview, paused;
    protected int dirFilesNumber, dirFilesHandled;
    protected long fileSelectorSize, fileSelectorTime;
    protected String[] sourceFilesSelector;
    protected FileSelectorType fileSelectorType;
    protected SimpleBooleanProperty optionsValid;

    protected ProcessParameters actualParameters, previewParameters, currentParameters;

    @FXML
    protected BatchTableController<T> tableController;
    @FXML
    protected VBox tableBox;
    @FXML
    protected TextField previewInput, acumFromInput, digitInput;
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

        browseTargets = viewTargetPath = false;

        sourcePathKey = "sourcePath";
        sourceExtensionFilter = CommonFxValues.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    /* ----Method may need override ------------------------------------------------- */
    public void initOptionsSection() {

    }

    // "targetFiles" and "actualParameters.finalTargetName" should be written by this method
    public String handleFile(File srcFile, File targetPath) {
        showHandling(srcFile);
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return AppVariables.message("Skip");
        }
        currentParameters.finalTargetName = target.getAbsolutePath();
        targetFiles.add(target);
        return AppVariables.message("Successful");
    }

    public void donePost() {
        tableView.refresh();
        if (miaoCheck != null && miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }
        if (!isPreview && openCheck != null && !openCheck.isSelected()) {
            return;
        }
        if (targetFiles != null && viewTargetPath) {
            openTarget(null);
        } else if (targetFiles == null || targetFiles.size() == 1) {
            if (actualParameters.finalTargetName == null
                    || !new File(actualParameters.finalTargetName).exists()) {
                alertInformation(AppVariables.message("NoDataNotSupported"));
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
            popInformation(AppVariables.message("NoFileGenerated"));
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
                browseURI(path.toURI());
                recordFileOpened(path);
            } else {
                popInformation(AppVariables.message("NoFileGenerated"));
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
    public void initValues() {
        try {
            super.initValues();
            optionsValid = new SimpleBooleanProperty(true);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

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
                targetSubdirCheck.setSelected(AppVariables.getUserConfigBoolean(targetSubdirKey));
            }

            if (acumFromInput != null) {
                FxmlControl.setNonnegativeValidation(acumFromInput);
                acumFromInput.setText("1");
            }

            if (previewInput != null) {
                previewInput.setText(AppVariables.getUserConfigValue(previewKey, "1"));
                FxmlControl.setPositiveValidation(previewInput);
                previewInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        AppVariables.setUserConfigValue(previewKey, newValue);
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

            if (startButton != null) {
                if (targetPathInput != null) {
                    if (tableView != null) {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(tableView.getItems())
                                        .or(Bindings.isEmpty(targetPathInput.textProperty()))
                                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                                        .or(optionsValid.not())
                        );
                    } else {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(targetPathInput.textProperty())
                                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                                        .or(optionsValid.not())
                        );
                    }
                } else {
                    if (tableView != null) {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(tableView.getItems())
                                        .or(optionsValid.not())
                        );
                    }
                }
                if (previewButton != null) {
                    previewButton.disableProperty().bind(startButton.disableProperty()
                            .or(startButton.textProperty().isNotEqualTo(AppVariables.message("Start")))
                    );
                }
            }

            if (miaoCheck != null) {
                miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("Miao", newValue);

                    }
                });
                miaoCheck.setSelected(AppVariables.getUserConfigBoolean("Miao"));
            }

            if (openCheck != null) {
                openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("OpenWhenComplete", newValue);

                    }
                });
                openCheck.setSelected(AppVariables.getUserConfigBoolean("OpenWhenComplete"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
//        logger.debug(event.getCode() + " " + event.getText());
        if (tableController != null) {
            tableController.keyEventsHandler(event); // pass event to table pane
        }
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

        fileSelectorType = tableController.fileSelectorType;
        sourceFilesSelector = null;
        if (tableController.tableFiltersInput != null) {
            sourceFilesSelector = tableController.tableFiltersInput.getText().trim().split("\\s+");
            if (sourceFilesSelector.length == 0) {
                sourceFilesSelector = null;
            }
        }
        fileSelectorSize = tableController.fileSelectorSize;
        fileSelectorTime = tableController.fileSelectorTime;

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
            AppVariables.setUserConfigValue(targetSubdirKey, actualParameters.targetSubDir);
        }

        createDirectories = tableController.tableCreateDirCheck != null
                && tableController.tableCreateDirCheck.isSelected();

        actualParameters.fromPage = 1;
        actualParameters.toPage = 0;
        actualParameters.startPage = 1;
        actualParameters.password = "";
        actualParameters.acumFrom = 1;
        actualParameters.acumStart = 1;
        actualParameters.acumDigit = 0;

        sourceFiles = new ArrayList<>();
        targetFiles = new ArrayList<>();

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
        for (int i = 0; i < tableData.size(); i++) {
            FileInformation d = tableController.fileInformation(i);
            if (d == null) {
                continue;
            }
            d.setHandled("");
            if (!isPreview && !sourceFiles.contains(d.getFile())) {
                sourceFiles.add(d.getFile());
            }
        }
        if (isPreview) {
            int index = 0;
            ObservableList<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (selected != null && !selected.isEmpty()) {
                index = selected.get(0);
            }
            sourceFiles.add(tableController.file(index));
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
            synchronized (this) {
                if (task != null) {
                    return;
                }
                currentParameters.startTime = new Date();
                currentParameters.currentTotalHandled = 0;
                updateInterface("Started");
                task = new SingletonTask<Void>() {

                    @Override
                    public Void call() {
                        if (!beforeHandleFiles()) {
                            ok = false;
                            return null;
                        }
                        updateTaskProgress(currentParameters.currentIndex, sourceFiles.size());
                        int len = sourceFiles.size();
                        for (; currentParameters.currentIndex < len; currentParameters.currentIndex++) {
                            if (isCancelled()) {
                                break;
                            }
                            currentParameters.currentSourceFile = sourceFiles.get(currentParameters.currentIndex);

                            handleCurrentFile();

                            updateTaskProgress(currentParameters.currentIndex + 1, len);

                            if (isCancelled() || isPreview) {
                                break;
                            }
                        }
                        afterHandleFiles();
                        updateTaskProgress(currentParameters.currentIndex, len);
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

                    @Override
                    protected void taskQuit() {
                        quitProcess();
                        task = null;
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

    public boolean beforeHandleFiles() {
        return true;
    }

    public void afterHandleFiles() {

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
        try {
            tableController.markFileHandling(currentParameters.currentIndex);
            currentParameters.currentSourceFile = getCurrentFile();
            String result;
            if (!currentParameters.currentSourceFile.exists()) {
                result = AppVariables.message("NotFound");
            } else if (currentParameters.currentSourceFile.isFile()) {
                result = handleFile(currentParameters.currentSourceFile);
            } else if (currentParameters.currentSourceFile.isDirectory()) {
                result = handleDirectory(currentParameters.currentSourceFile);
            } else {
                return;
            }
            currentParameters.currentTotalHandled++;
            tableController.markFileHandled(currentParameters.currentIndex, result);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public String handleFile(File file) {
        try {
            if (!match(file)) {
                return AppVariables.message("Skip");
            }
            if (currentParameters.targetPath != null) {
                return handleFile(file, new File(currentParameters.targetPath));
            } else {
                return handleFile(file, null);
            }
        } catch (Exception e) {
            return AppVariables.message("Failed");
        }
    }

    public boolean matchType(File file) {
        return true;
    }

    public boolean match(File file) {
        if (file == null || !file.isFile() || !matchType(file)) {
            return false;
        }

        if (fileSelectorType == FileSelectorType.All) {
            return true;
        }
        if (sourceFilesSelector == null) {
            sourceFilesSelector = new String[0];
        }
        String fname = file.getName();
        String suffix = FileTools.getFileSuffix(fname);
        switch (fileSelectorType) {

            case ExtensionEuqalAny:
                if (suffix.isBlank()) {
                    return false;
                }
                for (String name : sourceFilesSelector) {
                    if (suffix.equals(name) || ("." + suffix).equals(name)) {
                        return true;
                    }
                }
                return false;

            case ExtensionNotEqualAny:
                if (suffix.isBlank()) {
                    return true;
                }
                for (String name : sourceFilesSelector) {
                    if (suffix.equals(name) || ("." + suffix).equals(name)) {
                        return false;
                    }
                }
                return true;

            case NameIncludeAny:
                for (String name : sourceFilesSelector) {
                    if (fname.contains(name)) {
                        return true;
                    }
                }
                return false;

            case NameIncludeAll:
                for (String name : sourceFilesSelector) {
                    if (!fname.contains(name)) {
                        return false;
                    }
                }
                return true;

            case NameNotIncludeAny:
                for (String name : sourceFilesSelector) {
                    if (fname.contains(name)) {
                        return false;
                    }
                }
                return true;

            case NameNotIncludeAll:
                for (String name : sourceFilesSelector) {
                    if (!fname.contains(name)) {
                        return true;
                    }
                }
                return false;

            case NameMatchAnyRegularExpression:
                for (String name : sourceFilesSelector) {
                    if (StringTools.match(fname, name)) {
                        return true;
                    }
                }
                return false;

            case NameNotMatchAnyRegularExpression:
                for (String name : sourceFilesSelector) {
                    if (StringTools.match(fname, name)) {
                        return false;
                    }
                }
                return true;

            case FileSizeLargerThan:
                return file.length() > fileSelectorSize;

            case FileSizeSmallerThan:
                return file.length() < fileSelectorSize;

            case ModifiedTimeEarlierThan:
                return file.lastModified() < fileSelectorTime;

            case ModifiedTimeLaterThan:
                return file.lastModified() > fileSelectorTime;
        }

        return true;
    }

    public String handleDirectory(File dir) {
        try {
            dirFilesNumber = dirFilesHandled = 0;
            if (currentParameters.targetPath != null) {
                File targetDir;
                if (createDirectories) {
                    targetDir = new File(currentParameters.targetPath + File.separator + dir.getName());
                } else {
                    targetDir = new File(currentParameters.targetPath);
                }
                targetDir.mkdirs();
                handleDirectory(dir, targetDir);
            } else {
                handleDirectory(dir, null);
            }
            return MessageFormat.format(AppVariables.message("DirHandledSummary"), dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            logger.debug(e.toString());
            return AppVariables.message("Failed");
        }
    }

    protected boolean handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()
                || (isPreview && dirFilesHandled > 0)) {
            return false;
        }
        try {
            File[] files = sourcePath.listFiles();
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (srcFile.isFile()) {
                    dirFilesNumber++;
                    if (isPreview && dirFilesHandled > 0) {
                        return false;
                    }
                    if (!match(srcFile)) {
                        continue;
                    }
                    String result = handleFile(srcFile, targetPath);
                    if (!AppVariables.message("Failed").equals(result)
                            && !AppVariables.message("Skip").equals(result)) {
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
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @FXML
    public void pauseProcess(ActionEvent event) {
        paused = true;
        if (task != null && task.isRunning()) {
            task.cancel();
            task = null;
        } else {
            updateInterface("Canceled");
        }
    }

    public void cancelProcess(ActionEvent event) {
        paused = false;
        if (task != null && task.isRunning()) {
            task.cancel();
            task = null;
        } else {
            updateInterface("Canceled");
        }
    }

    public void disableControls(boolean disable) {
        if (tableBox != null) {
            tableBox.setDisable(disable);
        }
        if (paraBox != null) {
            paraBox.setDisable(disable);
        }
    }

    public void showHandling(File file) {
        if (statusLabel == null || file == null) {
            return;
        }
        showHandling(file.getAbsolutePath());
    }

    public void showHandling(String name) {
        if (statusLabel == null || name == null) {
            return;
        }
        showStatus(MessageFormat.format(message("HandlingObject"), name) + "    "
                + message("StartTime") + ": " + DateTools.nowString());
    }

    public void showStatus(String info) {
        if (statusLabel == null || info == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    statusLabel.setText(info);
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
            }
        });
    }

    public void updateInterface(final String newStatus) {
        currentParameters.status = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                switch (newStatus) {

                    case "Started":
                        startButton.setText(AppVariables.message("Cancel"));
                        startButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                cancelProcess(event);
                            }
                        });
                        if (allowPaused) {
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVariables.message("Pause"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pauseProcess(event);
                                }
                            });
                        }
                        disableControls(true);
                        break;

                    case "CompleteFile":
                        showCost();
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
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            pauseButton.setText(AppVariables.message("Continue"));
                            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
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
                            pauseButton.setVisible(false);
                            pauseButton.setDisable(true);
                            disableControls(false);
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
        long cost = new Date().getTime() - currentParameters.startTime.getTime();
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
            popInformation(MessageFormat.format(AppVariables.message("FilesGenerated"), count));
        }
        s += MessageFormat.format(AppVariables.message("FilesGenerated"), count) + space;
        s += message("Cost") + ": " + DateTools.showTime(cost) + "." + space
                + message("Average") + ":" + DateTools.showTime(Math.round(avg)) + " "
                + message("StartTime") + ":" + DateTools.datetimeToString(currentParameters.startTime) + space
                + message("EndTime") + ":" + DateTools.datetimeToString(new Date());
        statusLabel.setText(s);
    }

    public void quitProcess() {

    }

    @Override
    public boolean leavingScene() {
        if (tableController != null) {
            if (!tableController.leavingScene()) {
                return false;
            }
        }
        return super.leavingScene();
    }

}
