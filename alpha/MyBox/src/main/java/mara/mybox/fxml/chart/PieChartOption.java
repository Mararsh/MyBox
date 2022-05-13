package mara.mybox.fxml.chart;

import java.util.Map;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import mara.mybox.dev.MyBoxLog;
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
