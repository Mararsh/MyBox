package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public abstract class BaseFileController extends BaseController {

    @FXML
    protected ControlFileBrowse browseController;
    @FXML
    protected Label fileInfoLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (browseController != null) {
                browseController.setParameter(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
