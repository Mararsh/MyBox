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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
import mara.mybox.fxml.cell.TableTextTrimCell;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public abstract class BaseInfoTreeController extends BaseSysTableController<InfoNode> {

    protected String category;
    protected TableTreeNode tableTreeNode;
    protected TableTag tableTag;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String queryLabel;
    protected String nameMsg, valueMsg, timeMsg;
    protected InfoNode loadedParent;

    @FXML
    protected BaseInfoTreeViewController treeController;
    @FXML
    protected TableColumn<InfoNode, Long> nodeidColumn;
    @FXML
    protected TableColumn<InfoNode, String> nameColumn, valueColumn;
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

    public BaseInfoTreeController() {
        baseTitle = message("InformationInTree");
        category = InfoNode.InformationInTree;
        nameMsg = message("Title");
        valueMsg = message("Value");
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

            treeController.setParameters(this);

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
            MyBoxLog.error(e);
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
                valueColumn.setCellFactory(new TableTextTrimCell());
                valueColumn.setText(valueMsg);
            }
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setText(timeMsg);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            loadTree();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
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
        if (show && leftPaneCheck != null) {
            leftPaneCheck.setSelected(true);
        }
    }

    /*
        tree
     */
    public void loadTree() {
        treeController.loadTree();
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

    @FXML
    @Override
    public void viewAction() {
        InfoNode.view(selectedItem(), null);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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

}
