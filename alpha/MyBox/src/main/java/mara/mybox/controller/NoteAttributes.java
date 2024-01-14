package mara.mybox.controller;

import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class NoteAttributes extends InfoTreeNodeEditor {

    protected NoteEditor editorController;

    public void setEditor(NoteEditor editorController) {
        try {
            this.editorController = editorController;
            tabPane = editorController.tabPane;
            attributesTab = editorController.attributesTab;
            setManager(editorController.notesController);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editInfo(InfoNode node) {
        if (node != null) {
            editorController.loadContents(node.getInfo());
        } else {
            editorController.loadContents(null);
        }
        editorController.updateFileStatus(false);
    }

    @Override
    protected String nodeInfo() {
        return editorController.currentHtml(true);
    }

    @Override
    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        editorController.pasteText(node.getInfo());
    }

}
