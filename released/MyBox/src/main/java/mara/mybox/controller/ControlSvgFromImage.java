package mara.mybox.controller;

import java.sql.Connection;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-26
 * @License Apache License Version 2.0
 */
public class ControlSvgFromImage extends BaseController {

    protected HashMap<String, Float> options;
    protected Algorithm quantization, layer;

    @FXML
    protected TextField quanRatioInput, colorsNumberInput, quanCycleInput, blurDeltaInput,
            linesErrorInput, lineRadiusInput, quadraticErrorInput, quadraticRadiusInput,
            pathThresholdInput, decimalInput, scaleInput;
    @FXML
    protected CheckBox colorSamplingCheck, viewboxCheck, descCheck;
    @FXML
    protected RadioButton myboxRadio, miguelemosreverteRadio, jankovicsandrasRadio,
            blur0Radio, blur1Radio, blur2Radio, blur3Radio, blur4Radio, blur5Radio;
    @FXML
    protected ToggleGroup algorithmGroup;
    @FXML
    protected ControlImageQuantization quantizationController;
    @FXML
    protected VBox quantizationBox, jankovicsandrasBox, myboxBox;

    public enum Algorithm {
        miguelemosreverte, jankovicsandras, mybox
    }

    @Override
    public void initControls() {
        super.initControls();

        try (Connection conn = DerbyBase.getConnection()) {
            colorSamplingCheck.setSelected(UserConfig.getFloat(conn, "SvgToImage_colorsampling", 1f) == 1f);
            colorsNumberInput.setText(UserConfig.getFloat(conn, "SvgToImage_numberofcolors", 16f) + "");
            quanRatioInput.setText(UserConfig.getFloat(conn, "SvgToImage_mincolorratio", 0.02f) + "");
            quanCycleInput.setText(UserConfig.getFloat(conn, "SvgToImage_colorquantcycles", 3f) + "");

            blur0Radio.setSelected(true);
            blurDeltaInput.setText(UserConfig.getFloat(conn, "SvgToImage_blurdelta", 20f) + "");

            linesErrorInput.setText(UserConfig.getFloat(conn, "SvgToImage_ltres", 1f) + "");
            lineRadiusInput.setText(UserConfig.getFloat(conn, "SvgToImage_lcpr", 0f) + "");

            quadraticErrorInput.setText(UserConfig.getFloat(conn, "SvgToImage_qtres", 1f) + "");
            quadraticRadiusInput.setText(UserConfig.getFloat(conn, "SvgToImage_qcpr", 0f) + "");

            pathThresholdInput.setText(UserConfig.getFloat(conn, "SvgToImage_pathomit", 8f) + "");
            decimalInput.setText(UserConfig.getFloat(conn, "SvgToImage_roundcoords", 1f) + "");
            scaleInput.setText(UserConfig.getFloat(conn, "SvgToImage_scale", 1f) + "");

            viewboxCheck.setSelected(UserConfig.getFloat(conn, "SvgToImage_viewbox", 0f) == 1f);
            descCheck.setSelected(UserConfig.getFloat(conn, "SvgToImage_desc", 1f) == 1f);

            float v = UserConfig.getFloat(conn, "SvgToImage_blurradius", 0f);
            if (v == 1) {
                blur1Radio.setSelected(true);
            } else if (v == 2) {
                blur2Radio.setSelected(true);
            } else if (v == 3) {
                blur3Radio.setSelected(true);
            } else if (v == 4) {
                blur4Radio.setSelected(true);
            } else if (v == 5) {
                blur5Radio.setSelected(true);
            } else {
                blur0Radio.setSelected(true);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

        quantizationController.defaultForSvg();

        algorithmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) {
                checkAlgorithm();
            }
        });

        checkAlgorithm();

    }

    protected void checkAlgorithm() {
        if (myboxRadio.isSelected()) {
            if (quantizationBox.getChildren().contains(jankovicsandrasBox)) {
                quantizationBox.getChildren().remove(jankovicsandrasBox);
            }
            if (!quantizationBox.getChildren().contains(myboxBox)) {
                quantizationBox.getChildren().add(myboxBox);
            }

        } else {
            if (quantizationBox.getChildren().contains(myboxBox)) {
                quantizationBox.getChildren().remove(myboxBox);
            }
            if (!quantizationBox.getChildren().contains(jankovicsandrasBox)) {
                quantizationBox.getChildren().add(jankovicsandrasBox);
            }

        }

        refreshStyle(quantizationBox);
    }

    @FXML
    protected void defaultAction() {
        try {
            colorSamplingCheck.setSelected(true);
            colorsNumberInput.setText("16");
            quanRatioInput.setText("0.02");
            quanCycleInput.setText("3");

            blur0Radio.setSelected(true);
            blurDeltaInput.setText("20");

            linesErrorInput.setText("1");
            lineRadiusInput.setText("0");

            quadraticErrorInput.setText("1");
            quadraticRadiusInput.setText("0");

            pathThresholdInput.setText("8");
            decimalInput.setText("1");
            scaleInput.setText("1");

            viewboxCheck.setSelected(false);
            descCheck.setSelected(true);

            miguelemosreverteRadio.setSelected(true);
            quantizationController.defaultForSvg();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean pickValues() {
        try {
            if (!quantizationController.pickValues()) {
                return false;
            }
            options = new HashMap<>();

            if (colorSamplingCheck.isSelected()) {
                options.put("colorsampling", 1f);
            } else {
                options.put("colorsampling", 0f);
            }

            try {
                float v = Float.parseFloat(colorsNumberInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("ColorsNumber"));
                    return false;
                }
                options.put("numberofcolors", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ColorsNumber"));
                return false;
            }

            try {
                float v = Float.parseFloat(quanRatioInput.getText());
                if (v < 0 || v > 1) {
                    popError(message("InvalidParameter") + ": " + message("RatioThresholdForColorQuantization"));
                    return false;
                }
                options.put("mincolorratio", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("RatioThresholdForColorQuantization"));
                return false;
            }

            try {
                float v = Float.parseFloat(quanCycleInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("CyclesOfColorQuantization"));
                    return false;
                }
                options.put("colorquantcycles", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("CyclesOfColorQuantization"));
                return false;
            }

            if (blur0Radio.isSelected()) {
                options.put("blurradius", 0f);
            } else if (blur1Radio.isSelected()) {
                options.put("blurradius", 1f);
            } else if (blur2Radio.isSelected()) {
                options.put("blurradius", 2f);
            } else if (blur3Radio.isSelected()) {
                options.put("blurradius", 3f);
            } else if (blur4Radio.isSelected()) {
                options.put("blurradius", 4f);
            } else if (blur5Radio.isSelected()) {
                options.put("blurradius", 5f);
            }

            try {
                float v = Float.parseFloat(blurDeltaInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("DeltaThresholdForBlur"));
                    return false;
                }
                options.put("blurdelta", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("DeltaThresholdForBlur"));
                return false;
            }

            try {
                float v = Float.parseFloat(linesErrorInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("ErrorThresholdForStraightLines"));
                    return false;
                }
                options.put("ltres", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ErrorThresholdForStraightLines"));
                return false;
            }

            try {
                float v = Float.parseFloat(lineRadiusInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("RadiusOfStraightLineControlPoint"));
                    return false;
                }
                options.put("lcpr", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("RadiusOfStraightLineControlPoint"));
                return false;
            }

            try {
                float v = Float.parseFloat(quadraticErrorInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("ErrorThresholdForQuadratic"));
                    return false;
                }
                options.put("qtres", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("ErrorThresholdForQuadratic"));
                return false;
            }

            try {
                float v = Float.parseFloat(quadraticRadiusInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("RadiusOfQuadraticControlPoint"));
                    return false;
                }
                options.put("qcpr", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("RadiusOfQuadraticControlPoint"));
                return false;
            }

            try {
                float v = Float.parseFloat(pathThresholdInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("OmitThreaholdForEdgePath"));
                    return false;
                }
                options.put("pathomit", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("OmitThreaholdForEdgePath"));
                return false;
            }

            try {
                float v = Float.parseFloat(decimalInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("DecimalScale"));
                    return false;
                }
                options.put("roundcoords", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("DecimalScale"));
                return false;
            }

            try {
                float v = Float.parseFloat(scaleInput.getText());
                if (v < 0) {
                    popError(message("InvalidParameter") + ": " + message("Scale"));
                    return false;
                }
                options.put("scale", v);
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("Scale"));
                return false;
            }

            if (viewboxCheck.isSelected()) {
                options.put("viewbox", 1f);
            } else {
                options.put("viewbox", 0f);
            }

            if (descCheck.isSelected()) {
                options.put("desc", 1f);
            } else {
                options.put("desc", 0f);
            }

            try (Connection conn = DerbyBase.getConnection()) {
                for (String k : options.keySet()) {
                    UserConfig.setFloat(conn, "SvgToImage_" + k, options.get(k));
                }
            } catch (Exception e) {
//            MyBoxLog.debug(e);
            }

            if (jankovicsandrasRadio.isSelected()) {
                quantization = Algorithm.jankovicsandras;
            } else if (myboxRadio.isSelected()) {
                quantization = Algorithm.mybox;
            } else {
                quantization = Algorithm.miguelemosreverte;
            }

            layer = Algorithm.miguelemosreverte;

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        get
     */
    public HashMap<String, Float> getOptions() {
        return options;
    }

    public Algorithm getQuantization() {
        return quantization;
    }

    public Algorithm getLayer() {
        return layer;
    }

    public ControlImageQuantization getQuantizationController() {
        return quantizationController;
    }

}
