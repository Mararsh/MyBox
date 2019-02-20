package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
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
import javax.imageio.ImageIO;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchController extends ImageBaseController {

    protected int targetExistType;
    protected ObservableList<String> generatedFiles;
    protected String fileType, errorString, targetFormat;

    @FXML
    protected TableView<FileInformation> sourceTable;
    @FXML
    protected TableColumn<FileInformation, String> handledColumn, fileColumn, modifyTimeColumn, sizeColumn, createTimeColumn;
    @FXML
    protected ToggleGroup targetExistGroup, fileTypeGroup, alphaGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio, pcxRadio;
    @FXML
    protected TextField targetSuffixInput;
    @FXML
    protected Button addButton, upButton, downButton, clearButton, browseButton, openButton, insertButton;
    @FXML
    protected RadioButton blackRadio, whiteRadio;

    protected static class TargetExistType {

        public static int Rename = 0;
        public static int Replace = 1;
        public static int Skip = 2;
    }

    public ImageManufactureBatchController() {
    }

    @Override
    protected void initializeNext() {

        generatedFiles = FXCollections.observableArrayList();

        initSourceSection();
        initOptionsSection();
        initTargetSection();

        initializeNext2();

    }

    @Override
    protected void initializeNext2() {
        try {

        } catch (Exception e) {
            logger.debug(e.toString());
        }
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

    private void checkTableSelected() {
        ObservableList<Integer> selected = sourceTable.getSelectionModel().getSelectedIndices();
        boolean none = (selected == null || selected.isEmpty());
        insertButton.setDisable(none);
        openButton.setDisable(none);
        upButton.setDisable(none);
        downButton.setDisable(none);
        deleteButton.setDisable(none);
    }

    protected void initOptionsSection() {
        if (alphaGroup != null) {
            alphaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkAlpha();
                }
            });
            checkAlpha();
        }

    }

    protected void checkAlpha() {
        if (alphaGroup == null) {
            return;
        }
        RadioButton selected = (RadioButton) alphaGroup.getSelectedToggle();
        if (getMessage("ReplaceAlphaAsBlack").equals(selected.getText())) {
            AppVaribles.setUserConfigValue("AlphaAsBlack", true);
        } else {
            AppVaribles.setUserConfigValue("AlphaAsBlack", false);
        }
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

        Tooltip tips = new Tooltip(getMessage("PcxComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(pcxRadio, tips);

        fileTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFileType();
            }
        });
        checkFileType();

        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
        );

        tips = new Tooltip(getMessage("PreviewComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(previewButton, tips);

        previewButton.disableProperty().bind(
                operationBarController.startButton.disableProperty()
                        .or(operationBarController.startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
        );

        browseButton.setDisable(true);
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

    protected void checkFileType() {
        RadioButton selected = (RadioButton) fileTypeGroup.getSelectedToggle();
        if (getMessage("OriginalType").equals(selected.getText())) {
            fileType = null;
        } else {
            fileType = selected.getText();
        }
    }

    @FXML
    protected void addAction(ActionEvent event) {
        addAction(sourceFilesInformation.size());
    }

    @FXML
    void insertAction(ActionEvent event) {
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
            sourceTable.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void deleteAction() {
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
            openImageViewer(info.getFile().getAbsolutePath());
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

    @FXML
    protected void browseAction() {
        try {
            if (generatedFiles == null || generatedFiles.isEmpty()) {
                return;
            }
            final ImagesBrowserController controller = FxmlStage.openImagesBrowser(getClass(), null);
            if (controller != null) {
                controller.loadImages(generatedFiles);
            }
        } catch (Exception e) {
        }
    }

    @FXML
    protected void mouseEnterPane(MouseEvent event) {
        if (blackRadio == null || whiteRadio == null) {
            return;
        }
        if (AppVaribles.isAlphaAsBlack()) {
            blackRadio.setSelected(true);
        } else {
            whiteRadio.setSelected(true);
        }
    }

    @Override
    protected void openTarget(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new File(targetPathInput.getText()).toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void makeMoreParameters() {
        actualParameters.isBatch = true;

        if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
            actualParameters = null;
            return;
        }
        sourceFiles = new ArrayList();
        if (isPreview) {
            sourceFiles.add(sourceFilesInformation.get(0).getFile());
        } else {
            for (FileInformation f : sourceFilesInformation) {
                sourceFiles.add(f.getFile());
            }
        }
        actualParameters.targetPath = targetPathInput.getText();
    }

    @Override
    protected void doCurrentProcess() {
        try {
            if (currentParameters == null) {
                return;
            }
            generatedFiles = FXCollections.observableArrayList();
            browseButton.setDisable(true);
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
                            if (generatedFiles.size() > 0) {
                                browseButton.setDisable(false);
                            }

                            currentParameters.currentIndex++;
                            updateProgress(currentParameters.currentIndex, sourceFiles.size());
                            updateMessage(currentParameters.currentIndex + "/" + sourceFiles.size());
                            currentParameters.currentTotalHandled++;

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
                                browseAction();
//                                try {
//                                    Desktop.getDesktop().browse(new File(actualParameters.targetPath).toURI());
//                                } catch (Exception e) {
//                                    logger.error(e.toString());
//                                }
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

    protected String handleCurrentFile() {
        try {
            BufferedImage source = ImageIO.read(currentParameters.sourceFile);
            targetFormat = fileType;
            if (targetFormat == null) {
                targetFormat = FileTools.getFileSuffix(currentParameters.sourceFile.getName());
            }
            BufferedImage target = handleImage(source);
            if (target == null) {
                if (errorString != null) {
                    return errorString;
                } else {
                    return AppVaribles.getMessage("Failed");
                }
            }
            ImageFileWriters.writeImageFile(target, targetFormat, currentParameters.finalTargetName);
            generatedFiles.add(currentParameters.finalTargetName);
            return AppVaribles.getMessage("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVaribles.getMessage("Failed");
        }
    }

    protected BufferedImage handleImage(BufferedImage source) {
        return null;
    }

    protected void markFileHandled(int index, String msg) {
        FileInformation d = sourceFilesInformation.get(index);
        if (d == null) {
            return;
        }
        d.setHandled(msg);
        sourceTable.refresh();
    }

}
