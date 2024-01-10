package mara.mybox.controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public abstract class BaseInfoTreeController extends BaseController {

    protected ControlInfoTreeList infoTree;
    protected String category;
    protected TableTreeNode tableTreeNode;
    protected TableTag tableTag;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected String nameMsg, valueMsg, timeMsg;

    @FXML
    protected ControlInfoTreeList listController;
    @FXML
    protected ControlInfoTreeTable tableController;
    @FXML
    protected VBox timesBox;
    @FXML
    protected RadioButton findNameRadio, findValueRadio;
    @FXML
    protected FlowPane tagsPane;
    @FXML
    protected Button refreshTimesButton, queryTimesButton;
    @FXML
    protected InfoTreeTagsController tagsController;
    @FXML
    protected ControlTimesTree timesController;
    @FXML
    protected TextField findInput;
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
    public void initValues() {
        try {
            super.initValues();

            tableTreeNode = new TableTreeNode();
            tableTag = new TableTag();
            tableTreeNodeTag = new TableTreeNodeTag();

            infoTree = listController;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseInfoTreeController infoController) {
        try {
            infoTree.setParameters(infoController);

            tableController.setParameters(infoController);

            tagsController.setParameters(this);
            tagsController.loadTableData();

            initTimes();
            initFind();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void showNodesList(boolean show) {
        if (isSettingValues || infoTree.nodesListCheck == null) {
            return;
        }
        infoTree.isSettingValues = true;
        infoTree.nodesListCheck.setSelected(show);
        infoTree.isSettingValues = false;

        isSettingValues = true;
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
        infoTree.loadTree();
    }

    public void popNode(InfoNode item) {
        if (item == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    html = InfoNode.nodeHtml(this, myController, item, null);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlTableController.open(null, html);
            }

        };
        start(task);
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
        tableController.queryTimes(timesController.check(), timesController.getFinalTitle());
    }

    /*
        Tags
     */
    protected void refreshTagss() {
        tagsController.refreshAction();
    }

    public void tagsChanged() {
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
        tableController.find(findInput.getText(), findNameRadio.isSelected());

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
