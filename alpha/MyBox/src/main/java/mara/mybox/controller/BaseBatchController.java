package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.data.ProcessParameters;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchController<T> extends BaseTaskController {

    protected String targetSubdirKey;
    protected ObservableList<T> tableData;
    protected TableView<T> tableView;
    protected List<File> sourceFiles;
    protected List<String> filesPassword;
    protected boolean sourceCheckSubdir, allowPaused, browseTargets, viewTargetPath, createDirectories;
    protected boolean isPreview, paused;
    protected long dirFilesNumber, dirFilesHandled, totalFilesHandled = 0, totalItemsHandled = 0;
    protected long fileSelectorSize, fileSelectorTime;
    protected String[] sourceFilesSelector;
    protected FileSelectorType fileSelectorType;
    protected SimpleBooleanProperty optionsValid;
    protected ProcessParameters actualParameters, previewParameters, currentParameters;
    protected Date processStartTime, fileStartTime;

    @FXML
    protected Tab sourceTab, targetTab;
    @FXML
    protected BaseBatchTableController<T> tableController;
    @FXML
    protected VBox tableBox, optionsVBox, targetVBox;
    @FXML
    protected TextField acumFromInput, digitInput;
    @FXML
    protected CheckBox targetSubdirCheck;
    @FXML
    protected Button pauseButton, openTargetButton;
    @FXML
    protected ProgressBar progressBar, fileProgressBar;
    @FXML
    protected Label progressValue, fileProgressValue;

    public BaseBatchController() {
        targetSubdirKey = "targetSubdirKey";
        browseTargets = viewTargetPath = false;
        allowPaused = false;

        sourceExtensionFilter = FileFilters.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    /* ----Method may need override ------------------------------------------------- */
    public void initOptionsSection() {

    }

    // "targetFiles" and "finalTargetName" should be written by this method
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return message("Skip");
        }
        targetFileGenerated(target);
        return message("Successful");
    }

    public String handleFileWithName(File srcFile, String targetPath) {
        return handleFile(srcFile, targetPath == null ? null : new File(targetPath));
    }

    @Override
    public void afterTask() {
        showCost();
        tableView.refresh();
        recordTargetFiles();
        if (miaoCheck != null && miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (!isPreview && openCheck != null && !openCheck.isSelected()) {
            return;
        }
        if (targetFilesCount > 0 && viewTargetPath) {
            openTarget();
        } else if (targetFiles == null || targetFilesCount == 1) {
            if (lastTargetName == null || !new File(lastTargetName).exists()) {
                alertInformation(message("NoDataNotSupported"));
            } else {
                viewTarget(new File(lastTargetName));
            }
        } else if (targetFilesCount > 0) {
            if (browseTargets) {
                browseAction();
            } else {
                openTarget();
            }
        } else {
            popInformation(message("NoFileGenerated"));
        }
    }

    @Override
    public void recordTargetFiles() {
        if (targetFilesCount > 0) {
            super.recordTargetFiles();
        } else {
            File file = lastTargetFile();
            if (file != null) {
                recordFileWritten(file, TargetFileType);
            } else if (targetPath != null) {
                recordFileWritten(targetPath, TargetPathType);
            }
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
    public void openTarget() {
        try {
            File path = null;
            File lastFile = lastTargetFile();
            if (lastFile != null) {
                path = lastFile.getParentFile();
            } else if (actualParameters != null && actualParameters.targetPath != null) {
                path = new File(actualParameters.targetPath);
            } else if (targetPathController != null) {
                String p = targetPathController.text();
                if (targetPrefixInput != null && targetSubdirCheck != null && targetSubdirCheck.isSelected()) {
                    if (p.endsWith("/") || p.endsWith("\\")) {
                        p += targetPrefixInput.getText();
                    } else {
                        p += "/" + targetPrefixInput.getText();
                    }
                    if (!new File(p).exists()) {
                        p = targetPathController.text();
                    }
                }
                path = new File(p);
            }
            if (path != null && path.exists()) {
                browseURI(path.toURI());
                recordFileOpened(path);
            } else {
                popInformation(message("NoFileGenerated"));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void browseAction() {
        try {
            if (targetFiles == null || targetFiles.isEmpty()) {
                return;
            }
            ImagesBrowserController controller = ImagesBrowserController.open();
            if (controller != null) {
                List<File> files = new ArrayList<>();
                for (int type : targetFiles.keySet()) {
                    List<File> tfiles = targetFiles.get(type);
                    if (tfiles == null) {
                        continue;
                    }
                    files.addAll(tfiles);
                    if (files.size() >= 9) {
                        break;
                    }
                }
                controller.loadImages(files, 3);
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

            if (tableController != null) {
                tableController.parentController = this;
                tableController.parentFxml = myFxml;

                tableController.SourceFileType = getSourceFileType();
                tableController.SourcePathType = SourcePathType;
                tableController.AddFileType = AddFileType;
                tableController.AddPathType = AddPathType;
                tableController.sourceExtensionFilter = sourceExtensionFilter;

                tableController.TargetPathType = TargetPathType;
                tableController.TargetFileType = TargetFileType;
                tableController.targetExtensionFilter = targetExtensionFilter;

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
                statusInput = operationBarController.statusInput;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (targetSubdirCheck != null) {
                targetSubdirCheck.setSelected(UserConfig.getBoolean(targetSubdirKey));
            }

            if (acumFromInput != null) {
                ValidationTools.setNonnegativeValidation(acumFromInput);
                acumFromInput.setText("1");
            }

            initControlsMore();

            initOptionsSection();
            initTargetSection();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void initControlsMore() {

    }

    public void initTargetSection() {
        try {
            if (startButton != null) {
                if (targetPathController != null) {
                    if (tableView != null) {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(tableView.getItems())
                                        .or(Bindings.isEmpty(targetPathController.fileInput.textProperty()))
                                        .or(targetPathController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                                        .or(optionsValid.not())
                        );
                    } else {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(targetPathController.fileInput.textProperty())
                                        .or(targetPathController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                                        .or(optionsValid.not())
                        );
                    }

                } else if (targetFileController != null) {
                    if (tableView != null) {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(tableView.getItems())
                                        .or(Bindings.isEmpty(targetFileController.fileInput.textProperty()))
                                        .or(targetFileController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                                        .or(optionsValid.not())
                        );
                    } else {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(targetFileController.fileInput.textProperty())
                                        .or(targetFileController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
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
                    previewButton.disableProperty().bind(startButton.disableProperty());
                }
            }

            if (openCheck != null) {
                openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue,
                            Boolean newValue) {
                        UserConfig.setBoolean("OpenWhenComplete", newValue);

                    }
                });

                openCheck.setSelected(UserConfig.getBoolean("OpenWhenComplete"));
            }

            if (verboseCheck != null) {
                verboseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue,
                            Boolean newValue) {
                        UserConfig.setBoolean("BatchLogVerbose", newValue);

                    }
                });

                verboseCheck.setSelected(UserConfig.getBoolean("BatchLogVerbose", true));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (tableController != null) {
                return tableController.keyEventsFilter(event); // pass event to table pane
            }
            return false;
        } else {
            return true;
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (tableController != null) {
            tableController.stopCountSize();
        }
        if (statusInput != null) {
            statusInput.setText("");
        }
        isPreview = false;
        if (!makeActualParameters()) {
            popError(message("InvalidParameters"));
            actualParameters = null;
            return;
        }
        currentParameters = actualParameters;
        paused = false;
        beforeTask();
        doCurrentProcess();
    }

    @FXML
    public void previewAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (!makePreviewParameters()) {
            return;
        }
        doCurrentProcess();
    }

    public boolean makeActualParameters() {
        if (paused && currentParameters != null) {
            currentParameters.startIndex = currentParameters.currentIndex;
            currentParameters.startPage = currentParameters.currentPage;
            actualParameters = currentParameters;
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

        if (targetFileController != null) {
            targetFile = targetFileController.file();
            if (targetFile != null) {
                lastTargetName = targetFile.getAbsolutePath();
                targetPath = targetFile.getParentFile();
            }
        }
        if (targetPathController != null) {
            targetPath = targetPathController.file();
        }
        if (targetPath != null) {
            actualParameters.targetRootPath = targetPath.getAbsolutePath();
            actualParameters.targetPath = actualParameters.targetRootPath;
        }

        if (targetSubdirCheck != null) {
            actualParameters.targetSubDir = targetSubdirCheck.isSelected();
            UserConfig.setBoolean(targetSubdirKey, actualParameters.targetSubDir);
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
        beforeTask();

        return makeMoreParameters();

    }

    public boolean makeMoreParameters() {
        try {
            if (tableData == null || tableData.isEmpty()) {
                actualParameters = null;
                return false;
            }
            ObservableList<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            for (int i = 0; i < tableData.size(); ++i) {
                FileInformation d = tableController.fileInformation(i);
                if (d == null) {
                    continue;
                }
                d.setHandled("");
                if (selected != null && !selected.isEmpty() && !selected.contains(i)) {
                    continue;
                }
                File file = d.getFile();
                if (!sourceFiles.contains(file)) {
                    sourceFiles.add(file);
                    if (isPreview) {
                        break;
                    }
                }
            }

            initLogs();
            totalFilesHandled = totalItemsHandled = 0;
            processStartTime = new Date();
            fileStartTime = new Date();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public boolean makePreviewParameters() {
        if (!makeActualParameters()) {
            popError(message("Invalid"));
            actualParameters = null;
            return false;
        }
        try {
            previewParameters = (ProcessParameters) actualParameters.clone();
        } catch (Exception e) {
            return false;
        }
        previewParameters.status = "start";
        currentParameters = previewParameters;
        isPreview = true;
        return true;
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
                if (task != null && !task.isQuit()) {
                    return;
                }
                processStartTime = new Date();
                totalFilesHandled = totalItemsHandled = 0;
                tableController.markFileHandling(-1);
                updateInterface("Started");
                task = new SingletonTask<Void>(this) {

                    @Override
                    protected boolean handle() {
                        if (!beforeHandleFiles()) {
                            return false;
                        }
                        updateTaskProgress(currentParameters.currentIndex, sourceFiles.size());
                        int len = sourceFiles.size();
                        for (; currentParameters.currentIndex < len; currentParameters.currentIndex++) {
                            if (task == null || isCancelled()) {
                                break;
                            }
                            currentParameters.currentSourceFile = sourceFiles.get(currentParameters.currentIndex);
                            handleCurrentFile();
                            updateTaskProgress(currentParameters.currentIndex + 1, len);
                            if (task == null || isCancelled() || isPreview) {
                                break;
                            }
                        }
                        afterHandleFiles();
                        updateTaskProgress(currentParameters.currentIndex, len);
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        updateInterface("Done");
                        afterSuccessful();
                    }

                    @Override
                    protected void whenCanceled() {
                        updateInterface("Canceled");
                        taskCanceled();
                    }

                    @Override
                    protected void whenFailed() {
                        updateInterface("Failed");
                    }

                    @Override
                    protected void finalAction() {
                        super.finalAction();
                        task = null;
                        tableController.markFileHandling(-1);
                        afterTask();
                    }

                };
                start(task, false, null);
            }

        } catch (Exception e) {
            updateInterface("Failed");
            MyBoxLog.error(e.toString());
        }
    }

    public boolean beforeHandleFiles() {
        return true;
    }

    public void afterHandleFiles() {
    }

    public void afterSuccessful() {

    }

    public void updateTaskProgress(long number, long total) {
        Platform.runLater(() -> {
            double p = (number * 1d) / total;
            String s = number + "/" + total;
            if (fileProgressBar != null) {
                fileProgressBar.setProgress(p);
                fileProgressValue.setText(s);
            } else if (progressBar != null) {
                progressBar.setProgress(p);
                progressValue.setText(s);
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
            countHandling(currentParameters.currentSourceFile);
            if (!currentParameters.currentSourceFile.exists()) {
                result = message("NotFound");
            } else if (currentParameters.currentSourceFile.isFile()) {
                result = handleFile(currentParameters.currentSourceFile);
            } else if (currentParameters.currentSourceFile.isDirectory()) {
                result = handleDirectory(currentParameters.currentSourceFile);
            } else {
                result = message("Invalid");
            }
            if (!message("Successful").equals(result)) {
                showLogs(result);
            }
            tableController.markFileHandled(currentParameters.currentIndex, result);
        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    public String handleFile(File file) {
        try {
            if (task == null || task.isCancelled()) {
                return message("Canceled");
            }
            if (file == null || !file.isFile() || !match(file)) {
                return message("Skip" + ": " + file);
            }
            if (currentParameters.targetPath != null) {
                return handleFileWithName(file, currentParameters.targetPath);
            } else {
                return handleFileWithName(file, null);
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return file + " " + e.toString();
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
        String suffix = FileNameTools.suffix(fname);
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

            case NameMatchRegularExpression:
                for (String name : sourceFilesSelector) {
                    if (StringTools.match(fname, name, false)) {
                        return true;
                    }
                }
                return false;

            case NameNotMatchRegularExpression:
                for (String name : sourceFilesSelector) {
                    if (StringTools.match(fname, name, false)) {
                        return false;
                    }
                }
                return true;

            case NameIncludeRegularExpression:
                for (String name : sourceFilesSelector) {
                    if (StringTools.include(fname, name, false)) {
                        return true;
                    }
                }
                return false;

            case NameNotIncludeRegularExpression:
                for (String name : sourceFilesSelector) {
                    if (StringTools.include(fname, name, false)) {
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
                if (createDirectories
                        && !FileTools.isEqualOrSubPath(currentParameters.targetPath, dir.getAbsolutePath())) {
                    targetDir = new File(currentParameters.targetPath + File.separator + dir.getName());
                } else {
                    targetDir = new File(currentParameters.targetPath);
                }
                targetDir.mkdirs();
                handleDirectory(dir, targetDir.getAbsolutePath());
            } else {
                handleDirectory(dir, null);
            }
            return MessageFormat.format(message("DirHandledSummary"), dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            showLogs(e.toString());
            return message("Failed");
        }
    }

    protected boolean handleDirectory(File sourcePath, String targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()
                || (isPreview && dirFilesHandled > 0)) {
            return false;
        }
        try {
            File[] files = sourcePath.listFiles();
            if (files == null) {
                return false;
            }
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
                    logSourceFile(srcFile);
                    String result = handleFileWithName(srcFile, targetPath);
                    if (!message("Failed").equals(result)
                            && !message("Skip").equals(result)) {
                        dirFilesHandled++;
                    }
                } else if (srcFile.isDirectory() && sourceCheckSubdir) {
                    if (targetPath != null) {
                        if (FileTools.isEqualOrSubPath(targetPath, srcFile.getAbsolutePath())) {
                            continue;
                        }
                        String subPathName = makeTargetFilename(srcFile, targetPath);
                        if (!checkDirectory(srcFile, subPathName)) {
                            return false;
                        }
                        handleDirectory(srcFile, subPathName);
                    } else {
                        handleDirectory(srcFile, targetPath);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean checkDirectory(File srcFile, String pathname) {
        try {
            if (pathname == null) {
                return false;
            }
            File path = new File(pathname);
            if (!path.exists()) {
                path.mkdirs();
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @FXML
    public void pauseProcess(ActionEvent event) {
        paused = true;
        if (task != null && !task.isQuit()) {
            task.cancel();
            task = null;
        } else {
            updateInterface("Paused");
        }
    }

    public void cancelProcess(ActionEvent event) {
        paused = false;
        if (task != null && !task.isQuit()) {
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
        if (tableController != null) {
            tableController.thisPane.setDisable(disable);
        }
        if (optionsVBox != null) {
            optionsVBox.setDisable(disable);
        }
        if (targetVBox != null) {
            targetVBox.setDisable(disable);
        }
        if (!isPreview && tabPane != null && logsTab != null) {
            tabPane.getSelectionModel().select(logsTab);
        }
    }

    public void countHandling(File file) {
        if (file == null) {
            return;
        }
        countHandling(file.getAbsolutePath());
    }

    public void countHandling(String name) {
        if (name == null) {
            return;
        }
        String msg = MessageFormat.format(message("HandlingObject"), name);
        try {
            File f = new File(name);
            if (f.isFile()) {
                totalFilesHandled++;
                msg += " " + message("Length") + ": " + FileTools.showFileSize(f.length());
            }
        } catch (Exception e) {
        }
        fileStartTime = new Date();
        updateStatusLabel(msg + "  " + message("StartTime") + ": " + DateTools.datetimeToString(fileStartTime));
        showLogs(msg);
    }

    public void logSourceFile(File srcFile) {
        if (verboseCheck == null || !verboseCheck.isSelected() || srcFile == null) {
            return;
        }
        String msg = message("SourceFile") + ": " + srcFile;
        if (srcFile.isFile()) {
            msg += " " + message("Length") + ": " + FileTools.showFileSize(srcFile.length());
        }
        updateLogs(msg);
    }

    public void showStatus(String info) {
        updateStatusLabel(info);
        updateLogs(info, true, true);
    }

    public void updateStatusLabel(String info) {
        if (statusInput == null || info == null) {
            return;
        }
        Platform.runLater(() -> {
            statusInput.setText(info);
        });
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        File path = targetPath;
        if (targetSubdirCheck != null && targetSubdirCheck.isSelected()) {
            path = new File(targetPath, FileNameTools.prefix(srcFile.getName()));
        }
        return super.makeTargetFile(srcFile, path);
    }

    @Override
    public boolean targetFileGenerated(File target, int type) {
        if (!super.targetFileGenerated(target, type)) {
            return false;
        }
        String msg = message("Cost") + ":" + DateTools.datetimeMsDuration(new Date(), fileStartTime);
        showLogs(msg);
        updateStatusLabel(MessageFormat.format(message("FilesGenerated"), lastTargetName));
        return true;
    }

    public void targetFileGenerated(List<File> tFiles) {
        if (tFiles == null || tFiles.isEmpty()) {
            return;
        }
        lastTargetName = tFiles.get(tFiles.size() - 1).getAbsolutePath();
        if (targetFiles == null) {
            targetFiles = new LinkedHashMap<>();
            targetFilesCount = 0;
        }
        putTargetFile(tFiles, TargetFileType);
        String msg;
        if (tFiles.size() == 1) {
            msg = MessageFormat.format(message("FilesGenerated"), lastTargetName);
        } else {
            msg = MessageFormat.format(message("FilesGenerated"), tFiles.size());
        }
        msg += "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(new Date(), fileStartTime);
        updateStatusLabel(msg);
        updateLogs(msg, true, true);
    }

    public void updateInterface(final String newStatus) {
        currentParameters.status = newStatus;
        Platform.runLater(() -> {
            switch (newStatus) {

                case "Started":
                    StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
                    startButton.applyCss();
                    startButton.setOnAction((ActionEvent event) -> {
                        cancelProcess(event);
                    });
                    if (allowPaused && pauseButton != null) {
                        pauseButton.setVisible(true);
                        pauseButton.setDisable(false);
                        StyleTools.setNameIcon(pauseButton, message("Pause"), "iconPause.png");
                        pauseButton.applyCss();
                        pauseButton.setOnAction((ActionEvent event) -> {
                            pauseProcess(event);
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
                        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
                        startButton.applyCss();
                        startButton.setOnAction((ActionEvent event) -> {
                            cancelProcess(event);
                        });
                        if (pauseButton != null) {
                            pauseButton.setVisible(true);
                            pauseButton.setDisable(false);
                            StyleTools.setNameIcon(pauseButton, message("Start"), "iconStart.png");
                            pauseButton.applyCss();
                            pauseButton.setOnAction((ActionEvent event) -> {
                                startAction();
                            });
                        }
                        disableControls(true);
                    } else {
                        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
                        startButton.applyCss();
                        startButton.setOnAction((ActionEvent event) -> {
                            startAction();
                        });
                        if (pauseButton != null) {
                            pauseButton.setVisible(false);
                            pauseButton.setDisable(true);
                        }
                        disableControls(false);
                    }

            }
        });

    }

    public double countAverageTime(long cost) {
        double avg = 0;
        if (totalFilesHandled != 0) {
            avg = DoubleTools.scale3((double) cost / totalFilesHandled);
        }
        return avg;
    }

    public void showCost() {
        long cost = new Date().getTime() - processStartTime.getTime();
        String s;
        if (paused) {
            s = message("Paused");
        } else {
            s = message(currentParameters.status);
        }
        String space = "   ";
        String avgString = "";
        if (totalFilesHandled > 0) {
            s += ". " + message("HandledFiles") + ":" + totalFilesHandled + space;
            avgString = DateTools.datetimeMsDuration(Math.round(countAverageTime(cost))) + " " + message("PerFile");
        }
        if (totalItemsHandled > 0) {
            s += ". " + message("HandledItems") + ":" + totalItemsHandled + space;
            avgString += " " + DoubleTools.scale3((double) cost / totalItemsHandled) + " " + message("PerItem");
        }
        if (targetFilesCount > 0) {
            popInformation(MessageFormat.format(message("FilesGenerated"), targetFilesCount));
            s += MessageFormat.format(message("FilesGenerated"), targetFilesCount) + space;
        }
        s += message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime) + "." + space
                + message("Average") + ":" + avgString + " "
                + message("StartTime") + ":" + DateTools.datetimeToString(processStartTime) + space
                + message("EndTime") + ":" + DateTools.datetimeToString(new Date());
        if (statusInput != null) {
            statusInput.setText(s);
        }
        updateLogs(s, true, true);
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
