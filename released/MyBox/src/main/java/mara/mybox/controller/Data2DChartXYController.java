package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.XYChartMaker;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public class Data2DChartXYController extends BaseData2DChartController {

    protected XYChartMaker chartMaker;
    protected Data2DColumn categoryColumn;
    protected int categoryIndex;
    protected List<Integer> valueIndices;

    @FXML
    protected ControlChartXYSelection chartTypesController;
    @FXML
    protected CheckBox xyReverseCheck;
    @FXML
    protected VBox columnsBox, columnCheckBoxsBox;
    @FXML
    protected Label valuesLabel;
    @FXML
    protected FlowPane valueColumnPane, categoryColumnsPane;
    @FXML
    protected ControlData2DChartXY chartController;

    public Data2DChartXYController() {
        baseTitle = message("XYChart");
        TipsLabelKey = "DataChartXYTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            chartMaker = chartController.chartMaker;
            chartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawChart();
                }
            });

            if (xyReverseCheck != null) {
                xyReverseCheck.setSelected(!chartMaker.isIsXY());
                xyReverseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        initChart();
                        drawChart();
                    }
                });
            }

            typeChanged();
            chartTypesController.typeNodify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    typeChanged();
                }
            });

            chartTypesController.thisPane.disableProperty().bind(chartController.buttonsPane.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void typeChanged() {
        try {
            if (columnsBox == null) {
                return;
            }
            columnsBox.getChildren().clear();

            if (chartTypesController.isBubbleChart()) {
                columnsBox.getChildren().addAll(categoryColumnsPane, valueColumnPane, columnCheckBoxsBox);
                valuesLabel.setText(message("SizeColumns"));

            } else {
                columnsBox.getChildren().addAll(categoryColumnsPane, columnCheckBoxsBox);
                valuesLabel.setText(message("ValueColumns"));
            }

            changeChartAsType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void changeChartAsType() {
        if (categoryColumn == null || outputColumns == null || outputColumns.isEmpty()) {
            return;
        }
        if (chartTypesController.needChangeData()) {
            refreshAction();
        } else {
            initChart();
            drawXYChart();
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            dataColsIndices = new ArrayList<>();

            int categoryCol = data2D.colOrder(selectedCategory);
            if (categoryCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            categoryColumn = data2D.column(categoryCol);
            dataColsIndices.add(categoryCol);

            categoryIndex = showRowNumber() ? 1 : 0;

            valueIndices = new ArrayList<>();
            if (chartTypesController.isBubbleChart()) {
                int valueCol = data2D.colOrder(selectedValue);
                if (valueCol < 0) {
                    outOptionsError(message("SelectToHandle") + ": " + message("Column"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
                int pos = dataColsIndices.indexOf(valueCol);
                if (pos >= 0) {
                    valueIndices.add(pos + categoryIndex);
                } else {
                    valueIndices.add(dataColsIndices.size() + categoryIndex);
                    dataColsIndices.add(valueCol);
                }
            }

            for (int col : checkedColsIndices) {
                int pos = dataColsIndices.indexOf(col);
                if (pos >= 0) {
                    valueIndices.add(pos + categoryIndex);
                } else {
                    valueIndices.add(dataColsIndices.size() + categoryIndex);
                    dataColsIndices.add(col);
                }
            }

            return initChart();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public String chartTitle() {
        String title = selectedCategory;
        if (chartTypesController.isBubbleChart()) {
            title += " - " + selectedValue;
        }
        title += " - " + checkedColsNames;
        return title;
    }

    public boolean initChart() {
        if (categoryColumn != null) {
            return initChart(categoryColumn.isNumberType());
        } else {
            return false;
        }
    }

    public boolean initChart(boolean categoryIsNumbers) {
        try {
            String chartName = chartTypesController.chartName;
            UserConfig.setBoolean(chartName + "CategoryIsNumbers", categoryIsNumbers);
            chartMaker.init(chartTypesController.chartType, chartName)
                    .setDefaultChartTitle(chartTitle())
                    .setDefaultCategoryLabel(selectedCategory)
                    .setInvalidAs(invalidAs);
            chartMaker.setIsXY(!xyReverseCheck.isSelected());
            if (chartTypesController.isBubbleChart()) {
                chartMaker.setDefaultValueLabel(selectedValue);
            } else if (checkedColsNames != null) {
                chartMaker.setDefaultValueLabel(checkedColsNames.toString());
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void drawChart() {
        drawXYChart();
    }

    public void drawXYChart() {
        chartData = chartMax();
        if (chartData == null || chartData.isEmpty()) {
            return;
        }
        chartController.writeXYChart(outputColumns, chartData, categoryIndex, valueIndices);
    }

    /*
        static
     */
    public static Data2DChartXYController open(ControlData2DLoad tableController) {
        try {
            Data2DChartXYController controller = (Data2DChartXYController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartXYFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
