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
import mara.mybox.color.RGB2RGBConversionMatrix;
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
public class RGB2RGBConversionMatrixController extends ChromaticityBaseController {

    protected ObservableList<RGB2RGBConversionMatrix> allData;

    @FXML
    public RGBColorSpaceController sourceController, targetController;
    @FXML
    protected TextArea textsArea;
    @FXML
    protected WebView webView;
    @FXML
    protected TextField scaleMatricesInput;
    @FXML
    protected TableView<RGB2RGBConversionMatrix> matrixTableView;
    @FXML
    protected TableColumn<RGB2RGBConversionMatrix, String> sourceCSColumn, sourceWhiteColumn,
            targetCSColumn, targetWhiteColumn, algorithmColumn, source2targetColumn, target2sourceCloumn;
    @FXML
    protected Button calculateButton, calculateAllButton, exportButton;

    public RGB2RGBConversionMatrixController() {
        baseTitle = AppVariables.message("LinearRGB2RGBMatrix");
        exportName = "LinearRGB2RGBMatrix";
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
                .or(Bindings.isEmpty(sourceController.redXInput.textProperty()))
                .or(sourceController.redXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.redYInput.textProperty()))
                .or(sourceController.redYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.redZInput.textProperty()))
                .or(sourceController.redZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.greenXInput.textProperty()))
                .or(sourceController.greenXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.greenYInput.textProperty()))
                .or(sourceController.greenYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.greenZInput.textProperty()))
                .or(sourceController.greenZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.blueXInput.textProperty()))
                .or(sourceController.blueXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.blueYInput.textProperty()))
                .or(sourceController.blueYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.blueZInput.textProperty()))
                .or(sourceController.blueZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.whiteXInput.textProperty()))
                .or(sourceController.whiteXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.whiteYInput.textProperty()))
                .or(sourceController.whiteYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceController.whiteZInput.textProperty()))
                .or(sourceController.whiteZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.redXInput.textProperty()))
                .or(targetController.redXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.redYInput.textProperty()))
                .or(targetController.redYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.redZInput.textProperty()))
                .or(targetController.redZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.greenXInput.textProperty()))
                .or(targetController.greenXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.greenYInput.textProperty()))
                .or(targetController.greenYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.greenZInput.textProperty()))
                .or(targetController.greenZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.blueXInput.textProperty()))
                .or(targetController.blueXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.blueYInput.textProperty()))
                .or(targetController.blueYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.blueZInput.textProperty()))
                .or(targetController.blueZInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.whiteXInput.textProperty()))
                .or(targetController.whiteXInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.whiteYInput.textProperty()))
                .or(targetController.whiteYInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetController.whiteZInput.textProperty()))
                .or(targetController.whiteZInput.styleProperty().isEqualTo(badStyle))
        );

    }

    @Override
    public void checkAlgorithm() {
        super.checkAlgorithm();
        sourceController.algorithm = algorithm;
        targetController.algorithm = algorithm;
    }

    @Override
    public void checkScale() {
        super.checkScale();
        sourceController.scale = scale;
        targetController.scale = scale;
    }

    private void initMatrices() {
        sourceCSColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceWhiteColumn.setCellValueFactory(new PropertyValueFactory<>("sourceWhite"));
        targetCSColumn.setCellValueFactory(new PropertyValueFactory<>("target"));
        targetWhiteColumn.setCellValueFactory(new PropertyValueFactory<>("targetWhite"));
        algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithm"));
        source2targetColumn.setCellValueFactory(new PropertyValueFactory<>("source2target"));

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
            double[][] sourcePrimaries, sourceWhitePoint;
            if (sourceController.colorSpaceName != null) {
                ColorSpaceType cs = RGBColorSpace.type(sourceController.colorSpaceName);
                sourcePrimaries = primariesTristimulus(cs);
                sourceWhitePoint = whitePointMatrix(cs);
            } else {
                sourcePrimaries = new double[3][];
                sourcePrimaries[0] = sourceController.red;
                sourcePrimaries[1] = sourceController.green;
                sourcePrimaries[2] = sourceController.blue;
                sourceWhitePoint = MatrixDoubleTools.columnVector(sourceController.white);
            }
            if (sourcePrimaries == null || sourceWhitePoint == null) {
                return;
            }
            double[][] targetPrimaries, targetWhitePoint;
            if (targetController.colorSpaceName != null) {
                ColorSpaceType cs = RGBColorSpace.type(targetController.colorSpaceName);
                targetPrimaries = primariesTristimulus(cs);
                targetWhitePoint = whitePointMatrix(cs);
            } else {
                targetPrimaries = new double[3][];
                targetPrimaries[0] = targetController.red;
                targetPrimaries[1] = targetController.green;
                targetPrimaries[2] = targetController.blue;
                targetWhitePoint = MatrixDoubleTools.columnVector(targetController.white);
            }
            if (targetPrimaries == null || targetWhitePoint == null) {
                return;
            }
            Map<String, Object> rgb2rgb = (Map<String, Object>) RGB2RGBConversionMatrix.rgb2rgb(
                    sourcePrimaries, sourceWhitePoint, targetPrimaries, targetWhitePoint,
                    algorithm, scale, true);
            double[][] conversionMatrix = (double[][]) rgb2rgb.get("conversionMatrix");
            double[][] conversionMatrixInverse = MatrixDoubleTools.inverse(conversionMatrix);
            String s = message("Source") + " -> " + message("Target") + " =\n"
                    + MatrixDoubleTools.print(conversionMatrix, 20, scale)
                    + message("Target") + " -> " + message("Source") + " =\n"
                    + MatrixDoubleTools.print(conversionMatrixInverse, 20, scale)
                    + "\n----------------" + message("CalculationProcedure") + "----------------\n"
                    + message("ReferTo") + "： \n"
                    + "            http://brucelindbloom.com/index.html?WorkingSpaceInfo.html \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                    + (String) rgb2rgb.get("procedure");
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
                    allData.addAll(RGB2RGBConversionMatrix.all(scale));
                    allTexts = RGB2RGBConversionMatrix.allTexts(scale);
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
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public String exportTexts() {
        return textsArea.getText();
    }

}
