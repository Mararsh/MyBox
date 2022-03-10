package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeLeaf;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNodesController extends BaseNodeSelector<TreeNode> {

    protected TreeManageController treeController;
    protected TreeNodesController caller;
    protected TableTree tableTree;
    protected TableTreeLeaf tableTreeLeaf;
    protected String category;

    public TreeNodesController() {
    }

    public void setParameters(TreeManageController parent, boolean manageMode) {
        super.setManager(parent, manageMode);
        treeController = parent;
        tableTree = parent.tableTree;
        tableTreeLeaf = parent.tableTreeLeaf;
        category = treeController.category;
        baseTitle = category;
    }

    public void setCaller(TreeNodesController caller) {
        super.setManager(null, false);
        this.caller = caller;
        tableTree = caller.tableTree;
        tableTreeLeaf = caller.tableTreeLeaf;
        category = caller.category;
        cloneTree(caller.treeView, treeView, getIgnoreNode());
    }

    @Override
    public String display(TreeNode node) {
        return node.getTitle();
    }

    @Override
    public String tooltip(TreeNode node) {
        if (node.getAttribute() != null && !node.getAttribute().isBlank()) {
            return node.getTitle() + "\n" + node.getAttribute();
        } else {
            return null;
        }
    }

    public TreeNode root() {
        return tableTree.findAndCreateRoot(category);
    }

    @Override
    public TreeNode root(Connection conn) {
        return tableTree.findAndCreateRoot(conn, category);
    }

    @Override
    public int size(Connection conn, TreeNode node) {
        return TableTree.size(conn, node.getNodeid());
    }

    @Override
    public long id(TreeNode node) {
        return node.getNodeid();
    }

    @Override
    public List<TreeNode> children(Connection conn, TreeNode node) {
        return tableTree.children(conn, id(node));
    }

    @Override
    public List<TreeNode> ancestor(Connection conn, TreeNode node) {
        return tableTree.ancestor(conn, id(node));
    }

    @Override
    public boolean equal(TreeNode node1, TreeNode node2) {
        return node1.getNodeid() == node2.getNodeid();
    }

    @Override
    public TreeNode dummy() {
        return new TreeNode();
    }

    @Override
    public boolean isDummy(TreeNode node) {
        return node.getTitle() != null;
    }

    @Override
    public TreeNode createNode(TreeNode targetNode, String name) {
        if (targetNode == null) {
            return null;
        }
        TreeNode newNode = new TreeNode(targetNode.getNodeid(), name);
        newNode = tableTree.insertData(newNode);
        return newNode;
    }

    @Override
    public String name(TreeNode node) {
        return node.getTitle();
    }

    @FXML
    @Override
    protected void exportNode() {
        TreeNodeExportController exportController
                = (TreeNodeExportController) WindowTools.openStage(Fxmls.TreeNodeExportFxml);
        exportController.setController(treeController);
    }

    @Override
    protected void clearTree(Connection conn, TreeNode node) {
        TableTree.deleteChildren(conn, node.getNodeid());
    }

    @Override
    protected TreeNode rename(TreeNode node, String name) {
        node.setTitle(name);
        return tableTree.updateData(node);
    }

    @Override
    protected void delete(Connection conn, TreeNode node) {
        tableTree.deleteData(conn, node);
    }

    @Override
    protected void copyNode(Boolean onlyContents) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || isRoot(selectedItem.getValue())) {
            return;
        }
        String chainName = chainName(selectedItem);
        TreeNodeCopyController controller
                = (TreeNodeCopyController) WindowTools.openStage(Fxmls.TreeNodeCopyFxml);
        controller.tableTreeLeaf = treeController.tableTreeLeaf;
        controller.setCaller(this, selectedItem.getValue(), chainName, onlyContents);
    }

    @FXML
    @Override
    protected void moveNode() {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || isRoot(selectedItem.getValue())) {
            return;
        }
        String chainName = chainName(selectedItem);
        TreeNodeMoveController controller = (TreeNodeMoveController) WindowTools.openStage(Fxmls.TreeNodeMoveFxml);
        controller.setCaller(this, selectedItem.getValue(), chainName);
    }

    public TreeNodesController oneOpen() {
        TreeNodesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object.getClass().equals(myController.getClass())) {
                try {
                    controller = (TreeNodesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (TreeNodesController) WindowTools.openStage(myFxml);
        }
        if (controller != null) {
            controller.requestMouse();
        }
        return controller;
    }

    @FXML
    @Override
    protected void importExamples() {
        TreeNodeImportController controller = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.importExamples(treeController);
    }

    @FXML
    @Override
    protected void importAction() {
        TreeNodeImportController controller = (TreeNodeImportController) WindowTools.openChildStage(getMyWindow(), Fxmls.TreeNodeImportFxml);
        controller.setManage(treeController);
    }

    @FXML
    public void popFunctionsMenu(MouseEvent event) {
        List<MenuItem> items = makeNodeMenu(event, currectSelected());

        items.add(new SeparatorMenuItem());

        MenuItem menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(treeView, event.getScreenX(), event.getScreenY());
    }

}
