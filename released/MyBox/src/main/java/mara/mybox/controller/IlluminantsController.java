package mara.mybox.controller;

import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mara.mybox.color.CIEDataTools;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.color.Illuminant;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.DoubleArrayTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-6-1
 * @License Apache License Version 2.0
 */
// http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
public class IlluminantsController extends ChromaticityBaseController {

    protected double sourceWPX, sourceWPY, sourceWPZ, targetWPX, targetWPY, targetWPZ;

    @FXML
    public XYZController sourceColorController;
    @FXML
    public WhitePointController sourceWPController, targetWPController;
    @FXML
    protected Button calculateButton;
    @FXML
    protected HtmlTableController illuminantsController;

    public IlluminantsController() {
        baseTitle = Languages.message("Illuminants");
        exportName = "StandardIlluminants";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initAdaptation();
            initData();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    private void initAdaptation() {
        initOptions();

        calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                .or(scaleInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(sourceColorController.xInput.textProperty()))
                .or(sourceColorController.xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(sourceColorController.yInput.textProperty()))
                .or(sourceColorController.yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(sourceColorController.zInput.textProperty()))
                .or(sourceColorController.zInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(sourceWPController.xInput.textProperty()))
                .or(sourceWPController.xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(sourceWPController.yInput.textProperty()))
                .or(sourceWPController.yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(sourceWPController.zInput.textProperty()))
                .or(sourceWPController.zInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(targetWPController.xInput.textProperty()))
                .or(targetWPController.xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(targetWPController.yInput.textProperty()))
                .or(targetWPController.yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(Bindings.isEmpty(targetWPController.zInput.textProperty()))
                .or(targetWPController.zInput.styleProperty().isEqualTo(UserConfig.badStyle()))
        );

    }

    private void initData() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private StringTable table;

            @Override
            protected boolean handle() {
                table = Illuminant.table(scale);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                illuminantsController.loadTable(table);
            }

        };
        start(task);
    }

    @FXML
    public void calculateAction(ActionEvent event) {
        try {
            webView.getEngine().loadContent("");
            if (calculateButton.isDisabled()) {
                return;
            }
            double[] swp = sourceWPController.relative;
            double[] twp = targetWPController.relative;
            if (swp == null || twp == null) {
                return;
            }
            Map<String, Object> run = (Map<String, Object>) ChromaticAdaptation.adapt(
                    sourceColorController.x, sourceColorController.y, sourceColorController.z,
                    swp[0], swp[1], swp[2], twp[0], twp[1], twp[2], algorithm, scale, true);
            double[] adaptedColor = (double[]) run.get("adaptedColor");
            double[] mc = DoubleArrayTools.scale(adaptedColor, scale);
            String s = Languages.message("CalculatedValues") + ":    X=" + mc[0] + "    Y=" + mc[1] + "    Z=" + mc[2] + "\n";
            double[] mr = DoubleArrayTools.scale(CIEDataTools.relative(mc), scale);
            s += Languages.message("RelativeValues") + ":    X=" + mr[0] + "    Y=" + mr[1] + "    Z=" + mr[2] + "\n";
            double[] mn = DoubleArrayTools.scale(CIEDataTools.normalize(mc), scale);
            s += Languages.message("NormalizedValuesCC") + ":    x=" + mn[0] + "    y=" + mn[1] + "    z=" + mn[2] + "\n"
                    + "\n----------------" + Languages.message("CalculationProcedure") + "----------------\n"
                    + Languages.message("ReferTo") + "： \n"
                    + "            http://www.thefullwiki.org/Standard_illuminant#cite_note-30 \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                    + (String) run.get("procedure");
            webView.getEngine().loadContent("<pre>" + s + "</pre>");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
