package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.chart.PieChartMaker;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public class ControlData2DChartPie extends BaseData2DChartFx {

    protected PieChartMaker pieMaker;
    protected String categoryName, valueName;
    protected Data2DChartPieOptionsController optionsController;

    public ControlData2DChartPie() {
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            pieMaker = new PieChartMaker();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void writeChart(List<Data2DColumn> columns, List<List<String>> data, boolean hasRowNumber) {
        this.columns = columns;
        this.data = data;
        pieMaker.makeChart();
        setChart(pieMaker.getPieChart());
        pieMaker.writeChart(columns, data, hasRowNumber);
        if (optionsController != null && optionsController.isShowing()
                && !pieMaker.getChartName().equals(optionsController.chartName)) {
            optionsController.close();
            optionsController = Data2DChartPieOptionsController.open(this);
        }

    }

    @FXML
    @Override
    public boolean menuAction() {
        Data2DChartPieOptionsController.open(this);
        return true;
    }

}
