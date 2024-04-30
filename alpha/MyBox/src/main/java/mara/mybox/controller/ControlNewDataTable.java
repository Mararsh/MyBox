package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.data2d.writer.DataTableWriter;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-24
 * @License Apache License Version 2.0
 */
public class ControlNewDataTable extends BaseController {

    protected BaseTaskController taskController;
    protected Data2D data2D;
    protected DataTable dataTable;
    protected TableData2D tableData2D;
    protected List<Integer> columnIndices;
    protected long count;
    protected String tableName;

    @FXML
    protected ControlSelection columnsController;
    @FXML
    protected ToggleGroup keyGroup;
    @FXML
    protected TextField nameInput, idInput;
    @FXML
    protected RadioButton autoRadio;

    public ControlNewDataTable() {
        TipsLabelKey = message("SqlIdentifierComments");
    }

    public void setParameters(BaseTaskController taskController) {
        try {
            this.taskController = taskController;

            columnsController.setParameters(this, message("Column"), "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseTaskController taskController, Data2D data2D) {
        setParameters(taskController);
        setData(data2D);
    }

    public void setData(Data2D data2D) {
        try {
            this.data2D = data2D;
            String n = idInput.getText();
            if (n == null || n.isBlank()) {
                idInput.setText("id");
            }
            if (data2D != null) {
                nameInput.setText(data2D.shortName());
                columnsController.loadNames(data2D.columnNames());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setColumns(List<Integer> columnIndices) {
        try {
            if (data2D == null) {
                return;
            }
            this.columnIndices = columnIndices;
            columnsController.loadNames(null);
            if (columnIndices == null) {
                return;
            }
            List<String> names = new ArrayList<>();
            for (int index : columnIndices) {
                names.add(data2D.getColumns().get(index).getColumnName());
            }
            columnsController.loadNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkOptions() {
        try {
            tableName = null;
            String name = nameInput.getText();
            if (name == null || name.isBlank()) {
                taskController.popError(message("InvalidParameters") + ": " + message("TableName"));
                return false;
            }
            if (autoRadio.isSelected()) {
                String id = idInput.getText();
                if (id == null || id.isBlank()) {
                    taskController.popError(message("InvalidParameters") + ": " + message("ID"));
                    return false;
                }
            } else if (columnsController.selectedNames().isEmpty()) {
                taskController.popError(message("SelectToHandle") + ": " + message("PrimaryKey"));
                return false;
            }
            tableName = DerbyBase.fixedIdentifier(name.trim());
            if ((data2D instanceof DataTable) && tableName.equals(data2D.getSheet())) {
                alertError(message("CannotConvertToItself") + ": " + tableName);
                return false;
            }
            if (tableData2D == null) {
                tableData2D = new TableData2D();
            }
            return true;
        } catch (Exception e) {
            popError(e.toString());
            return false;
        }
    }

    public boolean checkOptions(Connection conn, boolean onlySQL) {
        try {
            if (!checkOptions()) {
                return false;
            }
            if (DerbyBase.exist(conn, tableName) > 0) {
                if (onlySQL) {
                    alertWarning(message("AlreadyExisted") + ": " + tableName);
                    return true;
                } else {
                    if (!PopTools.askSure(message("AlreadyExisted") + ": " + tableName,
                            message("SureReplaceExistedDatabaseTable"))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            popError(e.toString());
            return false;
        }
    }

    public DataTableWriter pickTableWriter() {
        try (Connection conn = DerbyBase.getConnection()) {
            if (!checkOptions(conn, false)) {
                return null;
            }
            DataTableWriter writer = new DataTableWriter();
            List<String> keys;
            if (autoRadio.isSelected()) {
                keys = null;
            } else {
                keys = columnsController.selectedNames();
            }
            writer.setTargetTableName(nameInput.getText().trim())
                    .setKeys(keys)
                    .setIdName(idInput.getText().trim())
                    .setTargetTableDesciption(data2D.getComments())
                    .setDropExisted(true)
                    .setRecordTargetFile(false)
                    .setRecordTargetData(true);
            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean makeTable(FxTask currentTask) {
        try {
            if (!checkOptions()) {
                return false;
            }
            List<Data2DColumn> sourceColumns = new ArrayList<>();
            for (int index : columnIndices) {
                sourceColumns.add(data2D.getColumns().get(index));
            }
            List<String> keys;
            if (autoRadio.isSelected()) {
                keys = null;
            } else {
                keys = columnsController.selectedNames();
            }
            dataTable = Data2DTableTools.makeTable(currentTask, nameInput.getText().trim(),
                    sourceColumns, keys, idInput.getText().trim());
            if (currentTask != null && !currentTask.isWorking()) {
                return false;
            }
            if (dataTable == null) {
                return false;
            }
            dataTable.setComments(data2D.getComments());
            tableData2D = dataTable.getTableData2D();
            return true;
        } catch (Exception e) {
            if (currentTask == null) {
                popError(e.toString());
            } else {
                currentTask.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean createTable(FxTask currentTask, Connection conn) {
        try {
            if (conn == null || !makeTable(currentTask)) {
                return false;
            }
            String tableName = tableData2D.getTableName();
            tableData2D = dataTable.getTableData2D();
            String sql = tableData2D.createTableStatement();
            taskController.updateLogs(sql);
            if (DerbyBase.exist(conn, tableName) > 0) {
                dataTable.drop(conn, tableName);
                conn.commit();
            }
            if (conn.createStatement().executeUpdate(sql) >= 0) {
                taskController.updateLogs(message("Created"));
            } else {
                taskController.updateLogs(message("Failed"));
                return false;
            }
            dataTable.recordTable(conn, tableName,
                    dataTable.getColumns(), dataTable.getComments());
            taskController.updateLogs(message("Record"));
            return true;
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
            return false;
        }
    }

    public boolean importData(Connection conn, List<Integer> rows, InvalidAs invalidAs) {
        try {
            conn.setAutoCommit(false);
            count = 0;
            if (rows == null || rows.isEmpty()) {
                for (List<String> pageRow : data2D.getPageData()) {
                    importRow(conn, pageRow, invalidAs);
                }
            } else {
                for (Integer row : rows) {
                    importRow(conn, data2D.getPageData().get(row), invalidAs);
                }
            }
            dataTable.setRowsNumber(count);
            data2D.getTableData2DDefinition().updateData(conn, dataTable);
            conn.commit();
            taskController.updateLogs(message("Imported") + ": " + count);
            setRowsNumber(conn);
            return true;
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
            return false;
        }
    }

    public void importRow(Connection conn, List<String> pageRow, InvalidAs invalidAs) {
        try {
            Data2DRow data2DRow = tableData2D.newRow();
            for (int i = 0; i < columnIndices.size(); i++) {
                int col = columnIndices.get(i);
                Data2DColumn sourceColumn = data2D.column(col);
                Data2DColumn targetColumn = dataTable.column(i + 1);
                data2DRow.setColumnValue(targetColumn.getColumnName(),
                        sourceColumn.fromString(pageRow.get(col + 1), invalidAs));
            }
            tableData2D.insertData(conn, data2DRow);
            if (++count % Database.BatchSize == 0) {
                conn.commit();
                taskController.updateLogs(message("Imported") + ": " + count);
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
    }

    public boolean importAllData(FxTask currentTask, Connection conn, InvalidAs invalidAs) {
        try {
            count = Data2DTableTools.importTable(currentTask, conn, data2D, dataTable, columnIndices, false, invalidAs);
            taskController.updateLogs(message("Imported") + ": " + count);
            setRowsNumber(conn);
            return count >= 0;
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
            return false;
        }
    }

    public void setRowsNumber(Connection conn) {
        try {
            if (count <= 0) {
                return;
            }
            dataTable.setRowsNumber(count);
            dataTable.getTableData2DDefinition().updateData(conn, dataTable);
            conn.commit();
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
    }

    @FXML
    public void sqlAction() {
        try (Connection conn = DerbyBase.getConnection()) {
            if (!checkOptions(conn, true) || !makeTable(null)) {
                return;
            }
            String sql = tableData2D.createTableStatement();
            TextPopController.loadText(sql);
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    @FXML
    public void sqlLink() {
        openLink(HelpTools.sqlLink());
    }

}
