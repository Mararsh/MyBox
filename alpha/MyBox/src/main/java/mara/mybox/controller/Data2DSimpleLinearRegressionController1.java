package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.LabeledScatterChart;
import mara.mybox.fxml.chart.LabeledSimpleRegressionChart1;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegressionController1 extends BaseData2DChartXYController {

    protected LabeledSimpleRegressionChart1 regressionChart​;
    protected SimpleLinearRegression simpleRegression;
    protected double alpha, intercept, slope, rSquare, r, slopeError;
    protected DataFileCSV regressionFile;
    protected List<List<String>> regressionData;
    protected List<List<String>> residualData;
    protected List<Data2DColumn> residualColumns;
    protected LabeledScatterChart residualChart;
    protected Map<String, String> residualPalette;

    @FXML
    protected CheckBox interceptCheck, displayAllCheck, textCheck, standardResidualCheck,
            fittedPointsCheck, confidenceLowerPointsCheck, confidenceUpPointsCheck,
            fittedLineCheck, confidenceLowerLineCheck, confidenceUpLineCheck;
    @FXML
    protected ComboBox<String> alphaSelector;
    @FXML
    protected ControlData2DLoad regressionDataController;
    @FXML
    protected ControlWebView modelViewController;
    @FXML
    protected ControlFxChart residualController;
    @FXML
    protected ControlData2DHtml residualDataController;

    public Data2DSimpleLinearRegressionController1() {
        baseTitle = message("SimpleLinearRegression");
        TipsLabelKey = "SimpleLinearRegressionTips";
    }

    @Override
    public void initControls() {
        try {
            regressionDataController.setData(Data2D.create(Data2D.Type.CSV));

            super.initControls();

            sourceController.noColumnSelection(true);

            modelViewController.setParent(this);

            residualDataController.setParameters(this);

            initChartTab();

            initResidualPane();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initChartTab() {
        try {

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

    public void initResidualPane() {
        try {
            residualController.initType("Point");

            residualController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    writeResidualChart();
                }
            });

            standardResidualCheck.setSelected(UserConfig.getBoolean(baseName + "StandardResidual", false));
            standardResidualCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "StandardResidual", standardResidualCheck.isSelected());
                    redrawResidualChart();
                }
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
        int categoryCol = data2D.colOrder(selectedCategory);
        if (categoryCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.add(categoryCol);
        int valueCol = data2D.colOrder(selectedValue);
        if (valueCol < 0) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.add(valueCol);
        simpleRegression = null;
        regressionFile = null;
        regressionData = null;
        return true;
    }

    @Override
    public void readData() {
        try {
            simpleRegression = new SimpleLinearRegression(interceptCheck.isSelected(), selectedCategory, selectedValue);
            if (sourceController.allPages()) {
                if (displayAllCheck.isSelected()) {
                    outputData = data2D.allRows(dataColsIndices, true);
                    regress(outputData);
                } else {
                    regressionFile = data2D.simpleLinearRegression(dataColsIndices, simpleRegression);
                    outputData = sourceController.selectedData(sourceController.checkedRowsIndices(), dataColsIndices, true);
                }
            } else {
                outputData = sourceController.selectedData(sourceController.checkedRowsIndices(), dataColsIndices, true);
                regress(outputData);
            }

            intercept = interceptCheck.isSelected() ? simpleRegression.getIntercept() : 0;
            slope = simpleRegression.getSlope();
            rSquare = simpleRegression.getRSquare();
            r = simpleRegression.getR();
            alpha = alpha <= 0 || alpha >= 1 ? 0.05 : alpha;
            slopeError = simpleRegression.getSlopeStdErr();

            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            outputColumns.add(data2D.col(selectedCategory));
            outputColumns.add(data2D.col(selectedValue));
            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("FittedValue"), ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("ConfidenceLowerLimit"), ColumnDefinition.ColumnType.Double));
            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("ConfidenceUpperLimit"), ColumnDefinition.ColumnType.Double));
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                double x = data2D.doubleValue(rowData.get(1));
                rowData.add(DoubleTools.format(intercept + slope * x, scale));
                rowData.add(DoubleTools.format(intercept + (slope - slopeError) * x, scale));
                rowData.add(DoubleTools.format(intercept + (slope + slopeError) * x, scale));
            }

            makeResidualData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeResidualData() {
        try {
            residualColumns = new ArrayList<>();
            residualColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            residualColumns.add(data2D.col(selectedCategory));
            residualColumns.add(data2D.col(selectedValue));
            residualColumns.add(new Data2DColumn(message("PredictedValue"), ColumnDefinition.ColumnType.Double));
            residualColumns.add(new Data2DColumn(message("Residual"), ColumnDefinition.ColumnType.Double));
            residualColumns.add(new Data2DColumn(message("StandardizedResidual"), ColumnDefinition.ColumnType.Double));
            residualData = new ArrayList<>();
            double std = Math.sqrt(simpleRegression.getMeanSquareError());
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                List<String> residualRow = new ArrayList<>();
                double x = data2D.doubleValue(rowData.get(1));
                double y = data2D.doubleValue(rowData.get(2));
                residualRow.add(DoubleTools.format(data2D.doubleValue(rowData.get(0)), scale));
                residualRow.add(DoubleTools.format(x, scale));
                residualRow.add(DoubleTools.format(y, scale));
                double predict = intercept + slope * x;
                double residual = intercept + slope * x - y;
                residualRow.add(DoubleTools.format(residual, scale));
                residualRow.add(DoubleTools.format(residual / std, scale));
                residualData.add(residualRow);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void regress(List<List<String>> data) {
        if (data == null || simpleRegression == null) {
            return;
        }
        regressionData = new ArrayList<>();
        for (List<String> row : data) {
            try {
                long index = Long.parseLong(row.get(0));
                double x = data2D.doubleValue(row.get(1));
                double y = data2D.doubleValue(row.get(2));
                List<String> resultRow = simpleRegression.addData(index, x, y);
                regressionData.add(resultRow);
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
        }
    }

    @Override
    public void clearChart() {
        super.clearChart();
        regressionChart = null;
        residualChart = null;
    }

    @Override
    public void makeChart() {
        try {
            makeAxis();

//            regressionChart = new LabeledSimpleRegressionChart(xAxis, yAxis)
//                    .setDisplayText(textCheck.isSelected())
//                    .setDisplayFittedPoints(fittedPointsCheck.isSelected())
//                    .setDisplayConfidenceLowerPoints(confidenceLowerPointsCheck.isSelected())
//                    .setDisplayConfidenceUpperPoints(confidenceUpPointsCheck.isSelected())
//                    .setDisplayFittedLine(fittedLineCheck.isSelected())
//                    .setDisplayConfidenceLowerLine(confidenceLowerLineCheck.isSelected())
//                    .setDisplayConfidenceUpperLine(confidenceUpLineCheck.isSelected());
            xyChart = regressionChart;
            regressionChart.setChartController(this);
            makeXYChart();
            makeFinalChart();

            makeResidualChart();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeResidualChart() {
        try {
            NumberAxis residualAxisX = new NumberAxis();
            initCategoryAxis(residualAxisX);

            NumberAxis residualAxisY = new NumberAxis();
            initValueAxis(residualAxisY);

            residualChart = new LabeledScatterChart(residualAxisX, residualAxisY);
            residualChart.setChartController(this, residualController);
            initXYChart(residualChart);
            styleChart(residualChart);
            residualChart.setTitle(selectedCategory + " - " + selectedValue + "_" + message("Residual"));

            residualController.setChart(residualChart, residualColumns, residualData);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void writeChartData() {
        try {
            writeXYChart(outputColumns, outputData, true);

            writeResidualChart();

            writeModelView();

            writeRegressionData();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeResidualChart() {
        try {
            writeXYChart(residualChart, residualColumns, residualData, null, true);
            ChartTools.setScatterChart​Colors(residualChart, palette, legendSide != null);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeModelView() {
        try {
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");
            s.append(" <script>\n"
                    + "    function calculate() {\n"
                    + "      var x = document.getElementById('inputX').value;  　\n"
                    + "      var y =  " + intercept + "  + " + slope + " * x ;\n"
                    + "      document.getElementById('outputY').value = y;\n"
                    + "      var v1 =  y - " + slopeError + ";\n"
                    + "      var v2 =  y + " + slopeError + ";\n"
                    + "      document.getElementById('ConfidenceIntervals').value = v1 + ' - ' + v2;\n"
                    + "    }\n"
                    + "  </script>\n\n");
            String m = message("LinearModel") + ": " + selectedValue + " = "
                    + (this.intercept == 0 ? "" : DoubleTools.format(intercept, scale) + " + ")
                    + DoubleTools.format(slope, scale) + " * " + selectedCategory;
            s.append("\n<DIV>").append(m).append("</DIV>\n");
            s.append("<DIV>\n");
            s.append("<P>").append(message("IndependentVariable")).append(": ").append(selectedCategory).append(" = \n");
            s.append("<INPUT id=\"inputX\" type=\"text\" style=\"width:200px\"/>\n");
            s.append("<INPUT type=\"submit\" onclick=\"calculate();\"/></P>\n");
            s.append("<P>").append(message("DependentVariable")).append(": ").append(selectedValue).append(" = \n");
            s.append("<INPUT id=\"outputY\"  type=\"text\" style=\"width:200px\"/></P>\n");
            s.append("<P>").append(message("ConfidenceIntervals")).append(" = \n");
            s.append("<INPUT id=\"ConfidenceIntervals\"  type=\"text\" style=\"width:300px\"/></P>\n");
            s.append("</DIV>\n<HR/>\n");

            s.append("<H3 align=center>").append(message("LastStatus")).append("</H3>\n");
            List<String> names = new ArrayList<>();
            names.add(message("Name"));
            names.add(message("Value"));
            StringTable table = new StringTable(names);
            List<Data2DColumn> columns = simpleRegression.getColumns();
            List<String> lastData = simpleRegression.getLastData();
            for (int i = 0; i < columns.size(); i++) {
                List<String> row = new ArrayList<>();
                Data2DColumn c = columns.get(i);
                row.add(c.getColumnName());
                row.add(lastData.get(i));
                table.add(row);
            }
            s.append(table.div());

            s.append("\n<HR/><P align=left style=\"font-size:1em;\">* ")
                    .append(message("HtmlEditableComments")).append("</P>\n");

            s.append("</BODY>\n");
            modelViewController.loadContents(HtmlWriteTools.html(s.toString()));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeRegressionData() {
        try {
            if (regressionFile != null) {
                regressionDataController.loadDef(regressionFile);
            } else {
                regressionDataController.loadTmpData(simpleRegression.getColumns(), regressionData);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void displayChartData() {
        chartDataController.loadData(outputColumns, outputData);
        residualDataController.loadData(residualColumns, residualData);
    }

    public String model() {
        return message("IndependentVariable") + ": " + selectedCategory + "\n"
                + message("DependentVariable") + ": " + selectedValue + "\n"
                + message("LinearModel") + ": " + selectedValue + " = "
                + (this.intercept == 0 ? "" : DoubleTools.format(intercept, scale) + " + ")
                + DoubleTools.format(slope, scale) + " * " + selectedCategory + "\n"
                + message("CoefficientOfDetermination") + ": " + DoubleTools.format(rSquare, scale) + "\n"
                + message("PearsonsR") + ": " + DoubleTools.format(r, scale);
    }

    @Override
    public void setChartStyle() {
        randomColorsFitting();
        randomColorResidual();
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
                if (i > 2 || color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                palette.put(column.getColumnName(), rgb);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeResidualPalette() {
        try {
            Random random = new Random();
            residualPalette = new HashMap();
            for (int i = 0; i < residualColumns.size(); i++) {
                Data2DColumn column = residualColumns.get(i);
                Color color = column.getColor();
                if (i > 1 || color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                residualPalette.put(column.getColumnName(), rgb);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void randomColorsFitting() {
        try {
            if (regressionChart == null) {
                return;
            }
            makePalette();
//            regressionChart.setModel(model())
//                    .setDisplayText(textCheck.isSelected())
//                    .setDisplayFittedPoints(fittedPointsCheck.isSelected())
//                    .setDisplayConfidenceLowerPoints(confidenceLowerPointsCheck.isSelected())
//                    .setDisplayConfidenceUpperPoints(confidenceUpPointsCheck.isSelected())
//                    .setDisplayFittedLine(fittedLineCheck.isSelected())
//                    .setDisplayConfidenceLowerLine(confidenceLowerLineCheck.isSelected())
//                    .setDisplayConfidenceUpperLine(confidenceUpLineCheck.isSelected());
            ChartTools.setScatterChart​Colors(regressionChart, palette, legendSide != null);
            regressionChart.displayResults();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void randomColorResidual() {
        try {
            makeResidualPalette();
            ChartTools.setScatterChart​Colors(residualChart, residualPalette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void redrawResidualChart() {
        try {
            makeResidualData();
            writeResidualChart();
            randomColorResidual();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void editRegressionAction() {
        if (regressionFile != null) {
            DataFileCSVController.open(regressionFile);
        } else {
            DataFileCSVController.open(simpleRegression.getColumns(), regressionData);
        }
    }

    @FXML
    public void editModelAction() {
        modelViewController.editAction();
    }

    @FXML
    public void popModelMenu(MouseEvent mouseEvent) {
        modelViewController.popFunctionsMenu(mouseEvent);
    }

    @FXML
    public void about() {
        try {
            StringTable table = new StringTable(null, message("AboutSimpleLinearRegression"));
            table.newLinkRow(message("Guide"), "https://www.itl.nist.gov/div898/handbook/");
            table.newLinkRow("", "https://book.douban.com/subject/10956491/");
            table.newLinkRow(message("Video"), "https://www.bilibili.com/video/BV1Ua4y1e7YG");
            table.newLinkRow("", "https://www.bilibili.com/video/BV1i7411d7aP");
            table.newLinkRow(message("Example"), "https://www.xycoon.com/simple_linear_regression.htm");
            table.newLinkRow("", "https://www.scribbr.com/statistics/simple-linear-regression/");
            table.newLinkRow("", "http://www.datasetsanalysis.com/regressions/simple-linear-regression.html");
            table.newLinkRow(message("Dataset"), "http://archive.ics.uci.edu/ml/datasets/Iris");
            table.newLinkRow("", "https://github.com/tomsharp/SVR/tree/master/data");
            table.newLinkRow("", "https://github.com/krishnaik06/simple-Linear-Regression");
            table.newLinkRow("", "https://github.com/susanli2016/Machine-Learning-with-Python/tree/master/data");
            table.newLinkRow("Apache-Math", "https://commons.apache.org/proper/commons-math/");
            table.newLinkRow("", "https://commons.apache.org/proper/commons-math/apidocs/index.html");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            openLink(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        static
     */
    public static Data2DSimpleLinearRegressionController1 open(ControlData2DEditTable tableController) {
        try {
            Data2DSimpleLinearRegressionController1 controller = (Data2DSimpleLinearRegressionController1) WindowTools.openChildStage(
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
