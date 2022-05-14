package mara.mybox.fxml.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-13
 * @License Apache License Version 2.0
 */
public class PieChartOption extends ChartOptions {

    protected PieChart pieChart;

    protected boolean clockwise;

    public PieChartOption(String chartName) {
        super(chartName);
        initPieOptions();
    }

    public final void initPieOptions() {
        try {
            if (chartName == null) {
                return;
            }

            clockwise = UserConfig.getBoolean(chartName + "Clockwise", false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public PieChart makeChart() {
        try {
            initPieChart();
            styleChart();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return pieChart;
    }

    public void initPieChart() {
        try {

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
            double total = 0;
            for (List<String> rowData : data) {
                double d = doubleValue(rowData.get(2));
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
                        label = (displayLabelName ? categoryLabel + ": " : "") + name;
                        break;
                    case Value:
                        label = (displayLabelName ? valueLabel + ": " : "") + value + "=" + percent + "%";
                        break;
                    case CategoryAndValue:
                        label = (displayLabelName ? categoryLabel + ": " : "") + name + "\n"
                                + (displayLabelName ? valueLabel + ": " : "") + value + "=" + percent + "%";
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
                            categoryLabel + ": " + name + "\n"
                            + valueLabel + ": " + value + "=" + percent + "%");
                }
                paletteList.add(FxColorTools.randomRGB(random));
            }

            pieChart.setLabelsVisible(labelVisible());
            ChartTools.setPieColors(pieChart, paletteList, showLegend());

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        get/set
     */
    public PieChart getPieChart() {
        return pieChart;
    }

    public void setPieChart(PieChart pieChart) {
        this.pieChart = pieChart;
    }

    public Map<Node, Node> getNodeLabels() {
        return nodeLabels;
    }

    public void setNodeLabels(Map<Node, Node> nodeLabels) {
        this.nodeLabels = nodeLabels;
    }

    public boolean isClockwise() {
        return clockwise;
    }

    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }

}
