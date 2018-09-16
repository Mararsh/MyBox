package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class PdfBaseController extends BaseController {

    protected boolean isTxt, allowPaused;
    protected String PdfSourceFromKey, PdfSourceToKey;

    @FXML
    protected PdfSourceSelectionController sourceSelectionController;
    @FXML
    protected Pane pdfConvertAttributes;
    @FXML
    protected PdfConvertAttributesController pdfConvertAttributesController;

    protected class ProcessParameters {

        protected File sourceFile;
        protected int fromPage, toPage, startPage, acumFrom, acumStart, acumDigit;
        protected String password, status, targetPath, targetPrefix, targetRootPath;
        protected Date startTime, endTime;
        protected int currentPage, currentNameNumber, currentFileIndex, currentTotalHandled;
        protected boolean fill, aDensity, aColor, aCompression, aQuality, isBatch, createSubDir;
    }

    protected ProcessParameters actualParameters, previewParameters, currentParameters;

    public PdfBaseController() {
        targetPathKey = "PdfTargetPath";
        creatSubdirKey = "PdfCreatSubdir";
        fillZeroKey = "PdfFillZero";
        previewKey = "PdfPreview";
        sourcePathKey = "PdfSourcePath";
        appendColorKey = "PdfAppendColor";
        appendCompressionTypeKey = "PdfAppendCompressionType";
        appendDensityKey = "PdfAppendDensity";
        appendQualityKey = "PdfAppendQuality";
        appendSizeKey = "PdfAppendSize";
        PdfSourceFromKey = "PdfSourceFromKey";
        PdfSourceToKey = "PdfSourceToKey";

        fileExtensionFilter = CommonValues.PdfExtensionFilter;

    }

    @Override
    protected void initializeNext() {
        try {
            allowPaused = true;

            if (sourceSelectionController != null) {
                sourceSelectionController.setParentController(this);
            }

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initializeNext2() {

    }

    @Override
    protected void sourceFileChanged(final File file) {
        if (targetSelectionController == null) {
            return;
        }
        if (targetSelectionController.targetPrefixInput != null) {
            String filename = file.getName();
            targetSelectionController.targetPrefixInput.setText(FileTools.getFilePrefix(filename));
        }
        if (targetSelectionController.targetPathInput != null && targetSelectionController.targetPathInput.getText().isEmpty()) {
            targetSelectionController.targetPathInput.setText(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
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
        if (actualParameters != null && paused) {
            actualParameters.startPage = actualParameters.currentPage;
            actualParameters.acumStart = actualParameters.currentNameNumber + 1;
            return;
        }
        actualParameters = new ProcessParameters();
        actualParameters.currentFileIndex = 0;

        if (fillZero != null) {
            actualParameters.fill = fillZero.isSelected();
            AppVaribles.setConfigValue(fillZeroKey, actualParameters.fill);
        }

        if (targetSelectionController != null) {
            actualParameters.targetRootPath = targetSelectionController.targetPathInput.getText();
            actualParameters.targetPath = actualParameters.targetRootPath;
            if (targetSelectionController.subdirCheck != null) {
                actualParameters.createSubDir = targetSelectionController.subdirCheck.isSelected();
                AppVaribles.setConfigValue(creatSubdirKey, actualParameters.createSubDir);
            }
        }

        if (pdfConvertAttributesController != null) {
            actualParameters.aDensity = appendDensity.isSelected();
            actualParameters.aColor = appendColor.isSelected();
            actualParameters.aCompression = appendCompressionType.isSelected();
            actualParameters.aQuality = appendQuality.isSelected();

            AppVaribles.setConfigValue(appendDensityKey, actualParameters.aDensity);
            AppVaribles.setConfigValue(appendColorKey, actualParameters.aColor);
            AppVaribles.setConfigValue(appendCompressionTypeKey, actualParameters.aCompression);
            AppVaribles.setConfigValue(appendQualityKey, actualParameters.aQuality);
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
//        AppVaribles.setConfigInt(PdfSourceFromKey, actualParameters.fromPage);
        actualParameters.toPage = sourceSelectionController.readToPage();
//        AppVaribles.setConfigInt(PdfSourceFromKey, actualParameters.toPage);
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
            if (targetSelectionController.targetFileInput != null) {
                actualParameters.targetPrefix = targetSelectionController.targetFileInput.getText();
            }
            if (targetSelectionController.subdirCheck != null && targetSelectionController.subdirCheck.isSelected()) {
                File finalPath = new File(actualParameters.targetPath + "/" + actualParameters.targetPrefix + "/");
                if (!finalPath.exists()) {
                    finalPath.mkdirs();
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

    @Override
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
                            if (allowPaused) {
                                operationBarController.pauseButton.setVisible(true);
                                operationBarController.pauseButton.setDisable(false);
                                operationBarController.pauseButton.setText(AppVaribles.getMessage("Pause"));
                                operationBarController.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
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
                            if (operationBarController.fileProgressBar != null) {
                                operationBarController.fileProgressBar.setProgress(currentParameters.currentFileIndex / sourceFiles.size());
                                operationBarController.fileProgressValue.setText(currentParameters.currentFileIndex + " / " + sourceFiles.size());
                            }
                            break;

                        case "Done":
                            if (isPreview) {
                                if (finalTargetName == null
                                        || !new File(finalTargetName).exists()) {
                                    alertInformation(AppVaribles.getMessage("NoDataNotSupported"));
                                } else if (isTxt) {
                                    File txtFile = new File(finalTargetName);
                                    Desktop.getDesktop().browse(txtFile.toURI());
                                } else {
                                    openImageManufactureInNew(finalTargetName);
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
//                                operationBarController.openTargetButton.setDisable(false);
                                paraBox.setDisable(false);
                            }
                            showCost();
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
