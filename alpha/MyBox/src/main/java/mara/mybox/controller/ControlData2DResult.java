package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DResult extends ControlData2DSource {

    public ControlData2DResult() {
    }

    @Override
    public void setParameters(Data2DOperateController opController) {
        try {
            this.opController = opController;
            this.data2D = opController.data2D;
            this.tableController = opController.tableController;
            tableData2DDefinition = tableController.tableData2DDefinition;
            tableData2DColumn = tableController.tableData2DColumn;

            setControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
