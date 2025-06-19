package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTools;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.AppValues.Indent;
import static mara.mybox.value.Languages.message;
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
    protected String dataName, html, sql;
    protected BaseNodeTable nodeTable;
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
                rootNode = writeHtml(this);
                return rootNode != null;
            }

            @Override
            protected void whenSucceeded() {
                dataController.rootNode = rootNode;
                dataController.currentNode = node != null ? node : rootNode;
                loadContent(html);
            }

        };
        start(task);
    }

    public DataNode writeHtml(FxTask currentTask) {
        try (Connection conn = DerbyBase.getConnection()) {
            DataNode rootNode = nodeTable.getRoot(conn);
            count = level = 0;
            childrenNumberStack = new Stack();
            childrenNumber = 0;
            html = HtmlWriteTools.htmlPrefix(nodeTable.getTreeName(), "utf-8", null);
            html += Indent + "<BODY>\n"
                    + " <script>\n"
                    + "    function changeChildrenVisible(nodeid) {\n"
                    + "      var obj = document.getElementById('children' + nodeid);\n"
                    + "      var objv = obj.style.display;\n"
                    + "      if (objv == 'none') {\n"
                    + "           obj.style.display = 'block';\n"
                    + "      } else {\n"
                    + "           obj.style.display = 'none';\n"
                    + "      }\n"
                    + "    }\n"
                    + "    function showChildren(nodeid) {\n"
                    + "      var obj = document.getElementById('children' +nodeid);\n"
                    + "      obj.style.display = 'block';\n"
                    + "    }\n"
                    + "    function hideChildren(nodeid) {\n"
                    + "      var obj = document.getElementById('children' +nodeid);\n"
                    + "      obj.style.display = 'none';\n"
                    + "    }\n"
                    + "    function scrollToNode(nodeid) {\n"
                    + "      var obj = document.getElementById('node' +nodeid);\n"
                    //                    + "      obj.scrollIntoView();\n"
                    + "      window.scrollTo(0, obj.offsetTop - 200);\n"
                    + "    }\n"
                    + "    function selectNode(nodeid) {\n"
                    + "      window.getSelection().removeAllRanges();     \n"
                    + "      var selection = window.getSelection();        \n"
                    + "      var range = document.createRange();        \n"
                    + "      range.selectNode(document.getElementById('title' +nodeid));        \n"
                    + "      selection.addRange(range);\n"
                    + "    }\n"
                    + "    function nodeClicked(nodeid) {\n"
                    + "      selectNode(nodeid);"
                    + "      alert('nodeClicked:' +nodeid);\n"
                    + "    }\n"
                    + "    function singleChecked(nodeid, checked) {\n"
                    + "      if (!checked) return;"
                    + "      var checks = document.getElementsByClassName(\"NodeCheck\"); \n"
                    + "      var checkid = 'check' + nodeid;"
                    + "      for (var i = 0 ; i < checks.length; i++) {\n"
                    + "         if (checks[i].id !=  checkid ) {\n"
                    + "              checks[i].checked = false;\n"
                    + "         }\n"
                    + "      }\n"
                    + "    }\n"
                    + "    function pickChecked() {\n"
                    + "      var checks = document.getElementsByClassName(\"NodeCheck\"); \n"
                    + "      var ids = '';"
                    + "      for (var i = 0 ; i < checks.length; i++) {\n"
                    + "         if (checks[i].checked ) {\n"
                    + "              ids += checks[i].id.substring(5) + ',';\n"
                    + "         }\n"
                    + "      }\n"
                    + "      return ids;\n"
                    + "    }\n"
                    + "    function contextMenu(nodeid) {\n"
                    + "      alert('contextMenu:' +nodeid);\n"
                    + "    }\n"
                    + "  </script>\n\n";
            sql = "SELECT nodeid FROM " + nodeTable.getTableName()
                    + " WHERE parentid=? AND parentid<>nodeid "
                    + " ORDER BY " + nodeTable.getOrderColumns();
            writeNode(currentTask, conn, rootNode.getNodeid(), null, 0);
            html += Indent + "</BODY>\n</HTML>\n";
            return rootNode;
        } catch (Exception e) {
            currentTask.setError(e.toString());
            return null;
        }
    }

    public void writeNode(FxTask currentTask, Connection conn,
            long nodeid, String parentHierarchyNumber, int nodeIndex) {
        level++;
        if (conn == null || nodeid < 0) {
            return;
        }
        try {
            count++;
            int indent = 4 * level;
            StringBuilder s = new StringBuilder();
            String indentNode = " ".repeat(indent);
            String indentAttr = " ".repeat(indent + 4);
            String hierarchyNumber = parentHierarchyNumber != null
                    ? parentHierarchyNumber + "." + nodeIndex
                    : (nodeIndex + "");
            s.append(indentNode).append("<DIV id='node").append(nodeid).append("'>\n");
            s.append(indentAttr).append("<DIV id='title").append(nodeid).append("' ");
            s.append("oncontextmenu=\"contextMenu(").append(nodeid)
                    .append("); return false;\" style=\"padding: 2px;\">");
            s.append("&nbsp;".repeat(indent));

            if (dataController.selectionType != DataNode.SelectionType.None) {
                s.append("<INPUT type=\"checkbox\" class=\"NodeCheck\" id='check").append(nodeid).append("'");
                if (dataController.selectionType == DataNode.SelectionType.Single) {
                    s.append(" onclick=\"singleChecked(").append(nodeid).append(",this.checked);\"");
                }
                s.append("/>\n");
            }

            String v = hierarchyNumber;
            if (nodeTable.hasChildren(conn, nodeid)) {
                v = "<a href=\"javascript:changeChildrenVisible('" + nodeid + "')\">" + v + "</a>";
            }
            s.append(v);
            v = "<a href=\"javascript:nodeClicked(" + nodeid + ")\">"
                    + nodeTable.title(conn, nodeid) + "</a>";
            s.append("&nbsp;".repeat(2)).append(v).append("\n");
            s.append(indentAttr).append(
                    DataNodeTools.tagsHtml(nodeTagsTable.nodeTags(conn, nodeid), indent));
            s.append(indentAttr).append("</DIV>\n");
            html += s.toString();
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }
            childrenNumberStack.push(childrenNumber);
            childrenNumber = 0;
            html += indentAttr + "<DIV id='children" + nodeid + "' class=\"Children\" >\n";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, nodeid);
                try (ResultSet results = statement.executeQuery()) {
                    String p = hierarchyNumber.equals("0") ? null : hierarchyNumber;
                    while (results != null && results.next()) {
                        if (currentTask == null || !currentTask.isWorking()) {
                            return;
                        }
                        childrenNumber++;
                        writeNode(currentTask, conn, results.getLong("nodeid"),
                                p, childrenNumber);
                    }
                } catch (Exception e) {
                    displayError(e.toString());
                }
            } catch (Exception e) {
                displayError(e.toString());
            }
            html += indentAttr + "</DIV>\n";
            html += indentNode + "</DIV>\n";
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
    protected void succeeded() {
        super.succeeded();
        if (content == null || content.isBlank()) {
            return;
        }
        selectNode(dataController.currentNode);
        setWebViewLabel(message("Count") + ": " + count);
    }

    @Override
    public void alert(WebEvent<String> ev) {
        try {
            String info = ev.getData();
            if (info == null || info.isBlank()) {
                return;
            }
            if (info.startsWith("nodeClicked:")) {
                nodeClicked(Long.parseLong(info.substring(12)));
                return;
            } else if (info.startsWith("contextMenu:")) {
                contextMenu(Long.parseLong(info.substring(12)));
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

    public void selectNode(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("scrollToNode(" + node.getNodeid() + ")");
        executeScript("selectNode(" + node.getNodeid() + ")");
    }

    public List<Long> selectedIDs() {
        List<Long> selectedIDs = new ArrayList<>();
        try {
            String[] ids = ((String) executeScript("pickChecked()")).split(",");
            for (String id : ids) {
                try {
                    long nodeid = Long.parseLong(id);
                    selectedIDs.add(nodeid);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
//            MyBoxLog.console(e.toString());
        }
        return selectedIDs;
    }

    public void unfoldNode(DataNode node) {
        unfold(node.getNodeid(), true);
    }

    public void unfoldNodeAndDescendants(DataNode node) {
        unfold(node.getNodeid(), false);
    }

    public void foldNode(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("hideChildren(" + node.getNodeid() + ")");
    }

    public void unfold(long nodeid, boolean onlyChildren) {
        if (nodeTable == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private String script;

            @Override
            protected boolean handle() {
                script = "";
                sql = "SELECT nodeid FROM " + nodeTable.getTableName()
                        + " WHERE parentid=? AND parentid<>nodeid";
                try (Connection conn = DerbyBase.getConnection()) {
                    unfold(conn, nodeid, onlyChildren);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return !script.isBlank();
            }

            protected void unfold(Connection conn,
                    long nodeid, boolean onlyChildren) {
                script += "showChildren(" + nodeid + ");\n";
                try (PreparedStatement query = conn.prepareStatement(sql)) {
                    query.setLong(1, nodeid);
                    ResultSet results = query.executeQuery();
                    while (results != null && results.next()) {
                        if (!isWorking()) {
                            return;
                        }
                        long childid = results.getLong("nodeid");
                        if (onlyChildren) {
                            script += "hideChildren(" + childid + ");\n";
                        } else {
                            unfold(conn, childid, false);
                        }
                    }
                } catch (Exception e) {
                    error = e.toString();
                }
            }

            @Override
            protected void whenSucceeded() {
                executeScript("scrollToNode(" + nodeid + ")");
                executeScript(script);
            }

        };
        start(task, false);
    }

    public List<MenuItem> foldMenuItems(DataNode node) {
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("UnfoldNode"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNode(node);
        });
        items.add(menu);

        menu = new MenuItem(message("UnfoldNodeAndDescendants"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNodeAndDescendants(node);
        });
        items.add(menu);

        menu = new MenuItem(message("FoldNode"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNode(node);
        });
        items.add(menu);

        return items;
    }

}
