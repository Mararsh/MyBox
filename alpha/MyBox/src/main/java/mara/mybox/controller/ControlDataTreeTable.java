package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.cell.TableTextTrimCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class ControlDataTreeTable extends BaseSysTableController<DataNode> {

    protected BaseDataTreeController dataController;
    protected BaseDataTreeManageController manager;
    protected TableDataNode tableTree;
    protected String queryLabel;
    protected DataNode loadedParent;

    @FXML
    protected TableColumn<DataNode, Long> nodeidColumn;
    @FXML
    protected TableColumn<DataNode, String> nameColumn, valueColumn;
    @FXML
    protected TableColumn<DataNode, Date> timeColumn;
    @FXML
    protected VBox conditionBox;
    @FXML
    protected ToggleGroup nodesGroup;
    @FXML
    protected RadioButton childrenRadio, descendantsRadio;
    @FXML
    protected FlowPane namesPane, nodeGroupPane;
    @FXML
    protected Label conditionLabel;
    @FXML
    protected Button operationsButton;

    @Override
    public void setTableDefinition() {

    }

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;
            baseName = dataController.baseName;
            tableTree = dataController.treeTable;
            tableDefinition = dataController.treeTable;

            if (dataController instanceof BaseDataTreeManageController) {
                manager = (BaseDataTreeManageController) dataController;
            }
            if (manager == null) {
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                operationsButton.setVisible(false);
            }

            nodeidColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(new TableTextTrimCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));

            if (UserConfig.getBoolean(baseName + "AllDescendants", false)) {
                descendantsRadio.setSelected(true);
            }
            nodesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldTab, Toggle newTab) {
                    UserConfig.setBoolean(baseName + "AllDescendants", descendantsRadio.isSelected());
                    if (loadedParent != null) {
                        loadTableData();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearQuery() {
        loadedParent = null;
        queryConditions = null;
        queryLabel = null;
        tableData.clear();
        conditionBox.getChildren().clear();
        namesPane.getChildren().clear();
        startRowOfCurrentPage = 0;
    }

    public void loadNodes(DataNode parentNode) {
        clearQuery();
        loadedParent = parentNode;
        if (loadedParent != null) {
            queryConditions = "parentid=" + loadedParent.getNodeid() + " AND nodeid<>parentid";
            loadTableData();
        }
//        dataController.showNodesList(true);
    }

    public void loadChildren(DataNode parentNode) {
        childrenRadio.setSelected(true);
        loadNodes(parentNode);
    }

    public void loadDescendants(DataNode parentNode) {
        descendantsRadio.setSelected(true);
        loadNodes(parentNode);
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        if (loadedParent != null) {
            tableData.add(0, loadedParent);
        }
        makeConditionPane();
    }

    public void makeConditionPane() {
        conditionBox.getChildren().clear();
        if (loadedParent == null) {
            if (queryConditionsString != null) {
                conditionLabel.setText(queryConditionsString.length() > 300
                        ? queryConditionsString.substring(0, 300) : queryConditionsString);
                conditionBox.getChildren().add(conditionLabel);
            }
            conditionBox.applyCss();
            return;
        }
        FxTask loadTask = new FxTask<Void>(this) {
            private List<DataNode> ancestor;

            @Override
            protected boolean handle() {
                ancestor = tableTree.ancestor(loadedParent.getNodeid());
                return true;
            }

            @Override
            protected void whenSucceeded() {
                List<Node> nodes = new ArrayList<>();
                if (ancestor != null) {
                    for (DataNode node : ancestor) {
                        Hyperlink link = new Hyperlink(node.getTitle());
                        link.setWrapText(true);
                        link.setMinHeight(Region.USE_PREF_SIZE);
                        link.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                loadNodes(node);
                            }
                        });
                        nodes.add(link);
                        nodes.add(new Label(">"));
                    }
                }
                Label label = new Label(loadedParent.getTitle());
                label.setWrapText(true);
                label.setMinHeight(Region.USE_PREF_SIZE);
                nodes.add(label);
                namesPane.getChildren().setAll(nodes);
                conditionBox.getChildren().setAll(namesPane, nodeGroupPane);
                conditionBox.applyCss();
            }
        };
        start(loadTask, false);
    }

    @Override
    public long readDataSize(FxTask currentTask, Connection conn) {
        if (loadedParent != null) {
            if (descendantsRadio.isSelected()) {
                return tableTree.decentantsSize(conn, loadedParent.getNodeid()) + 1;
            } else {
                return tableTree.conditionSize(conn, queryConditions) + 1;
            }

        } else if (queryConditions != null) {
            return tableTree.conditionSize(conn, queryConditions);

        } else {
            return 0;
        }

    }

    @Override
    public List<DataNode> readPageData(FxTask currentTask, Connection conn) {
        if (loadedParent != null && descendantsRadio.isSelected()) {
            return tableTree.decentants(conn, loadedParent.getNodeid(), startRowOfCurrentPage, pageSize);

        } else if (queryConditions != null) {
            return tableTree.queryConditions(conn, queryConditions, orderColumns, startRowOfCurrentPage, pageSize);

        } else {
            return null;
        }
    }

    @Override
    public void itemClicked() {
        if (manager == null) {
            viewAction();
        }
    }

    @Override
    public void itemDoubleClicked() {
        if (manager != null) {
            editAction();
        }
    }

    @FXML
    @Override
    public void viewAction() {
        DataNode item = selectedItem();
        if (item == null) {
            popError(message("SelectToHanlde"));
            return;
        }
//        if (dataController instanceof ControlInfoTreeHandler) {
//            ((ControlInfoTreeHandler) dataController).viewNode(item);
//        } else {
//            dataController.popNode(item);
//        }

    }

    @Override
    protected long clearData(FxTask currentTask) {
        if (queryConditions != null) {
            return tableTree.deleteCondition(queryConditions);

        } else {
            return -1;
        }
    }

    @Override
    protected void afterDeletion() {
        super.afterDeletion();
        if (manager != null) {
            manager.nodesDeleted();
        }
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        if (manager != null) {
            manager.nodesDeleted();
        }
    }

    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            boolean selected = !isNoneSelected();

            if (selected) {
                menu = new MenuItem(message("View"), StyleTools.getIconImageView("iconView.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                refreshAction();
            });
            items.add(menu);

            items.addAll(super.makeTableContextMenu());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        try {
            if (manager == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            boolean selected = !isNoneSelected();

            menu = new MenuItem(message("Add"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                addAction();
            });
            items.add(menu);

            if (selected) {
                menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Paste"), StyleTools.getIconImageView("iconPaste.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    manager.pasteAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Move"), StyleTools.getIconImageView("iconMove.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    manager.moveAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    manager.copyAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearAction();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        List<MenuItem> items = viewMenuItems(null);
        List<MenuItem> opItems = operationsMenuItems(null);
        if (opItems != null && !opItems.isEmpty()) {
            Menu m = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
            m.getItems().addAll(items);
            items.clear();
            items.addAll(opItems);
            items.add(new SeparatorMenuItem());
            items.add(m);
        }
        return items;
    }

    @FXML
    @Override
    public void addAction() {
        if (manager == null) {
            return;
        }
        if (!manager.checkBeforeNextAction()) {
            return;
        }
        if (loadedParent != null) {
            manager.nodeController.attributesController.parentNode = loadedParent;
        }
        manager.addNode();
    }

    @FXML
    @Override
    public void editAction() {
        if (manager == null) {
            return;
        }
//        manager.editNode(selectedItem());
    }

    @FXML
    @Override
    public void pasteAction() {
        if (manager == null) {
            return;
        }
//        manager.pasteNode(selectedItem());
    }

    public void queryTimes(String value, String title) {
        if (value == null) {
            popError(message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = value;
        queryConditionsString = title;
        loadTableData();

//        dataController.showNodesList(true);
    }

    public void queryTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        clearQuery();
        queryConditions = tableTree.tagsCondition(tags);
        queryConditionsString = message("Tag") + ": ";
        for (Tag tag : tags) {
            queryConditionsString += " " + tag.getTag();
        }
        loadTableData();
//        dataController.showNodesList(true);
    }

    public void find(String s, boolean isName) {
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Find"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(message("InvalidParameters") + ": " + message("Find"));
            return;
        }
        TableStringValues.add(baseName + "Histories", s);
        clearQuery();
        if (isName) {
            queryConditions = null;
            queryConditionsString = message("Title") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( title like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }

        } else {
            queryConditions = null;
            queryConditionsString = message("Contents") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( info like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }
        }
        loadTableData();
//
//        dataController.showNodesList(true);
    }

}
