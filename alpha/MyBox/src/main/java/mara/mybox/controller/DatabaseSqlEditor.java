package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-14
 * @License Apache License Version 2.0
 */
public class DatabaseSqlEditor extends BaseInfoTreeNodeEditor {

    protected boolean internal;

    @FXML
    protected TabPane sqlPane;
    @FXML
    protected Tab resultsTab, dataTab;
    @FXML
    protected TextArea outputArea;
    @FXML
    protected Button listButton, tableDefinitionButton;
    @FXML
    protected ControlData2DResults dataController;
    @FXML
    protected CheckBox wrapOutputsCheck;

    public DatabaseSqlEditor() {
        defaultExt = "sql";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            wrapOutputsCheck.setSelected(UserConfig.getBoolean(treeController.category + "OutputsWrap", false));
            wrapOutputsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(treeController.category + "OutputsWrap", newValue);
                    outputArea.setWrapText(newValue);
                }
            });
            outputArea.setWrapText(wrapOutputsCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setControlsStyle() {
        super.setControlsStyle();
        NodeStyleTools.setTooltip(listButton, new Tooltip(message("TableName")));
        startButton.requestFocus();
    }

    @FXML
    @Override
    public void startAction() {
        String s = valueInput.getText();
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Codes"));
            return;
        }
        String[] lines = s.split("\n", -1);
        if (lines == null || lines.length == 0) {
            popError(message("InvalidParameters") + ": " + message("Codes"));
            return;
        }
        List<String> sqls = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            sqls.add(line.trim());
        }
        if (sqls.isEmpty()) {
            popError(message("InvalidParameters") + ": " + message("Codes"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private DataFileCSV data;

            @Override
            protected boolean handle() {
                data = null;
                try (Connection conn = DerbyBase.getConnection();
                        Statement statement = conn.createStatement()) {
                    for (String sql : sqls) {
                        try {
                            TableStringValues.add(conn, hisName(), sql);
                            outputArea.appendText(DateTools.nowString() + "  " + sql + "\n");
                            if (statement.execute(sql)) {
                                int count = statement.getUpdateCount();
                                if (count >= 0) {
                                    outputArea.appendText(DateTools.nowString() + "  " + message("UpdatedCount") + ": " + count);
                                } else {
                                    ResultSet results = statement.getResultSet();
                                    data = DataTable.save(task, results);
                                }
                            }
                            conn.commit();
                        } catch (Exception e) {
                            outputArea.appendText(e.toString() + "\n ---- \n");
                        }
                    }
                } catch (Exception e) {
                    outputArea.appendText(e.toString() + "\n ---- \n");
                    return false;
                }
                outputArea.appendText(DateTools.nowString() + "  " + message("Done")
                        + "  " + message("Cost") + ": " + duration() + "\n\n");
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (data != null) {
                    sqlPane.getSelectionModel().select(dataTab);
                    dataController.loadDef(data);
                } else {
                    sqlPane.getSelectionModel().select(resultsTab);
                }
            }

        };
        start(task);
    }

    @FXML
    public void clearOutput() {
        outputArea.clear();
    }

    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("SqlExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        PopTools.popSqlExamples(this, valueInput, null, false, event);
    }

    protected String hisName() {
        return "SQLHistories" + (internal ? "Internal" : "");
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean(hisName() + "PopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popStringValues(this, valueInput, event, hisName(), false, true);
    }

    @FXML
    protected void popTableNames(MouseEvent event) {
        if (UserConfig.getBoolean("TableNamesPopWhenMouseHovering" + (internal ? "Internal" : ""), false)) {
            tableNames(event);
        }
    }

    @FXML
    protected void showTableNames(ActionEvent event) {
        tableNames(event);
    }

    protected void tableNames(Event event) {
        try {
            MenuController controller = MenuController.open(this, valueInput, event);

            List<Node> topButtons = new ArrayList<>();
            topButtons.add(new Label(message("TableName")));

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
            String pname = "TableNamesPopWhenMouseHovering" + (internal ? "Internal" : "");
            popCheck.setSelected(UserConfig.getBoolean(pname, false));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(pname, popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<String> names;
            if (internal) {
                names = DataInternalTable.InternalTables;
            } else {
                names = DataTable.userTables();
            }
            List<Node> valueButtons = new ArrayList<>();
            for (String name : names) {
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        valueInput.replaceText(valueInput.getSelection(), name);
                        controller.getThisPane().requestFocus();
                        valueInput.requestFocus();
                    }
                });
                valueButtons.add(button);
            }
            controller.addFlowPane(valueButtons);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void popTableDefinition(MouseEvent event) {
        if (UserConfig.getBoolean("TableDefinitionPopWhenMouseHovering", false)) {
            tableDefinition(event);
        }
    }

    @FXML
    protected void showTableDefinition(ActionEvent event) {
        tableDefinition(event);
    }

    protected void tableDefinition(Event event) {
        try {
            MenuController controller = MenuController.open(this, valueInput, event);

            List<Node> topButtons = new ArrayList<>();
            topButtons.add(new Label(message("TableDefinition")));

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImageView("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWindowWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean("TableDefinitionPopWhenMouseHovering", false));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("TableDefinitionPopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            List<String> names;
            if (internal) {
                names = DataInternalTable.InternalTables;
            } else {
                names = DataTable.userTables();
            }
            List<Node> valueButtons = new ArrayList<>();
            for (String name : names) {
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String html = TableData2D.tableDefinition(name);
                        if (html != null) {
                            HtmlPopController.openHtml(myController, html);
                        } else {
                            popError(message("NotFound"));
                        }
                    }
                });
                valueButtons.add(button);
            }
            controller.addFlowPane(valueButtons);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

}
