package mara.mybox.controller;

import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DMatrix extends BaseData2DRowsColumnsController {

    protected DataMatrix dataMatrix;

    @Override
    public void initControls() {
        try {
            super.initControls();

            createData(Data2D.DataType.Matrix);
            dataMatrix = (DataMatrix) data2D;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
