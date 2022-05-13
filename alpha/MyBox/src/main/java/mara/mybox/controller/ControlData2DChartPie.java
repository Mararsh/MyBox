package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.PieChartOption;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public class ControlData2DChartPie extends ControlData2DChartFx {

    protected PieChartOption chartOptions;
    protected PieChart pieChart;
    protected Data2D data2D;
    protected String categoryName, valueName;

    public ControlData2DChartPie() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initChart(String chartName) {
        try {
            if (chartName == null) {
                return;
            }
            chartOptions = new PieChartOption(chartName);
            pieChart = chartOptions.makeChart();

            setChart(pieChart, chartOptions);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeChart(List<Data2DColumn> columns, List<List<String>> data) {
        try {
            Random random = new Random();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            pieChart.setData(pieData);
            double total = 0;
            for (List<String> rowData : data) {
                double d = data2D.doubleValue(rowData.get(2));
                if (d > 0) {
                    total += d;
                }
            }
            if (total == 0) {
                return;
            }
            String label;
            List<String> paletteList = new ArrayList();
            boolean disName = chartOptions.isDisplayLabelName();
            for (List<String> rowData : data) {
                String name = rowData.get(1);
                double d = data2D.doubleValue(rowData.get(2));
                if (d <= 0) {
                    continue;
                }
                double percent = DoubleTools.scale(d * 100 / total, chartOptions.getScale());
                String value = DoubleTools.format(d, chartOptions.getScale());
                switch (chartOptions.getLabelType()) {
                    case Category:
                        label = (disName ? categoryName + ": " : "") + name;
                        break;
                    case Value:
                        label = (disName ? valueName + ": " : "") + value + "=" + percent + "%";
                        break;
                    case CategoryAndValue:
                        label = (disName ? categoryName + ": " : "") + name + "\n"
                                + (disName ? valueName + ": " : "") + value + "=" + percent + "%";
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
                if (chartOptions.popLabel()) {
                    NodeStyleTools.setTooltip(item.getNode(),
                            categoryName + ": " + name + "\n"
                            + valueName + ": " + value + "=" + percent + "%");
                }
                paletteList.add(FxColorTools.randomRGB(random));
            }

            pieChart.setLabelsVisible(chartOptions.labelVisible());
            ChartTools.setPieColors(pieChart, paletteList, chartOptions.showLegend());

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        Data2DChartPieOptionsController.open(this);
        return true;
    }

}
