package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;
import mara.mybox.calculation.Normalization;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
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
    protected double[][] bars;
    protected boolean allNeg, allPos;
    protected Color[] colors;

    public Data2DChartSelfComparisonBarsController() {
        baseTitle = message("SelfComparisonBarsChart");
        TipsLabelKey = "SelfComparisonBarsChartTips";
    }

    @Override
    public void readData() {
        try {
            List<Integer> cols = new ArrayList<>();
            cols.addAll(dataColsIndices);
            cols.addAll(otherColsIndices);
            outputData = scaledData(cols, true);
            if (outputData == null) {
                return;
            }
            normalization = null;
            int rowsNumber = outputData.size();
            int colsNumber = dataColsIndices.size();
            String[][] data = new String[rowsNumber][colsNumber];
            allNeg = true;
            allPos = true;
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = outputData.get(r);
                for (int c = 0; c < colsNumber; c++) {
                    String s = tableRow.get(c + 1);
                    data[r][c] = s;
                    double d = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    if (d > 0) {
                        allNeg = false;
                    } else if (d < 0) {
                        allPos = false;
                    }
                }
            }
            bars = DoubleTools.toDouble(calculate(data), InvalidAs.Zero);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
    }

    private String[][] calculate(String[][] data) {
        try {
            if (data == null || data.length == 0) {
                return data;
            }
            normalization = Normalization.create().setSourceMatrix(data).setInvalidAs(invalidAs);
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
            MyBoxLog.error(e);
        }
        return null;
    }

    @Override
    protected String makeHtml() {
        try {
            if (bars == null || outputData == null || normalization == null) {
                return null;
            }
            int rowsNumber = bars.length;
            int colsNumber = dataColsIndices.size();

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
                            .append(DoubleTools.scale(normalization.getMaxAbs(), scale))
                            .append("</P>\n");
                } else {
                    s.append("<P class=\"Calculated\" align=center>").append(message("Maximum")).append(": ")
                            .append(DoubleTools.scale(normalization.getMax(), scale))
                            .append("&nbsp;".repeat(8))
                            .append(message("Minimum")).append(": ")
                            .append(DoubleTools.scale(normalization.getMin(), scale))
                            .append("</P>\n");
                }
            }

            s.append("<TABLE>\n");
            s.append("<TR  style=\"font-weight:bold; \">\n");
            s.append("<TH align=center class=\"RowNumber\">").append(message("RowNumber")).append("</TH>\n");
            if (rowsRadio.isSelected()) {
                if (absoluateRadio.isSelected()) {
                    s.append("<TH class=\"Calculated\">").append(message("MaxAbsolute")).append("</TH>\n");
                } else {
                    s.append("<TH class=\"Calculated\">").append(message("Maximum")).append("</TH>\n");
                    s.append("<TH class=\"Calculated\">").append(message("Minimum")).append("</TH>\n");
                }
            }
            for (int col : dataColsIndices) {
                s.append("<TH>").append(data2D.columnName(col)).append("</TH>\n");
            }
            int otherColsNumber = otherColsIndices != null ? otherColsIndices.size() : 0;
            if (otherColsNumber > 0) {
                for (int col : otherColsIndices) {
                    s.append("<TH class=\"Others\">").append(data2D.columnName(col)).append("</TH>\n");
                }
            }
            s.append("</TR>\n");

            if (columnsRadio.isSelected()) {
                columnsNormalizationValues(s);
            }

            Normalization[] normalizationValues = normalization.getValues();
            colors = new Color[colsNumber];
            Random random = new Random();
            for (int i = 0; i < colsNumber; i++) {
                Color color = randomColor ? null : data2D.column(dataColsIndices.get(i)).getColor();
                if (color == null) {
                    color = FxColorTools.randomColor(random);
                }
                colors[i] = color;
            }
            int otherStart = dataColsIndices.size() + 1;
            for (int r = 0; r < rowsNumber; r++) {
                List<String> row = outputData.get(r);

                s.append("<TR>\n");

                s.append("<TD align=center class=\"RowNumber\">")
                        .append(message("Row")).append(row.get(0)).append("</TD>\n");

                if (rowsRadio.isSelected()) {
                    if (absoluateRadio.isSelected()) {
                        s.append("<TD align=center class=\"Calculated\">")
                                .append(DoubleTools.scale(normalizationValues[r].getMaxAbs(), scale))
                                .append("</TD>\n");
                    } else {
                        s.append("<TD align=center class=\"Calculated\">")
                                .append(DoubleTools.scale(normalizationValues[r].getMax(), scale))
                                .append("</TD>\n");
                        s.append("<TD align=center class=\"Calculated\">")
                                .append(DoubleTools.scale(normalizationValues[r].getMin(), scale))
                                .append("</TD>\n");
                    }
                }
                for (int i = 0; i < colsNumber; i++) {
                    s.append("<TD>")
                            .append(valueBar(row.get(i + 1), bars[r][i], colors[i], allNeg, allPos))
                            .append("</TD>\n");
                }
                for (int i = otherStart; i < row.size(); i++) {
                    s.append("<TD class=\"Others\">").append(row.get(i)).append("</TD>\n");
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
        int colsNumber = dataColsIndices.size();
        if (absoluateRadio.isSelected()) {
            s.append("<TR class=\"Calculated\">\n");
            s.append("<TD class=\"RowNumber\"></TD>\n");
            for (int i = 0; i < colsNumber; i++) {
                s.append("<TD align=center>")
                        .append(message("MaxAbsolute")).append(": ")
                        .append(DoubleTools.scale(normalizationValues[i].getMaxAbs(), scale))
                        .append("</TD>\n");
            }
            if (otherColsIndices != null) {
                for (int col : otherColsIndices) {
                    s.append("<TD class=\"Others\">").append("</TD>\n");
                }
            }
            s.append("</TR>\n");
        } else {
            s.append("<TR class=\"Calculated\">\n");
            s.append("<TD class=\"RowNumber\"></TD>\n");
            for (int i = 0; i < colsNumber; i++) {
                s.append("<TD align=center>")
                        .append(message("Maximum")).append(": ")
                        .append(DoubleTools.scale(normalizationValues[i].getMax(), scale))
                        .append("</TD>\n");
            }
            if (otherColsIndices != null) {
                for (int col : otherColsIndices) {
                    s.append("<TD class=\"Others\">").append("</TD>\n");
                }
            }
            s.append("</TR>\n");

            s.append("<TR class=\"Calculated\">\n");
            s.append("<TD class=\"RowNumber\"></TD>\n");
            for (int i = 0; i < colsNumber; i++) {
                s.append("<TD align=center >")
                        .append(message("Minimum")).append(": ")
                        .append(DoubleTools.scale(normalizationValues[i].getMin(), scale))
                        .append("</TD>\n");
            }
            if (otherColsIndices != null) {
                for (int col : otherColsIndices) {
                    s.append("<TD class=\"Others\">").append("</TD>\n");
                }
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
    public static Data2DChartSelfComparisonBarsController open(ControlData2DLoad tableController) {
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
