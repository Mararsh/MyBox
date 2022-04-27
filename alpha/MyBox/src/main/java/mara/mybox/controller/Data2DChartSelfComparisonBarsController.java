package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import mara.mybox.data.Normalization;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-20
 * @License Apache License Version 2.0
 */
public class Data2DChartSelfComparisonBarsController extends BaseData2DHtmlChartController {

    protected Normalization normalization;

    @FXML
    protected CheckBox otherCheck;

    public Data2DChartSelfComparisonBarsController() {
        baseTitle = message("SelfComparisonBarsChart");
        TipsLabelKey = "SelfComparisonBarsChartTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            otherCheck.setSelected(UserConfig.getBoolean(baseName + "WithOthers", true));
            otherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WithOthers", otherCheck.isSelected());
                    okAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected String handleData() {
        try {
            normalization = null;
            List<Integer> checkedRowsIndices = sourceController.checkedRowsIndices();
            checkedColsIndices = sourceController.checkedColsIndices();
            int rowsNumber = checkedRowsIndices.size();
            int colsNumber = checkedColsIndices.size();
            double[][] data = new double[rowsNumber][colsNumber];
            boolean allNeg = true, allPos = true;
            for (int r = 0; r < rowsNumber; r++) {
                int row = checkedRowsIndices.get(r);
                List<String> tableRow = tableController.tableData.get(row);
                for (int c = 0; c < colsNumber; c++) {
                    int col = checkedColsIndices.get(c);
                    double d = data2D.doubleValue(tableRow.get(col + 1));
                    data[r][c] = d;
                    if (d > 0) {
                        allNeg = false;
                    } else if (d < 0) {
                        allPos = false;
                    }
                }
            }
            StringTable table = dataTable(calculate(data), checkedRowsIndices, allNeg, allPos);
            if (table == null) {
                return null;
            }
            return table.html();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return null;
    }

    private double[][] calculate(double[][] data) {
        try {
            if (data == null || data.length == 0) {
                return data;
            }
            normalization = Normalization.create()
                    .setSourceMatrix(data);
            if (zeroCheck.isSelected()) {
                normalization.setWidth(barWidth).setA(Normalization.Algorithm.Width);
            } else {
                normalization.setFrom(0).setTo(barWidth).setA(Normalization.Algorithm.MinMax);
            }
            if (columnsRadio.isSelected()) {
                return normalization.columnsNormalize();
            } else if (rowsRadio.isSelected()) {
                return normalization.rowsNormalize();
            } else if (allRadio.isSelected()) {
                return normalization.allNormalize();
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return null;
    }

    private StringTable dataTable(double[][] bars, List<Integer> checkedRowsIndices,
            boolean allNeg, boolean allPos) {
        try {
            if (bars == null) {
                return null;
            }
            int rowsNumber = checkedRowsIndices.size();
            List<String> names = new ArrayList<>();
            if (rowNumberCheck.isSelected()) {
                names.add(message("RowNumber"));
            }
            String title = data2D.displayName() + " - ";
            if (columnsRadio.isSelected()) {
                title += message("ColumnComparison");
            } else if (rowsRadio.isSelected()) {
                title += message("RowComparison");
                if (calculatedCheck.isSelected()) {
                    if (zeroCheck.isSelected()) {
                        names.add(message("MaxAbsolute"));
                    } else {
                        names.add(message("Maximum"));
                        names.add(message("Minimum"));
                    }
                }
            } else if (allRadio.isSelected()) {
                title += message("AllComparison");
            }
            if (otherCheck.isSelected()) {
                for (Data2DColumn col : data2D.getColumns()) {
                    names.add(col.getColumnName());
                }
            } else {
                for (int col : checkedColsIndices) {
                    names.add(data2D.colName(col));
                }
            }
            StringTable table = new StringTable(names, title);
            if (calculatedCheck.isSelected()) {
                if (columnsRadio.isSelected()) {
                    columnsNormalizationValues(table);
                } else if (allRadio.isSelected()) {
                    String comments;
                    if (zeroCheck.isSelected()) {
                        comments = "<P align=center>" + message("MaxAbsolute") + ": " + normalization.getMaxAbs() + "</P>\n";
                    } else {
                        comments = "<P align=center>" + message("Maximum") + ": " + normalization.getMax() + "&nbsp;".repeat(8)
                                + message("Minimum") + ": " + normalization.getMin() + "</P>\n";
                    }
                    table.setComments(comments);
                }
            }
            Normalization[] normalizationValues = normalization.getValues();
            for (int r = 0; r < rowsNumber; r++) {
                int row = checkedRowsIndices.get(r);
                List<String> htmlRow = new ArrayList<>();
                if (rowNumberCheck.isSelected()) {
                    htmlRow.add(data2D.rowName(row));
                }
                if (calculatedCheck.isSelected() && rowsRadio.isSelected()) {
                    if (zeroCheck.isSelected()) {
                        htmlRow.add("" + normalizationValues[r].getMaxAbs());
                    } else {
                        htmlRow.add("" + normalizationValues[r].getMax());
                        htmlRow.add("" + normalizationValues[r].getMin());
                    }
                }
                List<String> tableRow = tableController.tableData.get(row);
                for (int i = 0; i < data2D.getColumns().size(); i++) {
                    String value = tableRow.get(i + 1);
                    int c = checkedColsIndices.indexOf(i);
                    if (c < 0) {
                        if (otherCheck.isSelected()) {
                            htmlRow.add(value);
                        }
                    } else {
                        double width = bars[r][c];
                        Data2DColumn column = data2D.column(i);
                        Color color = column.getColor();
                        htmlRow.add(valueBar(value, width, color, allNeg, allPos));
                    }
                }
                table.add(htmlRow);
            }

            return table;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return null;
    }

    private void columnsNormalizationValues(StringTable table) {
        if (table == null || normalization == null) {
            return;
        }
        Normalization[] normalizationValues = normalization.getValues();
        if (normalizationValues == null) {
            return;
        }
        List<String> htmlRow = new ArrayList<>();
        if (zeroCheck.isSelected()) {
            for (int i = 0; i < data2D.getColumns().size(); i++) {
                int c = checkedColsIndices.indexOf(i);
                if (c < 0) {
                    if (otherCheck.isSelected()) {
                        htmlRow.add("");
                    }
                } else {
                    htmlRow.add(message("MaxAbsolute") + ": " + normalizationValues[c].getMaxAbs());
                }
            }
            table.add(htmlRow);
        } else {
            for (int i = 0; i < data2D.getColumns().size(); i++) {
                int c = checkedColsIndices.indexOf(i);
                if (c < 0) {
                    if (otherCheck.isSelected()) {
                        htmlRow.add("");
                    }
                } else {
                    htmlRow.add(message("Maximum") + ": " + normalizationValues[c].getMax());
                }
            }
            table.add(htmlRow);
            htmlRow = new ArrayList<>();
            for (int i = 0; i < data2D.getColumns().size(); i++) {
                int c = checkedColsIndices.indexOf(i);
                if (c < 0) {
                    if (otherCheck.isSelected()) {
                        htmlRow.add("");
                    }
                } else {
                    htmlRow.add(message("Minimum") + ": " + normalizationValues[c].getMin());
                }
            }
            table.add(htmlRow);
        }
    }

    protected String valueBar(String value, double width, Color color,
            boolean allNeg, boolean allPos) {
        String v;
        String valueDis = valueCheck.isSelected() ? value : "&nbsp;";
        if (zeroCheck.isSelected()) {
            Color nColor = FxColorTools.invert(color);
            if (width == 0) {
                if (allNeg || allPos) {
                    v = "<SPAN>" + valueDis + "</SPAN>";
                } else {
                    v = "<SPAN style=\"display: inline-block; width:" + (barWidth * 2) + "px;text-align:center;\">" + valueDis + "</SPAN>";
                }
            } else if (width < 0) {
                double nWidth = Math.abs(width);
                v = "<SPAN style=\"display: inline-block; width:" + (barWidth - nWidth) + "px;\">&nbsp;</SPAN>";
                v += "<SPAN style=\"background-color:" + FxColorTools.color2rgb(nColor)
                        + ";color:" + FxColorTools.color2rgb(FxColorTools.foreColor(nColor))
                        + ";display: inline-block; width:" + nWidth + "px;text-align:center;font-size:0.8em;\">-"
                        + (percentageCheck.isSelected() ? (int) (nWidth * 100 / barWidth) + "%" : "&nbsp;")
                        + "</SPAN>";

                if (allNeg) {
                    v += "<SPAN>" + valueDis + "</SPAN>";
                } else {
                    v += "<SPAN style=\"display: inline-block; width:" + barWidth + "px;text-align:left;\">" + valueDis + "</SPAN>";
                }
            } else {
                if (!allPos) {
                    v = "<SPAN style=\"display: inline-block; width:" + barWidth + "px;text-align:right;\">" + valueDis + "</SPAN>";
                } else {
                    v = "";
                }
                v += "<SPAN style=\"background-color:" + FxColorTools.color2rgb(color)
                        + ";color:" + FxColorTools.color2rgb(FxColorTools.foreColor(color))
                        + ";display: inline-block; width:" + width + "px;text-align:center;font-size:0.8em;\">"
                        + (percentageCheck.isSelected() ? (int) (width * 100 / barWidth) + "%" : "&nbsp;")
                        + "</SPAN>";
                v += "<SPAN style=\"display: inline-block; width:" + (barWidth - width) + "px;\">&nbsp;</SPAN>";
                if (allPos) {
                    v += "<SPAN>" + valueDis + "</SPAN>";
                }
            }
        } else {
            if (width == 0) {
                v = "<SPAN>" + valueDis + "</SPAN>";
            } else {
                v = "<SPAN style=\"background-color:" + FxColorTools.color2rgb(color)
                        + ";color:" + FxColorTools.color2rgb(FxColorTools.foreColor(color))
                        + ";display: inline-block; width:" + width + "px;text-align:center;font-size:0.8em;\">"
                        + (percentageCheck.isSelected() ? (int) (width * 100 / barWidth) + "%" : "&nbsp;")
                        + "</SPAN>";
                v += "<SPAN>" + valueDis + "</SPAN>";
            }
        }
        return v;
    }

    /*
        static
     */
    public static Data2DChartSelfComparisonBarsController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartSelfComparisonBarsController controller = (Data2DChartSelfComparisonBarsController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartSelfComparisonBarsFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
