package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.fxml.ConditionTreeView;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-04-21
 * @License Apache License Version 2.0
 */
public class ConditionTreeController extends BaseController {

    private BaseController userController;
    private String finalConditions, finalTitle;

    @FXML
    protected ConditionTreeView treeView;
    @FXML
    protected CheckBox allCheck;

    public ConditionTreeController() {
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            if (allCheck != null) {
                allCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            if (allCheck.isSelected()) {
                                treeView.selectAll();
                            } else {
                                treeView.selectNone();
                            }
                        });
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
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

    public void check() {
        treeView.check();
        finalConditions = treeView.getFinalConditions();
        finalTitle = treeView.getFinalTitle();
    }

    public void refreshTree() {
        check();
        loadTree();
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
