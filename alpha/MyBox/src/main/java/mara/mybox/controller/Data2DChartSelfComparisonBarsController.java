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
            StringTable table = createHtml(calculate(data), checkedRowsIndices, allNeg, allPos);
            if (table != null) {
                return table.html();
            }
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
            Normalization n = Normalization.create();
            if (zeroCheck.isSelected()) {
                n.setFrom(barWidth)
                        .setA(Normalization.Algorithm.Width);
            } else {
                n.setFrom(0).setTo(barWidth)
                        .setA(Normalization.Algorithm.MinMax);
            }
            if (columnsRadio.isSelected()) {
                return n.columnsNormalize(data);
            } else if (rowsRadio.isSelected()) {
                return n.rowsNormalize(data);
            } else if (allRadio.isSelected()) {
                return n.allNormalize(data);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return null;
    }

    private StringTable createHtml(double[][] bars, List<Integer> checkedRowsIndices,
            boolean allNeg, boolean allPos) {
        try {
            int rowsNumber = checkedRowsIndices.size();
            List<String> names = new ArrayList<>();
            if (rowNumberCheck.isSelected()) {
                names.add(message("RowNumber"));
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
            String title = data2D.displayName() + " - ";
            if (columnsRadio.isSelected()) {
                title += message("ColumnComparison");
            } else if (rowsRadio.isSelected()) {
                title += message("RowComparison");
            } else if (allRadio.isSelected()) {
                title += message("AllComparison");
            }
            StringTable table = new StringTable(names, title);
            for (int r = 0; r < rowsNumber; r++) {
                int row = checkedRowsIndices.get(r);
                List<String> htmlRow = new ArrayList<>();
                if (rowNumberCheck.isSelected()) {
                    htmlRow.add(data2D.rowName(row));
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

    protected String valueBar(String value, double width, Color color,
            boolean allNeg, boolean allPos) {
        String v;
        if (zeroCheck.isSelected()) {
            Color nColor = FxColorTools.invert(color);
            if (width == 0) {
                if (allNeg || allPos) {
                    v = "<SPAN>" + value + "</SPAN>";
                } else {
                    v = "<SPAN style=\"display: inline-block; width:" + (barWidth * 2) + "px;text-align:center;\">" + value + "</SPAN>";
                }
            } else if (width < 0) {
                double nWidth = Math.abs(width);
                v = "<SPAN style=\"display: inline-block; width:" + (barWidth - nWidth) + "px;\">&nbsp;</SPAN>";
                v += "<SPAN style=\"background-color:" + FxColorTools.color2rgb(nColor)
                        + ";color:" + FxColorTools.color2rgb(FxColorTools.foreColor(nColor))
                        + ";display: inline-block; width:" + nWidth + "px;text-align:center;font-size:0.8em;\">-"
                        + (int) (nWidth * 100 / barWidth) + "%</SPAN>";
                if (allNeg) {
                    v += "<SPAN>" + value + "</SPAN>";
                } else {
                    v += "<SPAN style=\"display: inline-block; width:" + barWidth + "px;text-align:left;\">" + value + "</SPAN>";
                }
            } else {
                if (!allPos) {
                    v = "<SPAN style=\"display: inline-block; width:" + barWidth + "px;text-align:right;\">" + value + "</SPAN>";
                } else {
                    v = "";
                }
                v += "<SPAN style=\"background-color:" + FxColorTools.color2rgb(color)
                        + ";color:" + FxColorTools.color2rgb(FxColorTools.foreColor(color))
                        + ";display: inline-block; width:" + width + "px;text-align:center;font-size:0.8em;\">"
                        + (int) (width * 100 / barWidth) + "%</SPAN>";
                v += "<SPAN style=\"display: inline-block; width:" + (barWidth - width) + "px;\">&nbsp;</SPAN>";
                if (allPos) {
                    v += "<SPAN>" + value + "</SPAN>";
                }
            }
        } else {
            if (width == 0) {
                v = "<SPAN>" + value + "</SPAN>";
            } else {
                v = "<SPAN style=\"background-color:" + FxColorTools.color2rgb(color)
                        + ";color:" + FxColorTools.color2rgb(FxColorTools.foreColor(color))
                        + ";display: inline-block; width:" + width + "px;text-align:center;font-size:0.8em;\">"
                        + (int) (width * 100 / barWidth) + "%</SPAN>"
                        + "<SPAN>" + value + "</SPAN>";
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
