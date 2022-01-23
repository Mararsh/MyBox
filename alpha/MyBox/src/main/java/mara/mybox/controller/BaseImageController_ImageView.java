package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.sceneFontSize;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_ImageView extends BaseController {

    public static final String DefaultStrokeColor = "#c94d58", DefaultAnchorColor = "#0066cc";
    protected ImageInformation imageInformation;
    protected Image image;
    protected ImageAttributes attributes;
    protected final SimpleBooleanProperty loadNotify;
    protected boolean imageChanged, isPickingColor, operateOriginalSize;
    protected int loadWidth, defaultLoadWidth, framesNumber, frameIndex, // 0-based
            sizeChangeAware, zoomStep, xZoomStep, yZoomStep;
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
    protected CheckBox pickColorCheck, rulerXCheck, gridCheck, coordinateCheck, contextMenuCheck,
            selectAreaCheck, handleSelectCheck;
    @FXML
    protected ComboBox<String> zoomStepSelector, loadWidthBox;
    @FXML
    protected HBox operationBox;
    @FXML
    protected ControlImageRender renderController;

    public BaseImageController_ImageView() {
        baseTitle = message("Image");
        loadNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            isPickingColor = imageChanged = operateOriginalSize = false;
            loadWidth = defaultLoadWidth = -1;
            frameIndex = framesNumber = 0;
            sizeChangeAware = 1;
            zoomStep = xZoomStep = yZoomStep = 40;
            if (maskPane != null) {
                if (borderLine == null) {
                    borderLine = new Rectangle();
                    borderLine.setFill(Color.web("#ffffff00"));
                    borderLine.setStroke(Color.web("#cccccc"));
                    borderLine.setArcWidth(5);
                    borderLine.setArcHeight(5);
                    maskPane.getChildren().add(borderLine);
                }
                if (sizeText == null) {
                    sizeText = new Text();
                    sizeText.setFill(Color.web("#cccccc"));
                    sizeText.setStrokeWidth(0);
                    maskPane.getChildren().add(sizeText);
                }
                if (xyText == null) {
                    xyText = new Text();
                    maskPane.getChildren().add(xyText);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            if (zoomStepSelector != null) {
                NodeStyleTools.setTooltip(zoomStepSelector, new Tooltip(message("ZoomStep")));
            }
            if (selectAreaCheck != null) {
                NodeStyleTools.setTooltip(selectAreaCheck, new Tooltip(message("SelectArea") + "\nCTRL+t"));
            }
            if (pickColorCheck != null) {
                NodeStyleTools.setTooltip(pickColorCheck, new Tooltip(message("PickColor") + "\nCTRL+k"));
            }
            if (loadWidthBox != null) {
                NodeStyleTools.setTooltip(loadWidthBox, new Tooltip(message("ImageLoadWidthCommnets")));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void notifyLoad() {
        loadNotify.set(!loadNotify.get());
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
                    viewSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                }
            });

            imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));

                }
            });

            if (scrollPane != null) {
                scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        paneSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                    }
                });
                scrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        paneSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
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
                                zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void popImageMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null
                || !UserConfig.getBoolean(baseName + "ContextMenu", true)) {
            return;
        }
        MenuImageBaseController.open((BaseImageController) this, x, y);
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

    public void viewSizeChanged(double change) {
        if (change < sizeChangeAware
                || isSettingValues || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
    }

    public void paneSizeChanged(double change) {
        viewSizeChanged(change);
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

    protected int getRulerStep(double width) {
        if (width <= 1000) {
            return 10;
        } else if (width <= 10000) {
            return (int) (width / 1000) * 10;
        } else {
            return (int) (width / 10000) * 10;
        }
    }

    public synchronized void updateLabelsTitle() {
        try {
            updateStageTitle();
            updateLabels();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateStageTitle() {
        try {
            if (getMyStage() == null || thisPane.getParent() != null) {
                return;
            }

            String title;
            if (sourceFile != null) {
                title = getBaseTitle() + " " + sourceFile.getAbsolutePath();
                if (framesNumber > 1 && frameIndex >= 0) {
                    title += " - " + message("Frame") + " " + (frameIndex + 1);
                }
                if (imageInformation != null && imageInformation.isIsScaled()) {
                    title += " - " + message("Scaled");
                }
            } else {
                title = getBaseTitle();
            }
            if (imageChanged) {
                title += "  " + "*";
            }
            getMyStage().setTitle(title);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateLabels() {
        try {
            String imageInfo = "", fileInfo = "", loadInfo = "";
            if (sourceFile != null) {
                fileInfo = message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "\n"
                        + message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified());
            }
            if (framesNumber > 1) {
                imageInfo = message("FramesNumber") + ":" + framesNumber + "\n";
                if (frameIndex >= 0) {
                    imageInfo += message("CurrentFrame") + ":" + (frameIndex + 1) + "\n";
                }
            }
            if (imageInformation != null) {
                imageInfo += message("Format") + ":" + imageInformation.getImageFormat() + "\n"
                        + message("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight();
                if (imageInformation.isIsScaled()) {
                    imageInfo += "\n" + message("Scaled");
                }
            } else if (imageView != null && imageView.getImage() != null) {
                imageInfo += message("Pixels") + ":" + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight();
            }
            if (imageView != null && imageView.getImage() != null) {
                loadInfo = message("LoadedSize") + ":"
                        + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "\n"
                        + message("DisplayedSize") + ":"
                        + (int) imageView.getBoundsInLocal().getWidth() + "x" + (int) imageView.getBoundsInLocal().getHeight();
            }
            String more = moreDisplayInfo();
            loadInfo += (!loadInfo.isBlank() && !more.isBlank() ? "\n" : "") + more;
            if (imageChanged) {
                loadInfo += "\n" + message("ImageChanged");
            }
            if (imageInfoLabel != null) {
                String info = fileInfo + "\n" + imageInfo + "\n" + loadInfo;
                imageInfoLabel.setText(info);
                if (imageLabel != null) {
                    imageLabel.setText(loadInfo.replaceAll("\n", "  "));
                }
            } else if (imageLabel != null) {
                imageLabel.setText((fileInfo + "\n" + imageInfo + "\n" + loadInfo).replaceAll("\n", "  "));
            }
            if (imageView != null && imageView.getImage() != null) {
                if (borderLine != null) {
                    borderLine.setLayoutX(imageView.getLayoutX() - 1);
                    borderLine.setLayoutY(imageView.getLayoutY() - 1);
                    borderLine.setWidth(imageView.getBoundsInParent().getWidth() + 2);
                    borderLine.setHeight(imageView.getBoundsInParent().getHeight() + 2);
                }
                if (sizeText != null) {
                    sizeText.setText((int) (imageView.getImage().getWidth()) + "x" + (int) (imageView.getImage().getHeight()));
                    sizeText.setTextAlignment(TextAlignment.LEFT);
                    if (imageView.getImage().getWidth() >= imageView.getImage().getHeight()) {
                        sizeText.setX(borderLine.getBoundsInParent().getMinX());
                        sizeText.setY(borderLine.getBoundsInParent().getMinY() - sceneFontSize - 1);
                    } else {
                        sizeText.setX(borderLine.getBoundsInParent().getMinX() - sizeText.getBoundsInParent().getWidth() - sceneFontSize);
                        sizeText.setY(borderLine.getBoundsInParent().getMaxY() - sceneFontSize - 1);
                    }

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

    @FXML
    public void moveRight() {
        NodeTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        NodeTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        NodeTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        NodeTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    /*
        pick color
     */
    protected void checkPickingColor() {
        if (isPickingColor) {
            startPickingColor();
        } else {
            stopPickingColor();
        }
    }

    protected void startPickingColor() {
        if (paletteController == null || !paletteController.getMyStage().isShowing()) {
            paletteController = ColorsPopController.oneOpen(this);
            if (imageLabel != null) {
                imageLabelOriginal = new Label(imageLabel.getText());
                imageLabelOriginal.setStyle(imageLabel.getStyle());
                imageLabel.setText(message("PickingColorsNow"));
                imageLabel.setStyle(NodeStyleTools.darkRedText);
            } else {
                popInformation(message("PickingColorsNow"));
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

}
