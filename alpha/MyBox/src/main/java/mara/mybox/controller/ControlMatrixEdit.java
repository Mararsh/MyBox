package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataCell;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.db.table.TableDataCell;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-17
 * @License Apache License Version 2.0
 */
public class ControlMatrixEdit extends ControlMatrixEdit_Sheet {

    public ControlMatrixEdit() {
        baseTitle = message("MatrixEdit");
        dataType = DataType.Matrix;
        colPrefix = "Column";
        defaultColumnType = ColumnType.Double;
        defaultColValue = "0";
        defaultColNotNull = true;
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.removeTooltip(listSheetButton);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setManager(ControlMatricesList manager) {
        this.manager = manager;
        this.parentController = manager;
        baseTitle = manager.baseTitle;
        baseName = manager.baseName;
        setControls();
        newSheet(3, 3);
    }

    @Override
    public void initEdit() {
        try {
            super.initEdit();

            nameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    dataChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initOptions() {
        try {
            super.initOptions();

            rowsNumber = 3;
            colsNumber = 3;
            nameInput.setText(rowsNumber + "x" + colsNumber);
            makeSheet(new String[rowsNumber][colsNumber], false, false);

            autoNameCheck.setSelected(UserConfig.getBoolean(baseName + "AutoName", true));
            autoNameCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        UserConfig.setBoolean(baseName + "AutoName", newValue);
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void loadMatrix(DataDefinition matrix) {
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                task.cancel();
            }
            columns = null;
            sheetInputs = null;
            colsNumber = (int) matrix.getColsNumber();
            rowsNumber = (int) matrix.getRowsNumber();
            scale = matrix.getScale();
            isSettingValues = true;
            nameInput.setText(matrix.getDataName());
            commentsArea.setText(matrix.getComments());
            scaleSelector.setValue(matrix.getScale() + "");
            isSettingValues = false;
            if (matrix.getId() >= 0) {
                idInput.setText(matrix.getDfid() + "");
            } else {
                idInput.clear();
                loadMatrix(new double[rowsNumber][colsNumber]);
                return;
            }
            task = new SingletonTask<Void>(this) {
                private double[][] values;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection();
                             PreparedStatement query = conn.prepareStatement(TableDataCell.QeuryData)) {
                        values = new double[rowsNumber][colsNumber];
                        if (tableDataCell == null) {
                            tableDataCell = new TableDataCell();
                        }
                        query.setLong(1, matrix.getDfid());
                        ResultSet results = query.executeQuery();
                        while (results.next()) {
                            DataCell cell = tableDataCell.readData(results);
                            if (cell.getCol() < colsNumber && cell.getRow() < rowsNumber) {
                                values[(int) cell.getRow()][(int) cell.getCol()] = Double.valueOf(cell.getValue());
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.console(error);
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadMatrix(values);
                }
            };
            start(task);
        }

    }

    protected void loadMatrix(double[][] matrix) {
        String[][] values = null;
        try {
            rowsNumber = matrix.length;
            colsNumber = matrix[0].length;
            values = new String[rowsNumber][colsNumber];
            for (int row = 0; row < rowsNumber; ++row) {
                for (int col = 0; col < colsNumber; ++col) {
                    try {
                        values[row][col] = DoubleTools.format(matrix[row][col], scale);
                    } catch (Exception e) {
                        values[row][col] = defaultColValue;
                    }
                }
            }
        } catch (Exception e) {
        }
        columns = null;
        makeSheet(values, false, false);
    }

    public void loadNull() {
        try {
            idInput.clear();
            nameInput.clear();
            manager.tableView.getSelectionModel().clearSelection();
            newSheet(3, 3);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void scaleMatrix() {
        String[][] values = null;
        try {
            values = new String[rowsNumber][colsNumber];
            for (int j = 0; j < rowsNumber; ++j) {
                for (int i = 0; i < colsNumber; ++i) {
                    double d = cellDouble(j, i);
                    if (d != AppValues.InvalidDouble) {
                        values[j][i] = d + "";
                    } else {
                        values[j][i] = defaultColValue;
                    }
                }
            }
        } catch (Exception e) {
        }
        columns = null;
        makeSheet(values, true, false);
    }

    protected double[][] matrixDouble() {
        if (sheetInputs == null) {
            return null;
        }
        double[][] matrix = new double[sheetInputs.length][sheetInputs[0].length];
        for (int j = 0; j < rowsNumber; j++) {
            for (int i = 0; i < colsNumber; i++) {
                double d = cellDouble(j, i);
                if (d != AppValues.InvalidDouble) {
                    matrix[j][i] = d;
                }
            }
        }
        return matrix;
    }

    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private DataDefinition matrix;
                private long id = -1;
                private boolean notExist = false;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        matrix = new DataDefinition();
                        matrix.setColsNumber(colsNumber);
                        matrix.setRowsNumber(rowsNumber);
                        matrix.setScale(scale);
                        matrix.setDataName(nameInput.getText().trim());
                        String comments = commentsArea.getText();
                        matrix.setComments(comments == null || comments.isBlank() ? null : comments.trim());
                        matrix.setModifyTime(new Date());
                        try {
                            id = Long.parseLong(idInput.getText());
                        } catch (Exception e) {
                        }
                        if (id < 0) {
                            if (tableDataDefinition.insertData(conn, matrix) == null) {
                                return false;
                            }
                            id = tableDataDefinition.getNewID();
                            if (id < 0) {
                                return false;
                            }
                            matrix.setId(id);
                        } else {
                            matrix.setId(id);
                            if (tableDataDefinition.readData(conn, matrix) == null) {
                                notExist = true;
                                return true;
                            }
                            matrix.setId(id);
                            if (tableDataDefinition.updateData(conn, matrix) == null) {
                                return false;
                            }
                        }
                        if (tableDataCell == null) {
                            tableDataCell = new TableDataCell();
                        }
                        tableDataCell.update(conn, "DELETE FROM Data_Cell WHERE dcdid=" + id);
                        List<DataCell> data = new ArrayList<>();
                        for (int j = 0; j < rowsNumber; ++j) {
                            for (int i = 0; i < colsNumber; ++i) {
                                double d = 0d;
                                try {
                                    d = Double.parseDouble(cellString(j, i));
                                    d = DoubleTools.scale(d, scale);
                                } catch (Exception e) {
                                }
                                DataCell cell = DataCell.create()
                                        .setDfid(id).setCol(i).setRow(j).setValue(d + "");
                                data.add(cell);
                            }
                        }
                        tableDataCell.insertList(conn, data);
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (notExist) {
                        dataChangedNotify.set(false);
                        copyMatrixAction();
                        popError(message("NotExist"));
                    } else {
                        manager.loadTableData();
                        idInput.setText(id + "");
                        sheetSaved();
                        popSuccessful();
                    }
                }
            };
            start(task);
        }
    }

    @Override
    public void cleanPane() {
        try {
            manager = null;
            tableDataCell = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
