package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.bufferedimage.ImageContrast.ContrastAlgorithm;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageContrast extends BaseController {

    protected ContrastAlgorithm contrastAlgorithm;
    protected int left, right, offset;

    @FXML
    protected ToggleGroup contrastGroup;
    @FXML
    protected RadioButton hsbRaido, grayEqualizationRaido, grayStretchingRaido, grayShiftingRaido;
    @FXML
    protected VBox setBox;
    @FXML
    protected FlowPane leftValuePane, rightValuePane, offsetPane;
    @FXML
    protected TextField leftInput, rightInput, offsetInput;

    @Override
    public void setControlsStyle() {
        super.setControlsStyle();
        NodeStyleTools.setTooltip(leftInput, new Tooltip("0~255"));
        NodeStyleTools.setTooltip(rightInput, new Tooltip("0~255"));
        NodeStyleTools.setTooltip(offsetInput, new Tooltip("-255~255"));
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            left = UserConfig.getInt(baseName + "LeftThreshold", 100);
            if (left < 0 || left > 255) {
                left = 100;
            }
            leftInput.setText(left + "");
            leftInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkLeft();
                    }
                }
            });

            right = UserConfig.getInt(baseName + "RightThreshold", 100);
            if (right < 0 || right > 255) {
                right = 100;
            }
            rightInput.setText(right + "");
            rightInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkRight();
                    }
                }
            });

            offset = UserConfig.getInt(baseName + "Offset", 100);
            if (offset < -255 || offset > 255) {
                offset = 100;
            }
            offsetInput.setText(offset + "");
            offsetInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkOffset();
                    }
                }
            });

            contrastGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkContrastAlgorithm();
                }
            });

            checkContrastAlgorithm();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkContrastAlgorithm() {
        try {
            contrastAlgorithm = null;
            setBox.getChildren().clear();
            leftInput.setStyle(null);
            rightInput.setStyle(null);
            offsetInput.setStyle(null);
            if (hsbRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.HSB_Histogram_Equalization;

            } else if (grayEqualizationRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Equalization;

            } else if (grayStretchingRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Stretching;
                if (!setBox.getChildren().contains(leftValuePane)) {
                    setBox.getChildren().addAll(leftValuePane, rightValuePane);
                }
                checkLeft();
                checkRight();

            } else if (grayShiftingRaido.isSelected()) {
                contrastAlgorithm = ContrastAlgorithm.Gray_Histogram_Shifting;
                if (!setBox.getChildren().contains(offsetPane)) {
                    setBox.getChildren().add(offsetPane);
                }
                checkOffset();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected boolean checkLeft() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(leftInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v <= 255) {
            left = v;
            leftInput.setStyle(null);
            UserConfig.setInt(baseName + "LeftThreshold", left);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("LeftThreshold"));
            leftInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected boolean checkRight() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(rightInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v <= 255) {
            right = v;
            rightInput.setStyle(null);
            UserConfig.setInt(baseName + "RightThreshold", right);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("RightThreshold"));
            rightInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected boolean checkOffset() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(offsetInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= -255 && v <= 255) {
            offset = v;
            offsetInput.setStyle(null);
            UserConfig.setInt(baseName + "Offset", offset);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Offset"));
            offsetInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected ImageContrast pickValues() {
        if (grayStretchingRaido.isSelected()) {
            if (!checkLeft() || !checkRight()) {
                return null;
            }
        } else if (grayShiftingRaido.isSelected()) {
            if (!checkOffset()) {
                return null;
            }
        }
        ImageContrast imageContrast = new ImageContrast()
                .setAlgorithm(contrastAlgorithm);
        if (contrastAlgorithm == ContrastAlgorithm.Gray_Histogram_Stretching) {
            imageContrast.setIntPara1(left).setIntPara2(right);

        } else if (contrastAlgorithm == ContrastAlgorithm.Gray_Histogram_Shifting) {
            imageContrast.setIntPara1(offset);
        }
        return imageContrast;
    }

}
