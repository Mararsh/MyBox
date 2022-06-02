package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.BoxWhiskerChart;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.fxml.style.NodeStyleTools;
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
            dottedCheck, outliersCheck, meanCheck, meanLineCheck;

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

            chartController.dataController = this;
            chartMaker = chartController.chartMaker;
            chartMaker.init(ChartType.BoxWhiskerChart, message("BoxWhiskerChart"));

            initBoxOptions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

            isSettingValues = true;
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            categoryColumnSelector.getItems().add(0, message("RowNumber"));
            categoryColumnSelector.setValue(selectedCategory);
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initBoxOptions() {
        try {
            lines = new HashMap<>();

            q0Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
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

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        objectChanged();

        categorysCol = -1;
        if (rowsRadio.isSelected() && selectedCategory != null
                && categoryColumnSelector.getSelectionModel().getSelectedIndex() != 0) {
            categorysCol = data2D.colOrder(selectedCategory);
        }
        calculation = new DescriptiveStatistic()
                .setMean(true)
                .setMedian(true)
                .setMaximum(true)
                .setMinimum(true)
                .setUpperQuartile(true)
                .setLowerQuartile(true)
                .setUpperExtremeOutlierLine(true)
                .setUpperMildOutlierLine(true)
                .setLowerMildOutlierLine(true)
                .setLowerExtremeOutlierLine(true)
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
                .setColsIndices(selectController.checkedColsIndices())
                .setColsNames(selectController.checkedColsNames())
                .setCategoryName(categorysCol >= 0 ? selectedCategory : null);

        return ok;
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            if (categorysCol >= 0) {
                dataColsIndices.add(0, categorysCol);
            }
            return calculation.prepare();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void readData() {
        try {
            if (selectController.allPages()) {
                outputData = data2D.allRows(dataColsIndices, rowsRadio.isSelected() && categorysCol < 0);
            } else {
                outputData = selectController.selectedData(selectController.checkedRowsIndices(),
                        dataColsIndices, rowsRadio.isSelected() && categorysCol < 0);
            }
            if (outputData == null) {
                return;
            }
            calculation.setTask(task);
            if (!calculation.statisticData(outputData)) {
                calculation.setTask(null);
                outputData = null;
                return;
            }

            calculation.setTask(null);
            outputColumns = calculation.getOutputColumns();
            outputData = calculation.getOutputData();
            if (!rowsRadio.isSelected()) {
                List<List<String>> transposed = new ArrayList<>();
                for (int r = 1; r < outputColumns.size(); ++r) {
                    List<String> row = new ArrayList<>();
                    row.add(outputColumns.get(r).getColumnName());
                    for (int c = 0; c < outputData.size(); ++c) {
                        row.add(outputData.get(c).get(r));
                    }
                    transposed.add(row);
                }
                outputColumns = new ArrayList<>();
                outputColumns.add(new Data2DColumn(categoryName(), ColumnDefinition.ColumnType.String));
                String prefix = (allRadio.isSelected() ? message("All") : message("Column")) + "-";
                outputColumns.add(new Data2DColumn(prefix + message("Mean"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("MinimumQ0"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("LowerQuartile"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("Median"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("UpperQuartile"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("MaximumQ4"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("UpperExtremeOutlierLine"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("UpperMildOutlierLine"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("LowerMildOutlierLine"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("LowerExtremeOutlierLine"), ColumnDefinition.ColumnType.Double));
                outputData = transposed;
            }

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
        }
    }

    @Override
    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }

            List<Integer> displayCols;
            if (outliersCheck.isSelected() && meanCheck.isSelected()) {
                displayCols = null;
            } else {
                displayCols = new ArrayList<>();
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
            }
            chartMaker.setDefaultChartTitle((selectedCategory != null ? selectedCategory + " - " : "")
                    + calculation.getColsNames())
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(calculation.getColsNames().toString())
                    .setPalette(makePalette());
            chartController.writeXYChart(outputColumns, outputData, displayCols, false);
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

    public void setLinesStyle() {
        try {
            if (isSettingValues) {
                return;
            }
            BoxWhiskerChart boxWhiskerChart = chartMaker.getBoxWhiskerChart();
            List<XYChart.Series> seriesList = boxWhiskerChart.getData();
            if (seriesList == null || seriesList.size() != boxWhiskerChart.seriesSize()) {
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
    public void randomColors() {
        try {
            chartMaker.setPalette(makePalette());
            setLinesStyle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public Map<String, String> makePalette() {
        try {
            Random random = new Random();
            if (palette == null) {
                palette = new HashMap();
            } else {
                palette.clear();
            }
            for (int i = 0; i < outputColumns.size(); i++) {
                Data2DColumn column = outputColumns.get(i);
                column.setColor(FxColorTools.randomColor(random));
                String rgb = FxColorTools.color2rgb(FxColorTools.randomColor(random));
                palette.put(column.getColumnName(), rgb);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return palette;
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
    public static Data2DChartBoxWhiskerController open(ControlData2DEditTable tableController) {
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
