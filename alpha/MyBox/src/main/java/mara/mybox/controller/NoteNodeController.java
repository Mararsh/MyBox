package mara.mybox.controller;

import javafx.fxml.FXML;
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
    public void nodeChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        super.nodeChanged(changed);
        if (!changed) {
            valuesController.updateStatus(false);
        }
    }

    protected void editValue(InfoNode node) {
        if (node != null) {
            valuesController.loadContents(node.getInfo());
        } else {
            valuesController.loadContents(null);
        }
        valuesController.updateStatus(false);
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
