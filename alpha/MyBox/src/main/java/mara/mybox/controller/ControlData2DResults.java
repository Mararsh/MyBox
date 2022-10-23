package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;

/**
 * @Author Mara
 * @CreateDate 2022-5-10
 * @License Apache License Version 2.0
 */
public class ControlData2DResults extends ControlData2DLoad {

    protected boolean noDataRowNumber = false;

    public ControlData2DResults() {
        statusNotify = new SimpleBooleanProperty(false);
        readOnly = true;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            setData(Data2D.create(Data2D.Type.CSV));
            notUpdateTitle = true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setNoRowNumber() {
        noDataRowNumber = true;
        dataRowColumn.setVisible(false);
    }

    public void loadData(List<Data2DColumn> cols, List<List<String>> data) {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV targetCSV;

            @Override
            protected boolean handle() {
                try {
                    targetCSV = DataFileCSV.save(null, task, ",", cols, data);
                    return targetCSV != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                data2D = targetCSV;
                loadDef(data2D);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                task = null;
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void editAction() {
        if (data2D == null) {
            return;
        }
        if (data2D.isCSV()) {
            if (data2D.getFile() != null) {
                DataFileCSVController.open(data2D);
            } else {
                DataFileCSVController.open(data2D.dataName(), data2D.getColumns(), data2D.tableRows(false));
            }
        } else {
            Data2D.open(data2D);
        }
    }

}
