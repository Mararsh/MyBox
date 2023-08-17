package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-8-16
 * @License Apache License Version 2.0
 */
public class ShapeScaleInputController extends BaseShapeTransformController {

    protected float xRatio, yRatio;

    @FXML
    protected ComboBox<String> xSelector, ySelector;

    public void setParameters(BaseImageController parent, DoubleShape shapeData) {
        try {
            super.setParameters(parent, shapeData, null);

            xRatio = UserConfig.getFloat("ShapeScaleX", 0.5f);
            xSelector.getItems().addAll(Arrays.asList(
                    "0.5", "1.5", "2", "0.2", "0.6", "0.8", "3", "5", "4"));
            xSelector.setValue(xRatio + "");
            xSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    try {
                        xRatio = Float.parseFloat(xSelector.getValue());
                        UserConfig.setFloat("ShapeScaleX", xRatio);
                    } catch (Exception e) {
                        popError(message("InvalidParameter"));
                    }
                }
            });

            yRatio = UserConfig.getFloat("ShapeScaleY", 0.5f);
            ySelector.getItems().addAll(Arrays.asList(
                    "0.5", "1.5", "2", "0.2", "0.6", "0.8", "3", "5", "4"));
            ySelector.setValue(yRatio + "");
            ySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    try {
                        yRatio = Float.parseFloat(ySelector.getValue());
                        UserConfig.setFloat("ShapeScaleY", yRatio);
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
        DoublePoint c = DoubleShape.getCenter(shapeData);
        shapeData.scale(xRatio, yRatio);
        DoubleShape.translateCenterAbs(shapeData, c.getX(), c.getY());
        imageController.drawMaskShape();
        imageController.maskShapeDataChanged();
        close();
    }

    public static ShapeScaleInputController open(BaseImageController parent, DoubleShape shapeData) {
        try {
            ShapeScaleInputController controller = (ShapeScaleInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ShapeScaleInputFxml, true);
            controller.setParameters(parent, shapeData);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
