package mara.mybox.controller;

import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.color.CIEData;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.color.Illuminant;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-6-1
 * @License Apache License Version 2.0
 */
// http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
public class IlluminantsController extends ChromaticityBaseController {

    protected double sourceWPX, sourceWPY, sourceWPZ, targetWPX, targetWPY, targetWPZ;
    protected ObservableList<Illuminant> illuminants;

    @FXML
    public XYZController sourceColorController;
    @FXML
    public WhitePointController sourceWPController, targetWPController;
    @FXML
    protected TextArea illuminantsArea;
    @FXML
    protected WebView webView;
    @FXML
    protected Button calculateButton, exportButton;
    @FXML
    protected TableView<Illuminant> illuminantsTableView;
    @FXML
    protected TableColumn<Illuminant, String> illumColumn, observerColumn, illumCommentsColumn;
    @FXML
    protected TableColumn<Illuminant, Double> nxillumColumn, nyillumColumn, nzillumColumn,
            rxillumColumn, ryillumColumn, rzillumColumn;
    @FXML
    protected TableColumn<Illuminant, Integer> illuminautTemperatureColumn;

    public IlluminantsController() {
        baseTitle = AppVariables.message("Illuminants");
        exportName = "StandardIlluminants";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initAdaptation();
            initStandardIlluminants();

            initData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void initAdaptation() {
        initOptions();

        calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                .or(scaleInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceColorController.xInput.textProperty()))
                .or(sourceColorController.xInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceColorController.yInput.textProperty()))
                .or(sourceColorController.yInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceColorController.zInput.textProperty()))
                .or(sourceColorController.zInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceWPController.xInput.textProperty()))
                .or(sourceWPController.xInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceWPController.yInput.textProperty()))
                .or(sourceWPController.yInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceWPController.zInput.textProperty()))
                .or(sourceWPController.zInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetWPController.xInput.textProperty()))
                .or(targetWPController.xInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetWPController.yInput.textProperty()))
                .or(targetWPController.yInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(targetWPController.zInput.textProperty()))
                .or(targetWPController.zInput.styleProperty().isEqualTo(badStyle))
        );

    }

    private void initStandardIlluminants() {
        illumColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        observerColumn.setCellValueFactory(new PropertyValueFactory<>("observer"));
        nxillumColumn.setCellValueFactory(new PropertyValueFactory<>("normalizedX"));
        nyillumColumn.setCellValueFactory(new PropertyValueFactory<>("normalizedY"));
        nzillumColumn.setCellValueFactory(new PropertyValueFactory<>("normalizedZ"));
        rxillumColumn.setCellValueFactory(new PropertyValueFactory<>("relativeX"));
        ryillumColumn.setCellValueFactory(new PropertyValueFactory<>("relativeY"));
        rzillumColumn.setCellValueFactory(new PropertyValueFactory<>("relativeZ"));
        illuminautTemperatureColumn.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        illumCommentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

    }

    private void initData() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String illuminantString;

                @Override
                protected boolean handle() {
                    illuminants = FXCollections.observableArrayList();
                    illuminants.addAll(Illuminant.all(8));
                    illuminantString = Illuminant.string();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    illuminantsTableView.setItems(illuminants);
                    illuminantsArea.setText(illuminantString);
                    illuminantsArea.home();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
            String s = message("CalculatedValues") + ":    X=" + mc[0] + "    Y=" + mc[1] + "    Z=" + mc[2] + "\n";
            double[] mr = DoubleTools.scale(CIEData.relative(mc), scale);
            s += message("RelativeValues") + ":    X=" + mr[0] + "    Y=" + mr[1] + "    Z=" + mr[2] + "\n";
            double[] mn = DoubleTools.scale(CIEData.normalize(mc), scale);
            s += message("NormalizedValuesCC") + ":    x=" + mn[0] + "    y=" + mn[1] + "    z=" + mn[2] + "\n"
                    + "\n----------------" + message("CalculationProcedure") + "----------------\n"
                    + message("ReferTo") + "： \n"
                    + "            http://www.thefullwiki.org/Standard_illuminant#cite_note-30 \n"
                    + "            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n"
                    + (String) run.get("procedure");
            webView.getEngine().loadContent("<pre>" + s + "</pre>");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String exportTexts() {
        return illuminantsArea.getText();
    }

}
