package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-1
 * @License Apache License Version 2.0
 */
public class DataTableQueryEditor extends InfoTreeNodeEditor {

    protected DataTable dataTable;

    @FXML
    protected ListView namesList;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck;
    @FXML
    protected Label dataLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            namesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (newV == null || newV.isBlank()) {
                        return;
                    }
                    valueInput.replaceText(valueInput.getSelection(), newV);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseData2DLoadController tableController) {
        try {
            setDataTable(tableController.data2D);

            dataLabel.setText(dataTable.displayName());
            targetController.setParameters(this, tableController);

            rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
            rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setDataTable(Data2D data2D) {
        try {
            this.dataTable = (DataTable) data2D;
            namesList.getItems().clear();
            if (dataTable == null || !dataTable.isValid()) {
                return;
            }
            String name = dataTable.getSheet();
            if (name != null && !name.isBlank()) {
                namesList.getItems().add(name);
            }
            namesList.getItems().addAll(dataTable.columnNames());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void startAction() {
        Data2DWriter writer = targetController.pickWriter();
        if (writer == null || writer.getTargetFile() == null) {
            popError(message("InvalidParameter") + ": " + message("TargetFile"));
            return;
        }
        String s = valueInput.getText();
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters") + ": SQL");
            return;
        }
        String query = StringTools.replaceLineBreak(s);
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement statement = conn.prepareStatement(query);
                        ResultSet results = statement.executeQuery()) {
                    task.setInfo(query);
                    TableStringValues.add(conn, "DataTableQueryHistories", query);
                    dataTable.setTask(this);
                    return Data2DTableTools.write(task, dataTable, writer, results,
                            rowNumberCheck.isSelected() ? message("Row") : null,
                            dataTable.getScale(), ColumnDefinition.InvalidAs.Blank);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                writer.showResult();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataTable.stopTask();
            }

        };
        start(task);
    }

    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("SqlExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        PopTools.popSqlExamples(this, valueInput,
                dataTable != null ? dataTable.getSheet() : null,
                true, event);
    }

    @Override
    protected String editorName() {
        return "DataTableQuery";
    }

    @FXML
    protected void tableDefinition() {
        if (dataTable == null || dataTable.getSheet() == null) {
            popError(message("NotFound"));
            return;
        }
        String html = TableData2D.tableDefinition(dataTable.getSheet());
        if (html != null) {
            HtmlPopController.openHtml(this, html);
        } else {
            popError(message("NotFound"));
        }
    }

}
