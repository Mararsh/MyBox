package mara.mybox.fxml.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2022-5-16
 * @License Apache License Version 2.0
 */
public class PieChartMaker extends PieChartOptions {

    public PieChartMaker() {
        chartType = ChartType.Pie;
    }

    public PieChartMaker init(String chartName) {
        clearChart();
        this.chartName = chartName;
        initPieOptions();
        return this;
    }

    @Override
    public void clearChart() {
        super.clearChart();
        pieChart = null;
    }

    public PieChart makeChart() {
        try {
            clearChart();
            chartType = ChartType.Pie;
            if (chartName == null) {
                return null;
            }
            initPieChart();
            styleChart();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return pieChart;
    }

    public void initPieChart() {
        try {
            pieChart = new PieChart();
            chart = pieChart;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeChart(List<Data2DColumn> columns, List<List<String>> data) {
        try {
            Random random = new Random();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            pieChart.setData(pieData);
            if (legendSide == null) {
                pieChart.setLegendSide(Side.TOP);
            }
            double total = 0;
            for (List<String> rowData : data) {
                double d = scaleValue(rowData.get(2));
                if (d > 0) {
                    total += d;
                }
            }
            if (total == 0) {
                return;
            }
            String label;
            List<String> paletteList = new ArrayList();
            for (List<String> rowData : data) {
                String name = rowData.get(1);
                double d = doubleValue(rowData.get(2));
                if (d <= 0) {
                    continue;
                }
                double percent = DoubleTools.scale(d * 100 / total, scale);
                String value = DoubleTools.format(d, scale);
                switch (labelType) {
                    case Category:
                        label = (displayLabelName ? getCategoryLabel() + ": " : "") + name;
                        break;
                    case Value:
                        label = (displayLabelName ? getValueLabel() + ": " : "") + value + "=" + percent + "%";
                        break;
                    case CategoryAndValue:
                        label = (displayLabelName ? getCategoryLabel() + ": " : "") + name + "\n"
                                + (displayLabelName ? getValueLabel() + ": " : "") + value + "=" + percent + "%";
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
                if (popLabel()) {
                    NodeStyleTools.setTooltip(item.getNode(),
                            getCategoryLabel() + ": " + name + "\n"
                            + getValueLabel() + ": " + value + "=" + percent + "%");
                }
                paletteList.add(FxColorTools.randomRGB(random));
            }

            pieChart.setLegendVisible(legendSide == null);
            pieChart.setLabelsVisible(labelVisible());
            pieChart.setClockwise(clockwise);
            ChartTools.setPieColors(pieChart, paletteList, showLegend(), labelFontSize);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
