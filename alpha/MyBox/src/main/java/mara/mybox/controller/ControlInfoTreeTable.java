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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.data.Tag;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTreeNode;
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
public class ControlInfoTreeTable extends BaseSysTableController<InfoNode> {

    protected BaseInfoTreeController infoController;
    protected InfoTreeManageController manager;
    protected TableTreeNode tableTreeNode;
    protected String queryLabel;
    protected InfoNode loadedParent;

    @FXML
    protected TableColumn<InfoNode, Long> nodeidColumn;
    @FXML
    protected TableColumn<InfoNode, String> nameColumn, valueColumn;
    @FXML
    protected TableColumn<InfoNode, Date> timeColumn;
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
    protected HBox buttonsBox;

    @Override
    public void setTableDefinition() {

    }

    public void setParameters(BaseInfoTreeController controller) {
        try {
            infoController = controller;
            baseName = infoController.baseName;
            tableTreeNode = infoController.tableTreeNode;
            tableDefinition = infoController.tableTreeNode;

            if (infoController instanceof InfoTreeManageController) {
                manager = (InfoTreeManageController) infoController;
            }
            if (manager == null) {
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                thisPane.getChildren().remove(buttonsBox);
            }

            nodeidColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            nameColumn.setText(infoController.nameMsg);
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            valueColumn.setText(infoController.valueMsg);
            if (InfoNode.WebFavorite.equals(infoController.category)) {
                valueColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
            } else {
                valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            }
            valueColumn.setCellFactory(new TableTextTrimCell());
            timeColumn.setText(infoController.timeMsg);
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

    public void loadNodes(InfoNode parentNode) {
        clearQuery();
        loadedParent = parentNode;
        if (loadedParent != null) {
            queryConditions = " category='" + infoController.category + "' AND "
                    + "parentid=" + loadedParent.getNodeid() + " AND nodeid<>parentid";
            loadTableData();
        }
        infoController.showNodesList(true);
    }

    public void loadChildren(InfoNode parentNode) {
        childrenRadio.setSelected(true);
        loadNodes(parentNode);
    }

    public void loadDescendants(InfoNode parentNode) {
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
            private List<InfoNode> ancestor;

            @Override
            protected boolean handle() {
                ancestor = tableTreeNode.ancestor(loadedParent.getNodeid());
                return true;
            }

            @Override
            protected void whenSucceeded() {
                List<Node> nodes = new ArrayList<>();
                if (ancestor != null) {
                    for (InfoNode node : ancestor) {
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
                return tableTreeNode.decentantsSize(conn, loadedParent.getNodeid()) + 1;
            } else {
                return tableTreeNode.conditionSize(conn, queryConditions) + 1;
            }

        } else if (queryConditions != null) {
            return tableTreeNode.conditionSize(conn, queryConditions);

        } else {
            return 0;
        }

    }

    @Override
    public List<InfoNode> readPageData(FxTask currentTask, Connection conn) {
        if (loadedParent != null && descendantsRadio.isSelected()) {
            return tableTreeNode.decentants(conn, loadedParent.getNodeid(), startRowOfCurrentPage, pageSize);

        } else if (queryConditions != null) {
            return tableTreeNode.queryConditions(conn, queryConditions, orderColumns, startRowOfCurrentPage, pageSize);

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
        InfoNode item = selectedItem();
        if (item == null) {
            popError(message("SelectToHanlde"));
            return;
        }
        if (infoController instanceof ControlInfoTreeHandler) {
            ((ControlInfoTreeHandler) infoController).viewNode(item);
        } else {
            infoController.popNode(item);
        }

    }

    @Override
    protected long clearData(FxTask currentTask) {
        if (queryConditions != null) {
            return tableTreeNode.deleteCondition(queryConditions);

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
    public List<MenuItem> operationsMenuItems(Event fevent) {
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

            if (manager != null) {
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
            }

            items.add(new SeparatorMenuItem());

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
    protected List<MenuItem> makeTableContextMenu() {
        return operationsMenuItems(null);
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
            manager.editor.attributesController.parentNode = loadedParent;
        }
        manager.editNode(null);
    }

    @FXML
    @Override
    public void editAction() {
        if (manager == null) {
            return;
        }
        manager.editNode(selectedItem());
    }

    @FXML
    @Override
    public void pasteAction() {
        if (manager == null) {
            return;
        }
        manager.pasteNode(selectedItem());
    }

    public void queryTimes(String value, String title) {
        if (value == null) {
            popError(message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = " category='" + infoController.category + "' " + (value.isBlank() ? "" : (" AND " + value));
        queryConditionsString = title;
        loadTableData();

        infoController.showNodesList(true);
    }

    public void queryTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        clearQuery();
        queryConditions = " category='" + infoController.category + "' AND "
                + tableTreeNode.tagsCondition(tags);
        queryConditionsString = message("Tag") + ": ";
        for (Tag tag : tags) {
            queryConditionsString += " " + tag.getTag();
        }
        loadTableData();
        infoController.showNodesList(true);
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
        TableStringValues.add(baseName + infoController.category + "Histories", s);
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
        queryConditions = " category='" + infoController.category + "' AND " + queryConditions;
        loadTableData();

        infoController.showNodesList(true);
    }

}
