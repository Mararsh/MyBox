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
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesBatchController extends BaseController {

    protected TargetExistType targetExistType;
    protected int dirTotal, dirOk;
    protected List<Integer> sourcesHandling;

    protected class ProcessParameters {

        protected File sourceFile;
        protected int startIndex, currentIndex, currentTotalHandled;
        protected String status, targetPath, targetPrefix, targetRootPath, finalTargetName;
        protected Date startTime, endTime;
        protected boolean createSubDir;
    }

    protected ProcessParameters actualParameters, previewParameters, currentParameters;

    @FXML
    protected TableView<FileInformation> sourceTableView;
    @FXML
    protected TableColumn<FileInformation, String> handledColumn, fileColumn, modifyTimeColumn,
            sizeColumn, createTimeColumn;
    @FXML
    protected TableColumn<FileInformation, Boolean> typeColumn;
    @FXML
    protected ToggleGroup targetExistGroup, fileTypeGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected TextField targetSuffixInput;
    @FXML
    protected Button addFilesButton, addDirectoryButton, insertFilesButton, insertDirectoryButton,
            upButton, downButton, clearButton, openButton;
    @FXML
    protected CheckBox subDirCheck, filesNameCheck;
    @FXML
    protected TextField filesNameInput;

    protected static enum TargetExistType {
        Rename, Replace, Skip
    }

    public FilesBatchController() {
        sourcePathKey = "sourcePath";
        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*", "*.*"));
            }
        };
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

    protected void viewFile(File file) {
        if (file == null) {
            return;
        }
        if (file.isFile()) {
            TextEditerController controller = (TextEditerController) openStage(CommonValues.TextEditerFxml,
                    AppVaribles.getMessage("TextEditer"), false, true);
            controller.openFile(file);
        } else {
            try {
                Desktop.getDesktop().browse(file.toURI());
            } catch (Exception e) {
                logger.debug(e.toString());
            }
        }
    }

    protected String handleCurrentFile(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    protected String handleCurrentDirectory(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    protected void donePost() {

    }

    /* ----------------------------------------------------- */
    @Override
    protected void initializeNext() {
        try {
            initSourceSection();
            initOptionsSection();
            initTargetSection();

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initSourceSection() {
        try {
            sourceFilesInformation = FXCollections.observableArrayList();

            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));
            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, Boolean>("isFile"));
                typeColumn.setCellFactory(new Callback<TableColumn<FileInformation, Boolean>, TableCell<FileInformation, Boolean>>() {
                    @Override
                    public TableCell<FileInformation, Boolean> call(TableColumn<FileInformation, Boolean> param) {
                        TableCell<FileInformation, Boolean> cell = new TableCell<FileInformation, Boolean>() {
                            final Text text = new Text();

                            @Override
                            protected void updateItem(final Boolean item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    if (item) {
                                        text.setText(AppVaribles.getMessage("File"));
                                    } else {
                                        text.setText(AppVaribles.getMessage("Directory"));
                                    }
                                }
                                setGraphic(text);
                            }
                        };
                        return cell;
                    }
                });
            }
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("modifyTime"));
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("createTime"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileSize"));

            sourceTableView.setItems(sourceFilesInformation);
            sourceTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        openAction();
                    }
                }
            });
            sourceTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
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
        ObservableList<Integer> selected = sourceTableView.getSelectionModel().getSelectedIndices();
        boolean none = (selected == null || selected.isEmpty());
        insertFilesButton.setDisable(none);
        if (insertDirectoryButton != null) {
            insertDirectoryButton.setDisable(none);
        }
        if (openButton != null) {
            openButton.setDisable(none);
        }
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
        targetPathInput.setText(AppVaribles.getUserConfigPath(targetPathKey).getAbsolutePath());

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
    protected void addFilesAction(ActionEvent event) {
        addFilesAction(sourceFilesInformation.size());
    }

    @FXML
    protected void addDirectoryAction(ActionEvent event) {
        addDirectoryAction(sourceFilesInformation.size());
    }

    @FXML
    protected void insertFilesAction(ActionEvent event) {
        int index = sourceTableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFilesAction(index);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    protected void insertDirectoryAction(ActionEvent event) {
        if (insertDirectoryButton == null) {
            return;
        }
        int index = sourceTableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectoryAction(index);
        } else {
            insertDirectoryButton.setDisable(true);
        }
    }

    protected void addFilesAction(int index) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
            if (defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
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
            sourceTableView.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void addDirectoryAction(int index) {
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
            if (defaultPath != null) {
                dirChooser.setInitialDirectory(defaultPath);
            }
            File directory = dirChooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setUserConfigValue(sourcePathKey, directory.getPath());

            FileInformation d = new FileInformation(directory);
            if (index < 0 || index >= sourceFilesInformation.size()) {
                sourceFilesInformation.add(d);
            } else {
                sourceFilesInformation.add(index, d);
            }
            sourceTableView.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void deleteAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTableView.getSelectionModel().getSelectedIndices());
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
        sourceTableView.refresh();
    }

    @FXML
    protected void clearAction(ActionEvent event) {
        sourceFilesInformation.clear();
        sourceTableView.refresh();
    }

    @FXML
    protected void openAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            if (info.getNewName() != null && !info.getNewName().isEmpty()) {
                viewFile(new File(info.getNewName()));
            } else {
                viewFile(info.getFile());
            }
        }
    }

    @FXML
    protected void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTableView.getSelectionModel().getSelectedIndices());
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
                sourceTableView.getSelectionModel().select(index - 1);
            }
        }
        sourceTableView.refresh();
    }

    @FXML
    protected void downAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTableView.getSelectionModel().getSelectedIndices());
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
                sourceTableView.getSelectionModel().select(index + 1);
            }
        }
        sourceTableView.refresh();
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
    public void startAction() {
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
        sourcesHandling = new ArrayList();
        if (sourceFilesInformation != null && !sourceFilesInformation.isEmpty()) {
            if (isPreview) {
                int index = 0;
                ObservableList<Integer> selected = sourceTableView.getSelectionModel().getSelectedIndices();
                if (selected != null && !selected.isEmpty()) {
                    index = selected.get(0);
                }
                sourcesHandling.add(index);
            } else {
                for (int i = 0; i < sourceFilesInformation.size(); i++) {
                    sourcesHandling.add(i);
                }
            }
        }

        if (targetPathInput != null) {
            actualParameters.targetRootPath = targetPathInput.getText();
            actualParameters.targetPath = actualParameters.targetRootPath;
        }
        if (targetPrefixInput != null) {
            actualParameters.targetPrefix = targetFileInput.getText();
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
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        for (; currentParameters.currentIndex < sourcesHandling.size();) {
                            if (isCancelled()) {
                                break;
                            }
                            handleCurrentIndex();

                            currentParameters.currentIndex++;
                            updateProgress(currentParameters.currentIndex, sourcesHandling.size());
                            updateMessage(currentParameters.currentIndex + "/" + sourcesHandling.size());

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

    protected void handleCurrentIndex() {
        FileInformation d = sourceFilesInformation.get(sourcesHandling.get(currentParameters.currentIndex));
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
        sourceTableView.refresh();
        currentParameters.currentTotalHandled++;
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
                        operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourcesHandling.size());
                        operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesHandling.size());
                    }
                    switch (newStatus) {
                        case "StartFile":
                            operationBarController.statusLabel.setText(currentParameters.sourceFile.getName() + " "
                                    + getMessage("Handling...") + " "
                                    + getMessage("StartTime")
                                    + ": " + DateTools.datetimeToString(currentParameters.startTime));
                            if (operationBarController.fileProgressBar != null) {
                                operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourcesHandling.size());
                                operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesHandling.size());
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
                                operationBarController.fileProgressBar.setProgress(currentParameters.currentIndex / sourcesHandling.size());
                                operationBarController.fileProgressValue.setText(currentParameters.currentIndex + " / " + sourcesHandling.size());
                            }
                            break;

                        case "Done":
                            if (isPreview) {
                                if (currentParameters.finalTargetName == null
                                        || !new File(currentParameters.finalTargetName).exists()) {
                                    alertInformation(AppVaribles.getMessage("NoDataNotSupported"));
                                } else {
                                    viewFile(new File(currentParameters.finalTargetName));
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
