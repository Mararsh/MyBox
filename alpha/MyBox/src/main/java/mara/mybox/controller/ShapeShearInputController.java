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
 * @CreateDate 2023-8-16
 * @License Apache License Version 2.0
 */
public class ShapeShearInputController extends BaseShapeTransformController {

    protected float xRatio, yRatio;

    @FXML
    protected ComboBox<String> xSelector, ySelector;

    public void setParameters(BaseShapeController parent, DoubleShape shapeData) {
        try {
            super.setParameters(parent, shapeData, null);

            xRatio = UserConfig.getFloat("ShapeShearX", 0.5f);
            xSelector.getItems().addAll(Arrays.asList(
                    "0.5", "-0.5", "0", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2"));
            xSelector.setValue(xRatio + "");
            xSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    try {
                        xRatio = Float.parseFloat(xSelector.getValue());
                        UserConfig.setFloat("ShapeShearX", xRatio);
                    } catch (Exception e) {
                        popError(message("InvalidParameter"));
                    }
                }
            });

            yRatio = UserConfig.getFloat("ShapeShearY", 0f);
            ySelector.getItems().addAll(Arrays.asList(
                    "0", "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2"));
            ySelector.setValue(yRatio + "");
            ySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    try {
                        yRatio = Float.parseFloat(ySelector.getValue());
                        UserConfig.setFloat("ShapeShearY", yRatio);
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
        DoublePath pathData = DoubleShape.shear(shapeData, xRatio, yRatio);
        if (pathData == null) {
            return;
        }
        DoublePoint c = DoubleShape.getCenter(shapeData);
        if (c == null) {
            return;
        }
        DoubleShape.translateCenterAbs(pathData, c.getX(), c.getY());
        if (imageController.supportPath) {
            imageController.clearMask();
            imageController.maskPathData = pathData;
            imageController.showMaskPath();
            imageController.maskShapeDataChanged();
        } else {
            TextPopController.loadText(imageController, pathData.pathAbs());
        }
        close();
    }

    public static ShapeShearInputController open(BaseShapeController parent, DoubleShape shapeData) {
        try {
            ShapeShearInputController controller = (ShapeShearInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ShapeShearInputFxml, true);
            controller.setParameters(parent, shapeData);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
