package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.Normalization;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-20
 * @License Apache License Version 2.0
 */
public class Data2DColorBarsController extends BaseData2DHandleController {

    protected int barWidth = 100;

    @FXML
    protected CheckBox zeroCheck, otherCheck;
    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected ControlWebView webViewController;

    public Data2DColorBarsController() {
        baseTitle = message("ColorBars");
        TipsLabelKey = "ColorBarsTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            webViewController.setParent(this);

            barWidth = UserConfig.getInt(baseName + "Width", 150);
            if (barWidth < 0) {
                barWidth = 100;
            }
            widthSelector.getItems().addAll(
                    Arrays.asList("150", "100", "200", "50", "80", "120", "300")
            );
            widthSelector.setValue(barWidth + "");
            widthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(widthSelector.getValue());
                        if (v > 0) {
                            barWidth = v;
                            UserConfig.setInt(baseName + "Width", v);
                            widthSelector.getEditor().setStyle(null);
                            okAction();
                        } else {
                            widthSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        widthSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            zeroCheck.setSelected(UserConfig.getBoolean(baseName + "ZeroBased", true));
            zeroCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ZeroBased", zeroCheck.isSelected());
                    okAction();
                }
            });

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
    public void setParameters(ControlData2DEditTable editController) {
        try {
            super.setParameters(editController);

            sourceController.showAllPages(false);

            okAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        okAction();
    }

    @Override
    public void rowNumberCheckChanged() {
        super.rowNumberCheckChanged();
        okAction();
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            private List<Integer> checkedRowsIndices, checkedColsIndices;
            private double[][] data, bars;
            private StringTable table;
            private boolean zeroBased, allNeg, allPos;

            @Override
            protected boolean handle() {
                try {
                    checkedRowsIndices = sourceController.checkedRowsIndices();
                    checkedColsIndices = sourceController.checkedColsIndices();
                    int rowsNumber = checkedRowsIndices.size();
                    int colsNumber = checkedColsIndices.size();
                    data = new double[rowsNumber][colsNumber];
                    allNeg = true;
                    allPos = true;
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
                    zeroBased = zeroCheck.isSelected();
                    bars = calculate();
                    createHtml();
                    return table != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            private double[][] calculate() {
                try {
                    if (data == null || data.length == 0) {
                        return data;
                    }
                    Normalization n = Normalization.create();
                    if (zeroBased) {
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
                    return null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return null;
                }
            }

            protected void createHtml() {
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
                    table = new StringTable(names, title);
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
                                String v;
                                if (zeroBased) {
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
                                htmlRow.add(v);
                            }
                        }
                        table.add(htmlRow);
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }

            @Override
            protected void whenSucceeded() {
                webViewController.loadContents(table.html());
            }

        };
        start(task);
    }

    @FXML
    public void editAction() {
        webViewController.editAction();
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        webViewController.popFunctionsMenu(mouseEvent);
    }

    /*
        static
     */
    public static Data2DColorBarsController open(ControlData2DEditTable tableController) {
        try {
            Data2DColorBarsController controller = (Data2DColorBarsController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DColorBarsFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
