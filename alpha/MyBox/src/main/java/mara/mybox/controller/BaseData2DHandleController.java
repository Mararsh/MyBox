package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import mara.mybox.data.DataSort;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DHandleController extends BaseData2DSourceController {

    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected int scale, defaultScale = 2, maxData = -1;
    protected ObjectType objectType;
    protected InvalidAs invalidAs = InvalidAs.Skip;
    protected List<Integer> dataColsIndices;
    protected List<String> orders;

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
    protected ImageView tableTipsView;

    public BaseData2DHandleController() {
        baseTitle = message("Handle");
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            notSelectColumnsInTable(true);

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

            if (groupController != null) {
                groupController.setParameters(this);
            }

            if (sortController != null) {
                sortController.setParameters(this, message("Sort"), message("DataSortLabel"));
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

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

    public void setParameters(ControlData2DLoad tableController) {
        try {
            setParameters(this, tableController);

            loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

            if (groupController != null) {
                groupController.refreshControls();
            }

            makeSortList();

            checkOptions();

            if (leftPaneCheck != null) {
                leftPaneCheck.setSelected(true);
            } else {
                showLeftPane();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeSortList() {
        try {
            if (sortController == null) {
                return;
            }
            if (!data2D.isValid()) {
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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        if (tableController == null) {
            return;
        }
        super.sourceChanged();
        getMyStage().setTitle(baseTitle + (data2D == null ? "" : " - " + data2D.displayName()));
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
        return colNameCheck != null && colNameCheck.isSelected();
    }

    public boolean showRowNumber() {
        return rowNumberCheck != null && rowNumberCheck.isSelected();
    }

    // Check when selections are changed
    public boolean checkOptions() {
        try {
            if (isSettingValues) {
                return true;
            }
            if (data2D == null || !data2D.hasData()) {
                outOptionsError(message("NoData"));
                return false;
            }
            if (!checkSelections()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void outOptionsError(String error) {
        if (error != null && !error.isBlank()) {
            MyBoxLog.error(error);
            popError(error);
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

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions() || !initData()) {
            return;
        }
        showRightPane();
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
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                data2D.setTask(task);
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
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                if (ok) {
                    startOperation();
                }
            }

        };
        start(task);
    }

    protected void startOperation() {
    }

    public List<List<String>> filteredData(List<Integer> colIndices, boolean needRowNumber) {
        try {
            data2D.startTask(task, filterController.filter);
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
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<List<String>> sortedData(List<Integer> colIndices, boolean needRowNumber) {
        try {
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
            FileDeleteTools.delete(csvData.getFile());
            outputColumns = csvData.columns;
            return outputData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public TmpTable tmpTable(String dname, List<Integer> colIndices, boolean needRowNumber) {
        try {
            Data2D tmp2D = data2D.cloneAll();
            tmp2D.startTask(task, filterController.filter);
            if (task != null) {
                task.setInfo(message("Filter") + "...");
            }
            TmpTable tmpTable = new TmpTable()
                    .setSourceData(tmp2D)
                    .setTargetName(dname)
                    .setSourcePickIndice(colIndices)
                    .setImportData(true)
                    .setForStatistic(false)
                    .setOrders(orders)
                    .setIncludeColName(showColNames())
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
            tmpTable.setTask(task);
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
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
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
            if (task != null) {
                task.setInfo(message("GroupBy") + "...");
            }
            DataTableGroup group = new DataTableGroup(data2D, groupController, tmpTable)
                    .setOrders(orders).setMax(max)
                    .setSourcePickIndice(colIndices)
                    .setIncludeRowNumber(needRowNumber)
                    .setScale((short) dscale).setInvalidAs(invalidAs).setTask(task)
                    .setTargetType(targetType);
            return group;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public void cloneOptions(BaseData2DHandleController sourceController) {
        if (sourceController.allPagesRadio.isSelected()) {
            allPagesRadio.setSelected(true);
        } else if (sourceController.currentPageRadio.isSelected()) {
            currentPageRadio.setSelected(true);
        } else {
            selectedRadio.setSelected(true);
        }
        filterController.load(sourceController.filterController.scriptInput.getText(),
                sourceController.filterController.trueRadio.isSelected());
        filterController.maxInput.setText(sourceController.filterController.maxFilteredNumber + "");
        scaleSelector.getSelectionModel().select(sourceController.scale + "");
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    @Override
    public boolean keyF6() {
        close();
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            super.cleanPane();
            tableController = null;
            data2D = null;
            outputData = null;
            outputColumns = null;
        } catch (Exception e) {
        }
    }

}
