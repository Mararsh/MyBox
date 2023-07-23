package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.chart.ChartOptions.LabelType;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-17
 * @License Apache License Version 2.0
 */
public class MathFunctionXYChartController extends ControlData2DChartXY {

    protected String title;

    @FXML
    protected ToggleGroup chartGroup;
    @FXML
    protected RadioButton scatterChartRadio;

    public MathFunctionXYChartController() {
        baseTitle = message("XYChart");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            chartGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (scatterChartRadio.isSelected()) {
                        drawScatterChart();
                    } else {
                        drawLineChart();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(List<Data2DColumn> columns, List<List<String>> data, String title) {
        try {
            this.columns = columns;
            this.data = data;
            this.title = title;
            setTitle(baseTitle + " - " + title);
            drawScatterChart();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void drawScatterChart() {
        try {
            if (columns == null || columns.size() < 2) {
                return;
            }
            String chartName = message("ScatterChart");
            UserConfig.setBoolean(chartName + "CategoryIsNumbers", true);
            chartMaker.init(ChartType.Scatter, chartName)
                    .setLabelType(LabelType.Point)
                    .setDefaultChartTitle(title)
                    .setDefaultCategoryLabel(columns.get(0).getColumnName())
                    .setDefaultValueLabel(columns.get(1).getColumnName())
                    .setInvalidAs(InvalidAs.Skip);
            chartMaker.setIsXY(true);
            writeXYChart(columns, data);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void drawLineChart() {
        try {
            if (columns == null || columns.size() < 2) {
                return;
            }
            String chartName = message("LineChart");
            UserConfig.setBoolean(chartName + "CategoryIsNumbers", true);
            chartMaker.init(ChartType.Line, chartName)
                    .setLabelType(LabelType.NotDisplay)
                    .setDefaultChartTitle(title)
                    .setDefaultCategoryLabel(columns.get(0).getColumnName())
                    .setDefaultValueLabel(columns.get(1).getColumnName())
                    .setInvalidAs(InvalidAs.Skip);
            chartMaker.setIsXY(true);
            writeXYChart(columns, data);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void redraw() {
        writeXYChart(columns, data);
    }

    /*
        static
     */
    public static MathFunctionXYChartController open(List<Data2DColumn> columns, List<List<String>> data, String title) {
        try {
            MathFunctionXYChartController controller = (MathFunctionXYChartController) WindowTools.openStage(Fxmls.MathFunctionXYChartFxml);
            controller.setParameters(columns, data, title);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
