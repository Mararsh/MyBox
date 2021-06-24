package mara.mybox.controller;

import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.color.RGB2XYZConversionMatrix;
import mara.mybox.color.RGBColorSpace;
import mara.mybox.color.RGBColorSpace.ColorSpaceType;
import static mara.mybox.color.RGBColorSpace.primariesTristimulus;
import static mara.mybox.color.RGBColorSpace.whitePointMatrix;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-5-20
 * @Description
 * @License Apache License Version 2.0
 */
public class RGB2XYZConversionMatrixController extends ChromaticityBaseController {

    protected ObservableList<RGB2XYZConversionMatrix> allData;

    @FXML
    public RGBColorSpaceController rgbController;
    @FXML
    public WhitePointController xyzController;
    @FXML
    protected TextArea textsArea;
    @FXML
    protected WebView webView;
    @FXML
    protected TextField scaleMatricesInput;
    @FXML
    protected TableView<RGB2XYZConversionMatrix> matrixTableView;
    @FXML
    protected TableColumn<RGB2XYZConversionMatrix, String> rgbColumn, rgbWhiteColumn, xyzWhiteColumn, algorithmColumn,
            rgb2xyzColumn, xyz2rgbCloumn;
    @FXML
    protected Button calculateButton, calculateAllButton, exportButton;

    public RGB2XYZConversionMatrixController() {
        baseTitle = AppVariables.message("LinearRGB2XYZMatrix");
        exportName = "LinearRGB2XYZMatrix";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initAdaptation();
            initMatrices();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void initAdaptation() {

        initOptions();

        calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                .or(scaleInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.redXInput.textProperty()))
                .or(rgbController.redXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.redYInput.textProperty()))
                .or(rgbController.redYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.redZInput.textProperty()))
                .or(rgbController.redZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.greenXInput.textProperty()))
                .or(rgbController.greenXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.greenYInput.textProperty()))
                .or(rgbController.greenYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.greenZInput.textProperty()))
                .or(rgbController.greenZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.blueXInput.textProperty()))
                .or(rgbController.blueXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.blueYInput.textProperty()))
                .or(rgbController.blueYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.blueZInput.textProperty()))
                .or(rgbController.blueZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.whiteXInput.textProperty()))
                .or(rgbController.whiteXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.whiteYInput.textProperty()))
                .or(rgbController.whiteYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(rgbController.whiteZInput.textProperty()))
                .or(rgbController.whiteZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(xyzController.xInput.textProperty()))
                .or(xyzController.xInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(xyzController.yInput.textProperty()))
                .or(xyzController.yInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(xyzController.zInput.textProperty()))
                .or(xyzController.zInput.styleProperty().isEqualTo(badStyle))
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
        rgbColumn.setCellValueFactory(new PropertyValueFactory<>("rgb"));
        rgbWhiteColumn.setCellValueFactory(new PropertyValueFactory<>("rgbWhite"));
        xyzWhiteColumn.setCellValueFactory(new PropertyValueFactory<>("xyzWhite"));
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithm"));
        rgb2xyzColumn.setCellValueFactory(new PropertyValueFactory<>("rgb2xyz"));
        xyz2rgbCloumn.setCellValueFactory(new PropertyValueFactory<>("xyz2rgb"));

        scaleMatricesInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    int p = Integer.parseInt(scaleMatricesInput.getText());
                    if (p <= 0) {
                        scaleMatricesInput.setStyle(badStyle);
                    } else {
                        scale = p;
                        scaleMatricesInput.setStyle(null);
                        AppVariables.setUserConfigInt("MatrixDecimalScale", scale);
                    }
                } catch (Exception e) {
                    scaleMatricesInput.setStyle(badStyle);
                }
            }
        });
        int p = AppVariables.getUserConfigInt("MatrixDecimalScale", 8);
        scaleMatricesInput.setText(p + "");

        calculateAllButton.disableProperty().bind(scaleMatricesInput.textProperty().isEmpty()
                .or(scaleMatricesInput.styleProperty().isEqualTo(badStyle))
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
                sourceWhitePoint = MatrixDoubleTools.columnVector(rgbController.white);
            }
            if (xyzController.relative != null) {
                targetWhitePoint = MatrixDoubleTools.columnVector(xyzController.relative);
            }
            if (primaries == null || sourceWhitePoint == null || targetWhitePoint == null) {
                return;
            }
            Map<String, Object> rgb2xyz = (Map<String, Object>) RGB2XYZConversionMatrix.rgb2xyz(primaries,
                    sourceWhitePoint, targetWhitePoint, algorithm, scale, true);
            double[][] conversionMatrix = (double[][]) rgb2xyz.get("conversionMatrix");
            double[][] conversionMatrixInverse = MatrixDoubleTools.inverse(conversionMatrix);
            String s;
            if (MatrixDoubleTools.same(sourceWhitePoint, targetWhitePoint, scale)) {
                s = message("RGBXYZsameWhite");
            } else {
                s = message("RGBXYZdifferentWhite");
            }
            s += "\n\nLinear RGB -> XYZ =\n"
                    + MatrixDoubleTools.print(conversionMatrix, 20, scale)
                    + "XYZ -> Linear RGB =\n"
                    + MatrixDoubleTools.print(conversionMatrixInverse, 20, scale)
                    + "\n----------------" + message("CalculationProcedure") + "----------------\n"
                    + message("ReferTo") + "： \n"
                    + "            http://brucelindbloom.com/index.html?WorkingSpaceInfo.html \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                    + (String) rgb2xyz.get("procedure");
            webView.getEngine().loadContent("<pre>" + s + "</pre>");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void calculateAllAction(ActionEvent event) {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String allTexts;

                @Override
                protected boolean handle() {
                    allData = FXCollections.observableArrayList();
                    allData.addAll(RGB2XYZConversionMatrix.all(scale));
                    allTexts = RGB2XYZConversionMatrix.allTexts(scale);
                    return allTexts != null;
                }

                @Override
                protected void whenSucceeded() {
                    matrixTableView.setItems(allData);
                    textsArea.setText(allTexts);
                    textsArea.home();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public String exportTexts() {
        return textsArea.getText();
    }

}
