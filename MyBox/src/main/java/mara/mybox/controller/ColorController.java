package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import mara.mybox.color.CIEColorSpace;
import mara.mybox.color.CMYKColorSpace;
import mara.mybox.color.ColorConversion.RangeType;
import mara.mybox.color.ColorConversion.SpaceType;
import mara.mybox.color.Illuminant;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-6-7
 * @License Apache License Version 2.0
 */
public class ColorController extends BaseController {

    protected double d1, d2, d3, d4;
    protected SpaceType spaceType;
    protected RangeType rangeType;
    protected String spaceName, white, gamma, adaptation;
    protected int scale = 8;

    @FXML
    protected ToggleGroup rangeGroup, gammaGroup;
    @FXML
    protected RadioButton rangeRatio1, rangeRatio2, gammaRadio1, gammaRadio2;
    @FXML
    protected ComboBox<String> rgbSelector, cieSelector, cmykSelector, whiteSelector, othersSelector;
    @FXML
    protected Label vLabel1, vLabel2, vLabel3, vLabel4;
    @FXML
    protected TextField vInput1, vInput2, vInput3, vInput4;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected HBox paraBox1, paraBox2, paraBox3, gammaBox, rangeBox;

    public ColorController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            List<String> rgbNames = RGBColorSpace.names();
            rgbSelector.getItems().addAll(rgbNames);
            rgbSelector.setVisibleRowCount(15);
            rgbSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    rgbType();
                }
            });

            List<String> cieNames = CIEColorSpace.names();
            cieSelector.getItems().addAll(cieNames);
            cieSelector.setVisibleRowCount(15);
            cieSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    cieType();
                }
            });

            List<String> cmykNames = CMYKColorSpace.names();
            cmykSelector.getItems().addAll(cmykNames);
            cmykSelector.setVisibleRowCount(15);
            cmykSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    cmykType();
                }
            });

            othersSelector.getItems().addAll(Arrays.asList(
                    "HLS", "HSV", "YCbCr", "GREY"
            ));
            othersSelector.setVisibleRowCount(15);
            othersSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    othersType();
                }
            });

            List<String> whiteNames = Illuminant.names();
            whiteSelector.getItems().addAll(whiteNames);
            whiteSelector.setVisibleRowCount(15);
            whiteSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    white = newv;
                }
            });

            gammaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) {
                    gamma = ((RadioButton) gammaGroup.getSelectedToggle()).getText();
                }
            });

            rangeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) {
                    checkRange();
                }
            });

            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable, Color oldv, Color newv) {
                    checkColor();
                }
            });

            vInput1.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldv, String newv) {
                    checkInputs();
                }
            });

            vInput2.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldv, String newv) {
                    checkInputs();
                }
            });

            vInput3.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldv, String newv) {
                    checkInputs();
                }
            });

            vInput4.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldv, String newv) {
                    checkInputs();
                }
            });

            rgbSelector.getSelectionModel().select("sRGB");
            colorPicker.setValue(Color.RED);

        } catch (Exception e) {
            logger.debug(e.toString());

        }
    }

    public void rgbType() {
        spaceName = rgbSelector.getSelectionModel().getSelectedItem();
        if (spaceName == null) {
            return;
        }
        isSettingValues = true;

        white = RGBColorSpace.illuminantName(spaceName);
        whiteSelector.getSelectionModel().select(white);

        gamma = RGBColorSpace.gammaName(spaceName);
        if (gamma == null) {
            return;
        }
        if (!paraBox2.getChildren().contains(gammaBox)) {
            paraBox2.getChildren().add(gammaBox);
            FxmlControl.refreshStyle(paraBox2);
        }
        gammaRadio1.setText(gamma);
        gammaRadio1.setSelected(true);

        if (!paraBox3.getChildren().contains(rangeBox)) {
            paraBox3.getChildren().add(rangeBox);
            FxmlControl.refreshStyle(paraBox3);
        }
        rangeRatio2.setText("0~255");
        rangeRatio2.setSelected(true);

        vLabel1.setText(message("Red"));
        vLabel2.setText(message("Green"));
        vLabel3.setText(message("Blue"));
        if (paraBox3.getChildren().contains(vLabel4)) {
            vInput4.setText("0");
            vInput4.setStyle(null);
            paraBox3.getChildren().removeAll(vLabel4, vInput4);
        }

        spaceType = SpaceType.RGB;
        cieSelector.getSelectionModel().select(null);
        cmykSelector.getSelectionModel().select(null);
        othersSelector.getSelectionModel().select(null);

        isSettingValues = false;

        checkRange();

    }

    public void cieType() {
        spaceName = cieSelector.getSelectionModel().getSelectedItem();
        if (spaceName == null) {
            return;
        }
        if (paraBox2.getChildren().contains(gammaBox)) {
            paraBox2.getChildren().remove(gammaBox);
        }

        if ("CIEXYZ".equals(spaceName) || "CIExyY".equals(spaceName)) {
            if (!paraBox3.getChildren().contains(rangeBox)) {
                paraBox3.getChildren().add(rangeBox);
                FxmlControl.refreshStyle(paraBox3);
            }
            rangeRatio2.setText("0~100");
            checkRange();

            if ("CIEXYZ".equals(spaceName)) {
                vLabel1.setText("X");
                vLabel2.setText("Y");
                vLabel3.setText("Z");
            } else {
                vLabel1.setText("x");
                vLabel2.setText("y");
                vLabel3.setText("Y");
            }

        } else {
            if (paraBox3.getChildren().contains(rangeBox)) {
                paraBox3.getChildren().remove(rangeBox);
            }
            rangeType = null;

            switch (spaceName) {
                case "CIELuv":
                    vLabel1.setText("L");
                    vLabel2.setText("u");
                    vLabel3.setText("v");
                    break;
                case "CIELab":
                    vLabel1.setText("L");
                    vLabel2.setText("a");
                    vLabel3.setText("b");
                    break;
                case "LCHab":
                    vLabel1.setText("L");
                    vLabel2.setText("C");
                    vLabel3.setText("H");
                    break;
                case "LCHuv":
                    vLabel1.setText("L");
                    vLabel2.setText("C");
                    vLabel3.setText("H");
                    break;
                default:
                    return;
            }

        }
        if (paraBox3.getChildren().contains(vLabel4)) {
            paraBox3.getChildren().removeAll(vLabel4, vInput4);
        }

        spaceType = SpaceType.CIE;
        rgbSelector.getSelectionModel().select(null);
        cmykSelector.getSelectionModel().select(null);
        othersSelector.getSelectionModel().select(null);

    }

    public void cmykType() {
        spaceName = cmykSelector.getSelectionModel().getSelectedItem();
        if (spaceName == null) {
            return;
        }
        if (paraBox2.getChildren().contains(gammaBox)) {
            paraBox2.getChildren().remove(gammaBox);
        }

        if (!paraBox3.getChildren().contains(rangeBox)) {
            paraBox3.getChildren().add(rangeBox);
            FxmlControl.refreshStyle(paraBox3);
        }
        rangeRatio2.setText("0~100");
        checkRange();

        spaceType = SpaceType.CMYK;
        rgbSelector.getSelectionModel().select(null);
        cieSelector.getSelectionModel().select(null);
        othersSelector.getSelectionModel().select(null);

    }

    public void othersType() {
        spaceName = othersSelector.getSelectionModel().getSelectedItem();
        if (spaceName == null) {
            return;
        }

        spaceType = SpaceType.Others;
        rgbSelector.getSelectionModel().select(null);
        cieSelector.getSelectionModel().select(null);
        cmykSelector.getSelectionModel().select(null);

    }

    public void checkRange() {
        if (isSettingValues || rangeGroup.getSelectedToggle() == null) {
            return;
        }
        String selected = ((RadioButton) rangeGroup.getSelectedToggle()).getText();
        switch (selected) {
            case "0.0~1.0":
                rangeType = RangeType.Normalized;
                break;
            case "0~255":
                rangeType = RangeType.RGB;
                break;
            case "0~100":
                rangeType = RangeType.Hundred;
                break;
        }
        checkColor();
    }

    private void checkColor() {
        if (isSettingValues) {
            return;
        }
        Color color = colorPicker.getValue();
        double red = DoubleTools.scale(color.getRed(), scale);
        double green = DoubleTools.scale(color.getGreen(), scale);
        double blue = DoubleTools.scale(color.getBlue(), scale);
        isSettingValues = true;
        if (spaceType == SpaceType.RGB) {
            if (rangeType == RangeType.Normalized) {
                vInput1.setText(red + "");
                vInput2.setText(green + "");
                vInput3.setText(blue + "");
            } else {
                vInput1.setText(Math.round(red * 255) + "");
                vInput2.setText(Math.round(green * 255) + "");
                vInput3.setText(Math.round(blue * 255) + "");
            }
        }
        isSettingValues = false;
        checkInputs();
    }

    private void checkInputs() {
        if (isSettingValues) {
            return;
        }
        if (spaceType == SpaceType.RGB) {
            checkRGB();
        }

    }

    private void checkRGB() {
        if (rangeType == RangeType.Normalized) {
            try {
                double d = Double.parseDouble(vInput1.getText());
                if (d >= 0.0 && d <= 1.0) {
                    d1 = DoubleTools.scale(d, scale);
                    vInput1.setStyle(null);
                } else {
                    vInput1.setStyle(badStyle);
                    return;
                }
            } catch (Exception e) {
                vInput1.setStyle(badStyle);
                return;
            }
            try {
                double d = Double.parseDouble(vInput2.getText());
                if (d >= 0.0 && d <= 1.0) {
                    d2 = DoubleTools.scale(d, scale);
                    vInput2.setStyle(null);
                } else {
                    vInput2.setStyle(badStyle);
                    return;
                }
            } catch (Exception e) {
                vInput2.setStyle(badStyle);
                return;
            }
            try {
                double d = Double.parseDouble(vInput3.getText());
                if (d >= 0.0 && d <= 1.0) {
                    d3 = DoubleTools.scale(d, scale);
                    vInput3.setStyle(null);
                } else {
                    vInput3.setStyle(badStyle);
                    return;
                }
            } catch (Exception e) {
                vInput3.setStyle(badStyle);
                return;
            }
        } else {
            try {
                int v = Integer.parseInt(vInput1.getText());
                if (v >= 0 && v <= 255) {
                    d1 = DoubleTools.scale(v / 255.0d, scale);
                    vInput1.setStyle(null);
                } else {
                    vInput1.setStyle(badStyle);
                }
            } catch (Exception e) {
                vInput1.setStyle(badStyle);
            }
            try {
                int v = Integer.parseInt(vInput2.getText());
                if (v >= 0 && v <= 255) {
                    d2 = DoubleTools.scale(v / 255.0d, scale);
                    vInput2.setStyle(null);
                } else {
                    vInput2.setStyle(badStyle);
                }
            } catch (Exception e) {
                vInput2.setStyle(badStyle);
            }
            try {
                int v = Integer.parseInt(vInput3.getText());
                if (v >= 0 && v <= 255) {
                    d3 = DoubleTools.scale(v / 255.0d, scale);
                    vInput3.setStyle(null);
                } else {
                    vInput3.setStyle(badStyle);
                }
            } catch (Exception e) {
                vInput3.setStyle(badStyle);
            }
        }

        Color color = new Color(d1, d2, d3, 1.0);
        isSettingValues = true;
        colorPicker.setValue(color);
        isSettingValues = false;

    }

}
