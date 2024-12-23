package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.LocateTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.AppVariables.sceneFontSize;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Base extends BaseFileController {

    protected ImageInformation imageInformation;
    protected Image image;
    protected ImageAttributes attributes;
    protected final SimpleBooleanProperty loadNotify, sizeNotify;
    protected boolean imageChanged, isPickingColor, backgroundLoad;
    protected int loadWidth, defaultLoadWidth, framesNumber, frameIndex, // 0-based
            sizeChangeAware, zoomStep, xZoomStep, yZoomStep;
    protected FxTask loadTask;
    protected double mouseX, mouseY;
    protected ColorsPickingController paletteController;

    @FXML
    protected VBox imageBox;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected AnchorPane maskPane;
    @FXML
    protected ImageView imageView;
    @FXML
    protected Rectangle borderLine;
    @FXML
    protected Text sizeText, xyText;
    @FXML
    protected Label imageLabel;
    @FXML
    protected Button imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton, selectPixelsButton;
    @FXML
    protected CheckBox pickColorCheck, rulerXCheck, gridCheck, coordinateCheck;
    @FXML
    protected ComboBox<String> zoomStepSelector, loadWidthSelector;

    public BaseImageController_Base() {
        loadNotify = new SimpleBooleanProperty(false);
        sizeNotify = new SimpleBooleanProperty(false);
    }

    public void notifyLoad() {
        loadNotify.set(!loadNotify.get());
    }

    public void notifySize() {
        sizeNotify.set(!sizeNotify.get());
    }

    public void fitSize() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            if (scrollPane.getHeight() < imageHeight()
                    || scrollPane.getWidth() < imageWidth()) {
                paneSize();
            } else {
                loadedSize();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
        }
    }

    protected void popContextMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageViewController.imageViewMenu((BaseImageController) this, x, y);
    }


    /*
        status
     */
    protected void zoomStepChanged() {
        xZoomStep = zoomStep;
        yZoomStep = zoomStep;
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
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
        notifySize();
    }

    public void paneSizeChanged(double change) {
        viewSizeChanged(change);
    }

    public void refinePane() {
        if (isSettingValues || scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        LocateTools.moveCenter(scrollPane, imageView);
        scrollPane.setVvalue(scrollPane.getVmin());
        updateLabelsTitle();
    }

    public synchronized void updateLabelsTitle() {
        try {
            updateStageTitle();
            updateImageLabel();
            updateSizeLabel();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateStageTitle() {
        try {
            if (getMyStage() == null || !isTopPane) {
                return;
            }
            myStage.setTitle(getBaseTitle() + fileTitle());
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public String fileTitle() {
        try {
            String title;
            if (sourceFile != null) {
                title = " " + sourceFile.getAbsolutePath();
                if (framesNumber > 1 && frameIndex >= 0) {
                    title += " - " + message("Frame") + " " + (frameIndex + 1);
                }
                if (imageInformation != null && imageInformation.isIsScaled()) {
                    title += " - " + message("Scaled");
                }
            } else {
                title = "";
            }
            if (imageChanged) {
                title += "  " + "*";
            }
            return title;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return "";
        }
    }

    public void updateImageLabel() {
        try {
            if (imageLabel == null) {
                return;
            }
            String imageInfo = "", fileInfo = "", loadInfo = "";
            if (sourceFile != null) {
                fileInfo = message("File") + ":" + sourceFile.getAbsolutePath() + "\n"
                        + message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "\n"
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
                        + message("Pixels") + ":" + (int) imageInformation.getWidth() + "x" + (int) imageInformation.getHeight();
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
                        + (int) imageView.getBoundsInParent().getWidth() + "x" + (int) imageView.getBoundsInParent().getHeight();
            }
            String more = moreDisplayInfo();
            loadInfo += (!loadInfo.isBlank() && !more.isBlank() ? "\n" : "") + more;
            if (imageChanged) {
                loadInfo += "\n" + message("ImageChanged");
            }
            String finalInfo = fileInfo + "\n" + imageInfo + "\n" + loadInfo;
            imageLabel.setText(StringTools.replaceLineBreak(finalInfo));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateSizeLabel() {
        try {
            if (imageView == null || imageView.getImage() == null) {
                return;
            }
            if (borderLine != null) {
                borderLine.setLayoutX(imageView.getLayoutX() - 1);
                borderLine.setLayoutY(imageView.getLayoutY() - 1);
                borderLine.setWidth(viewWidth() + 2);
                borderLine.setHeight(viewHeight() + 2);
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
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected String moreDisplayInfo() {
        return "";
    }

    /*
        values
     */
    public double imageWidth() {
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

    public double imageHeight() {
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

    public double viewWidth() {
        return imageView.getBoundsInParent().getWidth();
    }

    public double viewHeight() {
        return imageView.getBoundsInParent().getHeight();
    }

    protected boolean operateOriginalSize() {
        return (this instanceof ImageSplitController)
                || (this instanceof ImageSampleController);
    }

    public double widthRatio() {
        if (!operateOriginalSize() || imageInformation == null || image == null) {
            return 1;
        }
        double ratio = imageWidth() / imageInformation.getWidth();
        return ratio;
    }

    public double heightRatio() {
        if (!operateOriginalSize() || imageInformation == null || image == null) {
            return 1;
        }
        double ratio = imageHeight() / imageInformation.getHeight();
        return ratio;
    }

    public int operationWidth() {
        return (int) (imageWidth() / widthRatio());
    }

    public int operationHeight() {
        return (int) (imageHeight() / heightRatio());
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

}
