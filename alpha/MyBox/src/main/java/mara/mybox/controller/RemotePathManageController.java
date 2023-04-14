package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileNode;
import mara.mybox.db.data.PathConnection;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
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
    @FXML
    protected Button clearDirectoryButton, permissionButton,
            makeDirectoryButton, downloadButton, uploadButton;

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

            filesTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    if (event.getButton() == MouseButton.SECONDARY) {
                        showFunctionsMenu(event);
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void startAction() {
        goPath();
    }

    @FXML
    public void goPath() {
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
                task = null;
                tabPane.getSelectionModel().select(filesTab);
                filesBox.setDisable(false);
                filesTreeView.setRoot(rootItem);
            }
        };
        start(task);
    }

    protected boolean checkConnection() {
        remoteController.task = task;
        if (remoteController.sshSession != null && remoteController.sshSession.isConnected()) {
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
                task = null;
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
    public void copyFileNameAction() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(this, item.getValue().getNodename());
    }

    @FXML
    public void copyFullNameAction() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(this, item.getValue().fullName());
    }

    @FXML
    public void renameAction() {
        RemotePathRenameController.open(this);
    }

    public void renameFile(String currentName, String newName) {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (!checkConnection()) {
                    return false;
                }
                return remoteController.renameFile(currentName, newName);
            }

            @Override
            protected void whenCanceled() {
                cancelled = true;
                showLogs(message("Cancel"));
                disconnect();
            }

            @Override
            protected void finalAction() {
                loadPath();
            }
        };
        start(task);
    }

    @FXML
    public void getAction() {
        RemotePathGetController.open(this);
    }

    @FXML
    public void putAction() {
        RemotePathPutController.open(this);
    }

    @FXML
    public void makeDirectory() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            item = filesTreeView.getRoot();
        }
        String makeName = PopTools.askValue(baseTitle,
                message("CreateFileComments"), message("MakeDirectory"),
                item.getValue().path(true) + "m");
        if (makeName == null || makeName.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (!checkConnection()) {
                    return false;
                }
                return remoteController.mkdirs(makeName);
            }

            @Override
            protected void whenCanceled() {
                cancelled = true;
                showLogs(message("Cancel"));
                disconnect();
            }

            @Override
            protected void finalAction() {
                loadPath();
            }
        };
        start(task);
    }

    @FXML
    public void permissionAction() {
        RemotePathPermissionController.open(this);
    }

    @FXML
    @Override
    public void deleteAction() {
        RemotePathDeleteController.open(this);
    }

    @FXML
    public void clearDirectory() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        String filename = item.getValue().path(false);
        String clearName = PopTools.askValue(baseTitle,
                message("MakeSureAccountHasPermission"), message("ClearDirectory"),
                filename);
        if (clearName == null || clearName.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (!checkConnection()) {
                    return false;
                }
                remoteController.count = 0;
                return remoteController.clearDirectory(clearName);
            }

            @Override
            protected void whenCanceled() {
                cancelled = true;
                showLogs(message("Cancel"));
                disconnect();
            }

            @Override
            protected void finalAction() {
                showLogs(message("Deleted") + ": " + remoteController.count);
                loadPath();
            }
        };
        start(task);
    }

    public void showFunctionsMenu(MouseEvent event) {
        if (getMyWindow() == null) {
            return;
        }
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        String filename;
        if (item == null || item.getValue() == null) {
            filename = null;
        } else if (item.getValue().isDirectory()) {
            filename = StringTools.menuSuffix(item.getValue().fullName());
        } else {
            filename = (StringTools.menuSuffix(item.getValue().path(true)) + "\n"
                    + StringTools.menuSuffix(item.getValue().getNodename()));
        }

        List<MenuItem> items = new ArrayList<>();
        MenuItem menuItem = new MenuItem(filename);
        menuItem.setStyle("-fx-text-fill: #2e598a;");
        items.add(menuItem);
        items.add(new SeparatorMenuItem());

        menuItem = new MenuItem(message("Download"), StyleTools.getIconImageView("iconDownload.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            getAction();
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("CopyFileName"), StyleTools.getIconImageView("iconCopySystem.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            copyFileNameAction();
        });
        menuItem.setDisable(item == null);
        items.add(menuItem);

        menuItem = new MenuItem(message("CopyFullName"), StyleTools.getIconImageView("iconCopySystem.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            copyFullNameAction();
        });
        menuItem.setDisable(item == null);
        items.add(menuItem);

        menuItem = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menuItem);

        items.add(new SeparatorMenuItem());

        menuItem = new MenuItem(message("MakeDirectory"), StyleTools.getIconImageView("iconNewItem.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            makeDirectory();
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Upload"), StyleTools.getIconImageView("iconUpload.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            putAction();
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            deleteAction();
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("ClearDirectory"), StyleTools.getIconImageView("iconClear.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            clearDirectory();
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconInput.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            renameAction();
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("SetPermissions"), StyleTools.getIconImageView("iconPermission.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            permissionAction();
        });
        items.add(menuItem);

        popEventMenu(event, items);
    }

    public void openPath(PathConnection profile) {
        remoteController.editProfile(profile);
        goPath();
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

    /*
        static methods
     */
    public static RemotePathManageController open(PathConnection profile) {
        try {
            RemotePathManageController controller
                    = (RemotePathManageController) WindowTools.openStage(Fxmls.RemotePathManageFxml);
            controller.requestMouse();
            controller.openPath(profile);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
