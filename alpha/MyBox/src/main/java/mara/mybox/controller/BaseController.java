package mara.mybox.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 *
 * BaseController < BaseController_MouseEvents < BaseController_KeyEvents <
 * BaseController_Actions < BaseController_Interface < BaseController_Files <
 * BaseController_Attributes
 */
public abstract class BaseController extends BaseController_MouseEvents implements Initializable {

    public BaseController() {
        baseTitle = Languages.message("AppTitle");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            interfaceName = NodeTools.getFxmlName(url);
            baseName = interfaceName;
            myFxml = "/fxml/" + interfaceName + ".fxml";

            initValues();
            monitorKeyEvents();
//            monitorMouseEvents();
            initBaseControls();
            initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initValues() {
        try {
            setFileType();

            myController = this;
            if (thisPane != null) {
                thisPane.setUserData(myController);
            }
            if (mainMenuController != null) {
                mainMenuController.parentFxml = myFxml;
                mainMenuController.parentController = this;
            }
            AppVariables.alarmClockController = null;
            isPop = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
