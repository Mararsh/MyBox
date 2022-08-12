package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-5-10
 * @License Apache License Version 2.0
 */
public class ControlData2DResults extends ControlData2DLoad {

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

    @FXML
    @Override
    public void editAction() {
        if (data2D == null) {
            return;
        }
        if (data2D.getFile() != null) {
            DataFileCSVController.open(data2D);
        } else {
            DataFileCSVController.open(data2D.dataName(), data2D.getColumns(), data2D.tableData());
        }
    }

}
