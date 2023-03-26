package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
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
            filesTreeView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {

                @Override
                public void onChanged(ListChangeListener.Change c) {
                    checkButtons();
                }
            });
            checkButtons();

            filesTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popFunctionsMenu(event);
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkButtons() {
        try {
            boolean noSelected = filesTreeView.getSelectionModel().getSelectedItem() == null;
            clearDirectoryButton.setDisable(noSelected);
            deleteButton.setDisable(noSelected);
            permissionButton.setDisable(noSelected);
            renameButton.setDisable(noSelected);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        remoteController.taskController = this;
        filesBox.setDisable(true);
        checkButtons();
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
        remoteController.taskController = this;
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
                checkButtons();
            }
        };
        start(task);
    }

    protected boolean checkConnection() {
        remoteController.task = task;
        if (remoteController.sshSession != null) {
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
    public void permissionAction() {

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
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        RemotePathRenameController.open(this, item.getValue().fullName());
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
            }

            @Override
            protected void finalAction() {
                task = null;
                loadPath();
                popSuccessful();
            }
        };
        start(task);
    }

    @FXML
    public void getAction() {

    }

    @FXML
    public void putAction() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            item = filesTreeView.getRoot();
        }
        RemotePathPutController.open(this, item.getValue().path(false));
    }

    @FXML
    public void makeDirectory() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            item = filesTreeView.getRoot();
        }
        String makeName = PopTools.askValue(baseTitle, message("MakeDirectory"), "",
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
            }

            @Override
            protected void finalAction() {
                task = null;
                loadPath();
                popSuccessful();
            }
        };
        start(task);
    }

    public void deleteFile() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        String filename = item.getValue().fullName();
        String deleteName = PopTools.askValue(baseTitle, message("DeleteFile"), "", filename);
        if (deleteName == null || deleteName.isBlank()) {
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
                return remoteController.delete(deleteName);
            }

            @Override
            protected void whenCanceled() {
                cancelled = true;
                showLogs(message("Cancel"));
            }

            @Override
            protected void finalAction() {
                task = null;
                loadPath();
                popSuccessful();
            }
        };
        start(task);
    }

    public void deleteFiles() {

    }

    @FXML
    @Override
    public void deleteAction() {
        deleteFiles();
    }

    @FXML
    public void clearDirectory() {
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        FileNode node = item.getValue();
        if (!node.isDirectory()) {
            popError(message("SelectToHandle"));
            return;
        }
        String filename = node.fullName();
        String clearName = PopTools.askValue(baseTitle, message("ClearDirectory"), "", filename);
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
                return remoteController.clearDirectory(clearName);
            }

            @Override
            protected void whenCanceled() {
                cancelled = true;
                showLogs(message("Cancel"));
            }

            @Override
            protected void finalAction() {
                task = null;
                loadPath();
                popSuccessful();
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void popFunctionsMenu(Event event) {
        if (getMyWindow() == null) {
            return;
        }
        TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
        String filename;
        if (item == null || item.getValue() == null) {
            filename = null;
        } else if (item.getValue().getParentNode() == null) {
            filename = StringTools.menuSuffix(item.getValue().getNodename());
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

        menuItem = new MenuItem(message("MakeDirectory"), StyleTools.getIconImageView("iconAdd.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            makeDirectory();
        });
        menuItem.setDisable(item == null);
        items.add(menuItem);

        menuItem = new MenuItem(message("Upload"), StyleTools.getIconImageView("iconUpload.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            putAction();
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("DeleteFiles"), StyleTools.getIconImageView("iconDelete.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            deleteFiles();
        });
        menuItem.setDisable(item == null);
        items.add(menuItem);

        menuItem = new MenuItem(message("ClearDirectory"), StyleTools.getIconImageView("iconClear.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            clearDirectory();
        });
        menuItem.setDisable(item == null);
        items.add(menuItem);

        menuItem = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconInput.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            renameAction();
        });
        menuItem.setDisable(item == null);
        items.add(menuItem);

        menuItem = new MenuItem(message("Permission"), StyleTools.getIconImageView("iconPermission.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            permissionAction();
        });
        menuItem.setDisable(item == null);
        items.add(menuItem);

        items.add(new SeparatorMenuItem());

        menuItem = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
        menuItem.setStyle("-fx-text-fill: #2e598a;");
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menuItem);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        LocateTools.locateEvent(event, popMenu);
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
