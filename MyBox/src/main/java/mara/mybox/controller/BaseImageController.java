package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.darkRedText;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController extends BaseController {

    public static final String DefaultStrokeColor = "#c94d58", DefaultAnchorColor = "#0066cc";
    protected ImageInformation imageInformation;
    protected Image image;
    protected ImageAttributes attributes;
    protected boolean careFrames, isPickingColor, imageChanged, operateOriginalSize,
            needNotRulers, needNotCoordinates, needNotContextMenu;
    protected int loadWidth, defaultLoadWidth, frameIndex, sizeChangeAware = 10,
            xZoomStep = 50, yZoomStep = 50;
    protected LoadingController loadingController;
    protected SingletonTask loadTask;
    protected double mouseX, mouseY;
    protected ColorPaletteManageController paletteController;
    protected Label imageLabelOriginal;

    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected AnchorPane maskPane;
    @FXML
    protected ImageView imageView, sampledView;
    @FXML
    protected Rectangle borderLine;
    @FXML
    protected Text sizeText, xyText;
    @FXML
    protected Label imageLabel;
    @FXML
    protected Button imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton,
            rotateLeftButton, rotateRightButton, turnOverButton;
    @FXML
    protected CheckBox pickColorCheck, rulerXCheck, rulerYCheck, coordinateCheck,
            contextMenuCheck, copyToSystemClipboardCheck;

    public BaseImageController() {
        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    public void initController(ImageViewerController parent) {
        this.parentController = parent;
        initMaskPane();
        initMaskControls(false);
    }

    public void initController(File sourceFile, Image image) {
        try {
            this.sourceFile = sourceFile;
            this.image = image;
            imageView.setImage(image);
            fitSize();

            checkRulerX();
            checkRulerY();
            checkCoordinate();
            setMaskStroke();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateImage(Image image) {
        try {
            imageView.setImage(image);
            setImageChanged(true);
            fitSize();
            drawMaskControls();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            isPickingColor = imageChanged = operateOriginalSize
                    = needNotRulers = needNotCoordinates = needNotContextMenu = false;
            careFrames = true;
            loadWidth = defaultLoadWidth = -1;
            frameIndex = 0;
            sizeChangeAware = 10;
            xZoomStep = yZoomStep = 50;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (pickColorCheck != null) {
                pickColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        if (isSettingValues) {
                            return;
                        }
                        isPickingColor = pickColorCheck.isSelected();
                        if (isPickingColor) {
                            startPickingColor();
                        } else {
                            stopPickingColor();
                        }
                    }
                });
            }
            if (sampledView != null) {
                sampledView.setVisible(false);
            }
            initImageView();
            initViewControls();
            initMaskPane();
            initMaskControls(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        if (event.getCode() != null) {
            if (event.isControlDown() || !FxmlControl.textInputFocus(this)) {
                switch (event.getCode()) {
                    case DIGIT1:
                        if (imageSizeButton != null && !imageSizeButton.isDisabled()) {
                            loadedSize();
                        }
                        return;
                    case DIGIT2:
                        if (paneSizeButton != null && !paneSizeButton.isDisabled()) {
                            paneSize();
                        }
                        return;
                    case DIGIT3:
                        if (zoomInButton != null && !zoomInButton.isDisabled()) {
                            zoomIn();
                        }
                        return;
                    case DIGIT4:
                        if (zoomOutButton != null && !zoomOutButton.isDisabled()) {
                            zoomOut();
                        }
                        return;
                }
            }
        }
        super.keyEventsHandler(event);
    }

    protected void initImageView() {
        if (imageView == null) {
            return;
        }
        try {
            imageView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                }
            });
            imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));

                }
            });
            if (scrollPane != null) {
                scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov,
                            Number old_val, Number new_val) {
                        viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                    }
                });
                scrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov,
                            Number old_val, Number new_val) {
                        viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initViewControls() {
        try {
            if (rulerXCheck != null) {
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "RulerX", rulerXCheck.isSelected());
                        checkRulerX();
                    }
                });
                rulerXCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "RulerX", false));
            }
            if (rulerYCheck != null) {
                rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "RulerY", rulerYCheck.isSelected());
                        checkRulerY();
                    }
                });
                rulerYCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "RulerY", false));
            }

            if (coordinateCheck != null) {
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "PopCooridnate", coordinateCheck.isSelected());
                        checkCoordinate();
                    }
                });
                coordinateCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "PopCooridnate", false));
            }

            if (contextMenuCheck != null) {
                contextMenuCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "ContextMenu", contextMenuCheck.isSelected());
                    }
                });
                contextMenuCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ContextMenu", true));
            }

            if (copyToSystemClipboardCheck != null) {
                copyToSystemClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue("CopyToSystemClipboard", copyToSystemClipboardCheck.isSelected());
                    }
                });
                copyToSystemClipboardCheck.setSelected(AppVariables.getUserConfigBoolean("CopyToSystemClipboard", true));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMaskPane() {
        if (maskPane == null) {
            return;
        }
        try {
            maskPane.prefWidthProperty().bind(imageView.fitWidthProperty());
            maskPane.prefHeightProperty().bind(imageView.fitHeightProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMaskControls(boolean show) {
        try {
            drawMaskRulerX();
            drawMaskRulerY();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void viewSizeChanged(double change) {
        if (isSettingValues || imageView.getImage() == null) {
            return;
        }
        refinePane();
//        if (change > sizeChangeAware) {
//            refinePane();
//        }
        drawMaskControls();
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        careFrames = true;
        loadImage(file, loadWidth);
    }

    public void clear() {

    }

    // Any mask operations when pane size is changed
    public void drawMaskControls() {
        setMaskStroke();
        checkRulerX();
        checkRulerY();
        checkCoordinate();
    }

    protected void checkRulerX() {
        drawMaskRulerX();
    }

    protected void checkRulerY() {
        drawMaskRulerY();
    }

    protected void checkCoordinate() {
        if (xyText != null) {
            xyText.setText("");
        }
    }

    protected int getRulerStep(double width) {
        if (width <= 1000) {
            return 10;
        } else if (width <= 10000) {
            return (int) (width / 1000) * 10;
        } else {
            return (int) (width / 10000) * 10;
        }
    }

    public void drawMaskRulerX() {
        if (needNotRulers || maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerX();
        if (AppVariables.getUserConfigBoolean(baseName + "RulerX", false)) {
            Color strokeColor = Color.web(AppVariables.getUserConfigValue("StrokeColor", "#FF0000"));
            double imageWidth = getImageWidth() / widthRatio();
            double ratio = imageView.getBoundsInParent().getWidth() / imageWidth;
            int step = getRulerStep(imageWidth);
            for (int i = step; i < imageWidth; i += step) {
                double x = i * ratio;
                Line line = new Line(x, 0, x, 8);
                line.setId("MaskRulerX" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = step * 10;
            for (int i = step10; i < imageWidth; i += step10) {
                double x = i * ratio;
                Line line = new Line(x, 0, x, 15);
                line.setId("MaskRulerX" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
                Text text = new Text(i + " ");
                text.setStyle(style);
                text.setFill(strokeColor);
                text.setLayoutX(imageView.getLayoutX() + x - 10);
                text.setLayoutY(imageView.getLayoutY() + 30);
                text.setId("MaskRulerXtext" + i);
                maskPane.getChildren().add(text);
            }
        }
    }

    public void clearMaskRulerX() {
        if (needNotRulers || maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("MaskRulerX")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    public void drawMaskRulerY() {
        if (needNotRulers || maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerY();
        if (AppVariables.getUserConfigBoolean(baseName + "RulerY", false)) {
            Color strokeColor = Color.web(AppVariables.getUserConfigValue("StrokeColor", "#FF0000"));
            double imageHeight = getImageHeight() / heightRatio();
            double ratio = imageView.getBoundsInParent().getHeight() / imageHeight;
            int step = getRulerStep(imageHeight);
            for (int j = step; j < imageHeight; j += step) {
                double y = j * ratio;
                Line line = new Line(0, y, 8, y);
                line.setId("MaskRulerY" + j);
                line.setStroke(strokeColor);
                line.setStrokeWidth(1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = step * 10;
            for (int j = step10; j < imageHeight; j += step10) {
                double y = j * ratio;
                Line line = new Line(0, y, 15, y);
                line.setId("MaskRulerY" + j);
                line.setStroke(strokeColor);
                line.setStrokeWidth(2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                Text text = new Text(j + " ");
                text.setStyle(style);
                text.setFill(strokeColor);
                text.setLayoutX(imageView.getLayoutX() + 25);
                text.setLayoutY(imageView.getLayoutY() + y + 8);
                text.setId("MaskRulerYtext" + j);
                maskPane.getChildren().addAll(line, text);
            }
        }
    }

    public void clearMaskRulerY() {
        if (needNotRulers || maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("MaskRulerY")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    public void setMaskStroke() {
        try {
            if (isSettingValues) {
                return;
            }
            if (xyText != null) {
                Color strokeColor = Color.web(AppVariables.getUserConfigValue("StrokeColor", DefaultStrokeColor));
                xyText.setFill(strokeColor);
                xyText.setText("");
            }
            drawMaskRulerX();
            drawMaskRulerY();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void refinePane() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        FxmlControl.moveCenter(scrollPane, imageView);
        scrollPane.setVvalue(scrollPane.getVmin());
        if (borderLine != null) {
            borderLine.setLayoutX(imageView.getLayoutX() - 1);
            borderLine.setLayoutY(imageView.getLayoutY() - 1);
            borderLine.setWidth(imageView.getBoundsInParent().getWidth() + 2);
            borderLine.setHeight(imageView.getBoundsInParent().getHeight() + 2);
        }
        if (sizeText != null) {
            sizeText.setX(borderLine.getLayoutX() + borderLine.getWidth() + 1);
            sizeText.setY(borderLine.getLayoutY() + borderLine.getHeight() - 10);
            sizeText.setText((int) (imageView.getImage().getWidth()) + "x" + (int) (imageView.getImage().getHeight()));
        }
    }

    public double getImageWidth() {
        if (imageView != null && imageView.getImage() != null) {
            return imageView.getImage().getWidth();
        } else if (image != null) {
            return image.getWidth();
        } else if (imageInformation != null) {
            return imageInformation.getWidth();
        } else {
            return -1;
        }
    }

    public double getImageHeight() {
        if (imageView != null && imageView.getImage() != null) {
            return imageView.getImage().getHeight();
        } else if (image != null) {
            return image.getHeight();
        } else if (imageInformation != null) {
            return imageInformation.getHeight();
        } else {
            return -1;
        }
    }

    public double widthRatio() {
        if (!operateOriginalSize || imageInformation == null || image == null) {
            return 1;
        }
        double ratio = 1d * getImageWidth() / imageInformation.getWidth();
        return ratio;
    }

    public double heightRatio() {
        if (!operateOriginalSize || imageInformation == null || image == null) {
            return 1;
        }
        double ratio = 1d * getImageHeight() / imageInformation.getHeight();
        return ratio;
    }

    public int getOperationWidth() {
        return (int) (getImageWidth() / widthRatio());
    }

    public int getOperationHeight() {
        return (int) (getImageHeight() / heightRatio());
    }

    @FXML
    public void loadedSize() {
        if (imageView == null || imageView.getImage() == null || scrollPane == null) {
            return;
        }
        try {
            FxmlControl.imageSize(scrollPane, imageView);
            refinePane();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void paneSize() {
        if (imageView == null || imageView.getImage() == null || scrollPane == null) {
            return;
        }
        try {
            FxmlControl.paneSize(scrollPane, imageView);
            refinePane();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void fitSize() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            if (scrollPane.getHeight() < getImageHeight()
                    || scrollPane.getWidth() < getImageWidth()) {
                paneSize();
            } else {
                loadedSize();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void zoomIn() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        FxmlControl.zoomIn(scrollPane, imageView, xZoomStep, yZoomStep);
        refinePane();
    }

    @FXML
    public void zoomOut() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        FxmlControl.zoomOut(scrollPane, imageView, xZoomStep, yZoomStep);
        refinePane();
    }

    public void loadImage(final File file) {
        loadImage(file, false, loadWidth, frameIndex, careFrames);
    }

    public void loadImage(final File file, int maxWidth) {
        loadImage(file, false, maxWidth, frameIndex, careFrames);
    }

    public void loadImageInformation(final File file) {
        loadImage(file, true);
    }

    public void loadImage(final File file, final boolean onlyInformation) {
        loadImage(file, onlyInformation, loadWidth, frameIndex, careFrames);
    }

    public void loadImage(File file, boolean onlyInformation,
            int requiredWidth, int inFrameIndex, boolean inCareFrames) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        recordFileOpened(file);
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            final String fileName = file.getPath();
            loadTask = new SingletonTask<Void>() {
                private boolean multiplied, needSample;
                private ImageInformation imageInfo;

                @Override
                protected boolean handle() {
                    final ImageFileInformation imageFileInformation = ImageInformation.loadImageFileInformation(file);
                    if (imageFileInformation == null
                            || imageFileInformation.getImagesInformation() == null
                            || imageFileInformation.getImagesInformation().isEmpty()) {
                        return false;
                    }
                    String format = FileTools.getFileSuffix(fileName).toLowerCase();
                    if (loadTask == null || isCancelled() || "raw".equals(format)) {
                        return false;
                    }
                    imageInfo = imageFileInformation.getImagesInformation().get(inFrameIndex);
                    if (!onlyInformation) {
                        if (imageFileInformation.getImagesInformation().size() > 1 && careFrames) {
                            multiplied = true;
                            return false;
                        }
                        int maxWidth = ImageInformation.countMaxWidth(imageInfo);
                        int checkWidth = requiredWidth > 0 ? requiredWidth : imageInfo.getWidth();
                        if (maxWidth < checkWidth) {
                            needSample = true;
                            return false;
                        }
                        ImageInformation.loadImage(imageInfo, checkWidth);
                        image = imageInfo.getThumbnail();
                    } else {
                        image = null;
                    }
                    return imageInfo != null;
                }

                @Override
                protected void whenSucceeded() {
                    sourceFile = file;
                    imageInformation = imageInfo;

                    frameIndex = inFrameIndex;
                    careFrames = inCareFrames;
                    getMyStage().setTitle(getBaseTitle() + " " + fileName);
                    afterInfoLoaded();
                    afterImageLoaded();
                    refinePane();
                }

                @Override
                protected void whenFailed() {
                    if (multiplied) {
                        loadMultipleFramesImage(file);
                    } else if (needSample) {
                        needSample(imageInfo);
                    } else {
                        popError(AppVariables.message("FailOpenImage"));
                    }
                }

            };
            openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void needSample(ImageInformation imageInfo) {
        ImageTooLargeController controller = (ImageTooLargeController) openStage(CommonValues.ImageTooLargeFxml, true);
        controller.setValues(this, imageInfo);
    }

    public void loadImage(final String fileName) {
        try {
            if (fileName == null) {
                return;
            }
            sourceFile = new File(fileName).getAbsoluteFile(); // Must convert to AbsoluteFile!
//            infoAction(fileName + "\n" + sourceFile.getAbsolutePath());
            if (sourceFileInput != null) {
                sourceFileInput.setText(sourceFile.getAbsolutePath());
            } else {
                loadImage(sourceFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImage(File sourceFile, ImageInformation imageInformation) {
        if (imageInformation == null) {
            loadImage(sourceFile);
            return;
        }
        boolean exist = this.sourceFile != null || image != null;
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;

        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            loadTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    image = imageInformation.loadImage(loadWidth);
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    afterImageLoaded();
                    setImageChanged(exist);
                }

            };
            loadingController = openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void loadImage(ImageInformation imageInformation) {
        if (imageInformation == null) {
            loadImage(sourceFile);
            return;
        }
        loadImage(imageInformation.getFile(), imageInformation);
    }

    public void loadImage(Image inImage) {
        sourceFile = null;
        imageInformation = null;
        image = inImage;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(File sourceFile, ImageInformation imageInformation, Image image) {
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        this.image = image;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadImage(Image inImage, int maxWidth) {
        sourceFile = null;
        imageInformation = null;
        image = FxmlImageManufacture.scaleImage(inImage, maxWidth);
        loadWidth = maxWidth;
        afterImageLoaded();
        setImageChanged(true);
    }

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

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void afterInfoLoaded() {

    }

    public boolean afterImageLoaded() {
        return true;
    }

    public void loadMultipleFramesImage(File file) {
        if (file == null || !file.exists() || file.length() == 0) {
            return;
        }
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
                    if (imageInformation.isIsScaled()) {
                        title += " - " + message("Scaled");
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
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void popAction() {
        ImageViewerController controller
                = (ImageViewerController) openStage(CommonValues.ImagePopupFxml);
        controller.loadImage(sourceFile, imageInformation, image);
        controller.paneSize();
        controller.checkAlwaysTop();
    }

    @FXML
    public DoublePoint showXY(MouseEvent event) {
        if (xyText == null || !xyText.isVisible()) {
            return null;
        }
        if (!isPickingColor
                && (needNotCoordinates || !AppVariables.getUserConfigBoolean(baseName + "PopCooridnate", false))) {
            xyText.setText("");
            return null;
        }
        DoublePoint p = FxmlControl.getImageXY(event, imageView);
        showXY(event, p);
        return p;
    }

    public DoublePoint showXY(MouseEvent event, DoublePoint p) {
        if (p == null) {
            xyText.setText("");
            return null;
        }
        PixelReader pixelReader = imageView.getImage().getPixelReader();
        Color color = pixelReader.getColor((int) p.getX(), (int) p.getY());
        String s = (int) Math.round(p.getX() / widthRatio()) + ","
                + (int) Math.round(p.getY() / heightRatio()) + "\n"
                + FxmlColor.colorDisplaySimple(color);
        if (isPickingColor) {
            s = message("PickingColorsNow") + "\n" + s;
        }
        xyText.setText(s);
        xyText.setX(event.getX() + 10);
        xyText.setY(event.getY());
        return p;
    }

    public IntPoint getImageXYint(MouseEvent event, ImageView view) {
        DoublePoint p = FxmlControl.getImageXY(event, view);
        if (p == null) {
            return null;
        }
        int ix = (int) Math.round(p.getX());
        int iy = (int) Math.round(p.getY());

        return new IntPoint(ix, iy);
    }

    @FXML
    public void imageClicked(MouseEvent event) {
//        MyBoxLog.debug("imageClicked");
    }

    @FXML
    public void paneClicked(MouseEvent event) {
        if (imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        DoublePoint p = FxmlControl.getImageXY(event, imageView);
        imageClicked(event, p);
    }

    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (p == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (isPickingColor) {
            pickColor(p, imageView);

        } else if (event.getClickCount() > 1) {  // Notice: Double click always trigger single click at first
            imageDoubleClicked(event, p);

        } else if (event.getClickCount() == 1) {
            imageSingleClicked(event, p);
        }
    }

    public void imageDoubleClicked(MouseEvent event, DoublePoint p) {

    }

    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        if (needNotContextMenu || !AppVariables.getUserConfigBoolean(baseName + "ContextMenu", true)
                || imageView == null || imageView.getImage() == null
                || event == null || event.getButton() != MouseButton.SECONDARY) {
            return;
        }
        Timer menuTimer = new Timer();
        menuTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    popImageMenu(imageView, event);
                });
            }
        }, 100);  // double click will be eaten by the menu if not delay

    }

    @FXML
    public void mousePressed(MouseEvent event) {

    }

    @FXML
    public void mouseDragged(MouseEvent event) {

    }

    @FXML
    public void mouseReleased(MouseEvent event) {

    }

    protected Color pickColor(MouseEvent event, ImageView view) {
//        MyBoxLog.debug("pickColor");
        DoublePoint p = FxmlControl.getImageXY(event, view);
        return pickColor(p, imageView);
    }

    protected Color pickColor(DoublePoint p, ImageView view) {
//        MyBoxLog.debug("pickColor");
        Color color = FxmlControl.imagePixel(p, imageView);
        if (color != null) {
            startPickingColor();
            if (paletteController != null && paletteController.getMyStage().isShowing()) {
                paletteController.addColor(color);
            }
        }
        return color;
    }

    protected void startPickingColor() {
        if (paletteController == null || !paletteController.getMyStage().isShowing()) {
            paletteController = ColorPaletteManageController.oneOpen();
            paletteController.parentController = this;
            popInformation(message("PickingColorsNow"));
            paletteController.myStage.setX(0);
            paletteController.myStage.setY(0);
            if (imageLabel != null) {
                imageLabelOriginal = new Label(imageLabel.getText());
                imageLabelOriginal.setStyle(imageLabel.getStyle());
                imageLabel.setText(message("PickingColorsNow"));
                imageLabel.setStyle(darkRedText);
            }
        }
    }

    protected void stopPickingColor() {
        if (paletteController != null) {
            paletteController.closeStage();
            paletteController = null;
        }
        if (imageLabel != null) {
            if (imageLabelOriginal != null) {
                imageLabel.setText(imageLabelOriginal.getText());
                imageLabel.setStyle(imageLabelOriginal.getStyle());
                imageLabelOriginal = null;
            } else {
                imageLabel.setText("");
                imageLabel.setStyle(null);
            }
        }
    }

    protected void popImageMenu(Node node, MouseEvent event) {
        if (needNotContextMenu || !AppVariables.getUserConfigBoolean(baseName + "ContextMenu", true)
                || node == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        List<MenuItem> items = makeImageContextMenu();
        if (items == null || items.isEmpty()) {
            return;
        }
        MenuItem menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(node, event.getScreenX(), event.getScreenY());

    }

    protected List<MenuItem> makeImageContextMenu() {
        try {
            if (imageView == null || imageView.getImage() == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("LoadedSize") + "  CTRL+1");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                loadedSize();
            });
            items.add(menu);

            menu = new MenuItem(message("PaneSize") + "  CTRL+2");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                paneSize();
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomIn") + "  CTRL+3");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                zoomIn();
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomOut") + "  CTRL+4");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                zoomIn();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public BaseImageController refresh() {
        File oldfile = sourceFile;
        ImageInformation oldInfo = imageInformation;
        Image oldImage = image;

        BaseController b = refreshBase();
        if (b == null) {
            return null;
        }
        BaseImageController c = (BaseImageController) b;
        if (oldfile != null && oldImage != null && oldInfo != null) {
            c.loadImage(oldfile, oldInfo);
        } else if (oldInfo != null) {
            c.loadImage(oldInfo);
        } else if (oldfile != null) {
            c.loadImage(oldfile);
        } else if (oldImage != null) {
            c.loadImage(oldImage);
        }

        return c;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (loadTask != null && !loadTask.isQuit()) {
            loadTask.cancel();
            loadTask = null;
        }
        if (paletteController != null) {
            paletteController.closeStage();
            paletteController = null;
        }
        return true;
    }

}
