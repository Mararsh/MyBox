package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-11-6
 * @License Apache License Version 2.0
 */
public class ImageShapeOptionsController extends ImageOptionsController {

    protected BaseShapeController shapeController;

    @FXML
    protected ComboBox<String> strokeWidthSelector, anchorSizeSelector;
    @FXML
    protected ControlColorSet strokeColorController, anchorColorController;

    public void setParameters(BaseShapeController parent) {
        try {
            super.setParameters(parent);

            shapeController = parent;

            strokeWidthSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
            strokeWidthSelector.setValue(UserConfig.getFloat("StrokeWidth", 2) + "");
            strokeWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || shapeController == null
                            || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        float v = Float.parseFloat(newValue);
                        if (v > 0) {
                            UserConfig.setFloat("StrokeWidth", v);
                            ValidationTools.setEditorNormal(strokeWidthSelector);
                            shapeController.setMaskShapesStyle();
                        } else {
                            ValidationTools.setEditorBadStyle(strokeWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(strokeWidthSelector);
                    }
                }
            });

            strokeColorController.init(this, "StrokeColor", Color.web(ShapeStyle.DefaultStrokeColor));
            strokeColorController.asSaved();
            strokeColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint oldValue, Paint newValue) {
                    if (isSettingValues || shapeController == null) {
                        return;
                    }
                    shapeController.setMaskAnchorsStyle();
                }
            });

            anchorSizeSelector.getItems().addAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
            anchorSizeSelector.setValue(UserConfig.getFloat("AnchorSize", 10) + "");
            anchorSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || shapeController == null
                            || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        float v = Float.parseFloat(newValue);
                        if (v > 0) {
                            UserConfig.setFloat("AnchorSize", v);
                            ValidationTools.setEditorNormal(anchorSizeSelector);
                            shapeController.setMaskAnchorsStyle();
                        } else {
                            ValidationTools.setEditorBadStyle(anchorSizeSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(anchorSizeSelector);
                    }
                }
            });

            anchorColorController.init(this, "AnchorColor", Color.web(ShapeStyle.DefaultAnchorColor));
            anchorColorController.asSaved();
            anchorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint oldValue, Paint newValue) {
                    if (isSettingValues || shapeController == null) {
                        return;
                    }
                    shapeController.setMaskAnchorsStyle();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageShapeOptionsController open(BaseShapeController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageShapeOptionsController controller = (ImageShapeOptionsController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageShapeOptionsFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
