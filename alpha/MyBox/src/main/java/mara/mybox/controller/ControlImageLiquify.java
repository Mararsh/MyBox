package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.data.ImageLiquify;
import mara.mybox.image.data.ImageLiquify.FalloffAlgorithm;
import mara.mybox.image.data.ImageLiquify.LiquifyOperation;
import mara.mybox.image.data.PixelSampler.EdgeMode;
import mara.mybox.image.data.PixelSampler.SamplingAlgorithm;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2026-11-5
 * @License Apache License Version 2.0
 *
 * Assisted by Doubao and DeepSeek
 */
public class ControlImageLiquify extends BaseController {

    protected ImageLiquifyController imageController;
    protected LiquifyOperation liquifyOperation;
    protected int imageWidth, imageHeight, radius, step, turbulenceLayers;
    protected IntPoint startPoint, endPoint;
    protected ImageLiquify liquify;
    protected float strengthMax, strengthThreshold, hardness, expandShrinkPower, rotatePower,
            turbulencePower, turbulenceFreq;

    @FXML
    protected ComboBox<String> fallOffSelector, samplerSelector, edgeSelector,
            radiusSelector, stepSelector, strengthSelector, strengthThresholdSelector,
            hardnessSelector, expandShrinkPowerSelector, rotatePowerSelector,
            turbulencePowerSelector, noiseFrequencySelector, turbulenceLayersSelector;
    @FXML
    protected ToggleGroup operationGroup;
    @FXML
    protected RadioButton pushRadio, expandRadio, shrinkRadio, rotateRadio, rotateCounterRadio, turbulenceRadio;
    @FXML
    protected TextField x1Input, y1Input, x2Input, y2Input;
    @FXML
    protected VBox setBox, turbulenceBox;
    @FXML
    protected FlowPane hardnessPane, expandShrinkPane, rotatePane;

    public ControlImageLiquify() {
        TipsLabelKey = "ImageLiquifyComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tipsView.setUserData("mybox_about_image_liquify");

            operationGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle o, Toggle n) {
                    checkOperation();
                }
            });

            for (FalloffAlgorithm a : FalloffAlgorithm.values()) {
                fallOffSelector.getItems().add(a.name());
            }
            fallOffSelector.setValue(UserConfig.getString(baseName + "FalloffAlgorithm",
                    FalloffAlgorithm.SMOOTH_QUADRATIC.name()));
            fallOffSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    checkFallOff();
                }
            });

            checkOperation();

            for (SamplingAlgorithm a : SamplingAlgorithm.values()) {
                samplerSelector.getItems().add(a.toString());
            }
            samplerSelector.setValue(UserConfig.getString(baseName + "SamplingAlgorithm",
                    SamplingAlgorithm.BilinearInterpolation.name()));

            for (EdgeMode a : EdgeMode.values()) {
                edgeSelector.getItems().add(a.toString());
            }
            edgeSelector.setValue(UserConfig.getString(baseName + "EdgeMode",
                    EdgeMode.CLAMP.name()));

            radiusSelector.getItems().addAll(Arrays.asList(
                    "80", "100", "50", "200", "20", "400", "10", "5", "150", "30")
            );
            radiusSelector.setValue(UserConfig.getInt(baseName + "Radius", 80) + "");

            stepSelector.getItems().addAll(Arrays.asList(
                    "20", "30", "15", "60", "5", "150", "100", "80", "10", "2", "1")
            );
            stepSelector.setValue(UserConfig.getInt(baseName + "Step", 20) + "");

            strengthSelector.getItems().addAll(Arrays.asList(
                    "0.6", "1.0", "0.8", "0.5", "0.3", "0.1", "0.7", "0.9", "0.4", "0.2")
            );
            strengthSelector.setValue(UserConfig.getFloat(baseName + "Strength", 0.6f) + "");

            strengthThresholdSelector.getItems().addAll(Arrays.asList(
                    "0.05", "0.07", "0.06", "0.02", "0.08", "0.01", "0", "0.1", "0.2", "0.15")
            );
            strengthThresholdSelector.setValue(UserConfig.getFloat(baseName + "StrengthThreshold", 0.05f) + "");

            hardnessSelector.getItems().addAll(Arrays.asList(
                    "0.5", "0.2", "0.7", "1.0", "0", "0.3", "0.1", "0.9", "0.8", "0.6", "0.7")
            );
            hardnessSelector.setValue(UserConfig.getFloat(baseName + "Hardness", 0.5f) + "");

            expandShrinkPowerSelector.getItems().addAll(Arrays.asList(
                    "0.3", "0.2", "0.6", "0.5", "0.35", "0.1", "0.9", "0.8", "0.6", "0.7", "0.4", "1.0", "0")
            );
            expandShrinkPowerSelector.setValue(UserConfig.getFloat(baseName + "ExpandShrinkPower", 0.3f) + "");

            rotatePowerSelector.getItems().addAll(Arrays.asList(
                    "0.5", "0.3", "0.7", "0.4", "0.45", "0.6", "0.1", "0.2", "0.6", "0.8", "0.9", "1.0", "0")
            );
            rotatePowerSelector.setValue(UserConfig.getFloat(baseName + "RotatePower", 0.5f) + "");

            turbulencePowerSelector.getItems().addAll(Arrays.asList(
                    "0.2", "0.1", "0.3", "0.22", "0.35", "0.15", "0.25", "0.4", "0.6", "0.8", "0.9", "1.0", "0")
            );
            turbulencePowerSelector.setValue(UserConfig.getFloat(baseName + "TurbulencePower", 0.2f) + "");

            noiseFrequencySelector.getItems().addAll(Arrays.asList(
                    "0.05", "0.02", "0.1", "0.06", "0.03", "0.08", "0.09", "0.055", "0.07", "0.04")
            );
            noiseFrequencySelector.setValue(UserConfig.getFloat(baseName + "TurbulenceNoiseFrequency", 0.05f) + "");

            turbulenceLayersSelector.getItems().addAll(Arrays.asList(
                    "3", "1", "2", "4")
            );
            turbulenceLayersSelector.setValue(UserConfig.getInt(baseName + "TurbulenceLayers", 3) + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setImage(ImageLiquifyController c) {
        try {
            imageController = c;
            imageWidth = (int) imageController.srcImage().getWidth();
            imageHeight = (int) imageController.srcImage().getHeight();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkOperation() {
        try {
            setBox.getChildren().clear();
            liquifyOperation = LiquifyOperation.PUSH;

            if (expandRadio.isSelected()) {
                liquifyOperation = ImageLiquify.LiquifyOperation.EXPAND;
                setBox.getChildren().add(expandShrinkPane);

            } else if (shrinkRadio.isSelected()) {
                liquifyOperation = ImageLiquify.LiquifyOperation.SHRINK;
                setBox.getChildren().add(expandShrinkPane);

            } else if (rotateRadio.isSelected()) {
                liquifyOperation = ImageLiquify.LiquifyOperation.ROTATE_CLOCKWISE;
                setBox.getChildren().add(rotatePane);

            } else if (rotateCounterRadio.isSelected()) {
                liquifyOperation = ImageLiquify.LiquifyOperation.ROTATE_COUNTER_CLOCKWISE;
                setBox.getChildren().add(rotatePane);

            } else if (turbulenceRadio.isSelected()) {
                liquifyOperation = ImageLiquify.LiquifyOperation.TURBULENCE;
                setBox.getChildren().add(turbulenceBox);
            }

            checkFallOff();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkFallOff() {
        try {
            if (FalloffAlgorithm.valueOf(fallOffSelector.getValue()) == FalloffAlgorithm.PS_LIKE) {
                if (!setBox.getChildren().contains(hardnessPane)) {
                    setBox.getChildren().add(hardnessPane);
                }
            } else {
                if (setBox.getChildren().contains(hardnessPane)) {
                    setBox.getChildren().remove(hardnessPane);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setStart(DoublePoint p) {
        if (p == null) {
            return;
        }
        x1Input.setText((int) p.getX() + "");
        y1Input.setText((int) p.getY() + "");
    }

    public void setEnd(DoublePoint p) {
        if (p == null) {
            return;
        }
        x2Input.setText((int) p.getX() + "");
        y2Input.setText((int) p.getY() + "");
    }

    private boolean checkPoints() {
        int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
        try {
            x1 = (int) Double.parseDouble(x1Input.getText());
        } catch (Exception e) {
        }
        if (x1 < 0 || x1 > imageWidth - 1) {
            popError(message("InvalidParameter") + ": " + message("StartPoint"));
            return false;
        }
        try {
            y1 = (int) Double.parseDouble(y1Input.getText());
        } catch (Exception e) {
        }
        if (y1 < 0 || y1 > imageHeight - 1) {
            popError(message("InvalidParameter") + ": " + message("StartPoint"));
            return false;
        }
        try {
            x2 = (int) Double.parseDouble(x2Input.getText());
        } catch (Exception e) {
        }
        if (x2 < 0 || x2 > imageWidth - 1) {
            popError(message("InvalidParameter") + ": " + message("EndPoint"));
            return false;
        }
        try {
            y2 = (int) Double.parseDouble(y2Input.getText());
        } catch (Exception e) {
        }
        if (y2 < 0 || y2 > imageHeight - 1) {
            popError(message("InvalidParameter") + ": " + message("EndPoint"));
            return false;
        }
        startPoint = new IntPoint(x1, y1);
        endPoint = new IntPoint(x2, y2);
        return true;
    }

    private boolean checkRadius() {
        int v = -1;
        try {
            v = (int) Double.parseDouble(radiusSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0) {
            radius = v;
            UserConfig.setInt(baseName + "Radius", radius);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("BrushRadius"));
            return false;
        }
    }

    private boolean checkStep() {
        int v = -1;
        try {
            v = (int) Double.parseDouble(stepSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0) {
            step = v;
            UserConfig.setInt(baseName + "Step", step);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Interval"));
            return false;
        }
    }

    private boolean checkStrength() {
        float v = -1f;
        try {
            v = Float.parseFloat(strengthSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0f && v <= 1f) {
            strengthMax = v;
            UserConfig.setFloat(baseName + "Strength", strengthMax);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Strength"));
            return false;
        }
    }

    private boolean checkStrengthThreshold() {
        float v = -1f;
        try {
            v = Float.parseFloat(strengthThresholdSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0f && v <= 1f) {
            strengthThreshold = v;
            UserConfig.setFloat(baseName + "StrengthThreshold", strengthThreshold);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("StrengthThreshold"));
            return false;
        }
    }

    private boolean checkHardness() {
        float v = -1f;
        try {
            v = Float.parseFloat(hardnessSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0f && v <= 1f) {
            hardness = v;
            UserConfig.setFloat(baseName + "Hardness", hardness);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Hardness"));
            return false;
        }
    }

    private boolean checkExpandShrinkPower() {
        float v = -1f;
        try {
            v = Float.parseFloat(expandShrinkPowerSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0f && v <= 1f) {
            expandShrinkPower = v;
            UserConfig.setFloat(baseName + "ExpandShrinkPower", expandShrinkPower);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("DisplacementPower"));
            return false;
        }
    }

    private boolean checkRotatePower() {
        float v = -1f;
        try {
            v = Float.parseFloat(rotatePowerSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0f && v <= 1f) {
            rotatePower = v;
            UserConfig.setFloat(baseName + "RotatePower", rotatePower);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("RotatePower"));
            return false;
        }
    }

    private boolean checkTurbulencePower() {
        float v = -1f;
        try {
            v = Float.parseFloat(turbulencePowerSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0f && v <= 1f) {
            turbulencePower = v;
            UserConfig.setFloat(baseName + "TurbulencePower", turbulencePower);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("TurbulencePower"));
            return false;
        }
    }

    private boolean checkNoiseFrequency() {
        float v = -1f;
        try {
            v = Float.parseFloat(noiseFrequencySelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0f && v <= 1f) {
            turbulenceFreq = v;
            UserConfig.setFloat(baseName + "TurbulenceNoiseFrequency", turbulenceFreq);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("TurbulenceNoiseFrequency"));
            return false;
        }
    }

    private boolean checkTurbulenceLayers() {
        int v = -1;
        try {
            v = Integer.parseInt(turbulenceLayersSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 1) {
            turbulenceLayers = v;
            UserConfig.setInt(baseName + "TurbulenceLayers", turbulenceLayers);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("TurbulenceLayers"));
            return false;
        }
    }

    public boolean pickValues() {
        try {
            liquify = null;
            if (!checkRadius() || !checkStep() || !checkPoints()
                    || !checkStrength() || !checkStrengthThreshold()) {
                return false;
            }

            if (expandRadio.isSelected() || shrinkRadio.isSelected()) {
                if (!checkExpandShrinkPower()) {
                    return false;
                }
            }

            if (rotateRadio.isSelected() || rotateCounterRadio.isSelected()) {
                if (!checkRotatePower()) {
                    return false;
                }
            }

            if (turbulenceRadio.isSelected()) {
                if (!checkTurbulencePower() || !checkNoiseFrequency() || !checkTurbulenceLayers()) {
                    return false;
                }
            }

            if (FalloffAlgorithm.valueOf(fallOffSelector.getValue()) == FalloffAlgorithm.PS_LIKE) {
                if (!checkHardness()) {
                    return false;
                }
            }

            liquify = new ImageLiquify()
                    .setStartPoint(startPoint)
                    .setEndPoint(endPoint)
                    .setOperation(liquifyOperation)
                    .setFallOff(FalloffAlgorithm.valueOf(fallOffSelector.getValue()))
                    .setSampler(SamplingAlgorithm.valueOf(samplerSelector.getValue()),
                            EdgeMode.valueOf(edgeSelector.getValue()))
                    .setRadius(radius)
                    .setStep(step)
                    .setStrengthMax(strengthMax)
                    .setStrengthThreshold(strengthThreshold)
                    .setHardness(hardness)
                    .setExpandShrinkPower(expandShrinkPower)
                    .setRotatePower(rotatePower)
                    .setTurbulenceSeed(System.currentTimeMillis())
                    .setTurbulencePower(turbulencePower)
                    .setTurbulenceFreq(turbulenceFreq)
                    .setTurbulenceOctaves(turbulenceLayers);

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    public void goPointAction() {
        imageController.okAction();
    }

}
