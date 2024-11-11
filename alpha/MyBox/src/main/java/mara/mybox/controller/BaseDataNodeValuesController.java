package mara.mybox.controller;

import mara.mybox.db.data.DataValues;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-11-9
 * @License Apache License Version 2.0
 */
public abstract class BaseDataNodeValuesController extends BaseController {

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

    public void valueChanged(boolean changed) {
        if (isSettingValues || nodeEditor == null) {
            return;
        }
        this.changed = changed;
        nodeEditor.updateStatus();
    }

    protected DataValues copyValues() {
        try {
            currentValues = pickNodeValues();
            currentValues.setValue(dataTable.getIdColumnName(), (long) -1);
            return currentValues;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
