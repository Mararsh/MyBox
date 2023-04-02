package mara.mybox.controller;

import com.jcraft.jsch.SftpATTRS;
import java.io.File;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class RemotePathSynchronizeFromLocalController extends DirectorySynchronizeController {

    @FXML
    protected ControlRemoteConnection remoteController;
    @FXML
    protected VBox sourceBox, remoteBox;

    public RemotePathSynchronizeFromLocalController() {
        baseTitle = message("RemotePathSynchronizeFromLocal");
    }

    @Override
    public void initTarget() {
        try {
            remoteController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected boolean checkTarget() {
        return remoteController.pickProfile();
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        sourceBox.setDisable(true);
        remoteBox.setDisable(true);
    }

    @Override
    public boolean doTask() {
        try {
            return remoteController.connect(task)
                    && synchronize(remoteController.currentConnection.getPath());
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterTask() {
        super.afterTask();
        remoteController.disconnect();
        sourceBox.setDisable(false);
        remoteBox.setDisable(false);
    }

    @Override
    public FileNode targetNode(String targetName) {
        return remoteController.FileNode(targetName);
    }

    @Override
    public List<FileNode> targetChildren(FileNode targetNode) {
        return remoteController.children(targetNode);
    }

    @Override
    public void deleteTargetFile(FileNode targetNode) {
        if (targetNode != null) {
            remoteController.delete(targetNode.fullName());
        }
    }

    @Override
    public void targetMkdirs(FileNode targetNode) {
        if (targetNode != null) {
            remoteController.mkdirs(targetNode.fullName());
        }
    }

    @Override
    public boolean copyFile(File sourceFile, FileNode targetNode) {
        try {
            if (task == null || task.isCancelled()
                    || targetNode == null || sourceFile == null
                    || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            String sourceName = sourceFile.getAbsolutePath();
            String targetName = remoteController.fixFilename(targetNode.fullName());
            showLogs("put " + sourceName + " " + targetName);
            remoteController.sftp.put(sourceName, targetName);
            if (copyAttr.isCopyMTime() || copyAttr.isSetPermissions()) {
                SftpATTRS attrs = remoteController.stat(targetName);
                String msg = "setStat: ";
                if (copyAttr.isSetPermissions()) {
                    attrs.setPERMISSIONS(copyAttr.getPermissions());
                    msg += copyAttr.getPermissions();
                }
                if (copyAttr.isCopyMTime()) {
                    int time = (int) (sourceFile.lastModified() / 1000);
                    attrs.setACMODTIME(time, time);
                    msg += "  " + new Date(sourceFile.lastModified());
                }
                showLogs(msg);
                remoteController.sftp.setStat(targetName, attrs);
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean isModified(File srcFile, FileNode targetNode) {
        int stime = (int) (srcFile.lastModified() / 1000);
        int ttime = (int) (targetNode.getModifyTime() / 1000);
        return stime > ttime;
    }

    @FXML
    @Override
    public void openTarget() {
        RemotePathManageController.open(remoteController.currentConnection);
    }

}
