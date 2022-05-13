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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.ResidualChart;
import mara.mybox.fxml.chart.SimpleRegressionChart;
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
public class Data2DSimpleLinearRegressionController extends BaseData2DChartController {

    protected SimpleRegressionChart regressionChart​;
    protected SimpleLinearRegression simpleRegression;
    protected double alpha, intercept, slope, rSquare, r;
    protected int residualInside;
    protected DataFileCSV regressionFile;
    protected List<List<String>> regressionData;
    protected List<List<String>> residualData;
    protected List<Data2DColumn> residualColumns;
    protected ResidualChart residualChart;
    protected Map<String, String> residualPalette;

    @FXML
    protected CheckBox interceptCheck, displayAllCheck, textCheck, fittedPointsCheck, fittedLineCheck,
            residualStdCheck;
    @FXML
    protected ComboBox<String> alphaSelector;
    @FXML
    protected ControlData2DResults regressionDataController;
    @FXML
    protected ControlWebView modelViewController;
    @FXML
    protected ControlData2DChartXY fittingController, residualController;
    @FXML
    protected ControlData2DHtml residualDataController;
    @FXML
    protected ToggleGroup residualXGroup;
    @FXML
    protected RadioButton residualPredicateRadio, residualIndRadio, residualActualRadio;

    public Data2DSimpleLinearRegressionController() {
        baseTitle = message("SimpleLinearRegression");
        TipsLabelKey = "SimpleLinearRegressionTips";
        defaultScale = 8;
    }

    @Override
    public void initControls() {
        try {
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

            fittedLineCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayFittedLine", true));
            fittedLineCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayFittedLine", fittedLineCheck.isSelected());
                regressionChart​.displayFittedLine(fittedLineCheck.isSelected());
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
//            residualController.initType("Point");
//
//            residualController.redrawNotify.addListener(new ChangeListener<Boolean>() {
//                @Override
//                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
//                    writeResidualChart();
//                }
//            });

            residualXGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    redrawResidualChart();
                }
            });

            residualStdCheck.setSelected(UserConfig.getBoolean(baseName + "StandardResidual", true));
            residualStdCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "StandardResidual", residualStdCheck.isSelected());
                    redrawResidualChart();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

//    @FXML
//    @Override
//    public void defaultCategoryLabel() {
//        categoryLabel.setText(message("IndependentVariable") + ": " + categoryName());
//    }
//
//    @FXML
//    @Override
//    public void defaultValueLabel() {
//        numberLabel.setText(message("DependentVariable") + ": " + valueName());
//    }
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
        try {
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

            fittingController.data2D = data2D;
            fittingController.initChart(ChartType.SimpleRegressionChart, "SimpleRegressionChart");
            regressionChart​ = fittingController.chartOptions.getSimpleRegressionChart();
            regressionChart.setDisplayText(textCheck.isSelected())
                    .setDisplayFittedPoints(fittedPointsCheck.isSelected())
                    .setDisplayFittedLine(fittedLineCheck.isSelected());

            residualController.data2D = data2D;
            residualController.initChart(ChartType.ResidualChart, "ResidualChart");
            residualChart = residualController.chartOptions.getResidualChart();
            residualChart.setDataNumber(residualColumns.size() - 2)
                    .setTitle(selectedCategory + " - " + selectedValue + "_" + message("Residual"));
            residualChart.getXAxis().setLabel(residualColumns.get(1).getColumnName());
            residualChart.getYAxis().setLabel(message("Residual"));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void readData() {
        try {
            simpleRegression = new SimpleLinearRegression(interceptCheck.isSelected(),
                    selectedCategory, selectedValue, scale);
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

            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            outputColumns.add(data2D.col(selectedCategory));
            outputColumns.add(data2D.col(selectedValue));
            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("FittedValue"), ColumnDefinition.ColumnType.Double));
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                double x = data2D.doubleValue(rowData.get(1));
                rowData.add(DoubleTools.format(intercept + slope * x, scale));
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
            if (residualIndRadio.isSelected()) {
                residualColumns.add(new Data2DColumn(message("IndependentVariable"), ColumnDefinition.ColumnType.Double));
            } else if (residualActualRadio.isSelected()) {
                residualColumns.add(new Data2DColumn(message("ActualValue"), ColumnDefinition.ColumnType.Double));
            } else {
                residualColumns.add(new Data2DColumn(message("PredictedValue"), ColumnDefinition.ColumnType.Double));
            }
            double stdDeviation = 1;
            if (residualStdCheck.isSelected()) {
                residualColumns.add(new Data2DColumn(message("StandardizedResidual"), ColumnDefinition.ColumnType.Double));
                residualColumns.add(new Data2DColumn(message("Sigma2UpperLine"), ColumnDefinition.ColumnType.Double));
                residualColumns.add(new Data2DColumn(message("Sigma2lLowerLine"), ColumnDefinition.ColumnType.Double));
                stdDeviation = Math.sqrt(simpleRegression.getMeanSquareError());
            } else {
                residualColumns.add(new Data2DColumn(message("Residual"), ColumnDefinition.ColumnType.Double));
            }
            residualData = new ArrayList<>();
            residualInside = 0;
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                List<String> residualRow = new ArrayList<>();
                double x = data2D.doubleValue(rowData.get(1));
                double y = data2D.doubleValue(rowData.get(2));
                double predict = intercept + slope * x;
                double residual = y - predict;
                residualRow.add(rowData.get(0));
                if (residualIndRadio.isSelected()) {
                    residualRow.add(DoubleTools.format(x, scale));
                } else if (residualActualRadio.isSelected()) {
                    residualRow.add(DoubleTools.format(y, scale));
                } else {
                    residualRow.add(DoubleTools.format(predict, scale));
                }
                if (residualStdCheck.isSelected()) {
                    double stdResidual = residual / stdDeviation;
                    residualRow.add(DoubleTools.format(stdResidual, scale));
                    residualRow.add("1.96");
                    residualRow.add("-1.96");
                    if (stdResidual >= -1.96 && stdResidual <= 1.96) {
                        residualInside++;
                    }
                } else {
                    residualRow.add(DoubleTools.format(residual, scale));
                }
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
    public void outputData() {
        writeModelView();
        writeRegressionData();
        drawChart();
    }

    @Override
    public void drawChart() {
        try {

            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            fittingController.writeChart(outputColumns, outputData, true);

            residualController.writeChart(residualColumns, residualData, null, true);
            ChartTools.setScatterChart​Colors(residualChart, palette, residualController.chartOptions.getLegendSide() != null);

            makePalette();
            setChartStyle();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void writeModelView() {
        try {
            String interceptScaled = DoubleTools.format(intercept, scale);
            String slopeScaled = DoubleTools.format(Math.abs(slope), scale);
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");
            s.append(" <script>\n"
                    + "    function calculate() {\n"
                    + "      var x = document.getElementById('inputX').value;  　\n"
                    + "      var y =  " + interceptScaled + "  + " + slopeScaled + " * x ;\n"
                    + "      document.getElementById('outputY').value = y;\n"
                    //                    + "      var v1 =  y - " + slopeError + ";\n"
                    //                    + "      var v2 =  y + " + slopeError + ";\n"
                    //                    + "      document.getElementById('ConfidenceIntervals').value = v1 + ' - ' + v2;\n"
                    + "    }\n"
                    + "  </script>\n\n");
            String m = message("LinearModel") + ": " + selectedValue + " = "
                    + interceptScaled + (slope > 0 ? " + " : " - ")
                    + slopeScaled + " * " + selectedCategory;
            s.append("\n<DIV>").append(m).append("</DIV>\n");
            s.append("<DIV>\n");
            s.append("<P>").append(message("IndependentVariable")).append(": ").append(selectedCategory).append(" = \n");
            s.append("<INPUT id=\"inputX\" type=\"text\" style=\"width:200px\"/>\n");
            s.append("<BUTTON type=\"button\" onclick=\"calculate();\">").append(message("Predict")).append("</BUTTON></P>\n");
            s.append("<P>").append(message("DependentVariable")).append(": ").append(selectedValue).append(" = \n");
            s.append("<INPUT id=\"outputY\"  type=\"text\" style=\"width:200px\"/></P>\n");
//            s.append("<P>").append(message("ConfidenceIntervals")).append(" = \n");
//            s.append("<INPUT id=\"ConfidenceIntervals\"  type=\"text\" style=\"width:300px\"/></P>\n");
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

    public String model() {
        return message("IndependentVariable") + ": " + selectedCategory + "\n"
                + message("DependentVariable") + ": " + selectedValue + "\n"
                + message("LinearModel") + ": " + selectedValue + " = "
                + DoubleTools.format(intercept, scale) + (slope > 0 ? " + " : " - ")
                + DoubleTools.format(Math.abs(slope), scale) + " * " + selectedCategory + "\n"
                + message("CoefficientOfDetermination") + ": " + DoubleTools.format(Math.abs(rSquare), scale) + "\n"
                + message("PearsonsR") + ": " + r;
    }

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
            regressionChart.setModel(model())
                    .setDisplayText(textCheck.isSelected())
                    .setDisplayFittedPoints(fittedPointsCheck.isSelected())
                    .setDisplayFittedLine(fittedLineCheck.isSelected());
            regressionChart.setLineWidth(residualController.chartOptions.getLineWidth()).setPalette(palette);
            ChartTools.setScatterChart​Colors(regressionChart, palette, residualController.chartOptions.getLegendSide() != null);
            regressionChart.displayControls();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void randomColorResidual() {
        try {
            if (residualChart == null) {
                return;
            }
            makeResidualPalette();
            residualChart.setInfo(message("InsideSigma2") + ": " + residualInside + "/" + residualData.size())
                    .setLineWidth(residualController.chartOptions.getLineWidth()).setPalette(residualPalette);
            ChartTools.setScatterChart​Colors(residualChart, residualPalette, residualController.chartOptions.getLegendSide() != null);
            residualChart.displayControls();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void redrawResidualChart() {
        try {
            makeResidualData();
            randomColorResidual();
            residualDataController.loadData(residualColumns, residualData);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        get/set
     */
    public Map<String, String> getResidualPalette() {
        return residualPalette;
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
