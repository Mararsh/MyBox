package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
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
        loadData();
    }

    public void viewNode(InfoNode node) {
        if (node == null) {
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
                    html = InfoNode.nodeHtml(this, myController, node, null);
                    return html != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                viewController.loadContents(html);
                selectedNode = node;
            }

        };
        start(task);
    }

}
