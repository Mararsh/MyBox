package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeManageController extends BaseSysTableController<TreeNode> {

    protected String category;
    protected TreeNode treeNode, parentNode;
    protected TableTreeNode tableTreeNode;
    protected TableTag tableTag;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String queryLabel;
    protected TreeNode currentNode;
    protected String nameMsg, valueMsg, moreMsg, timeMsg;

    @FXML
    protected TreeNodesController nodesController;
    @FXML
    protected TableColumn<TreeNode, Long> nodeidColumn;
    @FXML
    protected TableColumn<TreeNode, String> nameColumn, valueColumn, moreColumn;
    @FXML
    protected TableColumn<TreeNode, Date> timeColumn;
    @FXML
    protected VBox conditionBox, timesBox;
    @FXML
    protected CheckBox subCheck;
    @FXML
    protected FlowPane tagsPane, namesPane;
    @FXML
    protected Label conditionLabel;
    @FXML
    protected TreeNodeEditor nodeController;
    @FXML
    protected Button refreshTimesButton, queryTimesButton;
    @FXML
    protected TreeTagsController tagsController;
    @FXML
    protected ControlTimeTree timeController;
    @FXML
    protected TextField findInput;
    @FXML
    protected RadioButton findNameRadio, findValueRadio;

    public TreeManageController() {
        baseTitle = message("InformationInTree");
        category = TreeNode.InformationInTree;
        nameMsg = message("Title");
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
        tableTreeNode = new TableTreeNode();
        tableTag = new TableTag();
        tableTreeNodeTag = new TableTreeNodeTag();
        tableDefinition = tableTreeNode;
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            nodeidColumn.setCellValueFactory(new PropertyValueFactory<>("nodeid"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            nameColumn.setText(nameMsg);
            if (valueColumn != null) {
                valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
                valueColumn.setText(valueMsg);
            }
            if (moreColumn != null) {
                moreColumn.setCellValueFactory(new PropertyValueFactory<>("more"));
                moreColumn.setText(moreMsg);
            }
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setText(timeMsg);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            nodesController.setParameters(this, true);
            nodesController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    loadLeaves(nodesController.selectedNode);
                }
            });
            nodesController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    nodeChanged(nodesController.changedNode);
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

            nodeController.setParameters(this);
            tagsController.setParameters(this);
            tagsController.loadTableData();

            initTimes();
            initFind();

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

    @Override
    public boolean controlAltG() {
        if (nodeController.valueInput.isFocused()) {
            nodeController.clearValue();
        } else {
            clearAction();
        }
        return true;
    }

    /*
        nodes
     */
    protected void loadTree(TreeNode node) {
        if (!AppVariables.isTesting) {
            File file = TreeNode.exampleFile(category);
            if (file != null && tableTreeNode.size(category) < 1
                    && PopTools.askSure(this, getBaseTitle(), message("ImportExamples"))) {
                nodesController.importExamples();
                return;
            }
        }
        nodesController.loadTree(node);
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

    protected void nodeChanged(TreeNode node) {
        if (node == null) {
            return;
        }
        makeConditionPane();
        if (parentNode != null && node.getNodeid() == parentNode.getNodeid()) {
            parentNode = node;
            nodeController.updateParentNode();
        }
        if (nodesController.selectedNode != null && nodesController.selectedNode.getNodeid() == node.getNodeid()) {
            nodesController.selectedNode = node;
            makeConditionPane();
        }
    }


    /*
        leaves
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
                conditionLabel.setText(queryConditionsString.length() > 300
                        ? queryConditionsString.substring(0, 300) : queryConditionsString);
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
                    ancestor = tableTreeNode.ancestor(nodesController.selectedNode.getNodeid());
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
    public long readDataSize(Connection conn) {
        if (nodesController.selectedNode != null && subCheck.isSelected()) {
            return tableTreeNode.withSubSize(conn, nodesController.selectedNode.getNodeid());

        } else if (queryConditions != null) {
            return tableTreeNode.conditionSize(conn, queryConditions);

        } else {
            return 0;
        }

    }

    @Override
    public List<TreeNode> readPageData(Connection conn) {
        if (nodesController.selectedNode != null && subCheck.isSelected()) {
            return tableTreeNode.withSub(conn, nodesController.selectedNode.getNodeid(), startRowOfCurrentPage, pageSize);

        } else if (queryConditions != null) {
            return tableTreeNode.queryConditions(conn, queryConditions, orderColumns, startRowOfCurrentPage, pageSize);

        } else {
            return null;
        }
    }

    @Override
    protected long clearData() {
        if (queryConditions != null) {
            return tableTreeNode.deleteCondition(queryConditions);

        } else {
            return -1;
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (pasteButton != null) {
                menu = new MenuItem(message("Paste"), StyleTools.getIconImage("iconPaste.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pasteAction();
                });
                menu.setDisable(pasteButton.isDisabled());
                items.add(menu);
            }

            menu = new MenuItem(message("Move"), StyleTools.getIconImage("iconRef.png"));
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
        if (pasteButton != null) {
            pasteButton.setDisable(none);
        }
    }

    @FXML
    @Override
    public void addAction() {
        addNode();
    }

    @FXML
    @Override
    public void editAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        nodeController.editNode(tableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    @Override
    public void copyAction() {
        TreeNodesCopyController.oneOpen(this);
    }

    @FXML
    protected void moveAction() {
        TreeNodesMoveController.oneOpen(this);
    }

    @FXML
    @Override
    public void pasteAction() {
        TreeNode selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        nodeController.pasteText(selected.getValue());
    }

    /*
        node
     */
    @FXML
    protected void addNode() {
        if (!checkBeforeNextAction()) {
            return;
        }
        nodeController.editNode(null);
    }

    @FXML
    protected void copyNode() {
        nodeController.copyNode();
    }

    @FXML
    protected void recoverNode() {
        nodeController.editNode(currentNode);
    }

    @FXML
    @Override
    public void saveAction() {
        TreeNode node = nodeController.pickNodeData();
        if (node == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonTask<Void>(this) {
            private boolean notExist = false;

            @Override
            protected boolean handle() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    node.setUpdateTime(new Date());
                    if (currentNode != null) {
                        currentNode = tableTreeNode.readData(conn, currentNode);
                        parentNode = tableTreeNode.readData(conn, parentNode);
                        if (currentNode == null || parentNode == null) {
                            notExist = true;
                            currentNode = null;
                            return true;
                        } else {
                            node.setNodeid(currentNode.getNodeid());
                            node.setParentid(currentNode.getParentid());
                            currentNode = tableTreeNode.updateData(conn, node);
                        }
                    } else {
                        if (parentNode == null) {
                            parentNode = nodesController.root(conn);
                        }
                        node.setParentid(parentNode.getNodeid());
                        currentNode = tableTreeNode.insertData(conn, node);
                    }
                    if (currentNode == null) {
                        return false;
                    }
                    long nodeid = currentNode.getNodeid();
                    List<String> nodeTags = tableTreeNodeTag.nodeTagNames(nodeid);
                    List<Tag> selected = nodeController.tableView.getSelectionModel().getSelectedItems();
                    if (selected == null || selected.isEmpty()) {
                        tableTreeNodeTag.removeTags(conn, nodeid);
                    } else {
                        List<String> selectedNames = new ArrayList<>();
                        for (Tag tag : selected) {
                            selectedNames.add(tag.getTag());
                        }
                        List<String> items = new ArrayList<>();
                        for (String tagName : selectedNames) {
                            if (!nodeTags.contains(tagName)) {
                                items.add(tagName);
                            }
                        }
                        tableTreeNodeTag.addTags(conn, nodeid, category, items);
                        items.clear();
                        for (String tagName : nodeTags) {
                            if (!selectedNames.contains(tagName)) {
                                items.add(tagName);
                            }
                        }
                        tableTreeNodeTag.removeTags(conn, nodeid, category, items);
                    }
                    conn.commit();
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
                return currentNode != null;
            }

            @Override
            protected void whenSucceeded() {
                if (notExist) {
                    copyNode();
                    popError(message("NotExist"));
                } else {
                    popSaved();
                    afterSaved();
                }
            }

        };
        start(task, false);
    }

    public void afterSaved() {
        nodeController.editNode(currentNode);
        if (nodesController.selectedNode != null
                && currentNode.getParentid() == nodesController.selectedNode.getNodeid()) {
            refreshAction();
        }
    }

    public boolean nodeChanged() {
        return nodeController.nodeChanged;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!nodeChanged()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("DataChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null || !file.exists() || !checkBeforeNextAction()) {
            return;
        }
        nodeController.loadFile(file);
    }

    /*
        Times
     */
    public void initTimes() {
        try {
            timeController.setParent(this, false);

            timeController.queryNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    queryTimes();
                }
            });
            timeController.refreshNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    refreshTimes();
                }
            });

            refreshTimes();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void refreshTimes() {
        synchronized (this) {
            timeController.clearTree();
            timesBox.setDisable(true);
            SingletonTask timesTask = new SingletonTask<Void>(this) {
                private List<Date> times;

                @Override
                protected boolean handle() {
                    times = tableTreeNode.times(category);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    timeController.loadTree("update_time", times, false);
                }

                @Override
                protected void finalAction() {
                    timesBox.setDisable(false);
                }

            };
            start(timesTask, false);
        }
    }

    @FXML
    protected void queryTimes() {
        String c = timeController.check();
        if (c == null) {
            popError(message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = c;
        queryConditionsString = timeController.getFinalTitle();
        loadTableData();
    }

    /*
        find
     */
    public void initFind() {
        try {
            findNameRadio.setText(nameMsg);
            findValueRadio.setText(valueMsg);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void find() {
        String s = findInput.getText();
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Find"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(message("InvalidParameters") + ": " + message("Find"));
            return;
        }
        TableStringValues.add(baseName + category + "Histories", s);
        clearQuery();
        if (findNameRadio.isSelected()) {
            queryConditions = null;
            queryConditionsString = message("Title") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( name like '%" + DerbyBase.stringValue(v) + "%' ) ";
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
                queryConditions += " ( value like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }
        }
        loadTableData();
    }

    @FXML
    protected void popFindHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, findInput, mouseEvent, baseName + category + "Histories");
    }

}
