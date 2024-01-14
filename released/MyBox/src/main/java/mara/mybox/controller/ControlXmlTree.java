package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.*;

/**
 * @Author Mara
 * @CreateDate 2023-5-27
 * @License Apache License Version 2.0
 */
public class ControlXmlTree extends BaseTreeTableViewController<XmlTreeNode> {

    protected XmlEditorController xmlEditor;
    protected Document doc;

    @FXML
    protected TreeTableColumn<XmlTreeNode, String> typeColumn;
    @FXML
    protected ControlXmlNodeEdit nodeController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("typename"));

            nodeController.setParameters(this);

            clearTree();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        tree
     */
    @Override
    public void setRoot(TreeItem<XmlTreeNode> root) {
        super.setRoot(root);
        nodeController.clearNode();
    }

    public void makeTree(String xml) {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (xml == null) {
            clearTree();
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            TreeItem<XmlTreeNode> root;

            @Override
            protected boolean handle() {
                try {
                    doc = XmlTools.textToDoc(this, myController, xml);
                    if (doc == null || !isWorking()) {
                        return false;
                    }
                    root = makeTreeItem(new XmlTreeNode(doc));
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                setRoot(root);
                if (error != null) {
                    popError(error);
                }
            }

        };
        start(task);
    }

    public void loadNode(Node doc) {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (doc == null) {
            clearTree();
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            TreeItem<XmlTreeNode> root;

            @Override
            protected boolean handle() {
                root = makeTreeItem(new XmlTreeNode(doc));
                return true;
            }

            @Override
            protected void whenSucceeded() {
                setRoot(root);
                if (error != null) {
                    popError(error);
                }
            }

        };
        start(task);
    }

    public TreeItem<XmlTreeNode> makeTreeItem(XmlTreeNode xmlTreeNode) {
        try {
            if (xmlTreeNode == null) {
                return null;
            }
            TreeItem<XmlTreeNode> item = new TreeItem(xmlTreeNode);
            item.setExpanded(true);
            Node node = xmlTreeNode.getNode();
            if (node == null) {
                return item;
            }
            NodeList children = node.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    addTreeItem(item, -1, new XmlTreeNode(child));
                }
            }
            return item;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public TreeItem<XmlTreeNode> addTreeItem(TreeItem<XmlTreeNode> parent, int index, XmlTreeNode xmlTreeNode) {
        try {
            if (parent == null || xmlTreeNode == null) {
                return null;
            }
            TreeItem<XmlTreeNode> item = makeTreeItem(xmlTreeNode);
            if (item == null) {
                return null;
            }
            ObservableList<TreeItem<XmlTreeNode>> parentChildren = parent.getChildren();
            if (index >= 0 && index < parentChildren.size() - 1) {
                parentChildren.add(index, item);
            } else {
                parentChildren.add(item);
            }
            return item;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<XmlTreeNode> item) {
        nodeController.editNode(item);
    }

    @Override
    public void focusItem(TreeItem<XmlTreeNode> item) {
        super.focusItem(item);
        itemClicked(null, item);
    }

    /*
        values
     */
    public Node selectedNode() {
        TreeItem<XmlTreeNode> selecteItem = selected();
        return selecteItem == null ? null : selecteItem.getValue().getNode();
    }

    @Override
    public boolean validNode(XmlTreeNode node) {
        return node != null && node.getNode() != null;
    }

    @Override
    public String title(XmlTreeNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(XmlTreeNode node) {
        return node == null ? null : node.getValue();
    }

    @Override
    public String copyTitleMessage() {
        return message("CopyName");
    }

    /*
        actions
     */
    @Override
    public List<MenuItem> viewMenuItems(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        List<MenuItem> items = foldMenuItems(treeItem);
        MenuItem menu;

        XmlTreeNode node = treeItem.getValue();
        if (node == null) {
            return items;
        }
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("ViewNode"), StyleTools.getIconImageView("iconPop.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            viewNode(treeItem);
        });
        items.add(menu);

        String xml = XmlTools.transform(node.getNode());
        if (xml != null && !xml.isBlank()) {
            menu = new MenuItem(message("NodeXML"), StyleTools.getIconImageView("iconXML.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextPopController.loadText(xml);
            });
            items.add(menu);
        }

        String text = node.getNode() == null ? null : node.getNode().getTextContent();
        if (text != null && !text.isBlank()) {
            menu = new MenuItem(message("NodeTexts"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextPopController.loadText(text);
            });
            items.add(menu);
        }

        List<MenuItem> more = viewMoreItems(treeItem);
        if (more != null && !more.isEmpty()) {
            items.addAll(more);
        }

        items.add(new SeparatorMenuItem());

        if (xml != null && !xml.isBlank()) {
            menu = new MenuItem(message("CopyNodeXmlCodes"), StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextClipboardTools.copyToSystemClipboard(this, xml);
            });
            items.add(menu);
        }

        if (text != null && !text.isBlank()) {
            menu = new MenuItem(message("CopyNodeTextContent"), StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextClipboardTools.copyToSystemClipboard(this, text);
            });
            items.add(menu);
        }

        String value = node.getValue();
        if (value != null && !value.isBlank()) {
            menu = new MenuItem(copyValueMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextClipboardTools.copyToSystemClipboard(this, value);
            });
            items.add(menu);
        }

        String title = node.getTitle();
        if (title != null && !title.isBlank()) {
            menu = new MenuItem(copyTitleMessage(), StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextClipboardTools.copyToSystemClipboard(this, title);
            });
            items.add(menu);
        }

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshAction();
        });
        items.add(menu);

        return items;
    }

    public List<MenuItem> viewMoreItems(TreeItem<XmlTreeNode> treeItem) {
        return null;
    }

    @Override
    public List<MenuItem> functionMenuItems(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        items.add(viewMenu);
        viewMenu.getItems().addAll(viewMenuItems(treeItem));

        viewMenu.getItems().add(new SeparatorMenuItem());

        items.addAll(modifyMenus(treeItem));

        return items;
    }

    public List<MenuItem> modifyMenus(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            XmlAddNodeController.open(this, treeItem);
        });
        menu.setDisable(treeItem.getValue() == null || !treeItem.getValue().canAddNode());
        items.add(menu);

        String clip = TextClipboardTools.getSystemClipboardString();
        menu = new MenuItem(message("PasteXmlCodesAsNewNode"), StyleTools.getIconImageView("iconPaste.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            paste(clip, treeItem);
        });
        menu.setDisable(clip == null || clip.isBlank()
                || treeItem.getValue() == null || !treeItem.getValue().canAddNode());
        items.add(menu);

        menu = new MenuItem(message("DeleteNode"), StyleTools.getIconImageView("iconDelete.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNode(treeItem);
        });
        items.add(menu);

        menu = new MenuItem(message("DuplicateAfterNode"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(treeItem, true);
        });
        menu.setDisable(treeItem.getParent() == null);
        items.add(menu);

        menu = new MenuItem(message("DuplicateToParentEnd"), StyleTools.getIconImageView("iconCopy.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(treeItem, false);
        });
        menu.setDisable(treeItem.getParent() == null);
        items.add(menu);

        if (xmlEditor != null && xmlEditor.sourceFile != null && xmlEditor.sourceFile.exists()) {
            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("RecoverFile"), StyleTools.getIconImageView("iconRecover.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                recoverAction();
            });
            items.add(menu);
        }

        return items;
    }

    public void paste(String text, TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null || text == null || text.isBlank()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            Node newNode;

            @Override
            protected boolean handle() {
                try {
                    Element element = XmlTools.toElement(this, myController, text);
                    if (element == null || !isWorking()) {
                        return false;
                    }
                    newNode = doc.importNode(element, true);
                    if (newNode == null) {
                        error = message("InvalidFormat");
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    treeItem.getValue().getNode().appendChild(newNode);

                    TreeItem<XmlTreeNode> newItem = addTreeItem(treeItem, -1, new XmlTreeNode(newNode));

                    focusItem(newItem);
                    xmlEditor.domChanged(true);
                    xmlEditor.popInformation(message("CreatedSuccessfully"));
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

        };
        start(task);
    }

    public void deleteNode(TreeItem<XmlTreeNode> treeItem) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<XmlTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                if (PopTools.askSure(getTitle(), message("SureClear"))) {
                    clearTree();
                }
                return;
            }
            int index = parentItem.getChildren().indexOf(treeItem);
            if (index < 0) {
                return;
            }
            Node parentNode = parentItem.getValue().getNode();
            parentNode.removeChild(treeItem.getValue().getNode());
            parentItem.getChildren().remove(index);

            xmlEditor.domChanged(true);
            xmlEditor.popInformation(message("DeletedSuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void duplicate(TreeItem<XmlTreeNode> treeItem, boolean afterNode) {
        try {
            if (treeItem == null) {
                return;
            }
            TreeItem<XmlTreeNode> parentItem = treeItem.getParent();
            if (parentItem == null) {
                return;
            }
            int index = parentItem.getChildren().indexOf(treeItem);
            if (index < 0) {
                return;
            }
            Node xmlNode = treeItem.getValue().getNode();
            Node newNode = xmlNode.cloneNode(true);
            Node parentNode = xmlNode.getParentNode();

            TreeItem<XmlTreeNode> newTreeItem;
            if (afterNode && index < parentItem.getChildren().size() - 1) {
                parentNode.insertBefore(newNode, xmlNode.getNextSibling());
                newTreeItem = addTreeItem(parentItem, index + 1, new XmlTreeNode(newNode));
            } else {
                parentNode.appendChild(newNode);
                newTreeItem = addTreeItem(parentItem, -1, new XmlTreeNode(newNode));
            }

            focusItem(newTreeItem);

            xmlEditor.domChanged(true);
            xmlEditor.popInformation(message("CopySuccessfully"));

            nodeController.clearNode();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadNode(doc);
    }

    @FXML
    @Override
    public void recoverAction() {
        xmlEditor.recoverAction();
    }

}
