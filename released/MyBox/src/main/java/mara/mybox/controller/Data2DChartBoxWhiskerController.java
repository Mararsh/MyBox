package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.BoxWhiskerChart;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-27
 * @License Apache License Version 2.0
 */
public class Data2DChartBoxWhiskerController extends BaseData2DChartController {

    protected XYChartMaker chartMaker;
    protected int categorysCol, boxWidth;
    protected DescriptiveStatistic calculation;
    protected Map<String, Node> lines;

    @FXML
    protected ControlData2DChartXY chartController;
    @FXML
    protected ComboBox<String> boxWdithSelector;
    @FXML
    protected VBox dataOptionsBox;
    @FXML
    protected FlowPane categoryColumnsPane;
    @FXML
    protected HBox lineWidthBox;
    @FXML
    protected RadioButton categoryStringRadio, categoryNumberRadio;
    @FXML
    protected ToggleGroup categoryValuesGroup;
    @FXML
    protected CheckBox q0Check, q1Check, q2Check, q3Check, q4Check, e4Check, e3Check, e2Check, e1Check,
            dottedCheck, outliersCheck, meanCheck, meanLineCheck, xyReverseCheck;
    @FXML
    protected ControlData2DResults statisticDataController;

    public Data2DChartBoxWhiskerController() {
        baseTitle = message("BoxWhiskerChart");
        TipsLabelKey = "BoxWhiskerChartTips";
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(q0Check, new Tooltip(message("MinimumQ0") + "\n0%"));
            NodeStyleTools.setTooltip(q1Check, new Tooltip(message("LowerQuartile") + "\n25%"));
            NodeStyleTools.setTooltip(q2Check, new Tooltip(message("Median") + "\n50%"));
            NodeStyleTools.setTooltip(q3Check, new Tooltip(message("UpperQuartile") + "\n75%"));
            NodeStyleTools.setTooltip(q4Check, new Tooltip(message("MaximumQ4") + "\n100%"));
            NodeStyleTools.setTooltip(e4Check, new Tooltip(message("UpperExtremeOutlierLine") + "\n E4 =Q3 + 1.5 * ( Q3 - Q1 )"));
            NodeStyleTools.setTooltip(e3Check, new Tooltip(message("UpperMildOutlierLine") + "\n E3 =Q3 + 3 * ( Q3 - Q1 )"));
            NodeStyleTools.setTooltip(e2Check, new Tooltip(message("LowerMildOutlierLine") + "\n E2 = Q1 - 1.5 * ( Q3 - Q1 )"));
            NodeStyleTools.setTooltip(e1Check, new Tooltip(message("LowerExtremeOutlierLine") + "\n E1 = Q1 - 3 * ( Q3 - Q1 )"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            chartMaker = chartController.chartMaker;
            chartMaker.init(ChartType.BoxWhiskerChart, message("BoxWhiskerChart"));
            chartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawChart();
                }
            });

            xyReverseCheck.setSelected(!chartMaker.isIsXY());
            xyReverseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    initChart();
                    drawChart();
                }
            });

            initBoxOptions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void makeOptions() {
        try {
            super.makeOptions();

            if (categoryColumnSelector != null) {
                isSettingValues = true;
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
                categoryColumnSelector.getItems().add(0, message("RowNumber"));
                categoryColumnSelector.setValue(selectedCategory);
                isSettingValues = false;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initBoxOptions() {
        try {
            lines = new HashMap<>();

            q0Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ0", false));
            q0Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    MyBoxLog.console(isSettingValues + "  " + newValue);
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ0", q0Check.isSelected());
                    setLineVisible(message("MinimumQ0"), q0Check.isSelected());
                }
            });

            q1Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ1", false));
            q1Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ1", q1Check.isSelected());
                    setLineVisible(message("LowerQuartile"), q1Check.isSelected());
                }
            });

            q2Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ2", false));
            q2Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ2", q2Check.isSelected());
                    setLineVisible(message("Median"), q2Check.isSelected());
                }
            });

            q3Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ3", false));
            q3Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ3", q3Check.isSelected());
                    setLineVisible(message("UpperQuartile"), q3Check.isSelected());
                }
            });

            q4Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ4", false));
            q4Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ4", q4Check.isSelected());
                    setLineVisible(message("MaximumQ4"), q4Check.isSelected());
                }
            });

            e4Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ5", false));
            e4Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ5", e4Check.isSelected());
                    setLineVisible(message("UpperExtremeOutlierLine"), e4Check.isSelected());
                }
            });

            e3Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ6", false));
            e3Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ6", e3Check.isSelected());
                    setLineVisible(message("UpperMildOutlierLine"), e3Check.isSelected());
                }
            });

            e2Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ7", false));
            e2Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ7", e2Check.isSelected());
                    setLineVisible(message("LowerMildOutlierLine"), e2Check.isSelected());
                }
            });

            e1Check.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineQ8", false));
            e1Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineQ8", e1Check.isSelected());
                    setLineVisible(message("LowerExtremeOutlierLine"), e1Check.isSelected());
                }
            });

            meanLineCheck.setSelected(UserConfig.getBoolean("ChartBoxWhiskerLineMean", false));
            meanLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("ChartBoxWhiskerLineMean", meanLineCheck.isSelected());
                    setLineVisible(message("Mean"), meanLineCheck.isSelected());
                }
            });

            dottedCheck.setSelected(chartMaker.isDotted());
            dottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    chartMaker.setDotted(dottedCheck.isSelected());
                    chartMaker.setChartStyle();
                }
            });

            outliersCheck.setSelected(UserConfig.getBoolean("BoxWhiskerChartHandleOutliers", false));
            outliersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("BoxWhiskerChartHandleOutliers", outliersCheck.isSelected());
                    drawChart();
                }
            });

            e4Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            e3Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            e2Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            e1Check.disableProperty().bind(outliersCheck.selectedProperty().not());

            meanCheck.setSelected(UserConfig.getBoolean("BoxWhiskerChartHandleMean", false));
            meanCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean("BoxWhiskerChartHandleMean", meanCheck.isSelected());
                    drawChart();
                }
            });
            meanLineCheck.disableProperty().bind(meanCheck.selectedProperty().not());

            boxWidth = UserConfig.getInt("BoxWhiskerChartBoxWidth", 40);
            if (boxWidth <= 0) {
                boxWidth = 40;
            }
            boxWdithSelector.getItems().addAll(Arrays.asList(
                    "40", "60", "20", "30", "15", "50", "10", "4", "80", "18"
            ));
            boxWdithSelector.getSelectionModel().select(boxWidth + "");
            boxWdithSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                boxWidth = v;
                                boxWdithSelector.getEditor().setStyle(null);
                                UserConfig.setInt("BoxWhiskerChartBoxWidth", boxWidth);
                                chartController.redraw();
                            } else {
                                boxWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            boxWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        if (rowsRadio != null && dataOptionsBox != null) {
            if (rowsRadio.isSelected()) {
                if (!dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                    dataOptionsBox.getChildren().add(1, categoryColumnsPane);
                }
            } else {
                if (dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                    dataOptionsBox.getChildren().remove(categoryColumnsPane);
                }
            }
        }
        noticeMemory();
    }

    @Override
    public void noticeMemory() {
        if (noticeLabel != null) {
            noticeLabel.setVisible(isAllPages() && rowsRadio != null && rowsRadio.isSelected());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            categorysCol = -1;
            if (rowsRadio != null && rowsRadio.isSelected() && selectedCategory != null
                    && categoryColumnSelector.getSelectionModel().getSelectedIndex() != 0) {
                categorysCol = data2D.colOrder(selectedCategory);
            }

            dataColsIndices = new ArrayList<>();
            dataColsIndices.addAll(checkedColsIndices);
            if (categorysCol >= 0 && !dataColsIndices.contains(categorysCol)) {
                dataColsIndices.add(categorysCol);
            }

            calculation = new DescriptiveStatistic()
                    .add(StatisticType.Mean)
                    .add(StatisticType.MinimumQ0)
                    .add(StatisticType.LowerQuartile)
                    .add(StatisticType.Median)
                    .add(StatisticType.UpperQuartile)
                    .add(StatisticType.MaximumQ4)
                    .add(StatisticType.LowerExtremeOutlierLine)
                    .add(StatisticType.LowerMildOutlierLine)
                    .add(StatisticType.UpperMildOutlierLine)
                    .add(StatisticType.UpperExtremeOutlierLine)
                    .setScale(scale);
            switch (objectType) {
                case Rows:
                    calculation.setStatisticObject(DescriptiveStatistic.StatisticObject.Rows);
                    break;
                case All:
                    calculation.setStatisticObject(DescriptiveStatistic.StatisticObject.All);
                    break;
                default:
                    calculation.setStatisticObject(DescriptiveStatistic.StatisticObject.Columns);
                    break;
            }
            calculation.setHandleController(this).setData2D(data2D)
                    .setColsIndices(checkedColsIndices)
                    .setColsNames(checkedColsNames)
                    .setCategoryName(categorysCol >= 0 ? selectedCategory : null)
                    .setInvalidAs(invalidAs);

            statisticDataController.loadNull();

            return calculation.prepare();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void readData() {
        try {
            boolean ok;
            calculation.setTask(task);
            if (isAllPages()) {
                ok = handlePages();
            } else {
                ok = handleSelected();
            }
            calculation.setTask(null);
            if (!ok) {
                outputData = null;
                return;
            }
            outputColumns = calculation.getOutputColumns();
            outputData = calculation.getOutputData();
            if (rowsRadio != null && rowsRadio.isSelected()) {
                return;
            }

            makeChartData();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
        }
    }

    public boolean handleSelected() {
        try {
            outputData = tableFiltered(dataColsIndices, rowsRadio != null && rowsRadio.isSelected() && categorysCol < 0);
            if (outputData == null) {
                return false;
            }
            calculation.setTask(task);
            return calculation.statisticData(outputData);
        } catch (Exception e) {
            error = e.toString();
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean handlePages() {
        try {
            if (rowsRadio != null && rowsRadio.isSelected()) {
                outputData = data2D.allRows(dataColsIndices, rowsRadio.isSelected() && categorysCol < 0);
                if (outputData == null) {
                    return false;
                }
                calculation.setTask(task);
                return calculation.statisticData(outputData);
            }
            TmpTable tmpTable = TmpTable.toStatisticTable(data2D, task, dataColsIndices, invalidAs);
            if (tmpTable == null) {
                outputData = null;
                return false;
            }
            tmpTable.startTask(task, null);
            calculation.setData2D(tmpTable).setInvalidAs(invalidAs)
                    .setColsIndices(tmpTable.columnIndices().subList(1, tmpTable.columnsNumber()))
                    .setColsNames(tmpTable.columnNames().subList(1, tmpTable.columnsNumber()));
            boolean ok = calculation.statisticAllByColumns();
            tmpTable.stopFilter();
            tmpTable.drop();
            return ok;
        } catch (Exception e) {
            error = e.toString();
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public void makeChartData() {
        try {
            if (outputData == null || outputColumns == null) {
                return;
            }
            List<List<String>> transposed = new ArrayList<>();
            for (int col = 1; col < outputColumns.size(); ++col) {
                Data2DColumn column = outputColumns.get(col);
                List<String> row = new ArrayList<>();
                row.add(column.getColumnName());
                for (int c = 0; c < outputData.size(); ++c) {
                    String s = outputData.get(c).get(col);
                    if (s == null || !column.needScale() || scale < 0) {
                        row.add(s);
                    } else {
                        row.add(DoubleTools.scaleString(s, invalidAs, scale));
                    }
                }
                transposed.add(row);
            }
            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(message("ColumnName"), ColumnDefinition.ColumnType.String));
            String prefix = (allRadio.isSelected() ? message("All") : message("Column")) + "-";
            for (StatisticType type : calculation.types) {
                outputColumns.add(new Data2DColumn(prefix + message(type.name()), ColumnDefinition.ColumnType.Double));
            }
            outputData = transposed;

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
        }
    }

    @Override
    public void outputData() {
        statisticDataController.loadData(outputColumns, outputData);
        drawChart();
    }

    public boolean initChart() {
        try {
            chartMaker.setDefaultChartTitle(chartTitle())
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(checkedColsNames.toString())
                    .setInvalidAs(invalidAs);
            chartMaker.setIsXY(!xyReverseCheck.isSelected());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void drawChart() {
        drawChartBoxWhisker();
    }

    public void drawChartBoxWhisker() {
        try {
            chartData = chartMax();
            if (chartData == null || chartData.isEmpty()) {
                return;
            }
            List<Integer> displayCols = new ArrayList<>();
            if (meanCheck.isSelected()) {
                displayCols.add(1);
            }
            for (int i = 2; i < 7; i++) {
                displayCols.add(i);
            }
            if (outliersCheck.isSelected()) {
                for (int i = 7; i < 11; i++) {
                    displayCols.add(i);
                }
            }
            chartController.writeXYChart(outputColumns, chartData, 0, displayCols);
            chartMaker.getBoxWhiskerChart()
                    .setBoxWidth(boxWidth)
                    .setHandleMean(meanCheck.isSelected())
                    .setHandleOutliers(outliersCheck.isSelected())
                    .displayBoxWhisker();
            setLinesStyle();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String baseChartTitle() {
        return data2D.displayName() + " - " + message("BoxWhiskerChart");
    }

    public void setLinesStyle() {
        try {
            if (isSettingValues) {
                return;
            }
            BoxWhiskerChart boxWhiskerChart = chartMaker.getBoxWhiskerChart();
            List<XYChart.Series> seriesList = boxWhiskerChart.getData();
            if (seriesList == null || seriesList.size() != boxWhiskerChart.expectedSeriesSize()) {
                return;
            }
            chartMaker.setChartStyle();

            for (XYChart.Series series : seriesList) {
                if (meanCheck.isSelected()) {
                    setStyle(series, message("Mean"), meanLineCheck);
                }
                setStyle(series, message("MinimumQ0"), q0Check);
                setStyle(series, message("LowerQuartile"), q1Check);
                setStyle(series, message("Median"), q2Check);
                setStyle(series, message("UpperQuartile"), q3Check);
                setStyle(series, message("MaximumQ4"), q4Check);
                if (outliersCheck.isSelected()) {
                    setStyle(series, message("UpperExtremeOutlierLine"), e4Check);
                    setStyle(series, message("UpperMildOutlierLine"), e3Check);
                    setStyle(series, message("LowerMildOutlierLine"), e2Check);
                    setStyle(series, message("LowerExtremeOutlierLine"), e1Check);
                }
            }

            boxWhiskerChart.applyCss();
            boxWhiskerChart.layout();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setStyle(XYChart.Series series, String name, CheckBox checkbox) {
        if (name == null || series == null || checkbox == null) {
            return;
        }
        if (series.getName().endsWith(name)) {
            Node node = series.getNode().lookup(".chart-series-line");
            if (node != null) {
                node.setVisible(checkbox.isSelected());
            }
        }
    }

    public void setLineVisible(String name, boolean visible) {
        List<XYChart.Series> seriesList = chartMaker.getBoxWhiskerChart().getData();
        if (name == null || seriesList == null) {
            return;
        }
        for (XYChart.Series series : seriesList) {
            if (series.getName().endsWith(name)) {
                Node node = series.getNode().lookup(".chart-series-line");
                if (node != null) {
                    node.setVisible(visible);
                }
                return;
            }
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        isSettingValues = true;
        q0Check.setSelected(true);
        q1Check.setSelected(true);
        q2Check.setSelected(true);
        q3Check.setSelected(true);
        q4Check.setSelected(true);
        e4Check.setSelected(true);
        e3Check.setSelected(true);
        e2Check.setSelected(true);
        e1Check.setSelected(true);
        meanLineCheck.setSelected(true);
        isSettingValues = false;
        setLinesStyle();
    }

    @FXML
    @Override
    public void selectNoneAction() {
        isSettingValues = true;
        q0Check.setSelected(false);
        q1Check.setSelected(false);
        q2Check.setSelected(false);
        q3Check.setSelected(false);
        q4Check.setSelected(false);
        e4Check.setSelected(false);
        e3Check.setSelected(false);
        e2Check.setSelected(false);
        e1Check.setSelected(false);
        meanLineCheck.setSelected(false);
        isSettingValues = false;
        setLinesStyle();
    }

    /*
        static
     */
    public static Data2DChartBoxWhiskerController open(ControlData2DLoad tableController) {
        try {
            Data2DChartBoxWhiskerController controller = (Data2DChartBoxWhiskerController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartBoxWhiskerFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
