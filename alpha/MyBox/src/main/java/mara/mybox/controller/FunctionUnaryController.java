package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class FunctionUnaryController extends TreeManageController {

    protected XYChartMaker chartMaker;
    protected String outputs = "";
    protected ExpressionCalculator calculator;
    protected int dataScale, chartScale;

    @FXML
    protected FunctionUnaryEditor editorController;
    @FXML
    protected TextField xInput;
    @FXML
    protected ControlDataSplit xDataSplitController, xChartSplitController;
    @FXML
    protected ComboBox<String> dataScaleSelector, chartScaleSelector;
    @FXML
    protected ControlData2DResults dataController;
    @FXML
    protected ControlData2DChartXY chartController;
    @FXML
    protected ControlWebView outputController;

    public FunctionUnaryController() {
        baseTitle = message("UnaryFunction");
        category = TreeNode.MathFunction;
        TipsLabelKey = "UnaryFunctionTips";
        nameMsg = message("Title");
        valueMsg = message("MathFunction");
        moreMsg = message("FunctionDomain");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            calculator = new ExpressionCalculator();
            nodeController = editorController;

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            editorController.setParameters(this);

            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

            xDataSplitController.setParameters(baseName + "Data");

            xChartSplitController.setParameters(baseName + "Chart");
            chartMaker = chartController.chartMaker;
            chartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    okChartAction();
                }
            });

            dataScale = UserConfig.getInt(baseName + "DataScale", 8);
            if (dataScale < 0) {
                dataScale = 8;
            }
            dataScaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            dataScaleSelector.getSelectionModel().select(dataScale + "");

            chartScale = UserConfig.getInt(baseName + "ChartScale", 8);
            if (chartScale < 0) {
                chartScale = 8;
            }
            chartScaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            chartScaleSelector.getSelectionModel().select(chartScale + "");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void itemClicked() {
    }

    public String getScript() {
        return editorController.valueInput.getText();
    }

    public String getDomain() {
        return editorController.moreInput.getText();
    }

    public String finalScript(String script, double x) {
        try {
            if (script == null || script.isBlank()) {
                return null;
            }
            return "var x=" + x + ";\n" + script;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    public void calculateAction() {
        try {
            String script = getScript();
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }
            double x = DoubleTools.toDouble(xInput.getText(), Data2D_Attributes.InvalidAs.Blank);
            if (!inDomain(x)) {
                popError(message("NotInDomain"));
                return;
            }
            String ret = calculate(script, x);
            if (ret == null) {
                popError(message("Failed"));
                return;
            }
            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(script)
                    + "<br>x=" + x
                    + "</div>";
            outputs += "<div class=\"valueBox\">" + HtmlWriteTools.stringToHtml(ret) + "</div><br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            outputController.loadContents(html);
            TableStringValues.add("JavaScriptHistories", script.trim());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public String calculate(String script, double x) {
        try {
            return calculator.calculate(finalScript(script, x));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public boolean inDomain(double x) {
        return inDomain(getDomain(), x);
    }

    public boolean inDomain(String domain, double x) {
        try {
            if (domain == null || domain.isBlank()) {
                return true;
            }
            return calculator.condition(finalScript(domain, x));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @FXML
    public void popHtmlStyle(MouseEvent mouseEvent) {
        PopTools.popHtmlStyle(mouseEvent, outputController);
    }

    @FXML
    public void editResults() {
        outputController.editAction();
    }

    @FXML
    public void clearResults() {
        outputs = "";
        outputController.loadContents("");
    }

    protected DataFileCSV generateData(ControlDataSplit splitController, int scale) {
        try {
            File csvFile = getPathTempFile(AppPaths.getGeneratedPath(), interfaceName, ".csv");
            long count = 0;
            List<Data2DColumn> db2Columns = new ArrayList<>();
            List<String> fileRow = new ArrayList<>();
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                names.add("x");
                names.add("y");
                csvPrinter.printRecord(names);
                db2Columns.add(new Data2DColumn("x", ColumnType.Double, true));
                db2Columns.add(new Data2DColumn("y", ColumnType.Double, true));
                double interval;
                if (splitController.byInterval) {
                    interval = splitController.interval;
                } else {
                    interval = (splitController.to - splitController.from) / splitController.number;
                }
                interval = DoubleTools.scale(interval, scale);
                String script = getScript();
                String domain = getDomain();
                for (double d = splitController.from; d <= splitController.to; d += interval) {
                    double x = DoubleTools.scale(d, scale);
                    if (!inDomain(domain, x)) {
                        continue;
                    }

                    String fx = calculate(script, x);
                    if (fx == null) {
                        continue;
                    }
                    double y = DoubleTools.scale(fx, InvalidAs.Blank, scale);
                    count++;
                    fileRow.add(x + "");
                    fileRow.add(y + "");
                    csvPrinter.printRecord(fileRow);
                    fileRow.clear();
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
            DataFileCSV data = new DataFileCSV();
            data.setFile(csvFile).setDataName(interfaceName)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(2).setRowsNumber(count);
            data.setColumns(db2Columns);
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void okDataAction() {
        try {
            int v = Integer.parseInt(dataScaleSelector.getValue());
            if (v >= 0) {
                dataScale = v;
                dataScaleSelector.getEditor().setStyle(null);
                UserConfig.setInt(baseName + "DataScale", dataScale);
            } else {
                dataScaleSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("InvalidParamter") + ": " + message("DecimalScale"));
                return;
            }
        } catch (Exception e) {
            dataScaleSelector.getEditor().setStyle(UserConfig.badStyle());
            popError(message("InvalidParamter") + ": " + message("DecimalScale"));
            return;
        }
        if (!xDataSplitController.checkInputs()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV data;

            @Override
            protected boolean handle() {
                data = generateData(xDataSplitController, dataScale);
                if (data == null) {
                    return false;
                }
                data.saveAttributes();
                return true;
            }

            @Override
            protected void whenSucceeded() {
                dataController.loadDef(data);
            }

        };
        start(task);
    }

    @FXML
    public void okChartAction() {
        try {
            int v = Integer.parseInt(chartScaleSelector.getValue());
            if (v >= 0) {
                chartScale = v;
                chartScaleSelector.getEditor().setStyle(null);
                UserConfig.setInt(baseName + "ChartScale", chartScale);
            } else {
                chartScaleSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("InvalidParamter") + ": " + message("DecimalScale"));
                return;
            }
        } catch (Exception e) {
            chartScaleSelector.getEditor().setStyle(UserConfig.badStyle());
            popError(message("InvalidParamter") + ": " + message("DecimalScale"));
            return;
        }
        if (!xChartSplitController.checkInputs()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private List<List<String>> outputData;
            private List<Data2DColumn> outputColumns;

            @Override
            protected boolean handle() {
                try {
                    DataFileCSV data = generateData(xChartSplitController, chartScale);
                    if (data == null) {
                        return false;
                    }
                    data.setTask(this);
                    outputData = data.allRows(false);
                    if (outputData == null) {
                        return false;
                    }
                    FileDeleteTools.delete(data.getFile());
                    outputColumns = data.getColumns();
                    String chartName = message("LineChart");
                    UserConfig.setBoolean(chartName + "CategoryIsNumbers", true);
                    ChartType chartType = getScript().contains("Math.random()") ? ChartType.Scatter : ChartType.Line;
                    chartMaker.init(chartType, chartName)
                            .setDefaultChartTitle(getScript())
                            .setDefaultCategoryLabel("x")
                            .setDefaultValueLabel("y")
                            .setInvalidAs(InvalidAs.Skip);
                    Map<String, String> palette = new HashMap();
                    Random random = new Random();
                    for (int i = 0; i < outputColumns.size(); i++) {
                        Data2DColumn column = outputColumns.get(i);
                        String rgb = FxColorTools.color2rgb(FxColorTools.randomColor(random));
                        palette.put(column.getColumnName(), rgb);
                    }
                    chartMaker.setPalette(palette);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                chartController.writeXYChart(outputColumns, outputData, null, false);
            }

        };
        start(task);
    }

}
