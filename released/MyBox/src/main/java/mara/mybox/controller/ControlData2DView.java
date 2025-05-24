package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class ControlData2DView extends BaseData2DViewController {

    @Override
    public void initValues() {
        try {
            super.initValues();

            refreshTitle = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
