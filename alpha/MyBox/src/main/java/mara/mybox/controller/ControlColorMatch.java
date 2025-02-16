package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.color.ColorMatch;
import mara.mybox.color.ColorMatch.MatchAlgorithm;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.image.data.ImageScope;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public class ControlColorMatch extends BaseController {

    protected SimpleBooleanProperty changeNotify;
    protected ColorMatch colorMatch;

    @FXML
    protected ToggleGroup algorithmGroup;
    @FXML
    protected TextField thresholdInput, hueWeightInput,
            saturationWeightInput, brightnessWeightInput;
    @FXML
    protected VBox weightsBox;
    @FXML
    protected Label weightsLabel;
    @FXML
    protected FlowPane huePane;

    public ControlColorMatch() {
        TipsLabelKey = "ColorMatchComments";
        changeNotify = new SimpleBooleanProperty(false);
        colorMatch = new ColorMatch();
    }

    @Override
    public void initControls() {
        try (Connection conn = DerbyBase.getConnection()) {
            super.initControls();

            String a = UserConfig.getString(conn, baseName + "Algorithm",
                    message(ColorMatch.DefaultAlgorithm.name()));
            setAlgorithm(a);
            algorithmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    algorithmChanged();
                }
            });

            double threshold = UserConfig.getDouble(conn, baseName + "Threshold", 1.0d);
            if (threshold < 0) {
                threshold = 1.0d;
            }
            thresholdInput.setText(threshold + "");

            double hw = UserConfig.getDouble(conn, baseName + "HueWeight", 1.0d);
            if (hw < 0) {
                hw = 1.0d;
            }
            hueWeightInput.setText(hw + "");

            double sw = UserConfig.getDouble(conn, baseName + "SaturationWeight", 1.0d);
            if (sw < 0) {
                sw = 1.0d;
            }
            saturationWeightInput.setText(sw + "");

            double bw = UserConfig.getDouble(conn, baseName + "BrightnessWeight", 1.0d);
            if (bw < 0) {
                bw = 1.0d;
            }
            brightnessWeightInput.setText(bw + "");

            algorithmChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setAlgorithm(String a) {
        try {
            for (Toggle button : algorithmGroup.getToggles()) {
                RadioButton rbutton = (RadioButton) button;
                if (rbutton.getText().equals(a) || rbutton.getText().equals(message(a))) {
                    rbutton.setSelected(true);
                    break;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public MatchAlgorithm selectedAlgorithm() {
        try {
            return ColorMatch.algorithm(((RadioButton) algorithmGroup.getSelectedToggle()).getText());
        } catch (Exception e) {
            return ColorMatch.DefaultAlgorithm;
        }
    }

    protected void algorithmChanged() {
        try {
            if (isSettingValues) {
                return;
            }
            MatchAlgorithm a = selectedAlgorithm();
            if (a == null) {
                return;
            }
            if (ColorMatch.supportWeights(a)) {
                weightsBox.setVisible(true);
                if (!thisPane.getChildren().contains(weightsBox)) {
                    thisPane.getChildren().add(weightsBox);
                }
                huePane.setVisible(a != MatchAlgorithm.CMC);
                if (a == MatchAlgorithm.HSBEuclidean) {
                    weightsLabel.setText(message("LargerWeightMeansMoreContribution"));
                } else {
                    weightsLabel.setText(message("LargerWeightMeansLessContribution"));
                }
            } else {
                weightsBox.setVisible(false);
            }
            double s = ColorMatch.suggestedThreshold(a);
            NodeStyleTools.setTooltip(thresholdInput, message("SuggestedRange") + ": "
                    + "0~" + s);
            thresholdInput.setText(s + "");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try (Connection conn = DerbyBase.getConnection()) {
            double threshold;
            try {
                threshold = Double.parseDouble(thresholdInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Threshold"));
                return false;
            }

            MatchAlgorithm a = selectedAlgorithm();
            if (a == null) {
                popError(message("InvalidParameter") + ": " + message("Algorithm"));
                return false;
            }
            if (a == MatchAlgorithm.CIE94 || a == MatchAlgorithm.CIEDE2000) {
                double hw, sw, bw;
                try {
                    hw = Double.parseDouble(hueWeightInput.getText());
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("HueWeight"));
                    return false;
                }
                try {
                    sw = Double.parseDouble(saturationWeightInput.getText());
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("SaturationWeight"));
                    return false;
                }
                try {
                    bw = Double.parseDouble(brightnessWeightInput.getText());
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("BrightnessWeight"));
                    return false;
                }
                colorMatch.setHueWeight(hw);
                colorMatch.setSaturationWeight(sw);
                colorMatch.setBrightnessWeight(bw);

                UserConfig.setDouble(conn, baseName + "HueWeight", hw);
                UserConfig.setDouble(conn, baseName + "SaturationWeight", sw);
                UserConfig.setDouble(conn, baseName + "BrightnessWeight", bw);
            }

            colorMatch.setAlgorithm(a);
            colorMatch.setThreshold(threshold);

            UserConfig.setString(conn, baseName + "Algorithm", a.name());
            UserConfig.setDouble(conn, baseName + "Threshold", threshold);

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    public void defaultAction() {
        try {
            isSettingValues = true;
            thresholdInput.setText(ColorMatch.suggestedThreshold(selectedAlgorithm()) + "");
            hueWeightInput.setText("1.0");
            saturationWeightInput.setText("1.0");
            brightnessWeightInput.setText("1.0");
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.debug(e);
            isSettingValues = false;
        }
    }

    @FXML
    @Override
    public void goAction() {
        if (pickValues()) {
            changeNotify.set(!changeNotify.get());
        }
    }

    public boolean pickValuesTo(ImageScope scope) {
        if (scope == null || !pickValues()) {
            return false;
        }
        colorMatch.copyTo(scope.getColorMatch());
        return true;
    }

    public boolean loadValuesFrom(ImageScope scope) {
        try {
            scope.getColorMatch().copyTo(colorMatch);
            setAlgorithm(colorMatch.getAlgorithm().name());
            thresholdInput.setText(colorMatch.getThreshold() + "");
            hueWeightInput.setText(colorMatch.getHueWeight() + "");
            saturationWeightInput.setText(colorMatch.getSaturationWeight() + "");
            brightnessWeightInput.setText(colorMatch.getBrightnessWeight() + "");
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

}
