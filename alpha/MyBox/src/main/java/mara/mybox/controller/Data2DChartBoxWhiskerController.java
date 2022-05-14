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
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.XYChartOptions;
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

    protected XYChartOptions xyOptions;
    protected int categorysCol, boxWidth;
    protected BoxWhiskerChart boxWhiskerChart;
    protected DescriptiveStatistic calculation;
    protected Map<String, Node> lines;

    @FXML
    protected ControlData2DChartXY chartController;
    @FXML
    protected ComboBox<String> boxWdithSelector;
    @FXML
    protected VBox dataOptionsBox;
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

            xyOptions = chartController.xyOptions;

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

            q0Check.setSelected(UserConfig.getBoolean(baseName + "LineQ0", false));
            q0Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ0", q0Check.isSelected());
                    setLineVisible(message("MinimumQ0"), q0Check.isSelected());
                }
            });

            q1Check.setSelected(UserConfig.getBoolean(baseName + "LineQ1", false));
            q1Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ1", q1Check.isSelected());
                    setLineVisible(message("LowerQuartile"), q1Check.isSelected());
                }
            });

            q2Check.setSelected(UserConfig.getBoolean(baseName + "LineQ2", false));
            q2Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ2", q2Check.isSelected());
                    setLineVisible(message("Median"), q2Check.isSelected());
                }
            });

            q3Check.setSelected(UserConfig.getBoolean(baseName + "LineQ3", false));
            q3Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ3", q3Check.isSelected());
                    setLineVisible(message("UpperQuartile"), q3Check.isSelected());
                }
            });

            q4Check.setSelected(UserConfig.getBoolean(baseName + "LineQ4", false));
            q4Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ4", q4Check.isSelected());
                    setLineVisible(message("MaximumQ4"), q4Check.isSelected());
                }
            });

            e4Check.setSelected(UserConfig.getBoolean(baseName + "LineQ5", false));
            e4Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ5", e4Check.isSelected());
                    setLineVisible(message("UpperExtremeOutlierLine"), e4Check.isSelected());
                }
            });

            e3Check.setSelected(UserConfig.getBoolean(baseName + "LineQ6", false));
            e3Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ6", e3Check.isSelected());
                    setLineVisible(message("UpperMildOutlierLine"), e3Check.isSelected());
                }
            });

            e2Check.setSelected(UserConfig.getBoolean(baseName + "LineQ7", false));
            e2Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ7", e2Check.isSelected());
                    setLineVisible(message("LowerMildOutlierLine"), e2Check.isSelected());
                }
            });

            e1Check.setSelected(UserConfig.getBoolean(baseName + "LineQ8", false));
            e1Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ8", e1Check.isSelected());
                    setLineVisible(message("LowerExtremeOutlierLine"), e1Check.isSelected());
                }
            });

            meanLineCheck.setSelected(UserConfig.getBoolean(baseName + "LineMean", false));
            meanLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineMean", meanLineCheck.isSelected());
                    setLineVisible(message("Mean"), meanLineCheck.isSelected());
                }
            });

            dottedCheck.setSelected(UserConfig.getBoolean(baseName + "Dotted", true));
            dottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "Dotted", dottedCheck.isSelected());
                    setChartStyle();
                }
            });

            outliersCheck.setSelected(UserConfig.getBoolean(baseName + "Outliers", false));
            outliersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "Outliers", outliersCheck.isSelected());
                    redrawChart();
                }
            });

            e4Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            e3Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            e2Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            e1Check.disableProperty().bind(outliersCheck.selectedProperty().not());

            meanCheck.setSelected(UserConfig.getBoolean(baseName + "Mean", false));
            meanCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "Mean", meanCheck.isSelected());
                    redrawChart();
                }
            });
            meanLineCheck.disableProperty().bind(meanCheck.selectedProperty().not());

            boxWidth = UserConfig.getInt(baseName + "BoxWidth", 40);
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
                            if (v >= 0) {
                                boxWidth = v;
                                boxWdithSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "BoxWidth", boxWidth);
                                redrawChart();
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
//        if (rowsRadio.isSelected()) {
//            if (!dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
//                dataOptionsBox.getChildren().add(1, categoryColumnsPane);
//            }
//        } else {
//            if (dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
//                dataOptionsBox.getChildren().remove(categoryColumnsPane);
//            }
//        }
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
                .setColsIndices(sourceController.checkedColsIndices())
                .setColsNames(sourceController.checkedColsNames())
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
            if (!calculation.prepare()) {
                return false;
            }
            xyOptions.setChartType(ChartType.BoxWhiskerChart)
                    .setChartName("BoxWhiskerChart")
                    .setCategoryLabel(selectedCategory)
                    .setValueLabel(selectedValue)
                    .setPalette(makePalette())
                    .initChartOptions();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void readData() {
        try {
            if (sourceController.allPages()) {
                outputData = data2D.allRows(dataColsIndices, rowsRadio.isSelected() && categorysCol < 0);
            } else {
                outputData = sourceController.selectedData(sourceController.checkedRowsIndices(),
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
            chartController.writeXYChart(outputColumns, outputData, displayCols, false);

            makePalette();
            setChartStyle();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setLineVisible(String name, boolean visible) {
        List<XYChart.Series> seriesList = boxWhiskerChart.getData();
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

    public void setStyle(XYChart.Series series, String name, CheckBox checkbox) {
        if (name == null || series == null || checkbox == null) {
            return;
        }
        if (series.getName().endsWith(name)) {
            Node node = series.getNode().lookup(".chart-series-line");
            if (node != null) {
                node.setVisible(checkbox.isSelected());
                if (dottedCheck.isSelected()) {
                    node.setStyle(node.getStyle() + "; -fx-stroke-dash-array: 8 8;");
                }
            }
        }
    }

    public void setChartStyle() {
        try {
            if (isSettingValues) {
                return;
            }
            boxWhiskerChart.setBoxWidth(boxWidth)
                    .setHandleOutliers(outliersCheck.isSelected())
                    .setHandleMean(meanCheck.isSelected())
                    .setPalette(palette);
            List<XYChart.Series> seriesList = boxWhiskerChart.getData();
            if (seriesList == null || seriesList.size() != boxWhiskerChart.seriesSize()) {
                return;
            }
            boxWhiskerChart.applyCss();
            boxWhiskerChart.layout();

            ChartTools.setLineChartColors(boxWhiskerChart, chartController.xyOptions.getLineWidth(), palette,
                    chartController.xyOptions.getLegendSide() != null);

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

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void randomColors() {
        try {
            makePalette();
            setChartStyle();
            boxWhiskerChart.refreshBox();
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
            boxWhiskerChart.setPalette(palette);
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
        setChartStyle();
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
        setChartStyle();
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
