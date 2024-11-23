package mara.mybox.controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.table.TableInfoNode;
import mara.mybox.db.table.TableInfoNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-5
 * @License Apache License Version 2.0
 */
public class ControlJavaScriptRefer extends ControlInfoTreeList {

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected ListView<String> placeholdersList;

    public ControlJavaScriptRefer() {
        baseTitle = "JavaScript";
        category = InfoNode.JavaScript;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableTreeNode = new TableInfoNode();
            tableTreeNodeTag = new TableInfoNodeTag();
            loadTree();

            placeholdersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            placeholdersList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (isSettingValues) {
                        return;
                    }
                    String selected = placeholdersList.getSelectionModel().getSelectedItem();
                    if (selected != null && !selected.isBlank()) {
                        scriptInput.replaceText(scriptInput.getSelection(), selected);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        tree
     */
    @Override
    public void itemClicked(MouseEvent event, TreeItem<InfoNode> item) {
        editNode(item);
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<InfoNode> item) {
        editNode(item);
    }

    protected void editNode(TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        InfoNode node = item.getValue();
        if (node == null || node.getInfo() == null) {
            return;
        }
        scriptInput.replaceText(scriptInput.getSelection(), node.getInfo());
    }

    @FXML
    public void editAction() {
        ControlDataJavascript.loadScript(scriptInput.getText());
    }

    @FXML
    public void manageAction() {
        ControlDataJavascript.loadScript("");
    }

    public void clear() {
        scriptInput.clear();
        placeholdersList.getItems().clear();
    }

    /*
        script
     */
    @FXML
    public void clearScript() {
        scriptInput.clear();
    }

    @FXML
    protected void popScriptExamples(Event event) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", false)) {
            showScriptExamples(event);
        }
    }

    @FXML
    protected void showScriptExamples(Event event) {
        PopTools.popJavaScriptExamples(this, event, scriptInput, interfaceName + "Examples", null);
    }

    @FXML
    protected void popScriptHistories(Event event) {
        if (UserConfig.getBoolean(interfaceName + "HistoriesPopWhenMouseHovering", false)) {
            showScriptHistories(event);
        }
    }

    @FXML
    protected void showScriptHistories(Event event) {
        PopTools.popStringValues(this, scriptInput, event, interfaceName + "Histories", false);
    }

}
