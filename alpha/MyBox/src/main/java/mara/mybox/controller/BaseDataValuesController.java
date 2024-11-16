package mara.mybox.controller;

import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-11-9
 * @License Apache License Version 2.0
 */
public abstract class BaseDataValuesController extends BaseController {

    protected ControlDataNodeEditor nodeEditor;
    protected BaseNodeTable nodeTable;
    protected boolean changed;

    protected abstract void editValues();

    protected abstract DataNode pickValues(DataNode node);

    public void setParameters(ControlDataNodeEditor controller) {
        try {
            this.nodeEditor = controller;
            this.parentController = nodeEditor;
            this.baseName = nodeEditor.baseName;
            nodeTable = nodeEditor.nodeTable;

            initEditor();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initEditor() {

    }

    public void valueChanged(boolean changed) {
        if (isSettingValues || nodeEditor == null) {
            return;
        }
        this.changed = changed;
        nodeEditor.updateStatus();
    }

}
