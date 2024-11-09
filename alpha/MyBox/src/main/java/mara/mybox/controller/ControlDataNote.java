package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataValues;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class ControlDataNote extends BaseDataNodeValues {

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
    protected void editValues(DataValues values) {
        try {
            Object v;
            if (values == null) {
                v = null;
            } else {
                v = values.getValue("note");
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
    protected DataValues pickNodeValues() {
        return null;
    }

}
