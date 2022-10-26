package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-14
 * @License Apache License Version 2.0
 */
public class Data2DChartGroupXYController extends Data2DChartXYController {

    protected DataTable groupData;
    protected int framesNumber, groupid;
    protected List<String> copyNames, sorts;
    protected String parameterName, parameterValue;

    @FXML
    protected ControlData2DResults groupDataController;
    @FXML
    protected ControlPlay playController;

    public Data2DChartGroupXYController() {
        baseTitle = message("GroupData") + " - " + message("XYChart");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            playController.setParameters(this);

            playController.frameNodify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    displayFrame(playController.currentIndex);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void changeChartAsType() {
        playController.clear();
        if (chartTypesController.needChangeData()) {
            chartController.clearChart();
        } else {
            initChart(false);
            drawXYChart();
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!groupController.pickValues()) {
                return false;
            }
            checkObject();
            checkInvalidAs();

            dataColsIndices = new ArrayList<>();
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            int categoryCol = data2D.colOrder(selectedCategory);
            if (categoryCol < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.add(categoryCol);
            if (chartTypesController.isBubbleChart()) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
                int valueCol = data2D.colOrder(selectedValue);
                if (valueCol < 0) {
                    outOptionsError(message("SelectToHandle") + ": " + message("Column"));
                    tabPane.getSelectionModel().select(optionsTab);
                    return false;
                }
                dataColsIndices.add(valueCol);
            }
            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                outOptionsError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.addAll(checkedColsIndices);
            copyNames = new ArrayList<>();
            for (int i : dataColsIndices) {
                copyNames.add(data2D.columnName(i));
            }

            groupData = null;
            framesNumber = -1;
            parameterName = null;
            parameterValue = null;
            sorts = sortController.selectedNames();
            groupid = -1;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean initChart() {
        chartController.palette = null;
        return initChart(false);
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        playController.clear();
        groupDataController.loadNull();
        task = new SingletonTask<Void>(this) {

            private DataTableGroup group;

            @Override
            protected boolean handle() {
                try {
                    group = groupData(DataTableGroup.TargetType.Table,
                            copyNames, sorts, maxData, scale);
                    group.run();
                    framesNumber = (int) group.groupNumber();
                    groupData = group.getTargetData();
                    parameterName = group.getParameterName();
                    return framesNumber > 0 && groupData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    if (groupData == null) {
                        popError(message("NoData"));
                        return;
                    }
                    groupDataController.loadData(groupData);
                    outputColumns = new ArrayList<>();
                    for (int i : dataColsIndices) {
                        outputColumns.add(data2D.column(i));
                    }
                    chartController.palette = null;
                    initChart(false);
                    drawXYChart();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                task = null;
            }

        };
        start(task);
    }

    @Override
    public String chartTitle() {
        return message("Group") + groupid + " - " + parameterValue + "\n"
                + super.chartTitle();
    }

    @Override
    public String categoryName() {
        return selectedCategory;
    }

    @Override
    public void drawXYChart() {
        try {
            if (groupData == null || framesNumber <= 0) {
                popError(message("NoData"));
                return;
            }
            playController.play(framesNumber, 0, framesNumber - 1);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void displayFrame(int index) {
        if (groupData == null || framesNumber <= 0 || index < 0 || index > framesNumber) {
            playController.clear();
            return;
        }
        groupid = index + 1;
        parameterValue = null;
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        backgroundTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                outputData = new ArrayList<>();
                String sql = "SELECT * FROM " + groupData.getSheet()
                        + " WHERE " + message("Group") + "=" + groupid + orderBy();
                try ( Connection conn = DerbyBase.getConnection();
                         ResultSet query = conn.prepareStatement(sql).executeQuery()) {
                    while (query.next() && backgroundTask != null && !backgroundTask.isCancelled()) {
                        if (parameterValue == null) {
                            parameterValue = query.getString(2);
                        }
                        List<String> row = new ArrayList<>();
                        for (Data2DColumn column : outputColumns) {
                            String name = column.getColumnName();
                            String gname = groupData.tmpColumnName(name);
                            String s = column.toString(query.getObject(gname));
                            if (s != null && column.needScale() && scale >= 0) {
                                s = DoubleTools.scaleString(s, invalidAs, scale);
                            }
                            row.add(s);
                        }
                        outputData.add(row);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenFailed() {
                playController.pauseAction();
                if (!isCancelled()) {
                    return;
                }
                super.whenFailed();
            }

            @Override
            protected void whenSucceeded() {
                chartMaker.setDefaultChartTitle(chartTitle());
                chartController.writeXYChart(outputColumns, outputData, null, false);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                backgroundTask = null;
            }

        };
        start(backgroundTask, false);
    }

    protected String orderBy() {
        if (groupData == null) {
            return null;
        }
        String orderBy = null;
        if (sorts != null && !sorts.isEmpty()) {
            int desclen = ("-" + message("Descending")).length();
            int asclen = ("-" + message("Ascending")).length();
            String name, stype;
            for (String sort : sorts) {
                if (sort.endsWith("-" + message("Descending"))) {
                    name = sort.substring(0, sort.length() - desclen);
                    stype = " DESC";
                } else if (sort.endsWith("-" + message("Ascending"))) {
                    name = sort.substring(0, sort.length() - asclen);
                    stype = " ASC";
                } else {
                    continue;
                }
                name = groupData.tmpColumnName(name);
                if (orderBy == null) {
                    orderBy = name + stype;
                } else {
                    orderBy += ", " + name + stype;
                }
            }
        }
        return orderBy == null ? "" : " ORDER BY " + orderBy;
    }

    /*
        static
     */
    public static Data2DChartGroupXYController open(ControlData2DLoad tableController) {
        try {
            Data2DChartGroupXYController controller = (Data2DChartGroupXYController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartGroupXYFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
