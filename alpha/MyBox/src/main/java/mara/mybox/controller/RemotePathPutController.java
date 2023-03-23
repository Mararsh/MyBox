package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class RemotePathPutController extends BaseBatchFileController {

    protected RemotePathManageController manageController;
    protected String rootPathName, separator;

    @FXML
    protected TextField targetPathInput;
    @FXML
    protected Label hostLabel;

    public RemotePathPutController() {
        baseTitle = message("RemotePathPut");
    }

    public void setParameters(RemotePathManageController manageController, String pathName) {
        try {
            this.manageController = manageController;
            this.rootPathName = pathName;
            separator = "/";

            targetPathInput.setText(rootPathName);
            hostLabel.setText(manageController.remoteController.host());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        targetPath = null;
        actualParameters.targetRootPath = null;
        actualParameters.targetPath = null;
        return super.makeMoreParameters();
    }

    @FXML
    @Override
    public void startAction() {
        start();
    }

    @Override
    public void startTask() {
        super.startAction();
    }

    @Override
    public String handleFile(File file) {
        try {
            if (task == null || task.isCancelled()) {
                return message("Canceled");
            }
            if (file == null || !file.isFile() || !match(file)) {
                return message("Skip" + ": " + file);
            }
            return handleFile2(file, rootPathName);
        } catch (Exception e) {
            return file + " " + e.toString();
        }
    }

    @Override
    public String handleFile2(File srcFile, String targetPath) {
        try {
            String targetName = makeTargetFilename(srcFile, targetPath);
            if (targetName == null) {
                return message("Skip");
            }
            updateLogs(MessageFormat.format(message("FilesGenerated"), targetName));
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public String handleDirectory(File dir) {
        try {
            dirFilesNumber = dirFilesHandled = 0;
            handleDirectory(dir, rootPathName);
            return MessageFormat.format(message("DirHandledSummary"), dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return message("Failed");
        }
    }

    @Override
    public boolean checkDirectory(String pathname) {
        return pathname != null;
    }

    /*
        static methods
     */
    public static RemotePathPutController open(RemotePathManageController manageController, String pathName) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathPutController controller = (RemotePathPutController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathPutFxml, false);
            controller.setParameters(manageController, pathName);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
