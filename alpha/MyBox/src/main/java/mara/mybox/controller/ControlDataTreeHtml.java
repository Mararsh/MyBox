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
import mara.mybox.db.data.DataNodeTag;
import static mara.mybox.db.data.DataNodeTools.tagHtml;
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
                    + "    function changeChildrenDisplay(nodeid) {\n"
                    + "      var obj = document.getElementById('children' + nodeid);\n"
                    + "      var objv = obj.style.display;\n"
                    + "      if (objv == 'none') {\n"
                    + "        obj.style.display = 'block';\n"
                    + "      } else {\n"
                    + "        obj.style.display = 'none';\n"
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
                    + "      range.selectNode(document.getElementById('node' +nodeid));        \n"
                    + "      selection.addRange(range);\n"
                    + "    }\n"
                    + "    function nodeClicked(nodeid) {\n"
                    + "      selectNode(nodeid);"
                    + "      alert('nodeClicked:' +nodeid);\n"
                    + "    }\n"
                    + "    function contextMenu(nodeid) {\n"
                    + "      alert('contextMenu:' +nodeid);\n"
                    + "    }\n"
                    + "  </script>\n\n";
            writeHtml(currentTask, conn, rootNode.getNodeid(), "");
            html += Indent + "</BODY>\n</HTML>\n";
            return rootNode;
        } catch (Exception e) {
            currentTask.setError(e.toString());
            return null;
        }
    }

    public void writeHtml(FxTask currentTask, Connection conn,
            long nodeid, String hierarchyNumber) {
        level++;
        if (conn == null || nodeid < 0) {
            return;
        }
        try {
            count++;
            int indent = 4 * level;
            StringBuilder s = new StringBuilder();
            String indentNode = " ".repeat(indent);
            String spaceTag = "&nbsp;".repeat(2);
            String hieName = hierarchyNumber != null && !hierarchyNumber.isBlank() ? hierarchyNumber : "0";
            boolean hasChildren = nodeTable.hasChildren(conn, nodeid);
            if (hasChildren) {
                hieName = "<a href=\"javascript:changeChildrenDisplay('" + nodeid + "')\">" + hieName + "</a>";
            }
            s.append(indentNode).append("<DIV id='node").append(nodeid)
                    .append("' oncontextmenu=\"contextMenu(").append(nodeid)
                    .append("); return false;\" style=\"padding: 2px;\">")
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
            html += Indent + "<DIV id='children" + nodeid + "'>\n";
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
    protected void succeeded() {
        super.succeeded();
        selectNode(dataController.currentNode);
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

    public void selectNode(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("scrollToNode(" + node.getNodeid() + ")");
        executeScript("nodeClicked(" + node.getNodeid() + ")");
    }

    public void unfoldNode(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("scrollToNode(" + node.getNodeid() + ")");
        executeScript("showChildren(" + node.getNodeid() + ")");
    }

    public void foldNode(DataNode node) {
        if (node == null) {
            return;
        }
        executeScript("scrollToNode(" + node.getNodeid() + ")");
        executeScript("hideChildren(" + node.getNodeid() + ")");
    }

    public List<MenuItem> foldMenuItems(DataNode node) {
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("UnfoldNode"), StyleTools.getIconImageView("iconPlus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldNode(node);
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
