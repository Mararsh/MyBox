package mara.mybox.controller;

import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.controller.base.ChromaticityBaseController;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.MatrixTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.AppVaribles.message;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @Description
 * @License Apache License Version 2.0
 */
// http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
public class ChromaticAdaptationMatrixController extends ChromaticityBaseController {

    protected ObservableList<ChromaticAdaptation> allData;

    @FXML
    public WhitePointController sourceWPController, targetWPController;
    @FXML
    protected Button calculateButton, calculateAllButton, exportButton;
    @FXML
    protected TextField scaleMatricesInput;
    @FXML
    protected TextArea calculateArea, allArea;
    @FXML
    protected TableView<ChromaticAdaptation> allTableView;
    @FXML
    protected TableColumn<ChromaticAdaptation, String> sourceColumn, targetColumn,
            bradfordColumn, xyzColumn, vonCloumn;

    public ChromaticAdaptationMatrixController() {
        baseTitle = AppVaribles.message("ChromaticAdaptationMatrix");
        exportName = "ChromaticAdaptationMatrices";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initCalculation();
            initAll();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void initCalculation() {
        try {
            initOptions();

            calculateButton.disableProperty().bind(Bindings.isEmpty(scaleInput.textProperty())
                    .or(scaleInput.styleProperty().isEqualTo(badStyle))
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

        } catch (Exception e) {

        }
    }

    public void initAll() {
        try {
            sourceColumn.setCellValueFactory(new PropertyValueFactory<ChromaticAdaptation, String>("source"));
            targetColumn.setCellValueFactory(new PropertyValueFactory<ChromaticAdaptation, String>("target"));
            bradfordColumn.setCellValueFactory(new PropertyValueFactory<ChromaticAdaptation, String>("BradfordMethod"));
//            bradfordColumn.setCellFactory(TextFieldTableCell.<ChromaticAdaptation>forTableColumn());
            xyzColumn.setCellValueFactory(new PropertyValueFactory<ChromaticAdaptation, String>("XYZScalingMethod"));
//            xyzColumn.setCellFactory(TextFieldTableCell.<ChromaticAdaptation>forTableColumn());
            vonCloumn.setCellValueFactory(new PropertyValueFactory<ChromaticAdaptation, String>("VonKriesMethod"));
//            vonCloumn.setCellFactory(TextFieldTableCell.<ChromaticAdaptation>forTableColumn());

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
                            AppVaribles.setUserConfigInt("MatrixDecimalScale", scale);
                        }
                    } catch (Exception e) {
                        scaleMatricesInput.setStyle(badStyle);
                    }
                }
            });
            int p = AppVaribles.getUserConfigInt("MatrixDecimalScale", 8);
            scaleMatricesInput.setText(p + "");

            calculateAllButton.disableProperty().bind(scaleMatricesInput.textProperty().isEmpty()
                    .or(scaleMatricesInput.styleProperty().isEqualTo(badStyle))
            );

            exportButton.disableProperty().bind(allArea.textProperty().isEmpty()
            );

        } catch (Exception e) {

        }
    }

    @FXML
    public void calculateAction(ActionEvent event) {
        calculateArea.clear();
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
        calculateArea.setText(MatrixTools.print((double[][]) run.get("matrix"), 0, scale));
        calculateArea.appendText("\n\n----------------" + message("CalculationProcedure") + "----------------\n");
        calculateArea.appendText(message("ReferTo") + "： \n");
        calculateArea.appendText("            http://www.thefullwiki.org/Standard_illuminant#cite_note-30 \n");
        calculateArea.appendText("            http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html \n\n");
        calculateArea.appendText((String) run.get("procedure"));
        calculateArea.home();

    }

    @FXML
    public void calculateAllAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        task = new Task<Void>() {
            private boolean ok;
            private String allTexts;

            @Override
            protected Void call() throws Exception {
                allData = FXCollections.observableArrayList();
                allData.addAll(ChromaticAdaptation.all(scale));
                allTexts = ChromaticAdaptation.allTexts(scale);
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        allTableView.setItems(allData);
                        allArea.setText(allTexts);
                        allArea.home();
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public String exportTexts() {
        return allArea.getText();
    }

}
