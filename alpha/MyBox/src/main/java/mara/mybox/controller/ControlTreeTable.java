package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableIDCell;
import mara.mybox.fxml.cell.TableTextTrimCell;

/**
 * @Author Mara
 * @CreateDate 2025-4-25
 * @License Apache License Version 2.0
 */
public class ControlTreeTable extends BaseTablePagesController<DataNode> {

    protected BaseDataTreeController dataController;
    protected BaseNodeTable nodeTable;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected DataNode currentNode;

    @FXML
    protected TableColumn<DataNode, Integer> dataRowColumn;
    @FXML
    protected TableColumn<DataNode, String> hierarchyColumn, titleColumn;
    @FXML
    protected TableColumn<DataNode, Long> idColumn;
    @FXML
    protected TableColumn<DataNode, Float> orderColumn;
    @FXML
    protected TableColumn<DataNode, Date> timeColumn;
    @FXML
    protected FlowPane namesPane;

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initColumns() {
        try {

            hierarchyColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleColumn.setCellFactory(new TableTextTrimCell());

            idColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            idColumn.setCellFactory(new TableIDCell());

            orderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearData() {
        try {
            tableData.clear();
            namesPane.getChildren().clear();
            paginationController.reset();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTree() {
        loadTree(null);
    }

    public void loadTree(DataNode node) {
        if (node == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        clearData();
        task = new FxSingletonTask<Void>(this) {

            private TreeItem<DataNode> rootItem, selectItem;
            private int size;

            @Override
            protected boolean handle() {
                rootItem = null;
                if (nodeTable == null) {
                    return true;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    DataNode rootNode = nodeTable.getRoot(conn);
                    if (rootNode == null) {
                        return false;
                    }
                    rootItem = new TreeItem(rootNode);
                    rootItem.setExpanded(true);
                    size = nodeTable.size(conn);

                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return task != null && !isCancelled();
            }

            @Override
            protected void whenSucceeded() {

            }

        };
        start(task, thisPane);
    }

}
