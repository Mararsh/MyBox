package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DConvertTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableNodeSQL;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-14
 * @License Apache License Version 2.0
 */
public class ControlDataSQL extends BaseDataValuesController {

    protected boolean isInternalTable;

    @FXML
    protected TabPane sqlPane;
    @FXML
    protected Tab resultsTab, dataTab;
    @FXML
    protected TextArea sqlArea, outputArea;
    @FXML
    protected Button listButton, tableDefinitionButton;
    @FXML
    protected ControlData2DView viewController;
    @FXML
    protected CheckBox wrapCheck, wrapOutputsCheck;

    @Override
    public void setControlsStyle() {
        super.setControlsStyle();
        NodeStyleTools.setTooltip(listButton, new Tooltip(message("TableName")));
        startButton.requestFocus();
    }

    @Override
    public void initEditor() {
        try {
            valueInput = sqlArea;
            valueWrapCheck = wrapCheck;
            valueName = "statement";
            super.initEditor();

            wrapOutputsCheck.setSelected(UserConfig.getBoolean(baseName + "OutputsWrap", false));
            wrapOutputsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "OutputsWrap", newValue);
                    outputArea.setWrapText(newValue);
                }
            });
            outputArea.setWrapText(wrapOutputsCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        execution
     */
    @FXML
    @Override
    public void startAction() {
        String s = sqlArea.getText();
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("SQL"));
            return;
        }
        String[] lines = s.split("\n", -1);
        if (lines == null || lines.length == 0) {
            popError(message("InvalidParameters") + ": " + message("SQL"));
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
            popError(message("InvalidParameters") + ": " + message("SQL"));
            return;
        }
        showRightPane();
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataFileCSV data;

            @Override
            protected boolean handle() {
                data = null;
                try (Connection conn = DerbyBase.getConnection();
                        Statement statement = conn.createStatement()) {
                    conn.setAutoCommit(false);
                    for (String sql : sqls) {
                        try {
                            TableStringValues.add(conn, baseName + "Histories", sql);
                            outputArea.appendText(DateTools.nowString() + "  " + sql + "\n");
                            if (statement.execute(sql)) {
                                int count = statement.getUpdateCount();
                                if (count >= 0) {
                                    outputArea.appendText(DateTools.nowString() + "  " + message("UpdatedCount") + ": " + count);
                                } else {
                                    ResultSet results = statement.getResultSet();
                                    data = Data2DConvertTools.write(this, results);
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
                    viewController.loadDef(data);
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

    public void load(String sql) {
        sqlArea.setText(sql);
    }

    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("SqlExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        PopTools.popSqlExamples(this, sqlArea, null, false, event);
    }

    @FXML
    protected void popTableNames(Event event) {
        if (UserConfig.getBoolean("TableNamesPopWhenMouseHovering", false)) {
            showTableNames(event);
        }
    }

    @FXML
    protected void showTableNames(Event event) {
        PopTools.popTableNames(this, event, sqlArea, "TableNames", isInternalTable);
    }

    @FXML
    protected void tableDefinition() {
        DatabaseTableDefinitionController.open(isInternalTable);
    }

    /*
        static
     */
    public static DataTreeNodeEditorController open(BaseController parent, String sql) {
        try {
            DataTreeNodeEditorController controller
                    = DataTreeNodeEditorController.openTable(parent, new TableNodeSQL());
            ((ControlDataSQL) controller.valuesController).load(sql);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
