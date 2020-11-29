package mara.mybox.controller;

import java.io.File;
import javafx.application.Platform;
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
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.TreeTableFileSizeCell;
import mara.mybox.fxml.TreeTableEraCell;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-11-25
 * @License Apache License Version 2.0
 */
public class FilesTreeController extends BaseController {

    protected boolean listenDoubleClick;

    @FXML
    protected TreeTableView<FileInformation> filesTreeView;
    @FXML
    protected TreeTableColumn<FileInformation, String> fileColumn, typeColumn;
    @FXML
    protected TreeTableColumn<FileInformation, Long> sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    protected TreeTableColumn<FileInformation, Boolean> selectedColumn;

    public FilesTreeController() {
        baseTitle = AppVariables.message("FilesTree");
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

            fileColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileName"));
            fileColumn.setPrefWidth(400);

//            selectedColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("selected"));
            selectedColumn.setCellValueFactory(
                    new Callback<TreeTableColumn.CellDataFeatures<FileInformation, Boolean>, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<FileInformation, Boolean> param) {
                    if (param.getValue() != null) {
                        return param.getValue().getValue().getSelectedProperty();
                    }
                    return null;
                }
            });
            selectedColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectedColumn));

            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileSuffix"));

            sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileSize"));
            sizeColumn.setCellFactory(new TreeTableFileSizeCell());

            modifyTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("modifyTime"));
            modifyTimeColumn.setCellFactory(new TreeTableEraCell());

            createTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("createTime"));
            createTimeColumn.setCellFactory(new TreeTableEraCell());

            filesTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            if (listenDoubleClick) {
                filesTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() > 1) {
                            TreeItem<FileInformation> item = filesTreeView.getSelectionModel().getSelectedItem();
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

        }

    }

    protected TreeItem<FileInformation> getChild(TreeItem<FileInformation> item, String name) {
        if (item == null) {
            return null;
        }
        for (TreeItem<FileInformation> child : item.getChildren()) {
            if (name.equals(child.getValue().getFileName())) {
                return child;
            }
        }
        FileInformation childInfo = new FileInformation();
        childInfo.setFileName(name);
        final TreeItem<FileInformation> childItem = new TreeItem(childInfo);
        childItem.setExpanded(true);
        childInfo.getSelectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldItem, Boolean newItem) {
                if (!isSettingValues) {
                    treeItemSelected(childItem, newItem);
                }
            }
        });
        item.getChildren().add(childItem);
        return childItem;
    }

    protected void treeItemSelected(TreeItem<FileInformation> item, boolean select) {
        if (isSettingValues || item == null || item.getChildren() == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit() ) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                isSettingValues = true;
                                selectChildren(item, select);
                                isSettingValues = false;
                                filesTreeView.refresh();
                            }
                        });
                        return true;
                    } catch (Exception e) {
                        isSettingValues = false;
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    filesTreeView.refresh();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void selectChildren(TreeItem<FileInformation> item, boolean select) {
        if (item == null || item.getChildren() == null) {
            return;
        }
        for (TreeItem<FileInformation> child : item.getChildren()) {
            child.getValue().setSelected(select);
            selectChildren(child, select);
        }
    }

    protected TreeItem<FileInformation> find(TreeItem<FileInformation> item, String name) {
        if (item == null || name == null || item.getValue() == null) {
            return null;
        }
        if (name.equals(item.getValue().getFileName())) {
            return item;
        }
        for (TreeItem<FileInformation> child : item.getChildren()) {
            TreeItem<FileInformation> find = find(child, name);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

}
