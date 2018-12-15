package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FileBatchController extends BaseController {

    protected TargetExistType targetExistType;
    protected ObservableList<String> generatedFiles;
    protected String fileType, errorString, targetFormat;

    protected class ProcessParameters {

        protected File sourceFile;
        protected int startIndex, currentIndex, currentTotalHandled;
        protected String status, targetPath, targetPrefix, targetRootPath, finalTargetName;
        protected Date startTime, endTime;
        protected boolean createSubDir;
    }

    protected ProcessParameters actualParameters, previewParameters, currentParameters;

    @FXML
    protected TableView<FileInformation> sourceTable;
    @FXML
    protected TableColumn<FileInformation, String> handledColumn, fileColumn, modifyTimeColumn, sizeColumn, createTimeColumn;
    @FXML
    protected ToggleGroup targetExistGroup, fileTypeGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected TextField targetSuffixInput;
    @FXML
    protected Button addButton, upButton, downButton, deleteButton, clearButton, openButton, insertButton;

    protected static enum TargetExistType {
        Rename, Replace, Skip
    }

    public FileBatchController() {

    }

    /**
     * Methods to be implemented
     */
    protected void initializeNext2() {
        try {

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initOptionsSection() {

    }

    protected void viewFile(String file) {
        try {

        } catch (Exception e) {
        }
    }

    protected String handleCurrentFile() {
        try {
            // handle and generate file
            generatedFiles.add(currentParameters.finalTargetName);
            return AppVaribles.getMessage("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVaribles.getMessage("Failed");
        }
    }


    /* ----------------------------------------------------- */
    @Override
    protected void initializeNext() {

        generatedFiles = FXCollections.observableArrayList();

        initSourceSection();
        initOptionsSection();
        initTargetSection();

        initializeNext2();

    }

    protected void initSourceSection() {
        try {
            sourceFilesInformation = FXCollections.observableArrayList();

            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("modifyTime"));
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("createTime"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileSize"));

            sourceTable.setItems(sourceFilesInformation);
            sourceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        openAction();
                    }
                }
            });
            sourceTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });
            checkTableSelected();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkTableSelected() {
        ObservableList<Integer> selected = sourceTable.getSelectionModel().getSelectedIndices();
        boolean none = (selected == null || selected.isEmpty());
        insertButton.setDisable(none);
        openButton.setDisable(none);
        upButton.setDisable(none);
        downButton.setDisable(none);
        deleteButton.setDisable(none);
    }

    protected void initTargetSection() {
        targetPathInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    final File file = new File(newValue);
                    if (!file.exists() || !file.isDirectory()) {
                        targetPathInput.setStyle(badStyle);
                        return;
                    }
                    targetPathInput.setStyle(null);
                    AppVaribles.setUserConfigValue(targetPathKey, file.getPath());
                } catch (Exception e) {
                }
            }
        });
        targetPathInput.setText(AppVaribles.getUserConfigValue(targetPathKey, CommonValues.UserFilePath));

        targetExistGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkTargetExistType();
            }
        });
        checkTargetExistType();

        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
        );

        operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceFilesInformation))
        );

        Tooltip tips = new Tooltip(getMessage("PreviewComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(previewButton, tips);

        previewButton.disableProperty().bind(
                operationBarController.startButton.disableProperty()
                        .or(operationBarController.startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
        );

    }

    protected void checkTargetExistType() {
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
    protected void addAction(ActionEvent event) {
        addAction(sourceFilesInformation.size());
    }

    @FXML
    protected void insertAction(ActionEvent event) {
        int index = sourceTable.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addAction(index);
        } else {
            insertButton.setDisable(true);
        }
    }

    protected void addAction(int index) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getUserConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            List<FileInformation> infos = new ArrayList<>();
            for (File file : files) {
                FileInformation info = new FileInformation(file);
                infos.add(info);
            }
            if (infos.isEmpty()) {
                return;
            }
            if (index < 0 || index >= sourceFilesInformation.size()) {
                sourceFilesInformation.addAll(infos);
            } else {
                sourceFilesInformation.addAll(index, infos);
            }
            sourceTable.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    protected void deleteAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }
            sourceFilesInformation.remove(index);
        }
        sourceTable.refresh();
    }

    @FXML
    protected void clearAction(ActionEvent event) {
        sourceFilesInformation.clear();
        sourceTable.refresh();
    }

    @FXML
    protected void openAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            viewFile(info.getFile().getAbsolutePath());
        }
    }

    @FXML
    protected void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index == 0) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            sourceFilesInformation.set(index, sourceFilesInformation.get(index - 1));
            sourceFilesInformation.set(index - 1, info);
        }
        for (Integer index : selected) {
            if (index > 0) {
                sourceTable.getSelectionModel().select(index - 1);
            }
        }
        sourceTable.refresh();
    }

    @FXML
    protected void downAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == sourceFilesInformation.size() - 1) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            sourceFilesInformation.set(index, sourceFilesInformation.get(index + 1));
            sourceFilesInformation.set(index + 1, info);
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < sourceFilesInformation.size() - 1) {
                sourceTable.getSelectionModel().select(index + 1);
            }
        }
        sourceTable.refresh();
    }

    @Override
    protected void openTarget(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new File(targetPathInput.getText()).toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void startProcess(ActionEvent event) {
        isPreview = false;
        makeActualParameters();
        currentParameters = actualParameters;
        paused = false;
        doCurrentProcess();
    }

    @FXML
    protected void preview(ActionEvent event) {
        isPreview = true;
        makeActualParameters();
        previewParameters = copyParameters(actualParameters);
        previewParameters.status = "start";
        currentParameters = previewParameters;
        doCurrentProcess();
    }

    protected void makeActualParameters() {
        if (actualParameters != null && paused) {
            actualParameters.startIndex = actualParameters.currentIndex;
            return;
        }
        actualParameters = new ProcessParameters();
        actualParameters.currentIndex = 0;

        actualParameters.targetRootPath = targetPathInput.getText();
        actualParameters.targetPath = actualParameters.targetRootPath;
        if (subdirCheck != null) {
            actualParameters.createSubDir = subdirCheck.isSelected();
            AppVaribles.setUserConfigValue(creatSubdirKey, actualParameters.createSubDir);
        }

        if (targetPrefixInput != null) {
            actualParameters.targetPrefix = targetFileInput.getText();
        }

        if (sourceFilesInformation != null && !sourceFilesInformation.isEmpty()) {
            sourceFiles = new ArrayList();
            if (isPreview) {
                sourceFiles.add(sourceFilesInformation.get(0).getFile());
            } else {
                for (FileInformation f : sourceFilesInformation) {
                    sourceFiles.add(f.getFile());
                }
            }
        }

    }

    protected ProcessParameters copyParameters(ProcessParameters theConversion) {
        ProcessParameters newConversion = new ProcessParameters();
        newConversion.currentTotalHandled = theConversion.currentTotalHandled;
        newConversion.sourceFile = theConversion.sourceFile;
        newConversion.targetRootPath = theConversion.targetRootPath;
        newConversion.targetPath = theConversion.targetPath;
        newConversion.targetPrefix = theConversion.targetPrefix;
        newConversion.createSubDir = theConversion.createSubDir;
        newConversion.status = theConversion.status;
        newConversion.startTime = theConversion.startTime;
        return newConversion;
    }

    protected void doCurrentProcess() {
        try {
            generatedFiles = FXCollections.observableArrayList();
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        for (; currentParameters.currentIndex < sourceFiles.size();) {
                            if (isCancelled()) {
                                break;
                            }
                            handleCurrentIndex();

                            currentParameters.currentIndex++;
                            updateProgress(currentParameters.currentIndex, sourceFiles.size());
                            updateMessage(currentParameters.currentIndex + "/" + sourceFiles.size());

                            if (isCancelled() || isPreview) {
                                break;
                            }

                        }

                    } catch (Exception e) {
                        logger.error(e.toString());
                    }

                    if (!isPreview) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                openTarget(null);
                            }
                        });
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

    protected void handleCurrentIndex() {
        File file = sourceFiles.get(currentParameters.currentIndex);
        currentParameters.sourceFile = file;
        String filename = file.getName();
        if (fileType != null) {
            filename = FileTools.replaceFileSuffix(filename, fileType);
        }
        currentParameters.finalTargetName = currentParameters.targetPath
                + File.separator + filename;
        boolean skip = false;
        if (targetExistType == TargetExistType.Rename) {
            while (new File(currentParameters.finalTargetName).exists()) {
                filename = FileTools.getFilePrefix(filename)
                        + targetSuffixInput.getText().trim() + "." + FileTools.getFileSuffix(filename);
                currentParameters.finalTargetName = currentParameters.targetPath
                        + File.separator + filename;
            }
        } else if (targetExistType == TargetExistType.Skip) {
            if (new File(currentParameters.finalTargetName).exists()) {
                skip = true;
            }
        }
        if (!skip) {
            String result = handleCurrentFile();
            markFileHandled(currentParameters.currentIndex, result);
        } else {
            markFileHandled(currentParameters.currentIndex, AppVaribles.getMessage("Skip"));
        }

        currentParameters.currentTotalHandled++;

    }

    protected void markFileHandled(int index, String msg) {
        FileInformation d = sourceFilesInformation.get(index);
        if (d == null) {
            return;
        }
        d.setHandled(msg);
        sourceTable.refresh();
    }

    @Override
    protected void updateInterface(final String newStatus) {
        currentParameters.status = newStatus;
        if (operationBarController == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (operationBarController.fileProgressBar != null) {
                        operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourceFiles.size());
                        operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourceFiles.size());
                    }
                    switch (newStatus) {
                        case "StartFile":
                            operationBarController.statusLabel.setText(currentParameters.sourceFile.getName() + " "
                                    + getMessage("Handling...") + " "
                                    + getMessage("StartTime")
                                    + ": " + DateTools.datetimeToString(currentParameters.startTime));
                            if (operationBarController.fileProgressBar != null) {
                                operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourceFiles.size());
                                operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourceFiles.size());
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
                            operationBarController.pauseButton.setVisible(true);
                            operationBarController.pauseButton.setDisable(false);
                            operationBarController.pauseButton.setText(AppVaribles.getMessage("Pause"));
                            operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pauseProcess(event);
                                }
                            });
                            paraBox.setDisable(true);
                            break;

                        case "CompleteFile":
                            showCost();
                            if (operationBarController.fileProgressBar != null) {
                                operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourceFiles.size());
                                operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourceFiles.size());
                            }
                            break;

                        case "Done":
                            if (isPreview) {
                                if (currentParameters.finalTargetName == null
                                        || !new File(currentParameters.finalTargetName).exists()) {
                                    alertInformation(AppVaribles.getMessage("NoDataNotSupported"));
                                } else {
                                    viewFile(currentParameters.finalTargetName);
                                }
                            }

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
                                paraBox.setDisable(true);
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
                                paraBox.setDisable(false);
                            }
                            showCost();

                            if (operationBarController.miaoCheck.isSelected()) {
                                FxmlTools.miao3();
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
        long cost = (new Date().getTime() - currentParameters.startTime.getTime()) / 1000;
        double avg = 0;
        if (currentParameters.currentTotalHandled != 0) {
            avg = ValueTools.roundDouble3((double) cost / currentParameters.currentTotalHandled);
        }
        String s;
        if (paused) {
            s = getMessage("Paused");
        } else {
            s = getMessage(currentParameters.status);
        }
        s += ". " + getMessage("HandledThisTime") + ": " + currentParameters.currentTotalHandled + " "
                + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(currentParameters.startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

}
