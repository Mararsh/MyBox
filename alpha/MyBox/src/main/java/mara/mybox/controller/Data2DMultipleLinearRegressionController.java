package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import mara.mybox.calculation.OLSLinearRegression;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DMultipleLinearRegressionController extends BaseData2DRegressionController {

    protected OLSLinearRegression regression;
    protected String yName;
    protected int yCol;
    protected List<String> xNames;

    @FXML
    protected TabPane chartTabPane;
    @FXML
    protected Tab modelTab, resultsTab;

    public Data2DMultipleLinearRegressionController() {
        baseTitle = message("MultipleLinearRegression");
        TipsLabelKey = "MultipleLinearRegressionTips";
        defaultScale = 8;
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            invalidAs = InvalidAs.Empty;

            dataColsIndices = new ArrayList<>();
            yName = categoryColumnSelector.getSelectionModel().getSelectedItem();
            yCol = data2D.colOrder(yName);
            if (yCol < 0) {
                popError(message("SelectToHandle") + ": " + message("DependentVariable"));
                return false;
            }
            dataColsIndices.add(yCol);

            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("IndependentVariable"));
                return false;
            }
            xNames = new ArrayList<>();
            for (int i = 0; i < checkedColsIndices.size(); i++) {
                int col = checkedColsIndices.get(i);
                if (!dataColsIndices.contains(col)) {
                    dataColsIndices.add(col);
                    xNames.add(checkedColsNames.get(i));
                }
            }
            if (xNames.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("IndependentVariable"));
                return false;
            }
            regression = null;

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        modelController.clear();
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    List<List<String>> data = filteredData(dataColsIndices, false);
                    if (data == null || data.isEmpty()) {
                        error = message("NoData");
                        return false;
                    }
                    regression = new OLSLinearRegression(interceptCheck.isSelected())
                            .setTask(this).setScale(scale)
                            .setInvalidAs(invalidAs)
                            .setyName(yName).setxNames(xNames);
                    int n = data.size();
                    int k = xNames.size();
                    String[] sy = new String[data.size()];
                    String[][] sx = new String[n][k];
                    for (int i = 0; i < n; i++) {
                        List<String> row = data.get(i);
                        sy[i] = row.get(0);
                        for (int j = 0; j < k; j++) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            sx[i][j] = row.get(j + 1);
                        }
                    }
                    return regression.calculate(sy, sx);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void whenFailed() {
                if (isCancelled()) {
                    return;
                }
                if (error != null && !error.isBlank()) {
                    //https://db.apache.org/derby/docs/10.17/ref/rrefsqljvarsamp.html#rrefsqljvarsamp
                    if (error.contains("java.sql.SQLDataException: 22003 : [0] DOUBLE")) {
                        alertError(error + "\n\n" + message("DataOverflow"));
                    } else {
                        alertError(error + "\n\n" + message("RegressionFailedNotice"));
                    }
                } else {
                    alertError(message("RegressionFailedNotice"));
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                closeTask();
                if (ok) {
                    writeRegressionData();
                    writeModel();
                    rightPane.setDisable(false);
                }
            }

        };
        start(task, false);
    }

    protected void writeModel() {
        try {
            regression.setScale(scale);
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");
            double[] coefficients = regression.scaledCoefficients();
            intercept = DoubleTools.scale(regression.intercept, scale);
            String scriptModel = "y = " + intercept;
            String model = yName + " = " + intercept;
            boolean invalid = false;
            for (int i = 0; i < coefficients.length; i++) {
                double d = coefficients[i];
                if (DoubleTools.invalidDouble(d)) {
                    invalid = true;
                }
                scriptModel += " + " + d + " * x" + i;
                model += (d > 0 ? " + " : " - ") + Math.abs(d) + " * " + xNames.get(i);
            }
            s.append(" <script>\n"
                    + "    function calculate() {\n");
            for (int i = 0; i < xNames.size(); i++) {
                s.append("      var x" + i + " = document.getElementById('inputX" + i + "').value;  　\n");
            }
            s.append("      var y =  " + scriptModel + ";\n"
                    + "      document.getElementById('outputY').value = y;\n"
                    + "    }\n"
                    + "  </script>\n\n");
            String m = message("LinearModel") + ": " + model;
            s.append("\n<DIV>").append(m).append("</DIV>\n");
            s.append("<DIV>\n");
            for (int i = 0; i < xNames.size(); i++) {
                s.append("<P>").append(message("IndependentVariable")).append(": ").append(xNames.get(i)).append(" = \n");
                s.append("<INPUT id=\"inputX" + i + "\" type=\"text\" style=\"width:200px\"/>\n");
            }
            s.append("<BUTTON type=\"button\" onclick=\"calculate();\">").append(message("Predict")).append("</BUTTON></P>\n");
            s.append("<P>").append(message("DependentVariable")).append(": ").append(yName).append(" = \n");
            s.append("<INPUT id=\"outputY\"  type=\"text\" style=\"width:200px\"/></P>\n");
            s.append("</DIV>\n<HR/>\n");

            s.append("<H3 align=center>").append(message("Model")).append("</H3>\n");
            List<String> names = new ArrayList<>();
            names.add(message("Name"));
            names.add(message("Value"));
            StringTable table = new StringTable(names);

            List<String> row = new ArrayList<>();

            row.add(message("DependentVariable"));
            row.add(yName);
            table.add(row);

            row = new ArrayList<>();
            row.add(message("IndependentVariable"));
            row.add(xNames.toString());
            table.add(row);

            row = new ArrayList<>();
            row.add(message("NumberOfObservations"));
            row.add(regression.n + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("Intercept"));
            row.add(intercept + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("Coefficients"));
            row.add(Arrays.toString(coefficients));
            table.add(row);

            row = new ArrayList<>();
            row.add(message("CoefficientOfDetermination"));
            row.add(DoubleTools.scale(regression.rSqure, scale) + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("AdjustedRSquared"));
            row.add(DoubleTools.scale(regression.adjustedRSqure, scale) + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("StandardError"));
            row.add(DoubleTools.scale(regression.standardError, scale) + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("Variance"));
            row.add(DoubleTools.scale(regression.variance, scale) + "");
            table.add(row);

            s.append(table.div());
            s.append("</BODY>\n");
            modelController.loadContents(HtmlWriteTools.html(s.toString()));

            if (invalid) {
                alertError(message("InvalidData") + "\n" + message("RegressionFailedNotice"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void writeRegressionData() {
        regressionDataController.loadData(baseTitle,
                regression.makeColumns(), regression.makeRegressionData());
    }

    @FXML
    @Override
    public void refreshAction() {
        writeModel();
    }

    @FXML
    @Override
    public boolean menuAction() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == modelTab) {
            return modelController.menuAction();

        } else if (tab == resultsTab) {
            return regressionDataController.menuAction();

        }
        return false;
    }

    @FXML
    @Override
    public boolean popAction() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == modelTab) {
            return modelController.popAction();

        } else if (tab == resultsTab) {
            return regressionDataController.popAction();

        }
        return false;
    }


    /*
        static
     */
    public static Data2DMultipleLinearRegressionController open(BaseData2DLoadController tableController) {
        try {
            Data2DMultipleLinearRegressionController controller = (Data2DMultipleLinearRegressionController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DMultipleLinearRegressionFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
