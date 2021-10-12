package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.db.data.Matrix;
import mara.mybox.db.data.MatrixCell;
import mara.mybox.db.table.TableMatrixCell;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
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
        tableMatrix = manager.tableDefinition;
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
                    sheetChanged(true);
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
            makeSheet(new String[rowsNumber][colsNumber], false);

            autoNameCheck.setSelected(UserConfig.getBoolean(baseName + "AutoName", true));
            autoNameCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        UserConfig.setBoolean(baseName + "AutoName", newValue);
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void loadMatrix(Matrix matrix) {
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                task.cancel();
            }
            columns = null;
            sheetInputs = null;
            colsNumber = matrix.getColsNumber();
            rowsNumber = matrix.getRowsNumber();
            scale = matrix.getScale();
            isSettingValues = true;
            nameInput.setText(matrix.getName());
            commentsArea.setText(matrix.getComments());
            scaleSelector.setValue(matrix.getScale() + "");
            isSettingValues = false;
            if (matrix.getId() >= 0) {
                idInput.setText(matrix.getId() + "");
            } else {
                idInput.clear();
                loadMatrix(new double[rowsNumber][colsNumber]);
                return;
            }
            task = new SingletonTask<Void>() {
                private double[][] values;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        values = new double[rowsNumber][colsNumber];
                        if (tableMatrixCell == null) {
                            tableMatrixCell = new TableMatrixCell();
                        }
                        List<MatrixCell> data = tableMatrixCell.query(conn,
                                "SELECT * FROM " + tableMatrixCell.getTableName() + " WHERE mcxid=" + matrix.getId());
                        for (MatrixCell cell : data) {
                            if (cell.getCol() < colsNumber && cell.getRow() < rowsNumber) {
                                values[cell.getRow()][cell.getCol()] = cell.getValue();
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
        makeSheet(values, false);
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
        makeSheet(values, true);
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
            task = new SingletonTask<Void>() {

                private Matrix matrix;
                private long id = -1;
                private boolean notExist = false;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        matrix = new Matrix();
                        matrix.setColsNumber(colsNumber);
                        matrix.setRowsNumber(rowsNumber);
                        matrix.setScale(scale);
                        matrix.setName(nameInput.getText().trim());
                        String comments = commentsArea.getText();
                        matrix.setComments(comments == null || comments.isBlank() ? null : comments.trim());
                        matrix.setModifyTime(new Date());
                        try {
                            id = Long.parseLong(idInput.getText());
                        } catch (Exception e) {
                        }
                        if (id < 0) {
                            if (tableMatrix.insertData(conn, matrix) == null) {
                                return false;
                            }
                            id = tableMatrix.getNewID();
                            if (id < 0) {
                                return false;
                            }
                            matrix.setId(id);
                        } else {
                            matrix.setId(id);
                            if (tableMatrix.readData(conn, matrix) == null) {
                                notExist = true;
                                return true;
                            }
                            matrix.setId(id);
                            if (tableMatrix.updateData(conn, matrix) == null) {
                                return false;
                            }
                        }
                        if (tableMatrixCell == null) {
                            tableMatrixCell = new TableMatrixCell();
                        }
                        tableMatrixCell.update(conn, "DELETE FROM " + tableMatrixCell.getTableName() + " WHERE mcxid=" + id);
                        List<MatrixCell> data = new ArrayList<>();
                        for (int j = 0; j < rowsNumber; ++j) {
                            for (int i = 0; i < colsNumber; ++i) {
                                double d = 0d;
                                try {
                                    d = Double.parseDouble(cellString(j, i));
                                    d = DoubleTools.scale(d, scale);
                                } catch (Exception e) {
                                }
                                MatrixCell cell = MatrixCell.create()
                                        .setMcxid(id).setCol(i).setRow(j).setValue(d);
                                data.add(cell);
                            }
                        }
                        tableMatrixCell.insertList(conn, data);
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
            tableMatrixCell = null;
            tableMatrix = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
