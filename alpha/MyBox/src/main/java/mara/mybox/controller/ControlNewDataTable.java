package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
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
    
    @FXML
    protected ControlCheckBoxList columnsController;
    @FXML
    protected ToggleGroup keyGroup;
    @FXML
    protected TextField nameInput;
    @FXML
    protected RadioButton autoRadio;
    
    public ControlNewDataTable() {
        TipsLabelKey = message("SqlIdentifierComments");
    }
    
    public void setParameters(BaseTaskController taskController) {
        try {
            this.taskController = taskController;
            
            dataTable = new DataTable();
            tableData2D = new TableData2D();
            
            columnsController.setParent(this);
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public void setParameters(BaseTaskController taskController, Data2D data2D) {
        setParameters(taskController);
        setData(data2D);
    }
    
    public void setData(Data2D data2D) {
        try {
            this.data2D = data2D;
            nameInput.setText(data2D.shortName());
            columnsController.setValues(data2D.columnNames());
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public void setColumns(List<Integer> columnIndices) {
        try {
            if (data2D == null) {
                return;
            }
            this.columnIndices = columnIndices;
            columnsController.clear();
            if (columnIndices == null) {
                return;
            }
            List<String> names = new ArrayList<>();
            for (int index : columnIndices) {
                names.add(data2D.getColumns().get(index).getColumnName());
            }
            columnsController.setValues(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public boolean checkOptions(Connection conn, boolean onlySQL) {
        try {
            if (nameInput.getText().isBlank()) {
                taskController.popError(message("InvalidParameters") + ": " + message("TableName"));
                return false;
            }
            if (!autoRadio.isSelected() && columnsController.checkedValues().isEmpty()) {
                taskController.popError(message("SelectToHandle") + ": " + message("PrimaryKey"));
                return false;
            }
            String tableName = DerbyBase.fixedIdentifier(nameInput.getText().trim());
            if ((data2D instanceof DataTable) && tableName.equals(data2D.getSheet())) {
                alertError(message("CannotConvertToItself") + ": " + tableName);
                return false;
            }
            if (tableData2D.exist(conn, tableName)) {
                if (onlySQL) {
                    alertWarning(message("AlreadyExisted") + ": " + tableName);
                    return true;
                } else {
                    if (PopTools.askSure(this, message("AlreadyExisted") + ": " + tableName,
                            message("SureReplaceExistedDatabaseTable"))) {
                        return dataTable.getTableData2DDefinition().deleteUserTable(conn, tableName) >= 0;
                    } else {
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
    
    public boolean makeTable() {
        try {
            tableData2D.reset();
            String tableName = DerbyBase.fixedIdentifier(nameInput.getText().trim());
            tableData2D.setTableName(tableName);
            List<String> keys = new ArrayList<>();
            String idname = null;
            if (autoRadio.isSelected()) {
                idname = tableName.replace("\"", "") + "_id";
                Data2DColumn idcolumn = new Data2DColumn(idname, ColumnDefinition.ColumnType.Long);
                idcolumn.setAuto(true).setIsPrimaryKey(true).setNotNull(true).setEditable(false);
                tableData2D.addColumn(idcolumn);
            } else {
                keys = columnsController.checkedValues();
            }
            Map<String, String> columnsMap = new HashMap<>();
            for (int index : columnIndices) {
                Data2DColumn dataColumn = data2D.getColumns().get(index);
                ColumnDefinition dbColumn = new ColumnDefinition();
                dbColumn.cloneFrom(dataColumn);
                String columeName = DerbyBase.fixedIdentifier(dataColumn.getColumnName());
                if (columeName.equalsIgnoreCase(idname)) {
                    columeName += index;
                }
                dbColumn.setColumnName(columeName);
                dbColumn.setIsPrimaryKey(keys.contains(dataColumn.getColumnName()));
                tableData2D.addColumn(dbColumn);
                columnsMap.put(dataColumn.getColumnName(), dbColumn.getColumnName());
            }
            dataTable.setColumnsMap(columnsMap);
            return true;
        } catch (Exception e) {
            if (task == null) {
                popError(e.toString());
            } else {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public boolean createTable(Connection conn) {
        try {
            dataTable.resetData();
            if (!makeTable()) {
                return false;
            }
            dataTable.setTableData2D(tableData2D);
            String sql = tableData2D.createTableStatement();
            taskController.updateLogs(sql);
            if (conn.createStatement().executeUpdate(sql) >= 0) {
                taskController.updateLogs(message("Created"));
            } else {
                taskController.updateLogs(message("Failed"));
                return false;
            }
            List<ColumnDefinition> dbColumns = tableData2D.getColumns();
            List<Data2DColumn> dataColumns = new ArrayList<>();
            if (dbColumns != null) {
                for (ColumnDefinition dbColumn : dbColumns) {
                    Data2DColumn dataColumn = new Data2DColumn();
                    dataColumn.cloneFrom(dbColumn);
                    dataColumns.add(dataColumn);
                }
            }
            dataTable.recordTable(conn, tableData2D.getTableName(), dataColumns);
            taskController.updateLogs(message("Record"));
            return true;
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
            return false;
        }
    }
    
    public boolean importData(Connection conn, List<Integer> rows) {
        try {
            conn.setAutoCommit(false);
            count = 0;
            if (rows == null || rows.isEmpty()) {
                for (List<String> pageRow : data2D.tableData()) {
                    importRow(conn, pageRow);
                }
            } else {
                for (Integer row : rows) {
                    importRow(conn, data2D.tableData().get(row));
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
    
    public void importRow(Connection conn, List<String> pageRow) {
        try {
            Data2DRow data2DRow = tableData2D.newRow();
            for (int col : columnIndices) {
                Data2DColumn sourceColumn = data2D.getColumns().get(col);
                Object value = sourceColumn.fromString(pageRow.get(col + 1));
                if (value != null) {
                    String name = sourceColumn.getColumnName();
                    if (dataTable.getColumnsMap() != null) {
                        String tableColumnName = dataTable.getColumnsMap().get(name);
                        if (tableColumnName != null) {
                            name = tableColumnName;
                        }
                    }
                    data2DRow.setColumnValue(name, value);
                }
            }
            tableData2D.insertData(conn, data2DRow);
            if (++count % DerbyBase.BatchSize == 0) {
                conn.commit();
                taskController.updateLogs(message("Imported") + ": " + count);
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
    }
    
    public boolean importAllData(Connection conn) {
        try {
            count = data2D.writeTable(task, conn, dataTable, columnIndices, false);
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
        try ( Connection conn = DerbyBase.getConnection()) {
            if (!checkOptions(conn, true) || !makeTable()) {
                return;
            }
            String sql = tableData2D.createTableStatement();
            TextPopController.loadText(this, sql);
        } catch (Exception e) {
            popError(e.toString());
        }
    }
    
    @FXML
    public void sqlLink() {
        openLink("https://db.apache.org/derby/docs/10.15/ref/crefsqlj18919.html");
    }
    
}
