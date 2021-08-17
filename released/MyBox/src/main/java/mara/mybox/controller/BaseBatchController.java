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
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.data.ProcessParameters;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchController<T> extends BaseTaskController {

    protected String targetSubdirKey, previewKey;
    protected ObservableList<T> tableData;
    protected TableView<T> tableView;
    protected List<File> sourceFiles, targetFiles;
    protected List<String> filesPassword;
    protected boolean sourceCheckSubdir, allowPaused, browseTargets, viewTargetPath, createDirectories;
    protected boolean isPreview, paused;
    protected long dirFilesNumber, dirFilesHandled, totalFilesHandled = 0, totalItemsHandled = 0;
    protected long fileSelectorSize, fileSelectorTime;
    protected String[] sourceFilesSelector;
    protected FileSelectorType fileSelectorType;
    protected SimpleBooleanProperty optionsValid;
    protected ProcessParameters actualParameters, previewParameters, currentParameters;
    protected StringBuffer newLogs;
    protected int logsNewlines;
    protected Date processStartTime, fileStartTime;
    protected String finalTargetName;

    @FXML
    protected TabPane batchTabPane;
    @FXML
    protected Tab sourceTab, targetTab;
    @FXML
    protected BaseBatchTableController<T> tableController;
    @FXML
    protected VBox tableBox, optionsVBox, targetVBox;
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

    public BaseBatchController() {
        targetSubdirKey = "targetSubdirKey";
        previewKey = "previewKey";
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
            return Languages.message("Skip");
        }
        targetFileGenerated(target);
        return Languages.message("Successful");
    }

    public void donePost() {
        tableView.refresh();

        if (miaoCheck != null && miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (!isPreview && openCheck != null && !openCheck.isSelected()) {
            return;
        }
        if (targetFiles != null && viewTargetPath) {
            openTarget(null);
        } else if (targetFiles == null || targetFiles.size() == 1) {
            if (finalTargetName == null
                    || !new File(finalTargetName).exists()) {
                alertInformation(Languages.message("NoDataNotSupported"));
            } else {
                viewTarget(new File(finalTargetName));
            }
        } else if (targetFiles.size() > 1) {
            if (browseTargets) {
                browseAction();
            } else {
                openTarget(null);
            }
        } else {
            popInformation(Languages.message("NoFileGenerated"));
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
            } else if (actualParameters != null && finalTargetName != null) {
                path = new File(finalTargetName).getParentFile();
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
                popInformation(Languages.message("NoFileGenerated"));
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
            final ImagesBrowserController controller = ControllerTools.openImagesBrowser(null);
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
                statusLabel = operationBarController.statusLabel;
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

            if (previewInput != null) {
                previewInput.setText(UserConfig.getString(previewKey, "1"));
                ValidationTools.setPositiveValidation(previewInput);
                previewInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        UserConfig.setString(previewKey, newValue);
                    }
                });
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
                if (targetPathInput != null) {
                    if (tableView != null) {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(tableView.getItems())
                                        .or(Bindings.isEmpty(targetPathInput.textProperty()))
                                        .or(targetPathInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                                        .or(optionsValid.not())
                        );
                    } else {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(targetPathInput.textProperty())
                                        .or(targetPathInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                                        .or(optionsValid.not())
                        );
                    }

                } else if (targetFileInput != null) {
                    if (tableView != null) {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(tableView.getItems())
                                        .or(Bindings.isEmpty(targetFileInput.textProperty()))
                                        .or(targetFileInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                                        .or(optionsValid.not())
                        );
                    } else {
                        startButton.disableProperty().bind(
                                Bindings.isEmpty(targetFileInput.textProperty())
                                        .or(targetFileInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
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

            if (miaoCheck != null) {
                miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue,
                            Boolean newValue) {
                        UserConfig.setBoolean("Miao", newValue);

                    }
                });

                miaoCheck.setSelected(UserConfig.getBoolean("Miao"));
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
        if (statusLabel != null) {
            statusLabel.setText("");
        }
        isPreview = false;
        if (!makeActualParameters()) {
            popError(Languages.message("InvalidParameters"));
            actualParameters = null;
            return;
        }
        currentParameters = actualParameters;
        paused = false;
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

        if (targetFileInput != null) {
            finalTargetName = targetFileInput.getText();
            try {
                targetFile = new File(finalTargetName);
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
        targetFiles = new ArrayList<>();

        return makeMoreParameters();

    }

    public boolean makeMoreParameters() {
        if (tableData == null || tableData.isEmpty()) {
            actualParameters = null;
            return false;
        }
        for (int i = 0; i < tableData.size(); ++i) {
            FileInformation d = tableController.fileInformation(i);
            if (d == null) {
                continue;
            }
            d.setHandled("");
            File file = d.getFile();
            if (!isPreview && !sourceFiles.contains(file)) {
                sourceFiles.add(file);
            }
        }
        if (isPreview) {
            int index = 0;
            ObservableList<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (selected != null && !selected.isEmpty()) {
                index = selected.get(0);
            }
            File file = tableController.file(index);
            if (!sourceFiles.contains(file)) {
                sourceFiles.add(file);
            }
        }

        initLogs();
        totalFilesHandled = totalItemsHandled = 0;
        processStartTime = new Date();
        fileStartTime = new Date();
        return true;
    }

    public boolean makePreviewParameters() {
        if (!makeActualParameters()) {
            popError(Languages.message("Invalid"));
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

    @Override
    protected void initLogs() {
        if (logsTextArea != null) {
            logsTextArea.setText("");
            newLogs = new StringBuffer();
            logsNewlines = 0;
            logsTotalLines = 0;
            if (maxLinesinput != null) {
                try {
                    logsMaxLines = Integer.parseInt(maxLinesinput.getText());
                } catch (Exception e) {
                    logsMaxLines = 5000;
                }
            }
        }
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
                updateInterface("Started");
                task = new SingletonTask<Void>() {

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
                    }

                    @Override
                    protected void whenFailed() {
                        updateInterface("Failed");
                    }

                    @Override
                    protected void taskQuit() {
                        super.taskQuit();
                        quitProcess();
                        task = null;
                    }

                };
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
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
                result = Languages.message("NotFound");
            } else if (currentParameters.currentSourceFile.isFile()) {
                result = handleFile(currentParameters.currentSourceFile);
            } else if (currentParameters.currentSourceFile.isDirectory()) {
                result = handleDirectory(currentParameters.currentSourceFile);
            } else {
                result = Languages.message("Invalid");
            }
            if (!Languages.message("Successful").equals(result)) {
                updateLogs(result, true, true);
            }
            tableController.markFileHandled(currentParameters.currentIndex, result);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public String handleFile(File file) {
        try {
            if (task == null || task.isCancelled()) {
                return Languages.message("Canceled");
            }
            if (file == null || !file.isFile() || !match(file)) {
                return Languages.message("Skip" + ": " + file);
            }
            if (currentParameters.targetPath != null) {
                return handleFile(file, new File(currentParameters.targetPath));
            } else {
                return handleFile(file, null);
            }
        } catch (Exception e) {
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
        String suffix = FileNameTools.getFileSuffix(fname);
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
                        && !currentParameters.targetPath.startsWith(dir.getAbsolutePath())) {
                    targetDir = new File(currentParameters.targetPath + File.separator + dir.getName());
                } else {
                    targetDir = new File(currentParameters.targetPath);
                }
                targetDir.mkdirs();
                handleDirectory(dir, targetDir);
            } else {
                handleDirectory(dir, null);
            }
            return MessageFormat.format(Languages.message("DirHandledSummary"), dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return Languages.message("Failed");
        }
    }

    protected boolean handleDirectory(File sourcePath, File targetPath) {
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
                    String result = handleFile(srcFile, targetPath);
                    if (!Languages.message("Failed").equals(result)
                            && !Languages.message("Skip").equals(result)) {
                        dirFilesHandled++;
                    }
                } else if (srcFile.isDirectory() && sourceCheckSubdir) {
                    if (targetPath != null) {
                        if (targetPath.getAbsolutePath().startsWith(srcFile.getAbsolutePath())) {
                            continue;
                        }
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
            MyBoxLog.error(e.toString());
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
        if (!isPreview && batchTabPane != null && logsTab != null) {
            batchTabPane.getSelectionModel().select(logsTab);
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
        try {
            if (new File(name).isFile()) {
                totalFilesHandled++;
            }
        } catch (Exception e) {
        }
        fileStartTime = new Date();
        String msg = MessageFormat.format(Languages.message("HandlingObject"), name);
        updateStatusLabel(msg + "    " + Languages.message("StartTime") + ": " + DateTools.datetimeToString(fileStartTime));
        updateLogs(msg, true, true);
    }

    public void showStatus(String info) {
        updateStatusLabel(info);
        updateLogs(info, true, true);
    }

    public void updateStatusLabel(String info) {
        if (statusLabel == null || info == null) {
            return;
        }
        Platform.runLater(() -> {
            statusLabel.setText(info);
        });
    }

    @Override
    protected void updateLogs(final String line) {
        updateLogs(line, true, false);
    }

    protected void updateLogs(final String line, boolean immediate) {
        updateLogs(line, true, immediate);
    }

    protected void updateLogs(final String line, boolean showTime, boolean immediate) {
        if (logsTextArea == null) {
            return;
        }
        Platform.runLater(() -> {
            synchronized (logsTextArea) {
                try {
                    if (showTime) {
                        newLogs.append(DateTools.datetimeToString(new Date())).append("  ");
                    }
                    newLogs.append(line).append("\n");
                    logsNewlines++;
                    long past = new Date().getTime() - processStartTime.getTime();
                    if (immediate || logsTotalLines % 300 == 0 || past > 5000) {
                        logsTextArea.appendText(newLogs.toString());
                        logsTotalLines += logsNewlines;
                        newLogs = new StringBuffer();
                        logsNewlines = 0;
                        if (logsTotalLines > logsMaxLines + logsCacheLines) {
//                        logsTextArea.deleteText(0, newLogs.length());
                            logsTextArea.clear();
                            logsTotalLines = 0;
                        }

                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
        }
        );
    }

    @FXML
    @Override
    protected void clearLogs() {
        logsTextArea.setText("");
    }

    @Override
    public File makeTargetFile(String fileName, File targetPath) {
        File path = targetPath;
        if (targetSubdirCheck != null && targetSubdirCheck.isSelected()) {
            path = new File(targetPath, FileNameTools.namePrefix(fileName));
        }
        return super.makeTargetFile(fileName, path);
    }

    @Override
    protected boolean targetFileGenerated(File target) {
        if (target == null || !target.exists() || target.length() == 0) {
            return false;
        }
        finalTargetName = target.getAbsolutePath();
        targetFiles.add(target);
        String msg;
        msg = MessageFormat.format(Languages.message("FilesGenerated"), finalTargetName);
        msg += "  " + Languages.message("Cost") + ":" + DateTools.datetimeMsDuration(new Date(), fileStartTime);
        updateStatusLabel(msg);
        updateLogs(msg, true, true);
        recordFileWritten(target);
        return true;
    }

    public void targetFileGenerated(List<File> tFiles) {
        if (tFiles == null || tFiles.isEmpty()) {
            return;
        }
        finalTargetName = tFiles.get(0).getAbsolutePath();
        targetFiles.addAll(tFiles);
        String msg;
        if (tFiles.size() == 1) {
            msg = MessageFormat.format(Languages.message("FilesGenerated"), finalTargetName);
        } else {
            msg = MessageFormat.format(Languages.message("FilesGenerated"), tFiles.size());
        }
        msg += "  " + Languages.message("Cost") + ":" + DateTools.datetimeMsDuration(new Date(), fileStartTime);
        updateStatusLabel(msg);
        updateLogs(msg, true, true);
    }

    public void updateInterface(final String newStatus) {
        currentParameters.status = newStatus;
        Platform.runLater(() -> {
            switch (newStatus) {

                case "Started":
                    StyleTools.setNameIcon(startButton, Languages.message("Stop"), "iconStop.png");
                    startButton.applyCss();
                    startButton.setOnAction((ActionEvent event) -> {
                        cancelProcess(event);
                    });
                    if (allowPaused) {
                        pauseButton.setVisible(true);
                        pauseButton.setDisable(false);
                        StyleTools.setNameIcon(pauseButton, Languages.message("Pause"), "iconPause.png");
                        startButton.applyCss();
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
                        StyleTools.setNameIcon(startButton, Languages.message("Stop"), "iconStop.png");
                        startButton.applyCss();
                        startButton.setOnAction((ActionEvent event) -> {
                            cancelProcess(event);
                        });
                        pauseButton.setVisible(true);
                        pauseButton.setDisable(false);
                        StyleTools.setNameIcon(pauseButton, Languages.message("Start"), "iconStart.png");
                        pauseButton.applyCss();
                        pauseButton.setOnAction((ActionEvent event) -> {
                            startAction();
                        });
                        disableControls(true);
                    } else {
                        StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
                        startButton.applyCss();
                        startButton.setOnAction((ActionEvent event) -> {
                            startAction();
                        });
                        pauseButton.setVisible(false);
                        pauseButton.setDisable(true);
                        disableControls(false);
                        donePost();
                    }
                    showCost();

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
            s = Languages.message("Paused");
        } else {
            s = Languages.message(currentParameters.status);
        }
        String space = "   ";
        String avgString = "";
        if (totalFilesHandled > 0) {
            s += ". " + Languages.message("HandledFiles") + ":" + totalFilesHandled + space;
            avgString = DateTools.datetimeMsDuration(Math.round(countAverageTime(cost))) + " " + Languages.message("PerFile");
        }
        if (totalItemsHandled > 0) {
            s += ". " + Languages.message("HandledItems") + ":" + totalItemsHandled + space;
            avgString += " " + DoubleTools.scale3((double) cost / totalItemsHandled) + " " + Languages.message("PerItem");
        }
        int count = 0;
        if (targetFiles != null && !targetFiles.isEmpty()) {
            count = targetFiles.size();
            popInformation(MessageFormat.format(Languages.message("FilesGenerated"), count));
        }
        if (count > 0) {
            s += MessageFormat.format(Languages.message("FilesGenerated"), count) + space;
        }
        s += Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime) + "." + space
                + Languages.message("Average") + ":" + avgString + " "
                + Languages.message("StartTime") + ":" + DateTools.datetimeToString(processStartTime) + space
                + Languages.message("EndTime") + ":" + DateTools.datetimeToString(new Date());
        if (statusLabel != null) {
            statusLabel.setText(s);
        }
        updateLogs(s, true, true);
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
