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

    protected BaseDataTreeController dataController;
    protected DataTreeQueryResultsController queryController;
    protected BaseNodeTable nodeTable;
    protected String dataName;

    @FXML
    protected FlowPane opPane;

    public void setTree(BaseDataTreeController controller) {
        try {
            dataController = controller;
            nodeTable = dataController.nodeTable;
            dataName = dataController.dataName;

            setParent(dataController);
            initStyle = HtmlStyles.styleValue("Table");

            nullLoad();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setQuery(DataTreeQueryResultsController controller) {
        try {
            queryController = controller;
            nodeTable = queryController.nodeTable;
            dataName = queryController.dataName;

            setParent(queryController);
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
                    html = nodeTable.valuesHtml(this, conn, controller, node,
                            node.getHierarchyNumber(), 4);
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
                if (dataController != null) {
                    dataController.viewNode = node;
                }
                if (queryController != null) {
                    queryController.viewNode = node;
                }
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

}
