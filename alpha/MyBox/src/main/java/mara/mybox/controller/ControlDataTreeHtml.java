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
import static mara.mybox.db.data.DataNodeTools.tagHtml;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.AppValues.Indent;
import org.w3c.dom.Document;

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
                    html += Indent + "<BODY>\n"
                            + " <script>\n"
                            + "    function showNode(id) {\n"
                            + "      var obj = document.getElementById(id);\n"
                            + "      var objv = obj.style.display;\n"
                            + "      if (objv == 'none') {\n"
                            + "        obj.style.display = 'block';\n"
                            + "      } else {\n"
                            + "        obj.style.display = 'none';\n"
                            + "      }\n"
                            + "    }\n"
                            + "    function nodeClicked(id) {\n"
                            + "      window.getSelection().removeAllRanges();     \n"
                            + "      var selection = window.getSelection();        \n"
                            + "      var range = document.createRange();        \n"
                            + "      range.selectNode(document.getElementById('node' +id));        \n"
                            + "      selection.addRange(range);"
                            + "      alert('nodeClicked:' +id);\n"
                            + "    }\n"
                            + "    function contextMenu(id) {\n"
                            + "      alert('contextMenu:' +id);\n"
                            + "    }\n"
                            + "  </script>\n\n";
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
        start(task);

    }

    public void writeHtml(FxTask currentTask, Connection conn,
            long nodeid, String hierarchyNumber) {
        level++;
        if (conn == null || nodeid < 0) {
            return;
        }
        try {
            count++;
            String childrenid = "children" + nodeid;
            int indent = 4 * level;
            StringBuilder s = new StringBuilder();
            String indentNode = " ".repeat(indent);
            String spaceTag = "&nbsp;".repeat(2);
            String hieName = hierarchyNumber != null && !hierarchyNumber.isBlank() ? hierarchyNumber : "0";
            boolean hasChildren = nodeTable.hasChildren(conn, nodeid);
            if (hasChildren) {
                hieName = "<a href=\"javascript:showNode('" + childrenid + "')\">" + hieName + "</a>";
            }
            s.append(indentNode).append("<DIV id='node").append(nodeid)
                    .append("' oncontextmenu=\"contextMenu(").append(nodeid)
                    .append(")\" style=\"padding: 2px;\">")
                    .append("&nbsp;".repeat(indent)).append(hieName);
            String displayName = "<a href=\"javascript:nodeClicked(" + nodeid + ")\">"
                    + nodeTable.title(conn, nodeid) + "</a>";
            s.append(spaceTag).append(displayName);
            List<DataNodeTag> tags = nodeTagsTable.nodeTags(conn, nodeid);
            if (tags != null && !tags.isEmpty()) {
                String indentTag = " ".repeat(indent + 8);
                s.append(indentTag);
                for (DataNodeTag nodeTag : tags) {
                    s.append(indentTag).append(spaceTag).append(tagHtml(nodeTag));
                }
                s.append(indentTag).append("\n");
            }
            s.append(indentNode).append("</DIV>\n");
            html += s.toString();
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }
            childrenNumberStack.push(childrenNumber);
            childrenNumber = 0;
            html += Indent + "<DIV id='" + childrenid + "'>\n";
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
    protected void setListeners(Document doc) {
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
            } else if (info.startsWith("contextMenu:")) {
                contextMenu(Long.parseLong(info.substring("contextMenu:".length())));
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
            dataController.closePopup();
            dataController.currentNode = node;
            dataController.showNode(node);
        } catch (Exception e) {
        }
    }

    public void contextMenu(long id) {
        try {
            DataNode node = nodeTable.find(id);
            if (node == null) {
                return;
            }
            dataController.rightClicked(null, node);
        } catch (Exception e) {
        }
    }

}
