package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-18
 * @License Apache License Version 2.0
 */
public class RemotePathManageController extends FilesTreeController {

    protected String rootPath;
    protected ChangeListener<Boolean> expandListener;

    @FXML
    protected ControlRemotePath remoteController;
    @FXML
    protected Tab remoteTab, filesTab;

    public RemotePathManageController() {
        baseTitle = message("RemotePathManage");
        listenDoubleClick = false;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            remoteController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void goAction() {
        if (task != null) {
            task.cancel();
        }
        remoteController.disconnect();
        if (!remoteController.pickProfile()) {
            return;
        }
        tabPane.getSelectionModel().select(logsTab);
        rootPath = remoteController.currentConnection.getPath();
        FileNode rootInfo = new FileNode(null, rootPath, true);
        rootInfo.setFileType(FileInformation.FileType.Directory);
        TreeItem<FileNode> rootItem = new TreeItem(rootInfo);
        rootItem.setExpanded(true);
        filesTreeView.setRoot(rootItem);

        expandPath(rootItem);
    }

    protected void expandPath(TreeItem<FileNode> treeItem) {
        if (treeItem == null) {
            return;
        }
        FileNode remoteFile = (FileNode) (treeItem.getValue());
        if (remoteFile == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        treeItem.setExpanded(true);
        task = new SingletonTask<Void>(this) {

            List<TreeItem<FileNode>> children;

            @Override
            protected boolean handle() {
                try {
                    children = new ArrayList<>();
                    if (remoteController.sshSession == null && !remoteController.connect(task)) {
                        return false;
                    }
                    remoteController.task = task;
                    String path = remoteFile.fullName();
                    remoteController.showLogs("Expand: " + path);
                    Iterator<ChannelSftp.LsEntry> iterator = remoteController.ls(path);
                    if (iterator == null) {
                        return true;
                    }
                    while (iterator.hasNext()) {
                        if (task == null || task.isCancelled()) {
                            return true;
                        }
                        ChannelSftp.LsEntry entry = iterator.next();
                        String name = entry.getFilename();
                        if (name == null || name.isBlank() || ".".equals(name) || "..".equals(name)) {
                            continue;
                        }
                        FileNode fileInfo = new FileNode(remoteFile, name, true);
                        SftpATTRS attrs = entry.getAttrs();
                        boolean isDir = attrs.isDir();
                        fileInfo.setFileType(isDir ? FileInformation.FileType.Directory : FileInformation.FileType.File);
                        fileInfo.setModifyTime(attrs.getMTime());
                        fileInfo.setFileSize(attrs.getSize());
                        TreeItem<FileNode> fileItem = new TreeItem(fileInfo);
                        fileItem.setExpanded(true);
                        children.add(fileItem);

                        if (isDir) {
                            TreeItem<FileNode> dummyItem = new TreeItem(new FileNode(remoteFile, "Loading", false));
                            fileItem.getChildren().add(dummyItem);
                            fileItem.setExpanded(false);
                            fileItem.expandedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                                    if (!isSettingValues) {
                                        fileItem.expandedProperty().removeListener(this);
                                        expandPath(fileItem);
                                    }
                                }
                            });
                        }
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return true;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void whenCanceled() {
                cancelled = true;
                showLogs(message("Cancel"));
            }

            @Override
            protected void finalAction() {
                tabPane.getSelectionModel().select(filesTab);
                treeItem.getChildren().setAll(children);
                if (!children.isEmpty()) {
                    addSelectedListener(treeItem);
                }
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void refreshAction() {

    }

    @FXML
    public void clearFiles() {

    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            cancelTask();
            cancelled = true;
            remoteController.disconnect();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
