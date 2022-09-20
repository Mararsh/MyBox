package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
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
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.tools.TmpFileTools.getPathTempFile;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-13
 * @License Apache License Version 2.0
 */
public class ControlChartXYZ extends BaseController {

    protected double width, height, pointSize;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton scatterRadio, surfaceRadio, perspectiveRadio, orthographicRadio,
            colorGradientRadio, colorColumnsRadio, colorRandomRadio;
    @FXML
    protected ComboBox<String> widthSelector, HeightSelector, pointSelector;
    @FXML
    protected CheckBox darkCheck, wireframeCheck;
    @FXML
    protected FlowPane sizePane;

    public ControlChartXYZ() {
        baseTitle = message("XYZChart");
        TipsLabelKey = "ChartXYZTips";
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
            widthSelector.getItems().addAll(
                    Arrays.asList("800", "1000", "1200", "1500", "2000", "600", "500", "300")
            );
            widthSelector.setValue(width + "");

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
        if (scatterRadio.isSelected()) {
            if (!colorColumnsRadio.isDisabled()) {
                colorColumnsRadio.setSelected(true);
            }
            wireframeCheck.setVisible(false);
            sizePane.setVisible(true);
        } else {
            colorGradientRadio.setSelected(true);
            wireframeCheck.setVisible(true);
            sizePane.setVisible(false);
        }
    }

    public boolean checkParameters() {
        try {
            try {
                double v = Double.valueOf(widthSelector.getValue());
                if (v > 0) {
                    width = v;
                    widthSelector.getEditor().setStyle(null);
                    UserConfig.setDouble(baseName + "Width", width);
                } else {
                    widthSelector.getEditor().setStyle(UserConfig.badStyle());
                    popError(message("InvalidParameter") + ": " + message("Width"));
                    return false;
                }
            } catch (Exception e) {
                widthSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Width"));
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
                    popError(message("InvalidParameter") + ": " + message("Height"));
                    return false;
                }
            } catch (Exception e) {
                HeightSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Height"));
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
                    popError(message("InvalidParameter") + ": " + message("PointSize"));
                    return false;
                }
            } catch (Exception e) {
                pointSelector.getEditor().setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("PointSize"));
                return false;
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public File makeChart(List<Data2DColumn> columns, List<List<String>> data,
            int seriesSize, String dataName, int scale,
            boolean xCategory, boolean yCategory, boolean zCategory) {
        try {
            File chartFile;
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return null;
            }
            File echartsFile = FxFileTools.getInternalFile("/js/echarts.min.js", "js", "echarts.min.js");
            File echartsGLFile = FxFileTools.getInternalFile("/js/echarts-gl.min.js", "js", "echarts-gl.min.js");
            String xName = columns.get(0).getColumnName();
            String yName = columns.get(1).getColumnName();
            String z1Name = columns.get(2).getColumnName();
            String[] colors = new String[seriesSize];
            String[] symbols = new String[seriesSize];
            String chartName = (scatterRadio.isSelected() ? message("ScatterChart") : message("SurfaceChart"));
            String html = "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "  <head>\n"
                    + "    <meta charset=\"utf-8\" />\n"
                    + "    <title>" + dataName + " - " + chartName + "_" + xName + "-" + yName + "</title>\n"
                    + "    <script src=\"" + echartsFile.toURI().toString() + "\"></script>\n"
                    + "    <script src=\"" + echartsGLFile.toURI().toString() + "\"></script>\n"
                    + "  </head>\n"
                    + "  <body style=\"width:" + (width + 50) + "px; margin:0 auto;\">\n"
                    + "    <h3 align=center>" + dataName + " - " + chartName + "</h3>\n";
            if (scatterRadio.isSelected()) {
                html += "    <P>x:" + xName + "  y:" + yName + "</P>\n"
                        + "	<style type=\"text/css\">\n";
                String[] presymbols = {"circle", "triangle", "diamond", "arrow", "rect"};
                for (int i = 0; i < seriesSize; i++) {
                    String color;
                    if (colorColumnsRadio.isSelected()) {
                        color = FxColorTools.color2rgb(columns.get(i + 2).getColor());
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
                html += "    <P><div style=\"display: inline-block;\">z: </div>\n";
                for (int i = 0; i < seriesSize; i++) {
                    html += "		<div class=\"symbol" + i + "\"></div><span> " + columns.get(i + 2).getColumnName() + " </span>\n";
                }
                html += "	</P>\n";
            } else {
                html += "    <P>x:" + xName + "  y:" + yName + "  z:" + z1Name + "</P>\n";
            }
            html += "    <div id=\"chart\" style=\"width: " + width + "px;height: " + height + "px;\"></div>\n"
                    + "    <script type=\"text/javascript\">\n"
                    + "		var myChart = echarts.init(document.getElementById('chart')"
                    + (darkCheck.isSelected() ? ", 'dark'" : "") + ");\n";
            html += "		var srcData = [];\n";
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            String s;
            double d;
            for (List<String> row : data) {
                int rowLen = row.size();
                if (rowLen < 3) {
                    continue;
                }
                String push = "		srcData.push([";
                s = row.get(0);
                if (xCategory) {
                    push += "'" + s + "'";
                } else {
                    d = DoubleTools.scale(s, Data2D_Attributes.InvalidAs.Skip, scale);
                    if (DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    push += d;
                }
                s = row.get(1);
                if (yCategory) {
                    push += ", '" + s + "'";
                } else {
                    d = DoubleTools.scale(s, Data2D_Attributes.InvalidAs.Skip, scale);
                    if (DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    push += ", " + d;
                }
                for (int i = 2; i < 2 + seriesSize; i++) {
                    s = row.get(i);
                    if (zCategory) {
                        push += ", '" + s + "'";
                    } else {
                        d = DoubleTools.scale(s, Data2D_Attributes.InvalidAs.Skip, scale);
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
            for (int i = 3; i < columns.size(); i++) {
                dimensions += ",'" + columns.get(i).getColumnName() + "'";
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
                    + "				type: '" + (xCategory ? "category" : "value") + "',\n"
                    + "				name: 'X: " + xName + "'\n"
                    + "			},\n" + "			yAxis3D: {\n"
                    + "				type: '" + (yCategory ? "category" : "value") + "',\n"
                    + "				name: 'Y: " + yName + "'\n"
                    + "			},\n"
                    + "			zAxis3D: {\n"
                    + "				type: '" + (zCategory ? "category" : "value") + "',\n"
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
                    Data2DColumn column = columns.get(i + 2);
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
                Data2DColumn column = columns.get(2);
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
                        + "					itemStyle: {\n"
                        + "						color: '" + color + "'\n"
                        + "					},\n"
                        + "					wireframe: {\n"
                        + "						show: " + wireframeCheck.isSelected() + "\n"
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

            chartFile = getPathTempFile(AppPaths.getGeneratedPath(), dataName + "_" + chartName, ".html");
            TextFileTools.writeFile(chartFile, html, Charset.forName("UTF-8"));
            return chartFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
