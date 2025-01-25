package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.image.data.ImageContrast;
import mara.mybox.image.data.ImageContrast.ContrastAlgorithm;
import mara.mybox.db.DerbyBase;
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
    protected long threshold;
    protected int offset, percentage;

    @FXML
    protected ToggleGroup aGroup;
    @FXML
    protected RadioButton saturationRaido, brightnessRaido, saturationBrightnessRaido, grayRaido,
            equalizationRaido, stretchingRaido, shiftingRaido;
    @FXML
    protected VBox setBox;
    @FXML
    protected FlowPane thresholdPane, percentagePane, offsetPane;
    @FXML
    protected TextField thresholdInput, percentageInput, offsetInput;

    public ControlImageContrast() {
        TipsLabelKey = "ImageContrastComments";
    }

    @Override
    public void setControlsStyle() {
        super.setControlsStyle();
        NodeStyleTools.setTooltip(percentageInput, new Tooltip("1~49"));
        NodeStyleTools.setTooltip(offsetInput, new Tooltip("-255~255"));
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            threshold = UserConfig.getLong(baseName + "Threshold", 1000);
            if (threshold < 0) {
                threshold = 1000;
            }
            thresholdInput.setText(threshold + "");
            thresholdInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkThreshold();
                    }
                }
            });

            percentage = UserConfig.getInt(baseName + "Percentage", 5);
            if (percentage <= 0) {
                percentage = 5;
            }
            percentageInput.setText(percentage + "");
            percentageInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkPercentage();
                    }
                }
            });

            offset = UserConfig.getInt(baseName + "Offset", 5);
            if (offset < -255 || offset > 255) {
                offset = 5;
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

            aGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
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

    protected void forImage(Image image) {
        long size = (long) (image.getWidth() * image.getHeight());
        thresholdInput.setText("" + size / 2);
    }

    protected boolean checkContrastAlgorithm() {
        try {
            contrastAlgorithm = null;
            setBox.getChildren().clear();
            thresholdInput.setStyle(null);
            percentageInput.setStyle(null);
            offsetInput.setStyle(null);

            if (saturationRaido.isSelected()) {
                if (equalizationRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.SaturationHistogramEqualization;
                } else if (stretchingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.SaturationHistogramStretching;
                } else if (shiftingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.SaturationHistogramShifting;
                }

            } else if (brightnessRaido.isSelected()) {
                if (equalizationRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.BrightnessHistogramEqualization;
                } else if (stretchingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.BrightnessHistogramStretching;
                } else if (shiftingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.BrightnessHistogramShifting;
                }

            } else if (saturationBrightnessRaido.isSelected()) {
                if (equalizationRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.SaturationBrightnessHistogramEqualization;
                } else if (stretchingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.SaturationBrightnessHistogramStretching;
                } else if (shiftingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.SaturationBrightnessHistogramShifting;
                }

            } else if (grayRaido.isSelected()) {
                if (equalizationRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.GrayHistogramEqualization;
                } else if (stretchingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.GrayHistogramStretching;
                } else if (shiftingRaido.isSelected()) {
                    contrastAlgorithm = ContrastAlgorithm.GrayHistogramShifting;
                }

            }

            if (stretchingRaido.isSelected()) {
                if (!setBox.getChildren().contains(thresholdPane)) {
                    setBox.getChildren().addAll(thresholdPane, percentagePane);
                }
                return checkThreshold() && checkPercentage();

            } else if (shiftingRaido.isSelected()) {
                if (!setBox.getChildren().contains(offsetPane)) {
                    setBox.getChildren().add(offsetPane);
                }
                return checkOffset();
            } else {
                return true;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean checkThreshold() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(thresholdInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            threshold = v;
            thresholdInput.setStyle(null);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Threshold"));
            thresholdInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected boolean checkPercentage() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(percentageInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0 && v < 50) {
            percentage = v;
            percentageInput.setStyle(null);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Percentage"));
            percentageInput.setStyle(UserConfig.badStyle());
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
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Offset"));
            offsetInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected ImageContrast pickValues() {
        if (!checkContrastAlgorithm()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            UserConfig.setLong(conn, baseName + "Threshold", threshold);
            UserConfig.setInt(conn, baseName + "Percentage", percentage);
            UserConfig.setInt(conn, baseName + "Offset", offset);
        } catch (Exception e) {
        }
        ImageContrast imageContrast = new ImageContrast()
                .setAlgorithm(contrastAlgorithm);
        if (stretchingRaido.isSelected()) {
            imageContrast.setThreshold(threshold).setPercentage(percentage);

        } else if (shiftingRaido.isSelected()) {
            imageContrast.setOffset(offset);
        }
        return imageContrast;
    }

}
