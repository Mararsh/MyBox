package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mara.mybox.data.Normalization;
import mara.mybox.data.StringTable;
import static mara.mybox.data.StringTable.body;
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
public class Data2DChartComparisonBarsController extends BaseData2DHtmlChartController {

    protected String selectedValue2;

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
        return ok;
    }

    @Override
    protected String handleData() {
        try {
            List<Integer> checkedRowsIndices = sourceController.checkedRowsIndices();
            int rowsNumber = checkedRowsIndices.size();
            int col1 = data2D.colOrder(selectedValue);
            int col2 = data2D.colOrder(selectedValue2);
            int categorysCol = data2D.colOrder(selectedCategory);
            double[] data = new double[2 * rowsNumber];
            String[] categorys = new String[rowsNumber];
            boolean allNeg1 = true, allPos1 = true, allNeg2 = true, allPos2 = true;
            for (int r = 0; r < rowsNumber; r++) {
                int row = checkedRowsIndices.get(r);
                List<String> tableRow = tableController.tableData.get(row);
                categorys[r] = tableRow.get(categorysCol + 1);
                double d = data2D.doubleValue(tableRow.get(col1 + 1));
                data[r] = d;
                if (d > 0) {
                    allNeg1 = false;
                } else if (d < 0) {
                    allPos1 = false;
                }
                d = data2D.doubleValue(tableRow.get(col2 + 1));
                data[r + rowsNumber] = d;
                if (d > 0) {
                    allNeg2 = false;
                } else if (d < 0) {
                    allPos2 = false;
                }

            }
            double[] bar;
            if (zeroCheck.isSelected()) {
                bar = Normalization.width(data, barWidth);
            } else {
                bar = Normalization.minMax(data, 0, barWidth);
            }

            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(selectedValue, selectedCategory, selectedValue2));
            String title = data2D.displayName() + " - " + message("ComparisonBarsChart");
            StringTable table = new StringTable(names, title);
            Color color1 = data2D.column(col1).getColor();
            Color color2 = data2D.column(col2).getColor();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> htmlRow = new ArrayList<>();
                List<String> tableRow = tableController.tableData.get(checkedRowsIndices.get(r));

                htmlRow.add(valueBar(tableRow.get(col1 + 1), bar[r], color1, allNeg1, allPos1));

                htmlRow.add(categorys[r]);

                htmlRow.add(valueBar(tableRow.get(col2 + 1), bar[r + rowsNumber], color2, allNeg2, allPos2));

                table.add(htmlRow);
            }
            return HtmlWriteTools.html(table.getTitle(), "utf-8", null, body(table));
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
