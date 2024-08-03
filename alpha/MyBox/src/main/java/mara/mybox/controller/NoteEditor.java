package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class NoteEditor extends BaseDataTreeNodeController {

    @FXML
    protected ControlNoteEditor valueController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            valueController.setParameters(this);
            valuesEditor = valueController;
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
            valueController.updateStatus(false);
        }
    }

    @Override
    protected void editValue(InfoNode node) {
        if (node != null) {
            valueController.loadContents(node.getInfo());
        } else {
            valueController.loadContents(null);
        }
        valueController.updateStatus(false);
    }

    @Override
    protected InfoNode pickValue(InfoNode node) {
        if (node == null) {
            return null;
        }
        return node.setInfo(valueController.currentHtml());
    }

    @Override
    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        valueController.pasteText(node.getInfo());
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (valueController.thisPane.isFocused() || valueController.thisPane.isFocusWithin()) {
            if (valueController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (attributesController.thisPane.isFocused() || attributesController.thisPane.isFocusWithin()) {
            if (attributesController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return valueController.keyEventsFilter(event);
    }

}
