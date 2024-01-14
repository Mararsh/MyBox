package mara.mybox.controller;

import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.color.RGB2XYZConversionMatrix;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.color.RGBColorSpace.ColorSpaceType;
import static mara.mybox.color.RGBColorSpace.primariesTristimulus;
import static mara.mybox.color.RGBColorSpace.whitePointMatrix;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.DoubleMatrixTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-5-20
 * @License Apache License Version 2.0
 */
public class RGB2XYZConversionMatrixController extends ChromaticityBaseController {

    @FXML
    public RGBColorSpaceController rgbController;
    @FXML
    public WhitePointController xyzController;
    @FXML
    protected TextArea textsArea;
    @FXML
    protected TextField scaleMatricesInput;
    @FXML
    protected Button calculateButton, calculateAllButton, exportButton;
    @FXML
    protected HtmlTableController matrixController;

    public RGB2XYZConversionMatrixController() {
        baseTitle = Languages.message("LinearRGB2XYZMatrix");
        exportName = "LinearRGB2XYZMatrix";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initAdaptation();
            initMatrices();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    private void initAdaptation() {

        initOptions();

        calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                .or(scaleInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.redXInput.textProperty()))
                .or(rgbController.redXInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.redYInput.textProperty()))
                .or(rgbController.redYInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.redZInput.textProperty()))
                .or(rgbController.redZInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.greenXInput.textProperty()))
                .or(rgbController.greenXInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.greenYInput.textProperty()))
                .or(rgbController.greenYInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.greenZInput.textProperty()))
                .or(rgbController.greenZInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.blueXInput.textProperty()))
                .or(rgbController.blueXInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.blueYInput.textProperty()))
                .or(rgbController.blueYInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.blueZInput.textProperty()))
                .or(rgbController.blueZInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.whiteXInput.textProperty()))
                .or(rgbController.whiteXInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.whiteYInput.textProperty()))
                .or(rgbController.whiteYInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(rgbController.whiteZInput.textProperty()))
                .or(rgbController.whiteZInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(xyzController.xInput.textProperty()))
                .or(xyzController.xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(xyzController.yInput.textProperty()))
                .or(xyzController.yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(xyzController.zInput.textProperty()))
                .or(xyzController.zInput.styleProperty().isEqualTo(UserConfig.badStyle()))
        );

    }

    @Override
    public void checkAlgorithm() {
        super.checkAlgorithm();
        rgbController.algorithm = algorithm;
    }

    @Override
    public void checkScale() {
        super.checkScale();
        rgbController.scale = scale;
    }

    private void initMatrices() {
        scaleMatricesInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    int p = Integer.parseInt(scaleMatricesInput.getText());
                    if (p <= 0) {
                        scaleMatricesInput.setStyle(UserConfig.badStyle());
                    } else {
                        scale = p;
                        scaleMatricesInput.setStyle(null);
                        UserConfig.setInt("MatrixDecimalScale", scale);
                    }
                } catch (Exception e) {
                    scaleMatricesInput.setStyle(UserConfig.badStyle());
                }
            }
        });
        int p = UserConfig.getInt("MatrixDecimalScale", 8);
        scaleMatricesInput.setText(p + "");

        calculateAllButton.disableProperty().bind(scaleMatricesInput.textProperty().isEmpty()
                .or(scaleMatricesInput.styleProperty().isEqualTo(UserConfig.badStyle()))
        );

        exportButton.disableProperty().bind(textsArea.textProperty().isEmpty()
        );

    }

    @FXML
    public void calculateAction(ActionEvent event) {
        try {
            webView.getEngine().loadContent("");
            if (calculateButton.isDisabled()) {
                return;
            }
            double[][] primaries, sourceWhitePoint, targetWhitePoint = null;
            if (rgbController.colorSpaceName != null) {
                ColorSpaceType cs = RGBColorSpace.type(rgbController.colorSpaceName);
                primaries = primariesTristimulus(cs);
                sourceWhitePoint = whitePointMatrix(cs);
            } else {
                primaries = new double[3][];
                primaries[0] = rgbController.red;
                primaries[1] = rgbController.green;
                primaries[2] = rgbController.blue;
                sourceWhitePoint = DoubleMatrixTools.columnVector(rgbController.white);
            }
            if (xyzController.relative != null) {
                targetWhitePoint = DoubleMatrixTools.columnVector(xyzController.relative);
            }
            if (primaries == null || sourceWhitePoint == null || targetWhitePoint == null) {
                return;
            }
            Map<String, Object> rgb2xyz = (Map<String, Object>) RGB2XYZConversionMatrix.rgb2xyz(primaries,
                    sourceWhitePoint, targetWhitePoint, algorithm, scale, true);
            double[][] conversionMatrix = (double[][]) rgb2xyz.get("conversionMatrix");
            double[][] conversionMatrixInverse = DoubleMatrixTools.inverse(conversionMatrix);
            String s;
            if (DoubleMatrixTools.same(sourceWhitePoint, targetWhitePoint, scale)) {
                s = Languages.message("RGBXYZsameWhite");
            } else {
                s = Languages.message("RGBXYZdifferentWhite");
            }
            s += "\n\nLinear RGB -> XYZ =\n"
                    + DoubleMatrixTools.print(conversionMatrix, 20, scale)
                    + "XYZ -> Linear RGB =\n"
                    + DoubleMatrixTools.print(conversionMatrixInverse, 20, scale)
                    + "\n----------------" + Languages.message("CalculationProcedure") + "----------------\n"
                    + Languages.message("ReferTo") + "： \n"
                    + "            http://brucelindbloom.com/index.html?WorkingSpaceInfo.html \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                    + (String) rgb2xyz.get("procedure");
            webView.getEngine().loadContent("<pre>" + s + "</pre>");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void calculateAllAction(ActionEvent event) {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private StringTable table;
            private String allTexts;

            @Override
            protected boolean handle() {
                table = RGB2XYZConversionMatrix.allTable(scale);
                allTexts = RGB2XYZConversionMatrix.allTexts(scale);
                return table != null;
            }

            @Override
            protected void whenSucceeded() {
                matrixController.loadTable(table);
                textsArea.setText(allTexts);
                textsArea.home();
            }

        };
        start(task);
    }

    @Override
    public String exportTexts() {
        return textsArea.getText();
    }

}
