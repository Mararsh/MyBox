package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class RemotePathSynchronizeFromLocalController extends DirectorySynchronizeController {

    @FXML
    protected ControlRemotePath remoteController;

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
    public String targetName(String targetName) {
        return remoteController.fixFilename(targetName);
    }

    @Override
    public boolean targetExist(String targetName) {
        return remoteController.fileExist(targetName);
    }

    @Override
    public List<String> targetChildren(String targetName) {
        return remoteController.fileChildren(targetName);
    }

    @Override
    public boolean isTargetDirectory(String targetName) {
        return remoteController.isDirectory(targetName);
    }

    @Override
    public long targetFileLength(String targetName) {
        return remoteController.fileLength(targetName);
    }

    @Override
    public long targetFileModifyTime(String targetName) {
        return remoteController.fileModifyTime(targetName);
    }

    @Override
    public void deleteTargetFile(String targetName) {
        remoteController.deleteFile(targetName);
    }

    @Override
    public void targetMkdirs(String targetDirectory) {
        remoteController.mkdirs(targetDirectory);
    }

    @Override
    public boolean copyFile(File sourceFile, String targetFile) {
        return remoteController.copyFile(sourceFile, targetFile);
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
