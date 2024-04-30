package mara.mybox.fxml.chart;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-18
 * @License Apache License Version 2.0
 */
public class XYZChart {

    protected String title;
    protected ColorType colorType;
    protected boolean isScatter, darkMode, xCategory, yCategory, zCategory, isOrthographic;
    protected int seriesSize, scale;
    protected double width, height, pointSize;
    protected List<List<String>> data;
    protected List<Data2DColumn> columns;
    protected File chartFile;

    public static enum ColorType {
        Column, Random, Gradient
    }

    public XYZChart() {
        init();
    }

    private void init() {
        title = null;
        colorType = ColorType.Column;
        isScatter = true;
        darkMode = xCategory = yCategory = zCategory = isOrthographic = false;
        seriesSize = 1;
        scale = 8;
        width = 800;
        height = 600;
        pointSize = 10;
        data = null;
        columns = null;
        chartFile = null;
    }

    public static XYZChart create() {
        return new XYZChart();
    }

    public File makeChart() {
        try {
            chartFile = null;
            if (data == null || data.isEmpty()) {
                return null;
            }
            File echartsFile = FxFileTools.getInternalFile("/js/echarts.min.js", "js", "echarts.min.js");
            File echartsGLFile = FxFileTools.getInternalFile("/js/echarts-gl.min.js", "js", "echarts-gl.min.js");
            String xName = columns.get(0).getColumnName();
            String yName = columns.get(1).getColumnName();
            String z1Name = columns.get(2).getColumnName();
            String chartName = isScatter ? message("ScatterChart") : message("SurfaceChart");
            String[] colors = new String[seriesSize];
            String[] symbols = new String[seriesSize];
            String html = "<html>\n"
                    + "  <head>\n"
                    + "    <meta charset=\"utf-8\" />\n"
                    + "    <title>" + title + "</title>\n"
                    + "    <script src=\"" + echartsFile.toURI().toString() + "\"></script>\n"
                    + "    <script src=\"" + echartsGLFile.toURI().toString() + "\"></script>\n"
                    + "  </head>\n"
                    + "  <body style=\"width:" + (width + 50) + "px; margin:0 auto;\">\n"
                    + "    <h3 align=center>" + title + "</h2>\n"
                    + "    <h2 align=center>" + chartName + "</h2>\n";
            if (isScatter) {
                html += "	<style type=\"text/css\">\n";
                String[] presymbols = {"circle", "triangle", "diamond", "arrow", "rect"};
                for (int i = 0; i < seriesSize; i++) {
                    String color;
                    if (colorType == ColorType.Column) {
                        color = FxColorTools.color2css(columns.get(i + 2).getColor());
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
                    html += "		<div class=\"symbol" + i + "\"></div><span> " + columns.get(i + 2).getColumnName() + " </span>\n";
                }
                html += "	</div></P>\n";
            }
            html += "    <div id=\"chart\" style=\"width: " + width + "px;height: " + height + "px;\"></div>\n"
                    + "    <script type=\"text/javascript\">\n"
                    + "		var myChart = echarts.init(document.getElementById('chart')"
                    + (darkMode ? ", 'dark'" : "") + ");\n";
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
                    d = DoubleTools.scale(s, InvalidAs.Skip, scale);
                    if (DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    push += d;
                }
                s = row.get(1);
                if (yCategory) {
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
                    if (zCategory) {
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
            if (darkMode) {
                html += "			backgroundColor: '#ffffff',\n";
            }
            if (colorType == ColorType.Gradient) {
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
            if (isScatter) {
                dimensions = "'" + xName + "', '" + yName + "', '" + z1Name + "'";
            } else {
                dimensions = "'x', 'y', 'z'";  // looks surface ndoes not accpet customized names for xyz. Bug?
            }
            for (int i = 3; i < columns.size(); i++) {
                dimensions += ",'" + columns.get(i).getColumnName() + "'";
            }
            if (isScatter) {
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
                    + "					projection: '" + (isOrthographic ? "orthographic" : "perspective") + "'\n"
                    + "				}\n"
                    + "			},\n"
                    + "			tooltip: {},\n"
                    + "			series: [\n";
            if (isScatter) {
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
                if (colorType == ColorType.Column) {
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

            chartFile = FileTmpTools.generateFile(title, "html");
            TextFileTools.writeFile(chartFile, html, Charset.forName("UTF-8"));
            return chartFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        set
     */
    public XYZChart setTitle(String title) {
        this.title = title;
        return this;
    }

    public XYZChart setColorType(ColorType colorType) {
        this.colorType = colorType;
        return this;
    }

    public XYZChart setIsScatter(boolean isScatter) {
        this.isScatter = isScatter;
        return this;
    }

    public XYZChart setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        return this;
    }

    public XYZChart setxCategory(boolean xCategory) {
        this.xCategory = xCategory;
        return this;
    }

    public XYZChart setyCategory(boolean yCategory) {
        this.yCategory = yCategory;
        return this;
    }

    public XYZChart setzCategory(boolean zCategory) {
        this.zCategory = zCategory;
        return this;
    }

    public XYZChart setIsOrthographic(boolean isOrthographic) {
        this.isOrthographic = isOrthographic;
        return this;
    }

    public XYZChart setSeriesSize(int seriesSize) {
        this.seriesSize = seriesSize;
        return this;
    }

    public XYZChart setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public XYZChart setWidth(double width) {
        this.width = width;
        return this;
    }

    public XYZChart setHeight(double height) {
        this.height = height;
        return this;
    }

    public XYZChart setPointSize(double pointSize) {
        this.pointSize = pointSize;
        return this;
    }

    public XYZChart setData(List<List<String>> data) {
        this.data = data;
        return this;
    }

    public XYZChart setColumns(List<Data2DColumn> columns) {
        this.columns = columns;
        return this;
    }

    /*
        get
     */
    public File getChartFile() {
        return chartFile;
    }

}
