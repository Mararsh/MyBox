package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.CheckBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartPieController extends BaseData2DChartFxController {

    protected PieChart pieChart;
    protected List<String> paletteList;

    @FXML
    protected CheckBox clockwiseCheck;

    public Data2DChartPieController() {
        baseTitle = message("PieChart");
        TipsLabelKey = "DataChartPieTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            sourceController.noColumnSelection(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initPlotTab() {
        try {
            super.initPlotTab();

            clockwiseCheck.setSelected(UserConfig.getBoolean(baseName + "Clockwise", false));
            clockwiseCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Clockwise", clockwiseCheck.isSelected());
                if (pieChart != null) {
                    pieChart.setClockwise(clockwiseCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void checkChartType() {
        try {
            setSourceLabel(message("PieChartLabel"));
            checkAutoTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        dataColsIndices = new ArrayList<>();
        outputColumns = new ArrayList<>();
        outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
        int categoryCol = data2D.colOrder(selectedCategory);
        if (categoryCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.add(categoryCol);
        outputColumns.add(data2D.column(categoryCol));
        int valueCol = data2D.colOrder(selectedValue);
        if (valueCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.add(valueCol);
        outputColumns.add(data2D.column(valueCol));
        return true;
    }

    @Override
    public void clearChart() {
        super.clearChart();
        pieChart = null;
        paletteList = null;
    }

    @Override
    public void makeChart() {
        try {
            pieChart = new PieChart();
            pieChart.setClockwise(clockwiseCheck.isSelected());
            pieChart.setLabelLineLength(10d);
            chart = pieChart;
            makeFinalChart();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void writeChartData() {
        try {
            Random random = new Random();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            pieChart.setData(pieData);
            double total = 0;
            for (List<String> rowData : outputData) {
                double d = data2D.doubleValue(rowData.get(2));
                if (d > 0) {
                    total += d;
                }
            }
            if (total == 0) {
                return;
            }
            String label;
            paletteList = new ArrayList();
            boolean disName = chartController.disName();
            for (List<String> rowData : outputData) {
                String name = rowData.get(1);
                double d = data2D.doubleValue(rowData.get(2));
                if (d <= 0) {
                    continue;
                }
                double percent = DoubleTools.scale(d * 100 / total, scale);
                String value = DoubleTools.format(d, scale);
                switch (chartController.labelType) {
                    case Category:
                        label = (disName ? selectedCategory + ": " : "") + name;
                        break;
                    case Value:
                        label = (disName ? selectedValue + ": " : "") + value + "=" + percent + "%";
                        break;
                    case CategoryAndValue:
                        label = (disName ? selectedCategory + ": " : "") + name + "\n"
                                + (disName ? selectedValue + ": " : "") + value + "=" + percent + "%";
                        break;
                    case NotDisplay:
                    case Point:
                    case Pop:
                    default:
                        label = name;
                        break;
                }
                PieChart.Data item = new PieChart.Data(label, d);
                pieData.add(item);
                if (chartController.popLabel() || chartController.labelType == LabelType.Pop) {
                    NodeStyleTools.setTooltip(item.getNode(),
                            selectedCategory + ": " + name + "\n"
                            + selectedValue + ": " + value + "=" + percent + "%");
                }
                paletteList.add(FxColorTools.randomRGB(random));
            }

            pieChart.setLabelsVisible(chartController.labelType == LabelType.Category
                    || chartController.labelType == LabelType.Value
                    || chartController.labelType == LabelType.CategoryAndValue);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setChartStyle() {
        if (pieChart == null) {
            return;
        }
        ChartTools.setPieColors(pieChart, paletteList, legendSide != null);
        pieChart.requestLayout();
    }

    /*
        static
     */
    public static Data2DChartPieController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartPieController controller = (Data2DChartPieController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartPieFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
