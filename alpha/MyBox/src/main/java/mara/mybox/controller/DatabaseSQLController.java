package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-14
 * @License Apache License Version 2.0
 */
public class DatabaseSQLController extends BaseController {

    protected List<Data2DColumn> db2Columns;
    protected List<List<String>> data;
    protected boolean internal;
    protected int maxLines;

    @FXML
    protected TextArea sqlArea, outputArea;
    @FXML
    protected Button listButton, examplesButton, tableDefinitionButton;
    @FXML
    protected ControlWebView dataController;
    @FXML
    protected TextField maxLinesinput;

    public DatabaseSQLController() {
        baseTitle = message("DatabaseSQL");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.setParent(this);

            maxLines = UserConfig.getInt(baseName + "MaxLinesNumber", 5000);
            if (maxLines <= 0) {
                maxLines = 5000;
            }
            maxLinesinput.setText(maxLines + "");
            maxLinesinput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    try {
                        int iv = Integer.parseInt(maxLinesinput.getText());
                        if (iv > 0) {
                            maxLines = iv;
                            maxLinesinput.setStyle(null);
                            UserConfig.setInt(baseName + "MaxLinesNumber", maxLines);
                        } else {
                            maxLinesinput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        maxLinesinput.setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
        String s = sqlArea.getText();
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters"));
            return;
        }
        String[] cmds = s.split("\n", -1);
        if (cmds == null || cmds.length == 0) {
            popError(message("InvalidParameters"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private String html;

            @Override
            protected boolean handle() {
                html = null;
                try ( Connection conn = DerbyBase.getConnection();
                         Statement statement = conn.createStatement()) {
                    for (String sql : cmds) {
                        try {
                            TableStringValues.add(conn, "SQLHistories" + (internal ? "Internal" : ""), sql);
                            outputArea.appendText(DateTools.nowString() + "  " + sql + "\n");
                            if (statement.execute(sql)) {
                                int count = statement.getUpdateCount();
                                if (count >= 0) {
                                    outputArea.appendText(DateTools.nowString() + "  " + message("UpdatedCount") + ": " + count);
                                } else {
                                    ResultSet results = statement.getResultSet();
                                    if (results != null) {
                                        ResultSetMetaData meta = results.getMetaData();
                                        int cols = meta.getColumnCount();
                                        List<String> names = new ArrayList<>();
                                        db2Columns = new ArrayList<>();
                                        data = new ArrayList<>();
                                        for (int col = 1; col <= cols; col++) {
                                            String name = meta.getColumnName(col);
                                            names.add(name);
                                            Data2DColumn dc = new Data2DColumn(name,
                                                    ColumnDefinition.sqlColumnType(meta.getColumnType(col)),
                                                    meta.isNullable(col) == ResultSetMetaData.columnNoNulls);
                                            db2Columns.add(dc);
                                        }
                                        StringTable table = new StringTable(names, sql);
                                        count = 0;
                                        while (results.next() && count++ < maxLines) {
                                            List<String> row = new ArrayList<>();
                                            for (String name : names) {
                                                Object v = results.getObject(name);
                                                row.add(v == null ? "" : (v + ""));
                                            }
                                            table.add(row);
                                            data.add(row);
                                        }
                                        html = table.html();
                                    }
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

                if (html != null) {
                    dataController.loadContents(html);
                }
            }

        };
        start(task);
    }

    @FXML
    public void clearSQL() {
        sqlArea.clear();
    }

    @FXML
    public void clearOutput() {
        outputArea.clear();
    }

    @FXML
    public void clearData() {
        dataController.loadContents("");
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        PopTools.popSqlExamples(this, sqlArea, mouseEvent);
    }

    @FXML
    protected void popSqlHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, sqlArea, mouseEvent, "SQLHistories" + (internal ? "Internal" : ""));
    }

    @FXML
    protected void popTableNames(MouseEvent mouseEvent) {
        PopTools.popTableNames(this, sqlArea, mouseEvent, internal);
    }

    @FXML
    protected void popTableDefinition(MouseEvent mouseEvent) {
        PopTools.popTableDefinition(this, sqlArea, mouseEvent, internal);
    }

    @FXML
    public void dataAction() {
        if (db2Columns == null || data == null || data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        DataFileTextController.open(db2Columns, data);
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    /*
        static
     */
    public static DatabaseSQLController open(boolean internal) {
        DatabaseSQLController controller = (DatabaseSQLController) WindowTools.openStage(Fxmls.DatabaseSQLFxml);
        controller.setInternal(internal);
        controller.requestMouse();
        return controller;
    }

}
