package mara.mybox.controller;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.tools.DoubleTools.imageScale;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-8-2
 * @License Apache License Version 2.0
 */
public class ShapeRotateInputController extends BaseInputController {

    protected BaseImageController ImageController;
    protected float angle, x, y;
    protected DoubleShape shapeData;
    protected DoublePath pathData;

    @FXML
    protected ComboBox<String> angleSelector;
    @FXML
    protected TextField xInput, yInput;

    public void setParameters(BaseImageController parent, DoubleShape shapeData) {
        try {
            super.setParameters(parent, null);

            ImageController = parent;
            this.shapeData = shapeData;
            center();

            angle = UserConfig.getFloat("ShapeRotateAngle", 45);
            angleSelector.getItems().addAll(Arrays.asList(
                    "45", "30", "60", "90", "180", "270", "15", "20", "300", "330"));
            angleSelector.setValue(angle + "");
            angleSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    try {
                        angle = Float.parseFloat(angleSelector.getValue());
                        UserConfig.setFloat("ShapeRotateAngle", angle);
                    } catch (Exception e) {
                        popError(message("InvalidParameter"));
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    @FXML
    public void center() {
        if (shapeData == null) {
            return;
        }
        DoublePoint center = DoubleShape.getCenter(shapeData);
        xInput.setText(imageScale(center.getX()) + "");
        yInput.setText(imageScale(center.getY()) + "");
    }

    @FXML
    public void leftTop() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        xInput.setText(imageScale(bounds.getMinX()) + "");
        yInput.setText(imageScale(bounds.getMinY()) + "");
    }

    @FXML
    public void rightBottom() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        xInput.setText(imageScale(bounds.getMaxX()) + "");
        yInput.setText(imageScale(bounds.getMaxY()) + "");
    }

    @FXML
    public void leftBottom() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        xInput.setText(imageScale(bounds.getMinX()) + "");
        yInput.setText(imageScale(bounds.getMaxY()) + "");
    }

    @FXML
    public void rightTop() {
        if (shapeData == null) {
            return;
        }
        Rectangle2D bounds = DoubleShape.getBound(shapeData);
        xInput.setText(imageScale(bounds.getMaxX()) + "");
        yInput.setText(imageScale(bounds.getMinY()) + "");
    }

    @FXML
    @Override
    public boolean checkInput() {
        pathData = null;
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
        pathData = DoubleShape.rorate(shapeData, angle, x, y);
        return pathData != null;
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkInput()) {
            return;
        }
        ImageController.clearMask();
        ImageController.maskPathData = pathData;
        ImageController.showMaskPath();
        ImageController.maskShapeDataChanged();
        close();
    }

    public static ShapeRotateInputController open(BaseImageController parent, DoubleShape shapeData) {
        try {
            ShapeRotateInputController controller = (ShapeRotateInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ShapeRotateInputFxml, true);
            controller.setParameters(parent, shapeData);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
