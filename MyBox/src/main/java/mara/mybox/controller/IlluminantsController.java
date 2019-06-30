package mara.mybox.controller;

import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import mara.mybox.color.CIEData;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.color.Illuminant;
import mara.mybox.controller.base.ChromaticityBaseController;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

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
    private TextArea adaptationArea, illuminantsArea;
    @FXML
    protected Button calculateButton, exportButton;
    @FXML
    private TableView<Illuminant> illuminantsTableView;
    @FXML
    private TableColumn<Illuminant, String> illumColumn, observerColumn, illumCommentsColumn;
    @FXML
    private TableColumn<Illuminant, Double> nxillumColumn, nyillumColumn, nzillumColumn,
            rxillumColumn, ryillumColumn, rzillumColumn;
    @FXML
    private TableColumn<Illuminant, Integer> illuminautTemperatureColumn;

    public IlluminantsController() {
        baseTitle = AppVaribles.getMessage("Illuminants");
        exportName = "StandardIlluminants";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initAdaptation();
            initStandardIlluminants();

        } catch (Exception e) {
            logger.error(e.toString());
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
        illumColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, String>("name"));
        observerColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, String>("observer"));
        nxillumColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, Double>("normalizedX"));
        nyillumColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, Double>("normalizedY"));
        nzillumColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, Double>("normalizedZ"));
        rxillumColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, Double>("relativeX"));
        ryillumColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, Double>("relativeY"));
        rzillumColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, Double>("relativeZ"));
        illuminautTemperatureColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, Integer>("temperature"));
        illumCommentsColumn.setCellValueFactory(new PropertyValueFactory<Illuminant, String>("comments"));

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            initData();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void initData() {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        task = new Task<Void>() {
            private boolean ok;
            private String illuminantString;

            @Override
            protected Void call() throws Exception {
                illuminants = FXCollections.observableArrayList();
                illuminants.addAll(Illuminant.all(8));
                illuminantString = Illuminant.string();

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        illuminantsTableView.setItems(illuminants);
                        illuminantsArea.setText(illuminantString);
                        illuminantsArea.home();
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void calculateAction(ActionEvent event) {
        try {
            adaptationArea.clear();
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
            adaptationArea.setText(getMessage("CalculatedValues") + ":    X=" + mc[0] + "    Y=" + mc[1] + "    Z=" + mc[2] + "\n");
            double[] mr = DoubleTools.scale(CIEData.relative(mc), scale);
            adaptationArea.appendText(getMessage("RelativeValues") + ":    X=" + mr[0] + "    Y=" + mr[1] + "    Z=" + mr[2] + "\n");
            double[] mn = DoubleTools.scale(CIEData.normalize(mc), scale);
            adaptationArea.appendText(getMessage("NormalizedValuesCC") + ":    x=" + mn[0] + "    y=" + mn[1] + "    z=" + mn[2] + "\n");
            adaptationArea.appendText("\n----------------" + getMessage("CalculationProcedure") + "----------------\n");
            adaptationArea.appendText(getMessage("ReferTo") + "： \n");
            adaptationArea.appendText("            http://www.thefullwiki.org/Standard_illuminant#cite_note-30 \n");
            adaptationArea.appendText("            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n");
            adaptationArea.appendText((String) run.get("procedure"));
            adaptationArea.home();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public String exportTexts() {
        return illuminantsArea.getText();
    }

}
