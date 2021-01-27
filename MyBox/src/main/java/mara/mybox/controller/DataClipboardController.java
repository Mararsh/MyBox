package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-1-16
 * @License Apache License Version 2.0
 */
public class DataClipboardController extends BaseController {

    @FXML
    protected ControlDataTextController dataController;

    public DataClipboardController() {
        baseTitle = message("DataClipboard");
        TipsLabelKey = "DataInputComments";
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            dataController.delimiter = AppVariables.getUserConfigValue(baseName + "Delimiter", ",");
            dataController.setControls(baseName, false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
