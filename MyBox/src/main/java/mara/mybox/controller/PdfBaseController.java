package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;
import mara.mybox.objects.PdfInformation;
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

    protected Stage infoStage;
    protected boolean isPreview;
    PdfInformation pdfInformation;

    protected List<File> sourceFiles;
    protected ObservableList<FileInformation> sourceFilesInformation;

    @FXML
    protected Pane filesTable;
    @FXML
    protected FilesTableController filesTableController;
    @FXML
    protected TextField sourceFileInput;
    @FXML
    protected TextField targetPathInput;
    @FXML
    protected TextField targetPrefixInput;
    @FXML
    protected CheckBox subdirCheck;
    @FXML
    protected Button openTargetButton;
    @FXML
    protected TextField statusLabel;
    @FXML
    protected Pane pdfConvertAttributes;
    @FXML
    protected PdfConvertAttributesController pdfConvertAttributesController;
    @FXML
    protected CheckBox fillZero;
    @FXML
    protected CheckBox appendDensity;
    @FXML
    protected CheckBox appendColor;
    @FXML
    protected CheckBox appendCompressionType;
    @FXML
    protected CheckBox appendQuality;
    @FXML
    protected Button startButton;
    @FXML
    protected Button pauseButton;
    @FXML
    protected Button previewButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected Label progressValue;
    @FXML
    protected TextField fromPageInput;
    @FXML
    protected TextField toPageInput;
    @FXML
    protected TextField previewInput;
    @FXML
    protected PasswordField passwordInput;
    @FXML
    protected TextField acumFromInput;
    @FXML
    protected Button fileInformationButton;
    @FXML
    protected ProgressBar fileProgressBar;
    @FXML
    protected Label fileProgressValue;

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

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty() || !FileTools.isPDF(newValue)) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        final File file = new File(newValue);
                        if (!file.exists()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        sourceFileInput.setStyle(null);
                        sourceFileChanged(file);
                        if (file.isDirectory()) {
                            AppVaribles.setConfigValue("pdfSourcePath", file.getPath());
//                            if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
//                                targetPathInput.setText(sourceFile.getPath());
//                            }

                        } else {
                            AppVaribles.setConfigValue("pdfSourcePath", file.getParent());
                            if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
                                targetPathInput.setText(AppVaribles.getConfigValue("pdfTargetPath", System.getProperty("user.home")));
                            }
                            if (targetPrefixInput != null) {
                                targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
                            }
                        }
                    }
                });
            }

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            final File file = new File(newValue);
                            if (file.isDirectory()) {
                                AppVaribles.setConfigValue("pdfTargetPath", file.getPath());
                            } else {
                                AppVaribles.setConfigValue("pdfTargetPath", file.getParent());
                            }
                            if (openTargetButton != null) {
                                if (!file.exists()) {
                                    openTargetButton.setDisable(true);
                                } else {
                                    openTargetButton.setDisable(false);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                });
                FxmlTools.setFileValidation(targetPathInput);
            }

            fillZero.setSelected(AppVaribles.getConfigBoolean("pci_fill"));
            subdirCheck.setSelected(AppVaribles.getConfigBoolean("pci_creatSubdir"));

            if (fromPageInput != null) {
                FxmlTools.setNonnegativeValidation(fromPageInput);
            }
            if (toPageInput != null) {
                FxmlTools.setNonnegativeValidation(toPageInput);
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
    protected void selectSourceFile() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue("pdfSourcePath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file != null) {
                sourceFileInput.setText(file.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectTargetPath() {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue("pdfTargetPath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory != null) {
                targetPathInput.setText(directory.getPath());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void openTargetPath() {
        try {
            Desktop.getDesktop().browse(new File(targetPathInput.getText()).toURI());
//            new ProcessBuilder("Explorer", targetPath.getText()).start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        toPageInput.setText("");
        fileInformationButton.setDisable(true);
        statusLabel.setText(getMessage("Loading..."));

        pdfInformation = new PdfInformation(file);
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pdfInformation.loadDocument(passwordInput.getText());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        statusLabel.setText("");
                        if (pdfInformation != null) {
                            toPageInput.setText((pdfInformation.getNumberOfPages() - 1) + "");
                            fileInformationButton.setDisable(false);
                        }
                    }
                });
                return null;
            }
        };
        openLoadingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void startProcess() {
        isPreview = false;
        makeActualParameters();
        currentParameters = actualParameters;
        doCurrentProcess();
    }

    @FXML
    protected void preview() {
        isPreview = true;
        makeActualParameters();
        previewParameters = copyParameters(actualParameters);
        int page = 0;
        if (previewInput != null) {
            page = FxmlTools.getInputInt(previewInput);
            if (page > pdfInformation.getNumberOfPages()) {
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

        actualParameters.fill = fillZero.isSelected();
        AppVaribles.setConfigValue("pci_fill", actualParameters.fill);

        actualParameters.targetRootPath = targetPathInput.getText();
        actualParameters.targetPath = targetPathInput.getText();

        actualParameters.createSubDir = subdirCheck.isSelected();
        AppVaribles.setConfigValue("pci_creatSubdir", actualParameters.createSubDir);

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

    protected abstract void makeMoreParameters();

    protected void makeSingleParameters() {

        actualParameters.isBatch = false;

        sourceFiles = new ArrayList();
        sourceFiles.add(pdfInformation.getFile());
        actualParameters.sourceFile = pdfInformation.getFile();

        actualParameters.fromPage = FxmlTools.getInputInt(fromPageInput);
        actualParameters.toPage = FxmlTools.getInputInt(toPageInput);
        actualParameters.acumFrom = FxmlTools.getInputInt(acumFromInput);
        actualParameters.currentNameNumber = actualParameters.acumFrom;
        actualParameters.password = passwordInput.getText();
        actualParameters.startPage = actualParameters.fromPage;
        actualParameters.acumStart = actualParameters.acumFrom;
        actualParameters.acumDigit = (actualParameters.toPage + "").length();

        actualParameters.targetPrefix = targetPrefixInput.getText();
        if (subdirCheck.isSelected()) {
            File finalPath = new File(actualParameters.targetPath + "/" + actualParameters.targetPrefix + "/");
            if (!finalPath.exists()) {
                finalPath.mkdir();
            }
            actualParameters.targetPath += "/" + actualParameters.targetPrefix;
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

    protected abstract void doCurrentProcess();

    protected void updateInterface(final String newStatus) {
        currentParameters.status = newStatus;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                try {
                    if (fileProgressBar != null) {
                        fileProgressBar.setProgress(currentParameters.currentFileIndex / sourceFiles.size());
                        fileProgressValue.setText(currentParameters.currentFileIndex + " / " + sourceFiles.size());
                    }
                    if (null != newStatus) {
                        switch (newStatus) {
                            case "StartFile":
                                statusLabel.setText(currentParameters.sourceFile.getName() + " "
                                        + getMessage("Handling...") + " "
                                        + getMessage("StartTime")
                                        + ": " + DateTools.datetimeToString(currentParameters.startTime));
                                if (fileProgressBar != null) {
                                    fileProgressBar.setProgress(currentParameters.currentFileIndex / sourceFiles.size());
                                    fileProgressValue.setText(currentParameters.currentFileIndex + " / " + sourceFiles.size());
                                }
                                break;

                            case "Started":
                                startButton.setText(AppVaribles.getMessage("Cancel"));
                                startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        cancelProcess();
                                    }
                                });
                                pauseButton.setVisible(true);
                                pauseButton.setDisable(false);
                                pauseButton.setText(AppVaribles.getMessage("Pause"));
                                pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        pauseProcess();
                                    }
                                });
                                paraBox.setDisable(true);
                                break;

                            case "Paused":
                                startButton.setText(AppVaribles.getMessage("Cancel"));
                                startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        cancelProcess();
                                    }
                                });
                                pauseButton.setVisible(true);
                                pauseButton.setDisable(false);
                                pauseButton.setText(AppVaribles.getMessage("Continue"));
                                pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startProcess();
                                    }
                                });
                                paraBox.setDisable(true);
                                showCost();
                                break;

                            case "CompleteFile":
                                showCost();
                                if (fileProgressBar != null) {
                                    fileProgressBar.setProgress(currentParameters.currentFileIndex / sourceFiles.size());
                                    fileProgressValue.setText(currentParameters.currentFileIndex + " / " + sourceFiles.size());
                                }
                                break;

                            case "Done":
                                if (isPreview) {
                                    if (currentParameters.finalTargetName == null
                                            || !new File(currentParameters.finalTargetName).exists()) {
                                        popInformation(AppVaribles.getMessage("NoDataNotSupported"));
                                    } else {
                                        showImage(currentParameters.finalTargetName);
                                    }
                                }

                            default:
                                startButton.setText(AppVaribles.getMessage("Start"));
                                startButton.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        startProcess();
                                    }
                                });
                                pauseButton.setVisible(false);
                                pauseButton.setDisable(true);
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
        if (statusLabel == null) {
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
        statusLabel.setText(s);
    }

    @FXML
    protected void pauseProcess() {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Paused");
    }

    protected void cancelProcess() {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        updateInterface("Canceled");
    }

    @FXML
    protected void showFileInformation() {
        if (pdfInformation == null) {
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PdfInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            PdfInformationController controller = fxmlLoader.getController();
            controller.setInformation(pdfInformation);

            infoStage = new Stage();
            controller.setMyStage(infoStage);
            infoStage.setTitle(AppVaribles.getMessage("AppTitle"));
            infoStage.initModality(Modality.NONE);
            infoStage.initStyle(StageStyle.DECORATED);
            infoStage.initOwner(null);
            infoStage.getIcons().add(CommonValues.AppIcon);
            infoStage.setScene(new Scene(root));
            infoStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
