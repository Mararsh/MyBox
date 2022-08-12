package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegressionCombinationController extends BaseData2DHandleController {

    protected SimpleLinearRegression simpleRegression;
    protected double alpha;
    protected ObservableList<List<String>> results;

    @FXML
    protected CheckBox interceptCheck;
    @FXML
    protected ComboBox<String> alphaSelector;
    @FXML
    protected ControlData2DRegressionTable resultsController;
    @FXML
    protected Button chartButton;

    public Data2DSimpleLinearRegressionCombinationController() {
        baseTitle = message("SimpleLinearRegressionCombination");
        TipsLabelKey = "SimpleLinearRegressionTips";
        defaultScale = 8;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            noColumnSelection(true);

            alpha = UserConfig.getDouble(baseName + "Alpha", 0.05);
            if (alpha >= 1 || alpha <= 0) {
                alpha = 0.05;
            }
            alphaSelector.getItems().addAll(Arrays.asList(
                    "0.05", "0.01", "0.02", "0.03", "0.06", "0.1"
            ));
            alphaSelector.getSelectionModel().select(alpha + "");
            alphaSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.parseDouble(newValue);
                        if (v > 0 && v < 1) {
                            alpha = v;
                            alphaSelector.getEditor().setStyle(null);
                            UserConfig.setDouble(baseName + "Alpha", alpha);
                        } else {
                            alphaSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        alphaSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            interceptCheck.setSelected(UserConfig.getBoolean(baseName + "Intercept", true));
            interceptCheck.selectedProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue v, Object ov, Object nv) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "Intercept", interceptCheck.isSelected());
                }
            });

            resultsController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            columnsPane.getChildren().clear();
            for (String name : names) {
                columnsPane.getChildren().add(new CheckBox(name));
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void selectAllColumns() {
        try {
            for (Node node : columnsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(true);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void selectNoneColumn() {
        try {
            for (Node node : columnsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(false);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        resultsController.clear();
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    List<Integer> cols = new ArrayList<>();
                    for (Node node : columnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
                        if (cb.isSelected()) {
                            cols.add(data2D.colOrder(cb.getText()));
                        }
                    }
                    if (cols.isEmpty()) {
                        cols = data2D.columnIndices();
                    }
                    for (int yIndex : cols) {
                        for (int xIndex : cols) {
                            if (xIndex == yIndex) {
                                continue;
                            }
                            regress(xIndex, yIndex);
                        }
                    }
                    data2D.stopFilter();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void regress(int xIndex, int yIndex) {
                try {
                    String xName = data2D.columnName(xIndex);
                    String yName = data2D.columnName(yIndex);
                    List<Integer> dataColsIndices = new ArrayList<>();
                    dataColsIndices.add(xIndex);
                    dataColsIndices.add(yIndex);
                    simpleRegression = new SimpleLinearRegression(interceptCheck.isSelected(), xName, yName, scale);
                    if (isAllPages()) {
                        data2D.simpleLinearRegression(null, dataColsIndices, simpleRegression, false);
                    } else {
                        simpleRegression.addData(filtered(dataColsIndices, true), invalidAs);
                    }
                    List<String> row = new ArrayList<>();
                    row.add(yName);
                    row.add(xName);
                    row.add(DoubleTools.format(simpleRegression.getRSquare(), scale));
                    row.add(DoubleTools.format(simpleRegression.getR(), scale));
                    row.add(simpleRegression.getModel());
                    row.add(DoubleTools.format(simpleRegression.getSlope(), scale));
                    row.add(DoubleTools.format(simpleRegression.getIntercept(), scale));
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resultsController.addRow(row);
                        }
                    });

                } catch (Exception e) {
                    error = e.toString();
                }
            }

            @Override
            protected void whenSucceeded() {
                resultsController.sortR();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);

    }

    @FXML
    public void editResultsAction() {
        resultsController.editTable();
    }

    @FXML
    public void chartAction() {
        resultsController.editAction();
    }

    @FXML
    public void about() {
        openLink(HelpTools.aboutLinearRegressionHtml());
    }

    /*
        static
     */
    public static Data2DSimpleLinearRegressionCombinationController open(ControlData2DEditTable tableController) {
        try {
            Data2DSimpleLinearRegressionCombinationController controller = (Data2DSimpleLinearRegressionCombinationController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSimpleLinearRegressionCombinationFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
