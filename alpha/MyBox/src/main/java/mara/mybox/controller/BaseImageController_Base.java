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
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageAttributes;
import mara.mybox.image.data.ImageInformation;

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
    protected Button loadButton, imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton, selectPixelsButton;
    @FXML
    protected CheckBox pickColorCheck, rulerXCheck, gridCheck, grid9Check, grid4Check, coordinateCheck;
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
