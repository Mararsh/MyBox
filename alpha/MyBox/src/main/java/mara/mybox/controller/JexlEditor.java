package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.data.KeyValue;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;

/**
 * @Author Mara
 * @CreateDate 2022-5-17
 * @License Apache License Version 2.0
 */
public class JexlEditor extends TreeNodeEditor {

    protected JexlController jexlController;
    protected String outputs = "";
    protected JexlEngine jexlEngine;
    protected MapContext jexlContext;
    protected JexlScript jexlScript;
    protected ObservableList<KeyValue> variables, contextVariables, parameters;

    @FXML
    protected Button clearCodesButton;
    @FXML
    protected TableView<KeyValue> varsView, contextView, paraView;
    @FXML
    protected TableColumn<KeyValue, String> keyColumn, contextKeyColumn, paraKeyColumn;
    @FXML
    protected TableColumn<KeyValue, String> valueColumn, contextValueColumn, paraValueColumn;
    @FXML
    protected Label varLabel;
    @FXML
    protected TabPane varsPane;
    @FXML
    protected Tab varTab, paraTab, contextTab;

    public JexlEditor() {
        defaultExt = "java";
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(clearCodesButton, new Tooltip(message("Clear") + "\nCTRL+g"));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            variables = FXCollections.observableArrayList();
            varsView.setItems(variables);
            keyColumn.setEditable(false);
            keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            valueColumn.setEditable(true);
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.getStyleClass().add("editable-column");
            valueColumn.setCellFactory(new Callback<TableColumn<KeyValue, String>, TableCell<KeyValue, String>>() {
                @Override
                public TableCell<KeyValue, String> call(TableColumn<KeyValue, String> param) {
                    return new TableAutoCommitCell<KeyValue, String>(new DefaultStringConverter()) {
                        @Override
                        public void commitEdit(String value) {
                            try {
                                if (isSettingValues) {
                                    return;
                                }
                                super.commitEdit(value);
                                KeyValue row = row();
                                if (row == null) {
                                    return;
                                }
                                isSettingValues = true;
                                setContext(row.getKey(), value);
                                isSettingValues = false;
                            } catch (Exception e) {
                                MyBoxLog.debug(e);
                            }
                        }
                    };
                }
            });

            parameters = FXCollections.observableArrayList();
            paraView.setItems(parameters);
            paraKeyColumn.setEditable(false);
            paraKeyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            paraValueColumn.setEditable(true);
            paraValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            paraValueColumn.getStyleClass().add("editable-column");
            paraValueColumn.setCellFactory(new Callback<TableColumn<KeyValue, String>, TableCell<KeyValue, String>>() {
                @Override
                public TableCell<KeyValue, String> call(TableColumn<KeyValue, String> param) {
                    return new TableAutoCommitCell<KeyValue, String>(new DefaultStringConverter()) {
                        @Override
                        public void commitEdit(String value) {
                            try {
                                if (isSettingValues) {
                                    return;
                                }
                                super.commitEdit(value);
                                KeyValue row = row();
                                if (row == null) {
                                    return;
                                }
                                isSettingValues = true;
                                setContext(row.getKey(), value);
                                isSettingValues = false;
                            } catch (Exception e) {
                                MyBoxLog.debug(e);
                            }
                        }
                    };
                }
            });

            contextVariables = FXCollections.observableArrayList();
            contextView.setItems(contextVariables);
            contextKeyColumn.setEditable(true);
            contextKeyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            contextKeyColumn.getStyleClass().add("editable-column");
            contextKeyColumn.setCellFactory(new Callback<TableColumn<KeyValue, String>, TableCell<KeyValue, String>>() {
                @Override
                public TableCell<KeyValue, String> call(TableColumn<KeyValue, String> param) {
                    return new TableAutoCommitCell<KeyValue, String>(new DefaultStringConverter()) {
                        @Override
                        public void commitEdit(String key) {
                            try {
                                if (isSettingValues) {
                                    return;
                                }
                                super.commitEdit(key);
                                int index = rowIndex();
                                if (index < 0 || index >= contextVariables.size()) {
                                    return;
                                }
                                KeyValue row = contextVariables.get(index);
                                String value = row.getValue();
                                isSettingValues = true;
                                jexlContext.set(key, value);
                                updateList(variables, key, value);
                                updateList(parameters, key, value);
                                isSettingValues = false;
                            } catch (Exception e) {
                                MyBoxLog.debug(e);
                            }
                        }
                    };
                }
            });
            contextValueColumn.setEditable(true);
            contextValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            contextValueColumn.getStyleClass().add("editable-column");
            contextValueColumn.setCellFactory(new Callback<TableColumn<KeyValue, String>, TableCell<KeyValue, String>>() {
                @Override
                public TableCell<KeyValue, String> call(TableColumn<KeyValue, String> param) {
                    return new TableAutoCommitCell<KeyValue, String>(new DefaultStringConverter()) {
                        @Override
                        public void commitEdit(String value) {
                            try {
                                if (isSettingValues) {
                                    return;
                                }
                                super.commitEdit(value);
                                int index = rowIndex();
                                if (index < 0 || index >= contextVariables.size()) {
                                    return;
                                }
                                KeyValue row = contextVariables.get(index);
                                String key = row.getKey();
                                isSettingValues = true;
                                jexlContext.set(key, value);
                                updateList(variables, key, value);
                                updateList(parameters, key, value);
                                isSettingValues = false;
                            } catch (Exception e) {
                                MyBoxLog.debug(e);
                            }
                        }
                    };
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(JexlController jexlController) {
        try {
            this.jexlController = jexlController;
            jexlEngine = new JexlBuilder().cache(512).strict(true).silent(false).create();
            jexlContext = new MapContext();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void showEditorPane() {
    }

    @FXML
    @Override
    public void goAction() {
        try {
            variables.clear();
            parameters.clear();
            varLabel.setText("");
            String inputs = valueInput.getText();
            if (inputs == null || inputs.isBlank()) {
                popError(message("NoInput"));
                return;
            }
            try {
                jexlScript = jexlEngine.createScript(inputs);
            } catch (Exception e) {
                popError(e.toString());
                return;
            }
            String parsed = jexlScript.getParsedText();
            TableStringValues.add("JexlHistories", parsed);

            isSettingValues = true;
            Set<List<String>> varSet = jexlScript.getVariables();
            boolean newVar = false, newPara = false;
            Object v;
            if (varSet != null && !varSet.isEmpty()) {
                for (List<String> s : varSet) {
                    for (String p : s) {
                        if (jexlContext.has(p)) {
                            v = jexlContext.get(p);
                        } else {
                            v = null;
                            setContext(p, v);
                            newVar = true;
                        }
                        variables.add(new KeyValue(p, v == null ? null : v.toString()));
                    }
                }
            }
            String[] ps = jexlScript.getParameters();
            if (ps != null) {
                for (String p : ps) {
                    if (jexlContext.has(p)) {
                        v = jexlContext.get(p);
                    } else {
                        v = null;
                        setContext(p, v);
                        newPara = true;
                    }
                    parameters.add(new KeyValue(p, v == null ? null : v.toString()));
                }
            }
            isSettingValues = false;
            if (newVar) {
                varLabel.setText(message("SetVariablesForResult"));
                varsPane.getSelectionModel().select(varTab);
            } else if (newPara) {
                varLabel.setText(message("SetVariablesForResult"));
                varsPane.getSelectionModel().select(paraTab);
            } else {
                runScript(parsed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setContext(String key, Object value) {
        jexlContext.set(key, value);
        if (!updateList(contextVariables, key, value)) {
            contextVariables.add(new KeyValue(key, value == null ? null : value.toString()));
        }
    }

    public boolean updateList(List<KeyValue> list, String key, Object value) {
        if (list == null) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            KeyValue kv = list.get(i);
            if (kv.getKey().equals(key)) {
                kv.setValue(value == null ? null : value.toString());
                list.set(i, kv);
                return true;
            }
        }
        return false;
    }

    public boolean resetList(List<KeyValue> list) {
        if (list == null) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            KeyValue kv = list.get(i);
            kv.setValue(null);
            list.set(i, kv);
        }
        return false;
    }

    @FXML
    @Override
    public void startAction() {
        if (startButton.getUserData() != null) {
            cancelAction();
            return;
        }
        runScript(jexlScript.getParsedText());
    }

    protected void runScript(String script) {
        if (script == null || script.isBlank()) {
            popError(message("NoInput"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        task = new SingletonTask<Void>(this) {

            private String outputs;

            @Override
            protected boolean handle() {
                try {
                    Object results;
                    int psize = parameters.size();
                    if (psize > 0) {
                        String[] ps = new String[psize];
                        for (int i = 0; i < psize; i++) {
                            KeyValue kv = parameters.get(i);
                            ps[i] = kv.getValue();
                        }
                        results = jexlScript.execute(jexlContext, ps);
                    } else {
                        results = jexlScript.execute(jexlContext);
                    }
                    outputs = DateTools.nowString()
                            + "<div class=\"valueText\" >"
                            + HtmlWriteTools.stringToHtml(script)
                            + "</div>";
                    outputs += "<div class=\"valueBox\">"
                            + HtmlWriteTools.stringToHtml(results.toString()) + "</div>";
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                output(outputs);
            }

            @Override
            protected void finalAction() {
                cancelAction();
            }
        };
        start(task);

    }

    @Override
    public void cancelAction() {
        if (task != null) {
            task.cancel();
        }
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        startButton.setUserData(null);
    }

    protected void output(String msg) {
        Platform.runLater(() -> {
            outputs += msg + "<br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            jexlController.webViewController.loadContents(html);
        });

    }

    // https://commons.apache.org/proper/commons-jexl/reference/syntax.html
    @FXML
    protected void popSyntaxMenu(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(jexlController, valueInput,
                    mouseEvent.getScreenX(), mouseEvent.getScreenY() + 20);
            controller.setTitleLabel(message("Syntax"));

            List<Node> topButtons = new ArrayList<>();
            Button newLineButton = new Button(message("Newline"));
            newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.replaceText(valueInput.getSelection(), "\n");
                    valueInput.requestFocus();
                }
            });
            topButtons.add(newLineButton);
            Button clearInputButton = new Button(message("ClearInputArea"));
            clearInputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.clear();
                }
            });
            topButtons.add(clearInputButton);
            controller.addFlowPane(topButtons);

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    ";", " , ", "( )", " = ", " { } ", "[ ]", "\"", " + ", " - ", " * ", " / ",
                    " == ", " != ", " >= ", " > ", " <= ", " < "
            ));
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "var circleArea = function(r) \n"
                    + "{ 3.1415 * r * r };\n"
                    + "return circleArea (2.6);",
                    "3.1415 * r * r;",
                    "var d = 1.0d;\n"
                    + "for(var i: [1,2,3,4] ) {\n"
                    + "    d += i / 2.0d - 1;\n"
                    + "}\n"
                    + "return d;\n",
                    "var a = 5;\n"
                    + "if (b < 3) {\n"
                    + "    a += b;\n"
                    + "} else {\n"
                    + "    a -= b;\n"
                    + "}\n"
                    + "return a;\n",
                    "var s = \"hello \";\n"
                    + "while (s.length() < len) {\n"
                    + "   s += \"a\";\n"
                    + "}\n"
                    + "return s;\n"
            ));

            Hyperlink blink = new Hyperlink("JEXL Reference");
            blink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://commons.apache.org/proper/commons-jexl/reference/index.html");
                }
            });
            controller.addNode(blink);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, valueInput, mouseEvent, "JexlHistories", true);
    }

    @FXML
    public void addContext() {
        setContext("v" + new Date().getTime(), null);
    }

    @FXML
    public void deleteContext() {
        List<KeyValue> selected = contextView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        for (int i = 0; i < selected.size(); i++) {
            KeyValue kv = contextVariables.get(i);
            String key = kv.getKey();
            updateList(variables, key, null);
            updateList(parameters, key, null);
        }
        contextVariables.removeAll(selected);
        jexlContext.clear();
        for (KeyValue kv : contextVariables) {
            jexlContext.set(kv.getKey(), kv.getValue());
        }
        isSettingValues = false;
    }

    @FXML
    public void clearContext() {
        isSettingValues = true;
        jexlContext.clear();
        contextVariables.clear();
        resetList(variables);
        resetList(parameters);
        isSettingValues = false;
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
            jexlContext = null;
            jexlEngine = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
