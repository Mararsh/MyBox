package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-11-4
 * @License Apache License Version 2.0
 */
public class TextFindBatchController extends FindBatchController {

    @FXML
    protected TextFindBatchOptions textFindOptionsController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            optionsController = textFindOptionsController;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
