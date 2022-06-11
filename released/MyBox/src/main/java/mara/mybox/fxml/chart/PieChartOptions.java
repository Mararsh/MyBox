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
public class PieChartOptions extends ChartOptions {

    protected PieChart pieChart;

    protected boolean clockwise;

    public PieChartOptions() {
        chartType = ChartType.Pie;
    }

    public final void initPieOptions() {
        try {
            if (chartName == null) {
                return;
            }
            initChartOptions();
            clockwise = UserConfig.getBoolean(chartName + "Clockwise", false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        UserConfig.setBoolean(chartName + "Clockwise", clockwise);
        if (pieChart != null) {
            pieChart.setClockwise(clockwise);
        }
    }

}
