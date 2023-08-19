package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-8-15
 * @License Apache License Version 2.0
 */
public class ShapeRotateInputController extends BaseShapeTransformController {

    protected float angle;

    @FXML
    protected ComboBox<String> angleSelector;

    @Override
    public void setParameters(BaseImageController parent, DoubleShape shapeData, DoublePoint point) {
        try {
            super.setParameters(parent, shapeData, point);

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
    @Override
    public void okAction() {
        DoublePath pathData = DoubleShape.rorate(shapeData, angle, x, y);
        if (pathData == null) {
            return;
        }
        DoublePoint c = DoubleShape.getCenter(shapeData);
        DoubleShape.translateCenterAbs(pathData, c.getX(), c.getY());
        if (imageController.supportPath) {
            imageController.clearMask();
            imageController.maskPathData = pathData;
            imageController.maskShapeDataChanged();
        } else {
            TextPopController.loadText(imageController, pathData.svgAbs());
        }
        close();
    }

    public static ShapeRotateInputController open(BaseImageController parent, DoubleShape shapeData, DoublePoint point) {
        try {
            ShapeRotateInputController controller = (ShapeRotateInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ShapeRotateInputFxml, true);
            controller.setParameters(parent, shapeData, point);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
