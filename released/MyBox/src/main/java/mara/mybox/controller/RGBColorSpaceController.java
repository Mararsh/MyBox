package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.color.CIEData;
import mara.mybox.color.CIEDataTools;
import mara.mybox.color.ChromaticAdaptation.ChromaticAdaptationAlgorithm;
import mara.mybox.color.Illuminant;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.UserConfig;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-6-3
 * @License Apache License Version 2.0
 */
public class RGBColorSpaceController extends BaseController {

    public ValueType valueType;
    public double[] red, green, blue, white;
    public String colorSpaceName, csWhiteName, currentWhiteName;
    public int scale = 8;
    public ChromaticAdaptationAlgorithm algorithm;

    public enum ValueType {
        Relative, Normalized, Tristimulus
    }

    @FXML
    protected ComboBox<String> csSelector, illumSelector;
    @FXML
    protected ToggleGroup valueGroup, pGroup, wGroup;
    @FXML
    protected RadioButton standardIllumRadio, inputWPRadio;
    @FXML
    protected TextField redXInput, redYInput, redZInput, greenXInput, greenYInput, greenZInput,
            blueXInput, blueYInput, blueZInput, whiteXInput, whiteYInput, whiteZInput;
    @FXML
    protected Label redXLabel, redYLabel, redZLabel, greenXLabel, greenYLabel, greenZLabel,
            blueXLabel, blueYLabel, blueZLabel, whiteXLabel, whiteYLabel, whiteZLabel,
            commentsLabel;
    @FXML
    protected VBox primariesBox, whiteVBox, whiteValuesBox;

    public RGBColorSpaceController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkValueType();
                }
            });

            pGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkPrimaries();
                }
            });

            List<String> names = RGBColorSpace.names();
            csSelector.getItems().addAll(names);
            csSelector.setVisibleRowCount(15);
            csSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkColorSpace();
                }
            });

            redXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            redYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            redZInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            greenXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            greenYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            greenZInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            blueXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            blueYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });
            blueZInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRGBInputs();
                }
            });

            wGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkWhite();
                }
            });

            List<String> illum = Illuminant.names();
            illumSelector.getItems().addAll(illum);
            illumSelector.setVisibleRowCount(15);
            illumSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkIlluminant();
                }
            });

            whiteXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkWhiteInputs();
                }
            });
            whiteYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkWhiteInputs();
                }
            });
            whiteZInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkWhiteInputs();
                }
            });

            checkValueType();

            csSelector.getSelectionModel().select(0);
            illumSelector.getSelectionModel().select(0);

        } catch (Exception e) {

        }
    }

    public void clear() {
        isSettingValues = true;

        redXInput.setStyle(null);
        redYInput.setStyle(null);
        redZInput.setStyle(null);
        greenXInput.setStyle(null);
        greenYInput.setStyle(null);
        greenZInput.setStyle(null);
        blueXInput.setStyle(null);
        blueYInput.setStyle(null);
        blueZInput.setStyle(null);
        whiteXInput.setStyle(null);
        whiteYInput.setStyle(null);
        whiteZInput.setStyle(null);

        commentsLabel.setText("");
        commentsLabel.setStyle(null);

        redYInput.setDisable(false);
        greenYInput.setDisable(false);
        blueYInput.setDisable(false);
        whiteYInput.setDisable(false);
        redZInput.setDisable(false);
        greenZInput.setDisable(false);
        blueZInput.setDisable(false);
        whiteZInput.setDisable(false);
        isSettingValues = false;
    }

    public void checkValueType() {
        if (isSettingValues) {
            return;
        }
        clear();
        isSettingValues = true;
        redXInput.setText("");
        redYInput.setText("");
        redZInput.setText("");
        greenXInput.setText("");
        greenYInput.setText("");
        greenZInput.setText("");
        blueXInput.setText("");
        blueYInput.setText("");
        blueZInput.setText("");
        whiteXInput.setText("");
        whiteYInput.setText("");
        whiteZInput.setText("");
        RadioButton selected = (RadioButton) valueGroup.getSelectedToggle();
        if (Languages.message("NormalizedValuesCC").equals(selected.getText())) {
            valueType = ValueType.Normalized;
            redXLabel.setText("x");
            redYLabel.setText("y");
            redZLabel.setText("z");
            greenXLabel.setText("x");
            greenYLabel.setText("y");
            greenZLabel.setText("z");
            blueXLabel.setText("x");
            blueYLabel.setText("y");
            blueZLabel.setText("z");
            whiteXLabel.setText("x");
            whiteYLabel.setText("y");
            whiteZLabel.setText("z");
            redZInput.setDisable(true);
            greenZInput.setDisable(true);
            blueZInput.setDisable(true);
            whiteZInput.setDisable(true);
        } else if (Languages.message("Tristimulus").equals(selected.getText())) {
            valueType = ValueType.Tristimulus;
            redXLabel.setText("X'");
            redYLabel.setText("Y'");
            redZLabel.setText("Z'");
            greenXLabel.setText("X'");
            greenYLabel.setText("Y'");
            greenZLabel.setText("Z'");
            blueXLabel.setText("X'");
            blueYLabel.setText("Y'");
            blueZLabel.setText("Z'");
            whiteXLabel.setText("X'");
            whiteYLabel.setText("Y'");
            whiteZLabel.setText("Z'");
        } else {
            valueType = ValueType.Relative;
            redXLabel.setText("X");
            redYLabel.setText("Y");
            redZLabel.setText("Z");
            greenXLabel.setText("X");
            greenYLabel.setText("Y");
            greenZLabel.setText("Z");
            blueXLabel.setText("X");
            blueYLabel.setText("Y");
            blueZLabel.setText("Z");
            whiteXLabel.setText("X");
            whiteYLabel.setText("Y");
            whiteZLabel.setText("Z");
            redYInput.setText("1.0");
            greenYInput.setText("1.0");
            blueYInput.setText("1.0");
            whiteYInput.setText("1.0");
            redYInput.setText("1.0");
            redYInput.setDisable(true);
            greenYInput.setDisable(true);
            blueYInput.setDisable(true);
            whiteYInput.setDisable(true);
        }
        isSettingValues = false;
        checkPrimaries();
    }

    public void checkPrimaries() {
        try {
            RadioButton selected = (RadioButton) pGroup.getSelectedToggle();
            if (Languages.message("ColorSpace").equals(selected.getText())) {
                primariesBox.setDisable(true);
                whiteVBox.setDisable(true);
                checkColorSpace();
            } else if (Languages.message("InputPrimaries").equals(selected.getText())) {
                colorSpaceName = null;
                primariesBox.setDisable(false);
                whiteVBox.setDisable(false);
                checkRGBInputs();
                checkWhite();
            }
        } catch (Exception e) {
            checkRGBInputs();
        }
    }

    private void checkColorSpace() {
        try {
            clear();
            colorSpaceName = csSelector.getSelectionModel().getSelectedItem();
            if (colorSpaceName == null) {
                return;
            }
            isSettingValues = true;
            if (null != valueType) {
                switch (valueType) {
                    case Normalized:
                        double[][] n = RGBColorSpace.primariesNormalized(colorSpaceName);
                        redXInput.setText(DoubleTools.scale(n[0][0], scale) + "");
                        redYInput.setText(DoubleTools.scale(n[0][1], scale) + "");
                        redZInput.setText(DoubleTools.scale(n[0][2], scale) + "");
                        greenXInput.setText(DoubleTools.scale(n[1][0], scale) + "");
                        greenYInput.setText(DoubleTools.scale(n[1][1], scale) + "");
                        greenZInput.setText(DoubleTools.scale(n[1][2], scale) + "");
                        blueXInput.setText(DoubleTools.scale(n[2][0], scale) + "");
                        blueYInput.setText(DoubleTools.scale(n[2][1], scale) + "");
                        blueZInput.setText(DoubleTools.scale(n[2][2], scale) + "");
                        break;
                    case Relative:
                        double[][] r = RGBColorSpace.primariesRelative(colorSpaceName);
                        redXInput.setText(DoubleTools.scale(r[0][0], scale) + "");
                        redYInput.setText(DoubleTools.scale(r[0][1], scale) + "");
                        redZInput.setText(DoubleTools.scale(r[0][2], scale) + "");
                        greenXInput.setText(DoubleTools.scale(r[1][0], scale) + "");
                        greenYInput.setText(DoubleTools.scale(r[1][1], scale) + "");
                        greenZInput.setText(DoubleTools.scale(r[1][2], scale) + "");
                        blueXInput.setText(DoubleTools.scale(r[2][0], scale) + "");
                        blueYInput.setText(DoubleTools.scale(r[2][1], scale) + "");
                        blueZInput.setText(DoubleTools.scale(r[2][2], scale) + "");
                        break;
                    case Tristimulus:
                        double[][] t = RGBColorSpace.primariesTristimulus(colorSpaceName);
                        redXInput.setText(DoubleTools.scale(t[0][0], scale) + "");
                        redYInput.setText(DoubleTools.scale(t[0][1], scale) + "");
                        redZInput.setText(DoubleTools.scale(t[0][2], scale) + "");
                        greenXInput.setText(DoubleTools.scale(t[1][0], scale) + "");
                        greenYInput.setText(DoubleTools.scale(t[1][1], scale) + "");
                        greenZInput.setText(DoubleTools.scale(t[1][2], scale) + "");
                        blueXInput.setText(DoubleTools.scale(t[2][0], scale) + "");
                        blueYInput.setText(DoubleTools.scale(t[2][1], scale) + "");
                        blueZInput.setText(DoubleTools.scale(t[2][2], scale) + "");
                        break;
                    default:
                        break;
                }
            }
            standardIllumRadio.setSelected(true);
            csWhiteName = RGBColorSpace.illuminantName(colorSpaceName);
            illumSelector.getSelectionModel().select(csWhiteName);
            isSettingValues = false;
            checkRGBInputs();
            checkWhite();
        } catch (Exception e) {
            checkRGBInputs();
        }
    }

    public void checkRGBInputs() {
        if (isSettingValues) {
            return;
        }
        red = checkXYZInputs(redXInput, redYInput, redZInput);
        if (red == null) {
            return;
        }
        green = checkXYZInputs(greenXInput, greenYInput, greenZInput);
        if (green == null) {
            return;
        }
        blue = checkXYZInputs(blueXInput, blueYInput, blueZInput);
    }

    public void checkWhite() {
        if (isSettingValues) {
            return;
        }
        try {
            RadioButton selected = (RadioButton) wGroup.getSelectedToggle();
            if (Languages.message("StandardIlluminant").equals(selected.getText())) {
                whiteValuesBox.setDisable(true);
                checkIlluminant();
            } else if (Languages.message("InputWhitePoint").equals(selected.getText())) {
                currentWhiteName = null;
                whiteValuesBox.setDisable(false);
                checkWhiteInputs();
            }
        } catch (Exception e) {
            checkWhiteInputs();
        }
    }

    private void checkIlluminant() {
        if (isSettingValues) {
            return;
        }
        currentWhiteName = illumSelector.getSelectionModel().getSelectedItem();
        if (currentWhiteName == null) {
            return;
        }
        Illuminant.IlluminantType fromType = Illuminant.type(currentWhiteName.substring(0, currentWhiteName.length() - 10));
        Illuminant.Observer fromObserver = currentWhiteName.endsWith("1931") ? Illuminant.Observer.CIE1931 : Illuminant.Observer.CIE1964;
        Illuminant illuminant = new Illuminant(fromType, fromObserver);
        isSettingValues = true;
        if (null != valueType) {
            switch (valueType) {
                case Normalized:
                    whiteXInput.setText(DoubleTools.scale(illuminant.normalizedX, scale) + "");
                    whiteYInput.setText(DoubleTools.scale(illuminant.normalizedY, scale) + "");
                    whiteZInput.setText(DoubleTools.scale(illuminant.normalizedZ, scale) + "");
                    break;
                case Relative:
                    whiteXInput.setText(DoubleTools.scale(illuminant.relativeX, scale) + "");
                    whiteYInput.setText(DoubleTools.scale(illuminant.relativeY, scale) + "");
                    whiteZInput.setText(DoubleTools.scale(illuminant.relativeZ, scale) + "");
                    break;
                case Tristimulus:
                    whiteXInput.setText(DoubleTools.scale(illuminant.X, scale) + "");
                    whiteYInput.setText(DoubleTools.scale(illuminant.Y, scale) + "");
                    whiteZInput.setText(DoubleTools.scale(illuminant.Z, scale) + "");
                    break;
                default:
                    break;
            }
        }
        isSettingValues = false;
        checkWhiteInputs();
    }

    public void checkWhiteInputs() {
        if (isSettingValues) {
            return;
        }
        white = checkXYZInputs(whiteXInput, whiteYInput, whiteZInput);
        if (white != null) {
            white = CIEDataTools.relative(white);
        }
    }

    public double[] checkXYZInputs(TextField xInput, TextField yInput, TextField zInput) {
        if (isSettingValues) {
            return null;
        }
        if (commentsLabel != null) {
            commentsLabel.setText("");
            commentsLabel.setStyle(null);
        }
        double x, y, z;
        try {
            double v = Double.parseDouble(xInput.getText());
            if (v < 0) {
                xInput.setStyle(UserConfig.badStyle());
                return null;
            } else {
                if (valueType == ValueType.Normalized) {
                    if (v > 1.0) {
                        xInput.setStyle(UserConfig.badStyle());
                        if (commentsLabel != null) {
                            commentsLabel.setText(Languages.message("NormalizeError"));
                            commentsLabel.setStyle(UserConfig.badStyle());
                        }
                        return null;
                    }
                }
                x = v;
                xInput.setStyle(null);
            }
        } catch (Exception e) {
            xInput.setStyle(UserConfig.badStyle());
            return null;
        }

        try {
            double v = Double.parseDouble(yInput.getText());
            if (v < 0) {
                yInput.setStyle(UserConfig.badStyle());
                return null;
            } else {
                if (valueType == ValueType.Normalized) {
                    if (v > 1.0) {
                        yInput.setStyle(UserConfig.badStyle());
                        if (commentsLabel != null) {
                            commentsLabel.setText(Languages.message("NormalizeError"));
                            commentsLabel.setStyle(UserConfig.badStyle());
                        }
                        return null;
                    }
                }
                y = v;
                yInput.setStyle(null);
            }
        } catch (Exception e) {
            yInput.setStyle(UserConfig.badStyle());
            return null;
        }

        if (valueType == ValueType.Normalized) {
            isSettingValues = true;
            z = DoubleTools.scale(1 - x - y, scale);
            zInput.setText(z + "");
            isSettingValues = false;
        } else {
            try {
                double v = Double.parseDouble(zInput.getText());
                z = v;
                zInput.setStyle(null);
            } catch (Exception e) {
                zInput.setStyle(UserConfig.badStyle());
                return null;
            }
        }
        return DoubleTools.array(x, y, z);
    }

}
