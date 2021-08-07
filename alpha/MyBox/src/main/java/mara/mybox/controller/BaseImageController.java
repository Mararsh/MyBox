package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import static mara.mybox.fxml.NodeStyleTools.darkRedText;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController extends BaseController {

    public static final String DefaultStrokeColor = "#c94d58", DefaultAnchorColor = "#0066cc";
    protected ImageInformation imageInformation;
    protected Image image;
    protected ImageAttributes attributes;
    protected boolean isPickingColor, imageChanged, operateOriginalSize,
            needNotRulers, needNotCoordinates, needNotContextMenu;
    protected int loadWidth, defaultLoadWidth, framesNumber, frameIndex, sizeChangeAware = 10,
            zoomStep, xZoomStep, yZoomStep;
    protected LoadingController loadingController;
    protected SingletonTask loadTask;
    protected double mouseX, mouseY;
    protected ColorsManageController paletteController;
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
    protected Label imageLabel, imageInfoLabel;
    @FXML
    protected Button imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton,
            rotateLeftButton, rotateRightButton, turnOverButton;
    @FXML
    protected CheckBox pickColorCheck, rulerXCheck, rulerYCheck, coordinateCheck,
            contextMenuCheck;
    @FXML
    protected ComboBox<String> zoomStepSelector;

    public BaseImageController() {
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            isPickingColor = imageChanged = operateOriginalSize
                    = needNotRulers = needNotCoordinates = needNotContextMenu = false;
            loadWidth = defaultLoadWidth = -1;
            frameIndex = framesNumber = 0;
            sizeChangeAware = 10;
            zoomStep = xZoomStep = yZoomStep = 40;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
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
//            fitSize();
            drawMaskControls();
            setImageChanged(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            if (zoomStepSelector != null) {
                NodeStyleTools.setTooltip(zoomStepSelector, new Tooltip(Languages.message("ZoomStep")));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean controlAltK() {
        if (pickColorCheck != null) {
            pickColorCheck.setSelected(!pickColorCheck.isSelected());
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAlt1() {
        if (imageSizeButton != null) {
            if (!imageSizeButton.isDisabled()) {
                loadedSize();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAlt2() {
        if (paneSizeButton != null) {
            if (!paneSizeButton.isDisabled()) {
                paneSize();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAlt3() {
        if (zoomInButton != null) {
            if (!zoomInButton.isDisabled()) {
                zoomIn();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAlt4() {
        if (zoomOutButton != null) {
            if (!zoomOutButton.isDisabled()) {
                zoomOut();
            }
            return true;
        }
        return false;
    }

    protected void initImageView() {
        if (imageView == null) {
            return;
        }
        try {
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                }
            });
            imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));

                }
            });
            if (scrollPane != null) {
                scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        viewSizeChanged(Math.abs(new_val.intValue() - old_val.intValue()));
                    }
                });
                scrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
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
                rulerXCheck.setSelected(UserConfig.getBoolean(baseName + "RulerX", false));
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "RulerX", rulerXCheck.isSelected());
                        checkRulerX();
                    }
                });
                checkRulerX();
            }
            if (rulerYCheck != null) {
                rulerYCheck.setSelected(UserConfig.getBoolean(baseName + "RulerY", false));
                rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "RulerY", rulerYCheck.isSelected());
                        checkRulerY();
                    }
                });
                checkRulerY();
            }

            if (coordinateCheck != null) {
                coordinateCheck.setSelected(UserConfig.getBoolean(baseName + "PopCooridnate", false));
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "PopCooridnate", coordinateCheck.isSelected());
                        checkCoordinate();
                    }
                });
                checkCoordinate();
            }

            if (contextMenuCheck != null) {
                contextMenuCheck.setSelected(UserConfig.getBoolean(baseName + "ContextMenu", true));
                contextMenuCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ContextMenu", contextMenuCheck.isSelected());
                    }
                });
            }

            zoomStep = UserConfig.getInt(baseName + "ZoomStep", 40);
            zoomStep = zoomStep <= 0 ? 40 : zoomStep;
            xZoomStep = zoomStep;
            yZoomStep = zoomStep;
            if (zoomStepSelector != null) {
                zoomStepSelector.getItems().addAll(
                        Arrays.asList("40", "20", "5", "1", "3", "15", "30", "50", "80", "100", "150", "200", "300", "500")
                );
                zoomStepSelector.setValue(zoomStep + "");
                zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                        try {
                            int v = Integer.valueOf(newVal);
                            if (v > 0) {
                                zoomStep = v;
                                UserConfig.setInt(baseName + "ZoomStep", zoomStep);
                                zoomStepSelector.getEditor().setStyle(null);
                                xZoomStep = zoomStep;
                                yZoomStep = zoomStep;
                                zoomStepChanged();
                            } else {
                                zoomStepSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                            }
                        } catch (Exception e) {
                            zoomStepSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void zoomStepChanged() {
    }

    protected void setZoomStep(Image image) {
        if (image == null) {
            return;
        }
        zoomStep = (int) image.getWidth() / 10;
        if (zoomStepSelector != null) {
            zoomStepSelector.setValue(zoomStep + "");
        } else {
            xZoomStep = (int) image.getWidth() / 10;
            yZoomStep = (int) image.getHeight() / 10;
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
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        frameIndex = 0;
        framesNumber = 0;
        loadImageFile(file, loadWidth);
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
        if (UserConfig.getBoolean(baseName + "RulerX", false)) {
            Color strokeColor = Color.web(UserConfig.getString("StrokeColor", "#FF0000"));
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
        if (UserConfig.getBoolean(baseName + "RulerY", false)) {
            Color strokeColor = Color.web(UserConfig.getString("StrokeColor", "#FF0000"));
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
                Color strokeColor = Color.web(UserConfig.getString("StrokeColor", DefaultStrokeColor));
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
        LocateTools.moveCenter(scrollPane, imageView);
        scrollPane.setVvalue(scrollPane.getVmin());
        updateLabelsTitle();
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
            ImageViewTools.imageSize(scrollPane, imageView);
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
            ImageViewTools.paneSize(scrollPane, imageView);
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
        ImageViewTools.zoomIn(scrollPane, imageView, xZoomStep, yZoomStep);
        refinePane();
    }

    @FXML
    public void zoomOut() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        ImageViewTools.zoomOut(scrollPane, imageView, xZoomStep, yZoomStep);
        refinePane();
    }

    public void loadImageFile(File file) {
        loadImageFile(file, loadWidth);
    }

    public void loadImageFile(File file, int width) {
        loadImage(file, false, width, frameIndex);
    }

    public void loadImageFile(File file, boolean onlyInformation) {
        loadImage(file, onlyInformation, loadWidth, frameIndex);
    }

    public void loadImageFile(File file, int width, int index) {
        loadImage(file, false, width, index);
    }

    // 0-based
    public void loadImage(File file, boolean onlyInformation, int width, int index) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            loadTask = new SingletonTask<Void>() {
                private ImageInformation targetInfo;

                @Override
                protected boolean handle() {
                    targetInfo = null;
                    Object ret = ImageFileReaders.readFrame(file, onlyInformation, index, width, imageInformation);
                    if (ret == null) {
                        return false;
                    } else if (ret instanceof ImageInformation) {
                        targetInfo = (ImageInformation) ret;
                        return targetInfo != null;
                    } else if (ret instanceof Exception) {
                        error = ((Exception) ret).toString();
                        return false;
                    } else {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    recordFileOpened(file);
                    if (targetInfo.isNeedSample()) {
                        askSample(targetInfo);
                    } else {
                        sourceFile = file;
                        imageInformation = targetInfo;
                        image = targetInfo.getThumbnail();
                        afterInfoLoaded();
                        afterImageLoaded();
                    }
                }

            };
            handling(loadTask);
            Thread thread = new Thread(loadTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public void askSample(ImageInformation imageInfo) {
        ImageTooLargeController controller = (ImageTooLargeController) openChildStage(Fxmls.ImageTooLargeFxml, true);
        controller.setParameters(this, imageInfo);
    }

    public void loadImage(File sourceFile, ImageInformation imageInformation) {
        if (imageInformation == null) {
            loadImageFile(sourceFile);
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
                    image = imageInformation.loadThumbnail(loadWidth);
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    afterImageLoaded();
                    setImageChanged(exist);
                }

            };
            loadingController = handling(loadTask);
            Thread thread = new Thread(loadTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public void loadImageInfo(ImageInformation imageInformation) {
        if (imageInformation == null) {
            loadImageFile(sourceFile);
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

    public void loadImage(File sourceFile, ImageInformation imageInformation, Image image, boolean changed) {
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        this.image = image;
        afterImageLoaded();
        setImageChanged(changed);
    }

    public void loadImage(Image inImage, int maxWidth) {
        sourceFile = null;
        imageInformation = null;
        image = ScaleTools.scaleImage(inImage, maxWidth);
        loadWidth = maxWidth;
        afterImageLoaded();
        setImageChanged(true);
    }

    public void loadFrame(int index) {
        loadImage(sourceFile, false, loadWidth, index);
    }

    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;
            updateLabelsTitle();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void afterInfoLoaded() {

    }

    public boolean afterImageLoaded() {
        if (image != null) {
            setZoomStep(image);
        }

        if (imageInformation != null) {
            frameIndex = imageInformation.getIndex();
            if (imageInformation.getImageFileInformation() != null) {
                framesNumber = imageInformation.getImageFileInformation().getNumberOfImages();
            }
        } else if (image != null) {
            frameIndex = 0;
            framesNumber = 1;
        } else {
            frameIndex = 0;
            framesNumber = 0;
        }
        return true;
    }

    public void loadMultipleFramesImage(File file) {
        if (file == null || !file.exists() || file.length() == 0) {
            return;
        }
        ImagesEditorController controller = (ImagesEditorController) openStage(Fxmls.ImagesEditorFxml);
        controller.open(file);
    }

    public void updateLabelsTitle() {
        try {
            if (getMyStage() == null) {
                return;
            }
            String title;
            if (sourceFile != null) {
                title = getBaseTitle() + " " + sourceFile.getAbsolutePath();
                if (framesNumber > 1 && frameIndex >= 0) {
                    title += " - " + Languages.message("Frame") + " " + (frameIndex + 1);
                }
                if (imageInformation != null && imageInformation.isIsScaled()) {
                    title += " - " + Languages.message("Scaled");
                }
            } else {
                title = getBaseTitle();
            }
            if (imageChanged) {
                title += "  " + "*";
            }
            getMyStage().setTitle(title);

            String imageInfo = "", fileInfo = "", loadInfo = "";
            if (sourceFile != null) {
                fileInfo = Languages.message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "\n"
                        + Languages.message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified());
            }
            if (framesNumber > 1) {
                imageInfo = Languages.message("FramesNumber") + ":" + framesNumber + "\n";
                if (frameIndex >= 0) {
                    imageInfo += Languages.message("CurrentFrame") + ":" + (frameIndex + 1) + "\n";
                }
            }
            if (imageInformation != null) {
                imageInfo += Languages.message("Format") + ":" + imageInformation.getImageFormat() + "\n"
                        + Languages.message("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight();
                if (imageInformation.isIsScaled()) {
                    imageInfo += "\n" + Languages.message("Scaled");
                }
            } else if (imageView != null && imageView.getImage() != null) {
                imageInfo += Languages.message("Pixels") + ":" + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight();
            }
            if (imageView != null && imageView.getImage() != null) {
                loadInfo = Languages.message("LoadedSize") + ":"
                        + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "\n"
                        + Languages.message("DisplayedSize") + ":"
                        + (int) imageView.getBoundsInLocal().getWidth() + "x" + (int) imageView.getBoundsInLocal().getHeight();
            }
            String more = moreDisplayInfo();
            loadInfo += (!loadInfo.isBlank() && !more.isBlank() ? "\n" : "") + more;
            if (imageInfoLabel != null) {
                String info = fileInfo + "\n" + imageInfo + "\n" + loadInfo;
                if (imageChanged) {
                    info += "\n" + Languages.message("ImageChanged");
                }
                imageInfoLabel.setText(info);
                if (bottomLabel != null) {
                    bottomLabel.setText(loadInfo.replaceAll("\n", "  "));
                }
            } else if (bottomLabel != null) {
                bottomLabel.setText((fileInfo + "\n" + imageInfo + "\n" + loadInfo).replaceAll("\n", "  "));
            }
            if (imageView != null && imageView.getImage() != null) {
                if (borderLine != null) {
                    borderLine.setLayoutX(imageView.getLayoutX() - 1);
                    borderLine.setLayoutY(imageView.getLayoutY() - 1);
                    borderLine.setWidth(imageView.getBoundsInParent().getWidth() + 2);
                    borderLine.setHeight(imageView.getBoundsInParent().getHeight() + 2);
                }
                if (sizeText != null) {
                    sizeText.setX(borderLine.getLayoutX() + 1);
                    sizeText.setY(borderLine.getLayoutY() + borderLine.getHeight() + 5);
                    sizeText.setText((int) (imageView.getImage().getWidth()) + "x" + (int) (imageView.getImage().getHeight()));
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected String moreDisplayInfo() {
        return "";
    }

    @FXML
    @Override
    public void popAction() {
        if (image == null) {
            return;
        }
        ImageViewerController controller = (ImageViewerController) openStage(Fxmls.ImagePopupFxml);
        controller.setAsPopup(baseName + "Pop");
        controller.loadImage(sourceFile, imageInformation, image, imageChanged);
    }

    @FXML
    public DoublePoint showXY(MouseEvent event) {
        if (xyText == null || !xyText.isVisible()) {
            return null;
        }
        if (!isPickingColor
                && (needNotCoordinates || !UserConfig.getBoolean(baseName + "PopCooridnate", false))) {
            xyText.setText("");
            return null;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
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
                + FxColorTools.colorDisplaySimple(color);
        if (isPickingColor) {
            s = Languages.message("PickingColorsNow") + "\n" + s;
        }
        xyText.setText(s);
        xyText.setX(event.getX() + 10);
        xyText.setY(event.getY());
        return p;
    }

    public IntPoint getImageXYint(MouseEvent event, ImageView view) {
        DoublePoint p = ImageViewTools.getImageXY(event, view);
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
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
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
        if (needNotContextMenu || !UserConfig.getBoolean(baseName + "ContextMenu", true)
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

    protected Color pickColor(DoublePoint p, ImageView view) {
        Color color = ImageViewTools.imagePixel(p, view);
        if (color != null) {
            startPickingColor();
            if (paletteController != null && paletteController.getMyStage().isShowing()) {
                paletteController.colorsController.addColor(color);
            }
        }
        return color;
    }

    protected void startPickingColor() {
        if (paletteController == null || !paletteController.getMyStage().isShowing()) {
            paletteController = ColorsPopController.oneOpen(this);
            if (imageLabel != null) {
                imageLabelOriginal = new Label(imageLabel.getText());
                imageLabelOriginal.setStyle(imageLabel.getStyle());
                imageLabel.setText(Languages.message("PickingColorsNow"));
                imageLabel.setStyle(NodeStyleTools.darkRedText);
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
        if (needNotContextMenu || !UserConfig.getBoolean(baseName + "ContextMenu", true)
                || node == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        List<MenuItem> items = makeImageContextMenu();
        if (items == null || items.isEmpty()) {
            return;
        }
        MenuItem menu = new MenuItem(Languages.message("PopupClose"));
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

            menu = new MenuItem(Languages.message("LoadedSize") + "  CTRL+1");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                loadedSize();
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("PaneSize") + "  CTRL+2");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                paneSize();
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("ZoomIn") + "  CTRL+3");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                zoomIn();
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("ZoomOut") + "  CTRL+4");
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
