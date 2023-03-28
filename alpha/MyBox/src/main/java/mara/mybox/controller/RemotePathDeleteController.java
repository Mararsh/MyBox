package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-27
 * @License Apache License Version 2.0
 */
public class RemotePathDeleteController extends RemotePathHandleFilesController {

    public RemotePathDeleteController() {
        baseTitle = message("RemotePathDelete");
    }

    @Override
    public boolean handleFile(String name) {
        return manageController.remoteController.delete(name);
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
                    manageController.getMyWindow(), Fxmls.RemotePathDeleteFxml, false);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
