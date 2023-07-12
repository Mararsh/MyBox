package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufacturePenController extends ImageManufactureOperationController {

    @FXML
    protected ImageManufacturePenOptionsController optionsController;

    @Override
    public void initPane() {
        try {
            super.initPane();

            optionsController.setParameters(imageController);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        refreshShape();
    }

    protected void refreshShape() {
        imageController.resetImagePane();
        imageController.imageTab();
        optionsController.switchShape();
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (imageView == null || imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (imageController.isPickingColor) {
            Color color = ImageViewTools.imagePixel(p, imageView);
            if (color != null) {
                optionsController.strokeColorController.setColor(color);
            }
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (optionsController.coordinatePenCheck.isSelected()) {
            imageController.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null
                || imageController.maskShape == null) {
            return;
        }
        if (imageController.maskShape instanceof DoubleLines) {
            imageController.scrollPane.setPannable(false);
            if (imageController.maskPenData != null
                    && optionsController.drawLine(p) != null) {
                imageController.maskPenData.addPoint(p);
            }
        }
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (optionsController.coordinatePenCheck.isSelected()) {
            imageController.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null
                || imageController.maskShape == null) {
            return;
        }
        if (imageController.maskShape instanceof DoubleLines) {
            imageController.scrollPane.setPannable(false);
            if (imageController.maskPenData != null
                    && optionsController.drawLine(p) != null) {
                imageController.maskPenData.addPoint(p);
            }
        }
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        imageController.scrollPane.setPannable(true);
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (optionsController.coordinatePenCheck.isSelected()) {
            imageController.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null
                || imageController.maskShape == null) {
            return;
        }
        if (imageController.maskShape instanceof DoubleLines) {
            if (imageController.maskPenData != null) {
                imageController.maskPenData.endLine(p);
                optionsController.drawLines();
            }
            optionsController.lastPoint = null;
        }
    }

    @Override
    protected void resetOperationPane() {
        refreshShape();
    }

}
