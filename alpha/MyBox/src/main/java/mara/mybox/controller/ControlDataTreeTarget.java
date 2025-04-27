package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-2-18
 * @License Apache License Version 2.0
 */
public class ControlDataTreeTarget extends BaseDataTreeController {

    public void setParameters(BaseDataTreeController parent) {
        try {
            initDataTree(parent.nodeTable, null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
