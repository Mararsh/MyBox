package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
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
public class Data2DChartBoxWhiskerController extends BaseData2DChartXYController {

    protected int lineWidth, categorysCol, boxWidth;
    protected LabeledBoxWhiskerChart boxWhiskerChart;
    protected StatisticCalculation calculation;
    protected Node line0, line1, line2, line3, line4, line5, line6, line7, line8;

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
            dottedCheck, outliersCheck;
    @FXML
    protected Button colorButton;

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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(colorButton, new Tooltip(message("RandomColors")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            super.initPlotTab();

            q0Check.setSelected(UserConfig.getBoolean(baseName + "LineQ0", true));
            q0Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line0 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ0", q0Check.isSelected());
                    line0.setVisible(q0Check.isSelected());
                }
            });

            q1Check.setSelected(UserConfig.getBoolean(baseName + "LineQ1", true));
            q1Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line1 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ1", q1Check.isSelected());
                    line1.setVisible(q1Check.isSelected());
                }
            });

            q2Check.setSelected(UserConfig.getBoolean(baseName + "LineQ2", true));
            q2Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line2 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ2", q2Check.isSelected());
                    line2.setVisible(q2Check.isSelected());
                }
            });

            q3Check.setSelected(UserConfig.getBoolean(baseName + "LineQ3", true));
            q3Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line3 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ3", q3Check.isSelected());
                    line3.setVisible(q3Check.isSelected());
                }
            });

            q4Check.setSelected(UserConfig.getBoolean(baseName + "LineQ4", true));
            q4Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line4 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ4", q4Check.isSelected());
                    line4.setVisible(q4Check.isSelected());
                }
            });

            q5Check.setSelected(UserConfig.getBoolean(baseName + "LineQ5", true));
            q5Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line5 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ5", q5Check.isSelected());
                    line5.setVisible(q5Check.isSelected());
                }
            });

            q6Check.setSelected(UserConfig.getBoolean(baseName + "LineQ6", true));
            q6Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line6 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ6", q6Check.isSelected());
                    line6.setVisible(q6Check.isSelected());
                }
            });

            q7Check.setSelected(UserConfig.getBoolean(baseName + "LineQ7", true));
            q7Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line7 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ7", q7Check.isSelected());
                    line7.setVisible(q7Check.isSelected());
                }
            });

            q8Check.setSelected(UserConfig.getBoolean(baseName + "LineQ8", true));
            q8Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line8 == null) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "LineQ8", q8Check.isSelected());
                    line8.setVisible(q8Check.isSelected());
                }
            });

            dottedCheck.setSelected(UserConfig.getBoolean(baseName + "Dotted", true));
            dottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || line4 == null) {
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
                .setMedian(true)
                .setMaximum(true)
                .setMinimum(true)
                .setUpperQuartile(true)
                .setLowerQuartile(true)
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
            String prefix;
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
                prefix = (allRadio.isSelected() ? message("All") : message("Column")) + "-";
                outputColumns.add(new Data2DColumn(prefix + message("MinimumQ0"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("LowerQuartile"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("Median"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("UpperQuartile"), ColumnDefinition.ColumnType.Double));
                outputColumns.add(new Data2DColumn(prefix + message("MaximumQ4"), ColumnDefinition.ColumnType.Double));
                outputData = transposed;
            } else {
                prefix = message("Row") + "-";
            }
            outputColumns.add(new Data2DColumn(prefix + message("UpperExtremeOutlierLine"), ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn(prefix + message("UpperMildOutlierLine"), ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn(prefix + message("LowerMildOutlierLine"), ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn(prefix + message("LowerExtremeOutlierLine"), ColumnDefinition.ColumnType.Double));
            for (int r = 0; r < outputData.size(); ++r) {
                List<String> row = outputData.get(r);
                double q3 = data2D.doubleValue(row.get(4));
                double q1 = data2D.doubleValue(row.get(2));
                double IQR = q3 - q1;
                row.add(DoubleTools.format(q3 + 3 * IQR, scale));
                row.add(DoubleTools.format(q3 + 1.5 * IQR, scale));
                row.add(DoubleTools.format(q1 - 1.5 * IQR, scale));
                row.add(DoubleTools.format(q1 - 3 * IQR, scale));
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
        line0 = null;
        line1 = null;
        line2 = null;
        line3 = null;
        line4 = null;
        line5 = null;
        line6 = null;
        line7 = null;
        line8 = null;
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
                    .setPalette(palette);
            List<Data2DColumn> displayColumns;
            if (outliersCheck.isSelected()) {
                displayColumns = outputColumns;
            } else {
                displayColumns = new ArrayList<>();
                displayColumns.addAll(outputColumns.subList(0, 6));
            }
            writeXYChart(displayColumns, outputData);
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
                    .setPalette(palette);
            List<XYChart.Series> seriesList = boxWhiskerChart.getData();
            if (seriesList == null || seriesList.size() != (outliersCheck.isSelected() ? 9 : 5)) {
                return;
            }
            boxWhiskerChart.applyCss();
            boxWhiskerChart.layout();

            ChartTools.setLineChartColors(boxWhiskerChart, lineWidth, palette, legendSide != null);

            String style = "; -fx-stroke-dash-array: 8 8;";
            line0 = seriesList.get(0).getNode().lookup(".chart-series-line");
            line0.setVisible(q0Check.isSelected());
            if (dottedCheck.isSelected()) {
                line0.setStyle(line0.getStyle() + style);
            }

            line1 = seriesList.get(1).getNode().lookup(".chart-series-line");
            line1.setVisible(q1Check.isSelected());
            if (dottedCheck.isSelected()) {
                line1.setStyle(line1.getStyle() + style);
            }

            line2 = seriesList.get(2).getNode().lookup(".chart-series-line");
            line2.setVisible(q2Check.isSelected());
            if (dottedCheck.isSelected()) {
                line2.setStyle(line2.getStyle() + style);
            }

            line3 = seriesList.get(3).getNode().lookup(".chart-series-line");
            line3.setVisible(q3Check.isSelected());
            if (dottedCheck.isSelected()) {
                line3.setStyle(line3.getStyle() + style);
            }

            line4 = seriesList.get(4).getNode().lookup(".chart-series-line");
            line4.setVisible(q4Check.isSelected());
            if (dottedCheck.isSelected()) {
                line4.setStyle(line4.getStyle() + style);
            }

            if (outliersCheck.isSelected()) {
                line5 = seriesList.get(5).getNode().lookup(".chart-series-line");
                line5.setVisible(q5Check.isSelected());
                if (dottedCheck.isSelected()) {
                    line5.setStyle(line5.getStyle() + style);
                }

                line6 = seriesList.get(6).getNode().lookup(".chart-series-line");
                line6.setVisible(q6Check.isSelected());
                if (dottedCheck.isSelected()) {
                    line6.setStyle(line6.getStyle() + style);
                }

                line7 = seriesList.get(7).getNode().lookup(".chart-series-line");
                line7.setVisible(q7Check.isSelected());
                if (dottedCheck.isSelected()) {
                    line7.setStyle(line7.getStyle() + style);
                }

                line8 = seriesList.get(8).getNode().lookup(".chart-series-line");
                line8.setVisible(q8Check.isSelected());
                if (dottedCheck.isSelected()) {
                    line8.setStyle(line8.getStyle() + style);
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
