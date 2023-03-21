package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;
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

    protected ChangeListener<Boolean> expandListener;

    @FXML
    protected ControlRemoteConnection remoteController;
    @FXML
    protected Tab remoteTab, filesTab;
    @FXML
    protected VBox filesBox;
    @FXML
    protected TreeTableColumn<FileNode, Integer> uidColumn, gidColumn;

    public RemotePathManageController() {
        baseTitle = message("RemotePathManage");
        listenDoubleClick = false;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            remoteController.setParameters(this);
            filesBox.setDisable(true);

            uidColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("uid"));
            gidColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("gid"));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        openPath();
    }

    @FXML
    @Override
    public void openPath() {
        filesBox.setDisable(true);
        if (!remoteController.pickProfile()) {
            return;
        }
        remoteController.disconnect();
        loadPath();
    }

    public void loadPath() {
        if (task != null) {
            task.cancel();
        }
        filesTreeView.setRoot(null);
        tabPane.getSelectionModel().select(logsTab);
        task = new SingletonTask<Void>(this) {

            TreeItem<FileNode> rootItem;

            @Override
            protected boolean handle() {
                try {
                    if (!checkConnection()) {
                        return false;
                    }
                    String rootPath = remoteController.currentConnection.getPath();
                    FileNode rootInfo = new FileNode()
                            .setNodename(rootPath)
                            .setIsRemote(true)
                            .attrs(remoteController.stat(rootPath));
                    rootItem = new TreeItem(rootInfo);
                    rootItem.setExpanded(true);

                    List<TreeItem<FileNode>> children = makeChildren(rootItem);
                    if (children != null && !children.isEmpty()) {
                        rootItem.getChildren().setAll(children);
                        addSelectedListener(rootItem);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
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
                filesBox.setDisable(false);
                filesTreeView.setRoot(rootItem);
            }
        };
        start(task);
    }

    protected boolean checkConnection() {
        if (remoteController.sshSession != null) {
            remoteController.task = task;
            return true;
        }
        return remoteController.connect(task);
    }

    protected List<TreeItem<FileNode>> makeChildren(TreeItem<FileNode> treeItem) {
        List<TreeItem<FileNode>> children = new ArrayList<>();
        try {
            FileNode remoteFile = (FileNode) (treeItem.getValue());
            if (remoteFile == null || !checkConnection()) {
                return null;
            }
            String path = remoteFile.fullName();
            Iterator<ChannelSftp.LsEntry> iterator = remoteController.ls(path);
            if (iterator == null) {
                return children;
            }
            while (iterator.hasNext()) {
                if (task == null || task.isCancelled()) {
                    return children;
                }
                ChannelSftp.LsEntry entry = iterator.next();
                String name = entry.getFilename();
                if (name == null || name.isBlank() || ".".equals(name) || "..".equals(name)) {
                    continue;
                }
                FileNode fileInfo = new FileNode()
                        .setNodename(name)
                        .setParentFile(remoteFile)
                        .setIsRemote(true)
                        .attrs(entry.getAttrs());

                TreeItem<FileNode> fileItem = new TreeItem<>(fileInfo);
                fileItem.setExpanded(true);
                children.add(fileItem);

                if (fileInfo.isDirectory()) {
                    FileNode dummyInfo = new FileNode()
                            .setNodename("Loading")
                            .setParentFile(remoteFile)
                            .setIsRemote(false);
                    TreeItem<FileNode> dummyItem = new TreeItem(dummyInfo);
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

        } catch (Exception e) {
            error = e.toString();
        }
        return children;
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
                    children = makeChildren(treeItem);
                    return children != null;
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
    public void disconnect() {
        tabPane.getSelectionModel().select(logsTab);
        if (task != null) {
            task.cancel();
        }
        remoteController.disconnect();
        popInformation(message("Disconnected"));
    }

    @FXML
    @Override
    public void refreshAction() {
        loadPath();
    }

    @FXML
    public void permissionAction() {

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
