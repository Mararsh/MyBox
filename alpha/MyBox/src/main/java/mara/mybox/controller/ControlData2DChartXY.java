package mara.mybox.controller;

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
    protected List<Integer> colIndics;
    protected boolean rowNumber;
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
        writeXYChart(columns, data, null, true);
    }

    public void writeXYChart(List<Data2DColumn> columns, List<List<String>> data,
            List<Integer> colIndics, boolean rowNumber) {
        this.columns = columns;
        this.data = data;
        this.colIndics = colIndics;
        this.rowNumber = rowNumber;
        chartMaker.makeChart();
        setChart(chartMaker.getXyChart());
        chartMaker.writeXYChart(columns, data, colIndics, rowNumber);
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
