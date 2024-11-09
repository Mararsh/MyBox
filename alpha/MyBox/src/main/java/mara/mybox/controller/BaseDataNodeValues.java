package mara.mybox.controller;

import mara.mybox.db.data.DataValues;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-11-9
 * @License Apache License Version 2.0
 */
public abstract class BaseDataNodeValues extends BaseController {

    protected ControlDataNodeEditor nodeEditor;
    protected BaseTable dataTable;
    protected DataValues currentValues;
    protected boolean changed;

    protected abstract void editValues(DataValues values);

    protected abstract DataValues pickNodeValues();

    public void setParameters(ControlDataNodeEditor controller) {
        try {
            this.nodeEditor = controller;
            this.parentController = nodeEditor;
            this.baseName = nodeEditor.baseName;
            dataTable = nodeEditor.dataTable;
            initEditor();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initEditor() {

    }

}
