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
    protected String parameterName, parameterValue, orderby;

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

            playController.frameStartNodify.addListener(new ChangeListener<Boolean>() {
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
            if (!super.initData()) {
                return false;
            }

            groupData = null;
            framesNumber = -1;
            parameterName = null;
            parameterValue = null;
            groupid = -1;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean initChart() {
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
                            outputNames, orders, maxData, scale);
                    group.run();
                    framesNumber = (int) group.groupNumber();
                    groupData = group.getTargetData();
                    parameterName = group.getParameterName();
                    orderby = group.orderByString();
                    return framesNumber > 0 && groupData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                task = null;
                if (ok) {
                    groupDataController.loadData(groupData.cloneAll());
                    initChart(false);
                    drawXYChart();
                }
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
                return;
            }
            makeIndices();
            playController.play(framesNumber, 0, framesNumber - 1);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized void displayFrame(int index) {
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
                        + " WHERE " + message("Group") + "=" + groupid + orderby;
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
                    MyBoxLog.console(e.toString());
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                backgroundTask = null;
                if (ok) {
                    chartMaker.setDefaultChartTitle(chartTitle());
                    chartController.writeXYChart(outputColumns, outputData,
                            categoryIndex, valueIndices);
                }
            }

        };
        start(backgroundTask, false);
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
