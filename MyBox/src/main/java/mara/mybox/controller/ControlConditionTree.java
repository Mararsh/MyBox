package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBoxTreeItem;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.fxml.ConditionTreeView;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-04-21
 * @License Apache License Version 2.0
 */
public class ControlConditionTree extends BaseController {

    private BaseController userController;
    private String finalConditions, finalTitle;

    @FXML
    protected ConditionTreeView treeView;

    public ControlConditionTree() {
    }

    @FXML
    @Override
    public void selectAllAction() {
        treeView.selectAll();
    }

    @FXML
    @Override
    public void selectNoneAction() {
        treeView.selectNone();
    }

    public void clearAll() {
        treeView.setRoot(null);
        finalConditions = null;
        finalTitle = "";
    }

    public void clearTree() {
        treeView.setRoot(null);
        finalConditions = null;
        finalTitle = "";
    }

    public void loadTree() {
        clearTree();
        try {
            CheckBoxTreeItem<ConditionNode> allItem = new CheckBoxTreeItem(
                    ConditionNode.create(message("AllData"))
                            .setTitle(message("AllData"))
                            .setCondition("")
            );
            allItem.setExpanded(true);
            treeView.setRoot(allItem);
            treeView.setSelection();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public String check() {
        treeView.check();
        finalConditions = treeView.getFinalConditions();
        finalTitle = treeView.getFinalTitle();
        return finalConditions;
    }

    public void refreshTree() {
        check();
        loadTree();
    }

    public void select(String title) {
        treeView.select(title);
    }

    public void select(List<String> titles) {
        treeView.select(titles);
    }

    /*
        get/set
     */
    public BaseController getUserController() {
        return userController;
    }

    public void setUserController(BaseController userController) {
        this.userController = userController;
    }

    public String getFinalConditions() {
        return finalConditions;
    }

    public void setFinalConditions(String finalConditions) {
        this.finalConditions = finalConditions;
    }

    public String getFinalTitle() {
        return finalTitle;
    }

    public void setFinalTitle(String finalTitle) {
        this.finalTitle = finalTitle;
    }

    public ConditionTreeView getTreeView() {
        return treeView;
    }

    public void setTreeView(ConditionTreeView treeView) {
        this.treeView = treeView;
    }

}
