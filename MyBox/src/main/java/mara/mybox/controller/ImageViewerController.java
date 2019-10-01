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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.DoubleRectangle;
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
import mara.mybox.value.CommonImageValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageMaskController {

    final protected String ImageConfirmDeleteKey, ImageOpenSaveKey, ModifyImageKey;
    protected String ImageSelectKey, ImageDataShowKey, ImageLoadWidthKey;

    protected ImageScope scope;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;

    protected FileSortMode sortMode;

    @FXML
    protected HBox operation1Box, operation2Box, navBox;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected VBox contentBox, imageBox;
    @FXML
    public Button moveUpButton, moveDownButton, manufactureButton, statisticButton, splitButton,
            sampleButton, browseButton;
    @FXML
    protected CheckBox selectAreaCheck, deleteConfirmCheck, openSaveCheck;

    @FXML
    protected ComboBox<String> loadWidthBox, sortBox;

    public ImageViewerController() {
        baseTitle = AppVariables.message("ImageViewer");

        ImageConfirmDeleteKey = "ImageConfirmDeleteKey";
        ImageOpenSaveKey = "ImageOpenSaveKey";
        ModifyImageKey = "ModifyImageKey";
        TipsLabelKey = "ImageViewerTips";
        ImageDataShowKey = "ImageDataShowKey";
        ImageLoadWidthKey = "ImageViewerLoadWidthKey";
        ImageSelectKey = "ImageSelectKey";

    }

    @Override
    public void initializeNext() {
        try {
            initOperation1Box();
            initOperation2Box();
            initOperation3Box();
            initImageView();
            initMaskPane();
            initRulersCheck();

            initSortBox();

            moreAction();

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void moreAction() {
        if (moreButton == null || contentBox == null) {
            return;
        }
        if (moreButton.isSelected()) {
            if (!contentBox.getChildren().contains(operation2Box)) {
                contentBox.getChildren().add(1, operation2Box);
            }
        } else {
            if (contentBox.getChildren().contains(operation2Box)) {
                contentBox.getChildren().remove(operation2Box);
            }
        }
        FxmlControl.refreshStyle(contentBox);
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

    protected void initOperation1Box() {
        if (operation1Box != null) {
            operation1Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

        if (selectAreaCheck != null) {
            selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(ImageSelectKey, selectAreaCheck.isSelected());
                    checkSelect();
                }
            });
            selectAreaCheck.setSelected(AppVariables.getUserConfigBoolean(ImageSelectKey, false));
            checkSelect();
            Tooltip tips = new Tooltip("CTRL+t");
            FxmlControl.setTooltip(selectAreaCheck, tips);
        }

        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(ImageConfirmDeleteKey, deleteConfirmCheck.isSelected());
                }
            });
            deleteConfirmCheck.setSelected(AppVariables.getUserConfigBoolean(ImageConfirmDeleteKey, true));
        }

        if (openSaveCheck != null) {
            openSaveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(ImageOpenSaveKey, openSaveCheck.isSelected());
                }
            });
            openSaveCheck.setSelected(AppVariables.getUserConfigBoolean(ImageOpenSaveKey, true));
        }

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

    protected void initOperation2Box() {

        if (operation2Box != null) {
            operation2Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

        loadWidth = defaultLoadWidth;
        if (loadWidthBox != null) {
            List<String> values = Arrays.asList(AppVariables.message("OrignalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            loadWidthBox.getItems().addAll(values);
            loadWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
//                    logger.debug(oldValue + " " + newValue + " " + (String) loadWidthBox.getSelectionModel().getSelectedItem());
                    if (AppVariables.message("OrignalSize").equals(newValue)) {
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
                    AppVariables.setUserConfigInt(ImageLoadWidthKey, loadWidth);
                    if (!isSettingValues) {
                        setLoadWidth();
                    }
                }
            });

            isSettingValues = true;
            int v = AppVariables.getUserConfigInt(ImageLoadWidthKey, defaultLoadWidth);
            if (v <= 0) {
                loadWidthBox.getSelectionModel().select(0);
            } else {
                loadWidthBox.getSelectionModel().select(v + "");
            }
            isSettingValues = false;
            FxmlControl.setTooltip(loadWidthBox, new Tooltip(AppVariables.message("ImageLoadWidthCommnets")));
        }

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

    protected void initOperation3Box() {

        if (navBox != null) {
            navBox.setDisable(true);
        }

        if (manufactureButton != null) {
            manufactureButton.setDisable(true);
        }

    }

    protected void adjustSplitPane() {
        try {

            int size = splitPane.getItems().size();
            float p = 1.0f / size;
            if (size == 1) {
                splitPane.setDividerPositions(1);
            } else {
                for (int i = 0; i < size - 1; i++) {
                    splitPane.getDividers().get(i).setPosition(p);
                }
            }
            splitPane.layout();
            fitSize();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initSortBox() {
        if (sortBox == null) {
            return;
        }
        List<String> svalues = Arrays.asList(AppVariables.message("ModifyTimeDesc"),
                AppVariables.message("ModifyTimeAsc"),
                AppVariables.message("SizeDesc"),
                AppVariables.message("SizeAsc"),
                AppVariables.message("NameDesc"),
                AppVariables.message("NameAsc"),
                AppVariables.message("FormatDesc"),
                AppVariables.message("FormatAsc"),
                AppVariables.message("CreateTimeDesc"),
                AppVariables.message("CreateTimeAsc")
        );
        sortBox.getItems().addAll(svalues);
        sortBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                if (AppVariables.message("ModifyTimeDesc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.ModifyTimeDesc;
                } else if (AppVariables.message("ModifyTimeAsc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.ModifyTimeAsc;
                } else if (AppVariables.message("SizeDesc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.SizeDesc;
                } else if (AppVariables.message("SizeAsc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.SizeAsc;
                } else if (AppVariables.message("NameDesc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.NameDesc;
                } else if (AppVariables.message("NameAsc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.NameAsc;
                } else if (AppVariables.message("FormatDesc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.FormatDesc;
                } else if (AppVariables.message("FormatAsc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.FormatAsc;
                } else if (AppVariables.message("CreateTimeDesc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.CreateTimeDesc;
                } else if (AppVariables.message("CreateTimeAsc").equals(newValue)) {
                    sortMode = FileTools.FileSortMode.CreateTimeAsc;
                } else {
                    sortMode = FileTools.FileSortMode.ModifyTimeDesc;
                }
                if (!isSettingValues) {
                    makeImageNevigator();
                }
            }
        });
//            sortBox.getSelectionModel().select(0);
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
        if (navBox != null) {
            navBox.setDisable(sourceFile == null);
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

            if (sourceFile != null && navBox != null) {
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
                    loadWidthBox.getSelectionModel().select(AppVariables.message("OrignalSize"));
                } else {
                    loadWidthBox.getSelectionModel().select(loadWidth + "");
                }
                isSettingValues = false;
            }

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
            if (sourceFile != null) {
                String title = getBaseTitle() + " " + sourceFile.getAbsolutePath();
                if (imageInformation != null) {
                    if (imageInformation.getImageFileInformation().getNumberOfImages() > 1) {
                        title += " - " + message("Image") + " " + imageInformation.getIndex();
                    }
                    if (imageInformation.isIsSampled()) {
                        title += " - " + message("Sampled");
                    }
                }
                if (imageChanged) {
                    title += "  " + "*";
                }
                getMyStage().setTitle(title);
            }

            if (imageView != null && imageView.getImage() != null) {
                if (bottomLabel != null) {
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
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
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
        rotateModify(90);
    }

    public void rotateModify(final int rotateAngle) {
        if (imageView.getImage() == null) {
            return;
        }
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
        rotateModify(270);
    }

    @FXML
    public void turnOver() {
        rotateModify(180);
    }

    @FXML
    public void straighten() {
        currentAngle = 0;
        imageView.setRotate(currentAngle);
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
                    return ImageClipboard.add(areaImage, true) != null;
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
                if (result.get() == buttonCancel) {
                    return;
                }
            }

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private String filename;

                    @Override
                    protected boolean handle() {
                        String format = FileTools.getFileSuffix(sourceFile.getName());

                        final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(imageView.getImage());
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
                        imageInformation = ImageFileReaders.readImageFileMetaData(filename).getImageInformation();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        image = imageView.getImage();
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
        if (sourceFile != null) {
            return FileTools.getFilePrefix(sourceFile.getName());
        } else {
            return "";
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    saveAsPrefix(), CommonImageValues.ImageExtensionFilter, true);
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
                        if (sourceFile == null) {
                            loadImage(file);
                        }
                        if (openSaveCheck != null && openSaveCheck.isSelected()) {
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
                navBox.setDisable(true);
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
            if (result.get() == buttonCancel) {
                return false;
            }
        }
        if (sfile.delete()) {
            popSuccessul();
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
                    null, CommonImageValues.ImageExtensionFilter, true);
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
                popSuccessul();
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
            for (File file : files) {
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    pathFiles.add(file);
                }
            }
            FileTools.sortFiles(pathFiles, sortMode);

            for (int i = 0; i < pathFiles.size(); i++) {
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
        ImageDataController controller
                = (ImageDataController) FxmlStage.openStage(CommonValues.ImageDataFxml);
        controller.parent = this;
        controller.init(sourceFile, image, false);
        controller.loadData();
    }

}
