package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataTag;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableDataNode;
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
public class ControlDataTreeNodeTags extends BaseTableViewController<DataTag> {

    protected BaseDataTreeNodeController nodeController;
    protected TableDataNode dataNodeTable;
    protected TableDataTag dataTagTable;
    protected TableDataNodeTag dataNodeTagTable;
    protected BaseTable dataTable;
    protected DataNode currentNode;
    protected boolean changed;

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

    public void setParameters(BaseDataTreeNodeController controller) {
        try {
            this.nodeController = controller;
            this.parentController = nodeController;
            this.baseName = nodeController.baseName;
            dataTable = nodeController.dataTable;
            dataNodeTable = nodeController.dataNodeTable;
            dataTagTable = nodeController.dataTagTable;
            dataNodeTagTable = nodeController.dataNodeTagTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTags(DataNode node) {
        currentNode = node;
        tableData.clear();
        if (task != null) {
            task.cancel();
        }
        task = new FxTask<Void>(this) {
            private List<DataTag> tags;
            private List<DataNodeTag> nodeTags;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    tags = dataTagTable.readAll(conn);
                    if (currentNode != null) {
                        nodeTags = dataNodeTagTable.nodeTags(conn, currentNode.getNodeid());
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
                    if (nodeTags != null && !nodeTags.isEmpty()) {
                        isSettingValues = true;
                        for (DataTag tag : tableData) {
                            long tagid = tag.getTagid();
                            for (DataNodeTag nodeTag : nodeTags) {
                                if (nodeTag.getTtagid() == tagid) {
                                    tableView.getSelectionModel().select(tag);
                                    break;
                                }
                            }
                        }
                        isSettingValues = false;
                    }
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                tableChanged(false);
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
        DataTreeTagsController.open(this);
    }

    @Override
    public void tableChanged(boolean tableChanged) {
        if (isSettingValues || nodeController == null) {
            return;
        }
        super.tableChanged(changed);
        changed = tableChanged;
        nodeController.tagsChanged();
    }

}
