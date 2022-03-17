package mara.mybox.controller;

import mara.mybox.db.data.TreeLeaf;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class NoteAttributes extends TreeLeafEditor {

    protected NoteEditor editorController;

    public void setEditor(NoteEditor editorController) {
        try {
            this.editorController = editorController;
            tabPane = editorController.tabPane;
            attributesTab = editorController.attributesTab;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public TreeLeaf pickCurrentLeaf() {
        TreeLeaf leaf = super.pickCurrentLeaf();
        if (leaf == null) {
            return null;
        }
        leaf.setValue(editorController.currentHtml(true));
        return leaf;
    }

}
