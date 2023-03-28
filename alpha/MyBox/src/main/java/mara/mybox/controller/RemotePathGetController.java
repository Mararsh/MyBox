package mara.mybox.controller;

import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.tools.FileTools.showFileSize;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-27
 * @License Apache License Version 2.0
 */
public class RemotePathGetController extends RemotePathHandleFilesController {

    protected File targetPath;
    protected long srcLen;

    @FXML
    protected ControlPathInput targetPathInputController;

    public RemotePathGetController() {
        baseTitle = message("RemotePathGet");
    }

    @Override
    public void setParameters(RemotePathManageController manageController) {
        try {
            super.setParameters(manageController);

            targetPathInputController.baseName(baseName).init();

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
            targetPath = targetPathInputController.file();
            return targetPath != null && targetPath.exists();
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean handleFile(String name) {
        try {
            String targetName = "";
            if (targetName == null) {
                return false;
            }

            targetName = manageController.remoteController.fixFilename(targetName);
//            srcLen = srcFile.length();
            showLogs("get " + name + " " + targetName);
            manageController.remoteController.sftp.get(name, targetName, new GetMonitor());
            showLogs(MessageFormat.format(message("FilesGenerated"), targetName));
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

//    public String makeTargetFilename(File srcFile, String targetPath) {
//        if (srcFile == null || targetPath == null) {
//            return null;
//        }
//        try {
//            if (srcFile.isDirectory()) {
//                return makeTargetFilename(srcFile.getName(), "", targetPath);
//            } else {
//                String filename = srcFile.getName();
//                String namePrefix = FileNameTools.prefix(filename);
//                String nameSuffix;
//                if (targetFileSuffix != null) {
//                    nameSuffix = "." + targetFileSuffix;
//                } else {
//                    nameSuffix = FileNameTools.suffix(filename);
//                    if (nameSuffix != null && !nameSuffix.isEmpty()) {
//                        nameSuffix = "." + nameSuffix;
//                    } else {
//                        nameSuffix = "";
//                    }
//                }
//                return makeTargetFilename(namePrefix, nameSuffix, targetPath);
//            }
//        } catch (Exception e) {
//            return null;
//        }
//    }
    private class GetMonitor implements SftpProgressMonitor {

        private long len = 0;

        @Override
        public boolean count(long count) {
            len += count;
            if (manageController.verboseCheck.isSelected() && len % 500 == 0) {
                updateLogs(message("Status") + ": "
                        + FloatTools.percentage(len, srcLen) + "%   "
                        + showFileSize(len) + "/" + showFileSize(srcLen));
            }
            return true;
        }

        @Override
        public void end() {
        }

        @Override
        public void init(int op, String src, String dest, long max) {
        }
    }


    /*
        static methods
     */
    public static RemotePathGetController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathGetController controller = (RemotePathGetController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathGetFxml, false);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
