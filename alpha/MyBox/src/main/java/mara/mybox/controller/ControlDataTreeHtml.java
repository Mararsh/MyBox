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
                    + "      var childrenDiv = document.getElementById('children' + nodeid);\n"
                    + "      if (!childrenDiv) return;\n"
                    + "      if (childrenDiv.style.display == 'none') {\n"
                    + "           childrenDiv.style.display = 'block';\n"
                    + "      } else {\n"
                    + "           childrenDiv.style.display = 'none';\n"
                    + "      }\n"
                    + "    }\n"
                    + "    function unfoldNode(nodeid) {\n"
                    + "      var childrenDiv = document.getElementById('children' +nodeid);\n"
                    + "      if (!childrenDiv) return;\n"
                    + "      childrenDiv.style.display = 'block';\n"
                    + "      var children = childrenDiv.children; \n"
                    + "      if (!children) return;"
                    + "      for (var i = 0 ; i < children.length; i++) {\n"
                    + "         var childrenid = children[i].id;\n"
                    + "         if (childrenid)"
                    + "            foldDescendants(childrenid.substring(4));\n"
                    + "      }\n"
                    + "    }\n"
                    + "    function unfoldDescendants(nodeid) {\n"
                    + "      var childrenDiv = document.getElementById('children' +nodeid);\n"
                    + "      if (!childrenDiv) return;\n"
                    + "      childrenDiv.style.display = 'block';\n"
                    + "      var children = childrenDiv.children; \n"
                    + "      if (!children) return;"
                    + "      for (var i = 0 ; i < children.length; i++) {\n"
                    + "         var childrenid = children[i].id;\n"
                    + "         if (childrenid)"
                    + "            unfoldDescendants(childrenid.substring(4));\n"
                    + "      }\n"
                    + "    }\n"
                    + "    function foldDescendants(nodeid) {\n"
                    + "      var childrenDiv = document.getElementById('children' +nodeid);\n"
                    + "      if (!childrenDiv) return;\n"
                    + "      childrenDiv.style.display = 'none';\n"
                    + "      var children = childrenDiv.children; \n"
                    + "      if (!children) return;"
                    + "      for (var i = 0 ; i < children.length; i++) {\n"
                    + "         var childrenid = children[i].id;\n"
                    + "         if (childrenid)"
                    + "            foldDescendants(childrenid.substring(4));\n"
                    + "      }\n"
                    + "    }\n"
                    + "    function showChildren(nodeid) {\n"
                    + "      var childrenDiv = document.getElementById('children' +nodeid);\n"
                    + "      if (!childrenDiv) return;\n"
                    + "      childrenDiv.style.display = 'block';\n"
                    + "    }\n"
                    + "    function hideChildren(nodeid) {\n"
                    + "      var childrenDiv = document.getElementById('children' +nodeid);\n"
                    + "      if (!childrenDiv) return;\n"
                    + "      childrenDiv.style.display = 'none';\n"
                    + "    }\n"
                    + "    function scrollToNode(nodeid) {\n"
                    + "      var nodeDiv = document.getElementById('node' +nodeid);\n"
                    + "      if (!nodeDiv) return;\n"
                    //                    + "      childrenDiv.scrollIntoView();\n"
                    + "      window.scrollTo(0, nodeDiv.offsetTop - 200);\n"
                    + "    }\n"
                    + "    function selectNode(nodeid) {\n"
                    + "      var titleDiv = document.getElementById('title' +nodeid);\n"
                    + "      if (!titleDiv) return;\n"
                    + "      window.getSelection().removeAllRanges();     \n"
                    + "      var selection = window.getSelection();        \n"
                    + "      var range = document.createRange();        \n"
                    + "      range.selectNode(titleDiv);        \n"
                    + "      selection.addRange(range);\n"
                    + "    }\n"
                    + "    function nodeClicked(nodeid) {\n"
                    + "      if (!nodeid) return;\n"
                    + "      selectNode(nodeid);\n"
                    + "      alert('nodeClicked:' +nodeid);\n"
                    + "    }\n"
                    + "    function singleChecked(nodeid, checked) {\n"
                    + "      if (!nodeid || !checked) return;\n"
                    + "      var checks = document.getElementsByClassName(\"NodeCheck\"); \n"
                    + "      if (!checks) return;\n"
                    + "      var checkid = 'check' + nodeid;"
                    + "      for (var i = 0 ; i < checks.length; i++) {\n"
                    + "         if (checks[i].id !=  checkid ) {\n"
                    + "              checks[i].checked = false;\n"
                    + "         }\n"
                    + "      }\n"
                    + "    }\n"
                    + "    function pickChecked() {\n"
                    + "      var checks = document.getElementsByClassName(\"NodeCheck\"); \n"
                    + "      if (!checks) return;\n"
                    + "      var ids = '';\n"
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
            writeNode(currentTask, conn, rootNode.getNodeid(), null, 0, 4);
            html += Indent + "</BODY>\n</HTML>\n";
            return rootNode;
        } catch (Exception e) {
            currentTask.setError(e.toString());
            return null;
        }
    }

    public void writeNode(FxTask currentTask, Connection conn,
            long nodeid, String parentHierarchyNumber, int nodeIndex,
            int indent) {
        level++;
        if (conn == null || nodeid < 0) {
            return;
        }
        try {
            count++;
            StringBuilder s = new StringBuilder();
            String indentNode = " ".repeat(indent);
            String indentAttr = " ".repeat(indent + 4);
            String indentAttr2 = " ".repeat(indent + 8);
            String hierarchyNumber = parentHierarchyNumber != null
                    ? parentHierarchyNumber + "." + nodeIndex
                    : (nodeIndex + "");
            s.append(indentNode).append("<DIV id='node").append(nodeid).append("'>\n");
            s.append(indentAttr).append("<DIV id='title").append(nodeid).append("' ");
            s.append("oncontextmenu=\"contextMenu(").append(nodeid)
                    .append("); return false;\" style=\"padding: 2px;\">\n");

            s.append(indentAttr2).append("&nbsp;".repeat(indent)).append("\n");
            if (dataController.selectionType != DataNode.SelectionType.None) {
                s.append(indentAttr2).append("<INPUT type=\"checkbox\" class=\"NodeCheck\" id='check")
                        .append(nodeid).append("'");
                if (dataController.selectionType == DataNode.SelectionType.Single) {
                    s.append(" onclick=\"singleChecked(").append(nodeid).append(",this.checked);\"");
                }
                s.append("/>\n");
            }

            String v = hierarchyNumber;
            if (nodeTable.hasChildren(conn, nodeid)) {
                v = "<a href=\"javascript:changeChildrenVisible('" + nodeid + "')\">" + v + "</a>";
            }
            s.append(indentAttr2).append(v).append("\n");
            v = "<a href=\"javascript:nodeClicked(" + nodeid + ")\">"
                    + nodeTable.title(conn, nodeid) + "</a>";
            s.append(indentAttr2).append("&nbsp;".repeat(2)).append(v).append("\n");
            s.append(DataNodeTools.tagsHtml(nodeTagsTable.nodeTags(conn, nodeid), indent + 4));
            s.append(indentAttr).append("</DIV>\n");
            html += s.toString();
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }
            childrenNumberStack.push(childrenNumber);
            childrenNumber = 0;
            html += indentAttr + "<DIV id='children" + nodeid + "'>\n";
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
                                p, childrenNumber, indent + 8);
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
        if (node == null) {
            return;
        }
        executeScript("unfoldNode(" + node.getNodeid() + ")");
    }

    public void unfoldDescendants(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("unfoldDescendants(" + node.getNodeid() + ")");
    }

    public void foldNode(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("hideChildren(" + node.getNodeid() + ")");
    }

    public void foldDescendants(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("foldDescendants(" + node.getNodeid() + ")");
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
            unfoldDescendants(node);
        });
        items.add(menu);

        menu = new MenuItem(message("FoldNode"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldNode(node);
        });
        items.add(menu);

        menu = new MenuItem(message("FoldNodeAndDescendants"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldDescendants(node);
        });
        items.add(menu);

        return items;
    }

}
