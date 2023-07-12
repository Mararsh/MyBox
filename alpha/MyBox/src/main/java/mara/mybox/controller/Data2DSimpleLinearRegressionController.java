package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.ResidualChart;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegressionController extends BaseData2DRegressionController {

    protected XYChartMaker fittingMaker, residualMaker;
    protected SimpleLinearRegression simpleRegression;
    protected double slope, r;
    protected int residualInside;
    protected DataFileCSV regressionFile;
    protected List<List<String>> regressionData;
    protected List<List<String>> residualData;
    protected List<Data2DColumn> residualColumns;
    protected Map<String, String> residualPalette;

    @FXML
    protected CheckBox textCheck, fittedPointsCheck, fittedLineCheck, residualStdCheck;
    @FXML
    protected ControlData2DChartXY fittingController, residualController;
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

            fittingMaker = fittingController.chartMaker;
            fittingMaker.init(ChartType.SimpleRegressionChart, message("SimpleRegressionChart"));
            fittingController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawFittingChart();
                }
            });

            residualMaker = residualController.chartMaker;
            residualMaker.init(ChartType.ResidualChart, message("ResidualChart"));
            residualController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawResidualChart();
                }
            });

            initChartTab();

            initResidualPane();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initChartTab() {
        try {

            fittedPointsCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayFittedPoints", false));
            fittedPointsCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayFittedPoints", fittedPointsCheck.isSelected());
                fittingMaker.getSimpleRegressionChart().displayFittedPoints(fittedPointsCheck.isSelected());
            });

            fittedLineCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayFittedLine", true));
            fittedLineCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayFittedLine", fittedLineCheck.isSelected());
                fittingMaker.getSimpleRegressionChart().displayFittedLine(fittedLineCheck.isSelected());
            });

            textCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayText", true));
            textCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayText", textCheck.isSelected());
                fittingMaker.getSimpleRegressionChart().displayText(textCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initResidualPane() {
        try {
            residualXGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    makeResidualChart();
                }
            });

            residualStdCheck.setSelected(UserConfig.getBoolean(baseName + "StandardResidual", true));
            residualStdCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "StandardResidual", residualStdCheck.isSelected());
                    makeResidualChart();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            int categoryCol = data2D.colOrder(selectedCategory);
            if (categoryCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("CategoryColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            int valueCol = data2D.colOrder(selectedValue);
            if (valueCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("ValueColumn"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            if (categoryCol == valueCol) {
                outOptionsError(message("IndependentVariableShouldNotDependentVariable"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices = new ArrayList<>();
            dataColsIndices.add(categoryCol);
            dataColsIndices.add(valueCol);
            simpleRegression = null;
            regressionFile = null;
            regressionData = null;

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void readData() {
        try {
            simpleRegression = new SimpleLinearRegression(interceptCheck.isSelected(),
                    selectedCategory, selectedValue, scale);
            if (isAllPages()) {
                if (displayAllCheck.isSelected()) {
                    outputData = data2D.allRows(dataColsIndices, true);
                    regressionData = simpleRegression.addData(outputData, invalidAs);
                } else {
                    regressionFile = data2D.simpleLinearRegression(null, dataColsIndices, simpleRegression, true);
                    outputData = tableFiltered(dataColsIndices, true);
                }
            } else {
                outputData = tableFiltered(dataColsIndices, true);
                regressionData = simpleRegression.addData(outputData, invalidAs);
            }
            if (outputData == null) {
                return;
            }
            intercept = interceptCheck.isSelected() ? simpleRegression.getIntercept() : 0;
            slope = simpleRegression.getSlope();
            rSquare = simpleRegression.getRSquare();
            r = simpleRegression.getR();

            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            outputColumns.add(data2D.columnByName(selectedCategory));
            outputColumns.add(data2D.columnByName(selectedValue));
            outputColumns.add(new Data2DColumn(selectedValue + "_" + message("FittedValue"), ColumnDefinition.ColumnType.Double));
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                double x = DoubleTools.toDouble(rowData.get(1), invalidAs);
                rowData.add(NumberTools.format(intercept + slope * x, scale));
            }

            makeResidualData();

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            double x, y;
            for (int i = 0; i < outputData.size(); i++) {
                List<String> rowData = outputData.get(i);
                List<String> residualRow = new ArrayList<>();
                x = DoubleTools.toDouble(rowData.get(1), invalidAs);
                y = DoubleTools.toDouble(rowData.get(2), invalidAs);
                double predict = intercept + slope * x;
                double residual = y - predict;
                residualRow.add(rowData.get(0));
                if (residualIndRadio.isSelected()) {
                    residualRow.add(NumberTools.format(x, scale));
                } else if (residualActualRadio.isSelected()) {
                    residualRow.add(NumberTools.format(y, scale));
                } else {
                    residualRow.add(NumberTools.format(predict, scale));
                }
                if (residualStdCheck.isSelected()) {
                    double stdResidual = residual / stdDeviation;
                    residualRow.add(NumberTools.format(stdResidual, scale));
                    residualRow.add("1.96");
                    residualRow.add("-1.96");
                    if (stdResidual >= -1.96 && stdResidual <= 1.96) {
                        residualInside++;
                    }
                } else {
                    residualRow.add(NumberTools.format(residual, scale));
                }
                residualData.add(residualRow);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
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
        drawFittingChart();
        drawResidualChart();
    }

    public void drawFittingChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            fittingMaker.setDefaultChartTitle(selectedCategory + "_" + selectedValue + " - " + message("SimpleRegressionChart"))
                    .setChartTitle(fittingMaker.getDefaultChartTitle())
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(selectedValue);
            fittingController.writeXYChart(outputColumns, outputData, 1);
            fittingMaker.getSimpleRegressionChart()
                    .setDisplayText(textCheck.isSelected())
                    .setDisplayFittedPoints(fittedPointsCheck.isSelected())
                    .setDisplayFittedLine(fittedLineCheck.isSelected());
            randomColorsFitting();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void drawResidualChart() {
        try {
            if (residualColumns == null || residualData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            residualMaker.setDefaultChartTitle((selectedCategory + "_" + selectedValue + " - " + message("Residual")))
                    .setChartTitle(fittingMaker.getDefaultChartTitle())
                    .setDefaultCategoryLabel(residualColumns.get(1).getColumnName())
                    .setDefaultValueLabel(message("Residual"));
            residualController.writeXYChart(residualColumns, residualData, 1);
            randomColorResidual();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void writeModelView() {
        try {
            String interceptScaled = NumberTools.format(intercept, scale);
            String slopeScaled = NumberTools.format(Math.abs(slope), scale);
            if (DoubleTools.invalidDouble(slope)) {
                alertError(message("InvalidData") + "\n" + message("RegressionFailedNotice"));
            }

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
            modelController.loadContents(HtmlWriteTools.html(s.toString()));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void writeRegressionData() {
        try {
            if (regressionFile != null) {
                regressionDataController.loadDef(regressionFile);
            } else {
                regressionDataController.loadData(simpleRegression.getColumns(), regressionData);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void randomColorsFitting() {
        try {
            fittingMaker.setChartStyle();

            fittingMaker.getSimpleRegressionChart()
                    .setModel(simpleRegression.modelDesc())
                    .setDisplayText(textCheck.isSelected())
                    .setDisplayFittedPoints(fittedPointsCheck.isSelected())
                    .setDisplayFittedLine(fittedLineCheck.isSelected())
                    .displayControls();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void randomColorResidual() {
        try {
            residualMaker.setChartStyle();

            ResidualChart residualChart = residualMaker.getResidualChart();
            residualChart.setInfo(message("InsideSigma2") + ": "
                    + residualInside + "/" + residualData.size()
                    + " = " + DoubleTools.percentage(residualInside, residualData.size(), 2) + "%")
                    .displayControls(residualColumns.size() - 2);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeResidualChart() {
        makeResidualData();
        drawResidualChart();
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
    public static Data2DSimpleLinearRegressionController open(ControlData2DLoad tableController) {
        try {
            Data2DSimpleLinearRegressionController controller = (Data2DSimpleLinearRegressionController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSimpleLinearRegressionFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
