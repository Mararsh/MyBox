package mara.mybox.controller;

import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.color.CIEData;
import mara.mybox.color.CIEDataTools;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.color.Illuminant;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

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
    protected WebView webView;
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
            MyBoxLog.error(e.toString());
        }

    }

    private void initAdaptation() {
        initOptions();

        calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                .or(scaleInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                .or(Bindings.isEmpty(sourceColorController.xInput.textProperty()))
                .or(sourceColorController.xInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                .or(Bindings.isEmpty(sourceColorController.yInput.textProperty()))
                .or(sourceColorController.yInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                .or(Bindings.isEmpty(sourceColorController.zInput.textProperty()))
                .or(sourceColorController.zInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
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

    }

    private void initData() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
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
            double[] mc = DoubleTools.scale(adaptedColor, scale);
            String s = Languages.message("CalculatedValues") + ":    X=" + mc[0] + "    Y=" + mc[1] + "    Z=" + mc[2] + "\n";
            double[] mr = DoubleTools.scale(CIEDataTools.relative(mc), scale);
            s += Languages.message("RelativeValues") + ":    X=" + mr[0] + "    Y=" + mr[1] + "    Z=" + mr[2] + "\n";
            double[] mn = DoubleTools.scale(CIEDataTools.normalize(mc), scale);
            s += Languages.message("NormalizedValuesCC") + ":    x=" + mn[0] + "    y=" + mn[1] + "    z=" + mn[2] + "\n"
                    + "\n----------------" + Languages.message("CalculationProcedure") + "----------------\n"
                    + Languages.message("ReferTo") + "： \n"
                    + "            http://www.thefullwiki.org/Standard_illuminant#cite_note-30 \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                    + (String) run.get("procedure");
            webView.getEngine().loadContent("<pre>" + s + "</pre>");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
