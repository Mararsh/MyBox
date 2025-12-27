package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSource extends BaseData2DRowsColumnsController {

    @Override
    public void updateStatus() {
        try {
            super.updateStatus();

            if (fileMenuButton != null) {
                fileMenuButton.setVisible(data2D != null && data2D.isDataFile() && data2D.getFile() != null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
