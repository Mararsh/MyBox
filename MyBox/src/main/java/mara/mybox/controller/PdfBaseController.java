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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class PdfBaseController extends BaseController {

    protected boolean isPreview, isTxt;

    protected List<File> sourceFiles;
    protected ObservableList<FileInformation> sourceFilesInformation;

    @FXML
    protected Pane sourceSelection;
    @FXML
    protected PdfSourceSelectionController sourceSelectionController;
    @FXML
    protected Pane targetSelection;
    @FXML
    protected TargetSelectionController targetSelectionController;
    @FXML
    protected Pane filesTable;
    @FXML
    protected FilesTableController filesTableController;
    @FXML
    protected Pane pdfConvertAttributes;
    @FXML
    protected PdfConvertAttributesController pdfConvertAttributesController;
    @FXML
    protected Pane operationBar;
    @FXML
    protected PdfOperationController operationBarController;
    @FXML
    protected CheckBox fillZero, appendDensity, appendColor, appendCompressionType, appendQuality;
    @FXML
    protected Button previewButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected TextField previewInput, acumFromInput;

    protected class ProcessParameters {

        public File sourceFile;
        public int fromPage, toPage, startPage, acumFrom, acumStart, acumDigit;
        public String password, status, targetPath, targetPrefix, targetRootPath, finalTargetName;
        public Date startTime, endTime;
        public int currentPage, currentNameNumber, currentFileIndex, currentTotalHandled;
        boolean fill, aDensity, aColor, aCompression, aQuality, isBatch, createSubDir;
    }

    protected ProcessParameters actualParameters, previewParameters, currentParameters;

    public PdfBaseController() {
    }

    @Override
    protected void initializeNext() {
        try {
            fileExtensionFilter = new ArrayList();
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));

            if (sourceSelectionController != null) {
                sourceSelectionController.setParentController(this);
            }
            if (targetSelectionController != null) {
                targetSelectionController.setParentController(this);
            }
            if (operationBarController != null) {
                operationBarController.setParentController(this);
                if (targetSelectionController != null) {
                    operationBarController.openTargetButton.disableProperty().bind(
                            Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty())
                                    .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                    );
                }
            }

            if (fillZero != null) {
                fillZero.setSelected(AppVaribles.getConfigBoolean("pci_fill"));
            }

            if (acumFromInput != null) {
                FxmlTools.setNonnegativeValidation(acumFromInput);
            }
            if (previewInput != null) {
                previewInput.setText(AppVaribles.getConfigValue("pci_preview", "0"));
                FxmlTools.setNonnegativeValidation(previewInput);
                previewInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        AppVaribles.setConfigValue("pci_preview", newValue);
                    }
                });
            }

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initializeNext2() {

    }

    @FXML
    protected void openTarget(ActionEvent event) {
        try {
            if (targetSelectionController == null || targetSelectionController.targetPath == null) {
                return;
            }
            if (targetSelectionController.targetFileInput != null) {
                File txtFile = new File(currentParameters.finalTargetName);
                Desktop.getDesktop().browse(txtFile.toURI());
            } else if (targetSelectionController.targetPathInput != null) {
                Desktop.getDesktop().browse(targetSelectionController.targetPath.toURI());
            }
//            new ProcessBuilder("Explorer", targetPath.getText()).start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void sourceFileChanged() {
        if (sourceSelectionController == null || sourceSelectionController.sourceFile == null
                || targetSelectionController == null) {
            return;
        }
        if (targetSelectionController.targetPrefixInput != null) {
            String filename = sourceSelectionController.sourceFile.getName();
            targetSelectionController.targetPrefixInput.setText(FileTools.getFilePrefix(filename));
        }
        if (targetSelectionController.targetPathInput != null && targetSelectionController.targetPathInput.getText().isEmpty()) {
            targetSelectionController.targetPathInput.setText(AppVaribles.getConfigValue("pdfTargetPath", System.getProperty("user.home")));
        }

    }

    protected void targetPathChanged() {
        if (operationBarController == null || operationBarController.openTargetButton == null) {
            return;
        }
    }

    @FXML
    protected void startProcess(ActionEvent event) {
        isPreview = false;
        makeActualParameters();
        currentParameters = actualParameters;
        doCurrentProcess();
    }

    @FXML
    protected void preview(ActionEvent event) {
        isPreview = true;
        makeActualParameters();
        previewParameters = copyParameters(actualParameters);
        int page = 0;
        if (previewInput != null) {
            page = FxmlTools.getInputInt(previewInput);
            if (page > sourceSelectionController.pdfInformation.getNumberOfPages()) {
                page = 0;
                previewInput.setText("0");
            }
        }
        previewParameters.fromPage = page;
        previewParameters.startPage = page;
        previewParameters.toPage = page;
        previewParameters.acumStart = previewParameters.acumFrom;
        previewParameters.currentPage = 0;
        previewParameters.status = "start";
        currentParameters = previewParameters;
        doCurrentProcess();
    }

    protected void makeActualParameters() {
        if (actualParameters != null && "Paused".equals(actualParameters.status)) {
            actualParameters.startPage = actualParameters.currentPage;
            actualParameters.acumStart = actualParameters.currentNameNumber + 1;
            return;
        }
        actualParameters = new ProcessParameters();
        actualParameters.currentFileIndex = 0;

        if (fillZero != null) {
            actualParameters.fill = fillZero.isSelected();
            AppVaribles.setConfigValue("pci_fill", actualParameters.fill);
        }

        if (targetSelectionController != null) {
            actualParameters.targetRootPath = targetSelectionController.targetPath.getAbsolutePath();
            actualParameters.targetPath = targetSelectionController.targetPath.getAbsolutePath();
            if (targetSelectionController.subdirCheck != null) {
                actualParameters.createSubDir = targetSelectionController.subdirCheck.isSelected();
                AppVaribles.setConfigValue("pci_creatSubdir", actualParameters.createSubDir);
            }
        }

        if (pdfConvertAttributesController != null) {
            actualParameters.aDensity = appendDensity.isSelected();
            actualParameters.aColor = appendColor.isSelected();
            actualParameters.aCompression = appendCompressionType.isSelected();
            actualParameters.aQuality = appendQuality.isSelected();

            AppVaribles.setConfigValue("pci_aDensity", actualParameters.aDensity);
            AppVaribles.setConfigValue("pci_aColor", actualParameters.aColor);
            AppVaribles.setConfigValue("pci_aCompression", actualParameters.aCompression);
            AppVaribles.setConfigValue("pci_aQuality", actualParameters.aQuality);
        }

        makeMoreParameters();
    }

    protected void makeMoreParameters() {

    }

    protected void makeSingleParameters() {

        actualParameters.isBatch = false;

        sourceFiles = new ArrayList();
        sourceFiles.add(sourceSelectionController.pdfInformation.getFile());
        actualParameters.sourceFile = sourceSelectionController.pdfInformation.getFile();

        actualParameters.fromPage = sourceSelectionController.readFromPage();
        actualParameters.toPage = sourceSelectionController.readToPage();
        actualParameters.currentNameNumber = actualParameters.acumFrom;
        actualParameters.password = sourceSelectionController.readPassword();
        actualParameters.startPage = actualParameters.fromPage;
        if (acumFromInput != null) {
            actualParameters.acumFrom = FxmlTools.getInputInt(acumFromInput);
            actualParameters.acumStart = actualParameters.acumFrom;
            actualParameters.acumDigit = (actualParameters.toPage + "").length();
        }
        if (targetSelectionController != null) {
            if (targetSelectionController.targetPrefixInput != null) {
                actualParameters.targetPrefix = targetSelectionController.targetPrefixInput.getText();
            }
            if (targetSelectionController.subdirCheck != null && targetSelectionController.subdirCheck.isSelected()) {
                File finalPath = new File(actualParameters.targetPath + "/" + actualParameters.targetPrefix + "/");
                if (!finalPath.exists()) {
                    finalPath.mkdir();
                }
                actualParameters.targetPath += "/" + actualParameters.targetPrefix;
            }
        }
    }

    protected void makeBatchParameters() {

        actualParameters.isBatch = true;

        sourceFilesInformation = filesTableController.getTableData();
        if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
            actualParameters = null;
            return;
        }
        sourceFiles = new ArrayList();
        for (FileInformation f : sourceFilesInformation) {
            sourceFiles.add(new File(f.getFileName()));
        }

//        actualParameters.sourceFile = new File(sourceFilesInformation.get(0).getFileName());
        actualParameters.fromPage = 0;
        actualParameters.toPage = 100;
        actualParameters.acumFrom = 0;
        actualParameters.currentNameNumber = 0;
        actualParameters.password = "";
        actualParameters.startPage = 0;
        actualParameters.acumStart = 0;
        actualParameters.acumDigit = 0;

    }

    protected ProcessParameters copyParameters(ProcessParameters theConversion) {
        ProcessParameters newConversion = new ProcessParameters();
        newConversion.aColor = theConversion.aColor;
        newConversion.aCompression = theConversion.aCompression;
        newConversion.aDensity = theConversion.aDensity;
        newConversion.aQuality = theConversion.aQuality;
        newConversion.acumDigit = theConversion.acumDigit;
        newConversion.acumFrom = theConversion.acumFrom;
        newConversion.acumStart = theConversion.acumStart;
        newConversion.currentNameNumber = theConversion.currentNameNumber;
        newConversion.currentPage = theConversion.currentPage;
        newConversion.currentFileIndex = theConversion.currentFileIndex;
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

    protected void doCurrentProcess() {

    }

    protected void updateInterface(final String newStatus) {
        currentParameters.status = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                try {
                    if (operationBarController.fileProgressBar != null) {
                        operationBarController.fileProgressBar.setProgress(currentParameters.currentFileIndex / sourceFiles.size());
                        operationBarController.fileProgressValue.setText(currentParameters.currentFileIndex + " / " + sourceFiles.size());
                    }
                    if (null != newStatus) {
                        switch (newStatus) {
                            case "StartFile":
                                operationBarController.statusLabel.setText(currentParameters.sourceFile.getName() + " "
                                        + getMessage("Handling...") + " "
                                        + getMessage("StartTime")
                                        + ": " + DateTools.datetimeToString(currentParameters.startTime));
                                if (operationBarController.fileProgressBar != null) {
                                    operationBarController.fileProgressBar.setProgress(currentParameters.currentFileIndex / sourceFiles.size());
                                    operationBarController.fileProgressValue.setText(currentParameters.currentFileIndex + " / " + sourceFiles.size());
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

                            case "Paused":
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
                                showCost();
                                break;

                            case "CompleteFile":
                                showCost();
                                if (operationBarController.fileProgressBar != null) {
                                    operationBarController.fileProgressBar.setProgress(currentParameters.currentFileIndex / sourceFiles.size());
                                    operationBarController.fileProgressValue.setText(currentParameters.currentFileIndex + " / " + sourceFiles.size());
                                }
                                break;

                            case "Done":
                                if (isPreview) {
                                    if (currentParameters.finalTargetName == null
                                            || !new File(currentParameters.finalTargetName).exists()) {
                                        popInformation(AppVaribles.getMessage("NoDataNotSupported"));
                                    } else if (isTxt) {
                                        File txtFile = new File(currentParameters.finalTargetName);
                                        Desktop.getDesktop().browse(txtFile.toURI());
                                    } else {
                                        showImage(currentParameters.finalTargetName);
                                    }
                                }

                            default:
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
                                showCost();

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
        double avg = ValueTools.roundDouble3((double) cost / currentParameters.currentTotalHandled);
        String s = getMessage(currentParameters.status) + ". "
                + getMessage("HandledThisTime") + ": " + currentParameters.currentTotalHandled + " "
                + getMessage("Cost") + ": " + cost + " " + getMessage("Seconds") + ". "
                + getMessage("Average") + ": " + avg + " " + getMessage("SecondsPerItem") + ". "
                + getMessage("StartTime") + ": " + DateTools.datetimeToString(currentParameters.startTime) + ", "
                + getMessage("EndTime") + ": " + DateTools.datetimeToString(new Date());
        operationBarController.statusLabel.setText(s);
    }

    @FXML
    protected void pauseProcess(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Paused");
    }

    protected void cancelProcess(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Canceled");
    }

}
