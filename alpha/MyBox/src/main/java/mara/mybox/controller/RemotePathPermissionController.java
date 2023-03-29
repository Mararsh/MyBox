package mara.mybox.controller;

import com.jcraft.jsch.SftpATTRS;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class RemotePathPermissionController extends RemotePathHandleFilesController {

    public RemotePathPermissionController() {
        baseTitle = message("RemotePathPermission");
        doneString = message("Changed");
    }

    @Override
    public void setParameters(RemotePathManageController manageController) {
        try {
            super.setParameters(manageController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @Override
    public boolean checkParameters() {
        try {
            if (!super.checkParameters()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean handleFile(String srcfile) {
        try {
            SftpATTRS attrs = manageController.remoteController.stat(srcfile);
            if (attrs == null) {
                return false;
            }
//            if (attrs.isDir()) {
//                return downDirectory(srcfile, targetPath);
//            } else {
//                return downFile(srcfile, targetPath);
//            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    /*
        static methods
     */
    public static RemotePathPermissionController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathPermissionController controller = (RemotePathPermissionController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathPermissionFxml, false);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
