package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    public RemotePathSynchronizeFromLocalController() {
        baseTitle = message("RemotePathSynchronizeFromLocal");
    }

    @Override
    public void initTarget() {
        try {
            remoteController.setParameters(this);

            optionsController.copyAttrCheck.setSelected(false);
            optionsController.copyAttrCheck.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected boolean checkTarget() {
        return remoteController.pickProfile();
    }

    @Override
    public boolean doTask() {
        try {
            return remoteController.connect(task)
                    && synchronize(remoteController.currentConnection.getPath());
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    @Override
    public void afterTask() {
        remoteController.disconnect();
    }

    @Override
    public void cancelTask() {
        super.cancelTask();
        remoteController.disconnect();
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
            remoteController.deleteFile(targetNode.fullName());
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
        if (targetNode != null) {
            return remoteController.copyFile(sourceFile, targetNode.fullName());
        } else {
            return false;
        }
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
