package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public static final String NamesPrefix = "Names:::";

    @FXML
    protected TextField variablesInput, resultNameInput;

    public MathFunctionEditor() {
        defaultExt = "txt";
    }

    protected void setParameters(MathFunctionController controller) {
        try {
            this.functionController = controller;

            variablesInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    if (isSettingValues || nv) {
                        return;
                    }
                    functionController.calculateController.variablesChanged();
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
    protected synchronized void editNode(InfoNode node) {
        super.editNode(node);
        if (node == null) {
            functionController.calculateController.variablesChanged();
            return;
        }
        isSettingValues = true;
        variablesInput.clear();
        resultNameInput.clear();
        String v = node.getValue();
        if (v != null && v.startsWith(NamesPrefix)) {
            String namesString, script;
            int pos = v.indexOf("\n");
            if (pos > 0) {
                namesString = v.substring(NamesPrefix.length(), pos);
                script = v.substring(pos + 1);
            } else {
                namesString = v;
                script = "";
            }
            valueInput.setText(script);
            String[] names = namesString.split(",");
            resultNameInput.setText(names[0]);
            namesString = "";
            if (names.length > 1) {
                namesString = names[1];
                for (int i = 2; i < names.length; i++) {
                    namesString += ", " + names[i];
                }
            }
            variablesInput.setText(namesString);
        } else {
            resultNameInput.setText("f");
        }
        isSettingValues = false;
        functionController.calculateController.variablesChanged();
    }

    @Override
    public InfoNode pickNodeData() {
        InfoNode node = super.pickNodeData();
        if (node == null) {
            return node;
        }
        List<String> variableNames = variableNames();
        String script = valueInput.getText();
        if (variableNames == null || variableNames.isEmpty()) {
            node.setValue(script);
        } else {
            node.setValue(makeNames(variableNames) + "\n" + script);
        }
        return node;
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

    public String resultName() {
        String resultName = resultNameInput.getText();
        if (resultName == null || resultName.isBlank()) {
            return "f";
        } else {
            return resultName;
        }
    }

    public String makeNames(List<String> variableNames) {
        if (variableNames == null || variableNames.isEmpty()) {
            return null;
        } else {
            String resultName = resultName();
            String finalNames = NamesPrefix + resultName.trim();
            for (String name : variableNames) {
                finalNames += "," + name.trim();
            }
            return finalNames;
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
