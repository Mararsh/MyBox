package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TreeTableEraCell;
import mara.mybox.fxml.cell.TreeTableFileSizeCell;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-11-25
 * @License Apache License Version 2.0
 */
public class FilesTreeController extends BaseTaskController {

    protected boolean listenDoubleClick;

    @FXML
    protected TreeTableView<FileNode> filesTreeView;
    @FXML
    protected TreeTableColumn<FileNode, String> fileColumn, nodeColumn, hierarchyColumn, typeColumn, permissionColumn;
    @FXML
    protected TreeTableColumn<FileNode, Long> sizeColumn, modifyTimeColumn, accessTimeColumn, createTimeColumn;
    @FXML
    protected TreeTableColumn<FileNode, Boolean> selectedColumn;

    public FilesTreeController() {
        baseTitle = message("FilesTree");
        listenDoubleClick = false;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTreeTableView();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    protected void initTreeTableView() {
        try {
            if (hierarchyColumn != null) {
                fileColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileName"));
                fileColumn.setPrefWidth(400);
            }

            if (nodeColumn != null) {
                nodeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("nodename"));
                nodeColumn.setPrefWidth(400);
            }

            if (hierarchyColumn != null) {
                hierarchyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("hierarchyNumber"));
            }

            if (selectedColumn != null) {
                selectedColumn.setCellValueFactory(
                        new Callback<TreeTableColumn.CellDataFeatures<FileNode, Boolean>, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<FileNode, Boolean> param) {
                        if (param.getValue() != null) {
                            return param.getValue().getValue().getSelected();
                        }
                        return null;
                    }
                });
                selectedColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectedColumn));
            }

            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("suffix"));

            sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileSize"));
            sizeColumn.setCellFactory(new TreeTableFileSizeCell());

            modifyTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("modifyTime"));
            modifyTimeColumn.setCellFactory(new TreeTableEraCell());

            if (accessTimeColumn != null) {
                accessTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("accessTime"));
                accessTimeColumn.setCellFactory(new TreeTableEraCell());
            }

            if (createTimeColumn != null) {
                createTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("createTime"));
                createTimeColumn.setCellFactory(new TreeTableEraCell());
            }
            if (permissionColumn != null) {
                permissionColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("permission"));
            }

            filesTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            if (listenDoubleClick) {
                filesTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() > 1) {
                            TreeItem<FileNode> item = filesTreeView.getSelectionModel().getSelectedItem();
                            if (item == null) {
                                return;
                            }
                            File file = item.getValue().getFile();
                            if (file == null || !file.exists() || !file.isFile()) {
                                return;
                            }
                            view(file);
                        }
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void addSelectedListener(TreeItem<FileNode> item) {
        if (item == null) {
            return;
        }
        FileNode node = item.getValue();
        if (node == null) {
            return;
        }
        node.getSelected().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                if (isSettingValues || item == null || item.getChildren() == null) {
                    return;
                }
                isSettingValues = true;
                selectChildren(item, nv);
                filesTreeView.refresh();
                isSettingValues = false;
            }
        });
    }

    protected void selectChildren(TreeItem<FileNode> item, boolean select) {
        if (item == null || item.getChildren() == null) {
            return;
        }
        for (TreeItem<FileNode> child : item.getChildren()) {
            child.getValue().setSelected(select);
            selectChildren(child, select);
        }
    }

    protected TreeItem<FileNode> getChild(TreeItem<FileNode> item, String name) {
        if (item == null) {
            return null;
        }
        for (TreeItem<FileNode> child : item.getChildren()) {
            if (name.equals(child.getValue().getData())) {
                return child;
            }
        }
        FileNode childInfo = new FileNode();
        childInfo.setData(name);
        TreeItem<FileNode> childItem = new TreeItem(childInfo);
        childItem.setExpanded(true);
        addSelectedListener(childItem);
        item.getChildren().add(childItem);
        return childItem;
    }

    protected TreeItem<FileNode> find(TreeItem<FileNode> item, String name) {
        if (item == null || name == null || item.getValue() == null) {
            return null;
        }
        if (name.equals(item.getValue().getData())) {
            return item;
        }
        for (TreeItem<FileNode> child : item.getChildren()) {
            TreeItem<FileNode> find = find(child, name);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

}
