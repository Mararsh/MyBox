package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javax.script.Bindings;
import mara.mybox.calculation.ExpressionCalculator;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import static mara.mybox.fxml.PopTools.javaScriptExamples;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-8
 * @License Apache License Version 2.0
 */
public class ControlDataMathFunction extends BaseDataValuesController {

    protected ExpressionCalculator calculator;
    protected String expression, domain, outputs = "";
    protected int calculateScale;

    @FXML
    protected TextField variablesInput;
    @FXML
    protected TextArea expressionInput, domainInput;
    @FXML
    protected VBox inputsBox;
    @FXML
    protected ComboBox<String> calculateScaleSelector;
    @FXML
    protected ControlWebView outputController;
    @FXML
    protected Button calculateButton, dataButton, XYChartButton;

    public ControlDataMathFunction() {
        TipsLabelKey = "MathFunctionTips";
    }

    @Override
    public void initEditor() {
        try {

            calculator = new ExpressionCalculator();

            variablesInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    if (isSettingValues || nv) {
                        return;
                    }
                    valueChanged(true);
                    checkVariables();
                }
            });

            expressionInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    if (isSettingValues || nv) {
                        return;
                    }
                    valueChanged(true);
                }
            });

            domainInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    if (isSettingValues || nv) {
                        return;
                    }
                    valueChanged(true);
                }
            });

            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

            calculateScale = UserConfig.getInt(baseName + "CalculateScale", 8);
            if (calculateScale < 0) {
                calculateScale = 8;
            }
            calculateScaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            calculateScaleSelector.getSelectionModel().select(calculateScale + "");

            calculateButton.disableProperty().bind(expressionInput.textProperty().isEmpty());

            checkVariables();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            isSettingValues = true;
            if (nodeEditor.currentNode != null) {
                expressionInput.setText(nodeEditor.currentNode.getStringValue("expression"));
                variablesInput.setText(nodeEditor.currentNode.getStringValue("variables"));
                domainInput.setText(nodeEditor.currentNode.getStringValue("domain"));
            } else {
                expressionInput.clear();
                variablesInput.clear();
                domainInput.clear();
            }
            isSettingValues = false;
            checkVariables();
            valueChanged(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        try {
            String expression = expressionInput.getText();
            node.setValue("expression", expression == null ? null : expression.trim());
            String variables = variablesInput.getText();
            node.setValue("variables", variables == null ? null : variables.trim());
            String domain = domainInput.getText();
            node.setValue("domain", domain == null ? null : domain.trim());
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        values
     */
    public List<String> variableNames() {
        String variableNames = variablesInput.getText();
        if (variableNames == null || variableNames.isBlank()) {
            return null;
        } else {
            List<String> names = new ArrayList<>();
            String[] vnames = variableNames.split(",");
            for (String name : vnames) {
                String s = name.trim();
                if (!s.isBlank()) {
                    names.add(s);
                }
            }
            return names;
        }
    }

    public Bindings bindings() {
        if (calculator == null) {
            return null;
        } else {
            return calculator.variableValues;
        }
    }

    public String script() {
        return expressionInput.getText();
    }

    public String domain() {
        String d = domainInput.getText();
        if (d == null || d.isBlank()) {
            return null;
        }
        return StringTools.replaceLineBreak(d);
    }

    public String calculate(String script, Map<String, Object> variables) {
        return calculator.calculate(script, variables);
    }

    public boolean inDomain(String domain, Map<String, Object> variables) {
        if (domain == null || domain.isBlank()) {
            return true;
        }
        return calculator.condition(domain, variables);
    }

    public Map<String, Object> dummyBindings() {
        try {
            Map<String, Object> vs = new HashMap<>();
            List<String> variables = variableNames();
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    vs.put(variables.get(i), 1);
                }
            }
            return vs;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Map<String, Object> pickVariables() {
        try {
            Map<String, Object> vs = new HashMap<>();
            List<Node> nodes = inputsBox.getChildren();
            for (int i = 0; i < nodes.size(); i++) {
                FlowPane fp = (FlowPane) nodes.get(i);
                Label label = (Label) fp.getChildren().get(0);
                TextField input = (TextField) fp.getChildren().get(1);
                double d = DoubleTools.toDouble(input.getText(), ColumnDefinition.InvalidAs.Empty);
                vs.put(label.getText(), d);
            }
            return vs;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String functionName() {
        String name = nodeEditor.titleInput.getText();
        return name == null || name.isBlank() ? "f" : name;
    }


    /*
        status
     */
    public void checkVariables() {
        try {
            inputsBox.getChildren().clear();
            XYChartButton.setDisable(true);
            dataButton.setDisable(true);
            List<String> variables = variableNames();
            if (variables != null && !variables.isEmpty()) {
                for (String variable : variables) {
                    FlowPane fp = new FlowPane();
                    fp.setAlignment(Pos.CENTER_LEFT);
                    fp.setVgap(2);
                    fp.setHgap(2);
                    TextField input = new TextField();
                    input.setPrefWidth(80);
                    fp.getChildren().addAll(new Label(variable), input);
                    inputsBox.getChildren().add(fp);
                }
                if (variables.size() <= 2) {
                    XYChartButton.setDisable(false);
                }
                dataButton.setDisable(false);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean checkScripts() {
        String script = script();
        if (script == null || script.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Expression"));
            return false;
        }
        Map<String, Object> variableValues = dummyBindings();
        script = script.trim();
        String ret = calculate(script, variableValues);
        if (ret == null) {
            if (calculator.getError() != null) {
                popError(calculator.getError());
            } else {
                popError(message("InvalidParameters") + ": " + message("Expression"));
            }
            return false;
        }
        String edomain = domain();
        if (edomain == null) {
            return true;
        }
        ret = calculate(edomain, variableValues);
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

    /*
        edit pane
     */
    @FXML
    public void dataAction() {
        MathFunctionDataController.open(this);
    }

    @FXML
    public void chartAction() {
        MathFunctionChartController.open(this);
    }

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
                popError(message("InvalidParameter") + ": " + message("DecimalScale"));
                return;
            }
            Map<String, Object> variableValues = pickVariables();
            expression = script();
            domain = domain();
            if (!inDomain(domain, variableValues)) {
                popError(message("NotInDomain"));
                return;
            }
            String ret = calculate(expression, variableValues);
            if (ret == null) {
                popError(message("Failed"));
                return;
            }
            double d = DoubleTools.scale(ret, ColumnDefinition.InvalidAs.Empty, calculateScale);
            ret = DoubleTools.invalidDouble(d) ? ret : (d + "");
            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + message("Expression") + ": <br>\n"
                    + HtmlWriteTools.stringToHtml(expression) + "<br>\n";
            if (variableValues != null && !variableValues.isEmpty()) {
                outputs += "<br>" + message("Variables") + ": <br>\n";
                for (String n : variableValues.keySet()) {
                    outputs += n + "=" + variableValues.get(n) + "<br>";
                }
            }
            outputs += "</div>";
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

    @FXML
    public void clearExpression() {
        expressionInput.clear();
    }

    @FXML
    public void clearDomain() {
        domainInput.clear();
    }

    @FXML
    protected void popScriptExamples(MouseEvent event) {
        if (UserConfig.getBoolean("FunctionScriptExamplesPopWhenMouseHovering", false)) {
            showScriptExamples(event);
        }
    }

    @FXML
    protected void showScriptExamples(Event event) {
        PopTools.popJavaScriptExamples(this, event, expressionInput, "FunctionScriptExamples", null);
    }

    @FXML
    protected void popScriptHistories(Event event) {
        if (UserConfig.getBoolean("FunctionScriptHistoriesPopWhenMouseHovering", false)) {
            showScriptHistories(event);
        }
    }

    @FXML
    protected void showScriptHistories(Event event) {
        PopTools.popStringValues(this, expressionInput, event, "FunctionScriptHistories", false);
    }

    @FXML
    protected void popDomainExamples(MouseEvent event) {
        if (UserConfig.getBoolean("FunctionDomainExamplesPopWhenMouseHovering", false)) {
            domainExamples(event);
        }
    }

    @FXML
    protected void showDomainExamples(ActionEvent event) {
        domainExamples(event);
    }

    protected void domainExamples(Event event) {
        try {
            List<List<String>> preValues = new ArrayList<>();
            preValues.add(Arrays.asList(
                    "x > 0", "x >= 0", "x < 0", "x <= 0", "x != 0", "x != 1",
                    "x >= -1 && x <= 1", "( x - Math.PI / 2 ) % Math.PI != 0"
            ));
            preValues.addAll(javaScriptExamples("x", "stringV", "dateV"));

            PopTools.popJavaScriptExamples(this, event, domainInput, "FunctionDomainExamples", preValues);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void popDomainHistories(Event event) {
        if (UserConfig.getBoolean("FunctionDomainHistoriesPopWhenMouseHovering", false)) {
            showDomainHistories(event);
        }
    }

    @FXML
    protected void showDomainHistories(Event event) {
        PopTools.popStringValues(this, domainInput, event, "FunctionDomainHistories", false);
    }

    @FXML
    public void popMathFunctionHelps(Event event) {
        if (UserConfig.getBoolean("JavaScriptHelpsPopWhenMouseHovering", false)) {
            showMathFunctionHelps(event);
        }
    }

    @FXML
    public void showMathFunctionHelps(Event event) {
        popEventMenu(event, HelpTools.javascriptHelps());
    }

    /*
        results pane
     */
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

}
