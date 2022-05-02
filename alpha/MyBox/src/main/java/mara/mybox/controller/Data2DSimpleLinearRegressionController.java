package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledSimpleRegressionChart;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegressionController extends BaseData2DChartXYController {

    protected LabeledSimpleRegressionChart regressionChart​;
    protected SimpleRegression simpleRegression;
    protected List<Data2DColumn> resultColumns;
    protected List<List<String>> resultData;
    protected double intercept, slope;

    @FXML
    protected CheckBox interceptCheck, displayAllCheck, fittedCheck;
    @FXML
    protected ControlWebView resultsViewController;

    public Data2DSimpleLinearRegressionController() {
        baseTitle = message("SimpleLinearRegression");
        TipsLabelKey = "DataChartTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            resultsViewController.setParent(this);

            sourceController.noColumnSelection(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initDataTab() {
        try {
            super.initDataTab();

            interceptCheck.setSelected(UserConfig.getBoolean(baseName + "Intercept", true));
            interceptCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Intercept", interceptCheck.isSelected());
            });

            displayAllCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayAll", true));
            displayAllCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayAll", displayAllCheck.isSelected());
                noticeMemory();
            });

            displayAllCheck.visibleProperty().bind(sourceController.allPagesCheck.selectedProperty());

            fittedCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayFitted", true));
            fittedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayFitted", fittedCheck.isSelected());
                regressionChart​.displayFitted(fittedCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void checkChartType() {
        try {
            setSourceLabel(message("LinerRegressionSourceLabel"));
            checkAutoTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean isCategoryNumbers() {
        return true;
    }

    @FXML
    @Override
    public void defaultValueLabel() {
        numberLabel.setText(valueName());
    }

    @Override
    public void noticeMemory() {
        if (isSettingValues) {
            return;
        }
        if (sourceController.allPages() && displayAllCheck.isSelected()) {
            infoLabel.setText(message("AllRowsLoadComments"));
        } else {
            infoLabel.setText("");
        }

    }

    @Override
    public boolean initData() {
        dataColsIndices = new ArrayList<>();
        outputColumns = new ArrayList<>();
        int categoryCol = data2D.colOrder(selectedCategory);
        if (categoryCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.add(categoryCol);
        outputColumns.add(data2D.column(categoryCol));
        int valueCol = data2D.colOrder(selectedValue);
        if (valueCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.add(valueCol);
        outputColumns.add(data2D.column(valueCol));
        simpleRegression = null;
        return true;
    }

    @Override
    public void readData() {
        try {
            simpleRegression = new SimpleRegression(interceptCheck.isSelected());
            if (sourceController.allPages()) {
                if (displayAllCheck.isSelected()) {
                    outputData = data2D.allRows(dataColsIndices, false);
                    handleData(outputData);
                } else {
                    data2D.simpleLinearRegression(dataColsIndices, simpleRegression);
                    outputData = sourceController.selectedData(sourceController.checkedRowsIndices(), dataColsIndices, false);
                }
            } else {
                outputData = sourceController.selectedData(sourceController.checkedRowsIndices(), dataColsIndices, false);
                handleData(outputData);
            }

            intercept = interceptCheck.isSelected() ? simpleRegression.getIntercept() : 0;
            slope = simpleRegression.getSlope();

            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("FittedValue"), ColumnDefinition.ColumnType.Double));
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                double x = data2D.doubleValue(rowData.get(0));
                rowData.add(DoubleTools.format(intercept + slope * x, scale));
            }

            resultColumns = new ArrayList<>();
            resultColumns.add(new Data2DColumn(message("Name"), ColumnDefinition.ColumnType.String, 300));
            resultColumns.add(new Data2DColumn(message("Value"), ColumnDefinition.ColumnType.Double));

            resultData = new ArrayList<>();
            List<String> data;

            data = new ArrayList<>();
            data.add(message("NumberOfObservations"));
            data.add(DoubleTools.format(simpleRegression.getN(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("Intercept"));
            data.add(DoubleTools.format(intercept, scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("Slope"));
            data.add(DoubleTools.format(slope, scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("CoefficientOfDetermination"));
            data.add(DoubleTools.format(simpleRegression.getRSquare(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("PearsonsR"));
            data.add(DoubleTools.format(simpleRegression.getR(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("StandardErrorOfIntercept"));
            data.add(interceptCheck.isSelected() ? DoubleTools.format(simpleRegression.getInterceptStdErr(), scale) : "0");
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("StandardErrorOfSlope"));
            data.add(DoubleTools.format(simpleRegression.getSlopeStdErr(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("SumSquaredErrors"));
            data.add(DoubleTools.format(simpleRegression.getSumSquaredErrors(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("MeanSquareError"));
            data.add(DoubleTools.format(simpleRegression.getMeanSquareError(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("StandardErrorOfSlope"));
            data.add(DoubleTools.format(simpleRegression.getSlopeStdErr(), scale));
            resultData.add(data);

            simpleRegression.clear();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void handleData(List<List<String>> data) {
        if (data == null || simpleRegression == null) {
            return;
        }
        for (List<String> row : data) {
            try {
                double x = data2D.doubleValue(row.get(0));
                double y = data2D.doubleValue(row.get(1));
                simpleRegression.addData(x, y);
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        }
    }

    @Override
    public void clearChart() {
        super.clearChart();
        regressionChart = null;
    }

    @Override
    public void makeChart() {
        try {
            makeAxis();

            regressionChart = new LabeledSimpleRegressionChart(xAxis, yAxis)
                    .setDisplayFitted(fittedCheck.isSelected()).setLineWidth(2);
            xyChart = regressionChart;
            regressionChart.setChartController(this);

            makeXYChart();
            makeFinalChart();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void writeChartData() {
        try {
            List<String> names = new ArrayList<>();
            for (Data2DColumn c : resultColumns) {
                names.add(c.getColumnName());
            }
            StringTable table = new StringTable(names);
            for (List<String> row : resultData) {
                table.add(row);
            }
            resultsViewController.loadContents(table.html());

            writeXYChart(outputColumns, outputData);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setChartStyle() {
        if (regressionChart == null) {
            return;
        }
        makePalette();
        regressionChart.setDisplayFitted(fittedCheck.isSelected()).setLineWidth(2);
        ChartTools.setScatterChart​Colors(regressionChart, palette, legendSide != null);
        regressionChart.requestLayout();
    }

    @FXML
    public void editResultsAction() {
        resultsViewController.editAction();
    }

    @FXML
    public void resultsDataAction() {
        if (resultData == null || resultData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        DataManufactureController.open(resultColumns, resultData);
    }

    @FXML
    public void popResultsMenu(MouseEvent mouseEvent) {
        resultsViewController.popFunctionsMenu(mouseEvent);
    }


    /*
        static
     */
    public static Data2DSimpleLinearRegressionController open(ControlData2DEditTable tableController) {
        try {
            Data2DSimpleLinearRegressionController controller = (Data2DSimpleLinearRegressionController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSimpleLinearRegressionFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
