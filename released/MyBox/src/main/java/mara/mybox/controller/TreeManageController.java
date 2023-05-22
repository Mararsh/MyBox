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
import javafx.event.Event;
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
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeManageController extends BaseSysTableController<InfoNode> {

    protected String category;
    protected TableTreeNode tableTreeNode;
    protected TableTag tableTag;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String queryLabel;
    protected String nameMsg, valueMsg, moreMsg, timeMsg;
    protected InfoNode loadedParent;

    @FXML
    protected ControlInfoTreeManage nodesController;
    @FXML
    protected TableColumn<InfoNode, Long> nodeidColumn;
    @FXML
    protected TableColumn<InfoNode, String> nameColumn, valueColumn, moreColumn;
    @FXML
    protected TableColumn<InfoNode, Date> timeColumn;
    @FXML
    protected VBox conditionBox, timesBox;
    @FXML
    protected ToggleGroup nodesGroup;
    @FXML
    protected RadioButton childrenRadio, descendantsRadio, findNameRadio, findValueRadio;
    @FXML
    protected FlowPane tagsPane, namesPane, nodeGroupPane;
    @FXML
    protected Label conditionLabel;
    @FXML
    protected TreeNodeEditor nodeController;
    @FXML
    protected Button refreshTimesButton, queryTimesButton;
    @FXML
    protected TreeTagsController tagsController;
    @FXML
    protected ControlTimesTree timesController;
    @FXML
    protected TextField findInput;
    @FXML
    protected CheckBox nodesListCheck;
    @FXML
    protected SplitPane managePane;
    @FXML
    protected VBox nodesListBox;

    public TreeManageController() {
        baseTitle = message("InformationInTree");
        category = InfoNode.InformationInTree;
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
    public void initControls() {
        try {
            super.initControls();

            nodesController.setParameters(this);

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

            nodeController.setParameters(this);
            tagsController.setParameters(this);
            tagsController.loadTableData();

            initTimes();
            initFind();

            if (nodesListCheck != null) {
                nodesListCheck.setSelected(UserConfig.getBoolean(baseName + "NodesList", false));
                showNodesList(nodesListCheck.isSelected());
                nodesListCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        UserConfig.setBoolean(baseName + "NodesList", nodesListCheck.isSelected());
                        showNodesList(nodesListCheck.isSelected());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            loadTree();
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

    public void showNodesList(boolean show) {
        if (isSettingValues || nodesListCheck == null) {
            return;
        }
        isSettingValues = true;
        nodesListCheck.setSelected(show);
        if (show) {
            if (!managePane.getItems().contains(nodesListBox)) {
                managePane.getItems().add(1, nodesListBox);
            }
        } else {
            if (managePane.getItems().contains(nodesListBox)) {
                managePane.getItems().remove(nodesListBox);
            }
        }
        isSettingValues = false;
        if (show) {
            leftPaneCheck.setSelected(true);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (nodeController != null) {
                return nodeController.keyEventsFilter(event); // pass event to editor
            }
            return false;
        } else {
            return true;
        }
    }

    /*
        synchronize
     */
    public void nodeAdded(InfoNode parent, InfoNode newNode) {
        if (parent == null || newNode == null) {
            return;
        }
        if (loadedParent != null && parent.getNodeid() == loadedParent.getNodeid()) {
            loadNodes(parent);
        }
    }

    public void nodeRenamed(InfoNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();
        if (loadedParent != null && id == loadedParent.getNodeid()) {
            loadedParent = node;
            makeConditionPane();
        } else {
            for (int i = 0; i < tableData.size(); i++) {
                InfoNode tnode = tableData.get(i);
                if (tnode.getNodeid() == id) {
                    tableData.set(i, node);
                    break;
                }
            }
        }
        if (nodeController.parentNode != null && id == nodeController.parentNode.getNodeid()) {
            nodeController.setParentNode(node);
        }
        if (nodeController.currentNode != null && id == nodeController.currentNode.getNodeid()) {
            nodeController.renamed(node.getTitle());
        }
    }

    public void nodeDeleted(InfoNode node) {
        if (node == null) {
            return;
        }
        long id = node.getNodeid();
        if (loadedParent != null && id == loadedParent.getNodeid()) {
            loadedParent = null;
            makeConditionPane();
            tableData.clear();
        } else {
            for (int i = 0; i < tableData.size(); i++) {
                InfoNode tnode = tableData.get(i);
                if (tnode.getNodeid() == id) {
                    tableData.remove(tnode);
                    break;
                }
            }
        }
        if (nodeController.parentNode != null && id == nodeController.parentNode.getNodeid()) {
            nodeController.setParentNode(null);
            nodeController.copyNode();
        }
        if (nodeController.currentNode != null && id == nodeController.currentNode.getNodeid()) {
            nodeController.copyNode();
        }
    }

    public void nodeMoved(InfoNode parent, InfoNode node) {
        if (parent == null || node == null) {
            return;
        }
        long id = node.getNodeid();
        if (loadedParent != null) {
            loadNodes(loadedParent);
        }
        if (nodeController.currentNode != null && id == nodeController.currentNode.getNodeid()) {
            nodeController.setParentNode(parent);
        }
        if (nodeController.parentNode != null && id == nodeController.parentNode.getNodeid()) {
            nodeController.setParentNode(node);
        }
    }

    public void nodesMoved(InfoNode parent, List<InfoNode> nodes) {
        if (parent == null || nodes == null || nodes.isEmpty()) {
            return;
        }
        loadNodes(loadedParent);
        nodesController.loadTree();
    }

    public void nodesCopied(InfoNode parent) {
        nodesController.updateNode(parent);
    }

    public void nodesDeleted() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    loadedParent = tableTreeNode.readData(conn, loadedParent);
                    nodeController.currentNode = tableTreeNode.readData(conn, nodeController.currentNode);
                    nodeController.parentNode = tableTreeNode.readData(conn, nodeController.parentNode);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                nodeController.editNode(nodeController.currentNode);
                nodesController.loadTree(loadedParent);
            }
        };
        start(task);
    }

    public void nodeSaved() {
        if (nodeController.currentNode == null) {
            return;
        }
        long id = nodeController.currentNode.getNodeid();
        if (loadedParent != null && id == loadedParent.getNodeid()) {
            loadedParent = nodeController.currentNode;
            makeConditionPane();
        }
        for (int i = 0; i < tableData.size(); i++) {
            InfoNode tnode = tableData.get(i);
            if (tnode.getNodeid() == id) {
                tableData.set(i, nodeController.currentNode);
                break;
            }
        }
        nodesController.updateNode(nodeController.parentNode);
    }

    public void newNodeSaved() {
        if (nodeController.currentNode == null) {
            return;
        }
        nodesController.addNewNode(nodesController.find(nodeController.parentNode), nodeController.currentNode, false);
        if (loadedParent != null && nodeController.parentNode.getNodeid() == loadedParent.getNodeid()) {
            loadNodes(nodeController.parentNode);
        }
    }

    public void nodeChanged() {
        if (isSettingValues) {
            return;
        }
        String currentTitle = getTitle();
        if (nodeController.nodeChanged) {
            if (!currentTitle.endsWith(" *")) {
                setTitle(currentTitle + " *");
            }
        } else {
            if (currentTitle.endsWith(" *")) {
                setTitle(currentTitle.substring(0, currentTitle.length() - 2));
            }
        }
    }


    /*
        tree
     */
    public void loadTree() {
        try {
            if (tableTreeNode.categoryEmpty(category)) {
                File file = InfoNode.exampleFile(category);
                if (file == null) {
                    return;
                }
                if (AppVariables.isTesting
                        || PopTools.askSure(getTitle(), message("ImportExamples") + ": " + message(category))) {
                    nodesController.importExamples();
                    return;
                }
            }
            nodesController.loadTree();
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

    /*
        table
     */
    public void loadNodes(InfoNode parentNode) {
        showNodesList(true);
        clearQuery();
        loadedParent = parentNode;
        if (loadedParent != null) {
            queryConditions = " category='" + category + "' AND "
                    + "parentid=" + loadedParent.getNodeid() + " AND nodeid<>parentid";
            loadTableData();
        }
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
        SingletonTask bookTask = new SingletonTask<Void>(this) {
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
        start(bookTask, false);
    }

    @Override
    public long readDataSize(Connection conn) {
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
    public List<InfoNode> readPageData(Connection conn) {
        if (loadedParent != null && descendantsRadio.isSelected()) {
            return tableTreeNode.decentants(conn, loadedParent.getNodeid(), startRowOfCurrentPage, pageSize);

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
    protected void afterDeletion() {
        super.afterDeletion();
        nodesDeleted();
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        nodesDeleted();
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (pasteButton != null) {
                menu = new MenuItem(message("Paste"), StyleTools.getIconImageView("iconPaste.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pasteAction();
                });
                menu.setDisable(pasteButton.isDisabled());
                items.add(menu);
            }

            menu = new MenuItem(message("Move"), StyleTools.getIconImageView("iconRef.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveAction();
            });
            menu.setDisable(moveDataButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyAction();
            });
            menu.setDisable(copyButton.isDisabled());
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

        boolean none = isNoneSelected();
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
        if (!checkBeforeNextAction()) {
            return;
        }
        if (loadedParent != null) {
            nodeController.parentNode = loadedParent;
        }
        editNode(null);
    }

    @FXML
    @Override
    public void editAction() {
        editNode(selectedItem());
    }

    public void editNode(InfoNode node) {
        if (!checkBeforeNextAction()) {
            return;
        }
        nodeController.editNode(node);
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
        pasteNode(selectedItem());
    }

    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        String v = node.getValue();
        if (v == null || v.isBlank()) {
            return;
        }
        nodeController.pasteText(v);
    }

    public void executeNode(InfoNode node) {
        if (node == null) {
            return;
        }
        String v = node.getValue();
        if (v == null || v.isBlank()) {
            return;
        }
        editNode(node);
        if (nodeController.startButton != null) {
            nodeController.startAction();
        } else if (nodeController.goButton != null) {
            nodeController.goAction();
        } else if (startButton != null) {
            startAction();
        } else if (goButton != null) {
            goAction();
        }
    }


    /*
        node
     */
    @FXML
    protected void addNode() {
        editNode(null);
    }

    @FXML
    protected void copyNode() {
        nodeController.copyNode();
    }

    @FXML
    protected void recoverNode() {
        nodeController.editNode(nodeController.currentNode);
    }

    @FXML
    @Override
    public void saveAction() {
        nodeController.saveNode();
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null || !file.exists() || !checkBeforeNextAction()) {
            return;
        }
        nodeController.loadFile(file);
    }

    public boolean isNodeChanged() {
        return nodeController.nodeChanged;
    }


    /*
        Times
     */
    public void initTimes() {
        try {
            timesController.setParent(this, " category='" + category + "' ", "Tree_Node", "update_time");

            timesController.queryNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    queryTimes();
                }
            });
            timesController.refreshNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
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
        timesController.loadTree();
    }

    @FXML
    protected void queryTimes() {
        String c = timesController.check();
        if (c == null) {
            popError(message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = " category='" + category + "' " + (c.isBlank() ? "" : " AND " + c);
        queryConditionsString = timesController.getFinalTitle();
        loadTableData();
        showNodesList(true);
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
                queryConditions += " ( value like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }
        }
        queryConditions = " category='" + category + "' AND " + queryConditions;
        loadTableData();
        showNodesList(true);
    }

    @FXML
    protected void showFindHistories(Event event) {
        PopTools.popStringValues(this, findInput, event, baseName + category + "Histories", false, true);
    }

    @FXML
    public void popFindHistories(Event event) {
        if (UserConfig.getBoolean(baseName + category + "HistoriesPopWhenMouseHovering", false)) {
            showFindHistories(event);
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!isNodeChanged()) {
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

    /*
        static methods
     */
    public static TreeManageController oneOpen() {
        TreeManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TreeManageController) {
                try {
                    controller = (WebFavoritesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeManageController) WindowTools.openStage(Fxmls.TreeManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
