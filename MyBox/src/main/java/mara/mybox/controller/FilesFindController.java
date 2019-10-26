package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.TableFileSizeCell;
import mara.mybox.fxml.TableTimeCell;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-9
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesFindController extends FilesBatchController {

    protected ObservableList<FileInformation> filesList = FXCollections.observableArrayList();
    protected long totalChecked, totalMatched;

    @FXML
    protected TableView<FileInformation> filesView;
    @FXML
    protected TableColumn<FileInformation, String> fileColumn;
    @FXML
    protected TableColumn<FileInformation, Long> sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    private ToggleGroup filterGroup;
    @FXML
    protected TextField filterInput;
    @FXML
    protected VBox conditionsBox, logsBox;
    @FXML
    private FlowPane filterTypesPane;

    public FilesFindController() {
        baseTitle = AppVariables.message("FilesFind");
        allowPaused = false;
    }

    @Override
    public void initializeNext() {
        try {
            initConditionTab();
            initFilesTab();

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(sourcePathInput.styleProperty().isEqualTo(badStyle))
            );

            operationBarController.openTargetButton.disableProperty().bind(
                    startButton.disableProperty()
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initFilesTab() {
        try {
            filesList = FXCollections.observableArrayList();
            filesView.setItems(filesList);

            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            fileColumn.setPrefWidth(400);

            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
            sizeColumn.setCellFactory(new TableFileSizeCell());

            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifyTimeColumn.setCellFactory(new TableTimeCell());

            createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            createTimeColumn.setCellFactory(new TableTimeCell());

            filesView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        int index = filesView.getSelectionModel().getSelectedIndex();
                        if (index < 0 || index > filesList.size() - 1) {
                            return;
                        }
                        FileInformation info = filesList.get(index);
                        view(info.getFile());
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void initConditionTab() {

        filterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFilterType();
            }
        });
        checkFilterType();

        filterInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                if (newv == null || newv.trim().isEmpty()) {
                    filterInput.setStyle(null);
                    fileSelectorSize = -1;
                    fileSelectorTime = -1;
                    return;
                }
                if (fileSelectorType == FileSelectorType.FileSizeLargerThan
                        || fileSelectorType == FileSelectorType.FileSizeSmallerThan) {
                    long v = ByteTools.checkBytesValue(newv);
                    if (v >= 0) {
                        fileSelectorSize = v;
                        filterInput.setStyle(null);
                    } else {
                        filterInput.setStyle(badStyle);
                        popError(message("FileSizeComments"));
                    }

                } else if (fileSelectorType == FileSelectorType.ModifiedTimeEarlierThan
                        || fileSelectorType == FileSelectorType.ModifiedTimeLaterThan) {
                    Date d = DateTools.stringToDatetime(newv);
                    if (d != null) {
                        fileSelectorTime = d.getTime();
                    } else {
                        fileSelectorTime = -1;
                    }

                }
            }
        });

    }

    protected void checkFilterType() {

        String selected = ((RadioButton) filterGroup.getSelectedToggle()).getText();
        for (FileSelectorType type : FileSelectorType.values()) {
            if (message(type.name()).equals(selected)) {
                fileSelectorType = type;
                break;
            }
        }
        if (regexLink != null) {
            regexLink.setVisible(
                    fileSelectorType == FileSelectorType.NameMatchAnyRegularExpression
                    || fileSelectorType == FileSelectorType.NameNotMatchAnyRegularExpression
            );
        }

        filterInput.setText("");
        switch (fileSelectorType) {
            case FileSizeLargerThan:
            case FileSizeSmallerThan:
                filterInput.setPromptText(message("FileSizeComments"));
                FxmlControl.setTooltip(filterInput, new Tooltip(message("FileSizeComments")));
                break;
            case ModifiedTimeEarlierThan:
            case ModifiedTimeLaterThan:
                filterInput.setText("2019-10-24 10:10:10");
                FxmlControl.setTooltip(filterInput, new Tooltip("2019-10-24 10:10:10"));
                break;
            default:
                filterInput.setPromptText(message("SeparateBySpace"));
                FxmlControl.setTooltip(filterInput, new Tooltip(message("SeparateBySpace")));
                break;
        }

    }

    @Override
    public void checkSourcetPathInput() {
        filesList.clear();
        filesView.refresh();
        logsTextArea.clear();

        super.checkSourcetPathInput();
    }

    @FXML
    @Override
    public void startAction() {
        try {
            filesList.clear();
            filesView.refresh();
            logsTextArea.clear();

            sourcePath = new File(sourcePathInput.getText());
            if (!sourcePath.isDirectory()) {
                return;
            }
            initLogs();
            logsTextArea.setText(AppVariables.message("SourcePath") + ": " + sourcePath.getAbsolutePath() + "\n");

            startTime = new Date();
            totalChecked = totalMatched = 0;
            sourceFilesSelector = filterInput.getText().trim().split("\\s+");

            updateInterface("Started");
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        checkDirectory(sourcePath);
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        filesView.refresh();
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
            }
        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }

    }

    protected void checkDirectory(File directory) {
        try {
            if (directory == null || !directory.isDirectory()) {
                return;
            }
            updateLogs(message("HandlingDirectory") + " " + directory);
            File[] files = directory.listFiles();
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                if (srcFile.isFile()) {
                    totalChecked++;
                    if (!match(srcFile)) {
                        continue;
                    }
                    totalMatched++;
                    filesList.add(new FileInformation(srcFile));
                } else if (srcFile.isDirectory()) {
                    checkDirectory(srcFile);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void updateInterface(final String newStatus) {
        currentStatus = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    updateLogs(AppVariables.message(newStatus), true);
                    switch (newStatus) {
                        case "Started":
                            operationBarController.getStatusLabel().setText(message("Handling...") + " "
                                    + message("StartTime")
                                    + ": " + DateTools.datetimeToString(startTime));
                            startButton.setText(AppVariables.message("Cancel"));
                            startButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    cancelProcess(event);
                                }
                            });
                            operationBarController.pauseButton.setVisible(true);
                            operationBarController.pauseButton.setDisable(false);
                            operationBarController.pauseButton.setText(AppVariables.message("Pause"));
                            operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pauseProcess(event);
                                }
                            });
                            operationBarController.progressBar.setProgress(-1);
                            conditionsBox.setDisable(true);
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
                                operationBarController.pauseButton.setVisible(true);
                                operationBarController.pauseButton.setDisable(false);
                                operationBarController.pauseButton.setText(AppVariables.message("Continue"));
                                operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startAction();
                                    }
                                });

                            } else {
                                startButton.setText(AppVariables.message("Start"));
                                startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startAction();
                                    }
                                });
                                operationBarController.pauseButton.setVisible(false);
                                operationBarController.pauseButton.setDisable(true);
                                operationBarController.progressBar.setProgress(1);
                                conditionsBox.setDisable(false);
                            }
                            donePost();
                    }

                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        });

    }

    @Override
    public void showCost() {
        if (operationBarController.getStatusLabel() == null) {
            return;
        }
        long cost = new Date().getTime() - startTime.getTime();
        String s;
        if (paused) {
            s = message("Paused");
        } else {
            s = message(currentStatus);
        }
        s += ".  "
                + message("TotalCheckedFiles") + ": " + totalChecked + "   "
                + message("TotalMatched") + ": " + totalMatched + ".   "
                + message("Cost") + ": " + DateTools.showTime(cost) + ". "
                + message("StartTime") + ": " + DateTools.datetimeToString(startTime) + ", "
                + message("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.getStatusLabel().setText(s);
    }

    @Override
    public void donePost() {
        showCost();
        updateLogs(message("StartTime") + ": " + DateTools.datetimeToString(startTime) + "   "
                + AppVariables.message("Cost") + ": " + DateTools.showTime(new Date().getTime() - startTime.getTime()), false, true);
        updateLogs(message("TotalCheckedFiles") + ": " + totalChecked + "   "
                + message("TotalMatched") + ": " + totalMatched);

        if (operationBarController.miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {
            browseURI(new File(targetPathInput.getText()).toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
