package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
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
public class Data2DGroupEqualValuesController extends Data2DChartXYController {

    protected List<String> groups, calculationColumns, calculations;
    protected DataFileCSV resultsFile;
    protected PieChartMaker pieMaker;
    protected List<List<String>> xyData, pieData;
    protected List<Data2DColumn> xyColumns, pieColumns;

    @FXML
    protected Tab groupTab;
    @FXML
    protected ControlSelection groupController, calculationController;
    @FXML
    protected ControlData2DResults valuesController;
    @FXML
    protected CheckBox displayAllCheck;
    @FXML
    protected ControlData2DChartXY xyChartController;
    @FXML
    protected ControlData2DChartPie pieChartController;

    public Data2DGroupEqualValuesController() {
        baseTitle = message("GroupEqualValues");
    }

    @Override
    public void initControls() {
        try {
            chartController = xyChartController;
            super.initControls();

            pieChartController.dataController = this;
            pieMaker = pieChartController.pieMaker;

            noColumnSelection(true);

            groupController.setParameters(this, message("Column"), message("GroupBy"));
            groupController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

            calculationController.setParameters(this, message("Calculation"), message("Aggregate"));

            bubbleChartRadio.setDisable(true);

            displayAllCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayAll", true));
            displayAllCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayAll", displayAllCheck.isSelected());
                noticeMemory();
                drawChart();
            });

            displayAllCheck.visibleProperty().bind(allPagesRadio.selectedProperty());

            valuesController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawChart();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void makeOptions() {
        try {
            if (!data2D.isValid()) {
                groupController.loadNames(null);
                calculationController.loadNames(null);
                return;
            }
            List<String> gnames = new ArrayList<>();
            List<String> cnames = new ArrayList<>();
            for (Data2DColumn column : data2D.columns) {
                String name = column.getColumnName();
                gnames.add(name);
                cnames.add(name + "-" + message("Mean"));
                cnames.add(name + "-" + message("Summation"));
                cnames.add(name + "-" + message("Maximum"));
                cnames.add(name + "-" + message("Minimum"));
                cnames.add(name + "-" + message("PopulationVariance"));
                cnames.add(name + "-" + message("SampleVariance"));
                cnames.add(name + "-" + message("PopulationStandardDeviation"));
                cnames.add(name + "-" + message("SampleStandardDeviation"));
            }
            groupController.loadNames(gnames);
            calculationController.loadNames(cnames);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterRefreshControls() {
    }

    @Override
    public void noticeMemory() {
        noticeLabel.setVisible(isAllPages() && displayAllCheck.isSelected());
    }

    @Override
    public boolean initData() {
        try {
            checkObject();

            if (blankNonnumericRadio.isSelected()) {
                invalidAs = InvalidAs.Blank;
            } else {
                invalidAs = InvalidAs.Zero;
            }
            groups = groupController.selectedNames();
            if (groups == null || groups.isEmpty()) {
                outOptionsError(message("SelectToHandle") + ": " + message("GroupBy"));
                tabPane.getSelectionModel().select(groupTab);
                return false;
            }
            List<String> colsNames = new ArrayList<>();
            colsNames.addAll(groups);
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

            return initChart(groups.toString());
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
                        if (calculationColumns.contains(tmpColumn.getColumnName())) {
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
                    if (tmpTable == null) {
                        return false;
                    }
                    resultsFile = tmpTable.groupEqualValues(data2D.dataName() + "_group", task, groups, calculations);
                    tmpTable.drop();
                    return resultsFile != null;
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

    @Override
    public String chartTitle() {
        return message(message("GroupEqualValues") + " - " + groups);
    }

    @Override
    public String categoryName() {
        return selectedCategory;
    }

    @FXML
    @Override
    public void refreshAction() {
        drawChart();
    }

    @Override
    public void drawChart() {
        xyColumns = null;
        xyData = null;
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
                    return xyData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputColumns = xyColumns;
                chartMaker.setPalette(makePalette());
                xyChartController.writeXYChart(xyColumns, xyData, null, false);
                pieChartController.writeChart(pieColumns, pieData, false);
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
            int groupSize = groups.size(), columnSize = resultsFile.columns.size();
            if (groupSize > 1) {
                xyColumns = new ArrayList<>();
                xyColumns.add(resultsFile.columns.get(0));
                for (int i = groupSize + 1; i < columnSize; i++) {
                    xyColumns.add(resultsFile.columns.get(i));
                }
                pieColumns = xyColumns.subList(0, 2);
                xyData = new ArrayList<>();
                pieData = new ArrayList<>();
                for (List<String> data : resultsData) {
                    List<String> xyRow = new ArrayList<>();
                    xyRow.add(data.get(0));
                    for (int i = groupSize + 1; i < columnSize; i++) {
                        xyRow.add(data.get(i));
                    }
                    xyData.add(xyRow);
                    pieData.add(xyRow.subList(0, 2));
                }

            } else {
                xyColumns = new ArrayList<>();
                for (int i = 1; i < columnSize; i++) {
                    xyColumns.add(resultsFile.columns.get(i));
                }
                pieColumns = xyColumns.subList(0, 2);
                xyData = new ArrayList<>();
                pieData = new ArrayList<>();
                for (List<String> data : resultsData) {
                    List<String> xyRow = new ArrayList<>();
                    for (int i = 1; i < columnSize; i++) {
                        xyRow.add(data.get(i));
                    }
                    xyData.add(xyRow);
                    pieData.add(xyRow.subList(0, 2));
                }
            }
            selectedCategory = xyColumns.get(0).getColumnName();
            selectedValue = message("Aggregate");

            String title = chartTitle();
            initChart(title);

            pieMaker.init(message("PieChart"))
                    .setDefaultChartTitle(title)
                    .setChartTitle(title)
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

    /*
        static
     */
    public static Data2DGroupEqualValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DGroupEqualValuesController controller = (Data2DGroupEqualValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DGroupEqualValuesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
