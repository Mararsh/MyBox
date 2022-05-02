package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mara.mybox.data.Normalization;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
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
    protected double[] bar;
    protected Normalization normalization;

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

            sourceController.noColumnSelection(true);

            valueColumn2Selector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOptions();
                }
            });

            webViewController.initStyle = "";

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
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
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        selectedValue2 = valueColumn2Selector.getSelectionModel().getSelectedItem();
        if (selectedValue2 == null) {
            infoLabel.setText(message("SelectToHandle"));
            okButton.setDisable(true);
            return false;
        }
        col1 = data2D.colOrder(selectedValue);
        col2 = data2D.colOrder(selectedValue2);
        return ok;
    }

    @Override
    public boolean initData() {
        dataColsIndices = new ArrayList<>();
        dataColsIndices.add(col1);
        dataColsIndices.add(col2);
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
            rowsNumber = outputData.size();
            double[] data = new double[2 * rowsNumber];
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = outputData.get(r);
                data[r] = data2D.doubleValue(tableRow.get(1));
                data[r + rowsNumber] = data2D.doubleValue(tableRow.get(2));
            }
            normalization = Normalization.create().setSourceVector(data);
            if (zeroCheck.isSelected()) {
                normalization.setWidth(barWidth).setA(Normalization.Algorithm.Width);
            } else {
                normalization.setFrom(0).setTo(barWidth).setA(Normalization.Algorithm.MinMax);
            }
            bar = normalization.calculate();

            StringBuilder s = new StringBuilder();
            s.append(jsBody());
            String title = data2D.displayName() + " - " + message("ComparisonBarsChart");
            s.append("<DIV align=\"center\">\n");
            s.append("<H2>").append(title).append("</H2>\n");
            if (zeroCheck.isSelected()) {
                s.append("<P class=\"Calculated\" align=center>").append(message("MaxAbsolute")).append(": ")
                        .append(normalization.getMaxAbs()).append("</P>\n");
            } else {
                s.append("<P class=\"Calculated\" align=center>").append(message("Maximum")).append(": ")
                        .append(normalization.getMax()).append("&nbsp;".repeat(8))
                        .append(message("Minimum")).append(": ").append(normalization.getMin()).append("</P>\n");
            }
            normalization = null;
            Color color1 = data2D.column(col1).getColor();
            Color color2 = data2D.column(col2).getColor();
            s.append("<TABLE>\n");
            s.append("<TR  style=\"font-weight:bold; \">\n");
            s.append("<TH align=center class=\"RowNumber\">").append(message("RowNumber")).append("</TH>\n");
            s.append("<TH>").append(selectedValue).append("</TH>\n");
            s.append("<TH align=center class=\"Category\">").append(selectedCategory).append("</TH>\n");
            s.append("<TH>").append(selectedValue2).append("</TH>\n");
            s.append("</TR>\n");
            for (int r = 0; r < rowsNumber; r++) {
                List<String> tableRow = outputData.get(r);
                s.append("<TR>\n");

                s.append("<TD align=center class=\"RowNumber\">")
                        .append(message("Row")).append(tableRow.get(0)).append("</TD>\n");

                s.append("<TD align=right>")
                        .append("<SPAN class=\"DataValue\">").append(tableRow.get(1)).append("</SPAN>")
                        .append(bar(bar[r], color1)).append("</TD>\n");

                int pos = dataColsIndices.indexOf(categorysCol);
                String v;
                if (pos >= 0) {
                    v = tableRow.get(pos);
                } else if (tableRow.size() > 2) {
                    v = tableRow.get(2);
                } else {
                    v = "";
                }
                s.append("<TD align=center class=\"Category\">").append(v).append("</TD>\n");

                s.append("<TD align=left>")
                        .append(bar(bar[r + rowsNumber], color2))
                        .append("<SPAN class=\"DataValue\">").append(tableRow.get(2)).append("</SPAN>")
                        .append("</TD>\n");

                s.append("</TR>\n");
            }
            s.append("</Table>\n");
            s.append(jsComments());
            s.append("</DIV>\n</BODY>\n");
            bar = null;
            return HtmlWriteTools.html(title, "utf-8", null, s.toString());
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
        }
        bar = null;
        normalization = null;
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
    public static Data2DChartComparisonBarsController open(ControlData2DEditTable tableController) {
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
