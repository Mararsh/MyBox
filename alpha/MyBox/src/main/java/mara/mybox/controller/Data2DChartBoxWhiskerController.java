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
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.StatisticCalculation;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledBoxWhiskerChart;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-27
 * @License Apache License Version 2.0
 */
public class Data2DChartBoxWhiskerController extends BaseData2DChartXYController {

    protected int lineWidth, categorysCol, boxWidth;
    protected LabeledBoxWhiskerChart boxWhiskerChart;
    protected StatisticCalculation calculation;
    protected Map<String, Node> lines;

    @FXML
    protected ComboBox<String> lineWdithSelector, boxWdithSelector;
    @FXML
    protected VBox dataOptionsBox;
    @FXML
    protected HBox lineWidthBox;
    @FXML
    protected RadioButton categoryStringRadio, categoryNumberRadio;
    @FXML
    protected ToggleGroup categoryValuesGroup;
    @FXML
    protected CheckBox q0Check, q1Check, q2Check, q3Check, q4Check, q5Check, q6Check, q7Check, q8Check,
            dottedCheck, outliersCheck, meanCheck, meanLineCheck;

    public Data2DChartBoxWhiskerController() {
        baseTitle = message("BoxWhiskerChart");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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

    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();

            lineWidth = UserConfig.getInt(baseName + "LineWidth", 4);
            if (lineWidth < 0) {
                lineWidth = 1;
            }

            lineWdithSelector.getItems().addAll(Arrays.asList(
                    "4", "1", "2", "3", "5", "6", "7", "8", "9", "10"
            ));
            lineWdithSelector.getSelectionModel().select(lineWidth + "");
            lineWdithSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v >= 0) {
                                lineWidth = v;
                                lineWdithSelector.getEditor().setStyle(null);
                                UserConfig.setInt(baseName + "LineWidth", lineWidth);
                                setChartStyle();
                            } else {
                                lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            lineWdithSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initCategoryTab() {
        try {
            super.initCategoryTab();

            if (UserConfig.getBoolean(baseName + "CountCategoryAsNumbers", false)) {
                categoryNumberRadio.fire();
            }
            categoryCoordinatePane.setVisible(categoryNumberRadio.isSelected());
            categoryValuesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "CountCategoryAsNumbers", categoryNumberRadio.isSelected());
                    categoryCoordinatePane.setVisible(categoryNumberRadio.isSelected());
                    redrawChart();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initBoxOptions() {
        try {
            lines = new HashMap<>();

            q0Check.setSelected(UserConfig.getBoolean(baseName + "LineQ0", true));
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

            q1Check.setSelected(UserConfig.getBoolean(baseName + "LineQ1", true));
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

            q2Check.setSelected(UserConfig.getBoolean(baseName + "LineQ2", true));
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

            q3Check.setSelected(UserConfig.getBoolean(baseName + "LineQ3", true));
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

            q4Check.setSelected(UserConfig.getBoolean(baseName + "LineQ4", true));
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

            q5Check.setSelected(UserConfig.getBoolean(baseName + "LineQ5", true));
            q5Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ5", q5Check.isSelected());
                    setLineVisible(message("UpperExtremeOutlierLine"), q5Check.isSelected());
                }
            });

            q6Check.setSelected(UserConfig.getBoolean(baseName + "LineQ6", true));
            q6Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ6", q6Check.isSelected());
                    setLineVisible(message("UpperMildOutlierLine"), q6Check.isSelected());
                }
            });

            q7Check.setSelected(UserConfig.getBoolean(baseName + "LineQ7", true));
            q7Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ7", q7Check.isSelected());
                    setLineVisible(message("LowerMildOutlierLine"), q7Check.isSelected());
                }
            });

            q8Check.setSelected(UserConfig.getBoolean(baseName + "LineQ8", true));
            q8Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ8", q8Check.isSelected());
                    setLineVisible(message("LowerExtremeOutlierLine"), q8Check.isSelected());
                }
            });

            meanLineCheck.setSelected(UserConfig.getBoolean(baseName + "LineMean", true));
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

            outliersCheck.setSelected(UserConfig.getBoolean(baseName + "Outliers", true));
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

            q5Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            q6Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            q7Check.disableProperty().bind(outliersCheck.selectedProperty().not());
            q8Check.disableProperty().bind(outliersCheck.selectedProperty().not());

            meanCheck.setSelected(UserConfig.getBoolean(baseName + "Mean", true));
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
    public String title() {
        return message("BoxWhiskerChart");
    }

    @Override
    public boolean isCategoryNumbers() {
        return rowsRadio.isSelected() && categoryNumberRadio.isSelected();
    }

    @Override
    public String categoryName() {
        if (rowsRadio.isSelected()) {
            return selectedCategory;
        } else if (columnsRadio.isSelected()) {
            return message("Column");
        } else {
            return "";
        }
    }

    @FXML
    @Override
    public void defaultValueLabel() {
        numberLabel.setText(message("Statistic"));
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
        calculation = new StatisticCalculation()
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
                calculation.setStatisticObject(StatisticCalculation.StatisticObject.Rows);
                break;
            case All:
                calculation.setStatisticObject(StatisticCalculation.StatisticObject.All);
                break;
            default:
                calculation.setStatisticObject(StatisticCalculation.StatisticObject.Columns);
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
        if (!super.initData()) {
            return false;
        }
        if (categorysCol >= 0) {
            dataColsIndices.add(0, categorysCol);
        }
        return calculation.prepare();
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
    public void clearChart() {
        super.clearChart();
        boxWhiskerChart = null;
        lines.clear();
    }

    @Override
    public void makeChart() {
        try {
            makeAxis();

            boxWhiskerChart = new LabeledBoxWhiskerChart(xAxis, yAxis);
            xyChart = boxWhiskerChart;
            boxWhiskerChart.setChartController(this);

            makeXYChart();
            makeFinalChart();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void writeChartData() {
        try {
            makePalette();
            boxWhiskerChart.setBoxWidth(boxWidth)
                    .setHandleOutliers(outliersCheck.isSelected())
                    .setHandleMean(meanCheck.isSelected())
                    .setPalette(palette);
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
            writeXYChart(outputColumns, outputData, displayCols);
        } catch (Exception e) {
            MyBoxLog.debug(e);
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

    @Override
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

            ChartTools.setLineChartColors(boxWhiskerChart, lineWidth, palette, legendSide != null);

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
                    setStyle(series, message("UpperExtremeOutlierLine"), q5Check);
                    setStyle(series, message("UpperMildOutlierLine"), q6Check);
                    setStyle(series, message("LowerMildOutlierLine"), q7Check);
                    setStyle(series, message("LowerExtremeOutlierLine"), q8Check);
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
    public void makePalette() {
        try {
            Random random = new Random();
            palette = new HashMap();
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
        q5Check.setSelected(true);
        q6Check.setSelected(true);
        q7Check.setSelected(true);
        q8Check.setSelected(true);
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
        q5Check.setSelected(false);
        q6Check.setSelected(false);
        q7Check.setSelected(false);
        q8Check.setSelected(false);
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
