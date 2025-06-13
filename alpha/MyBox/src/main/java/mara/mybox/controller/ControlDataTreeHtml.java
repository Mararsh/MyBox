package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Stack;
import javafx.scene.web.WebEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataNodeTools;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.AppValues.Indent;

/**
 * @Author Mara
 * @CreateDate 2025-6-13
 * @License Apache License Version 2.0
 */
public class ControlDataTreeHtml extends ControlWebView {

    protected BaseDataTreeController dataController;
    protected ControlWebView viewController;
    protected TableDataTag tagTable;
    protected TableDataNodeTag nodeTagsTable;
    protected String dataName;
    protected BaseNodeTable nodeTable;
    protected String html, hierarchyNumber;
    protected int count, level, childrenNumber;
    protected Stack<Integer> childrenNumberStack;

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;
            nodeTable = dataController.nodeTable;
            tagTable = dataController.tagTable;
            nodeTagsTable = dataController.nodeTagsTable;
            dataName = dataController.dataName;
            baseName = dataController.baseName + "_" + baseName;

            if (dataController.viewController != null) {
                viewController = dataController.viewController;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTree(DataNode node) {
        if (nodeTable == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        clear();
        task = new FxSingletonTask<Void>(this) {

            private DataNode rootNode;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    rootNode = nodeTable.getRoot(conn);
                    count = level = 0;
                    childrenNumberStack = new Stack();
                    childrenNumber = 0;
                    html = HtmlWriteTools.htmlPrefix(nodeTable.getTreeName(), "utf-8", null);
                    html += Indent + "<BODY>\n" + Indent + Indent + "<H2>" + nodeTable.getTreeName() + "</H2>\n";
                    html += DataNodeTools.htmlControls();
                    writeHtml(this, conn, rootNode.getNodeid(), "");
                    html += Indent + "</BODY>\n</HTML>\n";
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                dataController.rootNode = rootNode;
                dataController.viewNode(node);
                loadContent(html);
            }

        };
        start(task, thisPane);

    }

    public void writeHtml(FxTask currentTask, Connection conn, long nodeid, String hierarchyNumber) {
        level++;
        if (conn == null || nodeid < 0) {
            return;
        }
        try {
            count++;
            DataNode node = nodeTable.query(conn, nodeid);
            List<DataNodeTag> tags = nodeTagsTable.nodeTags(conn, nodeid);
            String nodePageid = "item" + node.getNodeid();
            html += DataNodeTools.treeNodeHtml(currentTask, conn,
                    myController, nodeTable, node, tags,
                    nodePageid, 4 * level, hierarchyNumber, true);
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }
            childrenNumberStack.push(childrenNumber);
            childrenNumber = 0;
            html += Indent + "<DIV class=\"TreeNode\" id='" + nodePageid + "'>\n";
            String sql = "SELECT nodeid FROM " + nodeTable.getTableName()
                    + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + nodeTable.getOrderColumns();
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, nodeid);
                try (ResultSet results = statement.executeQuery()) {
                    String ps = hierarchyNumber == null || hierarchyNumber.isBlank() ? "" : hierarchyNumber + ".";
                    while (results != null && results.next()) {
                        if (currentTask == null || !currentTask.isWorking()) {
                            return;
                        }
                        childrenNumber++;
                        writeHtml(currentTask, conn, results.getLong("nodeid"),
                                ps + childrenNumber);
                    }
                } catch (Exception e) {
                    displayError(e.toString());
                }
            } catch (Exception e) {
                displayError(e.toString());
            }
            html += Indent + "</DIV>\n";
            childrenNumber = childrenNumberStack.pop();
        } catch (Exception e) {
            displayError(e.toString());
        }
        level--;
    }

    @Override
    public void alert(WebEvent<String> ev) {
        try {
            String info = ev.getData();
            if (info == null || info.isBlank()) {
                return;
            }
            if (info.startsWith("nodeClicked:")) {
                nodeClicked(Long.parseLong(info.substring("nodeClicked:".length())));
                return;
            }
            super.alert(ev);
        } catch (Exception e) {
        }
    }

    public void nodeClicked(long id) {
        try {
            DataNode node = nodeTable.find(id);
            if (node == null) {
                return;
            }
            dataController.currentNode = node;
            dataController.leftClicked(null, node);
        } catch (Exception e) {
        }
    }

}
