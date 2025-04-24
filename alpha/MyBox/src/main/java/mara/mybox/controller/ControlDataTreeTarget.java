package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-2-18
 * @License Apache License Version 2.0
 */
public class ControlDataTreeTarget extends BaseDataTreeViewController {

    public void setParameters(ControlTreeView parent) {
        try {
            initDataTree(parent.nodeTable);

            loadTree();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
