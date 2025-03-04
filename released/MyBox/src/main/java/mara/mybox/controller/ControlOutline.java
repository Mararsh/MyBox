package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class ControlOutline extends ControlImage {

    @FXML
    protected CheckBox keepRatioCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            keepRatioCheck.setSelected(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
