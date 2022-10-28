package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.chart.XYChartMaker;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public class ControlData2DChartXY extends BaseData2DChartFx {

    protected XYChartMaker<Axis, Axis> chartMaker;
    protected Data2DChartXYOptionsController optionsController;

    public ControlData2DChartXY() {
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            chartMaker = new XYChartMaker();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void writeXYChart(List<Data2DColumn> columns, List<List<String>> data) {
        List<Integer> valueIndices = new ArrayList<>();
        for (int i = 1; i < columns.size(); i++) {
            valueIndices.add(i);
        }
        writeXYChart(columns, data, 0, valueIndices);
    }

    public void writeXYChart(List<Data2DColumn> columns, List<List<String>> data,
            int catgoryCol, List<Integer> valueCols) {
        this.columns = columns;
        this.data = data;
        chartMaker.setPalette(makePalette());
        chartMaker.makeChart();
        setChart(chartMaker.getXyChart());
        chartMaker.writeXYChart(columns, data, catgoryCol, valueCols);
        if (optionsController != null && optionsController.isShowing()
                && !chartMaker.getChartName().equals(optionsController.chartName)) {
            optionsController.close();
            optionsController = Data2DChartXYOptionsController.open(this);
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (optionsController != null) {
            optionsController.close();
        }
        optionsController = Data2DChartXYOptionsController.open(this);
        return true;
    }

}
