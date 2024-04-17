package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticObject;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTableGroup;
import mara.mybox.data2d.DataTableGroupStatistic;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxBackgroundTask;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.PieChartMaker;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class Data2DGroupStatisticController extends Data2DChartXYController {

    protected DescriptiveStatistic calculation;
    protected DataFileCSV dataFile;
    protected PieChartMaker pieMaker;
    protected List<List<String>> pieData;
    protected List<Data2DColumn> pieColumns;
    protected int pieMaxData;

    @FXML
    protected TabPane chartTabPane;
    @FXML
    protected Tab groupDataTab, statisticDataTab, chartDataTab, xyChartTab, pieChartTab;
    @FXML
    protected ControlStatisticSelection statisticController;
    @FXML
    protected ControlData2DView statisticDataController, chartDataController;
    @FXML
    protected ControlData2DChartPie pieChartController;
    @FXML
    protected FlowPane columnsDisplayPane, valuesDisplayPane;
    @FXML
    protected RadioButton xyParametersRadio, pieParametersRadio;
    @FXML
    protected ToggleGroup pieCategoryGroup;
    @FXML
    protected TextField pieMaxInput;

    public Data2DGroupStatisticController() {
        baseTitle = message("GroupStatistic");
    }

    @Override
    public void initOptions() {
        try {
            super.initOptions();

            pieMaker = pieChartController.pieMaker;
            pieChartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawPieChart();
                }
            });

            pieMaxData = UserConfig.getInt(baseName + "PieMaxData", 100);
            if (pieMaxData <= 0) {
                pieMaxData = 100;
            }
            if (pieMaxInput != null) {
                pieMaxInput.setText(pieMaxData + "");
            }

            statisticController.mustCount();

            chartDataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    xyChartTab.setDisable(false);
                    pieChartTab.setDisable(false);
                    refreshAction();
                }
            });

            pieCategoryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    makeCharts(false, true);
                }
            });

            chartTypesController.disableBubbleChart();
            xyChartTab.setDisable(true);
            pieChartTab.setDisable(true);

            displayAllCheck.visibleProperty().unbind();
            displayAllCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(displayAllCheck, new Tooltip(message("AllRowsLoadComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!groupController.pickValues()) {
            return false;
        }
        checkObject();
        checkInvalidAs();
        return true;
    }

    @Override
    public boolean initChart() {
        return initChart(false);
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        dataFile = null;
        groupDataController.loadNull();
        statisticDataController.loadNull();
        chartDataController.loadNull();
        xyChartTab.setDisable(true);
        pieChartTab.setDisable(true);
        calculation = statisticController.pickValues()
                .setStatisticObject(StatisticObject.Columns)
                .setScale(scale)
                .setInvalidAs(invalidAs)
                .setTaskController(this)
                .setData2D(data2D)
                .setColsIndices(checkedColsIndices)
                .setColsNames(checkedColsNames);
        columnsDisplayPane.getChildren().clear();
        for (String c : checkedColsNames) {
            columnsDisplayPane.getChildren().add(new CheckBox(c));
        }
        valuesDisplayPane.getChildren().clear();
        for (StatisticType t : calculation.types) {
            valuesDisplayPane.getChildren().add(new CheckBox(message(t.name())));
        }
        task = new FxSingletonTask<Void>(this) {

            private DataTableGroup group;
            private DataTableGroupStatistic statistic;

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    group = groupData(DataTableGroup.TargetType.Table,
                            checkedColsIndices, false, -1, scale);
                    if (!group.run()) {
                        return false;
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    task.setInfo(message("Statistic") + "...");
                    statistic = new DataTableGroupStatistic()
                            .setGroups(group).setCountChart(true)
                            .setCalculation(calculation)
                            .setCalNames(checkedColsNames)
                            .setTask(this);
                    if (!statistic.run()) {
                        return false;
                    }
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    dataFile = statistic.getChartData();
                    return dataFile != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                chartDataController.loadDef(dataFile);
                groupDataController.loadDef(group.getTargetData());
                statisticDataController.loadDef(statistic.getStatisticData());
                rightPane.setDisable(false);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask();
            }

        };
        start(task, false);
    }

    @FXML
    public void makeCharts(boolean forXY, boolean forPie) {
        if (forXY) {
            outputColumns = null;
            outputData = null;
            chartMaker.clearChart();
        }
        if (forPie) {
            pieColumns = null;
            pieData = null;
            pieMaker.clearChart();
        }
        if (dataFile == null) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        backgroundTask = new FxBackgroundTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    dataFile.startTask(this, null);

                    List<List<String>> resultsData;
                    if (displayAllCheck.isSelected()) {
                        resultsData = dataFile.allRows(false);
                    } else {
                        resultsData = chartDataController.data2D.tableRows(false);
                    }
                    if (resultsData == null) {
                        return false;
                    }
                    if (forXY && !makeXYData(resultsData)) {
                        return false;
                    }
                    if (forPie && !makePieData(resultsData)) {
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (forXY) {
                    drawXYChart();
                }
                if (forPie) {
                    drawPieChart();
                }
            }

            @Override
            protected void whenFailed() {

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataFile.stopTask();
            }

        };
        start(backgroundTask, false);
    }

    protected boolean makeXYData(List<List<String>> resultsData) {
        try {
            if (resultsData == null) {
                return false;
            }
            outputColumns = new ArrayList<>();
            Data2DColumn xyCategoryColumn
                    = dataFile.column(xyParametersRadio.isSelected() ? 1 : 0);
            outputColumns.add(xyCategoryColumn);

            List<String> colNames = new ArrayList<>();
            List<String> allName = new ArrayList<>();
            for (Node n : columnsDisplayPane.getChildren()) {
                CheckBox cb = (CheckBox) n;
                String name = cb.getText();
                if (cb.isSelected()) {
                    colNames.add(name);
                }
                allName.add(name);
            }
            if (colNames.isEmpty()) {
                if (allName.isEmpty()) {
                    error = message("SelectToHanlde") + ": " + message("ColumnsDisplayed");
                    return false;
                }
                colNames = allName;
            }

            List<String> sTypes = new ArrayList<>();
            List<String> allTypes = new ArrayList<>();
            for (Node n : valuesDisplayPane.getChildren()) {
                CheckBox cb = (CheckBox) n;
                String tname = cb.getText();
                if (cb.isSelected()) {
                    sTypes.add(tname);
                }
                allTypes.add(tname);
            }
            if (sTypes.isEmpty()) {
                if (allTypes.isEmpty()) {
                    error = message("SelectToHanlde") + ": " + message("ValuesDisplayed");
                    return false;
                }
                sTypes = allTypes;
            }
            List<Integer> cols = new ArrayList<>();
            for (String stype : sTypes) {
                if (message("Count").equals(stype)) {
                    outputColumns.add(dataFile.column(2));
                    cols.add(2);
                } else {
                    for (String col : colNames) {
                        int colIndex = dataFile.colOrder(col + "_" + stype);
                        outputColumns.add(dataFile.column(colIndex));
                        cols.add(colIndex);
                    }
                }
            }
            outputData = new ArrayList<>();
            for (List<String> data : resultsData) {
                List<String> xyRow = new ArrayList<>();
                String category = data.get(xyParametersRadio.isSelected() ? 1 : 0);
                xyRow.add(category);
                for (int colIndex : cols) {
                    xyRow.add(data.get(colIndex));
                }
                outputData.add(xyRow);
            }
            selectedCategory = xyCategoryColumn.getColumnName();
            selectedValue = message("Statistic");
            return initChart();
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean makePieData(List<List<String>> resultsData) {
        try {
            if (resultsData == null) {
                return false;
            }
            pieColumns = new ArrayList<>();
            Data2DColumn pieCategoryColumn
                    = dataFile.columns.get(pieParametersRadio.isSelected() ? 1 : 0);
            pieColumns.add(pieCategoryColumn);
            pieColumns.add(dataFile.columns.get(2));
            pieColumns.add(new Data2DColumn(message("Percentage"), ColumnDefinition.ColumnType.Double));

            pieData = new ArrayList<>();
            double sum = 0, count;
            for (List<String> data : resultsData) {
                try {
                    sum += Double.parseDouble(data.get(2));
                } catch (Exception e) {
                }
            }
            for (List<String> data : resultsData) {
                try {
                    String category = data.get(pieParametersRadio.isSelected() ? 1 : 0);
                    List<String> pieRow = new ArrayList<>();
                    pieRow.add(category);
                    count = Double.parseDouble(data.get(2));
                    pieRow.add((long) count + "");
                    pieRow.add(DoubleTools.percentage(count, sum, scale));
                    pieData.add(pieRow);
                } catch (Exception e) {
                }
            }

            selectedCategory = pieCategoryColumn.getColumnName();
            String title = chartTitle();
            pieMaker.init(message("PieChart"))
                    .setDefaultChartTitle(title + " - " + message("Count"))
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(message("Count"))
                    .setValueLabel(message("Count"))
                    .setInvalidAs(invalidAs);

            return true;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void drawChart() {
        drawXYChart();
        drawPieChart();
    }

    @Override
    public void drawXYChart() {
        try {
            chartData = chartMax();
            if (chartData == null || chartData.isEmpty()) {
                return;
            }
            chartController.writeXYChart(outputColumns, chartData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void drawPieChart() {
        try {
            if (pieData == null || pieData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<List<String>> maxPieData;
            if (pieMaxData > 0 && pieMaxData < pieData.size()) {
                maxPieData = pieData.subList(0, pieMaxData);
            } else {
                maxPieData = pieData;
            }
            pieChartController.writeChart(pieColumns, maxPieData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void goXYchart() {
        makeCharts(true, false);
    }

    @FXML
    @Override
    public void refreshAction() {
        makeCharts(true, true);
    }

    @Override
    public void typeChanged() {
        initChart();
        goXYchart();
    }

    @FXML
    public void pieMaxAction() {
        if (pieMaxInput != null) {
            boolean ok;
            String s = pieMaxInput.getText();
            if (s == null || s.isBlank()) {
                pieMaxData = -1;
                ok = true;
            } else {
                try {
                    int v = Integer.parseInt(s);
                    if (v > 0) {
                        pieMaxData = v;
                        ok = true;
                    } else {
                        ok = false;
                    }
                } catch (Exception ex) {
                    ok = false;
                }
            }
            if (ok) {
                UserConfig.setInt(baseName + "PieMaxData", pieMaxData);
                pieMaxInput.setStyle(null);
            } else {
                pieMaxInput.setStyle(UserConfig.badStyle());
                popError(message("Invalid") + ": " + message("Maximum"));
                return;
            }
        }

        drawPieChart();
    }

    @FXML
    @Override
    public boolean menuAction() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == groupDataTab) {
            return groupDataController.menuAction();

        } else if (tab == statisticDataTab) {
            return statisticDataController.menuAction();

        } else if (tab == chartDataTab) {
            return chartDataController.menuAction();

        } else if (tab == xyChartTab) {
            return chartController.menuAction();

        } else if (tab == pieChartTab) {
            return pieChartController.menuAction();

        }
        return false;
    }

    @FXML
    @Override
    public boolean popAction() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == groupDataTab) {
            return groupDataController.popAction();

        } else if (tab == statisticDataTab) {
            return statisticDataController.popAction();

        } else if (tab == chartDataTab) {
            return chartDataController.popAction();

        } else if (tab == xyChartTab) {
            return chartController.popAction();

        } else if (tab == pieChartTab) {
            return pieChartController.popAction();

        }
        return false;
    }

    @Override
    public boolean controlAlt2() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == xyChartTab) {
            return chartController.controlAlt2();

        } else if (tab == pieChartTab) {
            return pieChartController.controlAlt2();

        }
        return false;
    }

    @Override
    public boolean controlAlt3() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == xyChartTab) {
            return chartController.controlAlt3();

        } else if (tab == pieChartTab) {
            return pieChartController.controlAlt3();

        }
        return false;
    }

    @Override
    public boolean controlAlt4() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == xyChartTab) {
            return chartController.controlAlt4();

        } else if (tab == pieChartTab) {
            return pieChartController.controlAlt4();

        }
        return false;
    }

    /*
        static
     */
    public static Data2DGroupStatisticController open(BaseData2DLoadController tableController) {
        try {
            Data2DGroupStatisticController controller = (Data2DGroupStatisticController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DGroupStatisticFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
