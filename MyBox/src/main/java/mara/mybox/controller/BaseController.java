package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:50:43
 * @Description
 * @License Apache License Version 2.0
 */
public class BaseController implements Initializable {

    protected static final Logger logger = LogManager.getLogger();

    protected List<FileChooser.ExtensionFilter> fileExtensionFilter;

    protected String myFxml, parentFxml, currentStatus;
    protected Stage myStage, loadingStage;
    protected Alert loadingAlert;
    protected Task<Void> task;
    protected BaseController parentController;

    protected boolean isPreview, targetIsFile, paused;
    protected File sourceFile, targetPath;
    protected List<File> sourceFiles;
    protected ObservableList<FileInformation> sourceFilesInformation;
    protected String finalTargetName;

    protected String targetPathKey, creatSubdirKey, fillZeroKey;
    protected String previewKey, sourcePathKey;
    protected String appendColorKey, appendCompressionTypeKey;
    protected String appendDensityKey, appendQualityKey, appendSizeKey;

    @FXML
    protected Pane thisPane, mainMenu, sourceSelection, targetSelection, filesTable, dirsTable, operationBar;
    @FXML
    protected MainMenuController mainMenuController;
    @FXML
    protected TextField sourceFileInput, targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    protected TargetSelectionController targetSelectionController;
    @FXML
    protected FilesTableController filesTableController;
    @FXML
    protected DirectoriesTableController dirsTableController;
    @FXML
    protected OperationController operationBarController;
    @FXML
    protected Button previewButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected TextField previewInput, acumFromInput, digitInput;
    @FXML
    protected CheckBox fillZero, subdirCheck, appendDensity, appendColor, appendCompressionType, appendQuality, appendSize;
    @FXML
    protected Label bottomLabel;

    public BaseController() {

        targetPathKey = "targetPath";
        creatSubdirKey = "creatSubdir";
        fillZeroKey = "fillZero";
        previewKey = "preview";
        sourcePathKey = "sourcePath";
        appendColorKey = "appendColor";
        appendCompressionTypeKey = "appendCompressionType";
        appendDensityKey = "appendDensity";
        appendQualityKey = "appendQuality";
        appendSizeKey = "appendSize";

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            myFxml = FxmlTools.getFxmlPath(url.getPath());
            AppVaribles.currentController = this;
            AppVaribles.alarmClockController = null;
            if (mainMenuController != null) {
                mainMenuController.setParentFxml(myFxml);
                mainMenuController.setParentController(this);
            }

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        final File file = new File(newValue);
                        if (!file.exists()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        sourceFileInput.setStyle(null);
                        sourceFile = file;
                        sourceFileChanged(file);
                        if (parentController != null) {
                            parentController.sourceFileChanged(file);
                        }
                        if (file.isDirectory()) {
                            AppVaribles.setConfigValue(sourcePathKey, file.getPath());
                        } else {
                            AppVaribles.setConfigValue(sourcePathKey, file.getParent());
                            if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
                                targetPathInput.setText(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
                            }
                            if (targetPrefixInput != null) {
                                targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
                            }
                            if (targetSelectionController != null && targetSelectionController.targetPrefixInput != null) {
                                targetSelectionController.targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
                            }
                        }
                    }
                });
            }

            if (filesTableController != null) {
                filesTableController.setFileExtensionFilter(fileExtensionFilter);
                filesTableController.setParentController(this);
            }
            if (dirsTableController != null) {
                dirsTableController.setFileExtensionFilter(fileExtensionFilter);
                dirsTableController.setParentController(this);
            }

            if (subdirCheck != null) {
                subdirCheck.setSelected(AppVaribles.getConfigBoolean(creatSubdirKey));
            }

            if (targetSelectionController != null) {
                targetSelectionController.setParentController(this);
                if (targetSelectionController.targetPathInput != null) {
                    targetSelectionController.targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            try {
                                final File file = new File(newValue);
                                if (!file.exists()) {
                                    targetSelectionController.targetPathInput.setStyle(badStyle);
                                    return;
                                }
                                targetSelectionController.targetPathInput.setStyle(null);
                                if (file.isDirectory()) {
                                    AppVaribles.setConfigValue(targetPathKey, file.getPath());
                                } else {
                                    AppVaribles.setConfigValue(targetPathKey, file.getParent());
                                }
                                targetPath = file;
                                targetPathChanged();
                            } catch (Exception e) {
                            }
                        }
                    });
                    targetSelectionController.targetPathInput.setText(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
                }
            }

            if (appendSize != null) {
                appendSize.setSelected(AppVaribles.getConfigBoolean(appendSizeKey));
            }
            if (appendColor != null) {
                appendColor.setSelected(AppVaribles.getConfigBoolean(appendColorKey));
            }
            if (appendCompressionType != null) {
                appendCompressionType.setSelected(AppVaribles.getConfigBoolean(appendCompressionTypeKey));
            }
            if (appendQuality != null) {
                appendQuality.setSelected(AppVaribles.getConfigBoolean(appendQualityKey));
            }
            if (appendDensity != null) {
                appendDensity.setSelected(AppVaribles.getConfigBoolean(appendDensityKey));
            }
            if (fillZero != null) {
                fillZero.setSelected(AppVaribles.getConfigBoolean(fillZeroKey));
            }

            if (previewInput != null) {
                previewInput.setText(AppVaribles.getConfigValue(previewKey, "0"));
                FxmlTools.setNonnegativeValidation(previewInput);
                previewInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        AppVaribles.setConfigValue(previewKey, newValue);
                    }
                });
            }

            if (operationBarController != null) {
                operationBarController.setParentController(this);
                if (targetSelectionController != null && targetSelectionController.targetPathInput != null) {
                    operationBarController.openTargetButton.disableProperty().bind(
                            Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty())
                                    .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                    );
                }
            }

            if (acumFromInput != null) {
                FxmlTools.setNonnegativeValidation(acumFromInput);
            }

            initializeNext();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initializeNext() {

    }

    @FXML
    protected void selectSourceFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(sourcePathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            sourceFile = file;
            AppVaribles.setConfigValue("LastPath", sourceFile.getParent());
            AppVaribles.setConfigValue(sourcePathKey, sourceFile.getParent());

            if (sourceFileInput != null) {
                sourceFileInput.setText(sourceFile.getAbsolutePath());
            } else {
                sourceFileChanged(sourceFile);
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    protected void sourceFileChanged(final File file) {

    }

    @FXML
    protected void selectTargetPath(ActionEvent event) {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", directory.getPath());
            AppVaribles.setConfigValue(targetPathKey, directory.getPath());

            targetPathInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void targetPathChanged() {

    }

    @FXML
    protected void openTarget(ActionEvent event) {
        try {
            if (targetSelectionController == null) {
                return;
            }
            if (targetIsFile && finalTargetName != null) {
                Desktop.getDesktop().browse(new File(finalTargetName).toURI());
            } else {
                if (targetSelectionController.targetPathInput != null) {
                    Desktop.getDesktop().browse(new File(targetSelectionController.targetPathInput.getText()).toURI());
                }
            }

//            new ProcessBuilder("Explorer", targetPath.getText()).start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void startProcess(ActionEvent event) {
    }

    @FXML
    protected void pauseProcess(ActionEvent event) {
        paused = true;
        if (task != null && task.isRunning()) {
            task.cancel();
        } else {
            updateInterface("Canceled");
        }
    }

    protected void cancelProcess(ActionEvent event) {
        paused = false;
        if (task != null && task.isRunning()) {
            task.cancel();
        } else {
            updateInterface("Canceled");
        }
    }

    protected void updateInterface(final String newStatus) {

    }

    protected void markFileHandled(int index) {
        if (filesTableController == null) {
            return;
        }
        FileInformation d = filesTableController.tableData.get(index);
        if (d == null) {
            return;
        }
        d.setHandled(getMessage("Yes"));
        filesTableController.filesTableView.refresh();
    }

    public void reloadStage(String newFxml) {
        reloadStage(newFxml, null);
    }

    public void reloadStage(String newFxml, String title) {
        try {
            getMyStage();
//            if (task != null && task.isRunning()) {
//                openStage(newFxml, title, false);
//                return;
//            }
//            if (parentController != null) {
//                if (parentController.task != null && parentController.task.isRunning()) {
//                    openStage(newFxml, title, false);
//                    return;
//                }
//            }
            if (!stageReloading()) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final BaseController controller = fxmlLoader.getController();
            controller.setMyStage(myStage);
            myStage.setScene(new Scene(pane));
            if (title != null) {
                myStage.setTitle(title);
            } else if (getMyStage().getTitle() == null) {
                myStage.setTitle(AppVaribles.getMessage("AppTitle"));
            }
//            myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//                @Override
//                public void handle(WindowEvent event) {
//                    controller.stageClosing(event);
//                }
//            });
//            if (!newFxml.equals(CommonValues.AlarmClockFxml)) {
//                AppVaribles.alarmClockController = null;
//            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public boolean stageReloading() {
        return true;
    }

    public void openStage(String newFxml, boolean isOwned) {
        openStage(newFxml, AppVaribles.getMessage("AppTitle"), isOwned, false);
    }

    public void openStage(String newFxml, boolean isOwned, boolean monitorClosing) {
        openStage(newFxml, AppVaribles.getMessage("AppTitle"), isOwned, monitorClosing);
    }

    public void openStage(String newFxml, String title, boolean isOwned, boolean monitorClosing) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final BaseController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);

            Scene scene = new Scene(pane);
            stage.initModality(Modality.NONE);
            if (isOwned) {
                stage.initOwner(getMyStage());
            } else {
                stage.initOwner(null);
            }
            if (title == null) {
                stage.setTitle(AppVaribles.getMessage("AppTitle"));
            } else {
                stage.setTitle(title);
            }
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            if (monitorClosing) {
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        if (!controller.stageClosing()) {
                            event.consume();
                        }
                    }
                });
            }
            stage.show();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public boolean stageClosing() {
        try {
//            logger.debug("stageClosing:" + getMyStage().getWidth() + "," + myStage.getHeight());
//            logger.debug(Platform.isImplicitExit());

            if (task != null && task.isRunning()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(AppVaribles.getMessage("AppTitle"));
                alert.setContentText(AppVaribles.getMessage("TaskRunning"));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK && task != null) {
                    task.cancel();
                } else {
                    return false;
                }
            }
            if (AppVaribles.scheduledTasks != null && !AppVaribles.scheduledTasks.isEmpty()) {
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.setTitle(AppVaribles.getMessage("AppTitle"));
//                alert.setContentText(MessageFormat.format(AppVaribles.getMessage("AlarmClocksRunning"), AppVaribles.scheduledTasks.size()));
//                ButtonType buttonStopAlarmsExit = new ButtonType(AppVaribles.getMessage("StopAlarmsExit"));
//                ButtonType buttonKeepAlarmsExit = new ButtonType(AppVaribles.getMessage("KeepAlarmsExit"));
//                ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
//                alert.getButtonTypes().setAll(buttonStopAlarmsExit, buttonKeepAlarmsExit, buttonCancel);
//
//                Optional<ButtonType> result = alert.showAndWait();
//                if (result.get() == buttonStopAlarmsExit) {

                if (AppVaribles.getConfigBoolean("StopAlarmsWhenExit")) {
                    for (Long key : AppVaribles.scheduledTasks.keySet()) {
                        ScheduledFuture future = AppVaribles.scheduledTasks.get(key);
                        future.cancel(true);
                    }
                    AppVaribles.scheduledTasks = null;
                    if (AppVaribles.executorService != null) {
                        AppVaribles.executorService.shutdownNow();
                        AppVaribles.executorService = null;
                    }
                }

            } else {
                if (AppVaribles.scheduledTasks != null) {
                    AppVaribles.scheduledTasks = null;
                }
                if (AppVaribles.executorService != null) {
                    AppVaribles.executorService.shutdownNow();
                    AppVaribles.executorService = null;
                }
            }

            if (AppVaribles.scheduledTasks == null) {
                Platform.setImplicitExit(true);
            }
//            logger.debug(Platform.isImplicitExit());
            return true;

        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public void popInformation(String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public Stage getMyStage() {
        if (myStage == null) {
            if (thisPane != null && thisPane.getScene() != null) {
                myStage = (Stage) thisPane.getScene().getWindow();
            }
        }
        return myStage;
    }

    public void setMyStage(Stage myStage) {
        this.myStage = myStage;

    }

    public void showImage(String filename) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageViewerFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageViewerController controller = fxmlLoader.getController();
            controller.loadImage(filename);

            Stage stage = new Stage();
            controller.setMyStage(stage);
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.initModality(Modality.NONE);
            stage.initStyle(StageStyle.DECORATED);
            stage.initOwner(null);
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageManufacture(String filename) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageManufactureFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageManufactureController controller = fxmlLoader.getController();
            controller.loadImage(filename);

            Stage stage = new Stage();
            controller.setMyStage(stage);
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.initModality(Modality.NONE);
            stage.initStyle(StageStyle.DECORATED);
            stage.initOwner(null);
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void openHandlingStage(final Task<?> task, Modality block) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.LoadingFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            LoadingController controller = fxmlLoader.getController();
            controller.init(task);

            loadingStage = new Stage();
            loadingStage.initModality(block);
//            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.initOwner(getMyStage());
            loadingStage.setScene(new Scene(pane));
            loadingStage.show();

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loadingStage.close();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public String getParentFxml() {
        return parentFxml;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public String getThisFxml() {
        return myFxml;
    }

    public void setThisFxml(String thisFxml) {
        this.myFxml = thisFxml;
    }

    public Stage getLoadingStage() {
        return loadingStage;
    }

    public void setLoadingStage(Stage loadingStage) {
        this.loadingStage = loadingStage;
    }

    public Alert getLoadingAlert() {
        return loadingAlert;
    }

    public void setLoadingAlert(Alert loadingAlert) {
        this.loadingAlert = loadingAlert;
    }

    public Pane getThisPane() {
        return thisPane;
    }

    public void setThisPane(Pane thisPane) {
        this.thisPane = thisPane;
    }

    public List<FileChooser.ExtensionFilter> getFileExtensionFilter() {
        return fileExtensionFilter;
    }

    public void setFileExtensionFilter(List<FileChooser.ExtensionFilter> fileExtensionFilter) {
        this.fileExtensionFilter = fileExtensionFilter;
    }

    public String getMyFxml() {
        return myFxml;
    }

    public void setMyFxml(String myFxml) {
        this.myFxml = myFxml;
    }

    public Task<Void> getTask() {
        return task;
    }

    public void setTask(Task<Void> task) {
        this.task = task;
    }

    public boolean isIsPreview() {
        return isPreview;
    }

    public void setIsPreview(boolean isPreview) {
        this.isPreview = isPreview;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public List<File> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<File> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public ObservableList<FileInformation> getSourceFilesInformation() {
        return sourceFilesInformation;
    }

    public void setSourceFilesInformation(ObservableList<FileInformation> sourceFilesInformation) {
        this.sourceFilesInformation = sourceFilesInformation;
    }

    public Pane getMainMenu() {
        return mainMenu;
    }

    public void setMainMenu(Pane mainMenu) {
        this.mainMenu = mainMenu;
    }

    public Pane getTargetSelection() {
        return targetSelection;
    }

    public void setTargetSelection(Pane targetSelection) {
        this.targetSelection = targetSelection;
    }

    public Pane getFilesTable() {
        return filesTable;
    }

    public void setFilesTable(Pane filesTable) {
        this.filesTable = filesTable;
    }

    public Pane getOperationBar() {
        return operationBar;
    }

    public void setOperationBar(Pane operationBar) {
        this.operationBar = operationBar;
    }

    public MainMenuController getMainMenuController() {
        return mainMenuController;
    }

    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    public TargetSelectionController getTargetSelectionController() {
        return targetSelectionController;
    }

    public void setTargetSelectionController(TargetSelectionController targetSelectionController) {
        this.targetSelectionController = targetSelectionController;
    }

    public FilesTableController getFilesTableController() {
        return filesTableController;
    }

    public void setFilesTableController(FilesTableController filesTableController) {
        this.filesTableController = filesTableController;
    }

    public OperationController getOperationBarController() {
        return operationBarController;
    }

    public void setOperationBarController(OperationController operationBarController) {
        this.operationBarController = operationBarController;
    }

    public Button getPreviewButton() {
        return previewButton;
    }

    public void setPreviewButton(Button previewButton) {
        this.previewButton = previewButton;
    }

    public VBox getParaBox() {
        return paraBox;
    }

    public void setParaBox(VBox paraBox) {
        this.paraBox = paraBox;
    }

    public TextField getPreviewInput() {
        return previewInput;
    }

    public void setPreviewInput(TextField previewInput) {
        this.previewInput = previewInput;
    }

    public TextField getAcumFromInput() {
        return acumFromInput;
    }

    public void setAcumFromInput(TextField acumFromInput) {
        this.acumFromInput = acumFromInput;
    }

    public CheckBox getFillZero() {
        return fillZero;
    }

    public void setFillZero(CheckBox fillZero) {
        this.fillZero = fillZero;
    }

    public Pane getSourceSelection() {
        return sourceSelection;
    }

    public void setSourceSelection(Pane sourceSelection) {
        this.sourceSelection = sourceSelection;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
    }

    public String getFinalTargetName() {
        return finalTargetName;
    }

    public void setFinalTargetName(String finalTargetName) {
        this.finalTargetName = finalTargetName;
    }

    public String getTargetPathKey() {
        return targetPathKey;
    }

    public void setTargetPathKey(String targetPathKey) {
        this.targetPathKey = targetPathKey;
    }

    public String getCreatSubdirKey() {
        return creatSubdirKey;
    }

    public void setCreatSubdirKey(String creatSubdirKey) {
        this.creatSubdirKey = creatSubdirKey;
    }

    public String getFillZeroKey() {
        return fillZeroKey;
    }

    public void setFillZeroKey(String fillZeroKey) {
        this.fillZeroKey = fillZeroKey;
    }

    public String getPreviewKey() {
        return previewKey;
    }

    public void setPreviewKey(String previewKey) {
        this.previewKey = previewKey;
    }

    public String getSourcePathKey() {
        return sourcePathKey;
    }

    public void setSourcePathKey(String sourcePathKey) {
        this.sourcePathKey = sourcePathKey;
    }

    public String getAppendColorKey() {
        return appendColorKey;
    }

    public void setAppendColorKey(String appendColorKey) {
        this.appendColorKey = appendColorKey;
    }

    public String getAppendCompressionTypeKey() {
        return appendCompressionTypeKey;
    }

    public void setAppendCompressionTypeKey(String appendCompressionTypeKey) {
        this.appendCompressionTypeKey = appendCompressionTypeKey;
    }

    public String getAppendDensityKey() {
        return appendDensityKey;
    }

    public void setAppendDensityKey(String appendDensityKey) {
        this.appendDensityKey = appendDensityKey;
    }

    public String getAppendQualityKey() {
        return appendQualityKey;
    }

    public void setAppendQualityKey(String appendQualityKey) {
        this.appendQualityKey = appendQualityKey;
    }

    public String getAppendSizeKey() {
        return appendSizeKey;
    }

    public void setAppendSizeKey(String appendSizeKey) {
        this.appendSizeKey = appendSizeKey;
    }

    public TextField getSourceFileInput() {
        return sourceFileInput;
    }

    public void setSourceFileInput(TextField sourceFileInput) {
        this.sourceFileInput = sourceFileInput;
    }

    public TextField getTargetPathInput() {
        return targetPathInput;
    }

    public void setTargetPathInput(TextField targetPathInput) {
        this.targetPathInput = targetPathInput;
    }

    public TextField getTargetPrefixInput() {
        return targetPrefixInput;
    }

    public void setTargetPrefixInput(TextField targetPrefixInput) {
        this.targetPrefixInput = targetPrefixInput;
    }

    public TextField getTargetFileInput() {
        return targetFileInput;
    }

    public void setTargetFileInput(TextField targetFileInput) {
        this.targetFileInput = targetFileInput;
    }

    public TextField getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(TextField statusLabel) {
        this.statusLabel = statusLabel;
    }

    public CheckBox getSubdirCheck() {
        return subdirCheck;
    }

    public void setSubdirCheck(CheckBox subdirCheck) {
        this.subdirCheck = subdirCheck;
    }

    public CheckBox getAppendDensity() {
        return appendDensity;
    }

    public void setAppendDensity(CheckBox appendDensity) {
        this.appendDensity = appendDensity;
    }

    public CheckBox getAppendColor() {
        return appendColor;
    }

    public void setAppendColor(CheckBox appendColor) {
        this.appendColor = appendColor;
    }

    public CheckBox getAppendCompressionType() {
        return appendCompressionType;
    }

    public void setAppendCompressionType(CheckBox appendCompressionType) {
        this.appendCompressionType = appendCompressionType;
    }

    public CheckBox getAppendQuality() {
        return appendQuality;
    }

    public void setAppendQuality(CheckBox appendQuality) {
        this.appendQuality = appendQuality;
    }

    public CheckBox getAppendSize() {
        return appendSize;
    }

    public void setAppendSize(CheckBox appendSize) {
        this.appendSize = appendSize;
    }

    public BaseController getParentController() {
        return parentController;
    }

    public void setParentController(BaseController parentController) {
//        logger.debug(this.getClass());
//        logger.debug(parentController.getClass());
        this.parentController = parentController;
    }

    public TextField getDigitInput() {
        return digitInput;
    }

    public void setDigitInput(TextField digitInput) {
        this.digitInput = digitInput;
    }

    public boolean isTargetIsFile() {
        return targetIsFile;
    }

    public void setTargetIsFile(boolean targetIsFile) {
        this.targetIsFile = targetIsFile;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Label getBottomLabel() {
        return bottomLabel;
    }

    public void setBottomLabel(Label bottomLabel) {
        this.bottomLabel = bottomLabel;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Pane getDirsTable() {
        return dirsTable;
    }

    public void setDirsTable(Pane dirsTable) {
        this.dirsTable = dirsTable;
    }

    public DirectoriesTableController getDirsTableController() {
        return dirsTableController;
    }

    public void setDirsTableController(DirectoriesTableController dirsTableController) {
        this.dirsTableController = dirsTableController;
    }

}
