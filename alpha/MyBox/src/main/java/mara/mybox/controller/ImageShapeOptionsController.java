package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.controller.BaseShapeController_Base.AnchorShape;
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
    protected VBox shapeBox;
    @FXML
    protected FlowPane strokePane;
    @FXML
    protected ComboBox<String> strokeWidthSelector, anchorSizeSelector;
    @FXML
    protected ControlColorSet strokeColorController, anchorColorController;
    @FXML
    protected ToggleGroup anchorShapeGroup;
    @FXML
    protected RadioButton anchorRectRadio, anchorCircleRadio, anchorNameRadio;

    public void setParameters(BaseShapeController parent, boolean withStroke) {
        try {
            super.setParameters(parent);

            shapeController = parent;

            if (withStroke) {
                strokeWidthSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
                strokeWidthSelector.setValue(UserConfig.getFloat(baseName + "StrokeWidth", 2) + "");
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
                                UserConfig.setFloat(baseName + "StrokeWidth", v);
                                ValidationTools.setEditorNormal(strokeWidthSelector);
                                if (shapeController.shapeStyle != null) {
                                    shapeController.shapeStyle.setStrokeWidth(v);
                                }
                                shapeController.setMaskShapesStyle();
                            } else {
                                ValidationTools.setEditorBadStyle(strokeWidthSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(strokeWidthSelector);
                        }
                    }
                });

                strokeColorController.init(this, baseName + "StrokeColor", Color.web(ShapeStyle.DefaultStrokeColor));
                strokeColorController.asSaved();
                strokeColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                    @Override
                    public void changed(ObservableValue v, Paint oldValue, Paint newValue) {
                        if (isSettingValues || shapeController == null) {
                            return;
                        }
                        if (shapeController.shapeStyle != null) {
                            shapeController.shapeStyle.setStrokeColor(strokeColorController.color());
                        }
                        shapeController.setMaskAnchorsStyle();
                    }
                });
            } else {
                shapeBox.getChildren().remove(strokePane);
            }

            anchorSizeSelector.getItems().addAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
            anchorSizeSelector.setValue(UserConfig.getFloat(baseName + "AnchorSize", 10) + "");
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
                            UserConfig.setFloat(baseName + "AnchorSize", v);
                            ValidationTools.setEditorNormal(anchorSizeSelector);
                            if (shapeController.shapeStyle != null) {
                                shapeController.shapeStyle.setAnchorSize(v);
                            }
                            shapeController.setMaskAnchorsStyle();
                        } else {
                            ValidationTools.setEditorBadStyle(anchorSizeSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(anchorSizeSelector);
                    }
                }
            });

            anchorColorController.init(this, baseName + "AnchorColor", Color.web(ShapeStyle.DefaultAnchorColor));
            anchorColorController.asSaved();
            anchorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint oldValue, Paint newValue) {
                    if (isSettingValues || shapeController == null) {
                        return;
                    }
                    if (shapeController.shapeStyle != null) {
                        shapeController.shapeStyle.setAnchorColor(anchorColorController.color());
                    }
                    shapeController.setMaskAnchorsStyle();
                }
            });

            String anchorShape = UserConfig.getString(baseName + "AnchorShape", "Rectangle");
            if ("Circle".equals(anchorShape)) {
                anchorCircleRadio.setSelected(true);
                shapeController.anchorShape = AnchorShape.Circle;
            } else if ("Name".equals(anchorShape)) {
                anchorNameRadio.setSelected(true);
                shapeController.anchorShape = AnchorShape.Name;
            } else {
                anchorRectRadio.setSelected(true);
                shapeController.anchorShape = AnchorShape.Rectangle;
            }
            anchorShapeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || shapeController == null) {
                        return;
                    }
                    if (anchorCircleRadio.isSelected()) {
                        UserConfig.setString(baseName + "AnchorShape", "Rectangle");
                        shapeController.anchorShape = AnchorShape.Circle;
                    } else if (anchorNameRadio.isSelected()) {
                        UserConfig.setString(baseName + "AnchorShape", "Name");
                        shapeController.anchorShape = AnchorShape.Name;
                    } else {
                        UserConfig.setString(baseName + "AnchorShape", "Rectangle");
                        shapeController.anchorShape = AnchorShape.Rectangle;
                    }
                    shapeController.redrawMaskShape();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageShapeOptionsController open(BaseShapeController parent, boolean withStroke) {
        try {
            if (parent == null) {
                return null;
            }
            ImageShapeOptionsController controller = (ImageShapeOptionsController) WindowTools.branchStage(
                    parent, Fxmls.ImageShapeOptionsFxml);
            controller.setParameters(parent, withStroke);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
