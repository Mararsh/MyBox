package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.chart.PieChartOption;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public class ControlData2DChartPie extends ControlData2DChartFx {

    protected PieChartOption pieOptions;
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
            pieOptions = new PieChartOption(chartName);
            setChart(pieOptions.makeChart());

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeXYChart(List<Data2DColumn> columns, List<List<String>> data) {
        pieOptions.writeChart(columns, data);
    }

    @FXML
    @Override
    public boolean menuAction() {
        Data2DChartPieOptionsController.open(this);
        return true;
    }

}
