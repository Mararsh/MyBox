package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class ControlImageOptions extends BaseController {

    @FXML
    protected ControlColorSet strokeColorController, anchorColorController, rulerColorController, gridColorController;
    @FXML
    protected ComboBox<String> strokeWidthSelector, anchorSizeSelector, gridWidthSelector,
            gridIntervalSelector, gridOpacitySelector, decimalSelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            setControls();

            asSaved();

            thisPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    asSaved();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setControls() {
        try {
            strokeWidthSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
            strokeWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        float v = Float.parseFloat(newValue);
                        if (v > 0) {
                            UserConfig.setFloat("StrokeWidth", v);
                            ValidationTools.setEditorNormal(strokeWidthSelector);
                            BaseImageController.updateMaskStrokes();
                        } else {
                            ValidationTools.setEditorBadStyle(strokeWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(strokeWidthSelector);
                    }
                }
            });

            strokeColorController.init(this, "StrokeColor", Color.web(ShapeStyle.DefaultStrokeColor));
            strokeColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint oldValue, Paint newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskStrokes();
                }
            });

            anchorSizeSelector.getItems().addAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
            anchorSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        float v = Float.parseFloat(newValue);
                        if (v > 0) {
                            UserConfig.setFloat("AnchorSize", v);
                            ValidationTools.setEditorNormal(anchorSizeSelector);
                            BaseImageController.updateMaskAnchors();
                        } else {
                            ValidationTools.setEditorBadStyle(anchorSizeSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(anchorSizeSelector);
                    }
                }
            });

            anchorColorController.init(this, "AnchorColor", Color.web(ShapeStyle.DefaultAnchorColor));
            anchorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint oldValue, Paint newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskAnchors();
                }
            });

            rulerColorController.init(this, "RulerColor", Color.RED);
            rulerColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskRulerXY();
                }
            });

            gridColorController.init(this, "GridLinesColor", Color.LIGHTGRAY);
            gridColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (isSettingValues) {
                        return;
                    }
                    BaseImageController.updateMaskGrid();
                }
            });

            gridWidthSelector.getItems().addAll(Arrays.asList("2", "1", "3", "4", "5", "6", "7", "8", "9", "10"));
            gridWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            UserConfig.setInt("GridLinesWidth", v);
                            ValidationTools.setEditorNormal(gridWidthSelector);
                            BaseImageController.updateMaskGrid();
                        } else {
                            ValidationTools.setEditorBadStyle(gridWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(gridWidthSelector);
                    }
                }
            });

            gridIntervalSelector.getItems().addAll(Arrays.asList(message("Automatic"), "10", "20", "25", "50", "100", "5", "1", "2", "200", "500"));
            gridIntervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    int v = -1;
                    try {
                        if (!message("Automatic").equals(newValue)) {
                            v = Integer.parseInt(newValue);
                        }
                    } catch (Exception e) {
                    }
                    UserConfig.setInt("GridLinesInterval", v);
                    BaseImageController.updateMaskGrid();
                }
            });

            gridOpacitySelector.getItems().addAll(Arrays.asList("0.5", "0.2", "1.0", "0.7", "0.1", "0.3", "0.8", "0.9", "0.6", "0.4"));
            gridOpacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    float v = 0.1f;
                    try {
                        v = Float.parseFloat(newValue);
                    } catch (Exception e) {
                    }
                    UserConfig.setFloat("GridLinesOpacity", v);
                    BaseImageController.updateMaskGrid();
                }
            });

            decimalSelector.getItems().addAll(Arrays.asList("2", "1", "3", "0", "4", "5", "6", "7", "8"));
            decimalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || newValue == null || newValue.isEmpty()) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            UserConfig.setInt("ImageDecimal", v);
                            ValidationTools.setEditorNormal(decimalSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(decimalSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(decimalSelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void asSaved() {
        try {
            isSettingValues = true;

            strokeWidthSelector.setValue(UserConfig.getFloat("StrokeWidth", 2) + "");
            anchorSizeSelector.setValue(UserConfig.getFloat("AnchorSize", 10) + "");
            gridWidthSelector.setValue(UserConfig.getInt("GridLinesWidth", 1) + "");
            int gi = UserConfig.getInt("GridLinesInterval", -1);
            gridIntervalSelector.setValue(gi <= 0 ? message("Automatic") : gi + "");
            gridOpacitySelector.setValue(UserConfig.getFloat("GridLinesOpacity", 0.1f) + "");
            decimalSelector.setValue(UserConfig.imageScale() + "");

            strokeColorController.asSaved();
            anchorColorController.asSaved();
            gridColorController.asSaved();

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
