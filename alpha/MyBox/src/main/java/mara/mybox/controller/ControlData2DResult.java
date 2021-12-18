package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DResult extends ControlData2DLoad {

    protected Data2DOperateController opController;

    public ControlData2DResult() {
        forDisplay = true;
    }

    public void setParameters(Data2DOperateController opController) {
        try {
            this.opController = opController;
            setData(opController.data2D.cloneAll());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void tableChanged(boolean changed) {
        validateData();
    }

    @Override
    public void updateStatus() {
        if (dataSizeLabel != null) {
            dataSizeLabel.setText(message("Rows") + ": "
                    + (tableData == null ? 0 : tableData.size())
                    + (dataSize > 0 ? "/" + dataSize : ""));
        }
    }

}
