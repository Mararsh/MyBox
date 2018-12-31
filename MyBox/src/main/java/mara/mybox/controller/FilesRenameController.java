package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.FileInformation;

import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTools.FileSortType;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesRenameController extends BaseController {

    protected List<String> targetNames;
    protected Date startTime;
    protected int currentIndex, currentAccum, currentTotalHandled;
    protected int total, success, fail, digit;
    protected FilesTableController tableController;

    @FXML
    protected CheckBox originalCheck, stringCheck, accumCheck, suffixCheck, descentCheck;
    @FXML
    protected TextField stringInput, suffixInput;
    @FXML
    protected ToggleGroup sortGroup;

    public FilesRenameController() {
        targetPathKey = "FileTargetPath";
        creatSubdirKey = "FileCreatSubdir";
        fillZeroKey = "FileFillZero";
        previewKey = "FilePreview";
        sourcePathKey = "FileSourcePath";
        appendColorKey = "FileAppendColor";
        appendCompressionTypeKey = "FileAppendCompressionType";
        appendDensityKey = "FileAppendDensity";
        appendQualityKey = "FileAppendQuality";
        appendSizeKey = "FileAppendSize";

        fileExtensionFilter = new ArrayList();
        fileExtensionFilter.add(new FileChooser.ExtensionFilter("*", "*.*"));
    }

    @Override
    protected void initializeNext() {
        tableController = filesTableController;

        tableController.tableData.addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                digitInput.setText((tableController.tableData.size() + "").length() + "");
            }
        });

        operationBarController.startButton.disableProperty().bind(
                Bindings.isEmpty(tableController.getFilesTableView().getItems())
                        .or(tableController.addButton.disableProperty())
        );

    }

    protected void handleSourceFiles() {
        sourceFiles = new ArrayList();
        for (FileInformation f : sourceFilesInformation) {
            sourceFiles.add(new File(f.getFileName()));
        }
        total = sourceFiles.size();
        sortFiles(sourceFiles);

        int bdigit = (tableController.tableData.size() + "").length();
        try {
            digit = Integer.valueOf(digitInput.getText());
            if (digit < bdigit) {
                digit = bdigit;
            }
        } catch (Exception e) {
            digit = bdigit;
        }
        digitInput.setText(digit + "");
    }

    protected void sortFiles(List<File> files) {
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        if (getMessage("OriginalFileName").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.FileName);
        } else if (getMessage("CreateTime").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.CreateTime);
        } else if (getMessage("ModifyTime").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.ModifyTime);
        } else if (getMessage("Size").equals(sort.getText())) {
            FileTools.sortFiles(files, FileSortType.Size);
        }
        if (descentCheck.isSelected()) {
            Collections.reverse(files);
        }
    }

    @FXML
    @Override
    protected void startProcess(ActionEvent event) {
        try {
            sourceFilesInformation = tableController.getTableData();
            if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
                return;
            }

            if (!paused) {
                handleSourceFiles();
                currentIndex = 0;
                try {
                    currentAccum = Integer.valueOf(acumFromInput.getText());
                } catch (Exception e) {
                    currentAccum = 0;
                }
                success = fail = 0;
            }

            paused = false;
            startTime = new Date();
            currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {

                @Override
                protected Void call() {
                    try {
                        for (; currentIndex < total;) {
                            if (isCancelled()) {
                                break;
                            }
                            handleCurrentIndex();

                            currentIndex++;
                            updateProgress(currentIndex, total);
                            updateMessage(currentIndex + "/" + total);

                            if (isCancelled()) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
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
        File file = sourceFiles.get(currentIndex);
        String newName = renameFile(file);
        if (tableController != null) {
            FileInformation d = tableController.findData(file.getAbsolutePath());
            d.setHandled(AppVaribles.getMessage("Yes"));
            if (newName != null) {
                d.setNewName(new File(newName).getAbsolutePath());
            } else {
                d.setNewName(AppVaribles.getMessage("FailRename"));
            }
        }
        currentTotalHandled++;
    }

    protected String renameFile(File file) {
        String newName = makeFilename(file);
        try {
            if (newName != null) {
                if (file.renameTo(new File(newName))) {
                    return newName;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    protected void openTarget(ActionEvent event) {
        try {
            sourceFilesInformation = tableController.getTableData();
            if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
                return;
            }
            File f = new File(sourceFilesInformation.get(0).getFileName());
            if (f.isDirectory()) {
                Desktop.getDesktop().browse(new File(f.getPath()).toURI());
            } else {
                Desktop.getDesktop().browse(new File(f.getParent()).toURI());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void updateInterface(final String newStatus) {
        currentStatus = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                try {
                    tableController.filesTableView.refresh();
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
                            paraBox.setDisable(true);
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
                                operationBarController.openTargetButton.setDisable(false);
                                operationBarController.pauseButton.setVisible(false);
                                operationBarController.pauseButton.setDisable(true);
                                tableController.addButton.setDisable(true);
                                tableController.deleteButton.setDisable(true);
                                tableController.recoveryAllButton.setDisable(false);
                                tableController.recoverySelectedButton.setDisable(false);
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
        long cost = (new Date().getTime() - startTime.getTime()) / 1000;
        double avg = 0;
        if (currentTotalHandled != 0) {
            avg = ValueTools.roundDouble3((double) cost / currentTotalHandled);
        }
        String s;
        if (paused) {
            s = getMessage("Paused");
        } else {
            s = getMessage(currentStatus);
        }
        s += ". " + getMessage("HandledThisTime") + ": " + currentTotalHandled + " "
                + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

    protected String makeFilename(File file) {
        try {
            if (file == null || !file.isFile()) {
                return null;
            }
            String filename = file.getParent() + "/";
            if (originalCheck.isSelected()) {
                filename += FileTools.getFilePrefix(file.getName());
            }
            if (stringCheck.isSelected()) {
                filename += stringInput.getText();
            }
            if (accumCheck.isSelected()) {
                String pageNumber = currentAccum + "";
                if (fillZero.isSelected()) {
                    pageNumber = ValueTools.fillNumber(currentAccum, digit);
                }
                filename += pageNumber;
                currentAccum++;
            }
            if (suffixCheck.isSelected()) {
                filename += "." + suffixInput.getText();
            } else {
                filename += "." + FileTools.getFileSuffix(file.getName());
            }
            return filename;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }
}
