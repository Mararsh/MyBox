package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartXYZController extends BaseData2DHandleController {

    protected XYChartMaker chartMaker;
    protected List<Integer> dataColsIndices;
    protected int seriesSize;
    protected double width, height, pointSize;
    protected File chartFile;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected VBox zBox, columnsBox;
    @FXML
    protected FlowPane zColumnPane, zlabelPane;
    @FXML
    protected RadioButton scatterRadio, surfaceRadio, perspectiveRadio, orthographicRadio,
            colorGradientRadio, colorColumnsRadio, colorRandomRadio;
    @FXML
    protected ComboBox<String> xSelector, ySelector, zSelector, wdithSelector, HeightSelector, pointSelector;
    @FXML
    protected CheckBox xCategoryCheck, yCategoryCheck, zCategoryCheck, darkCheck;

    public Data2DChartXYZController() {
        baseTitle = message("XYZChart");
        TipsLabelKey = "DataChartXYZTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    typeChanged();
                }
            });
            typeChanged();

            width = UserConfig.getDouble(baseName + "Width", 800);
            if (width < 0) {
                width = 800;
            }
            wdithSelector.getItems().addAll(
                    Arrays.asList("800", "1000", "1200", "1500", "2000", "600", "500", "300")
            );
            wdithSelector.setValue(width + "");

            height = UserConfig.getDouble(baseName + "Height", 600);
            if (height < 0) {
                height = 600;
            }
            HeightSelector.getItems().addAll(
                    Arrays.asList("600", "800", "1000", "1200", "1500", "500", "300")
            );
            HeightSelector.setValue(height + "");

            pointSize = UserConfig.getDouble(baseName + "PointSize", 10);
            if (pointSize < 0) {
                pointSize = 10;
            }
            pointSelector.getItems().addAll(
                    Arrays.asList("10", "12", "15", "20", "8", "5", "3", "2", "1")
            );
            pointSelector.setValue(pointSize + "");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void typeChanged() {
        zBox.getChildren().clear();
        if (scatterRadio.isSelected()) {
            if (!zlabelPane.getChildren().contains(zCategoryCheck)) {
                zlabelPane.getChildren().add(zCategoryCheck);
            }
            zBox.getChildren().add(columnsBox);
            colorColumnsRadio.setSelected(true);
        } else {
            if (!zColumnPane.getChildren().contains(zCategoryCheck)) {
                zColumnPane.getChildren().add(zCategoryCheck);
            }
            zBox.getChildren().add(zColumnPane);
            colorGradientRadio.setSelected(true);
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            List<String> names = data2D.columnNames();
            if (names == null || names.isEmpty()) {
                xSelector.getItems().clear();
                ySelector.getItems().clear();
                zSelector.getItems().clear();
                return;
            }
            String xCol = xSelector.getSelectionModel().getSelectedItem();
            xSelector.getItems().setAll(names);
            if (xCol != null && names.contains(xCol)) {
                xSelector.setValue(xCol);
            } else {
                xSelector.getSelectionModel().select(0);
            }
            String yCol = ySelector.getSelectionModel().getSelectedItem();
            ySelector.getItems().setAll(names);
            if (yCol != null && names.contains(yCol)) {
                ySelector.setValue(yCol);
            } else {
                ySelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
            }
            String zCol = zSelector.getSelectionModel().getSelectedItem();
            zSelector.getItems().setAll(names);
            if (zCol != null && names.contains(zCol)) {
                zSelector.setValue(zCol);
            } else {
                zSelector.getSelectionModel().select(names.size() > 2 ? 2 : 0);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            invalidAs = InvalidAs.Skip;
            try {
                double v = Double.valueOf(wdithSelector.getValue());
                if (v > 0) {
                    width = v;
                    wdithSelector.getEditor().setStyle(null);
                    UserConfig.setDouble(baseName + "Width", width);
                } else {
                    wdithSelector.getEditor().setStyle(UserConfig.badStyle());
                    outOptionsError(message("InvalidParameter") + ": " + message("Width"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
            } catch (Exception e) {
                wdithSelector.getEditor().setStyle(UserConfig.badStyle());
                outOptionsError(message("InvalidParameter") + ": " + message("Width"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            try {
                double v = Double.valueOf(HeightSelector.getValue());
                if (v > 0) {
                    height = v;
                    HeightSelector.getEditor().setStyle(null);
                    UserConfig.setDouble(baseName + "Height", height);
                } else {
                    HeightSelector.getEditor().setStyle(UserConfig.badStyle());
                    outOptionsError(message("InvalidParameter") + ": " + message("Height"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
            } catch (Exception e) {
                HeightSelector.getEditor().setStyle(UserConfig.badStyle());
                outOptionsError(message("InvalidParameter") + ": " + message("Height"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            try {
                double v = Double.valueOf(pointSelector.getValue());
                if (v > 0) {
                    pointSize = v;
                    pointSelector.getEditor().setStyle(null);
                    UserConfig.setDouble(baseName + "PointSize", pointSize);
                } else {
                    pointSelector.getEditor().setStyle(UserConfig.badStyle());
                    outOptionsError(message("InvalidParameter") + ": " + message("PointSize"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
            } catch (Exception e) {
                pointSelector.getEditor().setStyle(UserConfig.badStyle());
                outOptionsError(message("InvalidParameter") + ": " + message("PointSize"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }

            dataColsIndices = new ArrayList<>();
            outputColumns = new ArrayList<>();
            String xName = xSelector.getSelectionModel().getSelectedItem();
            int xCol = data2D.colOrder(xName);
            if (xCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("AxisX"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            outputColumns.add(data2D.column(xCol));
            dataColsIndices.add(xCol);

            String yName = ySelector.getSelectionModel().getSelectedItem();
            int yCol = data2D.colOrder(yName);
            if (yCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("AxisY"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            outputColumns.add(data2D.column(yCol));
            dataColsIndices.add(yCol);

            if (scatterRadio.isSelected()) {
                if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                    outOptionsError(message("SelectToHandle") + ": " + message("AxisZ"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
                dataColsIndices.addAll(checkedColsIndices);
                outputColumns.addAll(checkedColumns);
                seriesSize = checkedColsIndices.size();
            } else {
                String zName = zSelector.getSelectionModel().getSelectedItem();
                int zCol = data2D.colOrder(zName);
                if (zCol < 0) {
                    outOptionsError(message("SelectToHandle") + ": " + message("AxisZ"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
                outputColumns.add(data2D.column(zCol));
                dataColsIndices.add(zCol);
                seriesSize = 1;
            }
            if (otherColsIndices != null) {
                for (int c : otherColsIndices) {
                    if (!dataColsIndices.contains(c)) {
                        dataColsIndices.add(c);
                        outputColumns.add(data2D.column(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    makeChart();
                    data2D.stopFilter();
                    return chartFile != null && chartFile.exists();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                browse(chartFile);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    public void makeChart() {
        try {
            chartFile = null;
            if (isAllPages()) {
                outputData = data2D.allRows(dataColsIndices, false);
            } else {
                outputData = filtered(dataColsIndices, false);
            }
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            File echartsFile = FxFileTools.getInternalFile("/js/echarts.min.js", "js", "echarts.min.js");
            File echartsGLFile = FxFileTools.getInternalFile("/js/echarts-gl.min.js", "js", "echarts-gl.min.js");
            String xName = outputColumns.get(0).getColumnName();
            String yName = outputColumns.get(1).getColumnName();
            String z1Name = outputColumns.get(2).getColumnName();
            String title = data2D.shortName() + "_"
                    + (scatterRadio.isSelected() ? message("ScatterChart") : message("SurfaceChart"))
                    + "_" + xName + "-" + yName;
            String[] colors = new String[seriesSize];
            String[] symbols = new String[seriesSize];
            String html = "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "  <head>\n"
                    + "    <meta charset=\"utf-8\" />\n"
                    + "    <title>" + title + "</title>\n"
                    + "    <script src=\"" + echartsFile.toURI().toString() + "\"></script>\n"
                    + "    <script src=\"" + echartsGLFile.toURI().toString() + "\"></script>\n"
                    + "  </head>\n"
                    + "  <body style=\"width:" + (width + 50) + "px; margin:0 auto;\">\n";
            if (scatterRadio.isSelected()) {
                html += "	<style type=\"text/css\">\n";
                String[] presymbols = {"circle", "triangle", "diamond", "arrow", "rect"};
                for (int i = 0; i < seriesSize; i++) {
                    String color;
                    if (colorColumnsRadio.isSelected()) {
                        color = FxColorTools.color2rgb(outputColumns.get(i + 2).getColor());
                    } else {
                        color = FxColorTools.randomRGB();
                    }
                    colors[i] = color;
                    String symbol = presymbols[i % presymbols.length];
                    symbols[i] = symbol;
                    if ("circle".equals(symbol)) {
                        html += "		.symbol" + i + " {\n"
                                + "			width: 20px;\n"
                                + "			height: 20px;\n"
                                + "			background-color: " + color + ";\n"
                                + "			border-radius: 50%;\n"
                                + "			display: inline-block;\n"
                                + "		}\n";
                    } else if ("rect".equals(symbol)) {
                        html += "		.symbol" + i + " {\n"
                                + "			width: 20px;\n"
                                + "			height: 20px;\n"
                                + "			background-color: " + color + ";\n"
                                + "			display: inline-block;\n"
                                + "		}\n";
                    } else if ("triangle".equals(symbol)) {
                        html += "		.symbol" + i + " {\n"
                                + "			width: 0;\n"
                                + "			height: 0;\n"
                                + "			border-left: 10px solid transparent;\n"
                                + "			border-right: 10px solid transparent;\n"
                                + "			border-bottom: 20px solid " + color + ";\n"
                                + "			display: inline-block;\n"
                                + "		}\n";
                    } else if ("diamond".equals(symbol)) {
                        html += "		.symbol" + i + " {\n"
                                + "			width: 16px;   \n"
                                + "			height: 16px;   \n"
                                + "			background-color: " + color + ";   \n"
                                + "			transform:rotate(45deg);   \n"
                                + "			display: inline-block;\n"
                                + "		}\n";
                    } else if ("arrow".equals(symbol)) {
                        html += "		.symbol" + i + " {\n"
                                + "			width: 0;\n"
                                + "			height: 0;\n"
                                + "			border: 10px solid;\n"
                                + "			border-color: white white " + color + " white;\n"
                                + "			display: inline-block;\n"
                                + "		}\n";
                    }
                }
                html += "	</style>\n";
                html += "    <P><div style=\"text-align: center; margin:0 auto;\">\n";
                for (int i = 0; i < seriesSize; i++) {
                    html += "		<div class=\"symbol" + i + "\"></div><span>" + outputColumns.get(i + 2).getColumnName() + " </span>\n";
                }
                html += "	</div></P>\n";
            }
            html += "    <div id=\"chart\" style=\"width: " + width + "px;height: " + height + "px;\"></div>\n"
                    + "    <script type=\"text/javascript\">\n"
                    + "		var myChart = echarts.init(document.getElementById('chart')"
                    + (darkCheck.isSelected() ? ", 'dark'" : "") + ");\n";
            html += "		var srcData = [];\n";
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            String s;
            double d;
            for (List<String> row : outputData) {
                int rowLen = row.size();
                if (rowLen < 3) {
                    continue;
                }
                String push = "		srcData.push([";
                s = row.get(0);
                if (xCategoryCheck.isSelected()) {
                    push += "'" + s + "'";
                } else {
                    d = DoubleTools.scale(s, InvalidAs.Skip, scale);
                    if (DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    push += d;
                }
                s = row.get(1);
                if (yCategoryCheck.isSelected()) {
                    push += ", '" + s + "'";
                } else {
                    d = DoubleTools.scale(s, InvalidAs.Skip, scale);
                    if (DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    push += ", " + d;
                }
                for (int i = 2; i < 2 + seriesSize; i++) {
                    s = row.get(i);
                    if (zCategoryCheck.isSelected()) {
                        push += ", '" + s + "'";
                    } else {
                        d = DoubleTools.scale(s, InvalidAs.Skip, scale);
                        if (DoubleTools.invalidDouble(d)) {
                            push += ", Number.NaN";
                        } else {
                            push += ", " + d;
                            if (d > max) {
                                max = d;
                            }
                            if (d < min) {
                                min = d;
                            }
                        }
                    }
                }
                for (int i = 2 + seriesSize; i < rowLen; i++) {
                    push += ", '" + row.get(i) + "'";
                }
                html += push + "]);\n";
            }
            if (min == Double.MAX_VALUE) {
                min = -10;
            }
            if (max == -Double.MAX_VALUE) {
                max = 10;
            }
            html += "\n\n		option = {\n";
            if (!darkCheck.isSelected()) {
                html += "			backgroundColor: '#ffffff',\n";
            }
            if (colorGradientRadio.isSelected()) {
                html += "			visualMap: {\n"
                        + "				show: true,\n"
                        + "				dimension: 2,\n"
                        + "				min: " + min + ",\n"
                        + "				max: " + max + ",\n"
                        + "				inRange: {\n"
                        + "				  color: [\n"
                        + "					'#313695',\n"
                        + "					'#4575b4',\n"
                        + "					'#74add1',\n"
                        + "					'#abd9e9',\n"
                        + "					'#e0f3f8',\n"
                        + "					'#ffffbf',\n"
                        + "					'#fee090',\n"
                        + "					'#fdae61',\n"
                        + "					'#f46d43',\n"
                        + "					'#d73027',\n"
                        + "					'#a50026'\n"
                        + "				  ]\n"
                        + "				}\n"
                        + "			},\n";
            }
            String dimensions;
            if (scatterRadio.isSelected()) {
                dimensions = "'" + xName + "', '" + yName + "', '" + z1Name + "'";
            } else {
                dimensions = "'x', 'y', 'z'";  // looks surface ndoes not accpet customized names for xyz. Bug?
            }
            for (int i = 3; i < outputColumns.size(); i++) {
                dimensions += ",'" + outputColumns.get(i).getColumnName() + "'";
            }
            if (scatterRadio.isSelected()) {
                html += "			dataset: {\n"
                        + "				dimensions: [\n"
                        + "				" + dimensions + "\n"
                        + "				],\n"
                        + "				source: srcData\n"
                        + "			},\n";
            }
            html += "			xAxis3D: {\n"
                    + "				type: '" + (xCategoryCheck.isSelected() ? "category" : "value") + "',\n"
                    + "				name: 'X: " + xName + "'\n"
                    + "			},\n" + "			yAxis3D: {\n"
                    + "				type: '" + (yCategoryCheck.isSelected() ? "category" : "value") + "',\n"
                    + "				name: 'Y: " + yName + "'\n"
                    + "			},\n"
                    + "			zAxis3D: {\n"
                    + "				type: '" + (zCategoryCheck.isSelected() ? "category" : "value") + "',\n"
                    + "				name: 'Z" + (seriesSize == 1 ? ": " + z1Name : "") + "'\n"
                    + "			},\n"
                    + "			grid3D: {\n"
                    + "				viewControl: {\n"
                    + "					projection: '" + (perspectiveRadio.isSelected() ? "perspective" : "orthographic") + "'\n"
                    + "				}\n"
                    + "			},\n"
                    + "			tooltip: {},\n"
                    + "			series: [\n";
            if (scatterRadio.isSelected()) {
                for (int i = 0; i < seriesSize; i++) {
                    if (i > 0) {
                        html += "				,\n";
                    }
                    Data2DColumn column = outputColumns.get(i + 2);
                    String zName = column.getColumnName();
                    html += "				{\n"
                            + "					type: 'scatter3D',\n"
                            + "					name: '" + zName + "',\n"
                            + "					symbol: '" + symbols[i] + "',\n"
                            + "					symbolSize: " + pointSize + ",\n"
                            + "					itemStyle: {\n"
                            + "						color: '" + colors[i] + "'\n"
                            + "					},\n"
                            + "					encode: {\n"
                            + "						x: '" + xName + "',\n"
                            + "						y: '" + yName + "',\n"
                            + "						z: '" + zName + "'\n"
                            + "					}\n"
                            + "				}\n";
                }
            } else {
                Data2DColumn column = outputColumns.get(2);
                String zName = column.getColumnName();
                String color;
                if (colorColumnsRadio.isSelected()) {
                    color = FxColorTools.color2rgb(column.getColor());
                } else {
                    color = FxColorTools.randomRGB();
                }
                html += "				{\n"
                        + "					type: 'surface',\n"
                        + "					name: '" + zName + "',\n"
                        + "					symbolSize: " + pointSize + ",\n"
                        + "					itemStyle: {\n"
                        + "						color: '" + color + "'\n"
                        + "					},\n"
                        + "					wireframe: {\n"
                        + "						show: true\n"
                        + "					},\n"
                        + "					dimensions: [" + dimensions + "],\n"
                        + "					data: srcData\n"
                        + "				}\n";
            }
            html += "			]\n"
                    + "		}\n\n"
                    + "		myChart.setOption(option);\n"
                    + "    </script>\n"
                    + "  </body>\n"
                    + "</html>";

            chartFile = getPathTempFile(AppPaths.getGeneratedPath(), title, ".html");
            TextFileTools.writeFile(chartFile, html, Charset.forName("UTF-8"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static
     */
    public static Data2DChartXYZController open(ControlData2DEditTable tableController) {
        try {
            Data2DChartXYZController controller = (Data2DChartXYZController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartXYZFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
