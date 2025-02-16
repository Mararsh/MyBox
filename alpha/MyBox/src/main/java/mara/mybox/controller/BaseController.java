package mara.mybox.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController_Attributes.StageType;
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

            stageType = StageType.Normal;

            AppVariables.AlarmClockController = null;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParent(BaseController parent, StageType stageType) {
        this.parentController = parent;
        this.stageType = stageType;
        myStage = getMyStage();
        if (stageType == null || myStage == null) {
            return;
        }
        switch (stageType) {
            case Branch: {
                setAlwaysTop(true, false);
                try {
                    Stage parentStage = parent.getMyStage();
                    parentStage.setOnHiding(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            closeStage();
                        }
                    });
                    if (AppVariables.branchWindowIconifyParent) {
                        parentStage.setIconified(true);
                    }
                } catch (Exception e) {
                }
                break;
            }
            case Pop: {
                setAlwaysTop(true, false);
                try {
                    parent.getMyStage().setFullScreen(false);
                } catch (Exception e) {
                }
                break;
            }
        }
    }

}
