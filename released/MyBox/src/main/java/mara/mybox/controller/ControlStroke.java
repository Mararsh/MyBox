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
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ControlStroke extends BaseController {

    protected BaseShapeController shapeController;
    protected ShapeStyle style;

    @FXML
    protected ControlColorSet colorController, fillController;
    @FXML
    protected ComboBox<String> widthSelector, limitSelector, fillOpacitySelector;
    @FXML
    protected ToggleGroup joinGroup, capGroup;
    @FXML
    protected RadioButton joinMiterRadio, joinBevelRadio, joinRoundRadio,
            capButtRadio, capSquareRadio, capRoundRadio;
    @FXML
    protected TextField arrayInput, offsetInput;
    @FXML
    protected CheckBox dashCheck, fillCheck;
    @FXML
    protected VBox fillBox;
    @FXML
    protected FlowPane fillOpacityPane;

    protected void setParameters(BaseShapeController parent) {
        try {
            if (parent == null) {
                return;
            }
            shapeController = parent;
            baseName = parent.baseName;
            style = new ShapeStyle(baseName);

            colorController.init(this, baseName + "Color", style.getStrokeColor());

            widthSelector.setValue((int) style.getStrokeWidth() + "");

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

            List<String> vl = new ArrayList<>();
            vl.addAll(Arrays.asList("10", "5", "2", "1", "8", "15", "20"));
            int iv = (int) style.getStrokeLineLimit();
            if (!vl.contains(iv + "")) {
                vl.add(0, iv + "");
            }
            limitSelector.getItems().setAll(vl);
            limitSelector.setValue(iv + "");

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

            dashCheck.setSelected(style.isIsStrokeDash());
            if (arrayInput != null) {
                arrayInput.setText(style.getStrokeDashText());
            }
            if (offsetInput != null) {
                offsetInput.setText(style.getDashOffset() + "");
            }

            fillCheck.setSelected(style.isIsFillColor());
            fillController.init(this, baseName + "Fill", style.getFillColor());

            vl = new ArrayList<>();
            vl.addAll(Arrays.asList("0.5", "0.3", "0", "1.0", "0.05", "0.02", "0.1",
                    "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"));
            float fv = style.getFillOpacity();
            if (!vl.contains(fv + "")) {
                vl.add(0, fv + "");
            }
            fillOpacitySelector.getItems().setAll(vl);
            fillOpacitySelector.setValue(fv + "");

            if (shapeController instanceof BaseImageEditController) {
                fillBox.getChildren().remove(fillOpacityPane);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setWidthList() {
        isSettingValues = true;
        setWidthList(widthSelector, shapeController.imageView, (int) style.getStrokeWidth());
        isSettingValues = false;
    }

    protected static void setWidthList(ComboBox<String> selector, ImageView view, int initValue) {
        if (selector == null || view == null) {
            return;
        }
        List<String> ws = new ArrayList<>();
        ws.addAll(Arrays.asList("2", "3", "1", "5", "8", "10", "15", "25", "30",
                "50", "80", "100", "150", "200", "300", "500"));
        int max = (int) view.getImage().getWidth();
        int step = max / 10;
        for (int w = 10; w < max; w += step) {
            if (!ws.contains(w + "")) {
                ws.add(0, w + "");
            }
        }
        if (initValue >= 0) {
            if (!ws.contains(initValue + "")) {
                ws.add(0, initValue + "");
            }
        } else {
            initValue = 2;
        }
        selector.getItems().setAll(ws);
        selector.setValue(initValue + "");
    }

    protected ShapeStyle pickValues() {
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

        style.setStrokeColor(colorController.color());

        if (joinRoundRadio.isSelected()) {
            style.setStrokeLineJoin(StrokeLineJoin.ROUND);
        } else if (joinBevelRadio.isSelected()) {
            style.setStrokeLineJoin(StrokeLineJoin.BEVEL);
        } else {
            style.setStrokeLineJoin(StrokeLineJoin.MITER);
        }

        v = -1;
        try {
            v = Float.parseFloat(limitSelector.getValue());
        } catch (Exception e) {
        }
        if (v < 1) {
            popError(message("InvalidParameter") + ": " + message("StrokeMiterLimit"));
            return null;
        }
        style.setStrokeLineLimit(v);

        if (capRoundRadio.isSelected()) {
            style.setStrokeLineCap(StrokeLineCap.ROUND);
        } else if (capSquareRadio.isSelected()) {
            style.setStrokeLineCap(StrokeLineCap.SQUARE);
        } else {
            style.setStrokeLineCap(StrokeLineCap.BUTT);
        }
        style.setIsStrokeDash(dashCheck.isSelected());

        if (dashCheck.isSelected()) {
            List<Double> values = ShapeStyle.text2StrokeDash(arrayInput.getText());
            if (values == null || values.isEmpty()) {
                popError(message("InvalidParameter") + ": " + message("StrokeDashArray"));
                return null;
            }
            style.setStrokeDash(values);

            v = -1;
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

        style.setIsFillColor(fillCheck.isSelected());
        style.setFillColor(fillController.color());

        if (fillCheck.isSelected()) {
            v = -1;
            try {
                v = Float.parseFloat(fillOpacitySelector.getValue());
            } catch (Exception e) {
            }
            if (v < 0 || v > 1) {
                popError(message("InvalidParameter") + ": " + message("FillOpacity"));
                return null;
            }
            style.setFillOpacity(v);
        }

        style.save();
        return style;
    }

    @FXML
    public void aboutStroke() {
        openLink(HelpTools.strokeLink());
    }

}
