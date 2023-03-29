package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
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

    protected long srcLen;

    @FXML
    protected ControlPathInput targetPathInputController;

    public RemotePathGetController() {
        baseTitle = message("RemotePathGet");
        doneString = message("Downloaded");
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
    public boolean handleFile(String srcfile) {
        try {
            SftpATTRS attrs = manageController.remoteController.stat(srcfile);
            if (attrs == null) {
                return false;
            }
            srcLen = attrs.getSize();
            if (attrs.isDir()) {
                return downDirectory(srcfile, targetPath);
            } else {
                return downFile(srcfile, targetPath);
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean downFile(String srcfile, File path) {
        try {
            File target = new File(path + File.separator + new File(srcfile).getName());
            target.getParentFile().mkdirs();
            String tname = target.getAbsolutePath();
            showLogs("get " + srcfile + " " + tname);
            manageController.remoteController.sftp.get(srcfile, tname, new GetMonitor());
            showLogs(MessageFormat.format(message("FilesGenerated"), target));
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean downDirectory(String srcfile, File path) {
        try {
            File target = new File(path + File.separator + new File(srcfile).getName());
            target.getParentFile().mkdirs();
            Iterator<LsEntry> iterator = manageController.remoteController.ls(srcfile);
            if (iterator == null) {
                return false;
            }
            boolean ok;
            while (iterator.hasNext()) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                LsEntry entry = iterator.next();
                String child = entry.getFilename();
                if (child == null || child.isBlank()
                        || ".".equals(child) || "..".equals(child)) {
                    continue;
                }
                child = srcfile + "/" + child;
                SftpATTRS attrs = entry.getAttrs();
                srcLen = attrs.getSize();
                if (attrs.isDir()) {
                    ok = downDirectory(child, target);
                } else {
                    ok = downFile(child, target);
                }
                if (!ok && !continueCheck.isSelected()) {
                    if (task != null) {
                        task.cancel();
                    }
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    private class GetMonitor implements SftpProgressMonitor {

        private long len = 0;

        @Override
        public boolean count(long count) {
            len += count;
            if (manageController.verboseCheck.isSelected() && len % 500 == 0) {
                if (srcLen > 0) {
                    updateLogs(message("Status") + ": "
                            + FloatTools.percentage(len, srcLen) + "%   "
                            + showFileSize(len) + "/" + showFileSize(srcLen));
                } else {
                    updateLogs(message("Status") + ": " + showFileSize(len));
                }
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

    @FXML
    @Override
    public void openPath() {
        File path = targetPathInputController.file();
        browseURI(path.toURI());
        recordFileOpened(path);
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
