package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.XYChartOptions;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public class ControlData2DChartXY extends ControlData2DChartFx {

    protected XYChartOptions chartOptions;
    protected XYChart xyChart;
    protected Data2D data2D;

    public ControlData2DChartXY() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initChart(ChartType chartType, String chartName) {
        try {
            if (chartType == null || chartName == null) {
                return;
            }
            chartOptions = new XYChartOptions(chartType, chartName);
            xyChart = chartOptions.getXyChart();

            setChart(xyChart, chartOptions);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeChart(List<Data2DColumn> columns, List<List<String>> data, boolean rowNumber) {
        writeChart(columns, data, null, rowNumber);
    }

    // The first column is row number and the second columns is "Category"
    public void writeChart(List<Data2DColumn> columns, List<List<String>> data,
            List<Integer> colIndics, boolean rowNumber) {
        try {
            if (columns == null || data == null) {
                return;
            }
            xyChart.getData().clear();
            XYChart.Data xyData;
            int index = 0, startIndex = rowNumber ? 1 : 0;
            for (int col = 1 + startIndex; col < columns.size(); col++) {
                if (colIndics != null && !colIndics.contains(col)) {
                    continue;
                }
                Data2DColumn column = columns.get(col);
                String colName = column.getColumnName();
                XYChart.Series series = new XYChart.Series();
                series.setName(colName);

                double categoryValue, categoryCoordinateValue, numberValue, numberCoordinateValue;
                for (List<String> rowData : data) {
                    String category = rowData.get(startIndex);
                    numberValue = data2D.doubleValue(rowData.get(col));
                    numberCoordinateValue = ChartTools.coordinateValue(chartOptions.getNumberCoordinate(), numberValue);
                    categoryValue = data2D.doubleValue(category);
                    categoryCoordinateValue = ChartTools.coordinateValue(chartOptions.getCategoryCoordinate(), categoryValue);
                    if (chartOptions.isIsXY()) {
                        if (xyChart.getXAxis() instanceof NumberAxis) {
                            xyData = new XYChart.Data(categoryCoordinateValue, numberCoordinateValue);
                        } else {
                            xyData = new XYChart.Data(category, numberCoordinateValue);
                        }
                    } else {
                        if (xyChart.getYAxis() instanceof NumberAxis) {
                            xyData = new XYChart.Data(numberCoordinateValue, categoryCoordinateValue);
                        } else {
                            xyData = new XYChart.Data(numberCoordinateValue, category);
                        }
                    }
                    series.getData().add(xyData);
                }

                xyChart.getData().add(index++, series);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        Data2DChartXYOptionsController.open(this);
        return true;
    }

}
