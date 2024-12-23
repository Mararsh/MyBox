package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class ControlDataText extends BaseDataValuesController {

    @FXML
    protected TextArea textInput;
    @FXML
    protected CheckBox wrapCheck;

    @Override
    public void initEditor() {
        try {
            valueInput = textInput;
            valueWrapCheck = wrapCheck;
            valueName = "text";
            super.initEditor();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
