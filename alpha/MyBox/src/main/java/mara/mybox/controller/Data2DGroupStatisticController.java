package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.PieChartMaker;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class Data2DGroupStatisticController extends Data2DChartGroupXYController {

    protected List<String> calculationColumns, calculations;
    protected DataFileCSV resultsFile;
    protected PieChartMaker pieMaker;
    protected List<List<String>> pieData;
    protected List<Data2DColumn> pieColumns;

    @FXML
    protected ControlSelection calculationController;
    @FXML
    protected ControlData2DResults valuesController;
    @FXML
    protected CheckBox onlyStatisticCheck;
    @FXML
    protected ControlData2DChartPie pieChartController;

    public Data2DGroupStatisticController() {
        baseTitle = message("GroupStatistic");
        TipsLabelKey = "GroupEqualTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            pieMaker = pieChartController.pieMaker;
            pieChartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawPieChart();
                }
            });

            calculationController.setParameters(this, message("Calculation"), message("Aggregate"));
            calculationController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeSortList();
                }
            });

            valuesController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshAction();
                }
            });

            onlyStatisticCheck.setSelected(UserConfig.getBoolean(baseName + "OnlyStatisticNumbers", false));
            onlyStatisticCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "OnlyStatisticNumbers", nv);
                    makeStatisticList();
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

            if (!data2D.isValid()) {
                calculationController.loadNames(null);
                return;
            }
            makeStatisticList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeStatisticList() {
        try {
            if (!data2D.isValid()) {
                calculationController.loadNames(null);
                return;
            }
            List<String> cnames = new ArrayList<>();
            for (Data2DColumn column : data2D.columns) {
                String name = column.getColumnName();
                if (!onlyStatisticCheck.isSelected() || column.isNumberType()) {
                    cnames.add(name + "-" + message("Mean"));
                    cnames.add(name + "-" + message("Summation"));
                    cnames.add(name + "-" + message("Maximum"));
                    cnames.add(name + "-" + message("Minimum"));
                    cnames.add(name + "-" + message("PopulationVariance"));
                    cnames.add(name + "-" + message("SampleVariance"));
                    cnames.add(name + "-" + message("PopulationStandardDeviation"));
                    cnames.add(name + "-" + message("SampleStandardDeviation"));
                }
            }
            calculationController.loadNames(cnames);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void makeSortList() {
        try {
            List<String> names = new ArrayList<>();
            names.add(message("Count") + "-" + message("Descending"));
            names.add(message("Count") + "-" + message("Ascending"));
            if (groupController.byValues()) {
                List<String> groups = groupController.groupNames;
                if (groups != null) {
                    for (String name : groups) {
                        names.add(name + "-" + message("Descending"));
                        names.add(name + "-" + message("Ascending"));
                    }
                }
            }
            calculations = calculationController.selectedNames();
            if (calculations != null) {
                for (String name : calculations) {
                    names.add(name + "-" + message("Descending"));
                    names.add(name + "-" + message("Ascending"));
                }
            }
            sortController.loadNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            List<String> colsNames = new ArrayList<>();

            if (groupController.byValues()) {
                colsNames.addAll(groupController.groupNames);

            } else if (groupController.byConditions()) {
                colsNames = data2D.columnNames();

            } else {
                colsNames.add(groupController.groupName);

            }

            calculations = calculationController.selectedNames();
            calculationColumns = new ArrayList<>();
            if (calculations != null) {
                for (String name : calculations) {
                    if (name.endsWith("-" + message("Mean"))) {
                        name = name.substring(0, name.length() - ("-" + message("Mean")).length());
                    } else if (name.endsWith("-" + message("Summation"))) {
                        name = name.substring(0, name.length() - ("-" + message("Summation")).length());
                    } else if (name.endsWith("-" + message("Maximum"))) {
                        name = name.substring(0, name.length() - ("-" + message("Maximum")).length());
                    } else if (name.endsWith("-" + message("Minimum"))) {
                        name = name.substring(0, name.length() - ("-" + message("Minimum")).length());
                    } else if (name.endsWith("-" + message("PopulationVariance"))) {
                        name = name.substring(0, name.length() - ("-" + message("PopulationVariance")).length());
                    } else if (name.endsWith("-" + message("SampleVariance"))) {
                        name = name.substring(0, name.length() - ("-" + message("SampleVariance")).length());
                    } else if (name.endsWith("-" + message("PopulationStandardDeviation"))) {
                        name = name.substring(0, name.length() - ("-" + message("PopulationStandardDeviation")).length());
                    } else if (name.endsWith("-" + message("SampleStandardDeviation"))) {
                        name = name.substring(0, name.length() - ("-" + message("SampleStandardDeviation")).length());
                    } else {
                        continue;
                    }
                    if (!colsNames.contains(name)) {
                        colsNames.add(name);
                    }
                    if (!calculationColumns.contains(name)) {
                        calculationColumns.add(name);
                    }
                }
            }

            checkedColsIndices = new ArrayList<>();
            checkedColumns = new ArrayList<>();
            for (String name : colsNames) {
                checkedColsIndices.add(data2D.colOrder(name));
                checkedColumns.add(data2D.columnByName(name));
            }
            checkedColsNames = colsNames;

            sorts = sortController.selectedNames();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        resultsFile = null;
        valuesController.loadNull();
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Data2D tmp2D = data2D.cloneAll();
                    List<Data2DColumn> tmpColumns = new ArrayList<>();
                    for (Data2DColumn column : data2D.columns) {
                        Data2DColumn tmpColumn = column.cloneAll();
                        String name = tmpColumn.getColumnName();
                        if (groupController.groupName != null && groupController.groupName.equals(name)) {
                            tmpColumn.setType(ColumnDefinition.ColumnType.Double);
                        }
                        if (calculationColumns.contains(name)) {
                            tmpColumn.setType(ColumnDefinition.ColumnType.Double);
                        }
                        tmpColumns.add(tmpColumn);
                    }
                    tmp2D.setColumns(tmpColumns);
                    tmp2D.startTask(task, filterController.filter);
                    DataTable tmpTable;
                    if (isAllPages()) {
                        tmpTable = tmp2D.toTmpTable(task, checkedColsIndices, false, false, invalidAs);
                    } else {
                        outputData = filtered(checkedColsIndices, false);
                        if (outputData == null || outputData.isEmpty()) {
                            error = message("NoData");
                            return false;
                        }
                        tmpTable = tmp2D.toTmpTable(task, checkedColsIndices, outputData, false, false, invalidAs);
                        outputData = null;
                    }
                    tmp2D.stopFilter();
                    return groupData(tmpTable);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                valuesController.loadDef(resultsFile);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    public boolean groupData(DataTable tmpTable) {
        try {
            if (tmpTable == null) {
                return false;
            }
            if (groupController.byValues()) {
                resultsFile = tmpTable.groupStatisticByValues(data2D.dataName() + "_group", task,
                        groupController.groupNames, calculations, sorts, maxData, scale, invalidAs);

            } else if (groupController.byInterval()) {
                resultsFile = tmpTable.groupStatisticByRange(data2D.dataName() + "_group", task,
                        true, groupController.groupName, groupController.groupInterval,
                        calculations, sorts, maxData, scale, invalidAs);

            } else if (groupController.byNumber()) {
                resultsFile = tmpTable.groupStatisticByRange(data2D.dataName() + "_group", task,
                        false, groupController.groupName, groupController.groupNumber,
                        calculations, sorts, maxData, scale, invalidAs);

            } else if (groupController.byConditions()) {
                resultsFile = tmpTable.groupStatisticByFilters(data2D.dataName() + "_group", task,
                        groupController.groupConditions, calculations, sorts, maxData, scale, invalidAs);

            }

            tmpTable.drop();
            return resultsFile != null;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        outputColumns = null;
        outputData = null;
        pieColumns = null;
        pieData = null;
        chartMaker.clearChart();
        pieMaker.clearChart();
        if (resultsFile == null) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        backgroundTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    resultsFile.startTask(backgroundTask, null);
                    makeChartData();
                    return outputData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                drawChart();
            }

            @Override
            protected void whenFailed() {

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                resultsFile.stopTask();
                backgroundTask = null;
            }

        };
        start(backgroundTask, false);
    }

    public void makeChartData() {
        try {
            List<List<String>> resultsData;
            if (displayAllCheck.isSelected()) {
                resultsData = resultsFile.allRows(false);
            } else {
                resultsData = valuesController.data2D.tableRowsWithoutNumber();
            }
            if (resultsData == null) {
                return;
            }
            int groupSize, columnSize = resultsFile.columns.size();
            int categoryIndex;
            if (groupController.byValues()) {
                groupSize = groupController.groupNames.size();
                categoryIndex = groupSize > 1 ? 0 : 1;

            } else if (groupController.byInterval() || groupController.byNumber()) {
                groupSize = 1;
                categoryIndex = 1;

            } else if (groupController.byConditions()) {
                groupSize = 1;
                categoryIndex = 1;

            } else {
                return;
            }

            int countIndex = groupSize + 1;
            List<Integer> valueIndice = new ArrayList<>();
            if (columnSize > groupSize + 2) {
                for (int i = groupSize + 2; i < columnSize; i++) {
                    valueIndice.add(i);
                }
            } else {
                valueIndice.add(countIndex);
            }
            categoryColumn = resultsFile.columns.get(categoryIndex);
            Data2DColumn countColumn = resultsFile.columns.get(countIndex);

            outputColumns = new ArrayList<>();
            outputColumns.add(categoryColumn);
            for (int i : valueIndice) {
                outputColumns.add(resultsFile.columns.get(i));
            }

            pieColumns = new ArrayList<>();
            pieColumns.add(categoryColumn);
            pieColumns.add(countColumn);

            outputData = new ArrayList<>();
            pieData = new ArrayList<>();
            for (List<String> data : resultsData) {
                List<String> xyRow = new ArrayList<>();
                String category = data.get(categoryIndex);
                xyRow.add(category);
                for (int i : valueIndice) {
                    xyRow.add(data.get(i));
                }
                outputData.add(xyRow);
                List<String> pieRow = new ArrayList<>();
                pieRow.add(category);
                pieRow.add(data.get(countIndex));
                pieData.add(pieRow);
            }

            selectedCategory = categoryColumn.getColumnName();
            selectedValue = message("Aggregate");
            String title = chartTitle();
            initChart(title, categoryColumn.isNumberType());

            pieMaker.init(message("PieChart"))
                    .setDefaultChartTitle(title + " - " + message("Count"))
                    .setChartTitle(title + " - " + message("Count"))
                    .setDefaultCategoryLabel(selectedCategory)
                    .setCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(message("Count"))
                    .setValueLabel(message("Count"))
                    .setInvalidAs(invalidAs);

        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
    }

    @Override
    public void drawChart() {
        drawXYChart();
        drawPieChart();
    }

    @FXML
    public void drawPieChart() {
        try {
            if (pieData == null || pieData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            pieChartController.writeChart(pieColumns, pieData, false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static
     */
    public static Data2DGroupStatisticController open(ControlData2DLoad tableController) {
        try {
            Data2DGroupStatisticController controller = (Data2DGroupStatisticController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DGroupStatisticFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
