package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
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
            MyBoxLog.debug(e);
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
    public boolean doTask(FxTask currentTask) {
        try {
            return remoteController.connect(currentTask)
                    && synchronize(currentTask, remoteController.currentConnection.getPath());
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterTask(boolean ok) {
        super.afterTask(ok);
        remoteController.disconnect();
        sourceBox.setDisable(false);
        remoteBox.setDisable(false);
    }

    @Override
    public FileNode targetNode(String targetName) {
        return remoteController.FileNode(targetName);
    }

    @Override
    public List<FileNode> targetChildren(FxTask currentTask, FileNode targetNode) {
        return remoteController.children(currentTask, targetNode);
    }

    @Override
    public void deleteTargetFile(FxTask currentTask, FileNode targetNode) {
        if (targetNode != null) {
            remoteController.delete(currentTask, targetNode.nodeFullName());
        }
    }

    @Override
    public void targetMkdirs(File srcFile, FileNode targetNode) {
        if (targetNode != null) {
            remoteController.mkdirs(targetNode.nodeFullName(),
                    copyAttr.isCopyMTime() && srcFile != null ? (int) (srcFile.lastModified() / 1000) : -1,
                    copyAttr.getPermissions());
        }
    }

    @Override
    public boolean copyFile(FxTask currentTask, File sourceFile, FileNode targetNode) {
        try {
            if (targetNode == null) {
                return false;
            }
            return remoteController.put(currentTask, sourceFile, targetNode.nodeFullName(),
                    copyAttr.isCopyMTime(), copyAttr.getPermissions());
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
