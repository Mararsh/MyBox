package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledSimpleRegressionChart;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
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
    protected double alpha, intercept, slope, rSquare, r, confidenceIntervals;

    @FXML
    protected CheckBox interceptCheck, displayAllCheck, textCheck,
            fittedPointsCheck, confidenceLowerPointsCheck, confidenceUpPointsCheck,
            fittedLineCheck, confidenceLowerLineCheck, confidenceUpLineCheck;
    @FXML
    protected ControlWebView resultsViewController;
    @FXML
    protected ComboBox<String> alphaSelector;

    public Data2DSimpleLinearRegressionController() {
        baseTitle = message("SimpleLinearRegression");
        TipsLabelKey = "SimpleLinearRegressionTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            sourceController.noColumnSelection(true);
            resultsViewController.setParent(this);

            initChartTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initChartTab() {
        try {

            labelType = ChartTools.LabelType.Point;

            alpha = UserConfig.getDouble(baseName + "Alpha", 0.05);
            if (alpha >= 1 || alpha <= 0) {
                alpha = 0.05;
            }
            alphaSelector.getItems().addAll(Arrays.asList(
                    "0.05", "0.01", "0.02", "0.03", "0.06", "0.1"
            ));
            alphaSelector.getSelectionModel().select(alpha + "");
            alphaSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.parseDouble(newValue);
                        if (v > 0 && v < 1) {
                            alpha = v;
                            alphaSelector.getEditor().setStyle(null);
                            UserConfig.setDouble(baseName + "Alpha", alpha);
                        } else {
                            alphaSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        alphaSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

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

            fittedPointsCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayFittedPoints", false));
            fittedPointsCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayFittedPoints", fittedPointsCheck.isSelected());
                regressionChart​.displayFittedPoints(fittedPointsCheck.isSelected());
            });

            confidenceLowerPointsCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayConfidenceLowerPoints", false));
            confidenceLowerPointsCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayConfidenceLowerPoints", confidenceLowerPointsCheck.isSelected());
                regressionChart​.displayConfidenceLowerPoints(confidenceLowerPointsCheck.isSelected());
            });

            confidenceUpPointsCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayConfidenceUpPoints", false));
            confidenceUpPointsCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayConfidenceUpPoints", confidenceUpPointsCheck.isSelected());
                regressionChart.displayConfidenceUpperPoints(confidenceUpPointsCheck.isSelected());
            });

            fittedLineCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayFittedLine", true));
            fittedLineCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayFittedLine", fittedLineCheck.isSelected());
                regressionChart​.displayFittedLine(fittedLineCheck.isSelected());
            });

            confidenceLowerLineCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayConfidenceLowerLine", true));
            confidenceLowerLineCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayConfidenceLowerLine", confidenceLowerLineCheck.isSelected());
                regressionChart​.displayConfidenceLowerLine(confidenceLowerLineCheck.isSelected());
            });

            confidenceUpLineCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayConfidenceUpLine", true));
            confidenceUpLineCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayConfidenceUpLine", confidenceUpLineCheck.isSelected());
                regressionChart.displayConfidenceUpperLine(confidenceUpLineCheck.isSelected());
            });

            textCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayText", true));
            textCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayText", textCheck.isSelected());
                regressionChart.displayText(textCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void checkChartType() {
        try {
            setSourceLabel("");
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
    public void defaultCategoryLabel() {
        categoryLabel.setText(message("IndependentVariable") + ": " + categoryName());
    }

    @FXML
    @Override
    public void defaultValueLabel() {
        numberLabel.setText(message("DependentVariable") + ": " + valueName());
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
            rSquare = simpleRegression.getRSquare();
            r = simpleRegression.getR();
            alpha = alpha <= 0 || alpha >= 1 ? 0.05 : alpha;
            confidenceIntervals = simpleRegression.getSlopeConfidenceInterval(alpha);

            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("FittedValue"), ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("ConfidenceLowerLimit"), ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("ConfidenceUpperLimit"), ColumnDefinition.ColumnType.Double));
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                double x = data2D.doubleValue(rowData.get(0));
                rowData.add(DoubleTools.format(intercept + slope * x, scale));
                rowData.add(DoubleTools.format(intercept + (slope - confidenceIntervals) * x, scale));
                rowData.add(DoubleTools.format(intercept + (slope + confidenceIntervals) * x, scale));
            }

            resultColumns = new ArrayList<>();
            resultColumns.add(new Data2DColumn(message("Name"), ColumnDefinition.ColumnType.String, 300));
            resultColumns.add(new Data2DColumn(message("Value"), ColumnDefinition.ColumnType.Double));

            resultData = new ArrayList<>();
            List<String> data;

            data = new ArrayList<>();
            data.add(message("IndependentVariable"));
            data.add(selectedCategory);
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("DependentVariable"));
            data.add(selectedValue);
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("LinearModel"));
            data.add(selectedValue + " = "
                    + (intercept == 0 ? "" : DoubleTools.format(intercept, scale) + " + ")
                    + DoubleTools.format(slope, scale) + " * " + selectedCategory);
            resultData.add(data);

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
            data.add(DoubleTools.format(rSquare, scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("PearsonsR"));
            data.add(DoubleTools.format(r, scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("ConfidenceIntervals"));
            data.add(DoubleTools.format(confidenceIntervals, scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("SignificanceLevelSlopeCorrelation"));
            data.add(DoubleTools.format(simpleRegression.getSignificance(), scale));
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
            data.add(message("MeanSquareError"));
            data.add(DoubleTools.format(simpleRegression.getMeanSquareError(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("SumSquaredRegression"));
            data.add(DoubleTools.format(simpleRegression.getRegressionSumSquares(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("SumSquaredErrors"));
            data.add(DoubleTools.format(simpleRegression.getSumSquaredErrors(), scale));
            resultData.add(data);

            data = new ArrayList<>();
            data.add(message("SumSquaredDeviations"));
            data.add(DoubleTools.format(simpleRegression.getTotalSumSquares(), scale));
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
                    .setDisplayText(textCheck.isSelected())
                    .setDisplayFittedPoints(fittedPointsCheck.isSelected())
                    .setDisplayConfidenceLowerPoints(confidenceLowerPointsCheck.isSelected())
                    .setDisplayConfidenceUpperPoints(confidenceUpPointsCheck.isSelected())
                    .setDisplayFittedLine(fittedLineCheck.isSelected())
                    .setDisplayConfidenceLowerLine(confidenceLowerLineCheck.isSelected())
                    .setDisplayConfidenceUpperLine(confidenceUpLineCheck.isSelected());
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
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");
            s.append(" <script>\n"
                    + "    function calculate() {\n"
                    + "      var x = document.getElementById('inputX').value;  　\n"
                    + "      var y =  " + intercept + "  + " + slope + " * x ;\n"
                    + "      document.getElementById('outputY').value = y;\n"
                    + "    }\n"
                    + "  </script>\n\n");
            s.append("<DIV>\n");
            s.append("<P>").append(message("IndependentVariable")).append(": ").append(selectedCategory).append(" = \n");
            s.append("<INPUT id=\"inputX\" type=\"text\"/>\n");
            s.append("<INPUT type=\"submit\" onclick=\"calculate();\"/></P>\n");
            s.append("<P>").append(message("DependentVariable")).append(": ").append(selectedValue).append(" = \n");
            s.append("<INPUT id=\"outputY\"  type=\"text\"/></P>\n");
            s.append("</DIV>\n").append("<HR/>\n");
            s.append("<P align=left style=\"font-size:0.8em;\">* ")
                    .append(message("HtmlEditableComments")).append("</P>\n");
            s.append(table.div());
            s.append("</BODY>\n");
            resultsViewController.loadContents(HtmlWriteTools.html(s.toString()));

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
        String model = message("IndependentVariable") + ": " + selectedCategory + "\n"
                + message("DependentVariable") + ": " + selectedValue + "\n"
                + message("LinearModel") + ": " + selectedValue + " = "
                + (this.intercept == 0 ? "" : DoubleTools.format(intercept, scale) + " + ")
                + DoubleTools.format(slope, scale) + " * " + selectedCategory + "\n"
                + message("CoefficientOfDetermination") + ": " + DoubleTools.format(rSquare, scale) + "\n"
                + message("PearsonsR") + ": " + DoubleTools.format(r, scale);
        regressionChart.setModel(model)
                .setDisplayText(textCheck.isSelected())
                .setDisplayFittedPoints(fittedPointsCheck.isSelected())
                .setDisplayConfidenceLowerPoints(confidenceLowerPointsCheck.isSelected())
                .setDisplayConfidenceUpperPoints(confidenceUpPointsCheck.isSelected())
                .setDisplayFittedLine(fittedLineCheck.isSelected())
                .setDisplayConfidenceLowerLine(confidenceLowerLineCheck.isSelected())
                .setDisplayConfidenceUpperLine(confidenceUpLineCheck.isSelected());
        ChartTools.setScatterChart​Colors(regressionChart, palette, legendSide != null);
        regressionChart.displayResults();
    }

    @Override
    public void makePalette() {
        try {
            Random random = new Random();
            if (palette == null) {
                palette = new HashMap();
            } else {
                palette.clear();
            }
            for (int i = 0; i < outputColumns.size(); i++) {
                Data2DColumn column = outputColumns.get(i);
                Color color = column.getColor();
                if (i > 1 || color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                palette.put(column.getColumnName(), rgb);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void randomColors() {
        try {
            setChartStyle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
