package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufactureShapeController extends ImageManufactureOperationController {

    @FXML
    protected ImageManufactureShapeOptionsController optionsController;

    @Override
    public void initPane() {
        try {
            super.initPane();

            optionsController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected void paneExpanded() {
        refreshShape();
        optionsController.addListener();
    }

    @Override
    protected void paneUnexpanded() {
        optionsController.removeListener();
    }

    protected void refreshShape() {
        editor.resetImagePane();
        editor.imageTab();
        optionsController.switchShape();
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (imageView == null || imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (editor.isPickingColor) {
            Color color = ImageViewTools.imagePixel(p, imageView);
            if (color != null) {
                optionsController.strokeColorController.setColor(color);
            }
            return;
        }
        if (event.getButton() != MouseButton.SECONDARY
                || editor.shapeType != ShapeType.Lines
                || editor.maskLinesData == null) {
            return;
        }
        DoubleLines moved = editor.maskLinesData.moveTo(p.getX(), p.getY());
        if (moved != null) {
            editor.maskLinesData = moved;
            optionsController.drawLines();
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        handlePoint(event);
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        handlePoint(event);
    }

    public void handlePoint(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null || editor.isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (optionsController.coordinatePenCheck.isSelected()) {
            editor.showXY(event, p);
        }
        if (event.getButton() == MouseButton.SECONDARY || p == null
                || editor.shapeType != ShapeType.Lines
                || editor.maskLinesData == null) {
            return;
        }
        editor.scrollPane.setPannable(false);
        if (optionsController.lastPoint != null) {
            double offsetX = p.getX() - optionsController.lastPoint.getX();
            double offsetY = p.getY() - optionsController.lastPoint.getY();
            if (DoubleShape.changed(offsetX, offsetY)) {
                optionsController.drawLinePoint(p);
            }
        } else {
            editor.maskLinesData.addPoint(p);
        }
        optionsController.lastPoint = p;
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        editor.scrollPane.setPannable(true);
        if (imageView == null || imageView.getImage() == null || editor.isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (optionsController.coordinatePenCheck.isSelected()) {
            editor.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null
                || editor.shapeType != ShapeType.Lines
                || editor.maskLinesData == null) {
            return;
        }
        if (DoubleShape.changed(optionsController.lastPoint, p)) {
            editor.maskLinesData.endLine(p);
        } else {
            editor.maskLinesData.endLine(null);
        }
        optionsController.lastPoint = null;
        optionsController.drawLines();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        return optionsController.keyEventsFilter(event);
    }

    @Override
    protected void resetOperationPane() {
        refreshShape();
    }

}
