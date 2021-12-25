package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.data.DataFile;
import mara.mybox.data.DataFileCSV;
import mara.mybox.data.DataFileExcel;
import mara.mybox.data.DataFileText;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class Data2DHandleController extends BaseChildController {

    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected List<List<String>> handledData;
    protected List<Data2DColumn> handledColumns;
    protected DataFileCSV handledCSV;
    protected DataFile handledFile;

    @FXML
    protected ToggleGroup rowGroup;
    @FXML
    protected RadioButton allRowsRadio;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;
    @FXML
    protected Label dataLabel;

    public Data2DHandleController() {
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            data2D = tableController.data2D;

            if (dataLabel != null) {
                dataLabel.setText(data2D.displayName());
            }

            if (targetController != null) {
                targetController.setParameters(this, tableController);
            }

            if (rowNumberCheck != null) {
                rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
                rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
                    }
                });
            }
            if (colNameCheck != null) {
                colNameCheck.setSelected(UserConfig.getBoolean(baseName + "CopyColNames", true));
                colNameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "CopyColNames", colNameCheck.isSelected());
                    }
                });
            }

            if (rowGroup != null) {
                rowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        checkOptions();
                    }
                });
            }

            tableController.selectNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

            tableController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

            checkOptions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkOptions() {
        if (!tableController.checkSelections(all()) || allChanged()) {
            okButton.setDisable(true);
            return false;
        }
        okButton.setDisable(false);
        return true;
    }

    @FXML
    public void selectAllRows() {
        tableController.isSettingValues = true;
        tableController.allRowsCheck.setSelected(false);
        tableController.isSettingValues = false;
        tableController.allRowsCheck.setSelected(true);
    }

    @FXML
    public void selectNoneRows() {
        tableController.isSettingValues = true;
        tableController.allRowsCheck.setSelected(true);
        tableController.isSettingValues = false;
        tableController.allRowsCheck.setSelected(false);
    }

    @FXML
    public void selectAllCols() {
        tableController.isSettingValues = true;
        tableController.columnsCheck.setSelected(false);
        tableController.isSettingValues = false;
        tableController.columnsCheck.setSelected(true);
    }

    @FXML
    public void selectNoneCols() {
        tableController.isSettingValues = true;
        tableController.columnsCheck.setSelected(true);
        tableController.isSettingValues = false;
        tableController.columnsCheck.setSelected(false);
    }

    public boolean all() {
        return allRowsRadio != null && allRowsRadio.isSelected();
    }

    public boolean allPages() {
        return all() && data2D.isMutiplePages();
    }

    public boolean allChanged() {
        return allPages() && data2D.isTableChanged();
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!checkOptions()) {
                return;
            }
            handledColumns = tableController.checkedCols();
            if (rowNumberCheck != null && rowNumberCheck.isSelected()) {
                handledColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            if (allPages()) {
                if (tableController.checkBeforeLoadingTableData()) {
                    handleFileTask();
                }
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void handleFileTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    handledCSV = generatedFile();
                    if (handledCSV == null) {
                        return false;
                    }
                    switch (targetController.target) {
                        case "csv":
                            handledFile = handledCSV;
                            break;
                        case "excel":
                            handledFile = DataFileExcel.toExcel(task, handledCSV);
                            break;
                        case "texts":
                            handledFile = DataFileText.toText(handledCSV);
                            break;
                        case "systemClipboard":
                            handledFile = handledCSV;
                            break;
                        case "myBoxClipboard":
                            handledFile = DataClipboard.create(task, handledColumns, handledCSV);
                            break;
                        default:
                            return false;
                    }
                    handledFile.setD2did(-1);
                    Data2D.save(handledFile, handledColumns);
                    return true;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                outputFile();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
            }

        };
        start(task);
    }

    public DataFileCSV generatedFile() {
        return null;
    }

    public void outputFile() {
        try {
            if (handledFile == null) {
                return;
            }
            switch (targetController.target) {
                case "csv":
                    DataFileCSVController.open(handledFile.getFile(), handledFile.getCharset(),
                            handledFile.isHasHeader(), handledFile.getDelimiter().charAt(0));
                    break;
                case "excel":
                    DataFileExcelController.open(handledFile.getFile(), handledFile.isHasHeader());
                    break;
                case "texts":
                    DataFileTextController.open(handledFile.getFile(), handledFile.getCharset(),
                            handledFile.isHasHeader(), handledFile.getDelimiter());
                    break;
                case "systemClipboard":
                    TextClipboardTools.copyToSystemClipboard(this, TextFileTools.readTexts(handledFile.getFile()));
                    break;
                case "myBoxClipboard":
                    DataInMyBoxClipboardController.open(handledFile);
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void handleRowsTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    return handleRows();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (targetController.inTable()) {
                    updateTable();
                } else {
                    outputExternal();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public boolean showColNames() {
        return colNameCheck != null && colNameCheck.isSelected();
    }

    public boolean handleRows() {
        try {
            handledData = tableController.selectedData(all(), rowNumberCheck != null && rowNumberCheck.isSelected());
            if (handledData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = tableController.checkedColsNames();
                if (rowNumberCheck != null && rowNumberCheck.isSelected()) {
                    names.add(0, message("SourceRowNumber"));
                }
                handledData.add(0, names);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean updateTable() {
        try {
            if (targetController == null || !targetController.inTable() || handledData == null) {
                return false;
            }
            int row = targetController.row();
            int col = targetController.col();
            int rowsNumber = data2D.tableRowsNumber();
            int colsNumber = data2D.tableColsNumber();
            if (row < 0 || row >= rowsNumber || col < 0 || col >= colsNumber) {
                popError(message("InvalidParameters"));
                return false;
            }
            tableController.isSettingValues = true;
            if (targetController.replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + handledData.size(), rowsNumber); r++) {
                    List<String> tableRow = tableController.tableData.get(r);
                    List<String> dataRow = handledData.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    tableController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < handledData.size(); r++) {
                    List<String> newRow = tableController.data2D.newRow();
                    List<String> dataRow = handledData.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                tableController.tableData.addAll(targetController.insertRadio.isSelected() ? row : row + 1, newRows);
            }
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean outputExternal() {
        if (targetController == null || targetController.target == null
                || handledData == null || handledData.isEmpty()) {
            popError(message("NoData"));
            return false;
        }
        switch (targetController.target) {
            case "systemClipboard":
                tableController.copyToSystemClipboard(null, handledData);
                break;
            case "myBoxClipboard":
                tableController.copyToMyBoxClipboard2(handledColumns, handledData);
                break;
            case "csv":
                DataFileCSVController.open(handledColumns, handledData);
                break;
            case "excel":
                DataFileExcelController.open(handledColumns, handledData);
                break;
            case "texts":
                DataFileTextController.open(handledColumns, handledData);
                break;
            case "matrix":
                MatricesManageController controller = MatricesManageController.oneOpen();
                controller.dataController.loadTmpData(handledColumns, handledData);
                break;
        }
        popDone();
        return true;
    }

}
