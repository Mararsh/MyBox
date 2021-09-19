package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableMatrixCell;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-17
 * @License Apache License Version 2.0
 */
public abstract class ControlMatrixEdit_Sheet extends ControlSheet {

    protected ControlMatricesList manager;
    protected TableMatrixCell tableMatrixCell;
    protected BaseTable tableMatrix;
    protected boolean isMainMatrix;

    @FXML
    protected TextField nameInput, idInput;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected CheckBox autoNameCheck;
    @FXML
    protected Button listSheetButton;

    @Override
    protected String titleName() {
        return nameInput.getText();
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

    @Override
    public synchronized void sheetChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        super.sheetChanged(changed);
        updateStatus();
    }

    protected void updateStatus() {
        if (!isMainMatrix || getMyStage() == null) {
            return;
        }
        String title = manager.baseTitle;
        String name = nameInput.getText();
        if (name != null && !name.isBlank()) {
            title += " - " + name.trim();
        }
        if (dataChangedNotify.get()) {
            title += " *";
        }
        myStage.setTitle(title);
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            idInput.clear();
            nameInput.clear();
            manager.tableView.getSelectionModel().clearSelection();
            super.createAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void newSheet(int rows, int cols) {
        try {
            super.newSheet(rows, cols);
            if (autoNameCheck.isSelected()) {
                nameInput.setText(rows + "x" + cols + "_" + new Date().getTime());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void copyMatrixAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            idInput.clear();
            nameInput.setText(nameInput.getText() + "_" + message("Copy"));
            manager.tableView.getSelectionModel().clearSelection();
            sheetChanged(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
            nameInput.setText(rowsNumber + "x" + colsNumber + "_" + message("Normalization"));
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
        sheetChanged();
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + "_" + message("GaussianDistribution"));
        }
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
        sheetChanged();
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + "_" + message("IdentifyMatrix"));
        }
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
        sheetChanged();
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + "_" + message("UpperTriangle"));
        }
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
        sheetChanged();
        if (autoNameCheck.isSelected()) {
            nameInput.setText(rowsNumber + "x" + colsNumber + "_" + message("LowerTriangle"));
        }
    }

    @Override
    protected void addRows(int row, boolean above, int number) {
        if (number < 1 || columns == null || columns.isEmpty()) {
            return;
        }
        if (autoNameCheck.isSelected()) {
            nameInput.clear();
        }
        super.addRows(row, above, number);
    }

    @Override
    public void deleteRows(List<Integer> rows) {
        if (rows == null || rows.isEmpty()) {
            popError(message("NoSelection"));
            return;
        }
        if (rowsCheck == null || columns == null || columns.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (autoNameCheck.isSelected()) {
            nameInput.clear();
        }
        super.deleteRows(rows);
    }

    @Override
    public void deletePageRows() {
        if (rowsCheck == null || columns == null || columns.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (autoNameCheck.isSelected()) {
            nameInput.clear();
        }
        super.deletePageRows();
    }

    @Override
    public void deleteAllRows() {
        if (autoNameCheck.isSelected()) {
            nameInput.clear();
        }
        super.deleteAllRows();
    }

    @Override
    protected void addCols(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (autoNameCheck.isSelected()) {
            nameInput.clear();
        }
        super.addCols(col, left, number);
    }

    @Override
    public void deleteCols(List<Integer> cols) {
        if (cols == null || cols.isEmpty() || columns == null || columns.isEmpty()) {
            popError(message("NoSelection"));
            return;
        }
        if (autoNameCheck.isSelected()) {
            nameInput.clear();
        }
        super.deleteCols(cols);
    }

    @Override
    public void deleteAllCols() {
        if (autoNameCheck.isSelected()) {
            nameInput.clear();
        }
        super.deleteAllCols();
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
        pastePage(sourceController, row, col, enlarge);
    }

    @Override
    public boolean exportCols(SheetExportController exportController, List<Integer> cols) {
        return exportRowsCols(exportController, rowsIndex(true), cols);
    }

}
