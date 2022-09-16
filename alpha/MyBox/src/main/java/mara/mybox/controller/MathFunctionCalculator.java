package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class MathFunctionCalculator extends BaseController {

    protected String script, domain, resultName, outputs = "";
    protected ExpressionCalculator calculator;
    protected int calculateScale, dataScale, chartScale;
    protected List<String> variables;
    protected List<Double> variableValues;
    protected List<TextField> inputs;
    protected List<ControlDataSplit> xDataSplits, xChartSplits;

    @FXML
    protected Tab definitionTab, calculateTab, dataTab, chartTab;
    @FXML
    protected TabPane dataTabPane, chartTabPane;
    @FXML
    protected TextField titleInput, variablesInput, resultNameInput;
    @FXML
    protected TextArea scriptArea, domainArea;
    @FXML
    protected FlowPane inputsPane;
    @FXML
    protected ComboBox<String> calculateScaleSelector, dataScaleSelector, chartScaleSelector;
    @FXML
    protected ControlData2DResults dataController;
    @FXML
    protected ControlData2DChartXY chartController;
    @FXML
    protected ControlWebView outputController;
    @FXML
    protected RadioButton scatterChartRadio;

    public MathFunctionCalculator() {
        baseTitle = message("MathFunction");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            calculator = new ExpressionCalculator();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

            calculateScale = UserConfig.getInt(baseName + "CalculateScale", 8);
            if (calculateScale < 0) {
                calculateScale = 8;
            }
            calculateScaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            calculateScaleSelector.getSelectionModel().select(calculateScale + "");

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
            if (chartScaleSelector != null) {
                chartScaleSelector.getItems().addAll(
                        Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
                );
                chartScaleSelector.getSelectionModel().select(chartScale + "");
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(ControlMathFunctionEditor editorController) {
        try {

            String title = editorController.nameInput.getText();
            titleInput.setText(title);
            if (title != null && !title.isBlank()) {
                setTitle(baseTitle + " - " + title);
            }
            script = editorController.valueInput.getText();
            domain = editorController.moreInput.getText();
            resultName = editorController.resultName();
            scriptArea.setText(script);
            domainArea.setText(domain);
            resultNameInput.setText(resultName);

            tabPane.getSelectionModel().select(calculateTab);
            variables = editorController.variableNames();
            if (variables == null || variables.isEmpty()) {
                variablesInput.clear();
                tabPane.getTabs().remove(dataTab);
                calculateAction();
            } else {
                String finalNames = variables.get(0).trim();
                for (int i = 1; i < variables.size(); i++) {
                    finalNames += ", " + variables.get(i).trim();
                }
                variablesInput.setText(finalNames);

                for (String variable : variables) {
                    TextField input = new TextField();
                    input.setPrefWidth(80);
                    inputsPane.getChildren().addAll(new Label(variable), input);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public String finalScript(String script) {
        try {
            if (script == null || script.isBlank() || variables == null) {
                return script;
            }
            String vars = "";
            List<Node> nodes = inputsPane.getChildren();
            for (int i = 0; i < nodes.size(); i += 2) {
                Label label = (Label) nodes.get(i);
                TextField input = (TextField) nodes.get(i + 1);
                double d = DoubleTools.toDouble(input.getText(), Data2D_Attributes.InvalidAs.Blank);
                vars += "var " + label.getText() + "=" + d + ";\n";
            }
            return vars + script;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public String calculate(String script) {
        return calculator.calculate(script);
    }

    public boolean inDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return true;
        }
        return calculator.condition(finalScript(domain));
    }

    public String scriptResult(String script) {
        String finalScript = finalScript(script);
        return calculate(finalScript);
    }

    public boolean checkScript() {
        String script = scriptArea.getText();
        if (script == null || script.isBlank()) {
            popError(message("InvalidParameters") + ": JavaScript");
            return false;
        }
        return true;
    }

    public boolean checkVariables() {
        try {
            if (variables == null || variables.isEmpty()) {
                return true;
            }
            for (Node node : inputsPane.getChildren()) {
                if (!(node instanceof TextField)) {
                    continue;
                }
                TextField input = (TextField) node;
                double d = DoubleTools.toDouble(input.getText(), Data2D_Attributes.InvalidAs.Blank);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public int checkScale(ComboBox<String> selector) {
        try {
            int v = Integer.parseInt(selector.getValue());
            if (v >= 0) {
                selector.getEditor().setStyle(null);
                return v;
            } else {
                selector.getEditor().setStyle(UserConfig.badStyle());
                return -1;
            }
        } catch (Exception e) {
            selector.getEditor().setStyle(UserConfig.badStyle());
            return -2;
        }
    }

    @FXML
    public void recoverScript() {
        scriptArea.setText(script);
    }

    @FXML
    public void recoverDomain() {
        domainArea.setText(domain);
    }

    @FXML
    public void recoverResultName() {
        resultNameInput.setText(resultName);
    }

    /*
        calculate
     */
    @FXML
    public void calculateAction() {
        try {
            int v = checkScale(calculateScaleSelector);
            if (v >= 0) {
                calculateScale = v;
                UserConfig.setInt(baseName + "CalculateScale", v);
            } else {
                popError(message("InvalidParamter") + ": " + message("DecimalScale"));
                return;
            }
            if (!checkScript() || !checkVariables()) {
                return;
            }
            String currentDomain = domainArea.getText();
            if (!inDomain(currentDomain)) {
                popError(message("NotInDomain"));
                return;
            }
            if (task != null) {
                task.cancel();
            }
            String currentScript = scriptArea.getText();
            String finalScript = finalScript(currentScript);
            String ret = calculate(finalScript);
            if (ret == null) {
                popError(message("Failed"));
                return;
            }
            double d = DoubleTools.scale(ret, InvalidAs.Blank, calculateScale);
            ret = DoubleTools.invalidDouble(d) ? ret : (d + "");
            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(finalScript)
                    + "</div>";
            outputs += "<div class=\"valueBox\">"
                    + HtmlWriteTools.stringToHtml(resultNameInput.getText() + "=" + ret)
                    + "</div><br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            outputController.loadContents(html);
            TableStringValues.add("FunctionScriptHistories", currentScript.trim());
            if (currentDomain != null && !currentDomain.isBlank()) {
                TableStringValues.add("FunctionDomainHistories", currentDomain);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    /*
        data set
     */
    protected DataFileCSV generateData(TabPane tabPane, int scale) {
        try {
            if (tabPane == null || tabPane.getTabs().size() == 0) {
                return null;
            }
            int size = tabPane.getTabs().size();
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
//                if (splitController.byInterval) {
//                    interval = splitController.interval;
//                } else {
//                    interval = (splitController.to - splitController.from) / splitController.number;
//                }
//                interval = DoubleTools.scale(interval, scale);
//                String script = getScript();
//                String domain = getDomain();
//                for (double d = splitController.from; d <= splitController.to; d += interval) {
//                    double x = DoubleTools.scale(d, scale);
//                    if (!inDomain(domain, x)) {
//                        continue;
//                    }
//
//                    String fx = calculate(script, x);
//                    if (fx == null) {
//                        continue;
//                    }
//                    double y = DoubleTools.scale(fx, InvalidAs.Blank, scale);
//                    count++;
//                    fileRow.add(x + "");
//                    fileRow.add(y + "");
//                    csvPrinter.printRecord(fileRow);
//                    fileRow.clear();
//                }
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
        int v = checkScale(dataScaleSelector);
        if (v >= 0) {
            dataScale = v;
            UserConfig.setInt(baseName + "DataScale", v);
        } else {
            popError(message("InvalidParamter") + ": " + message("DecimalScale"));
            return;
        }
        if (!checkVariables()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV data;

            @Override
            protected boolean handle() {
//                data = generateData(dataScale);
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
//        if (!xChartSplitController.checkInputs()) {
//            return;
//        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private List<List<String>> outputData;
            private List<Data2DColumn> outputColumns;

            @Override
            protected boolean handle() {
                try {
//                    DataFileCSV data = generateData(xChartSplitController, chartScale);
//                    if (data == null) {
//                        return false;
//                    }
//                    data.setTask(this);
//                    outputData = data.allRows(false);
//                    if (outputData == null) {
//                        return false;
//                    }
//                    FileDeleteTools.delete(data.getFile());
//                    outputColumns = data.getColumns();
//                    String chartName = message("LineChart");
//                    UserConfig.setBoolean(chartName + "CategoryIsNumbers", true);
//                    ChartType chartType = scatterChartRadio.isSelected() ? ChartType.Scatter : ChartType.Line;
//                    LabelType labelType = scatterChartRadio.isSelected() ? LabelType.Point : LabelType.NotDisplay;
//                    String title = editorController.nameInput.getText();
//                    if (title == null || title.isBlank()) {
//                        title = getScript();
//                    }
//                    chartMaker.init(chartType, chartName)
//                            .setLabelType(labelType)
//                            .setDefaultChartTitle(title)
//                            .setDefaultCategoryLabel("x")
//                            .setDefaultValueLabel("y")
//                            .setInvalidAs(InvalidAs.Skip);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
//                chartController.writeXYChart(outputColumns, outputData, null, false);
            }

        };
        start(task);
    }

//    protected DataFileCSV generateData(ControlDataSplit xSplitController, ControlDataSplit ySplitController, int scale) {
//        try {
//            File csvFile = getPathTempFile(AppPaths.getGeneratedPath(), interfaceName, ".csv");
//            long count = 0;
//            List<Data2DColumn> db2Columns = new ArrayList<>();
//            List<String> fileRow = new ArrayList<>();
//            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
//                List<String> names = new ArrayList<>();
//                names.add("x");
//                names.add("y");
//                names.add("z");
//                csvPrinter.printRecord(names);
//                db2Columns.add(new Data2DColumn("x", ColumnType.Double, true));
//                db2Columns.add(new Data2DColumn("y", ColumnType.Double, true));
//                db2Columns.add(new Data2DColumn("z", ColumnType.Double, true));
//                double xinterval;
//                if (xSplitController.byInterval) {
//                    xinterval = xSplitController.interval;
//                } else {
//                    xinterval = (xSplitController.to - xSplitController.from) / xSplitController.number;
//                }
//                xinterval = DoubleTools.scale(xinterval, scale);
//                double yinterval;
//                if (ySplitController.byInterval) {
//                    yinterval = ySplitController.interval;
//                } else {
//                    yinterval = (ySplitController.to - ySplitController.from) / ySplitController.number;
//                }
//                yinterval = DoubleTools.scale(yinterval, scale);
//                String script = getScript();
//                String domain = getDomain();
//                for (double xd = xSplitController.from; xd <= xSplitController.to; xd += xinterval) {
//                    double x = DoubleTools.scale(xd, scale);
//                    for (double yd = xSplitController.from; yd <= ySplitController.to; yd += yinterval) {
//                        double y = DoubleTools.scale(yd, scale);
//                        if (!inDomain(domain, x, y)) {
//                            continue;
//                        }
//                        String fxy = calculate(script, x, y);
//                        if (fxy == null) {
//                            continue;
//                        }
//                        double z = DoubleTools.scale(fxy, Data2D_Attributes.InvalidAs.Blank, scale);
//                        count++;
//                        fileRow.add(x + "");
//                        fileRow.add(y + "");
//                        fileRow.add(z + "");
//                        csvPrinter.printRecord(fileRow);
//                        fileRow.clear();
//                    }
//                }
//            } catch (Exception e) {
//                if (task != null) {
//                    task.setError(e.toString());
//                }
//                MyBoxLog.error(e);
//                return null;
//            }
//            DataFileCSV data = new DataFileCSV();
//            data.setFile(csvFile).setDataName(interfaceName)
//                    .setCharset(Charset.forName("UTF-8"))
//                    .setDelimiter(",").setHasHeader(true)
//                    .setColsNumber(2).setRowsNumber(count);
//            data.setColumns(db2Columns);
//            return data;
//        } catch (Exception e) {
//            if (task != null) {
//                task.setError(e.toString());
//            }
//            MyBoxLog.error(e);
//            return null;
//        }
//    }
//    @FXML
//    @Override
//    public void okChartAction() {
//        if (!xDataSplitController.checkInputs() || !yDataSplitController.checkInputs()) {
//            return;
//        }
//        if (task != null) {
//            task.cancel();
//        }
//        task = new SingletonTask<Void>(this) {
//
//            private List<List<String>> outputData;
//            private List<Data2DColumn> outputColumns;
//
//            @Override
//            protected boolean handle() {
//                try {
//                    DataFileCSV data = generateData(xDataSplitController, yDataSplitController, chartScale);
//                    if (data == null) {
//                        return false;
//                    }
//                    data.setTask(this);
//                    outputData = data.allRows(false);
//                    if (outputData == null) {
//                        return false;
//                    }
////                    outputColumns = data.getColumns();
////                    String chartName = message("LineChart");
////                    UserConfig.setBoolean(chartName + "CategoryIsNumbers", true);
////                    ChartType chartType = getScript().contains("Math.random()") ? ChartType.Scatter : ChartType.Line;
////                    chartMaker.init(chartType, chartName)
////                            .setDefaultChartTitle(getScript())
////                            .setDefaultCategoryLabel("x")
////                            .setDefaultValueLabel("y")
////                            .setInvalidAs(InvalidAs.Skip);
////                    Map<String, String> palette = new HashMap();
////                    Random random = new Random();
////                    for (int i = 0; i < outputColumns.size(); i++) {
////                        Data2DColumn column = outputColumns.get(i);
////                        String rgb = FxColorTools.color2rgb(FxColorTools.randomColor(random));
////                        palette.put(column.getColumnName(), rgb);
////                    }
////                    chartMaker.setPalette(palette);
//                    return true;
//                } catch (Exception e) {
//                    error = e.toString();
//                    return false;
//                }
//            }
//
//            @Override
//            protected void whenSucceeded() {
////                chartController.writeXYChart(outputColumns, outputData, null, false);
//            }
//
//        };
//        start(task);
//    }

    /*
        static
     */
    public static MathFunctionCalculator open(ControlMathFunctionEditor editorController) {
        try {
            MathFunctionCalculator controller = (MathFunctionCalculator) WindowTools.openChildStage(
                    editorController.getMyWindow(), Fxmls.MathFunctionCalculatorFxml, false);
            controller.setParameters(editorController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }
}
