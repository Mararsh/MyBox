package mara.mybox.controller;

import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlStroke extends BaseController {

    protected ShapeStyle style;

    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected ComboBox<String> widthSelector, limitSelector;
    @FXML
    protected ToggleGroup joinGroup, capGroup;
    @FXML
    protected RadioButton joinMiterRadio, joinBevelRadio, joinRoundRadio,
            capButtRadio, capSquareRadio, capRoundRadio;
    @FXML
    protected TextField arrayInput, offsetInput;
    @FXML
    protected CheckBox dashCheck;

    protected void setParameters(BaseController parent) {
        try {
            parentController = parent;
            baseName = parent.baseName;
            style = new ShapeStyle(baseName);

            if (colorController != null) {
                colorController.init(this, baseName + "Color", style.getStrokeColor());
            }

            if (widthSelector != null) {
                setWidthList(200, (int) style.getStrokeWidth());
            }

            if (joinGroup != null) {
                switch (style.getStrokeLineJoinAwt()) {
                    case BasicStroke.JOIN_ROUND:
                        joinRoundRadio.setSelected(true);
                        break;
                    case BasicStroke.JOIN_BEVEL:
                        joinBevelRadio.setSelected(true);
                        break;
                    default:
                        joinMiterRadio.setSelected(true);
                        break;
                }
            }

            if (limitSelector != null) {
                List<String> vl = new ArrayList<>();
                vl.addAll(Arrays.asList("10", "5", "2", "1", "8", "15", "20"));
                int iv = (int) style.getStrokeLineLimit();
                if (!vl.contains(iv + "")) {
                    vl.add(0, iv + "");
                }
                limitSelector.getItems().setAll(vl);
                limitSelector.setValue(iv + "");
            }

            if (capGroup != null) {
                switch (style.getStrokeLineCapAwt()) {
                    case BasicStroke.CAP_ROUND:
                        capRoundRadio.setSelected(true);
                        break;
                    case BasicStroke.CAP_SQUARE:
                        capSquareRadio.setSelected(true);
                        break;
                    default:
                        capButtRadio.setSelected(true);
                        break;
                }
            }

            if (dashCheck != null) {
                dashCheck.setSelected(style.isIsStrokeDash());
                if (arrayInput != null) {
                    arrayInput.setText(style.getStrokeDashText());
                }
                if (offsetInput != null) {
                    offsetInput.setText(style.getDashOffset() + "");
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected ShapeStyle pickValues() {
        if (widthSelector != null) {
            float v = -1;
            try {
                v = Float.parseFloat(widthSelector.getValue());
            } catch (Exception e) {
            }
            if (v <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return null;
            }
            style.setStrokeWidth(v);
        }
        if (colorController != null) {
            style.setStrokeColor(colorController.color());
        }
        if (joinGroup != null) {
            if (joinRoundRadio.isSelected()) {
                style.setStrokeLineJoin(StrokeLineJoin.ROUND);
            } else if (joinBevelRadio.isSelected()) {
                style.setStrokeLineJoin(StrokeLineJoin.BEVEL);
            } else {
                style.setStrokeLineJoin(StrokeLineJoin.MITER);
            }
        }
        if (limitSelector != null) {
            float v = -1;
            try {
                v = Float.parseFloat(limitSelector.getValue());
            } catch (Exception e) {
            }
            if (v < 1) {
                popError(message("InvalidParameter") + ": " + message("StrokeMiterLimit"));
                return null;
            }
            style.setStrokeLineLimit(v);
        }
        if (capGroup != null) {
            if (capRoundRadio.isSelected()) {
                style.setStrokeLineCap(StrokeLineCap.ROUND);
            } else if (capSquareRadio.isSelected()) {
                style.setStrokeLineCap(StrokeLineCap.SQUARE);
            } else {
                style.setStrokeLineCap(StrokeLineCap.BUTT);
            }
        }
        if (dashCheck != null) {
            style.setIsStrokeDash(dashCheck.isSelected());

            if (dashCheck.isSelected()) {
                List<Double> values = ShapeStyle.text2StrokeDash(arrayInput.getText());
                if (values == null || values.isEmpty()) {
                    popError(message("InvalidParameter") + ": " + message("StrokeDashArray"));
                    return null;
                }
                style.setStrokeDash(values);

                float v = -1;
                try {
                    v = Float.parseFloat(offsetInput.getText());
                } catch (Exception e) {
                }
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("StrokeDashOffset"));
                    return null;
                }
                style.setDashOffset(v);
            }
        }

        return style;
    }

    protected void setWidthList(int max, int initValue) {
        if (widthSelector == null || max <= 0 || initValue <= 0) {
            return;
        }
        List<String> ws = new ArrayList<>();
        ws.addAll(Arrays.asList("3", "1", "2", "5", "8", "10", "15", "25", "30",
                "50", "80", "100", "150", "200", "300", "500"));
        int step = max / 10;
        for (int w = 10; w < max; w += step) {
            if (!ws.contains(w + "")) {
                ws.add(0, w + "");
            }
        }
        if (!ws.contains(initValue + "")) {
            ws.add(0, initValue + "");
        }
        isSettingValues = true;
        widthSelector.getItems().setAll(ws);
        widthSelector.setValue(initValue + "");
        isSettingValues = false;
    }

}
