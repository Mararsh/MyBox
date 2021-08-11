package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import mara.mybox.tools.FileTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends BaseImageController {

    protected ImageScope scope;
    protected File nextFile, previousFile;
    protected FileSortMode sortMode;

    @FXML
    protected TitledPane filePane, framePane, viewPane, saveAsPane, editPane, browsePane;
    @FXML
    protected VBox panesBox, contentBox, fileBox, saveAsBox;
    @FXML
    protected FlowPane saveFramesPane;
    @FXML
    protected CheckBox deleteConfirmCheck, saveConfirmCheck;
    @FXML
    protected ToggleGroup sortGroup, framesSaveGroup;
    @FXML
    protected ComboBox<String> frameSelector;
    @FXML
    protected Label framesLabel;
    @FXML
    protected RadioButton saveAllFramesRadio;
    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected ControlFileBackup backupController;

    public ImageViewerController() {
        baseTitle = message("ImageViewer");
        TipsLabelKey = "ImageViewerTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initFilePane();
            initFramePane();
            initViewPane();
            initSaveAsPane();
            initEditPane();
            initBrowsePane();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFilePane() {
        try {
            if (fileBox != null && imageView != null) {
                fileBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (saveButton != null && imageView != null) {
                saveButton.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

            loadWidth = defaultLoadWidth;
            if (loadWidthBox != null) {
                List<String> values = Arrays.asList(message("OriginalSize"),
                        "512", "1024", "256", "128", "2048", "100", "80", "4096");
                loadWidthBox.getItems().addAll(values);
                int v = UserConfig.getInt(baseName + "LoadWidth", defaultLoadWidth);
                if (v <= 0) {
                    loadWidth = -1;
                    loadWidthBox.getSelectionModel().select(0);
                } else {
                    loadWidth = v;
                    loadWidthBox.setValue(v + "");
                }
                loadWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (message("OriginalSize").equals(newValue)) {
                            loadWidth = -1;
                        } else {
                            try {
                                loadWidth = Integer.valueOf(newValue);
                                ValidationTools.setEditorNormal(loadWidthBox);
                            } catch (Exception e) {
                                ValidationTools.setEditorBadStyle(loadWidthBox);
                                return;
                            }
                        }
                        UserConfig.setInt(baseName + "LoadWidth", loadWidth);
                        setLoadWidth();
                    }
                });
            }

            if (deleteConfirmCheck != null) {
                deleteConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ConfirmDelete", deleteConfirmCheck.isSelected());
                    }
                });
                deleteConfirmCheck.setSelected(UserConfig.getBoolean(baseName + "ConfirmDelete", true));
            }

            if (saveConfirmCheck != null) {
                saveConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ConfirmSave", saveConfirmCheck.isSelected());
                    }
                });
                saveConfirmCheck.setSelected(UserConfig.getBoolean(baseName + "ConfirmSave", true));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFramePane() {
        try {
            if (framePane == null) {
                return;
            }
            if (imageView != null) {
                framePane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (frameSelector != null) {
                frameSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            if (isSettingValues) {
                                return;
                            }
                            int v = Integer.parseInt(frameSelector.getValue());
                            if (v < 1 || v > framesNumber) {
                                frameSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                            } else {
                                frameSelector.getEditor().setStyle(null);
                                loadFrame(v - 1);
                            }
                        } catch (Exception e) {
                            frameSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initViewPane() {
        try {
            if (viewPane != null) {
                if (imageView != null) {
                    viewPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                viewPane.setExpanded(UserConfig.getBoolean(baseName + "ViewPane", false));
                viewPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "ViewPane", viewPane.isExpanded());
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSaveAsPane() {
        try {
            if (saveAsPane != null) {
                if (imageView != null) {
                    saveAsPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                saveAsPane.setExpanded(UserConfig.getBoolean(baseName + "SaveAsPane", false));
                saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "SaveAsPane", saveAsPane.isExpanded());
                });
            }

            if (formatController != null) {
                formatController.setParameters(this, false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBrowsePane() {
        try {
            if (browsePane != null) {
                if (imageView != null) {
                    browsePane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                browsePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "BrowsePane", browsePane.isExpanded());
                });
                browsePane.setExpanded(UserConfig.getBoolean(baseName + "BrowsePane", false));
            }

            if (previousButton != null) {
                previousButton.setDisable(imageFile() == null);
            }
            if (nextButton != null) {
                nextButton.setDisable(imageFile() == null);
            }

            String saveMode = UserConfig.getString(baseName + "SortMode",
                    FileSortMode.NameAsc.name());
            sortMode = FileSortTools.sortMode(saveMode);
            if (sortGroup != null) {
                sortGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                    if (newValue == null || isSettingValues) {
                        return;
                    }
                    String selected = ((RadioButton) newValue).getText();
                    for (FileSortMode mode : FileSortMode.values()) {
                        if (message(mode.name()).equals(selected)) {
                            sortMode = mode;
                            break;
                        }
                    }
                    UserConfig.setString(baseName + "SortMode", sortMode.name());
                    makeImageNevigator();
                });
                for (Toggle toggle : sortGroup.getToggles()) {
                    RadioButton button = (RadioButton) toggle;
                    if (button.getText().equals(message(saveMode))) {
                        isSettingValues = true;
                        button.fire();
                        isSettingValues = false;
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEditPane() {
        try {
            if (editPane != null) {
                editPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                editPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "EditPane", editPane.isExpanded());
                });
                editPane.setExpanded(UserConfig.getBoolean(baseName + "EditPane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkSaveAs() {

    }

    @Override
    public void afterInfoLoaded() {
        super.afterInfoLoaded();
        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.setDisable(imageFile() == null);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            if (imageView == null) {
                return true;
            }
            if (saveAsBox != null && saveFramesPane != null) {
                if (framesNumber <= 1) {
                    if (saveAsBox.getChildren().contains(saveFramesPane)) {
                        saveAsBox.getChildren().remove(saveFramesPane);
                    }

                } else {
                    if (!saveAsBox.getChildren().contains(saveFramesPane)) {
                        saveAsBox.getChildren().add(0, saveFramesPane);
                    }
                }
            }
            if (saveAllFramesRadio != null) {
                saveAllFramesRadio.fire();
                saveAllFramesSelected();
            }
            if (framesLabel != null) {
                framesLabel.setText("/" + framesNumber);
            }
            if (frameSelector != null) {
                List<String> frames = new ArrayList<>();
                for (int i = 1; i <= framesNumber; i++) {
                    frames.add(i + "");
                }
                isSettingValues = true;
                frameSelector.getItems().setAll(frames);
                frameSelector.setValue((frameIndex + 1) + "");
                isSettingValues = false;
            }
            if (panesBox != null && framePane != null) {
                if (framesNumber <= 1) {
                    if (panesBox.getChildren().contains(framePane)) {
                        panesBox.getChildren().remove(framePane);
                    }
                } else {
                    if (!panesBox.getChildren().contains(framePane)) {
                        panesBox.getChildren().add(1, framePane);
                        framePane.setExpanded(true);
                    }
                }
            }

            if (imageFile() != null && nextButton != null) {
                makeImageNevigator();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            if (imageView != null) {
                imageView.setImage(null);
            }
            alertInformation(message("NotSupported"));
            return false;
        }
    }

    public void makeImageNevigator() {
        makeImageNevigator(imageFile());
    }

    public void makeImageNevigator(File currentfile) {
        try {
            if (currentfile == null) {
                previousFile = null;
                previousButton.setDisable(true);
                nextFile = null;
                nextButton.setDisable(true);
                return;
            }
            File path = currentfile.getParentFile();
            List<File> pathFiles = new ArrayList<>();
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        pathFiles.add(file);
                    }
                }
                FileSortTools.sortFiles(pathFiles, sortMode);

                for (int i = 0; i < pathFiles.size(); ++i) {
                    if (pathFiles.get(i).getAbsoluteFile().equals(currentfile.getAbsoluteFile())) {
                        if (i < pathFiles.size() - 1) {
                            nextFile = pathFiles.get(i + 1);
                            nextButton.setDisable(false);
                        } else {
                            nextFile = null;
                            nextButton.setDisable(true);
                        }
                        if (i > 0) {
                            previousFile = pathFiles.get(i - 1);
                            previousButton.setDisable(false);
                        } else {
                            previousFile = null;
                            previousButton.setDisable(true);
                        }
                        return;
                    }
                }
            }
            previousFile = null;
            previousButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void nextFrame() {
        loadFrame(frameIndex + 1);
    }

    @FXML
    public void previousFrame() {
        loadFrame(frameIndex - 1);
    }

    @FXML
    public void saveAllFramesSelected() {
        if (imageFile() != null && framesNumber > 1) {
            formatController.formatPane.getChildren().setAll(formatController.tifRadio, formatController.gifRadio);
            if ("gif".equalsIgnoreCase(FileNameTools.getFileSuffix(imageFile()))) {
                formatController.gifRadio.fire();
            } else {
                formatController.tifRadio.fire();
            }
        } else {
            formatController.formatPane.getChildren().setAll(formatController.pngRadio, formatController.jpgRadio,
                    formatController.tifRadio, formatController.gifRadio,
                    formatController.pcxRadio, formatController.pnmRadio,
                    formatController.bmpRadio, formatController.wbmpRadio, formatController.icoRadio);
        }
    }

    @FXML
    public void saveCurrentFramesSelected() {
        formatController.formatPane.getChildren().setAll(formatController.pngRadio, formatController.jpgRadio,
                formatController.tifRadio, formatController.gifRadio,
                formatController.pcxRadio, formatController.pnmRadio,
                formatController.bmpRadio, formatController.wbmpRadio, formatController.icoRadio);
    }

    @FXML
    @Override
    public void nextAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (nextFile != null) {
            loadImageFile(nextFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (previousFile != null) {
            loadImageFile(previousFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @FXML
    @Override
    public void cropAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private Image areaImage;

                    @Override
                    protected boolean handle() {
                        areaImage = imageToHandle();
                        return areaImage != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        imageView.setImage(areaImage);
                        setImageChanged(true);
                        resetMaskControls();
                    }

                };
                handling(task);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        boolean sizeChanged = getImageWidth() != image.getWidth()
                || getImageHeight() != image.getHeight();
        imageView.setImage(image);
        if (sizeChanged) {
            resetMaskControls();
        }
        setImageChanged(false);
        popInformation(message("Recovered"));
    }

    @FXML
    @Override
    public void saveAction() {
        if (imageView == null || imageView.getImage() == null
                || (saveButton != null && saveButton.isDisabled())) {
            return;
        }
        File imageFile = imageFile();
        if (imageFile == null) {
            saveAsAction();
            return;
        }
        try {
            String ask = null;
            if (imageInformation != null && imageInformation.isIsScaled()) {
                ask = message("SureSaveScaled");
            } else if (saveConfirmCheck != null && saveConfirmCheck.isSelected()) {
                ask = message("SureOverrideFile");
            }
            if (ask != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(ask);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSave = new ButtonType(message("Save"));
                ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
                ButtonType buttonCancel = new ButtonType(message("Cancel"));
                alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonCancel) {
                    return;
                } else if (result.get() == buttonSaveAs) {
                    saveAsAction();
                    return;
                }
            }

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private Image targetImage;

                    @Override
                    protected boolean handle() {
                        Object imageToSave = imageToSave();
                        if (imageToSave == null) {
                            return false;
                        }
                        BufferedImage bufferedImage;
                        if (imageToSave instanceof Image) {
                            targetImage = (Image) imageToSave;
                            bufferedImage = SwingFXUtils.fromFXImage(targetImage, null);
                        } else if (imageToSave instanceof BufferedImage) {
                            bufferedImage = (BufferedImage) imageToSave;
                            targetImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        } else {
                            return false;
                        }
                        if (bufferedImage == null || task == null || isCancelled()) {
                            return false;
                        }
                        if (backupController != null && backupController.isBack()) {
                            backupController.addBackup(imageFile);
                        }
                        String format = FileNameTools.getFileSuffix(imageFile.getName());
                        if (framesNumber > 1) {
                            error = ImageFileWriters.writeFrame(imageFile, frameIndex, bufferedImage, imageFile, null);
                            ok = error == null;
                        } else {
                            ok = ImageFileWriters.writeImageFile(bufferedImage, format, imageFile.getAbsolutePath());
                        }
                        if (!ok || task == null || isCancelled()) {
                            return false;
                        }
                        ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(imageFile);
                        if (finfo == null || finfo.getImageInformation() == null) {
                            return false;
                        }
                        imageInformation = finfo.getImageInformation();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        image = targetImage;
                        imageView.setImage(image);
                        popInformation(imageFile + "   " + message("Saved"));
                        setImageChanged(false);
                    }

                };
                handling(task);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public String saveAsPrefix() {
        String name;
        if (imageFile() != null) {
            name = FileNameTools.prefixFilter(imageFile())
                    + (framesNumber > 1 && (saveAllFramesRadio == null || !saveAllFramesRadio.isSelected())
                    ? "-" + message("Frame") + (frameIndex + 1) : "")
                    + "_" + DateTools.nowFileString();
        } else {
            name = DateTools.nowFileString();
        }
        if (formatController != null) {
            name += "." + formatController.attributes.getImageFormat();
        } else if (fileTypeGroup != null) {
            name += "." + ((RadioButton) fileTypeGroup.getSelectedToggle()).getText();
        }
        return name;
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null || imageView.getImage() == null
                || (saveAsButton != null && saveAsButton.isDisabled())) {
            return;
        }
        targetFile = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                saveAsPrefix(), formatController == null ? FileFilters.ImageExtensionFilter : null);
        if (targetFile == null) {
            return;
        }
        File imageFile = imageFile();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    Object imageToSave = imageToSave();
                    if (imageToSave == null) {
                        return false;
                    }
                    BufferedImage bufferedImage;
                    if (imageToSave instanceof Image) {
                        bufferedImage = SwingFXUtils.fromFXImage((Image) imageToSave, null);
                    } else if (imageToSave instanceof BufferedImage) {
                        bufferedImage = (BufferedImage) imageToSave;
                    } else {
                        return false;
                    }
                    if (bufferedImage == null || task == null || isCancelled()) {
                        return false;
                    }
                    boolean multipleFrames = imageFile != null && framesNumber > 1 && saveAllFramesRadio != null && saveAllFramesRadio.isSelected();
                    if (formatController != null) {
                        if (multipleFrames) {
                            error = ImageFileWriters.writeFrame(imageFile, frameIndex, bufferedImage, targetFile, formatController.attributes);
                            return error == null;
                        } else {
                            BufferedImage converted = ImageConvertTools.convertColorSpace(bufferedImage, formatController.attributes);
                            return ImageFileWriters.writeImageFile(converted, formatController.attributes, targetFile.getAbsolutePath());
                        }
                    } else {
                        if (multipleFrames) {
                            error = ImageFileWriters.writeFrame(imageFile, frameIndex, bufferedImage, targetFile, null);
                            return error == null;
                        } else {
                            return ImageFileWriters.writeImageFile(bufferedImage, targetFile);
                        }
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(targetFile);

                    if (saveAsType == SaveAsType.Load) {
                        sourceFileChanged(targetFile);

                    } else if (saveAsType == SaveAsType.Open) {
                        ControllerTools.openImageViewer(targetFile);

                    }
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        if (deleteFile(sourceFile)) {
            sourceFile = null;
            image = null;
            imageView.setImage(null);
            if (nextFile != null) {
                nextAction();
            } else if (previousFile != null) {
                previousAction();
            } else {
                if (previousButton != null) {
                    previousButton.setDisable(true);
                }
                if (nextButton != null) {
                    nextButton.setDisable(true);
                }
            }
        }
    }

    public boolean deleteFile(File sfile) {
        if (sfile == null) {
            return false;
        }
        if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
            if (!PopTools.askSure(getMyStage().getTitle(), message("SureDelete"))) {
                return false;
            }
        }
        if (FileDeleteTools.delete(sfile)) {
            popSuccessful();
            return true;
        } else {
            popFailed();
            return false;
        }
    }

    public void changeFile(ImageInformation info, File file) {
        if (info == null || file == null) {
            return;
        }
        ImageFileInformation finfo = info.getImageFileInformation();
        if (finfo != null) {
            finfo.setFile(file);
            finfo.setFileName(file.getAbsolutePath());
        }
        info.setFileName(file.getAbsolutePath());
        info.setFile(file);
    }

    @FXML
    public void renameAction() {
        try {
            if (imageChanged) {
                saveAction();
            }
            if (sourceFile == null) {
                return;
            }
            FileRenameController controller = (FileRenameController) openStage(Fxmls.FileRenameFxml);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(newFile);
                });
            });
            controller.set(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    public void fileRenamed(File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            popSuccessful();
            sourceFile = newFile;
            recordFileOpened(sourceFile);
            changeFile(imageInformation, newFile);
            updateLabelsTitle();
            makeImageNevigator();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @Override
    protected void popImageMenu(double x, double y) {
        if (!UserConfig.getBoolean(baseName + "ContextMenu", true)
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageViewController.open(this, x, y);
    }

    @FXML
    @Override
    public void menuAction() {
        Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
        MenuImageViewController.open(this, localToScreen.getX(), localToScreen.getY());
    }

}
