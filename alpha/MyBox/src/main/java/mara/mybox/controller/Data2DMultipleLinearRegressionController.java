package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.calculation.OLSLinearRegression;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DMultipleLinearRegressionController extends BaseData2DChartController {

    protected OLSLinearRegression regression;
    protected String yName;
    protected int yCol;
    protected List<String> xNames;
    protected double alpha;

    @FXML
    protected CheckBox interceptCheck;
    @FXML
    protected ComboBox<String> alphaSelector;
    @FXML
    protected ControlWebView modelController;
    @FXML
    protected ControlData2DResults regressionDataController;

    public Data2DMultipleLinearRegressionController() {
        baseTitle = message("MultipleLinearRegression");
        TipsLabelKey = "MultipleLinearRegressionTips";
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

            modelController.setParent(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterRefreshControls() {
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            dataColsIndices = new ArrayList<>();
            yName = colSelector.getSelectionModel().getSelectedItem();
            yCol = data2D.colOrder(yName);
            if (yCol < 0) {
                outError(message("SelectToHandle") + ": " + message("DependentVariable"));
                return false;
            }
            dataColsIndices.add(yCol);

            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                outError(message("SelectToHandle") + ": " + message("IndependentVariable"));
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
                outError(message("SelectToHandle") + ": " + message("IndependentVariable"));
                return false;
            }
            regression = null;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        modelController.loadContents("");
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    List<List<String>> data;
                    if (isAllPages()) {
                        data = data2D.allRows(dataColsIndices, false);
                    } else {
                        data = filtered(dataColsIndices, false);
                    }
                    data2D.stopFilter();
                    if (data == null || data.isEmpty()) {
                        error = message("NoData");
                        return false;
                    }
                    regression = new OLSLinearRegression(interceptCheck.isSelected())
                            .setTask(task).setScale(scale).setInvalidAs(0)
                            .setyName(yName).setxNames(xNames);
                    return regression.calculate(data);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                writeModel();
                writeRegressionData();
            }

            @Override
            protected void whenFailed() {
                if (isCancelled()) {
                    return;
                }
                alertError((error != null ? error + "\n\n" : "") + message("RegressionFailedNotice"));
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                regression = null;
            }

        };
        start(task);
    }

    protected void writeModel() {
        try {
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");
            double[] coefficients = regression.scaledCoefficients();
            double intercept = regression.scaledIntercept();
            String scriptModel = "y = " + intercept;
            String model = yName + " = " + intercept;
            for (int i = 0; i < coefficients.length; i++) {
                double d = coefficients[i];
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
            row.add(regression.getN() + "");
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
            row.add(regression.calculateRSquared() + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("AdjustedRSquared"));
            row.add(regression.calculateAdjustedRSquared() + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("StandardError"));
            row.add(regression.estimateRegressionStandardError() + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("Variance"));
            row.add(regression.estimateRegressandVariance() + "");
            table.add(row);

            s.append(table.div());
            s.append("</BODY>\n");
            modelController.loadContents(HtmlWriteTools.html(s.toString()));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void writeRegressionData() {
        regressionDataController.loadTmpData(null, regression.makeColumns(), regression.makeRegressionData());
    }

    @FXML
    public void about() {
        try {
            StringTable table = new StringTable(null, message("AboutLinearRegression"));
            table.newLinkRow(message("Guide"), "https://www.itl.nist.gov/div898/handbook/");
            table.newLinkRow("", "https://book.douban.com/subject/10956491/");
            table.newLinkRow(message("Video"), "https://www.bilibili.com/video/BV1Ua4y1e7YG");
            table.newLinkRow("", "https://www.bilibili.com/video/BV1i7411d7aP");
            table.newLinkRow(message("Example"), "https://www.xycoon.com/simple_linear_regression.htm");
            table.newLinkRow("", "https://www.scribbr.com/statistics/simple-linear-regression/");
            table.newLinkRow("", "http://www.datasetsanalysis.com/regressions/simple-linear-regression.html");
            table.newLinkRow(message("Dataset"), "http://archive.ics.uci.edu/ml/datasets/Iris");
            table.newLinkRow("", "https://github.com/tomsharp/SVR/tree/master/data");
            table.newLinkRow("", "https://github.com/krishnaik06/simple-Linear-Regression");
            table.newLinkRow("", "https://github.com/susanli2016/Machine-Learning-with-Python/tree/master/data");
            table.newLinkRow("Apache-Math", "https://commons.apache.org/proper/commons-math/");
            table.newLinkRow("", "https://commons.apache.org/proper/commons-math/apidocs/index.html");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            openLink(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static Data2DMultipleLinearRegressionController open(ControlData2DEditTable tableController) {
        try {
            Data2DMultipleLinearRegressionController controller = (Data2DMultipleLinearRegressionController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DMultipleLinearRegressionFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
