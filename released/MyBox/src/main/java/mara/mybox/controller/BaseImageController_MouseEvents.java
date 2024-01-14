package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
import mara.mybox.fximage.ImageViewTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_MouseEvents extends BaseImageController_Mask {

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (p == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (isPickingColor) {
            pickColor(p, imageView);
            return;
        }
        if (event.getClickCount() == 1) {
            imageSingleClicked(event, p);
        }
    }

    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        if (event == null || p == null) {
            return;
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            popContextMenu(event.getScreenX(), event.getScreenY());
        }
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
            paletteController = ColorsPickingController.oneOpen(this);
        }
        imageView.setCursor(Cursor.HAND);
    }

    protected void stopPickingColor() {
        if (paletteController != null) {
            paletteController.closeStage();
            paletteController = null;
        }
        imageView.setCursor(Cursor.DEFAULT);
    }

    protected Color pickColor(DoublePoint p, ImageView view) {
        Color color = ImageViewTools.imagePixel(p, view);
        if (color != null) {
            startPickingColor();
            if (paletteController != null && paletteController.getMyStage().isShowing()) {
                paletteController.pickColor(color);
            }
        }
        return color;
    }

}
