package mara.mybox.controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-5
 * @License Apache License Version 2.0
 */
public class ControlJavaScriptRefer extends BaseTreeInfoController {

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected ListView<String> placeholdersList;

    public ControlJavaScriptRefer() {
        baseTitle = "JavaScript";
        category = TreeNode.JavaScript;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableTreeNode = new TableTreeNode();
            tableTreeNodeTag = new TableTreeNodeTag();
            loadTree(null);

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
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    @Override
    protected void doubleClicked(TreeItem<TreeNode> item) {
        editNode(item);
    }

    @Override
    public void itemSelected(TreeItem<TreeNode> item) {
        editNode(item);
    }

    protected void editNode(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        TreeNode node = item.getValue();
        if (node == null || node.getValue() == null) {
            return;
        }
        scriptInput.replaceText(scriptInput.getSelection(), node.getValue());
    }

    @FXML
    public void editAction() {
        JavaScriptController.open(scriptInput.getText());
    }

    @FXML
    public void dataAction() {
        JavaScriptController.open("");
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
        try {
            MenuController controller
                    = PopTools.popJavaScriptExamples(this, event, scriptInput, interfaceName + "Examples");
            moreExampleButtons(controller);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void moreExampleButtons(MenuController controller) {

    }

    @FXML
    protected void popScriptHistories(Event event) {
        if (UserConfig.getBoolean(interfaceName + "HistoriesPopWhenMouseHovering", false)) {
            showScriptHistories(event);
        }
    }

    @FXML
    protected void showScriptHistories(Event event) {
        PopTools.popStringValues(this, scriptInput, event, interfaceName + "Histories", false, true);
    }

}
