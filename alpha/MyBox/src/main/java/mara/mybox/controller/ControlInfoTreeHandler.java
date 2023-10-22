package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class ControlInfoTreeHandler extends BaseInfoTreeController {

    protected BaseInfoTreeHandleController handler;
    protected InfoNode selectedNode;

    @FXML
    protected ControlInfoTreeListSelector nodesController;
    @FXML
    protected ControlWebView viewController;

    @Override
    public void initValues() {
        try {
            super.initValues();
            infoTree = nodesController;

            viewController.initStyle = HtmlStyles.styleValue("Table");
            tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseInfoTreeHandleController handler, String categroy) {
        if (handler == null) {
            return;
        }
        this.handler = handler;
        this.category = categroy;
        nodesController.handler = handler;
        setParameters(this);
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
        selectedNode = node;
    }

}
