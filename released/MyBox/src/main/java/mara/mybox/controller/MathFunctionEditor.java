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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javax.script.Bindings;
import mara.mybox.calculation.ExpressionCalculator;
import mara.mybox.db.data.InfoNode;
import static mara.mybox.db.data.InfoNode.ValueSeparater;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.fxml.PopTools.javaScriptExamples;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-8
 * @License Apache License Version 2.0
 */
public class MathFunctionEditor extends InfoTreeNodeEditor {

    protected MathFunctionController functionController;
    protected String outputs = "";
    protected ExpressionCalculator calculator;

    @FXML
    protected TextField variablesInput, functionNameInput;

    public MathFunctionEditor() {
        defaultExt = "txt";
    }

    @Override
    public void setManager(InfoTreeManageController treeController) {
        try {
            super.setManager(treeController);

            calculator = new ExpressionCalculator();

            functionController = (MathFunctionController) treeController;

            variablesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    valueChanged(true);
                }
            });

            variablesInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    if (isSettingValues || nv) {
                        return;
                    }
                    functionController.variablesChanged();
                }
            });

            functionNameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    valueChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    protected void showEditorPane() {
    }

    @Override
    protected void editValue(InfoNode node) {
        if (node != null) {
            isSettingValues = true;
            Map<String, String> values = InfoNode.parseInfo(node);
            valueInput.setText(values.get("Expression"));
            functionNameInput.setText(values.get("MathFunctionName"));
            variablesInput.setText(values.get("Variables"));
            moreInput.setText(values.get("FunctionDomain"));
            isSettingValues = false;
        }
        functionController.variablesChanged();
    }

    @Override
    protected InfoNode pickValue(InfoNode node) {
        if (node == null) {
            return null;
        }
        String name = functionNameInput.getText();
        String variables = variablesInput.getText();
        String exp = valueInput.getText();
        String domain = moreInput.getText();
        String info;
        if ((name == null || name.isBlank())
                && (variables == null || variables.isBlank())
                && (domain == null || domain.isBlank())) {
            info = exp == null || exp.isBlank() ? null : exp.trim();
        } else {
            info = (name == null ? "" : name.trim()) + ValueSeparater + "\n"
                    + (variables == null ? "" : variables.trim()) + ValueSeparater + "\n"
                    + (exp == null ? "" : exp.trim()) + ValueSeparater + "\n"
                    + (domain == null ? "" : domain.trim());
        }
        return node.setInfo(info);
    }

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
        return valueInput.getText();
    }

    public String domain() {
        String d = moreInput.getText();
        if (d == null || d.isBlank()) {
            return null;
        }
        return StringTools.replaceLineBreak(d);
    }

    public String functionName() {
        String name = functionNameInput.getText();
        return name == null || name.isBlank() ? "f" : name;
    }

    public String titleName() {
        String name = attributesController.nameInput.getText();
        return name == null || name.isBlank() ? message("MathFunction") : name;
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

    @FXML
    protected void popScriptExamples(MouseEvent event) {
        if (UserConfig.getBoolean("FunctionScriptExamplesPopWhenMouseHovering", false)) {
            showScriptExamples(event);
        }
    }

    @FXML
    protected void showScriptExamples(Event event) {
        PopTools.popJavaScriptExamples(this, event, valueInput, "FunctionScriptExamples", null);
    }

    @FXML
    protected void popScriptHistories(Event event) {
        if (UserConfig.getBoolean("FunctionScriptHistoriesPopWhenMouseHovering", false)) {
            showScriptHistories(event);
        }
    }

    @FXML
    protected void showScriptHistories(Event event) {
        PopTools.popStringValues(this, valueInput, event, "FunctionScriptHistories", false);
    }

    @FXML
    public void clearDomain() {
        moreInput.clear();
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

            PopTools.popJavaScriptExamples(this, event, moreInput, "FunctionDomainExamples", preValues);

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
        PopTools.popStringValues(this, moreInput, event, "FunctionDomainHistories", false);
    }

}
