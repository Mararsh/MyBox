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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
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
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.FileExtensions;
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

    @FXML
    protected TitledPane filePane, framePane, viewPane, saveAsPane, editPane, browsePane;
    @FXML
    protected VBox panesBox, contentBox, fileBox, saveAsBox;
    @FXML
    protected FlowPane saveFramesPane, buttonsPane;
    @FXML
    protected ToggleGroup framesSaveGroup;
    @FXML
    protected ComboBox<String> frameSelector;
    @FXML
    protected Label framesLabel;
    @FXML
    protected Button nextFrameButton, previousFrameButton, playFramesButton;
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

            if (imageView != null && rightPane != null) {
                rightPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            if (loadWidthSelector != null) {
                List<String> values = Arrays.asList(message("OriginalSize"),
                        "512", "1024", "256", "128", "2048", "100", "80", "4096");
                loadWidthSelector.getItems().addAll(values);
                int v = UserConfig.getInt(baseName + "LoadWidth", defaultLoadWidth);
                if (v <= 0) {
                    loadWidth = -1;
                    loadWidthSelector.getSelectionModel().select(0);
                } else {
                    loadWidth = v;
                    loadWidthSelector.setValue(v + "");
                }
                loadWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (message("OriginalSize").equals(newValue)) {
                            loadWidth = -1;
                        } else {
                            try {
                                loadWidth = Integer.parseInt(newValue);
                            } catch (Exception e) {
                                ValidationTools.setEditorBadStyle(loadWidthSelector);
                                return;
                            }
                        }
                        ValidationTools.setEditorNormal(loadWidthSelector);
                        setLoadWidth();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                                frameSelector.getEditor().setStyle(UserConfig.badStyle());
                            } else {
                                frameSelector.getEditor().setStyle(null);
                                loadFrame(v - 1);
                            }
                        } catch (Exception e) {
                            frameSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initEditPane() {
        try {
            if (imageView == null) {
                return;
            }
            if (editPane != null) {
                editPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                editPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "EditPane", editPane.isExpanded());
                });
                editPane.setExpanded(UserConfig.getBoolean(baseName + "EditPane", false));
            }

            if (buttonsPane != null) {
                buttonsPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (tabPane != null) {
                tabPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (scrollPane != null) {
                scrollPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || imageView == null) {
                return false;
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
                saveAllFramesRadio.setSelected(true);
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
                nextFrameButton.setDisable(framesNumber < 2);
                previousFrameButton.setDisable(framesNumber < 2);
                playFramesButton.setDisable(framesNumber < 2);
                isSettingValues = false;
            }

            setFramePane();

            setFilesBrowse();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (imageView != null) {
                imageView.setImage(null);
            }
            alertInformation(message("NotSupported"));
            return false;
        }
    }

    public void setFramePane() {
        try {
            if (framePane == null || panesBox == null) {
                return;
            }
            if (sourceFile == null) {
                if (panesBox.getChildren().contains(framePane)) {
                    panesBox.getChildren().remove(framePane);
                }
                return;
            }
            String fileFormat = FileNameTools.suffix(sourceFile.getName()).toLowerCase();
            if (FileExtensions.MultiFramesImages.contains(fileFormat)) {
                if (!panesBox.getChildren().contains(framePane)) {
                    panesBox.getChildren().add(1, framePane);
                }
                framePane.setExpanded(true);
            } else {
                if (panesBox.getChildren().contains(framePane)) {
                    panesBox.getChildren().remove(framePane);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setFilesBrowse() {
        if (browseController != null) {
            browseController.setCurrentFile(imageFile());
        }
    }

    @FXML
    public void editFrames() {
        ImagesEditorController controller = (ImagesEditorController) openStage(Fxmls.ImagesEditorFxml);
        controller.open(sourceFile);
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
        File file = imageFile();
        if (file != null && file.exists() && framesNumber > 1) {
            formatController.formatPane.getChildren().setAll(formatController.tifRadio, formatController.gifRadio);
            if ("gif".equalsIgnoreCase(FileNameTools.suffix(file.getName()))) {
                formatController.gifRadio.setSelected(true);
            } else {
                formatController.tifRadio.setSelected(true);
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
    public void cropAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        boolean sizeChanged = imageWidth() != image.getWidth()
                || imageHeight() != image.getHeight();
        imageView.setImage(image);
        if (sizeChanged) {
            redrawMaskShape();
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
        if (task != null) {
            task.cancel();
        }
        File srcFile = imageFile();
        if (srcFile == null) {
            targetFile = chooseSaveFile();
            if (targetFile == null) {
                return;
            }
        } else {
            targetFile = srcFile;
        }
        if (imageInformation != null && imageInformation.isIsScaled()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("SureSaveScaled"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAsAction();
                return;
            }
        }

        task = new SingletonCurrentTask<Void>(this) {

            private Image savedImage;

            @Override
            protected boolean handle() {
                savedImage = imageToHandle();
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(savedImage, null);
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                if (backupController != null && backupController.needBackup() && srcFile != null) {
                    backupController.addBackup(task, srcFile);
                }
                String format = FileNameTools.suffix(targetFile.getName());
                if (framesNumber > 1) {
                    error = ImageFileWriters.writeFrame(targetFile, frameIndex, bufferedImage, targetFile, null);
                    ok = error == null;
                } else {
                    ok = ImageFileWriters.writeImageFile(bufferedImage, format, targetFile.getAbsolutePath());
                }
                if (!ok || task == null || isCancelled()) {
                    return false;
                }
                ImageFileInformation finfo = ImageFileInformation.create(targetFile);
                if (finfo == null || finfo.getImageInformation() == null) {
                    return false;
                }
                imageInformation = finfo.getImageInformation();
                return true;
            }

            @Override
            protected void whenSucceeded() {
                sourceFile = targetFile;
                recordFileWritten(sourceFile);
                if (srcFile == null) {
                    if (savedImage != imageView.getImage()) {
                        openFile(sourceFile);
                    } else {
                        sourceFileChanged(sourceFile);
                    }
                } else {
                    image = savedImage;
                    imageView.setImage(image);
                    popInformation(sourceFile + "   " + message("Saved"));
                    setImageChanged(false);
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null || imageView.getImage() == null
                || (saveAsButton != null && saveAsButton.isDisabled())) {
            return;
        }
        File srcFile = imageFile();
        String fname;
        if (srcFile != null) {
            fname = FileNameTools.filter(FileNameTools.prefix(srcFile.getName()))
                    + (framesNumber > 1 && (saveAllFramesRadio == null || !saveAllFramesRadio.isSelected())
                    ? "-" + message("Frame") + (frameIndex + 1) : "")
                    + "_" + DateTools.nowFileString();
        } else {
            fname = DateTools.nowFileString();
        }
        String targetFormat = ".png";
        if (formatController != null) {
            targetFormat = formatController.attributes.getImageFormat();
        } else if (fileTypeGroup != null) {
            targetFormat = ((RadioButton) fileTypeGroup.getSelectedToggle()).getText();
        }
        fname += "." + targetFormat;
        targetFile = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                fname, FileFilters.imageFilter(targetFormat));
        if (targetFile == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                Object imageToSave = imageToSaveAs();
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
                boolean multipleFrames = srcFile != null && framesNumber > 1 && saveAllFramesRadio != null && saveAllFramesRadio.isSelected();
                if (formatController != null) {
                    if (multipleFrames) {
                        error = ImageFileWriters.writeFrame(srcFile, frameIndex, bufferedImage, targetFile, formatController.attributes);
                        return error == null;
                    } else {
                        BufferedImage converted = ImageConvertTools.convertColorSpace(bufferedImage, formatController.attributes);
                        return ImageFileWriters.writeImageFile(converted, formatController.attributes, targetFile.getAbsolutePath());
                    }
                } else {
                    if (multipleFrames) {
                        error = ImageFileWriters.writeFrame(srcFile, frameIndex, bufferedImage, targetFile, null);
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

                afterSaveAs(targetFile);

            }
        };
        start(task);
    }

    public void afterSaveAs(File file) {
        if (saveAsType == SaveAsType.Load) {
            sourceFileChanged(file);

        } else if (saveAsType == SaveAsType.Open) {
            openFile(file);

        } else if (saveAsType == SaveAsType.Edit) {
            ImageManufactureController.openFile(file);

        }
    }

    @FXML
    @Override
    public void deleteAction() {
        File focusFile = null;
        if (browseController != null) {
            focusFile = browseController.nextFile(sourceFile);
            if (focusFile == null) {
                focusFile = browseController.previousFile(sourceFile);
            }
        }
        if (deleteFile(sourceFile)) {
            sourceFile = null;
            image = null;
            imageView.setImage(null);
            if (focusFile != null) {
                sourceFileChanged(focusFile);
            }
        }
    }

    public boolean deleteFile(File sfile) {
        if (sfile == null) {
            return false;
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
        }
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
            controller.set(sourceFile);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(newFile);
                });
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            if (browseController != null) {
                browseController.setCurrentFile(sourceFile);
            }
            notifyLoad();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    @Override
    protected void popImageMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageViewController.imageViewMenu(this, x, y);
    }

    @FXML
    @Override
    public boolean menuAction() {
        Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
        MenuImageViewController.imageViewMenu(this, localToScreen.getX(), localToScreen.getY());
        return true;
    }

    public boolean scopeWhole() {
        return scope == null || scope.getScopeType() == ImageScope.ScopeType.All;
    }


    /*
        static methods
     */
    public static ImageViewerController open() {
        try {
            ImageViewerController controller = (ImageViewerController) WindowTools.openStage(Fxmls.ImageViewerFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageViewerController openFile(File file) {
        try {
            ImageViewerController controller = open();
            if (controller != null && file != null) {
                controller.loadImageFile(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageViewerController openImage(Image image) {
        ImageViewerController controller = open();
        if (controller != null) {
            controller.loadImage(image);
        }
        return controller;
    }

    public static ImageViewerController openImageInfo(ImageInformation info) {
        ImageViewerController controller = open();
        if (controller != null) {
            controller.loadImageInfo(info);
        }
        return controller;
    }

}
