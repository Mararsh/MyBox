package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-9-10
 * @License Apache License Version 2.0
 */
public class FunctionBinaryController extends FunctionUnaryController {

    @FXML
    protected FunctionBinaryEditor editorController;
    @FXML
    protected TextField yInput;
    @FXML
    protected ControlDataSplit yDataSplitController, yChartSplitController;

    public FunctionBinaryController() {
        baseTitle = message("BinaryFunction");
        TipsLabelKey = "BinaryFunctionTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            yDataSplitController.setParameters(baseName + "Data");
            yChartSplitController.setParameters(baseName + "Chart");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public String finalScript(String script, double x, double y) {
        try {
            if (script == null || script.isBlank()) {
                return null;
            }
            return "var x=" + x + ";\n"
                    + "var y=" + y + ";\n"
                    + script;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void calculateAction() {
        try {
            double x = DoubleTools.toDouble(xInput.getText(), Data2D_Attributes.InvalidAs.Blank);
            if (DoubleTools.invalidDouble(x)) {
                popError(message("InvalidParameter") + ": x");
                return;
            }
            String script = getScript();
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }
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

    public String calculate(String script, double x, double y) {
        try {
            return calculator.calculate(finalScript(script, x, y));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public boolean inDomain(double x, double y) {
        return inDomain(getDomain(), x, y);
    }

    public boolean inDomain(String domain, double x, double y) {
        try {
            if (domain == null || domain.isBlank()) {
                return true;
            }
            return calculator.condition(finalScript(domain, x, y));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected DataFileCSV generateData(ControlDataSplit xSplitController, ControlDataSplit ySplitController, int scale) {
        try {
            File csvFile = getPathTempFile(AppPaths.getGeneratedPath(), interfaceName, ".csv");
            long count = 0;
            List<Data2DColumn> db2Columns = new ArrayList<>();
            List<String> fileRow = new ArrayList<>();
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                names.add("x");
                names.add("y");
                names.add("z");
                csvPrinter.printRecord(names);
                db2Columns.add(new Data2DColumn("x", ColumnType.Double, true));
                db2Columns.add(new Data2DColumn("y", ColumnType.Double, true));
                db2Columns.add(new Data2DColumn("z", ColumnType.Double, true));
                double xinterval;
                if (xSplitController.byInterval) {
                    xinterval = xSplitController.interval;
                } else {
                    xinterval = (xSplitController.to - xSplitController.from) / xSplitController.number;
                }
                xinterval = DoubleTools.scale(xinterval, scale);
                double yinterval;
                if (ySplitController.byInterval) {
                    yinterval = ySplitController.interval;
                } else {
                    yinterval = (ySplitController.to - ySplitController.from) / ySplitController.number;
                }
                yinterval = DoubleTools.scale(yinterval, scale);
                String script = getScript();
                String domain = getDomain();
                for (double xd = xSplitController.from; xd <= xSplitController.to; xd += xinterval) {
                    double x = DoubleTools.scale(xd, scale);
                    for (double yd = xSplitController.from; yd <= ySplitController.to; yd += yinterval) {
                        double y = DoubleTools.scale(yd, scale);
                        if (!inDomain(domain, x, y)) {
                            continue;
                        }
                        String fxy = calculate(script, x, y);
                        if (fxy == null) {
                            continue;
                        }
                        double z = DoubleTools.scale(fxy, InvalidAs.Blank, scale);
                        count++;
                        fileRow.add(x + "");
                        fileRow.add(y + "");
                        fileRow.add(z + "");
                        csvPrinter.printRecord(fileRow);
                        fileRow.clear();
                    }
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
    @Override
    public void okDataAction() {
        if (!xDataSplitController.checkInputs() || !yDataSplitController.checkInputs()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV data;

            @Override
            protected boolean handle() {
                data = generateData(xDataSplitController, yDataSplitController, dataScale);
                return data != null;
            }

            @Override
            protected void whenSucceeded() {
                dataController.loadDef(data);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void okChartAction() {
        if (!xDataSplitController.checkInputs() || !yDataSplitController.checkInputs()) {
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
                    DataFileCSV data = generateData(xDataSplitController, yDataSplitController, chartScale);
                    if (data == null) {
                        return false;
                    }
                    data.setTask(this);
                    outputData = data.allRows(false);
                    if (outputData == null) {
                        return false;
                    }
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
