package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class ControlInfoNodeSelector extends BaseInfoTreeController {

    @FXML
    protected ControlInfoTreeSelector nodesController;
    @FXML
    protected ControlWebView viewController;

    @Override
    public void initValues() {
        try {
            super.initValues();
            treeController = nodesController;
            nodesController.selector = this;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        table
     */
    @Override
    public void itemClicked() {
        viewAction();
    }

    @FXML
    @Override
    public void viewAction() {
        viewNode(selectedItem());
    }

    protected void viewNode(InfoNode node) {
        if (node == null) {
            return;
        }
        String html = InfoNode.nodeHtml(node, null);
        viewController.loadContents(html);
    }

}
