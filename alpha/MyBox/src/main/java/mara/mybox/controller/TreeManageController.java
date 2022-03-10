package mara.mybox.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeLeaf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public abstract class TreeManageController extends BaseSysTableController<TreeLeaf> {

    protected String category;
    protected TableTree tableTree;
    protected TreeNode treeNode, nodeOfCurrentLeaf;
    protected TableTreeLeaf tableTreeLeaf;
    protected String queryLabel;
    protected TreeLeaf currentLeaf;
    protected String nameMsg, valueMsg, moreMsg, timeMsg;

    @FXML
    protected TreeNodesController nodesController;
    @FXML
    protected TableColumn<TreeLeaf, Long> leafidColumn;
    @FXML
    protected TableColumn<TreeLeaf, String> nameColumn, valueColumn, moreColumn;
    @FXML
    protected TableColumn<TreeLeaf, Date> timeColumn;
    @FXML
    protected VBox conditionBox;
    @FXML
    protected CheckBox subCheck;
    @FXML
    protected FlowPane namesPane;
    @FXML
    protected TextField idInput, nameInput, valueInput, moreInput, timeInput;
    @FXML
    protected Label nameLabel, valueLabel, moreLabel, timeLabel, conditionLabel, chainLabel;

    public TreeManageController() {
        baseTitle = message("Tree");
        category = "Root";
        nameMsg = message("Name");
        valueMsg = message("Value");
        moreMsg = message("More");
        timeMsg = message("UpdateTime");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setTableDefinition() {
        tableTree = new TableTree();
        tableTreeLeaf = new TableTreeLeaf();
        tableDefinition = tableTreeLeaf;
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            leafidColumn.setCellValueFactory(new PropertyValueFactory<>("leafid"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setText(nameMsg);
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setText(valueMsg);
            if (moreColumn != null) {
                moreColumn.setCellValueFactory(new PropertyValueFactory<>("more"));
                moreColumn.setText(moreMsg);
            }
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            timeColumn.setText(timeMsg);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            nodesController.setParameters(this, true);
            super.initControls();

            nameLabel.setText(nameMsg);
            valueLabel.setText(valueMsg);
            if (moreLabel != null) {
                moreLabel.setText(moreMsg);
            }
            timeLabel.setText(timeMsg);

            nodesController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    loadLeaves(nodesController.selectedNode);
                }
            });

            nodesController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    leafChanged(nodesController.changedNode);
                }
            });

            subCheck.setSelected(UserConfig.getBoolean(baseName + "IncludeSub", false));
            subCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    if (nodesController.selectedNode != null) {
                        loadTableData();
                    }
                }
            });

            goButton.disableProperty().bind(Bindings.isEmpty(nameInput.textProperty()));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

//            NodeStyleTools.setTooltip(deleteButton, new Tooltip(message("DeleteRows")));
//            NodeStyleTools.setTooltip(deleteButton, new Tooltip(message("DeleteRows")));
//            NodeStyleTools.setTooltip(deleteButton, new Tooltip(message("DeleteRows")));
//            NodeStyleTools.setTooltip(deleteButton, new Tooltip(message("DeleteRows")));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            loadTree(null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        tree
     */
    protected void loadTree(TreeNode node) {
        if (!AppVariables.isTesting && tableTreeLeaf.size() < 1
                && PopTools.askSure(this, getBaseTitle(), message("ImportExamples"))) {
            nodesController.importExamples();
        } else {
            nodesController.loadTree(node);
        }
    }

    protected void clearQuery() {
        nodesController.selectedNode = null;
        queryConditions = null;
        queryLabel = null;
        tableData.clear();
        conditionBox.getChildren().clear();
        namesPane.getChildren().clear();
        startRowOfCurrentPage = 0;
    }

    protected void loadLeaves(TreeNode node) {
        clearQuery();
        nodesController.selectedNode = node;
        if (node != null) {
            queryConditions = " parentid=" + node.getNodeid();
            loadTableData();
        }
    }

    protected void leafChanged(TreeNode node) {
        if (node == null) {
            return;
        }
        makeConditionPane();
        if (nodeOfCurrentLeaf != null && node.getNodeid() == nodeOfCurrentLeaf.getNodeid()) {
            nodeOfCurrentLeaf = node;
            updateNodeOfCurrentLeaf();
        }
        if (nodesController.selectedNode != null && nodesController.selectedNode.getNodeid() == node.getNodeid()) {
            nodesController.selectedNode = node;
            makeConditionPane();
        }
    }


    /*
        addresses list
     */
    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        makeConditionPane();
    }

    public void makeConditionPane() {
        conditionBox.getChildren().clear();
        if (nodesController.selectedNode == null) {
            if (queryConditionsString != null) {
                conditionLabel.setText(queryConditionsString);
                conditionBox.getChildren().add(conditionLabel);
            }
            conditionBox.applyCss();
            return;
        }
        synchronized (this) {
            SingletonTask bookTask = new SingletonTask<Void>(this) {
                private List<TreeNode> ancestor;

                @Override
                protected boolean handle() {
                    ancestor = tableTree.ancestor(nodesController.selectedNode.getNodeid());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    List<Node> nodes = new ArrayList<>();
                    if (ancestor != null) {
                        for (TreeNode node : ancestor) {
                            Hyperlink link = new Hyperlink(node.getTitle());
                            link.setWrapText(true);
                            link.setMinHeight(Region.USE_PREF_SIZE);
                            link.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    loadLeaves(node);
                                }
                            });
                            nodes.add(link);
                            nodes.add(new Label(">"));
                        }
                    }
                    Label label = new Label(nodesController.selectedNode.getTitle());
                    label.setWrapText(true);
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    nodes.add(label);
                    namesPane.getChildren().setAll(nodes);
                    conditionBox.getChildren().setAll(namesPane, subCheck);
                    conditionBox.applyCss();
                }
            };
            start(bookTask, false);
        }

    }

    @Override
    public long readDataSize() {
        if (nodesController.selectedNode != null && subCheck.isSelected()) {
            return TableTreeLeaf.withSubSize(tableTree, nodesController.selectedNode.getNodeid());

        } else if (queryConditions != null) {
            return tableTreeLeaf.conditionSize(queryConditions);

        } else {
            return 0;
        }

    }

    @Override
    public List<TreeLeaf> readPageData() {
        if (nodesController.selectedNode != null && subCheck.isSelected()) {
            return tableTreeLeaf.withSub(tableTree, nodesController.selectedNode.getNodeid(), startRowOfCurrentPage, pageSize);

        } else if (queryConditions != null) {
            return tableTreeLeaf.queryConditions(queryConditions, orderColumns, startRowOfCurrentPage, pageSize);

        } else {
            return null;
        }
    }

    @Override
    protected long clearData() {
        if (queryConditions != null) {
            return tableTreeLeaf.deleteCondition(queryConditions);

        } else {
            return -1;
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Move"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveAction();
            });
            menu.setDisable(moveDataButton.isDisabled());
            items.add(menu);

            items.addAll(super.makeTableContextMenu());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void itemClicked() {
        editAction();
    }

    @Override
    public void itemDoubleClicked() {
        currentLeaf = tableView.getSelectionModel().getSelectedItem();
        if (currentLeaf != null) {

        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        deleteButton.setDisable(none);
        copyButton.setDisable(none);
        moveDataButton.setDisable(none);
    }

    @FXML
    @Override
    public void addAction() {
        nodeOfCurrentLeaf = nodesController.selectedNode;
        editLeaf(null);
    }

    @FXML
    @Override
    public void editAction() {
        editLeaf(tableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    @Override
    public void copyAction() {
        TreeLeavesCopyController.oneOpen(this);
    }

    @FXML
    protected void moveAction() {
        TreeLeavesMoveController.oneOpen(this);
    }

    /*
        edit address
     */
    @FXML
    protected void addLeaf() {
        editLeaf(null);
    }

    protected void editLeaf(TreeLeaf leaf) {
        synchronized (this) {
            currentLeaf = leaf;
            if (leaf != null) {
                idInput.setText(leaf.getLeafid() + "");
                nameInput.setText(leaf.getName());
                valueInput.setText(leaf.getValue());
                if (moreInput != null) {
                    moreInput.setText(leaf.getMore());
                }
                timeInput.setText(DateTools.datetimeToString(leaf.getTime()));
            } else {
                idInput.setText("");
                nameInput.setText("");
                valueInput.setText("");
                if (moreInput != null) {
                    moreInput.setText("");
                }
                timeInput.setText("");
            }
            updateNodeOfCurrentLeaf();
            showRightPane();
        }
    }

    protected void updateNodeOfCurrentLeaf() {
        synchronized (this) {
            SingletonTask updateTask = new SingletonTask<Void>(this) {
                private String chainName;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (currentLeaf != null) {
                            if (nodeOfCurrentLeaf == null || nodeOfCurrentLeaf.getNodeid() != currentLeaf.getParentid()) {
                                nodeOfCurrentLeaf = tableTree.find(conn, currentLeaf.getParentid());
                            }
                        }
                        if (nodeOfCurrentLeaf == null) {
                            nodeOfCurrentLeaf = nodesController.root(conn);
                        }
                        if (nodeOfCurrentLeaf == null) {
                            return false;
                        }
                        chainName = nodesController.chainName(conn, nodeOfCurrentLeaf);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    chainLabel.setText(chainName);
                }
            };
            start(updateTask, false);
        }
    }

    @FXML
    protected void copyLeaf() {
        idInput.setText("");
        nameInput.appendText(" " + message("Copy"));
        currentLeaf = null;
    }

    @FXML
    protected void recoverLeaf() {
        editLeaf(currentLeaf);
    }

    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String name = nameInput.getText();
            if (name == null || name.isBlank()) {
                popError(message("InvalidData") + ": " + message("Title"));
                return;
            }
            String value = valueInput.getText();
            if ("WebFavorites".equals(category)) {
                try {
                    URL url = new URL(value);
                } catch (Exception e) {
                    popError(message("InvalidData") + ": " + message("Address"));
                    return;
                }
            }
            String more = moreInput != null ? moreInput.getText() : null;
            task = new SingletonTask<Void>(this) {
                private TreeLeaf data;
                private boolean notExist = false;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        data = new TreeLeaf();
                        data.setName(name);
                        data.setValue(value);
                        data.setMore(more);
                        data.setTime(new Date());
                        if (currentLeaf != null) {
                            currentLeaf = tableTreeLeaf.readData(conn, currentLeaf);
                            nodeOfCurrentLeaf = tableTree.readData(conn, nodeOfCurrentLeaf);
                            if (currentLeaf == null || nodeOfCurrentLeaf == null) {
                                notExist = true;
                                currentLeaf = null;
                                return true;
                            } else {
                                data.setLeafid(currentLeaf.getLeafid());
                                data.setParentid(currentLeaf.getParentid());
                                currentLeaf = tableTreeLeaf.updateData(conn, data);
                            }
                        } else {
                            if (nodeOfCurrentLeaf == null) {
                                nodeOfCurrentLeaf = nodesController.root(conn);
                            }
                            data.setParentid(nodeOfCurrentLeaf.getNodeid());
                            currentLeaf = tableTreeLeaf.insertData(conn, data);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return currentLeaf != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (notExist) {
                        copyLeaf();
                        popError(message("NotExist"));
                    } else {
                        popSaved();
                        editLeaf(currentLeaf);
                        if (nodesController.selectedNode != null
                                && currentLeaf.getParentid() == nodesController.selectedNode.getNodeid()) {
                            refreshAction();
                        }
                    }
                }

            };
            start(task, false);
        }
    }

}
