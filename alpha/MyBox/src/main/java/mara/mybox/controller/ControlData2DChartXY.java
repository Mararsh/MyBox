package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.chart.XYChartOptions;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public class ControlData2DChartXY extends BaseData2DChartFx {

    protected XYChartOptions xyOptions;
    protected List<Integer> colIndics;
    protected boolean rowNumber;
    protected Data2DChartXYOptionsController optionsController;

    public ControlData2DChartXY() {
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            xyOptions = new XYChartOptions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void redraw() {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            writeXYChart(columns, data, colIndics, rowNumber);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        xyOptions.makeChart();
        setChart(xyOptions.getXyChart());
        xyOptions.writeXYChart(columns, data, colIndics, rowNumber);
        if (optionsController != null && optionsController.isShowing()
                && !xyOptions.getChartName().equals(optionsController.chartName)) {
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
