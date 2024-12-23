package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataTag;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.cell.TableColorCell;

/**
 * @Author Mara
 * @CreateDate 2024-11-1
 * @License Apache License Version 2.0
 */
public class ControlDataNodeTags extends BaseTableViewController<DataTag> {

    protected DataTreeNodeEditorController nodeEditor;
    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected boolean changed;
    protected List<Long> loadedTags = new ArrayList<>();

    @FXML
    protected TableColumn<DataTag, String> tagColumn;
    @FXML
    protected TableColumn<DataTag, Color> colorColumn;

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));

            colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
            colorColumn.setCellFactory(new TableColorCell<>());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(DataTreeNodeEditorController controller) {
        try {
            this.nodeEditor = controller;
            this.parentController = nodeEditor;
            nodeTable = nodeEditor.nodeTable;
            tagTable = nodeEditor.tagTable;
            nodeTagsTable = nodeEditor.nodeTagsTable;

            baseName = baseName + "_" + nodeTable.getTableName();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTags() {
        tableData.clear();
        loadedTags.clear();
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {
            private List<DataTag> tags;
            private List<DataNodeTag> nodeTags;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tags = tagTable.readAll(conn);
                    if (nodeEditor.currentNode != null
                            && nodeEditor.currentNode.getNodeid() >= 0) {
                        nodeTags = nodeTagsTable.nodeTags(conn,
                                nodeEditor.currentNode.getNodeid());
                        if (nodeTags != null && !nodeTags.isEmpty()) {
                            for (DataNodeTag nodeTag : nodeTags) {
                                loadedTags.add(nodeTag.getTtagid());
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (tags != null && !tags.isEmpty()) {
                    tableData.setAll(tags);
                    if (!loadedTags.isEmpty()) {
                        isSettingValues = true;
                        for (DataTag tag : tableData) {
                            if (loadedTags.contains(tag.getTagid())) {
                                tableView.getSelectionModel().select(tag);
                            }
                        }
                        isSettingValues = false;
                    }
                }
            }

        };
        start(task, thisPane);
    }

    public void copyTags() {
        tableData.clear();
        loadedTags.clear();
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {
            private List<DataTag> tags;
            private List<DataNodeTag> nodeTags;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tags = tagTable.readAll(conn);
                    if (nodeEditor.currentNode != null
                            && nodeEditor.currentNode.getNodeid() >= 0) {
                        nodeTags = nodeTagsTable.nodeTags(conn,
                                nodeEditor.currentNode.getNodeid());
                        if (nodeTags != null && !nodeTags.isEmpty()) {
                            for (DataNodeTag nodeTag : nodeTags) {
                                loadedTags.add(nodeTag.getTtagid());
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (tags != null && !tags.isEmpty()) {
                    tableData.setAll(tags);
                    if (!loadedTags.isEmpty()) {
                        isSettingValues = true;
                        for (DataTag tag : tableData) {
                            if (loadedTags.contains(tag.getTagid())) {
                                tableView.getSelectionModel().select(tag);
                            }
                        }
                        isSettingValues = false;
                    }
                }
            }

        };
        start(task, thisPane);
    }

    @FXML
    @Override
    public void recoverAction() {
        loadTags();
    }

    @FXML
    public void manageAction() {
        DataTreeTagsController.edit(this);
    }

    @Override
    public void notifySelected() {
        if (isSettingValues || nodeEditor == null) {
            return;
        }
        isSettingValues = true;
        changed = false;
        List<DataTag> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            changed = !loadedTags.isEmpty();
        } else {
            if (loadedTags.isEmpty()) {
                changed = true;
            } else {
                List<Long> selectedIDs = new ArrayList<>();
                for (DataTag tag : selected) {
                    selectedIDs.add(tag.getTagid());
                }
                for (long id : selectedIDs) {
                    if (!loadedTags.contains(id)) {
                        changed = true;
                        break;
                    }
                }
                if (!changed) {
                    for (long id : loadedTags) {
                        if (!selectedIDs.contains(id)) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }
        isSettingValues = false;
        nodeEditor.updateStatus();
    }

}
