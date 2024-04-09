package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DTaskTargetsController extends BaseData2DTaskController {

    protected Data2DWriter writer;

    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected ComboBox<String> colSelector;

    @Override
    public void setParameters(BaseData2DLoadController controller) {
        try {
            super.setParameters(controller);

            if (targetController != null) {
                targetController.setParameters(this, controller);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceChanged() {
        try {
            super.sourceChanged();

            if (colSelector != null) {
                colSelector.getItems().clear();
                if (data2D == null) {
                    return;
                }
                List<String> names = data2D.columnNames();
                if (names == null || names.isEmpty()) {
                    return;
                }
                String selectedCol = colSelector.getSelectionModel().getSelectedItem();
                isSettingValues = true;
                colSelector.getItems().setAll(names);
                if (selectedCol != null && names.contains(selectedCol)) {
                    colSelector.setValue(selectedCol);
                } else {
                    colSelector.getSelectionModel().select(0);
                }
                isSettingValues = false;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        if (targetController != null) {
            targetController.setNotInTable(isAllPages());
        }
    }

    @Override
    public void checkInvalidAs() {
        if (targetController != null) {
            invalidAs = targetController.invalidAs();
        } else if (zeroNonnumericRadio != null && zeroNonnumericRadio.isSelected()) {
            invalidAs = ColumnDefinition.InvalidAs.Zero;
        } else if (blankNonnumericRadio != null && blankNonnumericRadio.isSelected()) {
            invalidAs = ColumnDefinition.InvalidAs.Blank;
        } else if (skipNonnumericRadio != null && skipNonnumericRadio.isSelected()) {
            invalidAs = ColumnDefinition.InvalidAs.Skip;
        } else {
            invalidAs = ColumnDefinition.InvalidAs.Blank;
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            writer = null;
            if (targetController != null) {
                if (targetController.format == null) {
                    popError(message("SelectToHandle") + ": " + message("Target"));
                    return false;
                }
                if (isAllPages()) {
                    writer = targetController.pickWriter();
                    if (writer == null) {
                        popError(message("Invalid") + ": " + message("Target"));
                        return false;
                    }
                }
            }
            return super.checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        try {
            if (isAllPages()) {
                handleAllTask();
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void handleAllTask() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.startTask(this, filterController.filter);
                List<Data2DColumn> targetColumns = data2D.targetColumns(
                        checkedColsIndices, null,
                        rowNumberCheck != null && rowNumberCheck.isSelected(), null);
                writer.setColumns(targetColumns)
                        .setHeaderNames(Data2DColumnTools.toNames(targetColumns))
                        .setWriteHeader(colNameCheck == null || colNameCheck.isSelected());
                updateLogs(message("Columns") + ": " + writer.getHeaderNames(), true);
                return handleAllData(this, writer);
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                writer.showResult();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                closeTask();
                if (ok) {
                    if (closeAfterCheck != null && closeAfterCheck.isSelected()) {
                        close();
                    } else if (targetController != null) {
                        targetController.refreshControls();
                    }
                }
            }

        };
        start(task, false);
    }

    public boolean handleAllData(FxTask currentTask, Data2DWriter writer) {
        return false;
    }

    public void handleRowsTask() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    ok = handleRows();
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                closeTask();
                if (ok) {
                    ouputRows();
                }
            }

        };
        start(task, false);
    }

    public boolean handleRows() {
        try {
            outputData = rowsFiltered();
            return outputData != null;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public void ouputRows() {
        if (targetController == null || targetController.inTable()) {
            updateTable();
        } else {
            outputRowsToExternal();
        }
    }

    public boolean updateTable() {
        try {
            if (targetController == null || !targetController.inTable() || outputData == null) {
                return false;
            }
            int row = targetController.row();
            int col = targetController.col();
            int rowsNumber = dataController.data2D.tableRowsNumber();
            int colsNumber = dataController.data2D.tableColsNumber();
            if (row < 0 || row >= rowsNumber || col < 0 || col >= colsNumber) {
                popError(message("InvalidParameters"));
                return false;
            }
            dataController.isSettingValues = true;
            if (targetController.replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + outputData.size(), rowsNumber); r++) {
                    List<String> tableRow = dataController.tableData.get(r);
                    List<String> dataRow = outputData.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    dataController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < outputData.size(); r++) {
                    List<String> newRow = dataController.data2D.newRow();
                    List<String> dataRow = outputData.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                int index = targetController.insertRadio.isSelected() ? row : row + 1;
                dataController.tableData.addAll(index, newRows);
            }
            dataController.tableView.refresh();
            dataController.isSettingValues = false;
            dataController.tableChanged(true);
            dataController.requestMouse();
            dataController.popDone();
            if (closeAfterCheck != null && closeAfterCheck.isSelected()) {
                close();
            } else if (targetController != null) {
                targetController.refreshControls();
            }
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public void outputRowsToExternal() {
        if (targetController == null) {
            return;
        }
        if (outputData == null || outputData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        writer = targetController.pickWriter();
        if (writer == null || writer.getTargetFile() == null) {
            popError(message("InvalidParamter"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            protected DataTable dataTable = null;

            @Override
            protected boolean handle() {
                data2D.startTask(this, null);
                if (targetController.format == Data2D_Attributes.TargetType.DatabaseTable) {
                    dataTable = Data2DTableTools.importTable(this, writer.getDataName(),
                            outputColumns, outputData, invalidAs);
                    return dataTable != null;
                } else {
                    writer.setColumns(outputColumns)
                            .setHeaderNames(Data2DColumnTools.toNames(outputColumns))
                            .setWriteHeader(colNameCheck == null || colNameCheck.isSelected());
                    writer.openWriter();
                    for (List<String> row : outputData) {
                        if (!isWorking()) {
                            break;
                        }
                        writer.writeRow(row);
                    }
                    writer.closeWriter();
                    return writer.isCreated();
                }
            }

            @Override
            protected void whenSucceeded() {
                dataController.popDone();
                if (dataTable != null) {
                    Data2DManufactureController.openDef(dataTable);
                } else {
                    writer.showResult();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                closeTask();
                if (ok && closeAfterCheck != null && closeAfterCheck.isSelected()) {
                    close();
                }
            }

        };
        start(task, false);
    }

}
