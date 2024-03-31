package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.DataSort;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.DataTableGroup;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.writer.ListWriter;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DTaskController extends BaseTaskController {

    protected BaseData2DLoadController dataController;
    protected Data2D data2D;
    protected List<Integer> checkedColsIndices, otherColsIndices, filteredRowsIndices;
    protected List<String> checkedColsNames, otherColsNames;
    protected List<Data2DColumn> checkedColumns, otherColumns;
    protected boolean idExclude = false, noCheckedColumnsMeansAll = true;
    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected int scale, defaultScale = 2, maxData = -1;
    protected ObjectType objectType;
    protected InvalidAs invalidAs = InvalidAs.Skip;
    protected List<Integer> dataColsIndices;
    protected List<String> orders;
    protected ChangeListener<Boolean> tableLoadListener, tableStatusListener;

    @FXML
    protected Tab sourceTab, filterTab, optionsTab, groupTab, outputsTab;
    @FXML
    protected BaseData2DSourceRowsController sourceController;
    @FXML
    protected ControlData2DRowFilter filterController;
    @FXML
    protected ControlData2DGroup groupController;
    @FXML
    protected ControlSelection sortController;
    @FXML
    protected Label infoLabel, dataSelectionLabel;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected ToggleGroup objectGroup;
    @FXML
    protected TextField maxInput;
    @FXML
    protected RadioButton columnsRadio, rowsRadio, allRadio,
            skipNonnumericRadio, zeroNonnumericRadio, blankNonnumericRadio;
    @FXML
    protected CheckBox rowNumberCheck, colNameCheck;
    @FXML
    protected FlowPane columnsPane, otherColumnsPane;
    @FXML
    protected CheckBox formatValuesCheck;

    public BaseData2DTaskController() {
        baseTitle = message("Handle");
    }

    /*
        controls
     */
    public void setParameters(BaseData2DLoadController controller) {
        try {
            dataController = controller;

            initSource();

            filterController.setParameters(this);

            if (groupController != null) {
                groupController.setParameters(this);
            }

            if (sortController != null) {
                sortController.setParameters(this, message("Sort"), message("DataSortLabel"));
            }

            initOptions();

            sourceLoaded();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        source
     */
    public void initSource() {
        try {
            sourceController.setParameters(this, dataController);

            tableLoadListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceLoaded();
                }
            };
            dataController.loadedNotify.addListener(tableLoadListener);

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceChanged();
                }
            };
            dataController.statusNotify.addListener(tableStatusListener);

            sourceController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void sourceLoaded() {
        sourceChanged();
    }

    public void sourceChanged() {
        try {
            data2D = dataController.data2D.cloneAll();

            if (groupController != null) {
                groupController.refreshControls();
            }
            sourceController.sourceChanged(data2D);

            filterController.setData2D(data2D);

            makeSortList();
            isSettingValues = true;
            if (columnsPane != null) {
                columnsPane.getChildren().clear();
                List<String> names = data2D.columnNames();
                if (names != null) {
                    for (String name : names) {
                        columnsPane.getChildren().add(new CheckBox(name));
                    }
                }
            }

            if (otherColumnsPane != null) {
                otherColumnsPane.getChildren().clear();
                List<String> names = data2D.columnNames();
                if (names != null) {
                    for (String name : names) {
                        otherColumnsPane.getChildren().add(new CheckBox(name));
                    }
                }
            }
            isSettingValues = false;
            if (checkedColsIndices != null && !checkedColsIndices.isEmpty()
                    && checkedColsIndices.size() != data2D.getColumns().size()) {
                if (columnsPane != null) {
                    for (Node node : columnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
                        int col = data2D.colOrder(cb.getText());
                        cb.setSelected(col >= 0 && checkedColsIndices.contains(col));
                    }
                }
            } else {
                selectNoneColumn();
            }

            if (otherColumnsPane != null) {
                if (otherColsIndices != null && !otherColsIndices.isEmpty()
                        && otherColsIndices.size() != data2D.getColumns().size()) {
                    for (Node node : otherColumnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
                        int col = data2D.colOrder(cb.getText());
                        cb.setSelected(col >= 0 && otherColsIndices.contains(col));
                    }
                } else {
                    selectNoneOtherColumn();
                }
            }

            checkOptions();
            getMyStage().setTitle(baseTitle
                    + (data2D == null ? "" : " - " + data2D.displayName()));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        data
     */
    public boolean checkSelections() {
        return checkRowFilter() && sourceController.checkedRows() && checkColumns();
    }

    public boolean isAllPages() {
        return sourceController.isAllPages();
    }

    private boolean checkRowFilter() {
        if (filterController != null
                && !filterController.checkExpression(isAllPages())) {
            String ferror = filterController.error;
            if (ferror != null && !ferror.isBlank()) {
                if (filterTab != null) {
                    tabPane.getSelectionModel().select(filterTab);
                }
                alertError(ferror);
            }
            return false;
        } else {
            return true;
        }
    }

    public List<Integer> filteredRowsIndices() {
        try {
            DataFilter filter = data2D.filter;
            if (filter == null || !filter.needFilter()
                    || sourceController.selectedRowsIndices == null
                    || sourceController.selectedRowsIndices.isEmpty()) {
                return sourceController.selectedRowsIndices;
            }
            filteredRowsIndices = new ArrayList<>();
            int size = sourceController.tableData.size();
            for (int row : sourceController.selectedRowsIndices) {
                if (row < 0 || row >= size
                        || !filter.filterTableRow(data2D, sourceController.tableData.get(row), row)) {
                    continue;
                }
                if (filter.reachMaxPassed()) {
                    break;
                }
                filteredRowsIndices.add(row);
            }
            return filteredRowsIndices;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<List<String>> tableFiltered(boolean showRowNumber) {
        return tableFiltered(checkedColsIndices, showRowNumber);
    }

    public List<List<String>> tableFiltered(List<Integer> cols, boolean showRowNumber) {
        return tableFiltered(sourceController.selectedRowsIndices, cols, showRowNumber);
    }

    public List<List<String>> tableFiltered(List<Integer> rows, List<Integer> cols, boolean showRowNumber) {
        try {
            if (rows == null || rows.isEmpty()
                    || cols == null || cols.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            int size = sourceController.tableData.size();
            filteredRowsIndices = new ArrayList<>();
            data2D.resetFilterNumber();
            for (int row : rows) {
                if (row < 0 || row >= size) {
                    continue;
                }
                List<String> tableRow = sourceController.tableData.get(row);
                if (!data2D.filterTableRow(tableRow, row)) {
                    continue;
                }
                if (data2D.filterReachMaxPassed()) {
                    break;
                }

                List<String> newRow = new ArrayList<>();
                if (showRowNumber) {
                    if (data2D.isTmpData()) {
                        newRow.add((row + 1) + "");
                    } else {
                        newRow.add(tableRow.get(0) + "");
                    }
                }
                for (int col : cols) {
                    int index = col + 1;
                    if (index < 0 || index >= tableRow.size()) {
                        continue;
                    }
                    String v = tableRow.get(index);
                    if (v != null && formatValuesCheck != null && formatValuesCheck.isSelected()) {
                        v = data2D.column(col).format(v);
                    }
                    newRow.add(v);
                }
                data.add(newRow);
                filteredRowsIndices.add(row);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<List<String>> selectedData(FxTask task) {
        try {
            if (data2D == null || checkedColsIndices == null) {
                return null;
            }
            data2D.startFilter(filterController != null ? filterController.filter : null);
            if (!data2D.fillFilterStatistic()) {
                return null;
            }
            List<List<String>> data;
            if (isAllPages()) {
                ListWriter writer = new ListWriter();
                List<Data2DColumn> targetColumns = data2D.targetColumns(checkedColsIndices, false);
                writer.setColumns(targetColumns)
                        .setHeaderNames(Data2DColumnTools.toNames(targetColumns))
                        .setWriteHeader(true);
                data2D.copy(task, writer, checkedColsIndices, false,
                        formatValuesCheck != null && formatValuesCheck.isSelected(),
                        InvalidAs.Blank);
                data = writer.getRows();
            } else {
                data = tableFiltered(false);
            }
            data2D.stopTask();
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
    }

    /*
        options
     */
    public void initOptions() {
        try {

            objectType = ObjectType.Columns;
            if (objectGroup != null) {
                objectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        objectChanged();
                    }
                });
                objectChanged();
            }

            scale = (short) UserConfig.getInt(baseName + "Scale", defaultScale);
            if (scale < 0) {
                scale = defaultScale;
            }
            if (scaleSelector != null) {
                scaleSelector.getItems().addAll(
                        Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
                );
                scaleSelector.setValue(scale + "");
                scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        scaleChanged();
                    }
                });
            }

            maxData = UserConfig.getInt(baseName + "MaxDataNumber", -1);
            if (maxInput != null) {
                if (maxData > 0) {
                    maxInput.setText(maxData + "");
                }
                maxInput.setStyle(null);
                maxInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        String maxs = maxInput.getText();
                        if (maxs == null || maxs.isBlank()) {
                            maxData = -1;
                            maxInput.setStyle(null);
                            UserConfig.setLong(baseName + "MaxDataNumber", -1);
                        } else {
                            try {
                                maxData = Integer.parseInt(maxs);
                                maxInput.setStyle(null);
                                UserConfig.setLong(baseName + "MaxDataNumber", maxData);
                            } catch (Exception e) {
                                maxInput.setStyle(UserConfig.badStyle());
                            }
                        }
                    }
                });
            }

            if (rowNumberCheck != null) {
                rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
                rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        rowNumberCheckChanged();
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        return checkParameters() && initData();
    }

    // Check when selections are changed
    public boolean checkParameters() {
        try {
            if (isSettingValues) {
                return true;
            }
            if (data2D == null || !data2D.isValidDefinition()) {
                popError(message("NoData"));
                return false;
            }
            return checkSelections();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    // Check when "OK"/"Start" button is clicked
    public boolean initData() {
        try {
            if (groupController != null && !groupController.pickValues()) {
                return false;
            }

            checkObject();
            checkInvalidAs();

            if (sortController != null) {
                orders = sortController.selectedNames();
            } else {
                orders = null;
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }


    /*
        status
     */
    public boolean scaleChanged() {
        try {
            int v = Integer.parseInt(scaleSelector.getValue());
            if (v >= 0 && v <= 15) {
                scale = (short) v;
                UserConfig.setInt(baseName + "Scale", v);
                scaleSelector.getEditor().setStyle(null);
                return true;
            } else {
                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
        }
        return false;
    }

    public void makeSortList() {
        try {
            if (sortController == null) {
                return;
            }
            if (!data2D.isValidDefinition()) {
                sortController.loadNames(null);
                return;
            }
            List<String> names = new ArrayList<>();
            for (String name : data2D.columnNames()) {
                names.add(name + "-" + message("Descending"));
                names.add(name + "-" + message("Ascending"));
            }
            sortController.loadNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void objectChanged() {
        checkObject();
    }

    public void checkObject() {
        if (rowsRadio == null) {
            return;
        }
        if (rowsRadio.isSelected()) {
            objectType = ObjectType.Rows;
        } else if (allRadio != null && allRadio.isSelected()) {
            objectType = ObjectType.All;
        } else {
            objectType = ObjectType.Columns;
        }
    }

    public void checkInvalidAs() {
        if (zeroNonnumericRadio != null && zeroNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Zero;
        } else if (blankNonnumericRadio != null && blankNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Blank;
        } else if (skipNonnumericRadio != null && skipNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Skip;
        } else {
            invalidAs = InvalidAs.Blank;
        }
    }

    public boolean showColNames() {
        return colNameCheck == null || colNameCheck.isSelected();
    }

    public boolean showRowNumber() {
        return rowNumberCheck != null && rowNumberCheck.isSelected();
    }

    public void rowNumberCheckChanged() {
        UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
    }

    /*
        columns
     */
    // If none selected then select all
    public boolean checkColumns() {
        try {
            checkedColsIndices = new ArrayList<>();
            checkedColsNames = new ArrayList<>();
            checkedColumns = new ArrayList<>();
            otherColsIndices = new ArrayList<>();
            otherColsNames = new ArrayList<>();
            otherColumns = new ArrayList<>();
            List<Integer> allIndices = new ArrayList<>();
            List<String> allNames = new ArrayList<>();
            List<Data2DColumn> allCols = new ArrayList<>();
            boolean needSelection = false;
            if (columnsPane != null) {
                needSelection = true;
                for (Node node : columnsPane.getChildren()) {
                    CheckBox cb = (CheckBox) node;
                    String name = cb.getText();
                    int col = data2D.colOrder(name);
                    if (col >= 0) {
                        allIndices.add(col);
                        allNames.add(name);
                        Data2DColumn dcol = data2D.getColumns().get(col).cloneAll();
                        allCols.add(dcol);
                        if (cb.isSelected()) {
                            checkedColsIndices.add(col);
                            checkedColsNames.add(name);
                            checkedColumns.add(dcol);
                        }
                    }
                }
            }

            if (noCheckedColumnsMeansAll && checkedColsIndices.isEmpty()) {
                checkedColsIndices = allIndices;
                checkedColsNames = allNames;
                checkedColumns = allCols;
            }
            if (needSelection && checkedColsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("Columns"));
                return false;
            }
            if (otherColumnsPane != null) {
                for (Node node : otherColumnsPane.getChildren()) {
                    CheckBox cb = (CheckBox) node;
                    String name = cb.getText();
                    int col = data2D.colOrder(name);
                    if (col >= 0) {
                        Data2DColumn dcol = data2D.getColumns().get(col).cloneAll();
                        if (cb.isSelected()) {
                            otherColsIndices.add(col);
                            otherColsNames.add(name);
                            otherColumns.add(dcol);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void selectColumns(List<String> names) {
        try {
            selectNoneColumn();
            if (names == null || names.isEmpty()) {
                return;
            }
            if (columnsPane != null) {
                for (Node node : columnsPane.getChildren()) {
                    CheckBox cb = (CheckBox) node;
                    if (names.contains(cb.getText())) {
                        cb.setSelected(true);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void idExclude(boolean idExclude) {
        this.idExclude = idExclude;
    }

    @FXML
    public void selectAllColumns() {
        setColumnsSelected(true);
    }

    @FXML
    public void selectNoneColumn() {
        setColumnsSelected(false);
    }

    public void setColumnsSelected(boolean select) {
        try {
            if (columnsPane != null) {
                isSettingValues = true;
                for (Node node : columnsPane.getChildren()) {
                    CheckBox cb = (CheckBox) node;
                    cb.setSelected(select);
                }
                isSettingValues = false;
                columnSelected();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectAllOtherColumns() {
        setOtherColumnsSelected(true);
    }

    @FXML
    public void selectNoneOtherColumn() {
        setOtherColumnsSelected(false);
    }

    public void setOtherColumnsSelected(boolean select) {
        try {
            if (otherColumnsPane == null) {
                return;
            }
            isSettingValues = true;
            for (Node node : otherColumnsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(select);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void columnSelected() {
    }

    public boolean isSquare() {
        return sourceController.selectedRowsIndices != null && checkedColsIndices != null
                && !sourceController.selectedRowsIndices.isEmpty()
                && sourceController.selectedRowsIndices.size() == checkedColsIndices.size();
    }

    /*
        run
     */
    @Override
    public void startTask() {
        showRightPane();
        startTime = new Date();
        preprocessStatistic();
    }

    public void preprocessStatistic() {
        List<String> scripts = new ArrayList<>();
        String filterScript = data2D.filterScipt();
        boolean hasFilterScript = filterScript != null && !filterScript.isBlank();
        if (hasFilterScript) {
            scripts.add(filterScript);
        }
        List<String> groupScripts = null;
        if (groupController != null) {
            groupScripts = groupController.scripts();
            if (groupScripts != null) {
                scripts.addAll(groupScripts);
            }
        }
        boolean hasGroupScripts = groupScripts != null && !groupScripts.isEmpty();
        if (scripts.isEmpty()) {
            startOperation();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.setTask(this);
                List<String> filledScripts = data2D.calculateScriptsStatistic(scripts);
                if (filledScripts == null || filledScripts.size() != scripts.size()) {
                    return true;
                }
                if (hasFilterScript) {
                    data2D.filter.setFilledScript(filledScripts.get(0));
                }
                if (hasGroupScripts) {
                    if (hasFilterScript) {
                        groupController.fillScripts(filledScripts.subList(1, filledScripts.size()));
                    } else {
                        groupController.fillScripts(filledScripts);
                    }
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void whenCanceled() {
                taskCanceled();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                if (ok) {
                    startOperation();
                } else {
                    closeTask();
                }
            }

        };
        start(task, false, null);
    }

    protected void startOperation() {
    }

    public List<List<String>> filteredData(List<Integer> colIndices, boolean needRowNumber) {
        try {
            data2D.startTask(data2D.getTask(), filterController.filter);
            if (isAllPages()) {
                outputData = data2D.allRows(colIndices, needRowNumber);
            } else {
                outputData = tableFiltered(colIndices, needRowNumber);
            }
            data2D.stopFilter();
            if (outputData != null) {
                outputColumns = data2D.makeColumns(colIndices, needRowNumber);
            } else {
                outputColumns = null;
            }
            return outputData;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<String> sortNames() {
        return DataSort.parseNames(orders);
    }

    public List<Integer> sortIndices() {
        try {
            if (orders == null || orders.isEmpty()) {
                return null;
            }
            List<Integer> cols = new ArrayList<>();
            for (String name : sortNames()) {
                int col = data2D.colOrder(name);
                if (!cols.contains(col)) {
                    cols.add(col);
                }
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataFileCSV sortedFile(String dname, List<Integer> colIndices, boolean needRowNumber) {
        try {
            TmpTable tmpTable = tmpTable(dname, colIndices, needRowNumber);
            if (tmpTable == null) {
                return null;
            }
            DataFileCSV csvData = tmpTable.sort(maxData);
            tmpTable.drop();
            return csvData;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    public List<List<String>> sortPage(List<Integer> colIndices, boolean needRowNumber) {
        try {
            if (data2D == null) {
                return null;
            }
            if (maxData <= 0 && (sortController == null || orders == null || orders.isEmpty())) {
                return filteredData(colIndices, needRowNumber);
            }
            DataFileCSV csvData = sortedFile(data2D.dataName(), colIndices, needRowNumber);
            if (csvData == null) {
                return null;
            }
            outputData = csvData.allRows(false);
            if (showColNames()) {
                outputData.add(0, csvData.columnNames());
            }
            FileDeleteTools.delete(data2D.getTask(), csvData.getFile());
            outputColumns = csvData.columns;
            return outputData;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    public TmpTable tmpTable(String dname, List<Integer> colIndices, boolean needRowNumber) {
        try {
            FxTask data2DTask = data2D.getTask();
            Data2D tmp2D = data2D.cloneAll();
            tmp2D.startTask(data2DTask, filterController.filter);
            if (data2DTask != null) {
                data2DTask.setInfo(message("Filter") + "...");
            }
            TmpTable tmpTable = new TmpTable()
                    .setSourceData(tmp2D)
                    .setTargetName(dname)
                    .setSourcePickIndice(colIndices)
                    .setImportData(true)
                    .setForStatistic(false)
                    .setOrders(orders)
                    .setIncludeColName(false)
                    .setIncludeRowNumber(needRowNumber)
                    .setInvalidAs(invalidAs);
            if (groupController != null) {
                if (groupController.byValueRange()) {
                    tmpTable.setGroupRangleColumnName(groupController.groupName());
                } else if (groupController.byEqualValues()) {
                    tmpTable.setGroupEqualColumnNames(groupController.groupNames());
                } else if (groupController.byTime()) {
                    tmpTable.setGroupTimeColumnName(groupController.timeName());
                    tmpTable.setGroupTimeType(groupController.timeType());
                } else if (groupController.byExpression()) {
                    tmpTable.setGroupExpression(groupController.filledExpression);
                }
            }
            tmpTable.setTask(data2DTask);
            if (!isAllPages()) {
                outputData = tableFiltered(data2D.columnIndices(), needRowNumber);
                if (outputData == null || outputData.isEmpty()) {
                    return null;
                }
                tmpTable.setImportRows(outputData);
            }
            if (!tmpTable.createTable()) {
                tmpTable = null;
            }
            tmp2D.stopFilter();
            return tmpTable;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    public DataTableGroup groupData(DataTableGroup.TargetType targetType,
            List<Integer> colIndices, boolean needRowNumber, long max, int dscale) {
        try {
            if (groupController == null) {
                return null;
            }
            TmpTable tmpTable = tmpTable(data2D.getDataName(), data2D.columnIndices(), true);
            if (tmpTable == null) {
                return null;
            }
            displayInfo(message("GroupBy") + "...");
            DataTableGroup group = new DataTableGroup(data2D, groupController, tmpTable)
                    .setOrders(orders).setMax(max)
                    .setSourcePickIndice(colIndices)
                    .setIncludeRowNumber(needRowNumber)
                    .setScale((short) dscale).setInvalidAs(invalidAs)
                    .setTask(data2D.getTask())
                    .setTargetType(targetType);
            return group;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    public void cloneOptions(BaseData2DTaskController controller) {
        if (controller.sourceController.allPagesRadio.isSelected()) {
            sourceController.allPagesRadio.setSelected(true);
        } else if (controller.sourceController.currentPageRadio.isSelected()) {
            sourceController.currentPageRadio.setSelected(true);
        } else {
            sourceController.selectedRadio.setSelected(true);
        }
        filterController.load(controller.filterController.scriptInput.getText(),
                controller.filterController.trueRadio.isSelected());
        filterController.maxInput.setText(controller.filterController.maxFilteredNumber + "");
        scaleSelector.getSelectionModel().select(controller.scale + "");
    }

    @Override
    public void cleanPane() {
        try {
            if (dataController != null) {
                dataController.loadedNotify.removeListener(tableLoadListener);
                tableLoadListener = null;
                dataController.statusNotify.removeListener(tableStatusListener);
                tableStatusListener = null;
            }
            dataController = null;
            data2D = null;
            outputData = null;
            outputColumns = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
