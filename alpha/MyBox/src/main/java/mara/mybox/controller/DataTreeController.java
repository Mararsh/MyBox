package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import static mara.mybox.db.data.DataNode.TitleSeparater;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableNodeData2DDefinition;
import mara.mybox.db.table.TableNodeHtml;
import mara.mybox.db.table.TableNodeImageScope;
import mara.mybox.db.table.TableNodeJEXL;
import mara.mybox.db.table.TableNodeJShell;
import mara.mybox.db.table.TableNodeJavaScript;
import mara.mybox.db.table.TableNodeMathFunction;
import mara.mybox.db.table.TableNodeRowFilter;
import mara.mybox.db.table.TableNodeSQL;
import mara.mybox.db.table.TableNodeText;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class DataTreeController extends BaseDataTreeViewController {

    /*
        tree
     */
    @Override
    public void whenTreeEmpty() {
        File file = nodeTable.exampleFile();
        if (file != null) {
            if (AppVariables.isTesting
                    || PopTools.askSure(getTitle(), message("ImportExamples") + ": " + baseTitle)) {
                importExamples(null);
            }
        }
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<DataNode> item) {
        clicked(UserConfig.getString(baseName + "WhenDoubleClickNode", "PopNode"), item);
    }

    @Override
    public void rightClicked(MouseEvent event, TreeItem<DataNode> item) {
        clicked(UserConfig.getString(baseName + "WhenRightClickNode", "PopMenu"), item);
    }

    public void clicked(String clickAction, TreeItem<DataNode> item) {
        if (item == null || clickAction == null) {
            return;
        }
        switch (clickAction) {
            case "PopMenu":
                showPopMenu(item);
                break;
            case "EditNode":
                editNode(item.getValue());
                break;
            case "PopNode":
                popNode(item.getValue());
                break;
            case "ExecuteNode":
                executeNode(item);
                break;
            default:
                break;
        }
    }

    /*
        menu
     */
    @Override
    public List<MenuItem> operationsMenuItems(TreeItem<DataNode> treeItem) {
        List<MenuItem> items = new ArrayList<>();

        items.addAll(updateMenuItems(treeItem));

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(treeItem));
        items.add(rightClickMenu(treeItem));

        return items;
    }

    public List<MenuItem> updateMenuItems(TreeItem<DataNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        boolean isRoot = isRoot(treeItem.getValue());

        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(treeItem.getValue());
        });
        items.add(menu);

        menu = new MenuItem(message("DeleteNodeAndDescendants"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNodeAndDescendants(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("DeleteDescendants"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteDescendants(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("RenameNode"), StyleTools.getIconImageView("iconInput.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameNode(treeItem);
        });
        menu.setDisable(isRoot);
        items.add(menu);

        menu = new MenuItem(message("CopyNodes"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyNode(treeItem);
        });
        menu.setDisable(isRoot);
        items.add(menu);

        menu = new MenuItem(message("MoveNodes"), StyleTools.getIconImageView("iconMove.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            moveNode(treeItem);
        });
        menu.setDisable(isRoot);
        items.add(menu);

        if (nodeTable.isNodeExecutable()) {
            menu = new MenuItem(message("Execute"), StyleTools.getIconImageView("iconGo.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                executeNode(treeItem);
            });
            menu.setDisable(treeItem == null);
            items.add(menu);
        }

        menu = new MenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            editNode(treeItem.getValue());
        });
        items.add(menu);

        return items;
    }

    @Override
    public List<MenuItem> dataMenuItems(TreeItem<DataNode> item) {
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("Tags"), StyleTools.getIconImageView("iconTag.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            DataTreeTagsController.manage(this);
        });
        items.add(menu);

        menu = new MenuItem(message("TreeView"), StyleTools.getIconImageView("iconHtml.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            treeView();
        });
        items.add(menu);

        menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importExamples(item);
        });
        items.add(menu);

        menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            exportNode(item);
        });
        items.add(menu);

        menu = new MenuItem(message("Import"), StyleTools.getIconImageView("iconImport.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            importAction(item);
        });
        items.add(menu);

        return items;
    }

    public Menu doubleClickMenu(TreeItem<DataNode> treeItem) {
        Menu clickMenu = new Menu(message("WhenDoubleClickNode"), StyleTools.getIconImageView("iconSelectAll.png"));
        clickMenu(treeItem, clickMenu, "WhenDoubleClickNode", "PopNode");
        return clickMenu;
    }

    public Menu rightClickMenu(TreeItem<DataNode> treeItem) {
        Menu clickMenu = new Menu(message("WhenRightClickNode"), StyleTools.getIconImageView("iconSelectNone.png"));
        clickMenu(treeItem, clickMenu, "WhenRightClickNode", "PopMenu");
        return clickMenu;
    }

    public Menu clickMenu(TreeItem<DataNode> treeItem, Menu menu, String key, String defaultAction) {
        ToggleGroup clickGroup = new ToggleGroup();
        String currentClick = UserConfig.getString(baseName + key, defaultAction);

        RadioMenuItem editNodeMenu = new RadioMenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        editNodeMenu.setSelected("EditNode".equals(currentClick));
        editNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "EditNode");
            }
        });
        editNodeMenu.setToggleGroup(clickGroup);

        RadioMenuItem popNodeMenu = new RadioMenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
        popNodeMenu.setSelected("PopNode".equals(currentClick));
        popNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "PopNode");
            }
        });
        popNodeMenu.setToggleGroup(clickGroup);

        menu.getItems().addAll(editNodeMenu, popNodeMenu);

        if (nodeTable.isNodeExecutable()) {
            RadioMenuItem executeNodeMenu = new RadioMenuItem(message("Execute"), StyleTools.getIconImageView("iconGo.png"));
            executeNodeMenu.setSelected("ExecuteNode".equals(currentClick));
            executeNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + key, "ExecuteNode");
                }
            });
            executeNodeMenu.setToggleGroup(clickGroup);
            menu.getItems().add(executeNodeMenu);
        }

        RadioMenuItem nothingMenu = new RadioMenuItem(message("DoNothing"));
        nothingMenu.setSelected(currentClick == null || "DoNothing".equals(currentClick));
        nothingMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "DoNothing");
            }
        });
        nothingMenu.setToggleGroup(clickGroup);

        RadioMenuItem clickPopMenu = new RadioMenuItem(message("ContextMenu"), StyleTools.getIconImageView("iconMenu.png"));
        clickPopMenu.setSelected("PopMenu".equals(currentClick));
        clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + key, "PopMenu");
            }
        });
        clickPopMenu.setToggleGroup(clickGroup);

        menu.getItems().addAll(clickPopMenu, nothingMenu);

        return menu;
    }

    @FXML
    public void showPopMenu(TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImageView("iconData.png"));
        dataMenu.getItems().addAll(dataMenuItems(item));
        items.add(dataMenu);

        Menu treeMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        treeMenu.getItems().addAll(viewMenuItems(item));
        items.add(treeMenu);

        items.add(new SeparatorMenuItem());

        items.addAll(operationsMenuItems(item));

        popNodeMenu(treeView, items);
    }

    protected void deleteNodeAndDescendants(TreeItem<DataNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode node = item.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        boolean isRoot = isRoot(node);
        if (isRoot) {
            if (!PopTools.askSure(getTitle(), message("DeleteNodeAndDescendants"), message("SureDeleteAll"))) {
                return;
            }
        } else {
            String chainName = chainName(item);
            if (!PopTools.askSure(getTitle(), chainName, message("DeleteNodeAndDescendants"))) {
                return;
            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    return nodeTable.deleteNode(conn, node.getNodeid()) >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (isRoot) {
                    loadTree(null);
                } else {
                    item.getParent().getChildren().remove(item);
                }
                popSuccessful();
            }

        };
        start(task, treeView);
    }

    protected void deleteDescendants(TreeItem<DataNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode node = item.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        boolean isRoot = isRoot(node);
        if (isRoot) {
            if (!PopTools.askSure(getTitle(), message("DeleteDescendants"), message("SureDeleteAll"))) {
                return;
            }
        } else {
            String chainName = chainName(item);
            if (!PopTools.askSure(getTitle(), chainName, message("DeleteDescendants"))) {
                return;
            }
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    return nodeTable.deleteDecentants(conn, node.getNodeid()) >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                item.getChildren().clear();
                popSuccessful();
            }

        };
        start(task, treeView);
    }

    protected void renameNode(TreeItem<DataNode> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        DataNode nodeValue = item.getValue();
        if (nodeValue == null || isRoot(nodeValue)) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(item);
        String name = PopTools.askValue(getBaseTitle(), chainName, message("RenameNode"), nodeValue.getTitle() + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.contains(TitleSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + TitleSeparater + "\"");
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private DataNode updatedNode;

            @Override
            protected boolean handle() {
                nodeValue.setTitle(name);
                updatedNode = nodeTable.updateData(nodeValue);
                return updatedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                item.setValue(updatedNode);
                treeView.refresh();
                popSuccessful();
            }
        };
        start(task, treeView);
    }

    protected void copyNode(TreeItem<DataNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        InfoTreeNodeCopyController controller
                = (InfoTreeNodeCopyController) childStage(Fxmls.InfoTreeNodeCopyFxml);
//        controller.setParameters(manager, item.getValue(), chainName);
    }

    protected void moveNode(TreeItem<DataNode> item) {
        if (item == null || isRoot(item.getValue())) {
            return;
        }
        String chainName = chainName(item);
        InfoTreeNodeMoveController controller = (InfoTreeNodeMoveController) childStage(Fxmls.InfoTreeNodeMoveFxml);
//        controller.setParameters(manager, item.getValue(), chainName);
    }

    protected void executeNode(TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
//        manager.executeNode(item.getValue());
    }

    protected void exportNode(TreeItem<DataNode> item) {
        DataTreeExportController exportController
                = (DataTreeExportController) branchStage(Fxmls.DataTreeExportFxml);
        exportController.setParamters(this, item);
    }

    @FXML
    protected void importAction(TreeItem<DataNode> item) {
        DataTreeImportController importController
                = (DataTreeImportController) branchStage(Fxmls.DataTreeImportFxml);
        importController.setParamters(this, item);
    }

    @FXML
    protected void importExamples(TreeItem<DataNode> item) {
        DataTreeImportController importController
                = (DataTreeImportController) branchStage(Fxmls.DataTreeImportFxml);
        importController.importExamples(this, item);
    }

    @FXML
    public void treeView() {
        treeView(selected());
    }

    public void treeView(TreeItem<DataNode> item) {
        if (item == null) {
            return;
        }
        DataNode node = item.getValue();
        if (node == null) {
            return;
        }
        FxTask infoTask = new FxTask<Void>(this) {
            private File file;

            @Override
            protected boolean handle() {
                file = FileTmpTools.generateFile(dataName, "htm");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, Charset.forName("utf-8"), false))) {
                    writer.write(HtmlWriteTools.htmlPrefix(chainName(item), "utf-8", HtmlStyles.TableStyle));
                    // https://www.jb51.net/article/116957.htm
                    writer.write("<BODY>\n");
                    writer.write(" <script>\n"
                            + "    function nodeClicked(id) {\n"
                            + "      var obj = document.getElementById(id);\n"
                            + "      var objv = obj.style.display;\n"
                            + "      if (objv == 'none') {\n"
                            + "        obj.style.display = 'block';\n"
                            + "      } else {\n"
                            + "        obj.style.display = 'none';\n"
                            + "      }\n"
                            + "    }\n"
                            + "    function showClass(className, show) {\n"
                            + "      var nodes = document.getElementsByClassName(className);  　\n"
                            + "      if ( show) {\n"
                            + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                            + "              nodes[i].style.display = '';\n"
                            + "           }\n"
                            + "       } else {\n"
                            + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                            + "              nodes[i].style.display = 'none';\n"
                            + "           }\n"
                            + "       }\n"
                            + "    }\n"
                            + "  </script>\n\n");
                    writer.write("<DIV>\n<DIV>\n"
                            + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('TreeNode', this.checked);\">"
                            + message("Unfold") + "</INPUT>\n"
                            + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('SerialNumber', this.checked);\">"
                            + message("HierarchyNumber") + "</INPUT>\n"
                            + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('NodeTag', this.checked);\">"
                            + message("Tags") + "</INPUT>\n"
                            + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('nodeValue', this.checked);\">"
                            + message("Values") + "</INPUT>\n"
                            + "</DIV>\n<HR>\n");
                    try (Connection conn = DerbyBase.getConnection()) {
                        treeView(this, writer, conn, node.getNodeid(), 4, "");
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
//                    writer.write("\n<HR>\n<TreeNode style=\"font-size:0.8em\">* "
//                            + message("HtmlEditableComments") + "</P>\n");
                    writer.write("</BODY>\n</HTML>\n");
                    writer.flush();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                WebBrowserController.openFile(file);
            }
        };
        start(infoTask, false);
    }

    protected void treeView(FxTask infoTask, BufferedWriter writer, Connection conn,
            long nodeid, int indent, String serialNumber) {
        try {
            if (conn == null || nodeid < 0) {
                return;
            }
            DataNode node = nodeTable.query(conn, nodeid);
            String indentNode = " ".repeat(indent);
            String spaceNode = "&nbsp;".repeat(indent);
            String nodePageid = "item" + node.getNodeid();
            String nodeName = node.getTitle();
            String displayName = "<SPAN class=\"SerialNumber\">" + serialNumber + "&nbsp;&nbsp;</SPAN>" + nodeName;
            boolean hasChildren = nodeTable.hasChildren(conn, nodeid);
            if (hasChildren) {
                displayName = "<a href=\"javascript:nodeClicked('" + nodePageid + "')\">" + displayName + "</a>";
            }
            writer.write(indentNode + "<DIV style=\"padding: 2px;\">" + spaceNode + displayName + "\n");
            List<DataNodeTag> tags = nodeTagsTable.nodeTags(conn, nodeid);
            if (tags != null && !tags.isEmpty()) {
                String indentTag = " ".repeat(indent + 8);
                String spaceTag = "&nbsp;".repeat(2);
                writer.write(indentTag + "<SPAN class=\"NodeTag\">\n");
                for (DataNodeTag nodeTag : tags) {
                    if (infoTask != null && !infoTask.isWorking()) {
                        return;
                    }
                    Color color = nodeTag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    writer.write(indentTag + spaceTag
                            + "<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: "
                            + FxColorTools.color2rgb(color)
                            + "; color: " + FxColorTools.color2rgb(FxColorTools.foreColor(color))
                            + ";\">" + nodeTag.getTag().getTag() + "</SPAN>\n");
                }
                writer.write(indentTag + "</SPAN>\n");
            }
            writer.write(indentNode + "</DIV>\n");

            String dataHtml = nodeTable.valuesHtml(infoTask, conn, myController, node);
            if (dataHtml != null && !dataHtml.isBlank()) {
                writer.write(indentNode + "<DIV class=\"nodeValue\">"
                        + "<DIV style=\"padding: 0 0 0 " + (indent + 4) * 6 + "px;\">"
                        + "<DIV class=\"valueBox\">\n");
                writer.write(indentNode + dataHtml + "\n");
                writer.write(indentNode + "</DIV></DIV></DIV>\n");
            }
            if (hasChildren) {
                writer.write(indentNode + "<DIV class=\"TreeNode\" id='" + nodePageid + "'>\n");
                String sql = "SELECT nodeid FROM " + nodeTable.getTableName()
                        + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + nodeTable.getOrderColumns();
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, nodeid);
                    try (ResultSet results = statement.executeQuery()) {
                        int count = 0;
                        String ps = serialNumber == null || serialNumber.isBlank() ? "" : serialNumber + ".";
                        while (results != null && results.next()) {
                            if (infoTask != null && !infoTask.isWorking()) {
                                break;
                            }
                            treeView(infoTask, writer, conn, results.getLong("nodeid"),
                                    indent + 4, ps + ++count);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                writer.write(indentNode + "</DIV>\n");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cancelAction() {

    }

    @FXML
    protected void moveAction() {
//        InfoTreeNodesMoveController.oneOpen(this);
    }

    public void executeNode(DataNode node) {
        if (node == null) {
            return;
        }

    }


    /*
        static methods
     */
    public static DataTreeController open(BaseController pController, boolean shouldLoad, BaseNodeTable table) {
        try {
            DataTreeController controller;
            if ((shouldLoad || AppVariables.closeCurrentWhenOpenTool) && pController != null) {
                controller = (DataTreeController) pController.loadScene(Fxmls.DataTreeFxml);
            } else {
                controller = (DataTreeController) WindowTools.openStage(Fxmls.DataTreeFxml);
            }
            controller.requestMouse();
            controller.initTree(table);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeController textTree(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeText());
    }

    public static DataTreeController htmlTree(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeHtml());
    }

    public static DataTreeController webFavorite(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeWebFavorite());
    }

    public static DataTreeController sql(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeSQL());
    }

    public static DataTreeController mathFunction(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeMathFunction());
    }

    public static DataTreeController imageScope(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeImageScope());
    }

    public static DataTreeController jShell(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeJShell());
    }

    public static DataTreeController jexl(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeJEXL());
    }

    public static DataTreeController javascript(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeJavaScript());
    }

    public static DataTreeController rowFilter(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeRowFilter());
    }

    public static DataTreeController data2DDefinition(BaseController pController, boolean shouldLoad) {
        return open(pController, shouldLoad, new TableNodeData2DDefinition());
    }

}
