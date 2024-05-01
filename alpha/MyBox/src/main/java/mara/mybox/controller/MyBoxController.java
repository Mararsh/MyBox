package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public class MyBoxController extends MyBoxController_About {

    @FXML
    protected Label titleLabel;

    public MyBoxController() {
        baseTitle = message("AppTitle") + " v" + AppValues.AppVersion;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            titleLabel.setText(baseTitle);
            titleLabel.requestFocus();

//            if (scheduledTasks != null && !scheduledTasks.isEmpty()) {
//                bottomLabel.setText(MessageFormat.format(message("AlarmClocksRunning"), scheduledTasks.size()));
//            }
//            if (DerbyBase.isStarted() && !SystemConfig.getBoolean("MyBoxWarningDisplayed", false)) {
//                alertInformation(message("MyBoxWarning"));
//                SystemConfig.setBoolean("MyBoxWarningDisplayed", true);
//            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
