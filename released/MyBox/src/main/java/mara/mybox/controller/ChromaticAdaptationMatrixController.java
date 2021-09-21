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
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @License Apache License Version 2.0
 */
// http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
public class ChromaticAdaptationMatrixController extends ChromaticityBaseController {

    @FXML
    public WhitePointController sourceWPController, targetWPController;
    @FXML
    protected Button calculateButton, calculateAllButton, exportButton;
    @FXML
    protected TextField scaleMatricesInput;
    @FXML
    protected TextArea allArea;
    @FXML
    protected HtmlTableController matrixController;

    public ChromaticAdaptationMatrixController() {
        baseTitle = Languages.message("ChromaticAdaptationMatrix");
        exportName = "ChromaticAdaptationMatrices";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initCalculation();
            initAll();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void initCalculation() {
        try {
            initOptions();

            calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                    .or(scaleInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(sourceWPController.xInput.textProperty()))
                    .or(sourceWPController.xInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(sourceWPController.yInput.textProperty()))
                    .or(sourceWPController.yInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(sourceWPController.zInput.textProperty()))
                    .or(sourceWPController.zInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(targetWPController.xInput.textProperty()))
                    .or(targetWPController.xInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(targetWPController.yInput.textProperty()))
                    .or(targetWPController.yInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(targetWPController.zInput.textProperty()))
                    .or(targetWPController.zInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

        } catch (Exception e) {

        }
    }

    public void initAll() {
        try {
            scaleMatricesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int p = Integer.parseInt(scaleMatricesInput.getText());
                        if (p <= 0) {
                            scaleMatricesInput.setStyle(NodeStyleTools.badStyle);
                        } else {
                            scale = p;
                            scaleMatricesInput.setStyle(null);
                            UserConfig.setInt("MatrixDecimalScale", scale);
                        }
                    } catch (Exception e) {
                        scaleMatricesInput.setStyle(NodeStyleTools.badStyle);
                    }
                }
            });
            int p = UserConfig.getInt("MatrixDecimalScale", 8);
            scaleMatricesInput.setText(p + "");

            calculateAllButton.disableProperty().bind(scaleMatricesInput.textProperty().isEmpty()
                    .or(scaleMatricesInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

            exportButton.disableProperty().bind(allArea.textProperty().isEmpty()
            );

        } catch (Exception e) {

        }
    }

    @FXML
    public void calculateAction(ActionEvent event) {
        webView.getEngine().loadContent("");
        if (calculateButton.isDisabled()) {
            return;
        }
        double[] swp = sourceWPController.relative;
        double[] twp = targetWPController.relative;
        if (swp == null || twp == null) {
            return;
        }
        Map<String, Object> run = ChromaticAdaptation.matrixDemo(
                swp[0], swp[1], swp[2], twp[0], twp[1], twp[2], algorithm, scale);
        String s = MatrixDoubleTools.print((double[][]) run.get("matrix"), 0, scale)
                + "\n\n----------------" + Languages.message("CalculationProcedure") + "----------------\n"
                + Languages.message("ReferTo") + "： \n"
                + "            http://www.thefullwiki.org/Standard_illuminant#cite_note-30 \n"
                + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                + (String) run.get("procedure");
        webView.getEngine().loadContent("<pre>" + s + "</pre>");
    }

    @FXML
    public void calculateAllAction(ActionEvent event) {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private StringTable table;
                private String allTexts;

                @Override
                protected boolean handle() {
                    table = ChromaticAdaptation.table(scale);
                    allTexts = ChromaticAdaptation.allTexts(scale);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    matrixController.loadTable(table);
                    allArea.setText(allTexts);
                    allArea.home();
                }

            };
            start(task);
        }
    }

    @Override
    public String exportTexts() {
        return allArea.getText();
    }

}
