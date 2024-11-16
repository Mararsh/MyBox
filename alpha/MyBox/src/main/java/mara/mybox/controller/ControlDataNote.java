package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class ControlDataNote extends BaseDataValuesController {

    @FXML
    protected ControlNoteEditor noteController;

    @Override
    public void initEditor() {
        try {
            super.initControls();

            noteController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            Object v;
            if (nodeEditor.currentNode == null) {
                v = null;
            } else {
                v = nodeEditor.currentNode.getValue("note");
            }
            if (v != null) {
                noteController.loadContents((String) v);
            } else {
                noteController.loadContents(null);
            }
            noteController.updateStatus(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        try {

            node.setValue("title", nodeEditor.titleInput.getText());
            node.setValue("note", noteController.currentHtml());
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
