package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-4-25
 * @License Apache License Version 2.0
 */
public class ControlDataTreeNodeView extends ControlWebView {

    protected BaseNodeTable nodeTable;
    protected DataNode viewNode;

    @FXML
    protected FlowPane opPane;

    public void setParameters(BaseController parent, BaseNodeTable table) {
        try {
            nodeTable = table;

            setParent(parent);
            initStyle = HtmlStyles.styleValue("Table");

            nullLoad();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void nullLoad() {
        opPane.setVisible(false);
        loadContent("");
    }

    public void loadNode(long id) {
        nullLoad();
        if (id < 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        webViewLabel.setText(message("Loading"));
        task = new FxSingletonTask<Void>(this) {
            private String html;
            private DataNode node;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    node = nodeTable.readChain(this, conn, id);
                    if (node == null) {
                        return false;
                    }
                    html = nodeTable.nodeHtml(this, conn, controller, node);
                    return html != null && !html.isBlank();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadContent(html);
                opPane.setVisible(true);
                goButton.setVisible(nodeTable.isNodeExecutable(node));
                viewNode = node;
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
//                webViewLabel.setText(null);
                closePopup();
            }

        };
        start(task, thisPane);
    }

    @FXML
    @Override
    public void editAction() {
        if (viewNode == null) {
            return;
        }
        DataTreeNodeEditorController.loadNode(parentController, nodeTable, viewNode, false);
    }

    @FXML
    @Override
    public boolean popAction() {
        if (viewNode == null) {
            return false;
        }
        nodeTable.popNode(parentController, viewNode);
        return true;
    }

    @FXML
    @Override
    public void goAction() {
        if (viewNode == null) {
            return;
        }
        nodeTable.executeNode(parentController, viewNode);
    }

    @FXML
    public void locateAction() {
        if (viewNode == null) {
            return;
        }
        DataTreeController.open(nodeTable, viewNode);
    }

}
