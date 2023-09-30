package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.tools.FileTmpTools.generateFile;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class ControlMathFunctionCalculator extends BaseController {

    protected MathFunctionEditor editorController;
    protected String expression, domain, outputs = "";
    protected ExpressionCalculator calculator;
    protected int calculateScale, dataScale, variablesSize;
    protected List<String> variables;
    protected List<ControlDataSplit> splits;
    protected CSVPrinter csvPrinter;
    protected long count;
    protected List<Object> row;

    @FXML
    protected Tab calculateTab, dataTab;
    @FXML
    protected TabPane dataTabPane;
    @FXML
    protected FlowPane inputsPane;
    @FXML
    protected ComboBox<String> calculateScaleSelector, dataScaleSelector;
    @FXML
    protected ControlWebView outputController;
    @FXML
    protected VBox xyzChartBox;
    @FXML
    protected Button calculateButton, dataButton, xyChartButton;
    @FXML
    protected ControlChartXYZ xyzController;

    public ControlMathFunctionCalculator() {
        baseTitle = message("MathFunction");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            calculator = new ExpressionCalculator();
            splits = new ArrayList<>();

        } catch (Exception e) {
            MyBoxLog.debug(e);
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

            xyzController.colorGradientRadio.setSelected(true);
            xyzController.colorColumnsRadio.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(MathFunctionEditor editorController) {
        try {
            this.editorController = editorController;
            calculateButton.disableProperty().bind(editorController.valueInput.textProperty().isEmpty());
            dataButton.disableProperty().bind(editorController.valueInput.textProperty().isEmpty());
            xyChartButton.setVisible(false);
            xyzChartBox.setVisible(false);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public void variablesChanged() {
        try {
            inputsPane.getChildren().clear();
            dataTabPane.getTabs().clear();
            splits.clear();
            xyChartButton.setVisible(false);
            xyzChartBox.setVisible(false);
            variables = editorController.variableNames();
            if (variables == null || variables.isEmpty()) {
                dataTab.setDisable(true);
            } else {
                dataTab.setDisable(false);
                for (String variable : variables) {
                    TextField input = new TextField();
                    input.setPrefWidth(80);
                    inputsPane.getChildren().addAll(new Label(variable), input);
                    Tab tab = new Tab(variable);
                    tab.setClosable(false);
                    dataTabPane.getTabs().add(tab);
                    FXMLLoader fxmlLoader = new FXMLLoader(WindowTools.class.getResource(
                            Fxmls.ControlDataSplitFxml), AppVariables.currentBundle);
                    Pane pane = fxmlLoader.load();
                    tab.setContent(pane);
                    refreshStyle(pane);

                    ControlDataSplit controller = (ControlDataSplit) fxmlLoader.getController();
                    controller.name = variable;
                    splits.add(controller);
                }
                int size = variables.size();
                if (size == 1) {
                    xyChartButton.setVisible(true);
                } else if (size == 2) {
                    xyzChartBox.setVisible(true);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public String script() {
        return editorController.valueInput.getText();
    }

    public String domain() {
        String d = editorController.moreInput.getText();
        if (d == null || d.isBlank()) {
            return null;
        }
        return StringTools.replaceLineBreak(d);
    }

    public String functionName() {
        String name = editorController.functionNameInput.getText();
        return name == null || name.isBlank() ? "f" : name;
    }

    public String titleName() {
        String name = editorController.attributesController.nameInput.getText();
        return name == null || name.isBlank() ? message("MathFunction") : name;
    }

    public String eval(String script) {
        return calculator.calculate(script);
    }

    public boolean inDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return true;
        }
        return calculator.condition(domain);
    }

    public String fillDummy(String script) {
        try {
            if (script == null || script.isBlank() || variables == null) {
                return script;
            }
            String vars = "";
            for (int i = 0; i < variables.size(); i++) {
                vars += "var " + variables.get(i) + "=" + 1 + ";\n";
            }
            return vars + script;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean checkScripts() {
        String script = script();
        if (script == null || script.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Expression"));
            return false;
        }
        script = script.trim();
        String ret = eval(fillDummy(script));
        if (ret == null) {
            if (calculator.getError() != null) {
                popError(calculator.getError());
            } else {
                popError(message("InvalidParameters") + ": " + message("Expression"));
            }
            return false;
        }
        String domain = domain();
        if (domain == null) {
            return true;
        }
        ret = eval(fillDummy(domain));
        if (ret == null) {
            if (calculator.getError() != null) {
                popError(calculator.getError());
            } else {
                popError(message("InvalidParameters") + ": " + message("FunctionDomain"));
            }
            return false;
        }
        return true;
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

    public boolean checkSplits() {
        try {
            long num = 1;
            for (ControlDataSplit split : splits) {
                if (!split.checkInputs()) {
                    return false;
                }
                num *= Math.ceil((split.to - split.from) / split.interval()) + 1;
            }
            return num <= 5000 || PopTools.askSure(null,
                    message("SureContinueGenerateLotsData") + "\n"
                    + message("DataSize") + " ~= " + num);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        calculate
     */
    @FXML
    public void calculateAction() {
        try {
            if (!checkScripts()) {
                return;
            }
            int v = checkScale(calculateScaleSelector);
            if (v >= 0) {
                calculateScale = v;
                UserConfig.setInt(baseName + "CalculateScale", v);
            } else {
                popError(message("InvalidParamter") + ": " + message("DecimalScale"));
                return;
            }
            expression = script();
            domain = domain();
            if (!inDomain(fillInputs(domain))) {
                popError(message("NotInDomain"));
                return;
            }
            String finalScript = fillInputs(expression);
            String ret = eval(finalScript);
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
                    + HtmlWriteTools.stringToHtml(functionName() + "=" + ret)
                    + "</div><br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            outputController.loadContents(html);
            TableStringValues.add("FunctionScriptHistories", expression);
            if (domain != null && !domain.isBlank()) {
                TableStringValues.add("FunctionDomainHistories", domain);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String fillInputs(String script) {
        try {
            if (script == null || script.isBlank() || variables == null) {
                return script;
            }
            String vars = "";
            List<Node> nodes = inputsPane.getChildren();
            for (int i = 0; i < nodes.size(); i += 2) {
                Label label = (Label) nodes.get(i);
                TextField input = (TextField) nodes.get(i + 1);
                double d = DoubleTools.toDouble(input.getText(), InvalidAs.Blank);
                vars += "var " + label.getText() + "=" + d + ";\n";
            }
            return vars + script;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    protected void showHtmlStyle(Event event) {
        PopTools.popHtmlStyle(event, outputController);
    }

    @FXML
    protected void popHtmlStyle(Event event) {
        if (UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false)) {
            showHtmlStyle(event);
        }
    }

    @FXML
    public void editResults() {
        outputController.editAction();
    }

    @FXML
    public void clearResults() {
        outputs = "";
        outputController.clear();
    }

    /*
        data set
     */
    protected DataFileCSV generateData() {
        try {
            if (variables == null || variables.isEmpty()) {
                return null;
            }
            variablesSize = variables.size();
            count = 0;
            File csvFile = generateFile(titleName(), "csv");
            List<Data2DColumn> db2Columns = new ArrayList<>();
            try (CSVPrinter printer = CsvTools.csvPrinter(csvFile)) {
                csvPrinter = printer;
                String resultName = functionName();
                row = new ArrayList<>();
                row.addAll(variables);
                row.add(resultName);
                csvPrinter.printRecord(row);
                for (Object name : row) {
                    db2Columns.add(new Data2DColumn((String) name, ColumnType.Double, true));
                }
                List<Object> values = new ArrayList<>();
                makeRow(values);
                csvPrinter.flush();
                csvPrinter.close();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                csvPrinter = null;
                return null;
            }
            if (task == null || task.isCancelled()) {
                return null;
            }
            DataFileCSV data = new DataFileCSV();
            data.setColumns(db2Columns)
                    .setFile(csvFile).setDataName(interfaceName)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(2).setRowsNumber(count);
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public void makeRow(List<Object> values) {
        try {
            if (task == null || task.isCancelled()) {
                return;
            }
            int index = values.size();
            if (index >= variables.size()) {
                calculateRow(values);
                return;
            }
            ControlDataSplit split = splits.get(index);
            double interval = split.interval();
            for (double d = split.from; d <= split.to; d += interval) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                values.add(d);
                makeRow(values);
                values.remove(index);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);

        }
    }

    public void calculateRow(List<Object> values) {
        try {
            if (!inDomain(fillValues(domain, values))) {
                return;
            }
            String finalScript = fillValues(expression, values);
            String fx = eval(finalScript);
            if (fx == null) {
                return;
            }
            double d = DoubleTools.scale(fx, InvalidAs.Blank, dataScale);
            row.clear();
            row.addAll(values);
            row.add(d);
            csvPrinter.printRecord(row);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);

        }
    }

    public String fillValues(String script, List<Object> values) {
        try {
            if (script == null || script.isBlank()
                    || variables == null || variables.size() > values.size()) {
                return script;
            }
            String vars = "";
            for (int i = 0; i < variables.size(); i++) {
                vars += "var " + variables.get(i) + "=" + values.get(i) + ";\n";
            }
            return vars + script;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean initData() {
        if (!checkScripts() || !checkSplits()) {
            return false;
        }
        int v = checkScale(dataScaleSelector);
        if (v >= 0) {
            dataScale = v;
            UserConfig.setInt(baseName + "DataScale", v);
        } else {
            popError(message("InvalidParamter") + ": " + message("DecimalScale"));
            return false;
        }
        expression = script();
        domain = domain();
        return true;
    }

    @FXML
    public void dataAction() {
        if (!initData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private DataFileCSV data;

            @Override
            protected boolean handle() {
                data = generateData();
                return data != null && data.saveAttributes();
            }

            @Override
            protected void whenSucceeded() {
                DataFileCSVController.loadCSV(data);
            }

        };
        start(task);
    }

    @FXML
    public void xyChartAction() {
        if (!initData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private List<List<String>> rows;
            private List<Data2DColumn> columns;

            @Override
            protected boolean handle() {
                try {
                    DataFileCSV dataFile = generateData();
                    dataFile.setTask(this);
                    rows = dataFile.allRows(false);
                    if (rows == null) {
                        return false;
                    }
                    FileDeleteTools.delete(dataFile.getFile());
                    columns = dataFile.getColumns();
                    return columns != null && columns.size() == 2;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                MathFunctionXYChartController.open(columns, rows, title());
            }

        };
        start(task);
    }

    public String title() {
        String title = editorController.attributesController.nameInput.getText();
        if (title == null || title.isBlank()) {
            int pos = expression.indexOf("\n");
            title = pos < 0 ? expression : expression.substring(0, pos);
        }
        return title;
    }

    @FXML
    public void xyzChartAction() {
        if (!initData() || !xyzController.checkParameters()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private File chartFile;

            @Override
            protected boolean handle() {
                try {
                    DataFileCSV dataFile = generateData();
                    dataFile.setTask(this);
                    List<List<String>> rows = dataFile.allRows(false);
                    if (rows == null) {
                        return false;
                    }
                    FileDeleteTools.delete(dataFile.getFile());
                    List<Data2DColumn> columns = dataFile.getColumns();
                    if (columns == null || columns.size() != 3) {
                        return false;
                    }

                    chartFile = xyzController.makeChart(columns, rows, 1, title(), dataScale, false, false, false);
                    return chartFile != null && chartFile.exists();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                browse(chartFile.getParentFile());
                browse(chartFile);
            }

        };
        start(task);
    }

    /*
        static
     */
    public static ControlMathFunctionCalculator open(MathFunctionEditor editorController) {
        try {
            ControlMathFunctionCalculator controller = (ControlMathFunctionCalculator) WindowTools.openChildStage(
                    editorController.getMyWindow(), Fxmls.MathFunctionCalculatorFxml, false);
            controller.setParameters(editorController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
}
