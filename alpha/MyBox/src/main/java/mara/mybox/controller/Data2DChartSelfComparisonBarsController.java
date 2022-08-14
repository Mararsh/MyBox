package mara.mybox.controller;

import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.calculation.Normalization;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-20
 * @License Apache License Version 2.0
 */
public class Data2DChartSelfComparisonBarsController extends BaseData2DChartHtmlController {

    protected Normalization normalization;

    public Data2DChartSelfComparisonBarsController() {
        baseTitle = message("SelfComparisonBarsChart");
        TipsLabelKey = "SelfComparisonBarsChartTips";
    }

    @Override
    public boolean initData() {
        if (!super.initData()) {
            return false;
        }
        if (!dataColsIndices.contains(categorysCol)) {
            dataColsIndices.add(categorysCol);
        }
        return true;
    }

    @Override
    protected String handleData() {
        try {
            if (outputData == null) {
                return null;
            }
            normalization = null;
            int rowsNumber = outputData.size();
            int colsNumber = checkedColsIndices.size();
            double[][] data = new double[rowsNumber][colsNumber];
            boolean allNeg = true, allPos = true;
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = outputData.get(r);
                for (int c = 0; c < colsNumber; c++) {
                    double d = DoubleTools.toDouble(tableRow.get(c + 1), invalidAs);
                    data[r][c] = d;
                    if (d > 0) {
                        allNeg = false;
                    } else if (d < 0) {
                        allPos = false;
                    }
                }
            }
            return dataHtml(calculate(data), allNeg, allPos);
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
            if (absoluateRadio.isSelected()) {
                normalization.setWidth(barWidth).setA(Normalization.Algorithm.Absoluate);
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

    private String dataHtml(double[][] bars, boolean allNeg, boolean allPos) {
        try {
            if (bars == null || normalization == null) {
                return null;
            }
            int rowsNumber = bars.length;
            int colsNumber = checkedColsIndices.size();

            StringBuilder s = new StringBuilder();
            s.append(jsBody());
            String title = data2D.displayName() + " - ";
            if (columnsRadio.isSelected()) {
                title += message("ColumnComparison");
            } else if (rowsRadio.isSelected()) {
                title += message("RowComparison");
            } else if (allRadio.isSelected()) {
                title += message("AllComparison");
            }
            s.append("<DIV align=\"center\">\n");
            s.append("<H2>").append(title).append("</H2>\n");

            if (allRadio.isSelected()) {
                if (absoluateRadio.isSelected()) {
                    s.append("<P class=\"Calculated\" align=center>").append(message("MaxAbsolute")).append(": ")
                            .append(normalization.getMaxAbs()).append("</P>\n");
                } else {
                    s.append("<P class=\"Calculated\" align=center>").append(message("Maximum")).append(": ")
                            .append(normalization.getMax()).append("&nbsp;".repeat(8))
                            .append(message("Minimum")).append(": ").append(normalization.getMin()).append("</P>\n");
                }
            }

            s.append("<TABLE>\n");
            s.append("<TR  style=\"font-weight:bold; \">\n");
            s.append("<TH align=center class=\"RowNumber\">").append(message("RowNumber")).append("</TH>\n");
            s.append("<TH align=center class=\"Category\">").append(selectedCategory).append("</TH>\n");
            if (rowsRadio.isSelected()) {
                if (absoluateRadio.isSelected()) {
                    s.append("<TH class=\"Calculated\">").append(message("MaxAbsolute")).append("</TH>\n");
                } else {
                    s.append("<TH class=\"Calculated\">").append(message("Maximum")).append("</TH>\n");
                    s.append("<TH class=\"Calculated\">").append(message("Minimum")).append("</TH>\n");
                }
            }
            for (int col : checkedColsIndices) {
                s.append("<TH>").append(data2D.columnName(col)).append("</TH>\n");
            }
            s.append("</TR>\n");

            if (columnsRadio.isSelected()) {
                columnsNormalizationValues(s);
            }

            Normalization[] normalizationValues = normalization.getValues();
            Color[] color = new Color[checkedColsIndices.size()];
            for (int i = 0; i < colsNumber; i++) {
                Data2DColumn column = data2D.column(checkedColsIndices.get(i));
                color[i] = column.getColor();
            }
            int categoryIndex = dataColsIndices.indexOf(categorysCol);
            if (categoryIndex < 0) {
                categoryIndex = colsNumber + 1;
            }
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = outputData.get(r);

                s.append("<TR>\n");

                s.append("<TD align=center class=\"RowNumber\">")
                        .append(message("Row")).append(tableRow.get(0)).append("</TD>\n");

                s.append("<TD align=center class=\"Category\">")
                        .append(tableRow.size() > categoryIndex ? tableRow.get(categoryIndex + 1) : "")
                        .append("</TD>\n");

                if (rowsRadio.isSelected()) {
                    if (absoluateRadio.isSelected()) {
                        s.append("<TD align=center class=\"Calculated\">")
                                .append(normalizationValues[r].getMaxAbs()).append("</TD>\n");
                    } else {
                        s.append("<TD align=center class=\"Calculated\">")
                                .append(normalizationValues[r].getMax()).append("</TD>\n");
                        s.append("<TD align=center class=\"Calculated\">")
                                .append(normalizationValues[r].getMin()).append("</TD>\n");
                    }
                }
                for (int i = 0; i < colsNumber; i++) {
                    s.append("<TD>")
                            .append(valueBar(tableRow.get(i + 1), bars[r][i], color[i], allNeg, allPos))
                            .append("</TD>\n");
                }

                s.append("</TR>\n");
            }
            s.append("</Table>\n");
            s.append(jsComments());
            s.append("</DIV>\n</BODY>\n");
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return null;
    }

    private void columnsNormalizationValues(StringBuilder s) {
        if (s == null || normalization == null) {
            return;
        }
        Normalization[] normalizationValues = normalization.getValues();
        if (normalizationValues == null) {
            return;
        }
        int colsNumber = checkedColsIndices.size();
        if (absoluateRadio.isSelected()) {
            s.append("<TR class=\"Calculated\">\n");
            s.append("<TD class=\"RowNumber\"></TD>\n");
            s.append("<TD class=\"Category\"></TD>\n");
            for (int i = 0; i < colsNumber; i++) {
                s.append("<TD align=center>")
                        .append(message("MaxAbsolute")).append(": ").append(normalizationValues[i].getMaxAbs())
                        .append("</TD>\n");
            }
            s.append("</TR>\n");
        } else {
            s.append("<TR class=\"Calculated\">\n");
            s.append("<TD class=\"RowNumber\"></TD>\n");
            s.append("<TD class=\"Category\"></TD>\n");
            for (int i = 0; i < colsNumber; i++) {
                s.append("<TD align=center>")
                        .append(message("Maximum")).append(": ").append(normalizationValues[i].getMax())
                        .append("</TD>\n");
            }
            s.append("</TR>\n");

            s.append("<TR class=\"Calculated\">\n");
            s.append("<TD class=\"RowNumber\"></TD>\n");
            s.append("<TD class=\"Category\"></TD>\n");
            for (int i = 0; i < colsNumber; i++) {
                s.append("<TD align=center >")
                        .append(message("Minimum")).append(": ").append(normalizationValues[i].getMin())
                        .append("</TD>\n");
            }
            s.append("</TR>\n");
        }

    }

    protected String valueBar(String value, double width, Color color,
            boolean allNeg, boolean allPos) {
        StringBuilder s = new StringBuilder();
        String vSpan = "<SPAN class=\"DataValue\">" + value + "</SPAN>";
        if (absoluateRadio.isSelected()) {
            Color nColor = FxColorTools.invert(color);
            if (width == 0) {
                if (allNeg || allPos) {
                    s.append(vSpan);
                } else {
                    s.append("<SPAN style=\"display: inline-block; width:").append(barWidth * 2).
                            append("px;text-align:center;\">").append(vSpan).append("&nbsp;</SPAN>");
                }
            } else if (width < 0) {
                double nWidth = -width;
                s.append("<SPAN style=\"display: inline-block; width:").append(barWidth - nWidth).append("px;\">&nbsp;</SPAN>");
                s.append("<SPAN style=\"background-color:").append(FxColorTools.color2rgb(nColor))
                        .append(";color:").append(FxColorTools.color2rgb(FxColorTools.foreColor(nColor)))
                        .append(";display: inline-block; width:").append(nWidth).append("px;text-align:center;font-size:0.8em;\">")
                        .append("<SPAN class=\"Percentage\">-").append((int) (nWidth * 100 / barWidth)).append("%</SPAN>")
                        .append("&nbsp;</SPAN>");

                if (allNeg) {
                    s.append(vSpan);
                } else {
                    s.append("<SPAN style=\"display: inline-block; width:").append(barWidth)
                            .append("px;text-align:left;\">").append(vSpan).append("&nbsp;</SPAN>");
                }
            } else {
                if (!allPos) {
                    s.append("<SPAN style=\"display: inline-block; width:").append(barWidth)
                            .append("px;text-align:right;\">").append(vSpan).append("&nbsp;</SPAN>");
                }
                s.append("<SPAN style=\"background-color:").append(FxColorTools.color2rgb(color))
                        .append(";color:").append(FxColorTools.color2rgb(FxColorTools.foreColor(color)))
                        .append(";display: inline-block; width:").append(width).append("px;text-align:center;font-size:0.8em;\">")
                        .append("<SPAN class=\"Percentage\">").append((int) (width * 100 / barWidth)).append("%</SPAN>")
                        .append("&nbsp;</SPAN>");
                s.append("<SPAN style=\"display: inline-block; width:").append(barWidth - width).append("px;\">&nbsp;</SPAN>");
                if (allPos) {
                    s.append(vSpan);
                }
            }
        } else {
            if (width == 0) {
                s.append(vSpan);
            } else {
                s.append("<SPAN style=\"background-color:").append(FxColorTools.color2rgb(color))
                        .append(";color:").append(FxColorTools.color2rgb(FxColorTools.foreColor(color)))
                        .append(";display: inline-block; width:").append(width).append("px;text-align:center;font-size:0.8em;\">")
                        .append("<SPAN class=\"Percentage\">").append((int) (width * 100 / barWidth)).append("%</SPAN>")
                        .append("&nbsp;</SPAN>");
                s.append(vSpan);
            }
        }
        return s.toString();
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
