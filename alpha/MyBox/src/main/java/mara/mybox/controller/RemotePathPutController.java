package mara.mybox.controller;

import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.tools.FileTools.showFileSize;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class RemotePathPutController extends BaseBatchFileController {

    protected RemotePathManageController manageController;
    protected String targetPathName;
    protected long srcLen;

    @FXML
    protected TextField targetPathInput;
    @FXML
    protected Label hostLabel;

    public RemotePathPutController() {
        baseTitle = message("RemotePathPut");
    }

    public void setParameters(RemotePathManageController manageController) {
        try {
            this.manageController = manageController;
            logsTextArea = manageController.logsTextArea;
            logsMaxChars = manageController.logsMaxChars;
            verboseCheck = manageController.verboseCheck;

            TreeItem<FileNode> item = manageController.filesTreeView.getSelectionModel().getSelectedItem();
            if (item == null) {
                item = manageController.filesTreeView.getRoot();
            }
            if (item != null && item.getValue() != null) {
                targetPathName = item.getValue().path(false);
                targetPathInput.setText(targetPathName);
                targetPathInput.selectEnd();
            }

            hostLabel.setText(message("Host") + ": " + manageController.remoteController.host());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        targetPathName = targetPathInput.getText();
        if (targetPathName == null || targetPathName.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("TargetPath"));
            return false;
        }

        if (manageController.task != null) {
            manageController.task.cancel();
        }
        manageController.tabPane.getSelectionModel().select(manageController.logsTab);
        manageController.requestMouse();
        return super.makeMoreParameters();
    }

    @FXML
    @Override
    public void startAction() {
        runTask();
    }

    @Override
    public void startTask() {
        super.startAction();
    }

    @Override
    public boolean beforeHandleFiles() {
        manageController.task = task;
        manageController.remoteController.task = task;
        return manageController.checkConnection() && checkDirectory(targetPathName);
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
            return handleFileWithName(file, targetPathName);
        } catch (Exception e) {
            return file + " " + e.toString();
        }
    }

    @Override
    public String handleFileWithName(File srcFile, String targetPath) {
        try {
            String targetName = makeTargetFilename(srcFile, targetPath);
            if (targetName == null) {
                return message("Skip");
            }
            targetName = manageController.remoteController.fixFilename(targetName);
            srcLen = srcFile.length();
            showLogs("put " + srcFile.getAbsolutePath() + " " + targetName);
            manageController.remoteController.sftp.put(srcFile.getAbsolutePath(), targetName, new PutMonitor());
            showLogs(MessageFormat.format(message("FilesGenerated"), targetName));
            return message("Successful");
        } catch (Exception e) {
            showLogs(e.toString());
            return null;
        }
    }

    @Override
    public String handleDirectory(File dir) {
        try {
            dirFilesNumber = dirFilesHandled = 0;
            handleDirectory(dir, targetPathName);
            return MessageFormat.format(message("DirHandledSummary"), dirFilesNumber, dirFilesHandled);
        } catch (Exception e) {
            showLogs(e.toString());
            return message("Failed");
        }
    }

    @Override
    public boolean checkDirectory(String pathname) {
        try {
            if (pathname == null) {
                return false;
            }
            return manageController.remoteController.mkdirs(pathname);
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public void donePost() {
        tableView.refresh();
        if (miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (manageController == null) {
            return;
        }
        manageController.loadPath();
    }

    private class PutMonitor implements SftpProgressMonitor {

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
    public static RemotePathPutController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathPutController controller = (RemotePathPutController) WindowTools.openChildStage(
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
