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
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DMultipleLinearRegressionController extends BaseData2DHandleController {

    protected OLSMultipleLinearRegression olsRegression;
    protected String yName;
    protected int yCol;
    protected List<Integer> dataColsIndices;
    protected double alpha, intercept, rSquared, sigma, regressandVariance;
    protected double[] beta, residuals;
    protected double[][] parametersVariance;

    @FXML
    protected CheckBox interceptCheck;
    @FXML
    protected ComboBox<String> alphaSelector;
    @FXML
    protected ControlWebView resultsController;

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

            resultsController.setParent(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
                popError(message("SelectToHandle") + ": " + message("DependentVariable"));
                return false;
            }
            dataColsIndices.add(yCol);

            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("IndependentVariable"));
                return false;
            }
            for (int col : checkedColsIndices) {
                if (!dataColsIndices.contains(col)) {
                    dataColsIndices.add(col);
                }
            }
            olsRegression = null;
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
        resultsController.loadContents("");
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    List<List<String>> data = data2D.allRows(checkedColsIndices, false);
                    data2D.stopFilter();

                    olsRegression = new OLSMultipleLinearRegression();
                    olsRegression.setNoIntercept(!interceptCheck.isSelected());
                    int n = data.size();
                    int k = checkedColsIndices.size();
//                    double[] y = new double[n];
//                    double[][] x = new double[n][k];
//                    for (int i = 0; i < n; i++) {
//                        List<String> row = data.get(i);
//                        y[i] = DoubleTools.toDouble(row.get(0), invalidAs);
//                        for (int j = 1; j < row.size(); j++) {
//                            x[i][j - 1] = DoubleTools.toDouble(row.get(j), invalidAs);
//                        }
//                    }
                    double[] y = new double[]{11.0, 12.0, 13.0, 14.0, 15.0, 16.0};
                    double[][] x = new double[6][];
                    x[0] = new double[]{0, 0, 0, 0, 0};
                    x[1] = new double[]{2.0, 0, 0, 0, 0};
                    x[2] = new double[]{0, 3.0, 0, 0, 0};
                    x[3] = new double[]{0, 0, 4.0, 0, 0};
                    x[4] = new double[]{0, 0, 0, 5.0, 0};
                    x[5] = new double[]{0, 0, 0, 0, 6.0};
                    olsRegression.newSampleData(y, x);
                    beta = olsRegression.estimateRegressionParameters();
                    residuals = olsRegression.estimateResiduals();
                    parametersVariance = olsRegression.estimateRegressionParametersVariance();
                    regressandVariance = olsRegression.estimateRegressandVariance();
                    rSquared = olsRegression.calculateRSquared();
                    sigma = olsRegression.estimateRegressionStandardError();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                printResults();
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

    protected void printResults() {
        try {
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");

            s.append("<H3 align=center>").append(message("LastStatus")).append("</H3>\n");
            List<String> names = new ArrayList<>();
            names.add(message("Name"));
            names.add(message("Value"));
            StringTable table = new StringTable(names);

            List<String> row = new ArrayList<>();
            row.add(message("IndependentVariable"));
            row.add(checkedColsNames.toString());
            table.add(row);

            row = new ArrayList<>();
            row.add(message("DependentVariable"));
            row.add(yName);
            table.add(row);

            row = new ArrayList<>();
            row.add("beta");
            row.add(Arrays.toString(beta));
            table.add(row);

            row = new ArrayList<>();
            row.add("residuals");
            row.add(Arrays.toString(residuals));
            table.add(row);

            row = new ArrayList<>();
            row.add(message("CoefficientOfDetermination"));
            row.add(rSquared + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("sigma"));
            row.add(sigma + "");
            table.add(row);

            row = new ArrayList<>();
            row.add(message("regressandVariance"));
            row.add(regressandVariance + "");
            table.add(row);

            s.append(table.div());
            s.append("</BODY>\n");
            resultsController.loadContents(HtmlWriteTools.html(s.toString()));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
