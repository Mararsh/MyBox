package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class Data2DGroupEqualValuesController extends Data2DChartXYController {

    protected List<String> groups, calculationColumns, calculations;
    protected DataFileCSV results;

    @FXML
    protected ControlSelection groupController, calculationController;
    @FXML
    protected ControlData2DResults valuesController;

    public Data2DGroupEqualValuesController() {
        baseTitle = message("GroupEqualValues");
        TipsLabelKey = "GroupEqualValuesTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            noColumnSelection(true);

            groupController.setParameters(this, message("Column"), message("GroupBy"));
            groupController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

            calculationController.setParameters(this, message("Calculation"), message("GroupAggregateComments"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void makeOptions() {
        try {
            if (!data2D.isValid()) {
                groupController.loadNames(null);
                calculationController.loadNames(null);
                return;
            }
            List<String> gnames = new ArrayList<>();
            List<String> cnames = new ArrayList<>();
            cnames.add(message("Count"));
            for (Data2DColumn column : data2D.columns) {
                String name = column.getColumnName();
                gnames.add(name);
                cnames.add(name + "-" + message("Mean"));
                cnames.add(name + "-" + message("Summation"));
                cnames.add(name + "-" + message("Maximum"));
                cnames.add(name + "-" + message("Minimum"));
                cnames.add(name + "-" + message("PopulationVariance"));
                cnames.add(name + "-" + message("SampleVariance"));
                cnames.add(name + "-" + message("PopulationStandardDeviation"));
                cnames.add(name + "-" + message("SampleStandardDeviation"));
            }
            groupController.loadNames(gnames);
            calculationController.loadNames(cnames);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterRefreshControls() {
    }

    @Override
    public boolean initData() {
        try {
            checkObject();

            if (skipNonnumericRadio != null) {
                invalidAs = skipNonnumericRadio.isSelected() ? Double.NaN : 0;
            } else {
                invalidAs = 0;
            }
            groups = groupController.selectedNames();
            if (groups == null || groups.isEmpty()) {
                outOptionsError(message("SelectToHandle") + ": " + message("GroupBy"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            List<String> colsNames = new ArrayList<>();
            colsNames.addAll(groups);
            calculations = calculationController.selectedNames();
            calculationColumns = new ArrayList<>();
            if (calculations != null) {
                for (String name : calculations) {
                    if (name.endsWith("-" + message("Mean"))) {
                        name = name.substring(0, name.length() - ("-" + message("Mean")).length());
                    } else if (name.endsWith("-" + message("Summation"))) {
                        name = name.substring(0, name.length() - ("-" + message("Summation")).length());
                    } else if (name.endsWith("-" + message("Maximum"))) {
                        name = name.substring(0, name.length() - ("-" + message("Maximum")).length());
                    } else if (name.endsWith("-" + message("Minimum"))) {
                        name = name.substring(0, name.length() - ("-" + message("Minimum")).length());
                    } else if (name.endsWith("-" + message("PopulationVariance"))) {
                        name = name.substring(0, name.length() - ("-" + message("PopulationVariance")).length());
                    } else if (name.endsWith("-" + message("SampleVariance"))) {
                        name = name.substring(0, name.length() - ("-" + message("SampleVariance")).length());
                    } else if (name.endsWith("-" + message("PopulationStandardDeviation"))) {
                        name = name.substring(0, name.length() - ("-" + message("PopulationStandardDeviation")).length());
                    } else if (name.endsWith("-" + message("SampleStandardDeviation"))) {
                        name = name.substring(0, name.length() - ("-" + message("SampleStandardDeviation")).length());
                    } else {
                        continue;
                    }
                    calculationColumns.add(name);
                    if (!colsNames.contains(name)) {
                        colsNames.add(name);
                    }
                }
            }
            checkedColsIndices = new ArrayList<>();
            checkedColumns = new ArrayList<>();
            for (String name : colsNames) {
                checkedColsIndices.add(data2D.colOrder(name));
                checkedColumns.add(data2D.columnByName(name));
            }
            checkedColsNames = colsNames;

            return initChart(groups.toString());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        results = null;
        valuesController.loadNull();
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Data2D tmp2D = data2D.cloneAll();
                    List<Data2DColumn> tmpColumns = new ArrayList<>();
                    for (Data2DColumn column : data2D.columns) {
                        Data2DColumn tmpColumn = column.cloneAll();
                        if (calculationColumns.contains(tmpColumn.getColumnName())) {
                            tmpColumn.setType(ColumnDefinition.ColumnType.Double);
                        }
                        tmpColumns.add(tmpColumn);
                    }
                    tmp2D.setColumns(tmpColumns);
                    tmp2D.startTask(task, filterController.filter);
                    DataTable tmpTable;
                    if (isAllPages()) {
                        tmpTable = tmp2D.toTmpTable(task, checkedColsIndices, false, false);
                    } else {
                        outputData = filtered(checkedColsIndices, false);
                        if (outputData == null || outputData.isEmpty()) {
                            error = message("NoData");
                            return false;
                        }
                        tmpTable = tmp2D.toTmpTable(task, checkedColsIndices, outputData, false, false);
                    }
                    tmp2D.stopFilter();
                    if (tmpTable == null) {
                        return false;
                    }
                    results = tmpTable.groupEqualValues(null, task, groups, calculations);
                    tmpTable.drop();
                    return results != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                valuesController.loadDef(results);
                drawChart();
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

    @Override
    public String categoryName() {
        return null;
    }

    @FXML
    @Override
    public void refreshAction() {
        drawChart();
    }

    @Override
    public void drawChart() {
        try {
            if (results == null) {
                return;
            }
//            chartController.writeXYChart(outputColumns, outputData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DGroupEqualValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DGroupEqualValuesController controller = (Data2DGroupEqualValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DGroupEqualValuesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
