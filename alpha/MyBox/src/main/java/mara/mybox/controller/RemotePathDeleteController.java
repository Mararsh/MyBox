package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-27
 * @License Apache License Version 2.0
 */
public class RemotePathDeleteController extends BaseTaskController {

    protected RemotePathManageController manageController;
    protected String separator;
    protected long srcLen;

    @FXML
    protected TextArea namesArea;
    @FXML
    protected Label hostLabel;

    public RemotePathDeleteController() {
        baseTitle = message("RemotePathDelete");
    }

    public void setParameters(RemotePathManageController manageController) {
        try {
            this.manageController = manageController;
            logsTextArea = manageController.logsTextArea;
            logsMaxChars = manageController.logsMaxChars;
            verboseCheck = manageController.verboseCheck;

            separator = "/";

            hostLabel.setText(message("Host") + ": " + manageController.remoteController.host());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    /*
        static methods
     */
    public static RemotePathDeleteController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathDeleteController controller = (RemotePathDeleteController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathPutFxml, false);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
