package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-8
 * @License Apache License Version 2.0
 */
public class MathFunctionEditor extends BaseInfoTreeNodeController {

    protected MathFunctionController functionController;
    protected String outputs = "";

    @FXML
    protected TextField variablesInput, functionNameInput;

    public MathFunctionEditor() {
        defaultExt = "txt";
    }

    protected void setParameters(MathFunctionController controller) {
        try {
            this.functionController = controller;

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
                    functionController.calculateController.variablesChanged();
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
    protected void editInfo(InfoNode node) {
        if (node != null) {
            isSettingValues = true;
            Map<String, String> values = InfoNode.parseInfo(node);
            valueInput.setText(values.get("Expression"));
            functionNameInput.setText(values.get("MathFunctionName"));
            variablesInput.setText(values.get("Variables"));
            moreInput.setText(values.get("FunctionDomain"));
            isSettingValues = false;
        } else {
            functionNameInput.setText("f");
        }
        functionController.calculateController.variablesChanged();
    }

    @Override
    protected String nodeInfo() {
        String info;
        String functionName = functionName();
        if (functionName != null && !functionName.isBlank()) {
            info = functionName.trim() + "\n";
        } else {
            info = "";
        }
        String variableNames = variablesInput.getText();
        if (variableNames != null && !variableNames.isBlank()) {
            info += InfoNode.ValueSeparater + "\n" + variableNames.trim() + "\n";
        } else {
            info += InfoNode.ValueSeparater + "\n";
        }
        String exp = valueInput.getText();
        if (exp != null && !exp.isBlank()) {
            info += InfoNode.ValueSeparater + "\n" + exp.trim() + "\n";
        } else {
            info += InfoNode.ValueSeparater + "\n";
        }
        String domain = moreInput.getText();
        if (domain != null && !domain.isBlank()) {
            info += InfoNode.ValueSeparater + "\n" + domain.trim();
        } else {
            info += InfoNode.ValueSeparater;
        }
        return info;
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

    public String functionName() {
        String name = functionNameInput.getText();
        if (name == null || name.isBlank()) {
            return "f";
        } else {
            return name;
        }
    }

    @FXML
    protected void popScriptExamples(MouseEvent event) {
        if (UserConfig.getBoolean("FunctionScriptExamplesPopWhenMouseHovering", false)) {
            scriptExamples(event);
        }
    }

    @FXML
    protected void showScriptExamples(ActionEvent event) {
        scriptExamples(event);
    }

    protected void scriptExamples(Event event) {
        try {
            String menuName = "FunctionScriptExamples";
            MenuController controller = PopTools.popJavaScriptExamples(this, event, valueInput, menuName);

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "Math.PI", "Math.E", "Math.random()", "Math.abs(x)",
                    "Math.pow(x,2)", "Math.pow(x,3)", "Math.sqrt(x)", "Math.pow(x,1d/3)",
                    "Math.pow(3, x)", "Math.exp(x)",
                    "Math.log(x)", "Math.sin(x)", "Math.cos(x)", "Math.tan(x)"
            ), 4, menuName);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void popScriptHistories(Event event) {
        if (UserConfig.getBoolean("FunctionScriptHistoriesPopWhenMouseHovering", false)) {
            showScriptHistories(event);
        }
    }

    @FXML
    protected void showScriptHistories(Event event) {
        PopTools.popStringValues(this, valueInput, event, "FunctionScriptHistories", false, true);
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
            String menuName = "FunctionDomainExamples";
            MenuController controller = PopTools.popJavaScriptExamples(this, event, moreInput, menuName);

            PopTools.addButtonsPane(controller, moreInput, Arrays.asList(
                    "x > 0", "x >= 0", "x < 0", "x <= 0", "x != 0", "x != 1",
                    "x >= -1 && x <= 1", "( x - Math.PI / 2 ) % Math.PI != 0"
            ), 4, menuName);

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
        PopTools.popStringValues(this, moreInput, event, "FunctionDomainHistories", false, true);
    }

}
