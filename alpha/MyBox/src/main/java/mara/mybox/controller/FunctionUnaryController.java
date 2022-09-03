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
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
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
public class FunctionUnaryController extends JavaScriptController {

    protected FindReplaceString findReplace;
    protected XYChartMaker chartMaker;

    @FXML
    protected FunctionUnaryEditor editorController;
    @FXML
    protected TextField xInput, fromInput, toInput, numberInput;
    @FXML
    protected ControlData2DChartXY chartController;

    public FunctionUnaryController() {
        baseTitle = message("UnaryFunction");
        category = TreeNode.JavaScript;
        TipsLabelKey = "UnaryFunctionTips";
        nameMsg = message("Name");
        valueMsg = message("UnaryFunction");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            editorController.setParameters(this);

            findReplace = FindReplaceString.create().setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setIsRegex(false).setCaseInsensitive(false).setMultiline(false);

            htmlWebView = outputController;

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

    public String getFunction() {
        String script = editorController.getScript();
        if (script == null || script.isBlank()) {
            popError(message("InvalidParameters") + ": JavaScript");
            return null;
        }
        return findReplace.replaceStringAll(script, "#{x}", "x");
    }

    @FXML
    public void calculateAction() {
        try {
            double x = DoubleTools.toDouble(xInput.getText(), Data2D_Attributes.InvalidAs.Blank);
            if (DoubleTools.invalidDouble(x)) {
                popError(message("InvalidParameter") + ": x");
                return;
            }
            String script = editorController.getScript();
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
            String filledScript = findReplace.replaceStringAll(script, "#{x}", x + "");
            String ret;
            try {
                Object o = outputController.webEngine.executeScript(filledScript);
                if (o != null) {
                    ret = o.toString();
                } else {
                    ret = "";
                }
            } catch (Exception e) {
                ret = e.toString();
            }
            return ret;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    public void chartAction() {
        try {
            String script = editorController.getScript();
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }
            double from = Double.NaN;
            try {
                from = Double.valueOf(fromInput.getText());
            } catch (Exception e) {
            }
            if (DoubleTools.invalidDouble(from)) {
                popError(message("InvalidParameter") + ": " + message("From"));
                return;
            }
            double to = Double.NaN;
            try {
                to = Double.valueOf(toInput.getText());
            } catch (Exception e) {
            }
            if (DoubleTools.invalidDouble(to) || to < from) {
                popError(message("InvalidParameter") + ": " + message("To"));
                return;
            }
            int n = 0;
            try {
                n = Integer.valueOf(numberInput.getText());
            } catch (Exception e) {
            }
            if (n < 1) {
                popError(message("InvalidParameter") + ": " + message("NumberOfData"));
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
            double interval = (to - from) / n;
            double x = from;
            for (int i = 0; i < n; i++) {
                x = from + i * interval;
                String y = calculate(script, x);
                if (y == null) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                row.add(x + "");
                row.add(y);
                outputData.add(row);
            }
            if (x != to) {
                String y = calculate(script, to);
                if (y != null) {
                    List<String> row = new ArrayList<>();
                    row.add(to + "");
                    row.add(y);
                    outputData.add(row);
                }
            }

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
