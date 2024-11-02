package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataValues;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class NoteNodeController extends BaseDataTreeNodeController {

    @FXML
    protected ControlNoteEditor valuesController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            valuesEditor = valuesController;

            valuesController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues(DataValues values) {
        try {
            Object v;
            if (values == null) {
                v = null;
            } else {
                v = values.getValue("note");
            }
            if (v != null) {
                valuesController.loadContents((String) v);
            } else {
                valuesController.loadContents(null);
            }
            valuesController.updateStatus(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void valueChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        super.valueChanged(changed);
        if (!changed) {
            valuesController.isSettingValues = true;
            valuesController.updateStatus(false);
            valuesController.isSettingValues = false;
        }
    }

    @Override
    protected DataValues pickNodeValues() {
        return null;
    }

    protected InfoNode pickValue(InfoNode node) {
        if (node == null) {
            return null;
        }
        return node.setInfo(valuesController.currentHtml());
    }

    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        valuesController.pasteText(node.getInfo());
    }

}
