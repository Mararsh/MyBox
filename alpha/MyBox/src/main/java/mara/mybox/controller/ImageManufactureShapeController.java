package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufactureShapeController extends ImageManufactureOperationController {

    @FXML
    protected ControlImageShapeOptions optionsController;

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
        editor.resetImagePane();
        editor.clearMaskShapesData();
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
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        mousePoint(event);
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        mousePoint(event);
    }

    public void mousePoint(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null || editor.isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (optionsController.coordinatePenCheck.isSelected()) {
            editor.showXY(event, p);
        }
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
