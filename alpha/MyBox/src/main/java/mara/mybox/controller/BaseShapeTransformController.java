package mara.mybox.controller;

import java.awt.geom.Rectangle2D;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-8-16
 * @License Apache License Version 2.0
 */
public class BaseShapeTransformController extends BaseInputController {

    protected BaseShapeController imageController;
    protected float x, y;
    protected DoubleShape shapeData;
    protected DoublePoint point;

    @FXML
    protected TextField xInput, yInput;
    @FXML
    protected Button pointButton;
    @FXML
    protected Label infoLabel;

    public void setParameters(BaseShapeController parent, DoubleShape shapeData, DoublePoint point) {
        try {
            super.setParameters(parent, null);

            imageController = parent;
            this.shapeData = shapeData;
            this.point = point;
            String info = DoubleShape.values(shapeData);
            if (point != null) {
                info += "\n" + message("Point") + ": " + imageScale(point.getX()) + ", " + imageScale(point.getY());
            }
            infoLabel.setText(info);
            if (pointButton != null) {
                pointButton.setVisible(point != null);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    @FXML
    public void shapeCenter() {
        if (shapeData == null) {
            return;
        }
        DoublePoint center = DoubleShape.getCenter(shapeData);
        if (center == null) {
            return;
        }
        xInput.setText(imageScale(center.getX()) + "");
        yInput.setText(imageScale(center.getY()) + "");
    }

    @FXML
    public void shapeLeftTop() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        if (bounds == null) {
            return;
        }
        xInput.setText(imageScale(bounds.getMinX()) + "");
        yInput.setText(imageScale(bounds.getMinY()) + "");
    }

    @FXML
    public void shapeRightBottom() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        if (bounds == null) {
            return;
        }
        xInput.setText(imageScale(bounds.getMaxX()) + "");
        yInput.setText(imageScale(bounds.getMaxY()) + "");
    }

    @FXML
    public void shapeLeftBottom() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        if (bounds == null) {
            return;
        }
        xInput.setText(imageScale(bounds.getMinX()) + "");
        yInput.setText(imageScale(bounds.getMaxY()) + "");
    }

    @FXML
    public void shapeRightTop() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        if (bounds == null) {
            return;
        }
        xInput.setText(imageScale(bounds.getMaxX()) + "");
        yInput.setText(imageScale(bounds.getMinY()) + "");
    }

    @FXML
    public void imageCenter() {
        if (imageController == null) {
            return;
        }
        xInput.setText(imageScale(imageController.imageWidth() / 2) + "");
        yInput.setText(imageScale(imageController.imageHeight() / 2) + "");
    }

    @FXML
    public void imageLeftTop() {
        if (shapeData == null) {
            return;
        }
        xInput.setText("0");
        yInput.setText("0");
    }

    @FXML
    public void imageRightBottom() {
        if (shapeData == null) {
            return;
        }
        xInput.setText(imageScale(imageController.imageWidth()) + "");
        yInput.setText(imageScale(imageController.imageHeight()) + "");
    }

    @FXML
    public void imageLeftBottom() {
        if (shapeData == null) {
            return;
        }
        xInput.setText("0");
        yInput.setText(imageScale(imageController.imageHeight()) + "");
    }

    @FXML
    public void imageRightTop() {
        if (shapeData == null) {
            return;
        }
        xInput.setText(imageScale(imageController.imageWidth()) + "");
        yInput.setText("0");
    }

    @FXML
    public void point() {
        if (point == null) {
            return;
        }
        xInput.setText(imageScale(point.getX()) + "");
        yInput.setText(imageScale(point.getY()) + "");
    }

    @FXML
    @Override
    public boolean checkInput() {
        if (shapeData == null) {
            popError(message("noData"));
            return false;
        }
        try {
            x = Float.parseFloat(xInput.getText());
        } catch (Exception e) {
            popError(message("InvalidValue") + ": x");
            return false;
        }
        try {
            y = Float.parseFloat(yInput.getText());
        } catch (Exception e) {
            popError(message("InvalidValue") + ": y");
            return false;
        }
        return true;
    }

}
