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
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
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
    protected int col1, col2, rowsNumber;
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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            col1 = data2D.colOrder(selectedValue);
            col2 = data2D.colOrder(selectedValue2);
            dataColsIndices = new ArrayList<>();
            dataColsIndices.add(col1);
            dataColsIndices.add(col2);
            outputColumns.add(data2D.column(col1));
            outputColumns.add(data2D.column(col2));
            if (!dataColsIndices.contains(categorysCol)) {
                dataColsIndices.add(categorysCol);
                outputColumns.add(data2D.column(categorysCol));
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void readData() {
        try {
            outputData = readData(dataColsIndices, true);
            if (outputData == null) {
                return;
            }
            normalization = null;
            rowsNumber = outputData.size();
            String[] data = new String[2 * rowsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = outputData.get(r);
                data[r] = tableRow.get(1);
                data[r + rowsNumber] = tableRow.get(1);
            }
            normalization = Normalization.create().setSourceVector(data).setInvalidAs(invalidAs);
            if (absoluateRadio.isSelected()) {
                normalization.setWidth(barWidth).setA(Normalization.Algorithm.Absoluate);
            } else {
                normalization.setFrom(0).setTo(barWidth).setA(Normalization.Algorithm.MinMax);
            }
            bars = DoubleTools.toDouble(normalization.calculate(), InvalidAs.Zero);
            otherData = null;
            if (otherColsIndices != null) {
                otherData = readData(otherColsIndices, false);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    @Override
    protected String makeHtml() {
        try {
            if (bars == null || outputData == null || normalization == null) {
                return null;
            }
            StringBuilder s = new StringBuilder();
            s.append(jsBody());
            String title = data2D.displayName() + " - " + message("ComparisonBarsChart");
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
            Random random = new Random();
            if (randomColor) {
                color1 = null;
                color2 = null;
            } else {
                color1 = data2D.column(col1).getColor();
                color2 = data2D.column(col2).getColor();
            }
            if (color1 == null) {
                color1 = FxColorTools.randomColor(random);
            }
            if (color2 == null) {
                color2 = FxColorTools.randomColor(random);
            }
            s.append("<TABLE>\n");
            s.append("<TR  style=\"font-weight:bold; \">\n");
            s.append("<TH align=center class=\"RowNumber\">").append(message("RowNumber")).append("</TH>\n");
            s.append("<TH>").append(selectedValue).append("</TH>\n");
            s.append("<TH align=center class=\"Category\">").append(selectedCategory).append("</TH>\n");
            s.append("<TH>").append(selectedValue2).append("</TH>\n");
            int otherColsNumber = otherColsIndices != null ? otherColsIndices.size() : 0;
            if (otherColsNumber > 0) {
                for (int col : otherColsIndices) {
                    s.append("<TH class=\"Others\">").append(data2D.columnName(col)).append("</TH>\n");
                }
            }
            s.append("</TR>\n");
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = outputData.get(r);
                s.append("<TR>\n");

                s.append("<TD align=center class=\"RowNumber\">")
                        .append(message("Row")).append(tableRow.get(0)).append("</TD>\n");

                s.append("<TD align=right>")
                        .append("<SPAN class=\"DataValue\">").append(tableRow.get(1)).append("</SPAN>")
                        .append(bar(bars[r], color1)).append("</TD>\n");

                int pos = dataColsIndices.indexOf(categorysCol);
                String cv;
                if (pos >= 0) {
                    cv = tableRow.get(pos + 1);
                } else if (tableRow.size() > 3) {
                    cv = tableRow.get(3);
                } else {
                    cv = "";
                }
                s.append("<TD align=center class=\"Category\">").append(cv).append("</TD>\n");

                s.append("<TD align=left>")
                        .append(bar(bars[r + rowsNumber], color2))
                        .append("<SPAN class=\"DataValue\">").append(tableRow.get(2)).append("</SPAN>")
                        .append("</TD>\n");
                if (otherColsNumber > 0) {
                    List<String> otherRow = otherData.get(r);
                    for (int i = 0; i < otherColsNumber; i++) {
                        s.append("<TD class=\"Others\">").append(otherRow.get(i)).append("</TD>\n");
                    }
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
        }
        return null;
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
