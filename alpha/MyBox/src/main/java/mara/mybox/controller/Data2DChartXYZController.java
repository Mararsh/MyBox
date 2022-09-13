package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
    protected Data2DColumn xColumn, yColumn, zColumn;
    protected List<Integer> dataColsIndices;
    protected String title, xName, yName, zName;
    protected double width, height, pointSize;
    protected File chartFile;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton scatterRadio, surfaceRadio, perspectiveRadio, orthographicRadio;
    @FXML
    protected ComboBox<String> xSelector, ySelector, zSelector, wdithSelector, HeightSelector, pointSelector;
    @FXML
    protected CheckBox xCategoryCheck, yCategoryCheck, zCategoryCheck, gradientCheck;

    public Data2DChartXYZController() {
        baseTitle = message("XYZChart");
        TipsLabelKey = "DataChartXYZTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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

            gradientCheck.setSelected(UserConfig.getBoolean(baseName + "Gradient", true));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            UserConfig.setBoolean(baseName + "Gradient", gradientCheck.isSelected());
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
            xName = xSelector.getSelectionModel().getSelectedItem();
            int xCol = data2D.colOrder(xName);
            if (xCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("AxisX"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            xColumn = data2D.column(xCol);
            dataColsIndices.add(xCol);

            yName = ySelector.getSelectionModel().getSelectedItem();
            int yCol = data2D.colOrder(yName);
            if (yCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("AxisY"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            yColumn = data2D.column(yCol);
            dataColsIndices.add(yCol);

            zName = zSelector.getSelectionModel().getSelectedItem();
            int zCol = data2D.colOrder(zName);
            if (zCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("AxisZ"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            zColumn = data2D.column(zCol);
            dataColsIndices.add(zCol);

            title = xName + " - " + yName + " - " + zName;

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
            String html = "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "  <head>\n"
                    + "    <meta charset=\"utf-8\" />\n"
                    + "    <title>" + title + "</title>\n"
                    + "    <script src=\"" + echartsFile.toURI().toString() + "\"></script>\n"
                    + "    <script src=\"" + echartsGLFile.toURI().toString() + "\"></script>\n"
                    + "  </head>\n"
                    + "  <body>\n"
                    + "    <div id=\"chart\" style=\"width: " + width + "px;height: " + height + "px;\"></div>\n"
                    + "    <script type=\"text/javascript\">\n"
                    + "      var myChart = echarts.init(document.getElementById('chart'));\n";
            html += "		var srcData = [];\n";
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (List<String> row : outputData) {
                int size = row.size();
                if (size < 3) {
                    continue;
                }
                double x = DoubleTools.scale(row.get(0), InvalidAs.Skip, scale);
                if (DoubleTools.invalidDouble(x)) {
                    continue;
                }
                double y = DoubleTools.scale(row.get(1), InvalidAs.Skip, scale);
                if (DoubleTools.invalidDouble(x)) {
                    continue;
                }
                double z = DoubleTools.scale(row.get(2), InvalidAs.Skip, scale);
                if (DoubleTools.invalidDouble(z)) {
                    continue;
                }
                if (z > max) {
                    max = z;
                }
                if (z < min) {
                    min = z;
                }
                html += "				srcData.push([" + x + ", " + y + ", " + z + "]);\n";
            }
            html += "\n\n		option = {\n"
                    + "			backgroundColor: '#ffffff',\n"
                    + "			darkMode: true,\n";
            if (gradientCheck.isSelected()) {
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
            html += "			xAxis3D: {\n"
                    + "				type: '" + (xCategoryCheck.isSelected() ? "category" : "value") + "',\n"
                    + "				name: 'X: " + xName + "'\n"
                    + "			},\n"
                    + "			yAxis3D: {\n"
                    + "				type: '" + (yCategoryCheck.isSelected() ? "category" : "value") + "',\n"
                    + "				name: 'Y: " + yName + "'\n"
                    + "			},\n"
                    + "			zAxis3D: {\n"
                    + "				type: '" + (zCategoryCheck.isSelected() ? "category" : "value") + "',\n"
                    + "				name: 'Z: " + zName + "'\n"
                    + "			},\n"
                    + "			grid3D: {\n"
                    + "				viewControl: {\n"
                    + "					projection: '" + (perspectiveRadio.isSelected() ? "perspective" : "orthographic") + "'\n"
                    + "				}\n"
                    + "			},\n"
                    + "			tooltip: {},\n"
                    + "			series: [{\n"
                    + "				type: '" + (scatterRadio.isSelected() ? "scatter3D" : "surface") + "',\n"
                    + "				symbolSize: " + pointSize + ",\n"
                    + "				wireframe: {\n"
                    + "					show: true\n"
                    + "				},\n"
                    + "				itemStyle: {\n"
                    + "					color: '" + FxColorTools.randomRGB() + "'\n"
                    + "				},\n"
                    + "				data: srcData\n"
                    + "			}]\n"
                    + "		}\n\n";
            html += "		myChart.setOption(option);\n"
                    + "    </script>\n"
                    + "  </body>\n"
                    + "</html>";

            chartFile = getPathTempFile(AppPaths.getGeneratedPath(), ".html");
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
