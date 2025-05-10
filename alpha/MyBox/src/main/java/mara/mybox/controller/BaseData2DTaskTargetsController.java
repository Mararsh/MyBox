package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Toggle;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
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

            sourceController.rowsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    sourceTypeChanged();
                }
            });
            sourceTypeChanged();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void dataChanged() {
        try {
            super.dataChanged();

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

    public void sourceTypeChanged() {
        if (targetController != null) {
            targetController.setNotInTable(isAllPages());
        }
    }

    @Override
    public InvalidAs checkInvalidAs() {
        if (targetController != null) {
            invalidAs = targetController.invalidAs();
        } else {
            invalidAs = super.checkInvalidAs();
        }
        return invalidAs;
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            writer = null;
            if (targetController != null) {
                if (targetController.format == null) {
                    popError(message("SelectToHandle") + ": " + message("Target"));
                    return false;
                }
                if (!targetController.inTable()) {
                    writer = targetController.pickWriter();
                    if (writer == null) {
                        popError(message("Invalid") + ": " + message("Target"));
                        return false;
                    }
                    writer.setColumns(outputColumns)
                            .setHeaderNames(Data2DColumnTools.toNames(outputColumns))
                            .setWriteHeader(colNameCheck == null || colNameCheck.isSelected());
                    writer.setInvalidAs(checkInvalidAs());
                }
            }
            return true;
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
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.startTask(this, filterController.filter);
                writer.setColumns(outputColumns)
                        .setHeaderNames(Data2DColumnTools.toNames(outputColumns))
                        .setWriteHeader(colNameCheck == null || colNameCheck.isSelected())
                        .setFormatValues(formatValuesCheck != null && formatValuesCheck.isSelected());
                taskSuccessed = handleAllData(this, writer);
                return taskSuccessed;
            }

            @Override
            protected void whenSucceeded() {
                if (writer.showResult()) {
                    popDone();
                    if (targetController != null) {
                        targetController.sourceChanged();
                    }
                } else {
                    alertInformation(message("NoData"));
                    updateLogs(message("NoData"));
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
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
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    taskSuccessed = handleRows();
                    data2D.stopFilter();
                    return taskSuccessed;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputRows();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }

        };
        start(task, false);
    }

    public boolean handleRows() {
        outputData = rowsFiltered();
        return true;
    }

    public void outputRows() {
        if (targetController == null || targetController.inTable()) {
            updateTable();
        } else {
            outputRowsToExternal();
        }
    }

    public boolean updateTable() {
        try {
            if (targetController == null || !targetController.inTable()) {
                return false;
            }
            if (outputData == null || outputData.isEmpty()) {
                alertInformation(message("NoData"));
                updateLogs(message("NoData"));
                return true;
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
            List<List<String>> tableData = new ArrayList<>();
            tableData.addAll(dataController.tableData);
            if (targetController.replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + outputData.size(), rowsNumber); r++) {
                    List<String> tableRow = dataController.data2D.pageRow(r, true);
                    List<String> dataRow = outputData.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    tableData.set(r, tableRow);
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
                tableData.addAll(index, newRows);
            }
            dataController.isSettingValues = false;
            dataController.updateTable(tableData);
            dataController.tableChanged(true);
            dataController.requestMouse();
            dataController.popDone();
            if (targetController != null) {
                targetController.sourceChanged();
            }
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public void outputRowsToExternal() {
        if (writer == null) {
            return;
        }
        if (outputData == null || outputData.isEmpty()) {
            alertInformation(message("NoData"));
            updateLogs(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.startTask(this, null);
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
                taskSuccessed = writer.isCompleted();
                return taskSuccessed;
            }

            @Override
            protected void whenSucceeded() {
                if (writer.showResult()) {
                    popDone();
                    if (dataController != null) {
                        dataController.popDone();
                    }
                } else {
                    alertInformation(message("NoData"));
                    updateLogs(message("NoData"));
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }

        };
        start(task, false);
    }

}
