package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static mara.mybox.controller.BaseController.openImageViewer;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageClipboard;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageScope;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTools.FileSortMode;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageMaskController {

    protected ImageScope scope;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;
    protected FileSortMode sortMode;

    @FXML
    protected TitledPane filePane, viewPane, saveAsPane, editPane, browsePane, tipsPane;
    @FXML
    protected VBox contentBox, fileBox;
    @FXML
    protected HBox operationBox;
    @FXML
    public Button moveUpButton, moveDownButton, manufactureButton, statisticButton, splitButton,
            sampleButton, browseButton;
    @FXML
    protected CheckBox selectAreaCheck, deleteConfirmCheck, saveConfirmCheck;
    @FXML
    protected ToggleGroup saveAsGroup, sortGroup;
    @FXML
    protected RadioButton saveLoadRadio, saveOpenRadio, saveJustRadio;
    @FXML
    protected ComboBox<String> loadWidthBox;
    @FXML
    protected Button pickColorButton;

    public ImageViewerController() {
        baseTitle = message("ImageViewer");

    }

    @Override
    public void initializeNext() {
        try {
            initFilePane();
            initViewPane();
            initSaveAsPane();
            initEditPane();
            initBrowsePane();
            initTipsPane();
            initOperationBox();
            initImageView();
            initMaskPane();
            initRulersCheck();

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
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
                List<String> values = Arrays.asList(AppVariables.message("OriginalSize"),
                        "512", "1024", "256", "128", "2048", "100", "80", "4096");
                loadWidthBox.getItems().addAll(values);
                loadWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
//                    logger.debug(oldValue + " " + newValue + " " + (String) loadWidthBox.getSelectionModel().getSelectedItem());
                        if (AppVariables.message("OriginalSize").equals(newValue)) {
                            loadWidth = -1;
                        } else {
                            try {
                                loadWidth = Integer.valueOf(newValue);
                                FxmlControl.setEditorNormal(loadWidthBox);
                            } catch (Exception e) {
                                FxmlControl.setEditorBadStyle(loadWidthBox);
                                return;
                            }
                        }
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigInt(baseName + "LoadWidth", loadWidth);
                        if (!isSettingValues) {
                            setLoadWidth();
                        }
                    }
                });

                isSettingValues = true;
                int v = AppVariables.getUserConfigInt(baseName + "LoadWidth", defaultLoadWidth);
                if (v <= 0) {
                    loadWidthBox.getSelectionModel().select(0);
                } else {
                    loadWidthBox.getSelectionModel().select(v + "");
                }
                isSettingValues = false;
                FxmlControl.setTooltip(loadWidthBox, new Tooltip(AppVariables.message("ImageLoadWidthCommnets")));
            }

            if (deleteConfirmCheck != null) {
                deleteConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("ImageConfirmDelete", deleteConfirmCheck.isSelected());
                    }
                });
                deleteConfirmCheck.setSelected(AppVariables.getUserConfigBoolean("ImageConfirmDelete", true));
            }

            if (saveConfirmCheck != null) {
                saveConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("ImageConfirmSave", saveConfirmCheck.isSelected());
                    }
                });
                saveConfirmCheck.setSelected(AppVariables.getUserConfigBoolean("ImageConfirmSave", true));
            }

            if (manufactureButton != null) {
                manufactureButton.setDisable(true);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initViewPane() {
        try {
            if (viewPane != null) {
                if (imageView != null) {
                    viewPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                viewPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "ViewPane", viewPane.isExpanded());
                });
                viewPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "ViewPane", false));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initSaveAsPane() {
        try {
            if (saveAsPane != null) {
                if (imageView != null) {
                    saveAsPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "SaveAsPane", saveAsPane.isExpanded());
                });
                saveAsPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "SaveAsPane", false));

            }

            if (saveAsGroup != null) {
                String v = AppVariables.getUserConfigValue("ImageSaveAsType", SaveAsType.Open.name());
                for (SaveAsType s : SaveAsType.values()) {
                    if (v.equals(s.name())) {
                        saveAsType = s;
                        break;
                    }
                }
                if (saveAsType == null
                        || (saveLoadRadio == null && saveAsType == SaveAsType.Load)) {
                    saveAsType = SaveAsType.Open;
                }
                switch (saveAsType) {
                    case Load:
                        saveLoadRadio.setSelected(true);
                        break;
                    case Open:
                        saveOpenRadio.setSelected(true);
                        break;
                    case None:
                        saveJustRadio.setSelected(true);
                        break;
                    default:
                        break;
                }
                saveAsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        if (saveOpenRadio.isSelected()) {
                            saveAsType = SaveAsType.Open;
                        } else if (saveJustRadio.isSelected()) {
                            saveAsType = SaveAsType.None;
                        } else if (saveLoadRadio != null && saveLoadRadio.isSelected()) {
                            saveAsType = SaveAsType.Load;
                        } else {
                            saveAsType = SaveAsType.Open;
                        }
                        AppVariables.setUserConfigValue("ImageSaveAsType", saveAsType.name());
                    }
                });
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initBrowsePane() {
        try {
            if (browsePane != null) {
                if (imageView != null) {
                    browsePane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                browsePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "BrowsePane", browsePane.isExpanded());
                });
                browsePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "BrowsePane", false));
            }

            if (previousButton != null) {
                previousButton.setDisable(sourceFile == null);
            }
            if (nextButton != null) {
                nextButton.setDisable(sourceFile == null);
            }

            sortMode = FileTools.FileSortMode.ModifyTimeDesc;
            if (sortGroup != null) {
                sortGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                            if (newValue == null) {
                                return;
                            }
                            String selected = ((RadioButton) newValue).getText();
                            for (FileSortMode mode : FileSortMode.values()) {
                                if (message(mode.name()).equals(selected)) {
                                    sortMode = mode;
                                    break;
                                }
                            }
                            if (!isSettingValues) {
                                makeImageNevigator();
                            }
                        });
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initEditPane() {
        try {
            if (editPane != null) {
                if (imageView != null) {
                    editPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                editPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "EditPane", editPane.isExpanded());
                });
                editPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "EditPane", false));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initTipsPane() {
        if (tipsPane != null) {
            tipsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                AppVariables.setUserConfigValue(baseName + "TipsPane", tipsPane.isExpanded());
            });
            tipsPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "TipsPane", false));
        }
    }

    protected void initOperationBox() {
        if (imageView != null) {
            if (operationBox != null) {
                operationBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (leftPaneControl != null) {
                leftPaneControl.visibleProperty().bind(Bindings.isNotNull(imageView.imageProperty()));
            }
            if (rightPaneControl != null) {
                rightPaneControl.visibleProperty().bind(Bindings.isNotNull(imageView.imageProperty()));
            }
        }

        if (selectAreaCheck != null) {
            selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("ImageSelect", selectAreaCheck.isSelected());
                    checkSelect();
                }
            });
            selectAreaCheck.setSelected(AppVariables.getUserConfigBoolean("ImageSelect", false));
            checkSelect();
            FxmlControl.setTooltip(selectAreaCheck, new Tooltip("CTRL+t"));
        }

    }

    protected void setAsPopped() {
        controlLeftPane();
        myStage.sizeToScene();
        myStage.centerOnScreen();
        topCheck.setVisible(true);
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "t":
                case "T":
                    if (selectAreaCheck != null) {
                        selectAreaCheck.setSelected(!selectAreaCheck.isSelected());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    protected void checkSaveAs() {

    }

    protected void checkSelect() {
        if (cropButton != null) {
            cropButton.setDisable(!selectAreaCheck.isSelected());
        }
        if (selectAllButton != null) {
            selectAllButton.setDisable(!selectAreaCheck.isSelected());
        }

        if (selectAreaCheck != null) {
            initMaskRectangleLine(selectAreaCheck.isSelected());
        }
        updateLabelTitle();
    }

    protected void setLoadWidth() {
        careFrames = false;
        if (sourceFile != null) {
            loadImage(sourceFile, loadWidth);
        } else if (imageView.getImage() != null) {
            loadImage(imageView.getImage(), loadWidth);
        } else if (image != null) {
            loadImage(image, loadWidth);
        }
        if (imageInformation != null) {
            setImageChanged(imageInformation.isIsScaled());
        } else {
            setImageChanged(false);
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        careFrames = true;
        loadImage(file, loadWidth);
    }

    @Override
    public void afterInfoLoaded() {
        if (infoButton != null) {
            infoButton.setDisable(imageInformation == null);
        }
        if (metaButton != null) {
            metaButton.setDisable(imageInformation == null);
        }
        if (statisticButton != null) {
            statisticButton.setDisable(image == null);
        }
        if (deleteButton != null) {
            deleteButton.setDisable(sourceFile == null);
        }
        if (renameButton != null) {
            renameButton.setDisable(sourceFile == null);
        }
        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.setDisable(sourceFile == null);
        }
        if (previousButton != null) {
            previousButton.setDisable(sourceFile == null);
        }
        if (nextButton != null) {
            nextButton.setDisable(sourceFile == null);
        }
        if (manufactureButton != null) {
            manufactureButton.setDisable(imageInformation == null && image == null);
        }

    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            afterInfoLoaded();
            if (image == null || imageView == null) {
                return;
            }

            imageView.setPreserveRatio(true);
            imageView.setImage(image);
            imageChanged = isCroppped = false;
            xZoomStep = (int) image.getWidth() / 10;
            yZoomStep = (int) image.getHeight() / 10;
            careFrames = true;

            if (sourceFile != null && nextButton != null) {
                makeImageNevigator();
            }
            fitSize();

            setMaskStroke();

            if (selectAreaCheck != null) {
                checkSelect();
            }

            if (imageInformation == null) {
                setImageChanged(true);
            } else if (!imageInformation.isIsSampled()) {
                setImageChanged(imageInformation.isIsScaled());
            }

            if (imageInformation != null && imageInformation.isIsSampled()) {
                if (sampledTips != null) {
                    sampledTips.setVisible(true);
                    FxmlControl.setTooltip(sampledTips, new Tooltip(getSmapledInfo()));
                }
                loadWidth = (int) image.getWidth();
                loadSampledImage();
            } else {
                if (sampledTips != null) {
                    sampledTips.setVisible(false);
                }
            }

            if (loadWidthBox != null) {
                isSettingValues = true;
                if (loadWidth == -1) {
                    loadWidthBox.getSelectionModel().select(AppVariables.message("OriginalSize"));
                } else {
                    loadWidthBox.getSelectionModel().select(loadWidth + "");
                }
                isSettingValues = false;
            }

            refinePane();

        } catch (Exception e) {
            logger.error(e.toString());
            imageView.setImage(null);
            alertInformation(AppVariables.message("NotSupported"));
        }
    }

    protected String getSmapledInfo() {
        Map<String, Long> sizes = imageInformation.getSizes();
        if (sizes == null) {
            return "";
        }
        int sampledSize = (int) (image.getWidth() * image.getHeight() * imageInformation.getColorChannels() / (1014 * 1024));
        String msg = MessageFormat.format(message("ImageTooLarge"),
                imageInformation.getWidth(), imageInformation.getHeight(), imageInformation.getColorChannels(),
                sizes.get("pixelsSize"), sizes.get("requiredMem"), sizes.get("availableMem"),
                (int) image.getWidth(), (int) image.getHeight(), sampledSize);
        return msg;
    }

    protected void loadSampledImage() {
        if (sampledTips != null) {
            sampledTips.setOnMouseMoved(null);
            sampledTips.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    popSampleInformation(getSmapledInfo());
                }
            });
        }

        popSampleInformation(getSmapledInfo());

    }

    protected void popSampleInformation(String msg) {
        if (imageInformation == null || !imageInformation.isIsSampled()
                || msg == null || msg.isEmpty()) {
            return;
        }

        VBox box = new VBox();
        Label label = new Label(msg);
        box.getChildren().add(label);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setContent(box);
        alert.setContentText(msg);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        ButtonType buttonExtend = new ButtonType(AppVariables.message("ExtendMemory"));
        ButtonType buttonSplit = new ButtonType(AppVariables.message("ImageSplit"));
        ButtonType buttonSample = new ButtonType(AppVariables.message("ImageSubsample"));
        ButtonType buttonView = new ButtonType(AppVariables.message("ImageViewer"));
        ButtonType buttonSave = new ButtonType(AppVariables.message("SaveSampledImage"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        switch (myFxml) {
            case CommonValues.ImageViewerFxml:
                alert.getButtonTypes().setAll(buttonExtend, buttonSample, buttonSplit, buttonSave, buttonCancel);
                break;
            case CommonValues.ImageSplitFxml:
                alert.getButtonTypes().setAll(buttonExtend, buttonSample, buttonView, buttonSave, buttonCancel);
                break;
            case CommonValues.ImageSampleFxml:
                alert.getButtonTypes().setAll(buttonExtend, buttonSplit, buttonView, buttonSave, buttonCancel);
                break;
            default:
                alert.getButtonTypes().setAll(buttonExtend, buttonSample, buttonSplit, buttonView, buttonSave, buttonCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonExtend) {
            SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
            controller.setParentController(this);
            controller.setParentFxml(myFxml);
            controller.tabPane.getSelectionModel().select(controller.baseTab);

        } else if (result.get() == buttonSplit) {
            splitAction();

        } else if (result.get() == buttonSample) {
            sampleAction();

        } else if (result.get() == buttonView) {
            if (!CommonValues.ImageViewerFxml.equals(myFxml)) {
                ImageViewerController controller
                        = (ImageViewerController) loadScene(CommonValues.ImageViewerFxml);
                controller.loadImage(sourceFile, image, imageInformation);
            }

        } else if (result.get() == buttonSave) {
            saveAsAction();
        }

    }

    @Override
    public void loadMultipleFramesImage(File file) {
        String format = FileTools.getFileSuffix(file.getAbsolutePath()).toLowerCase();
        if (format.contains("gif")) {
            final ImageGifViewerController controller
                    = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
            controller.loadImage(file.getAbsolutePath());

        } else {
            final ImageFramesViewerController controller
                    = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
            controller.selectSourceFile(file);
        }

    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;
            updateLabelTitle();

            if (saveButton != null && !saveButton.disableProperty().isBound()) {
                if (imageInformation != null
                        && imageInformation.getImageFileInformation().getNumberOfImages() > 1) {
                    saveButton.setDisable(true);
                } else {
                    saveButton.setDisable(!imageChanged);
                }
            }

            if (imageChanged) {
                resetMaskControls();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void updateLabelTitle() {
        try {
            if (getMyStage() == null) {
                return;
            }
            String title;
            if (sourceFile != null) {
                title = getBaseTitle() + " " + sourceFile.getAbsolutePath();
                if (imageInformation != null) {
                    if (imageInformation.getImageFileInformation().getNumberOfImages() > 1) {
                        title += " - " + message("Image") + " " + imageInformation.getIndex();
                    }
                    if (imageInformation.isIsSampled()) {
                        title += " - " + message("Sampled");
                    }
                }
            } else {
                title = getBaseTitle();
            }
            if (imageChanged) {
                title += "  " + "*";
            }
            getMyStage().setTitle(title);

            if (bottomLabel != null) {
                if (imageView != null && imageView.getImage() != null) {
                    String bottom = "";
                    if (imageInformation != null) {
                        bottom += AppVariables.message("Format") + ":" + imageInformation.getImageFormat() + "  ";
                        bottom += AppVariables.message("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight() + "  ";
                    }
                    bottom += AppVariables.message("LoadedSize") + ":"
                            + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "  "
                            + AppVariables.message("DisplayedSize") + ":"
                            + (int) imageView.getFitWidth() + "x" + (int) imageView.getFitHeight();

                    if (maskRectangleLine != null && maskRectangleLine.isVisible() && maskRectangleData != null) {
                        bottom += "  " + message("SelectedSize") + ": "
                                + (int) maskRectangleData.getWidth() + "x" + (int) maskRectangleData.getHeight();
                    }
                    if (sourceFile != null) {
                        bottom += "  " + AppVariables.message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "  "
                                + AppVariables.message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified()) + "  ";
                    }
                    bottomLabel.setText(bottom);

                } else {
                    bottomLabel.setText("");
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    public void pickColorAction(ActionEvent event) {
        if (paletteController == null || !paletteController.getMyStage().isShowing()) {
            paletteController = (ColorPaletteController) openStage(CommonValues.ColorPaletteFxml);
            paletteController.init(this, pickColorButton, message("ImageViewer"), true);
            paletteController.pickColorButton.setSelected(true);
            popInformation(message("PickingColorsNow"));
        }
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event) {
        if (paletteController == null || !paletteController.getParentController().equals(this)) {
            isPickingColor.set(false);
        }
        if (isPickingColor.get()) {
            IntPoint p = getImageXYint(event, imageView);
            if (p == null) {
                return;
            }
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor(p.getX(), p.getY());
            paletteController.setColor(color);
        } else {
            super.paneClicked(event);
        }
    }

    @FXML
    @Override
    public void infoAction() {
        if (imageInformation == null) {
            return;
        }
        showImageInformation(imageInformation);
    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFile != null) {
            careFrames = false;
            loadImage(nextFile.getAbsoluteFile(), loadWidth);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFile != null) {
            careFrames = false;
            loadImage(previousFile.getAbsoluteFile(), loadWidth);
        }
    }

    @FXML
    public void browseAction() {
        try {
            final ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
            if (controller != null && sourceFile != null) {
                controller.loadImages(sourceFile.getParentFile(), 9);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void viewImageAction() {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
            if (controller != null && sourceFile != null) {
                controller.loadImage(sourceFile);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popMetaData() {
        showImageMetaData(imageInformation);
    }

    @FXML
    public void moveRight() {
        FxmlControl.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        FxmlControl.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    @FXML
    public void rotateRight() {
        rotate(90);
    }

    public void rotate(final int rotateAngle) {
        if (imageView.getImage() == null) {
            return;
        }
        currentAngle = rotateAngle;
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.rotateImage(imageView.getImage(), rotateAngle);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageView.setImage(newImage);
                    checkSelect();
                    setImageChanged(true);
                    refinePane();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void rotateLeft() {
        rotate(270);
    }

    @FXML
    public void turnOver() {
        rotate(180);
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (imageView.getImage() == null
                || maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return;
        }
        maskRectangleData = new DoubleRectangle(0, 0,
                getImageWidth() - 1, getImageHeight() - 1);

        drawMaskRectangleLineAsData();
    }

    @FXML
    @Override
    public void cropAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private Image areaImage;

                    @Override
                    protected boolean handle() {
                        areaImage = cropImage();
                        if (areaImage == null) {
                            areaImage = imageView.getImage();
                        }
                        return areaImage != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        imageView.setImage(areaImage);
                        isCroppped = true;
                        setImageChanged(true);
                        resetMaskControls();
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected Image cropImage() {
        Image inImage = imageView.getImage();
//        if (handleLoadedSize || imageInformation == null) {
//            inImage = imageView.getImage();
//        } else {
//            inImage = imageInformation.getImage();
//        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            if (maskRectangleData.getSmallX() == 0
                    && maskRectangleData.getSmallY() == 0
                    && maskRectangleData.getBigX() == (int) inImage.getWidth() - 1
                    && maskRectangleData.getBigY() == (int) inImage.getHeight() - 1) {
                return null;
            }
            return FxmlImageManufacture.cropOutsideFx(inImage, maskRectangleData, Color.WHITE);

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskCircleData, Color.WHITE);

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskEllipseData, Color.WHITE);

        } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskPolygonData, Color.WHITE);

        } else {
            return null;
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
        if (isCroppped) {
            isCroppped = false;
        }
        setImageChanged(false);
    }

    @FXML
    @Override
    public void copyAction() {
        if (imageView == null || imageView.getImage() == null || copyButton == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = cropImage();
                    if (areaImage == null) {
                        areaImage = imageView.getImage();
                    }
                    return ImageClipboard.add(areaImage) != null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(AppVariables.message("ImageSelectionInClipBoard"));
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (imageView == null) {
            return;
        }
        if (sourceFile == null) {
            saveAsAction();
            return;
        }

        try {
            if (saveConfirmCheck != null && saveConfirmCheck.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(AppVariables.message("SureOverrideFile"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
                ButtonType buttonSaveAs = new ButtonType(AppVariables.message("SaveAs"));
                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
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

            if (imageInformation != null && imageInformation.isIsSampled()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(message("SureSaveSampled"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                alert.getButtonTypes().setAll(buttonSure, buttonCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() != buttonSure) {
                    return;
                }
            }

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private String filename;
                    private Image selected;

                    @Override
                    protected boolean handle() {
                        String format = FileTools.getFileSuffix(sourceFile.getName());
                        selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }

                        final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(selected);
                        if (bufferedImage == null || task == null || isCancelled()) {
                            return false;
                        }
                        filename = sourceFile.getAbsolutePath();
                        if (imageInformation.isIsSampled()) {
                            filename = FileTools.appendName(filename, "-sampled");
                        }
                        ok = ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                        if (!ok || task == null || isCancelled()) {
                            return false;
                        }
                        ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(filename);
                        if (finfo == null || finfo.getImageInformation() == null) {
                            return false;
                        }
                        imageInformation = finfo.getImageInformation();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        image = selected;
                        imageView.setImage(image);
                        popInformation(filename + "   " + AppVariables.message("Saved"));
                        setImageChanged(false);
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public String saveAsPrefix() {
        String name = "";
        if (sourceFile != null) {
            name = FileTools.getFilePrefix(sourceFile.getName());
        }
        if (fileTypeGroup != null) {
            name += "." + ((RadioButton) fileTypeGroup.getSelectedToggle()).getText();
        }
        return name;
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    saveAsPrefix(), CommonFxValues.ImageExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        Image selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }

                        String format = FileTools.getFileSuffix(file.getName());
                        final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(selected);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return bufferedImage != null
                                && ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    }

                    @Override
                    protected void whenSucceeded() {
                        popInformation(AppVariables.message("Saved"));
                        if (sourceFile == null
                                || saveAsType == SaveAsType.Load) {
                            loadImage(file);

                        } else if (saveAsType == SaveAsType.Open) {
                            openImageViewer(file);
                        }
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("SureDelete"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != buttonSure) {
                return false;
            }
        }
        if (sfile.delete()) {
            popSuccessful();
            return true;
        } else {
            popFailed();
            return false;
        }
    }

    @FXML
    public void renameAction() {
        try {
            saveAction();
            File file = renameFile(sourceFile);
            if (file == null) {
                return;
            }
            sourceFile = file;
            if (imageInformation != null) {
                ImageFileInformation finfo = imageInformation.getImageFileInformation();
                if (finfo != null) {
                    finfo.setFile(file);
                    finfo.setFileName(file.getAbsolutePath());
                }
                imageInformation.setFileName(file.getAbsolutePath());
            }
            if (imageInformation != null && imageInformation.isIsSampled()) {
                if (imageInformation.getIndex() > 0) {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " - " + message("Image") + " " + imageInformation.getIndex()
                            + " " + message("Sampled"));
                } else {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " " + message("Sampled"));
                }

            } else {
                if (imageInformation != null && imageInformation.getIndex() > 0) {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " - " + message("Image") + " " + imageInformation.getIndex());
                } else {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
                }
                updateLabelTitle();
            }
            makeImageNevigator();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public File renameFile(File sfile) {
        if (sfile == null) {
            return null;
        }
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    null, CommonFxValues.ImageExtensionFilter, true);
            if (file == null) {
                return null;
            }
            recordFileWritten(file);

            if (file.exists()) {
                if (!file.delete()) {
                    popFailed();
                }
            }
            if (sfile.renameTo(file)) {
                popSuccessful();
                return file;
            } else {
                popFailed();
                return null;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    @FXML
    protected void manufactureAction() {
        if (image == null) {
            return;
        }
        try {
            if (imageInformation != null && imageInformation.isIsMultipleFrames()) {
                String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
                if (format.contains("gif")) {
                    final ImageGifViewerController controller
                            = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                    controller.loadImage(sourceFile.getAbsolutePath());

                } else {
                    final ImageFramesViewerController controller
                            = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
                    controller.selectSourceFile(sourceFile);
                }
            } else {
                final ImageManufactureController controller
                        = (ImageManufactureController) FxmlStage.openStage(CommonValues.ImageManufactureFxml);
                controller.loadImage(sourceFile, image, imageInformation);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void sampleAction() {
        ImageSampleController controller
                = (ImageSampleController) FxmlStage.openStage(CommonValues.ImageSampleFxml);
        controller.loadImage(sourceFile, image, imageInformation);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller
                = (ImageSplitController) FxmlStage.openStage(CommonValues.ImageSplitFxml);
        controller.loadImage(sourceFile, image, imageInformation);
    }

    public void makeImageNevigator() {
        makeImageNevigator(sourceFile);
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
                FileTools.sortFiles(pathFiles, sortMode);

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
            logger.debug(e.toString());
        }
    }

    @FXML
    public void statisticAction() {
        if (image == null) {
            return;
        }
        ImageAnalyseController controller
                = (ImageAnalyseController) FxmlStage.openStage(CommonValues.ImageAnalyseFxml);
        controller.init(sourceFile, imageView.getImage());
        controller.setParentView(imageView);
        controller.loadData();
    }

}
