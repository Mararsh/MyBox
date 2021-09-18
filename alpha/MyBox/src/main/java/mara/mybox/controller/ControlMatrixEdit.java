package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.db.data.Matrix;
import mara.mybox.db.data.MatrixCell;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableMatrixCell;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
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
public class ControlMatrixEdit extends ControlSheet {

    protected ControlMatricesList manager;
    protected TableMatrixCell tableMatrixCell;
    protected BaseTable tableMatrix;

    @FXML
    protected TextField nameInput, idInput;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected CheckBox autoNameCheck;
    @FXML
    protected Button listSheetButton;

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

    public void setParent(ControlMatricesList manager) {
        this.manager = manager;
        this.parentController = manager;
        baseName = manager.baseName;
        tableMatrix = manager.tableDefinition;
        setControls();

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

    @Override
    protected String titleName() {
        return nameInput.getText();
    }

    protected String matrixName() {
        return nameInput.getText().trim();
    }

    public void loadMatrix(Matrix matrix) {
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
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
//            start(task);
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
        makeSheet(values, false);
    }

    protected double[][] matrix() {
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

    protected double cellDouble(int row, int col) {
        try {
            double d = Double.parseDouble(cellString(row, col));
            d = DoubleTools.scale(d, scale);
            return d;
        } catch (Exception e) {
            return AppValues.InvalidDouble;
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
        makeSheet(values);
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            idInput.clear();
            manager.tableView.getSelectionModel().clearSelection();
            dataChangedNotify.set(false);
            popInformation(message("NewMatrix"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
                    manager.loadTableData();
                    idInput.setText(id + "");
                    sheetSaved();
                    popSuccessful();
                }
            };
            start(task);
        }

    }

    @FXML
    public void copyMatrixAction() {
        createAction();
        nameInput.setText(nameInput.getText() + " " + message("Copy"));
    }

    public void normalization() {
        double sum = 0d;
        for (int j = 0; j < rowsNumber; ++j) {
            for (int i = 0; i < colsNumber; ++i) {
                double d = cellDouble(j, i);
                if (d == AppValues.InvalidDouble) {
                    sheetInputs[j][i].setStyle(NodeStyleTools.badStyle);
                    popError(message("InvalidData"));
                    return;
                }
                sum += d;
            }
        }
        if (sum == 0) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < rowsNumber; ++j) {
            for (int i = 0; i < colsNumber; ++i) {
                double d = cellDouble(j, i);
                sheetInputs[j][i].setText(DoubleTools.format(d / sum, scale));
            }
        }
        isSettingValues = false;
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + " " + message("Normalization"));
        }
        sheetChanged();
    }

    public void gaussianDistribution() {
        isSettingValues = true;
        float[][] m = ConvolutionKernel.makeGaussMatrix(rowsNumber / 2);
        for (int j = 0; j < rowsNumber; ++j) {
            for (int i = 0; i < colsNumber; ++i) {
                sheetInputs[j][i].setText(DoubleTools.format(m[j][i], scale));
            }
        }
        isSettingValues = false;
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + " " + message("GaussianDistribution"));
        }
        sheetChanged();
    }

    public void identifyMatrix() {
        isSettingValues = true;
        for (int j = 0; j < rowsNumber; ++j) {
            for (int i = 0; i < colsNumber; ++i) {
                if (i == j) {
                    sheetInputs[j][i].setText(1d + "");
                } else {
                    sheetInputs[j][i].setText(0d + "");
                }
            }
        }
        isSettingValues = false;
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + " " + message("IdentifyMatrix"));
        }
        sheetChanged();
    }

    public void upperTriangleMatrix() {
        isSettingValues = true;
        for (int j = 0; j < rowsNumber; ++j) {
            for (int i = 0; i < colsNumber; ++i) {
                if (i < j) {
                    sheetInputs[j][i].setText(0d + "");
                }
            }
        }
        isSettingValues = false;
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + " " + message("UpperTriangle"));
        }
        sheetChanged();
    }

    public void lowerTriangleMatrix() {
        isSettingValues = true;
        for (int j = 0; j < rowsNumber; ++j) {
            for (int i = 0; i < colsNumber; ++i) {
                if (i > j) {
                    sheetInputs[j][i].setText(0d + "");
                }
            }
        }
        isSettingValues = false;
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + " " + message("LowerTriangle"));
        }
        sheetChanged();
    }

    @FXML
    public void popSetMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Normalization"));
            menu.setOnAction((ActionEvent event) -> {
                normalization();
            });
            menu.setDisable(sheetInputs == null);
            items.add(menu);

            menu = new MenuItem(message("GaussianDistribution"));
            menu.setOnAction((ActionEvent event) -> {
                gaussianDistribution();
            });
            menu.setDisable(sheetInputs == null || colsNumber != rowsNumber || colsNumber < 3);
            items.add(menu);

            menu = new MenuItem(message("SetAsIdentifyMatrix"));
            menu.setOnAction((ActionEvent event) -> {
                identifyMatrix();
            });
            menu.setDisable(sheetInputs == null || colsNumber != rowsNumber);
            items.add(menu);

            menu = new MenuItem(message("SetAsUpperTriangleMatrix"));
            menu.setOnAction((ActionEvent event) -> {
                upperTriangleMatrix();
            });
            menu.setDisable(sheetInputs == null || colsNumber != rowsNumber);
            items.add(menu);

            menu = new MenuItem(message("SetAsLowerTriangleMatrix"));
            menu.setOnAction((ActionEvent event) -> {
                lowerTriangleMatrix();
            });
            menu.setDisable(sheetInputs == null || colsNumber != rowsNumber);
            items.add(menu);

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);
            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void copyCols(List<Integer> cols, boolean withNames, boolean toSystemClipboard) {
        copyRowsCols(rowsIndex(true), cols, withNames, toSystemClipboard);
    }

    @Override
    public void setCols(List<Integer> cols, String value) {
        setRowsCols(rowsIndex(true), cols, value);
    }

    @Override
    public void sort(int col, boolean asc) {
        sortRows(rowsIndex(true), col, asc);
    }

    @Override
    public void pasteFile(ControlSheetCSV sourceController, int row, int col, boolean enlarge) {

    }

    @Override
    public boolean exportCols(SheetExportController exportController, List<Integer> cols) {
        return false;
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
