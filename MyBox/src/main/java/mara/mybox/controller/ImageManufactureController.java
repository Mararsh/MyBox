package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableImageHistory;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.fxml.FxmlControl.darkRedText;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageClipboard;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageHistory;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageScope;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-8-12
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    protected SimpleBooleanProperty imageLoaded, imageUpdated;
    protected SimpleIntegerProperty hisIndex;
    protected String imageHistoriesPath;
    protected boolean pickingColor, sychronizeZoom;
    protected int zoomStep, newWidth, newHeight;

    protected File refFile;
    protected ImageInformation refInformation;
    protected SimpleBooleanProperty editable;

    public static enum ImageOperation {
        Load, History, Saved, Recover, Clipboard, Paste, Arc, Color, Crop, Copy,
        Text, RichText,
        Effects, Enhancement, Shadow, Scale, Picture, Transform, Pen, Margins,
        Mosaic, Convolution
    }

    @FXML
    protected TitledPane newPane;
    @FXML
    protected VBox displayBox, imageBox, rightPaneBox;
    @FXML
    protected Label refLabel, imageTipsLabel;
    @FXML
    protected ImageView refView, scopeView;
    @FXML
    protected Rectangle bgRect;
    @FXML
    protected Button paletteButton;
    @FXML
    protected TextField newWidthInput, newHeightInput;
    @FXML
    protected ImageManufactureOperationController operationController;
    @FXML
    protected ImageManufacturePaneController currentImageController, hisImageController, refImageController;
    @FXML
    protected TabPane imageTabs;
    @FXML
    protected Tab currentImageTab, hisImageTab, refImageTab;
    @FXML
    protected Button imageManuHisAsCurrentButton, imageManuHisDeleteButton, imageManuHisClearButton,
            imageManuHisNextButton, imageManuHisPreviousButton;
    @FXML
    protected HBox commonBar, currentImageBar;
    @FXML
    protected ComboBox<ImageHistory> hisBox;

    public ImageManufactureController() {
        baseTitle = AppVariables.message("ImageManufacture");

        TipsLabelKey = "ImageManufactureTips";
    }

    @Override
    public void initValues() {
        super.initValues();
        imageLoaded = new SimpleBooleanProperty(false);
        imageUpdated = new SimpleBooleanProperty(false);
        editable = new SimpleBooleanProperty(false);
        hisIndex = new SimpleIntegerProperty(-1);
        if (imageHistoriesPath == null) {
            imageHistoriesPath = AppVariables.getImageHisPath();
        }
        zoomStep = 10;
        sychronizeZoom
                = AppVariables.getUserConfigBoolean("ImageZoomSychronize", true);
    }

    @Override
    public void initializeNext() {
        try {
            currentImageController.parent = this;
            hisImageController.parent = this;
            refImageController.parent = this;

            initLeftPane();
            initRightPane();
            initImageBox();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
//        logger.debug(event.getCode() + " " + event.getText());
        operationController.eventsHandler(event); // pass event to right pane

    }

    @Override
    public void controlHandler(KeyEvent event) {
        super.controlHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "p":
                case "P":
                    if (!popButton.isDisabled()) {
                        popAction();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void altHandler(KeyEvent event) {
        super.altHandler(event);
        if (event.isAltDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "p":
                case "P":
                    if (!popButton.isDisabled()) {
                        popAction();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @FXML
    @Override
    public void okAction() {
        operationController.okAction();
    }

    @FXML
    public void settingsAction() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.setParentFxml(myFxml);
        controller.tabPane.getSelectionModel().select(controller.imageTab);
    }

    /*
       Left Pane
     */
    protected void initLeftPane() {
        try {
            fileBox.disableProperty().bind(imageLoaded.not());
            saveAsPane.disableProperty().bind(imageLoaded.not());
            browsePane.disableProperty().bind(imageLoaded.not());
            saveButton.disableProperty().bind(imageUpdated.not());

            newPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue("ImageManufactureNewPane", newPane.isExpanded());
                    });
            newPane.setExpanded(AppVariables.getUserConfigBoolean("ImageManufactureNewPane", true));

            newWidthInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                newWidth = v;
                                newWidthInput.setStyle(null);
                            } else {
                                newWidthInput.setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            newWidthInput.setStyle(badStyle);
                        }
                    });
            newHeightInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                newHeight = v;
                                newHeightInput.setStyle(null);
                            } else {
                                newHeightInput.setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            newHeightInput.setStyle(badStyle);
                        }
                    });
            try {
                String c = AppVariables.getUserConfigValue("NewBackgroundColor", Color.TRANSPARENT.toString());
                bgRect.setFill(Color.web(c));
            } catch (Exception e) {
                bgRect.setFill(Color.TRANSPARENT);
                AppVariables.setUserConfigValue("NewBackgroundColor", Color.TRANSPARENT.toString());
            }
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay((Color) bgRect.getFill()));
            newWidthInput.setText("500");
            newHeightInput.setText("500");

            saveConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("ImageConfirmSave", saveConfirmCheck.isSelected());
                }
            });
            saveConfirmCheck.setSelected(AppVariables.getUserConfigBoolean("ImageConfirmSave", true));

            createButton.disableProperty().bind(
                    newWidthInput.styleProperty().isEqualTo(badStyle)
                            .or(newHeightInput.styleProperty().isEqualTo(badStyle))
            );

            initSaveAsPane();
            initBrowsePane();
            initTipsPane();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        if (paletteButton.equals(control)) {
            bgRect.setFill(color);
            FxmlControl.setTooltip(bgRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("NewBackgroundColor", color.toString());
        }
        return true;
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("BackgroundColor"), false);
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        Image newImage = FxmlImageManufacture.createImage(newWidth, newHeight, (Color) bgRect.getFill());
        loadImage(newImage);

        if (operationController.myPane != operationController.marginsPane) {
            operationController.expandPane(operationController.marginsPane);
        } else {
            operationController.accordionPane.setExpandedPane(operationController.marginsPane);
        }
    }

    /*
       Right Pane
     */
    protected void initRightPane() {
        try {
            rightPaneBox.disableProperty().bind(imageLoaded.not());
            operationController.initPane(this);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void operationChanged(ImageManufactureOperationController c) {
        operationController = c;
//        switch (operationController.operation) {
//            case Clipboard:
//                break;
//            case Crop:
//                imageTabs.getSelectionModel().select(currentImageTab);
//                break;
//        }
    }

    public void pickColor(boolean picking) {
        pickingColor = picking;
        if (picking) {
            imageLabel.setText(message("PickingColorNow"));
            imageLabel.setStyle(FxmlControl.darkRedText);
        } else {
            imageLabel.setText("");
            imageLabel.setStyle(FxmlControl.blueText);
        }

    }

    /*
        Image Box
     */
    protected void initImageBox() {
        try {
            imageBox.disableProperty().bind(imageLoaded.not());
            leftPaneControl.visibleProperty().bind(imageLoaded);
            rightPaneControl.visibleProperty().bind(imageLoaded);

            editable.bind(imageTabs.getSelectionModel().selectedItemProperty().isEqualTo(currentImageTab));
            imageUpdated.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
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
                        if (imageUpdated.get()) {
                            title += "  " + "*";
                        }
                        getMyStage().setTitle(title);
                    }
                }
            });

            isPickingColor.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    imageLabel.setStyle(darkRedText);
                    imageLabel.setText(message("PickingColorsNow"));
                }
            });

            currentImageController.init(this);
            hisImageController.init(this);
            refImageController.init(this);

            currentImageBar.disableProperty().bind(editable.not());
            cropButton.disableProperty().bind(currentImageController.typeGroup.selectedToggleProperty().isNull());
            redoButton.disableProperty().bind(hisIndex.lessThanOrEqualTo(0));
            recoverButton.disableProperty().bind(imageUpdated.not());

            imageTabs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    if (newValue.equals(currentImageTab)) {
                    } else {
                        if (currentImageController.getPaletteController() != null) {
                            currentImageController.getPaletteController().closeStage();
                            currentImageController.setPaletteController(null);
                        }
                    }

                    if (newValue.equals(hisImageTab)) {

                    } else if (hisImageController.getPaletteController() != null) {
                        hisImageController.getPaletteController().closeStage();
                        hisImageController.setPaletteController(null);
                    }

                    if (newValue.equals(refImageTab)) {

                    } else if (refImageController.getPaletteController() != null) {
                        refImageController.getPaletteController().closeStage();
                        refImageController.setPaletteController(null);
                    }

                }
            });

            initHisImageTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void checkRulerX() {
        currentImageController.drawMaskRulerX();
        hisImageController.drawMaskRulerX();
        refImageController.drawMaskRulerX();
    }

    @Override
    protected void checkRulerY() {
        currentImageController.drawMaskRulerY();
        hisImageController.drawMaskRulerY();
        refImageController.drawMaskRulerY();
    }

    @Override
    protected void checkCoordinate() {
        currentImageController.checkCoordinate();
        hisImageController.checkCoordinate();
        refImageController.checkCoordinate();
    }

    @FXML
    @Override
    public void copyAction() {
        if (operationController.myPane == operationController.textPane
                || operationController.myPane == operationController.richTextPane) {
            return;
        }
        copy(false);
    }

    public void copy(boolean crop) {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    try {
                        Image srcImage;
                        ImageScope scope;
                        if (imageTabs.getSelectionModel().getSelectedItem() == currentImageTab) {
                            srcImage = currentImageController.image;
                            scope = currentImageController.scope;
                        } else if (imageTabs.getSelectionModel().getSelectedItem() == hisImageTab) {
                            srcImage = hisImageController.image;
                            scope = hisImageController.scope;
                        } else if (imageTabs.getSelectionModel().getSelectedItem() == refImageTab) {
                            srcImage = refImageController.image;
                            scope = refImageController.scope;
                        } else {
                            return false;
                        }
                        if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All
                                || scope.getScopeType() == ImageScope.ScopeType.Operate) {
                            newImage = srcImage;
                        } else {
                            switch (scope.getScopeType()) {
                                case Matting:
                                case Color:
                                case RectangleColor:
                                case CircleColor:
                                case EllipseColor:
                                case PolygonColor:
                                    ImageScope tmpScope = scope.cloneValues();
                                    tmpScope.setColorExcluded(!scope.isColorExcluded());
                                    newImage = FxmlImageManufacture.crop(srcImage, tmpScope, Color.TRANSPARENT, true);
                                    break;
                                case Outline:
                                case Rectangle:
                                case Circle:
                                case Ellipse:
                                case Polygon:
                                    newImage = FxmlImageManufacture.crop(srcImage, scope, Color.TRANSPARENT, !scope.isAreaExcluded());
                            }

                        }
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return ImageClipboard.add(newImage) != null;
                    } catch (Exception e) {
                        logger.debug(e.toString());
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    updateBottom(ImageOperation.Copy);
                    popText(AppVariables.message(ImageOperation.Copy.name()) + " " + message("Successful"),
                            AppVariables.getCommentsDelay(), "white", "1.5em", null);
                    if (operationController.myPane == operationController.clipboardPane) {
                        ((ImageManufactureClipboardController) operationController).loadClipboard();
                    }
                    if (crop) {
                        crop();
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }


    /*
        Current Image
     */
    @Override
    public void afterImageLoaded() {
        try {
            undoButton.setDisable(true);
            if (image == null) {
                return;
            }
            if (imageInformation == null) {
                setImageChanged(true);
            } else if (!imageInformation.isIsSampled()) {
                setImageChanged(imageInformation.isIsScaled());
            }
            if (imageInformation != null && imageInformation.isIsSampled()) {
                sampledTips.setVisible(true);
                loadWidth = (int) image.getWidth();
                loadSampledImage();
            } else {
                sampledTips.setVisible(false);
            }

            if (imageInformation != null) {
                if (imageInformation.getImageType() == BufferedImage.TYPE_BYTE_INDEXED
                        && imageInformation.getColorChannels() == 4) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    if (sourceFile != null) {
                        alert.setTitle(sourceFile.getAbsolutePath());
                    } else {
                        alert.setTitle(getBaseTitle());
                    }
                    alert.setContentText(AppVariables.message("IndexedAlphaWarning"));
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    ButtonType buttonConvert = new ButtonType(AppVariables.message("Convert"));
                    ButtonType buttonISee = new ButtonType(AppVariables.message("ISee"));
                    alert.getButtonTypes().setAll(buttonConvert, buttonISee);
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.setAlwaysOnTop(true);
                    stage.toFront();

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == buttonConvert) {
                        openStage(CommonValues.ImageConverterBatchFxml);
                        return;
                    }
                }
            }

            String title = getMyStage().getTitle();
            currentImageController.init(sourceFile, image, title + " - " + message("CurrentImage"));
            hisImageController.init(sourceFile, image, title + " - " + message("HistoricalImage"));
            refImageController.init(sourceFile, image, title + " - " + message("ReferenceImage"));
            currentImageController.clearValues();

            imageLoaded.set(true);
            imageUpdated.set(false);

            imageView = currentImageController.imageView;
            xZoomStep = (int) (image.getWidth() * zoomStep / 100);
            yZoomStep = (int) (image.getHeight() * zoomStep / 100);
            currentImageController.xZoomStep = xZoomStep;
            currentImageController.yZoomStep = yZoomStep;
            hisImageController.xZoomStep = xZoomStep;
            hisImageController.yZoomStep = yZoomStep;
            refImageController.xZoomStep = xZoomStep;
            refImageController.yZoomStep = yZoomStep;
            refInformation = imageInformation;

            operationController.expandPane(operationController.myPane);

            hisIndex.set(-1);
            loadImageHistories();

            makeImageNevigator();

            updateBottom(ImageOperation.Load);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public ImageScope scope() {
        return currentImageController.scope;
    }

    @FXML
    @Override
    public void loadedSize() {
        if (sychronizeZoom) {
            currentImageController.loadedSize();
            hisImageController.loadedSize();
            refImageController.paneSize();
        } else {
            if (currentImageTab.isSelected()) {
                currentImageController.loadedSize();
            } else if (refImageTab.isSelected()) {
                refImageController.loadedSize();
            } else if (hisImageTab.isSelected()) {
                hisImageController.loadedSize();
            }
        }
    }

    @FXML
    @Override
    public void paneSize() {
        if (sychronizeZoom) {
            currentImageController.paneSize();
            hisImageController.paneSize();
            refImageController.paneSize();
        } else {
            if (currentImageTab.isSelected()) {
                currentImageController.paneSize();
            } else if (refImageTab.isSelected()) {
                refImageController.paneSize();
            } else if (hisImageTab.isSelected()) {
                hisImageController.paneSize();
            }
        }
    }

    @Override
    public void fitSize() {
        if (sychronizeZoom) {
            currentImageController.fitSize();
            hisImageController.fitSize();
            refImageController.fitSize();
        } else {
            if (currentImageTab.isSelected()) {
                currentImageController.fitSize();
            } else if (refImageTab.isSelected()) {
                refImageController.fitSize();
            } else if (hisImageTab.isSelected()) {
                hisImageController.fitSize();
            }
        }
    }

    @FXML
    @Override
    public void zoomIn() {
        if (sychronizeZoom) {
            currentImageController.zoomIn();
            hisImageController.zoomIn();
            refImageController.zoomIn();
        } else {
            if (currentImageTab.isSelected()) {
                currentImageController.zoomIn();
            } else if (refImageTab.isSelected()) {
                refImageController.zoomIn();
            } else if (hisImageTab.isSelected()) {
                hisImageController.zoomIn();
            }
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        if (sychronizeZoom) {
            currentImageController.zoomOut();
            hisImageController.zoomOut();
            refImageController.zoomOut();
        } else {
            if (currentImageTab.isSelected()) {
                currentImageController.zoomOut();
            } else if (refImageTab.isSelected()) {
                refImageController.zoomOut();
            } else if (hisImageTab.isSelected()) {
                hisImageController.zoomOut();
            }
        }
    }

    @FXML
    public void refAction() {

    }

    @FXML
    public void popAction() {
        ImageViewerController controller
                = (ImageViewerController) openStage(CommonValues.ImageViewerFxml);
        if (currentImageTab.isSelected()) {
            controller.loadImage(currentImageController.imageView.getImage());
        } else if (refImageTab.isSelected()) {
            controller.loadImage(refImageController.imageView.getImage());
        } else if (hisImageTab.isSelected()) {
            controller.loadImage(hisImageController.imageView.getImage());
        }
        controller.setAsPopped();
    }

    public void updateBottom(ImageOperation operation) {
        try {
            String bottom = message(operation.name()) + "  ";
            if (imageInformation != null) {
                bottom += message("Format") + ":" + imageInformation.getImageFormat() + "  ";
                bottom += message("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight() + "  ";
            }
            bottom += message("LoadedSize") + ":"
                    + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "  "
                    + message("DisplayedSize") + ":"
                    + (int) imageView.getFitWidth() + "x" + (int) imageView.getFitHeight();
            if (sourceFile != null) {
                bottom += "  " + message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "  "
                        + message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified()) + "  ";
            }
            bottomLabel.setText(bottom);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    /*
        History Image
     */
    protected String hisDesc(ImageHistory his) {
        String s = DateTools.datetimeToString(his.getOperationTime())
                + " " + message(his.getUpdateType());
        if (his.getObjectType() != null && !his.getObjectType().isEmpty()) {
            s += " " + message(his.getObjectType());
        }
        if (his.getOpType() != null && !his.getOpType().isEmpty()) {
            s += " " + message(his.getOpType());
        }
        if (his.getScopeType() != null && !his.getScopeType().isEmpty()) {
            s += " " + message(his.getScopeType());
        }
        if (his.getScopeName() != null && !his.getScopeName().isEmpty()) {
            s += " " + message(his.getScopeName());
        }
        return s;
    }

    protected void initHisImageTab() {

        hisBox.setButtonCell(new ListCell<ImageHistory>() {
            {
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override
            protected void updateItem(ImageHistory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(hisDesc(item));
            }
        });
        hisBox.setCellFactory(new Callback<ListView<ImageHistory>, ListCell<ImageHistory>>() {
            @Override
            public ListCell<ImageHistory> call(ListView<ImageHistory> param) {
                ListCell<ImageHistory> cell = new ListCell<ImageHistory>() {
                    private final ImageView view;

                    {
                        setContentDisplay(ContentDisplay.LEFT);
                        view = new ImageView();
                        view.setPreserveRatio(true);
                    }

                    @Override
                    protected void updateItem(ImageHistory item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }
                        String s = hisDesc(item);
                        if (getIndex() == hisIndex.get()) {
                            setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                            s = "** " + message("CurrentImage") + " " + s;
                        } else {
                            setStyle("");
                        }
                        view.setFitWidth(AppVariables.getUserConfigInt("ThumbnailWidth", 100));
                        view.setImage(item.getThumbnail());
                        setGraphic(view);
                        setText(s);
                    }
                };
                return cell;
            }
        });
        hisBox.setVisibleRowCount(6);
        hisBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                loadImageHistory(newVal.intValue(), false);
            }
        });

        imageManuHisAsCurrentButton.disableProperty().bind(hisBox.getSelectionModel().selectedItemProperty().isNull());
        imageManuHisDeleteButton.disableProperty().bind(hisBox.getSelectionModel().selectedItemProperty().isNull());

        checkHisNavigateButtons();
    }

    public void checkHisNavigateButtons() {
        if (hisBox.getItems() == null || hisBox.getItems().isEmpty()) {
            imageManuHisPreviousButton.setDisable(true);
            imageManuHisNextButton.setDisable(true);
            return;
        }
        int select = hisBox.getSelectionModel().getSelectedIndex();
        imageManuHisPreviousButton.setDisable(select <= 0);
        imageManuHisNextButton.setDisable(select >= hisBox.getItems().size() - 1);
    }

    protected void loadImageHistories() {
        synchronized (this) {
            if (loadTask != null) {
                return;
            }
            hisBox.getItems().clear();
            hisIndex.set(-1);
            int max = AppVariables.getUserConfigInt("MaxImageHistories", 20);
            if (max <= 0 || sourceFile == null) {
                return;
            }
            hisBox.setPromptText(message("Loading..."));
            loadTask = new Task<Void>() {
                private List<ImageHistory> list;
                private File currentFile;

                @Override
                protected Void call() {
                    try {
                        currentFile = sourceFile;
                        int max = AppVariables.getUserConfigInt("MaxImageHistories", 20);
                        if (max <= 0 || currentFile == null) {
                            return null;
                        }
                        list = TableImageHistory.read(currentFile.getAbsolutePath());
                        if (list != null) {
                            for (ImageHistory his : list) {
                                if (loadTask == null || isCancelled()
                                        || !currentFile.equals(sourceFile)) {
                                    return null;
                                }
                                loadThumbnail(his);
                            }
                        }

                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    loadTask = null;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (currentFile.equals(sourceFile)) {
                                hisBox.setPromptText("");

                                if (list != null) {
                                    if (currentFile.equals(sourceFile)) {
                                        hisBox.getItems().addAll(list);
                                    }
                                }
                                checkHisNavigateButtons();

                                recordImageHistory(ImageOperation.Load, image);
                            }
                        }
                    });
                }

                @Override
                protected void failed() {
                    super.failed();
                    loadTask = null;
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    loadTask = null;
                }

            };
            Thread thread = new Thread(loadTask);
//        openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void recordImageHistory(final ImageOperation operation, final Image newImage) {
        recordImageHistory(operation, null, null, newImage);
    }

    protected void recordImageHistory(final ImageOperation operation,
            String objectType, String opType, final Image newImage) {
        if (operation == ImageOperation.Load
                && !AppVariables.getUserConfigBoolean("RecordImageLoad", true)) {
            return;
        }
        int max = AppVariables.getUserConfigInt("MaxImageHistories", 20);
        if (sourceFile == null || max <= 0 || operation == null || newImage == null) {
            return;
        }
        if (imageHistoriesPath == null) {
            imageHistoriesPath = AppVariables.getImageHisPath();
        }
        hisBox.setPromptText(message("Writing..."));
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private File currentFile;
                private String finalname;
                private BufferedImage thumbnail;

                private String getFilename() {
                    String name = imageHistoriesPath + File.separator
                            + FileTools.getFilePrefix(currentFile.getName())
                            + "_" + (new Date().getTime())
                            + "_" + operation;
                    if (objectType != null && !objectType.trim().isEmpty()) {
                        name += "_" + objectType
                                + "_" + new Random().nextInt(1000);
                    }
                    if (opType != null && !opType.trim().isEmpty()) {
                        name += "_" + opType
                                + "_" + new Random().nextInt(1000);
                    }
                    name += "_" + new Random().nextInt(1000);
                    return name;
                }

                @Override
                protected boolean handle() {
                    try {
                        currentFile = sourceFile;
                        BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(newImage);
                        if (isCancelled()) {
                            return false;
                        }
                        String filename = getFilename();
                        while (new File(filename).exists()) {
                            filename = getFilename();
                        }
                        filename = new File(filename).getAbsolutePath();
                        finalname = new File(filename + ".png").getAbsolutePath();
                        ImageFileWriters.writeImageFile(bufferedImage, "png", finalname);
                        if (isCancelled()) {
                            return false;
                        }

                        thumbnail = ImageManufacture.scaleImageWidthKeep(bufferedImage,
                                AppVariables.getUserConfigInt("ThumbnailWidth", 100));
                        String thumbname = new File(filename + "_thumbnail.png").getAbsolutePath();
                        if (isCancelled()) {
                            return false;
                        }
                        return ImageFileWriters.writeImageFile(thumbnail, "png", thumbname);

                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    TableImageHistory.add(currentFile.getAbsolutePath(), finalname, operation.name(),
                            objectType, opType, scope());
                    if (currentFile.equals(sourceFile)) {  // The file may be changed while writing
                        hisBox.setPromptText("");

                        ImageHistory his = new ImageHistory();
                        his.setImage(sourceFile.getAbsolutePath());
                        his.setHistoryLocation(finalname);
                        his.setUpdateType(operation.name());
                        his.setObjectType(objectType);
                        his.setOpType(opType);
                        if (scope() != null) {
                            his.setScopeType(scope().getScopeType().name());
                            his.setScopeName(scope().getName());
                        }
                        his.setOperationTime(new Date());
                        his.setThumbnail(SwingFXUtils.toFXImage(thumbnail, null));

                        hisBox.getItems().add(0, his);
                        hisIndex.set(0);
                        undoButton.setDisable(false);

                        checkHisNavigateButtons();
                    }
                }
            };
            Thread thread = new Thread(task);
//            openHandlingStage(task, Modality.WINDOW_MODAL);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void loadThumbnail(ImageHistory his) {
        try {
            if (his == null) {
                return;
            }
            String fname = his.getHistoryLocation();
            int width = AppVariables.getUserConfigInt("ThumbnailWidth", 100);
            String thumbname = FileTools.appendName(fname, "_thumbnail");
            File thumbfile = new File(thumbname);
            BufferedImage bufferedImage;
            if (thumbfile.exists()) {
                bufferedImage = ImageFileReaders.readImage(thumbfile);
            } else {
                bufferedImage = ImageFileReaders.readFileByWidth("png", fname, width);
            }
            if (bufferedImage != null) {
                his.setThumbnail(SwingFXUtils.toFXImage(bufferedImage, null));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void loadImageHistory(int index, boolean asCurrent) {
        synchronized (this) {
            if (loadTask != null) {
                return;
            }
            checkHisNavigateButtons();
            int max = AppVariables.getUserConfigInt("MaxImageHistories", 20);
            if (max <= 0 || sourceFile == null) {
                undoButton.setDisable(true);
                return;
            }
            undoButton.setDisable(index < 0 || index >= hisBox.getItems().size() - 1);
            if (index < 0 || index > hisBox.getItems().size() - 1) {
                return;
            }
            loadTask = new Task<Void>() {
                private boolean ok;
                private Image hisImage;
                private String hisDesc;

                @Override
                protected Void call() {
                    try {
                        ImageHistory his = hisBox.getItems().get(index);
                        File file = new File(his.getHistoryLocation());
                        if (!file.exists()) {
                            TableImageHistory.deleteHistory(his.getImage(), his.getHistoryLocation());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    hisBox.getItems().remove(his);
                                }
                            });
                            return null;
                        }
                        BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                        if (bufferedImage != null) {
                            hisImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        }
                        hisDesc = DateTools.datetimeToString(his.getOperationTime()) + " " + message(his.getUpdateType());
                        ok = true;
                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    loadTask = null;
                    if (!ok) {
                        return;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (asCurrent) {
                                hisIndex.set(index);
                                String info = MessageFormat.format(message("CurrentImageSetAs"), hisDesc);
                                popText(info, AppVariables.getCommentsDelay(), "white", "1.5em", null);
                                // Force listView to refresh
                                // https://stackoverflow.com/questions/13906139/javafx-update-of-listview-if-an-element-of-observablelist-changes?r=SearchResults
                                for (int i = 0; i < hisBox.getItems().size();
                                        ++i) {
                                    hisBox.getItems().set(i, hisBox.getItems().get(i));
                                }

                                loadImage(ImageOperation.History, hisImage);
                            } else {
                                hisImageController.updateImage(hisImage);
                            }
                            checkHisNavigateButtons();
                        }
                    });
                }

                @Override
                protected void failed() {
                    super.failed();
                    loadTask = null;
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    loadTask = null;
                }
            };
            Thread thread = new Thread(loadTask);
            openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void hisClearAction() {
        if (sourceFile == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonSure) {
            return;
        }
        hisBox.getItems().clear();
        TableImageHistory.clearImage(sourceFile.getAbsolutePath());
        checkHisNavigateButtons();
    }

    @FXML
    public void hisDeleteAction() {
        ImageHistory selected = hisBox.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        TableImageHistory.deleteHistory(selected.getImage(), selected.getHistoryLocation());
        hisBox.getItems().remove(selected);
        checkHisNavigateButtons();
    }

    @FXML
    public void hisAsCurrentAction() {
        loadImageHistory(hisBox.getSelectionModel().getSelectedIndex(), true);
    }

    @FXML
    public void hisNextAction() {
        int selected = hisBox.getSelectionModel().getSelectedIndex();
        if (selected < 0) {
            hisBox.getSelectionModel().select(0);
        } else if (selected >= hisBox.getItems().size() - 1) {
            checkHisNavigateButtons();
        } else {
            hisBox.getSelectionModel().select(selected + 1);
        }
    }

    @FXML
    public void hisPreviousAction() {
        int selected = hisBox.getSelectionModel().getSelectedIndex();
        if (selected < 0) {
            hisBox.getSelectionModel().select(0);
        } else if (selected == 0) {
            checkHisNavigateButtons();
        } else {
            hisBox.getSelectionModel().select(selected - 1);
        }
    }


    /*
        Reference
     */
    @FXML
    public void selectReference() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVariables.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            referenceSelected(file);

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void referenceSelected(final File file) {
        try {
            if (file == null) {
                return;
            }
            recordFileOpened(file);

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private ImageFileInformation finfo;
                    private Image refImage;

                    @Override
                    protected boolean handle() {
                        finfo = ImageFileReaders.readImageFileMetaData(file);
                        if (finfo == null || finfo.getImageInformation() == null) {
                            return false;
                        }
                        BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                        if (task == null || isCancelled() || bufferedImage == null) {
                            return false;
                        }
                        refImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        if (task == null || isCancelled() || refImage == null) {
                            return false;
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        refFile = file;
                        refInformation = finfo.getImageInformation();
                        refImageController.init(refFile, refImage, message("ReferenceImage"));
                        refImageController.xZoomStep = (int) (refImage.getWidth() * zoomStep / 100);
                        refImageController.yZoomStep = (int) (refImage.getHeight() * zoomStep / 100);

                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void popRefFile(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectReference();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                referenceSelected(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void popRefInformation() {
        showImageInformation(refInformation);
    }

    @FXML
    public void popRefMeta() {
        showImageMetaData(refInformation);
    }

    /*
        Edit current image
     */
    @FXML
    @Override
    public void undoAction() {
        if (undoButton.isDisabled()) {
            return;
        }
        loadImageHistory(hisIndex.get() + 1, true);
        currentImageController.clearValues();
    }

    @FXML
    @Override
    public void redoAction() {
        if (redoButton.isDisabled()) {
            return;
        }
        loadImageHistory(hisIndex.get() - 1, true);
        currentImageController.clearValues();
    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        hisIndex.set(-1);
        currentImageController.updateImage(image);
        imageUpdated.set(false);
        updateBottom(ImageOperation.Recover);
        currentImageController.clearValues();
    }

    @FXML
    @Override
    public void saveAction() {
        if (!editable.get() || saveButton.isDisabled()) {
            return;
        }
        if (sourceFile == null) {
            saveAsAction();
            return;
        }
        if (saveConfirmCheck.isSelected()) {
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

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    String format = "png";
                    if (imageInformation != null) {
                        format = imageInformation.getImageFormat();
                    }
                    final BufferedImage bufferedImage
                            = FxmlImageManufacture.getBufferedImage(currentImageController.image);
                    if (bufferedImage == null || task == null || isCancelled()) {
                        return false;
                    }
                    ok = ImageFileWriters.writeImageFile(bufferedImage, format, sourceFile.getAbsolutePath());
                    if (!ok || task == null || isCancelled()) {
                        return false;
                    }
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(sourceFile.getAbsolutePath());
                    if (finfo == null || finfo.getImageInformation() == null) {
                        return false;
                    }
                    imageInformation = finfo.getImageInformation();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    imageUpdated.set(false);
                    updateBottom(ImageOperation.Saved);
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
    public void saveAsAction() {
        if (!editable.get() || saveAsButton.isDisabled()) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    saveAsPrefix(), targetExtensionFilter, true);
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
                        String format = FileTools.getFileSuffix(file.getName());
                        final BufferedImage bufferedImage
                                = FxmlImageManufacture.getBufferedImage(currentImageController.image);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (sourceFile == null
                                || saveAsType == SaveAsType.Load) {
                            sourceFileChanged(file);

                        } else if (saveAsType == SaveAsType.Open) {
                            openImageManufacture(file.getAbsolutePath());
                        }
                        popSuccessful();
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

    public void updateImage(ImageOperation operation, Image newImage, long cost) {
        if (!editable.get()) {
            return;
        }
        updateImage(operation, null, null, newImage, cost);
    }

    public void updateImage(ImageOperation operation, String objectType, String opType,
            Image newImage, long cost) {
        if (!editable.get()) {
            return;
        }
        try {
            currentImageController.updateImage(newImage);
            imageUpdated.set(true);
            recordImageHistory(operation, objectType, opType, newImage);

            String info;
            if (operation == ImageOperation.Scale) {
                info = AppVariables.message("Scale2") + " " + message("Successful");
            } else {
                info = AppVariables.message(operation.name()) + " " + message("Successful");
            }
            if (objectType != null) {
                info += "  " + message(objectType);
            }
            if (opType != null) {
                info += "  " + message(opType);
            }
            if (cost > 0) {
                info += "    " + message("Cost") + ": " + DateTools.showTime(cost);
            }
            bottomLabel.setText(info);

            popText(info, AppVariables.getCommentsDelay(), "white", "1.5em", null);

            currentImageController.clearValues();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void loadImage(ImageOperation operation, Image newImage) {
        try {
            currentImageController.updateImage(newImage);
            imageUpdated.set(true);
            updateBottom(operation);
            imageTabs.getSelectionModel().select(currentImageTab);
            currentImageController.clearValues();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    // This should be implemented in frame since cropping happens anytime and right side may be other pane
    @FXML
    @Override
    public void cropAction() {
        if (AppVariables.getUserConfigBoolean("ImageCropPutClipboard", false)) {
            copy(true);
        } else {
            crop();
        }

    }

    public void crop() {
        if (!editable.get() || scope() == null || scope().getScopeType() == ImageScope.ScopeType.All) {
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

                    String cString = AppVariables.getUserConfigValue("CropBackgroundColor", Color.TRANSPARENT.toString());
                    Color bgColor;
                    try {
                        bgColor = Color.web(cString);
                    } catch (Exception e) {
                        bgColor = Color.TRANSPARENT;
                    }
                    newImage = FxmlImageManufacture.crop(imageView.getImage(), scope(), bgColor, scope().isAreaExcluded());
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    updateImage(ImageOperation.Crop, newImage, cost);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    // This can be implemented in right pane since clipboard always opened when this happens
    @FXML
    @Override
    public void pasteAction() {
        if (!editable.get()) {
            return;
        }
        // Ctrl-c/v are necessary for text editing
        if (operationController.myPane == operationController.textPane
                || operationController.myPane == operationController.richTextPane) {
            return;
        }
        if (operationController.myPane != operationController.clipboardPane) {
            operationController = operationController.expandPane(operationController.clipboardPane);
            timer = new Timer();   // Waiting for thumbs list loaded
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (operationController.task == null) {
                                operationController.pasteAction();
                                timer.cancel();
                                timer = null;
                            }
                        }
                    });
                }
            }, 0, 300);

        } else {
            operationController.accordionPane.setExpandedPane(operationController.clipboardPane);
        }
    }

    /*
        Browse
     */
    @FXML
    @Override
    public void nextAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile());
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (previousFile != null) {
            loadImage(previousFile.getAbsoluteFile());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!imageLoaded.get() || !imageUpdated.get()) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVariables.message("ImageChanged"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
        ButtonType buttonSaveAs = new ButtonType(AppVariables.message("SaveAs"));
        ButtonType buttonNotSave = new ButtonType(AppVariables.message("NotSave"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonNotSave, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            saveAction();
            return true;
        } else if (result.get() == buttonNotSave) {
            return true;
        } else if (result.get() == buttonSaveAs) {
            saveAsAction();
            return true;
        } else {
            return false;
        }

    }

    @FXML
    @Override
    public void statisticAction() {
        ImageView view = null;
        File file = null;
        if (currentImageTab.isSelected()) {
            view = currentImageController.imageView;
            file = sourceFile;

        } else if (refImageTab.isSelected()) {
            view = refImageController.imageView;
            sourceFile = refFile;

        } else if (hisImageTab.isSelected()) {
            view = hisImageController.imageView;

        }
        if (view == null || view.getImage() == null) {
            return;
        }
        ImageAnalyseController controller
                = (ImageAnalyseController) FxmlStage.openStage(CommonValues.ImageAnalyseFxml);
        controller.init(file, view.getImage());
        controller.setParentView(view);
        controller.loadData();
    }

}
