package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mara.mybox.calculation.Normalization;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-25
 * @License Apache License Version 2.0
 */
public class Data2DChartComparisonBarsController extends BaseData2DChartHtmlController {

    protected String selectedValue2;
    protected int col1Index, col2Index, categoryIndex;

    protected double[] bars;
    protected Normalization normalization;
    protected Color color1, color2;

    @FXML
    protected ComboBox<String> valueColumn2Selector;

    public Data2DChartComparisonBarsController() {
        baseTitle = message("ComparisonBarsChart");
        TipsLabelKey = "ComparisonBarsChartTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            valueColumn2Selector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOptions();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void makeOptions() {
        try {
            super.makeOptions();

            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            valueColumn2Selector.getItems().clear();
            selectedValue2 = valueColumn2Selector.getSelectionModel().getSelectedItem();
            valueColumn2Selector.getItems().setAll(names);
            if (selectedValue2 != null && names.contains(selectedValue2)) {
                valueColumn2Selector.setValue(selectedValue2);
            } else {
                valueColumn2Selector.getSelectionModel().select(names.size() > 2 ? 2 : 0);
            }
            isSettingValues = false;

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
            selectedValue2 = valueColumn2Selector.getSelectionModel().getSelectedItem();
            if (selectedValue2 == null) {
                outOptionsError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            int col1 = data2D.colOrder(selectedValue);
            int col2 = data2D.colOrder(selectedValue2);
            dataColsIndices = new ArrayList<>();
            dataColsIndices.add(col1);
            if (!dataColsIndices.contains(col2)) {
                dataColsIndices.add(col2);
            }
            if (!dataColsIndices.contains(categorysCol)) {
                dataColsIndices.add(categorysCol);
            }
            for (int col : otherColsIndices) {
                if (!dataColsIndices.contains(col)) {
                    dataColsIndices.add(col);
                }
            }

            col1Index = dataColsIndices.indexOf(col1) + 1;
            col2Index = dataColsIndices.indexOf(col2) + 1;
            categoryIndex = dataColsIndices.indexOf(categorysCol) + 1;
            otherIndices = new ArrayList<>();
            for (int col : otherColsIndices) {
                otherIndices.add(dataColsIndices.indexOf(col) + 1);
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected String makeHtml() {
        chartData = chartMax();
        if (chartData == null || chartData.isEmpty()) {
            return null;
        }
        if (!makeBars()) {
            return null;
        }
        return writeHtml();
    }

    protected boolean makeBars() {
        try {
            if (chartData == null) {
                return false;
            }
            outputColumns = data2D.makeColumns(dataColsIndices, showRowNumber());
            normalization = null;
            int rowsNumber = chartData.size();
            String[] data = new String[2 * rowsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = chartData.get(r);
                data[r] = tableRow.get(col1Index);
                data[r + rowsNumber] = tableRow.get(col2Index);
            }
            normalization = Normalization.create().setSourceVector(data).setInvalidAs(invalidAs);
            if (absoluateRadio.isSelected()) {
                normalization.setWidth(barWidth).setA(Normalization.Algorithm.Absoluate);
            } else {
                normalization.setFrom(0).setTo(barWidth).setA(Normalization.Algorithm.MinMax);
            }
            bars = DoubleTools.toDouble(normalization.calculate(), InvalidAs.Zero);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
    }

    @Override
    public String baseChartTitle() {
        return data2D.displayName() + " - " + message("ComparisonBarsChart");
    }

    protected String writeHtml() {
        try {
            if (bars == null || data2D == null || normalization == null || chartData == null) {
                return null;
            }
            StringBuilder s = new StringBuilder();
            s.append(jsBody());
            String title = chartTitle();
            s.append("<DIV align=\"center\">\n");
            s.append("<H2>").append(title).append("</H2>\n");
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
            s.append("<TABLE>\n");
            s.append("<TR  style=\"font-weight:bold; \">\n");
            s.append("<TH align=center class=\"RowNumber\">").append(message("RowNumber")).append("</TH>\n");
            s.append("<TH>").append(selectedValue).append("</TH>\n");
            s.append("<TH align=center class=\"Category\">").append(selectedCategory).append("</TH>\n");
            s.append("<TH>").append(selectedValue2).append("</TH>\n");
            if (otherColsNames != null) {
                for (String name : otherColsNames) {
                    s.append("<TH class=\"Others\">").append(name).append("</TH>\n");
                }
            }
            s.append("</TR>\n");

            Random random = new Random();
            if (randomColor) {
                color1 = null;
                color2 = null;
            } else {
                color1 = outputColumns.get(col1Index).getColor();
                color2 = outputColumns.get(col2Index).getColor();
            }
            if (color1 == null) {
                color1 = FxColorTools.randomColor(random);
            }
            if (color2 == null) {
                color2 = FxColorTools.randomColor(random);
            }
            int rowsNumber = chartData.size();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> row = chartData.get(r);
                s.append("<TR>\n");

                s.append("<TD align=center class=\"RowNumber\">")
                        .append(message("Row")).append(row.get(0)).append("</TD>\n");

                s.append("<TD align=right>")
                        .append("<SPAN class=\"DataValue\">").append(row.get(col1Index)).append("</SPAN>")
                        .append(bar(bars[r], color1)).append("</TD>\n");

                s.append("<TD align=center class=\"Category\">").append(row.get(categoryIndex)).append("</TD>\n");

                s.append("<TD align=left>")
                        .append(bar(bars[r + rowsNumber], color2))
                        .append("<SPAN class=\"DataValue\">").append(row.get(col2Index)).append("</SPAN>")
                        .append("</TD>\n");
                for (int index : otherIndices) {
                    s.append("<TD class=\"Others\">").append(row.get(index)).append("</TD>\n");
                }
                s.append("</TR>\n");
            }
            s.append("</Table>\n");
            s.append(jsComments());
            s.append("</DIV>\n</BODY>\n");
            return HtmlWriteTools.html(title, "utf-8", null, s.toString());
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
    }

    protected String bar(double width, Color color) {
        Color dColor = color;
        double dWitdh = width;
        if (width < 0) {
            dColor = FxColorTools.invert(color);
            dWitdh = Math.abs(width);
        }
        int pec = (int) (dWitdh * 100 / barWidth);
        if (pec == 0) {
            return "<SPAN class=\"Percentage\">0%</SPAN>";
        } else {
            return "<SPAN style=\"background-color:" + FxColorTools.color2rgb(dColor)
                    + ";color:" + FxColorTools.color2rgb(FxColorTools.foreColor(dColor))
                    + ";display: inline-block; width:" + (int) dWitdh + "px;font-size:1em;\">"
                    + "<SPAN class=\"Percentage\">" + (width < 0 ? "-" : "") + pec + "%</SPAN>"
                    + "&nbsp;</SPAN>";
        }
    }

    /*
        static
     */
    public static Data2DChartComparisonBarsController open(ControlData2DLoad tableController) {
        try {
            Data2DChartComparisonBarsController controller = (Data2DChartComparisonBarsController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartComparisonBarsFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
