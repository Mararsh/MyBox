package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-13
 * @License Apache License Version 2.0
 */
public class Data2DConvertToDataBaseController extends BaseTaskController {

    protected TableData2D tableData2D;
    protected ControlData2DEditTable editController;
    protected DataTable dataTable;
    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected List<Data2DColumn> selectedColumns;
    protected List<Integer> selectedRowsIndices, selectedColumnsIndices;

    @FXML
    protected ControlData2DSource sourceController;
    @FXML
    protected VBox dataVBox;
    @FXML
    protected TextField nameInput;
    @FXML
    protected CheckBox importCheck, autoCheck;
    @FXML
    protected Label dataLabel, infoLabel;

    public Data2DConvertToDataBaseController() {
        TipsLabelKey = message("SqlIdentifierComments");
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    public void setParameters(ControlData2DEditTable editController) {
        try {
            dataTable = new DataTable();
            tableData2D = new TableData2D();

            this.editController = editController;
            data2D = editController.data2D;
            tableData2DDefinition = editController.tableData2DDefinition;
            tableData2DColumn = editController.tableData2DColumn;

            if (data2D.getDataName() != null) {
                nameInput.setText(data2D.getDataName() + (data2D.isTable() ? "_m" : ""));
            }

            sourceController.setParameters(this, editController);

            sourceController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            sourceController.selectNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            checkSource();

            importCheck.setSelected(UserConfig.getBoolean(baseName + "ImportData", true));
            importCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ImportData", importCheck.isSelected());
                }
            });

            autoCheck.setSelected(UserConfig.getBoolean(baseName + "AutoColumn", true));
            autoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "AutoColumn", autoCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkSource() {
        try {
            getMyStage().setTitle(editController.getTitle());
            infoLabel.setText("");

            selectedColumns = sourceController.checkedCols();
            if (selectedColumns == null || selectedColumns.isEmpty()) {
                infoLabel.setText(message("SelectToHandle"));
                return false;
            }

            if (!sourceController.allPages()) {
                selectedRowsIndices = sourceController.checkedRowsIndices();
                if (selectedRowsIndices == null || selectedRowsIndices.isEmpty()) {
                    infoLabel.setText(message("SelectToHandle"));
                    return false;
                }
            } else {
                selectedRowsIndices = null;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (nameInput.getText().isBlank()) {
                popError(message("InvalidParameters") + ": " + message("TableName"));
                return false;
            }
            infoLabel.setText("");
            if (!checkSource()) {
                return false;
            }
            if (!data2D.hasData()) {
                infoLabel.setText(message("NoData"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public void makeTable() {
        try {
            selectedColumns = sourceController.checkedCols();

            String tableName = DerbyBase.fixedIdentifier(nameInput.getText().trim());
            tableData2D.setTableName(DerbyBase.fixedIdentifier(tableName));

            boolean havePrimaryKey = false;
            for (Data2DColumn column : selectedColumns) {
                if (column.isIsPrimaryKey()) {
                    havePrimaryKey = true;
                    break;
                }
            }
            if (!havePrimaryKey) {
                selectedColumns.add(0, dataTable.idColumn());
            }
            for (Data2DColumn column : selectedColumns) {
                column.setColumnName(DerbyBase.fixedIdentifier(column.getColumnName()));
                ColumnDefinition c = new ColumnDefinition();
                c.cloneFrom(column);
                tableData2D.addColumn(column);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    @Override
    public void beforeTask() {
        try {
            dataVBox.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean doTask() {
        try ( Connection conn = DerbyBase.getConnection()) {
            dataTable.resetData();
            makeTable();
            if (tableData2D.exist(conn, tableData2D.getTableName())) {
                updateLogs(message("AlreadyExisted"));
                return false;
            }
            String sql = tableData2D.createTableStatement();
            updateLogs(sql);
            if (conn.createStatement().executeUpdate(sql) >= 0) {
                updateLogs(message("Created"));
            } else {
                updateLogs(message("Failed"));
                return false;
            }

            dataTable.recordTable(conn, tableData2D.getTableName(), selectedColumns);

            if (!importCheck.isSelected()) {
                selectedColumnsIndices = sourceController.checkedColsIndices();
                if (!sourceController.allPages() || !data2D.isMutiplePages()) {
                    selectedRowsIndices = sourceController.checkedRowsIndices();
                    List<String> names = sourceController.checkedColsNames();
                    for (Integer row : selectedRowsIndices) {
                        List<String> dataRow = editController.tableData.get(row);
                        Data2DRow data2DRow = new Data2DRow();
                        for (int i = 0; i < selectedColumnsIndices.size(); i++) {
                            data2DRow.setValue(names.get(i), dataRow.get(selectedColumnsIndices.get(i) + 1));
                        }
                        tableData2D.insertData(conn, data2DRow);
                    }

                } else {
                    data2D.setTask(task);
                    data2D.writeTable(conn, tableData2D, selectedColumnsIndices);
                    data2D.setTask(null);
                }
                conn.commit();
            }

            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        try {
            SoundTools.miao3();
            DataTablesController.open(dataTable);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterTask() {
        try {
            dataVBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void sqlAction() {
        try {
            if (!checkOptions()) {
                return;
            }
            makeTable();
            String sql = tableData2D.createTableStatement();
            TextPopController.loadText(this, sql);
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    /*
        static
     */
    public static Data2DConvertToDataBaseController open(ControlData2DEditTable tableController) {
        try {
            Data2DConvertToDataBaseController controller = (Data2DConvertToDataBaseController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DConvertToDatabaseFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
