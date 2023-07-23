package mara.mybox.controller;

import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class NoteAttributes extends TreeNodeEditor {

    protected NoteEditor editorController;

    public void setEditor(NoteEditor editorController) {
        try {
            this.editorController = editorController;
            tabPane = editorController.tabPane;
            attributesTab = editorController.attributesTab;

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
        editorController.attributesChanged();
    }

    @Override
    public InfoNode pickNodeData() {
        InfoNode node = super.pickNodeData();
        if (node == null) {
            return null;
        }
        node.setValue(editorController.currentHtml(true));
        return node;
    }

    @Override
    public void pasteText(String text) {
        editorController.pasteText(text);
    }

}
