package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticObject;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTableGroupStatistic;
import mara.mybox.data2d.reader.DataTableGroup;
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

    protected DescriptiveStatistic calculation;
    protected DataFileCSV statisticFile;
    protected PieChartMaker pieMaker;
    protected List<List<String>> pieData;
    protected List<Data2DColumn> pieColumns;

    @FXML
    protected ControlData2DStatisticSelection statisticController;
    @FXML
    protected ControlData2DResults groupDataController, statisticDataController;
    @FXML
    protected ControlData2DChartPie pieChartController;
    @FXML
    protected CheckBox parametersCheck;

    public Data2DGroupStatisticController() {
        baseTitle = message("GroupStatistic");
        TipsLabelKey = "GroupStatisticTips";
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

            statisticController.countCheck.setSelected(true);
            statisticController.countCheck.setDisable(true);

            statisticDataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshAction();
                }
            });

            parametersCheck.setSelected(UserConfig.getBoolean(baseName + "Parameters", true));
            parametersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Parameters", parametersCheck.isSelected());
                    refreshAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        statisticFile = null;
        groupDataController.loadNull();
        statisticDataController.loadNull();
        calculation = statisticController.pickValues()
                .setStatisticObject(StatisticObject.Columns)
                .setScale(scale)
                .setInvalidAs(invalidAs)
                .setHandleController(this)
                .setData2D(data2D)
                .setColsIndices(checkedColsIndices)
                .setColsNames(checkedColsNames);
        task = new SingletonTask<Void>(this) {

            private DataTableGroup group;
            private DataTableGroupStatistic statistic;

            @Override
            protected boolean handle() {
                try {
                    group = groupData(DataTableGroup.TargetType.TmpTable,
                            checkedColsNames, null, -1, scale);
                    if (!group.run()) {
                        return false;
                    }
                    Platform.runLater(() -> {
                        groupDataController.loadData(group.getTargetData());
                    });
                    statistic = new DataTableGroupStatistic()
                            .setGroups(group)
                            .setCalculation(calculation)
                            .setCalNames(checkedColsNames)
                            .setTask(task);
                    if (!statistic.run()) {
                        return false;
                    }
                    statisticFile = statistic.getTargetFile();
                    return statisticFile != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                statisticDataController.loadData(statisticFile);
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

    @FXML
    @Override
    public void refreshAction() {
        outputColumns = null;
        outputData = null;
        pieColumns = null;
        pieData = null;
        chartMaker.clearChart();
        pieMaker.clearChart();
        if (statisticFile == null) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        backgroundTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    statisticFile.startTask(backgroundTask, null);
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
                statisticFile.stopTask();
                backgroundTask = null;
            }

        };
        start(backgroundTask, false);
    }

    public void makeChartData() {
        try {
            List<List<String>> resultsData;
            if (displayAllCheck.isSelected()) {
                resultsData = statisticFile.allRows(false);
            } else {
                resultsData = statisticDataController.data2D.tableRowsWithoutNumber();
            }
            if (resultsData == null) {
                return;
            }
            categoryColumn = statisticFile.columns.get(parametersCheck.isSelected() ? 1 : 0);
            Data2DColumn countColumn = statisticFile.columns.get(2);

            outputColumns = new ArrayList<>();
            outputColumns.add(categoryColumn);
            int colSize = statisticFile.columns.size();
            if (colSize > 3) {
                outputColumns.addAll(statisticFile.columns.subList(3, colSize));
            } else {
                outputColumns.add(countColumn);
            }

            pieColumns = new ArrayList<>();
            pieColumns.add(categoryColumn);
            pieColumns.add(countColumn);

            outputData = new ArrayList<>();
            pieData = new ArrayList<>();
            for (List<String> data : resultsData) {
                List<String> xyRow = new ArrayList<>();
                String category = data.get(parametersCheck.isSelected() ? 1 : 0);
                String count = data.get(2);
                xyRow.add(category);
                if (colSize > 3) {
                    xyRow.addAll(data.subList(3, colSize));
                } else {
                    xyRow.add(count);
                }
                outputData.add(xyRow);
                List<String> pieRow = new ArrayList<>();
                pieRow.add(category);
                pieRow.add(count);
                pieData.add(pieRow);
            }

            selectedCategory = categoryColumn.getColumnName();
            selectedValue = message("Statistic");
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

    @Override
    public void drawXYChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            chartController.writeXYChart(outputColumns, outputData, null, false);
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
