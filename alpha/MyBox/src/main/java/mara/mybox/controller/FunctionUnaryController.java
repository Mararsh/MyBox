package mara.mybox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.chart.ChartOptions;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class FunctionUnaryController extends BaseJavaScriptReferController {

    protected XYChartMaker chartMaker;
    protected String outputs = "";
    protected ExpressionCalculator calculator;

    @FXML
    protected TextField xInput;
    @FXML
    protected ControlDataSplit dataSplitController, chartSplitController;
    @FXML
    protected ControlData2DResults dataController;
    @FXML
    protected ControlData2DChartXY chartController;
    @FXML
    protected ControlWebView outputController;

    public FunctionUnaryController() {
        baseTitle = message("UnaryFunction");
        category = TreeNode.JavaScript;
        TipsLabelKey = "UnaryFunctionTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            calculator = new ExpressionCalculator();

            placeholdersList.getItems().add("#{x}");

            dataSplitController.setParameters(baseName + "Data");
            chartSplitController.setParameters(baseName + "Chart");

            chartMaker = chartController.chartMaker;
            chartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    chartAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void okDataAction() {

    }

    @FXML
    public void popContextExamples() {

    }

    @FXML
    public void showContextExamples() {

    }

    @FXML
    public void showDomainHistories() {

    }

    @FXML
    public void popContextHistories() {

    }

    @FXML
    public void clearDomain() {

    }

    @FXML
    public void okChartAction() {

    }

    public String getFunction() {
        String script = scriptInput.getText();
        if (script == null || script.isBlank()) {
            popError(message("InvalidParameters") + ": JavaScript");
            return null;
        }
        return calculator.replaceStringAll(script, "#{x}", "x");
    }

    @FXML
    public void calculateAction() {
        try {
            double x = DoubleTools.toDouble(xInput.getText(), Data2D_Attributes.InvalidAs.Blank);
            if (DoubleTools.invalidDouble(x)) {
                popError(message("InvalidParameter") + ": x");
                return;
            }
            String script = scriptInput.getText();
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }
            String ret = calculate(script, x);
            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(getFunction())
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
            String filledScript = calculator.replaceStringAll(script, "#{x}", x + "");
            return calculator.calculate(filledScript);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
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

    @FXML
    public void chartAction() {
        try {
            String script = scriptInput.getText();
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }

            List<Data2DColumn> outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn("x", ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn("y", ColumnDefinition.ColumnType.Double));

            String chartName = message("LineChart");
            UserConfig.setBoolean(chartName + "CategoryIsNumbers", true);
            chartMaker.init(ChartOptions.ChartType.Line, chartName)
                    .setDefaultChartTitle(getFunction())
                    .setDefaultCategoryLabel("x")
                    .setDefaultValueLabel("y")
                    .setInvalidAs(InvalidAs.Skip);

            List<List<String>> outputData = new ArrayList<>();
//            double interval = (to - from) / n;
//            double x = from;
//            for (int i = 0; i < n; i++) {
//                x = from + i * interval;
//                String y = calculate(script, x);
//                if (y == null) {
//                    continue;
//                }
//                List<String> row = new ArrayList<>();
//                row.add(x + "");
//                row.add(y);
//                outputData.add(row);
//            }
//            if (x != to) {
//                String y = calculate(script, to);
//                if (y != null) {
//                    List<String> row = new ArrayList<>();
//                    row.add(to + "");
//                    row.add(y);
//                    outputData.add(row);
//                }
//            }

            Map<String, String> palette = new HashMap();
            Random random = new Random();
            for (int i = 0; i < outputColumns.size(); i++) {
                Data2DColumn column = outputColumns.get(i);
                String rgb = FxColorTools.color2rgb(FxColorTools.randomColor(random));
                palette.put(column.getColumnName(), rgb);
            }
            chartMaker.setPalette(palette);
            chartController.writeXYChart(outputColumns, outputData, null, false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
