package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
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

    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected boolean changed;
    protected List<Long> loadedTags = new ArrayList<>();
    protected DataNode currentNode;
    protected List<DataTag> selected;

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

    public void setParameters(BaseController controller, BaseNodeTable ntable,
            TableDataTag ttable, TableDataNodeTag nttable) {
        try {
            this.parentController = controller;
            nodeTable = ntable;
            tagTable = ttable;
            nodeTagsTable = nttable;

            baseName = baseName + "_" + nodeTable.getTableName();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTags(DataNode node) {
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
                    if (node != null && node.getNodeid() >= 0) {
                        nodeTags = nodeTagsTable.nodeTags(conn, node.getNodeid());
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
                currentNode = node;
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
        loadTags(currentNode);
    }

    @FXML
    public void manageAction() {
        DataTreeTagsController.edit(this);
        setIconified(true);
    }

    @Override
    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        changed = false;
        selected = tableView.getSelectionModel().getSelectedItems();
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
        selectedNotify.set(!selectedNotify.get());
    }

}
