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
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TreeTableTextTrimCell;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @Author Mara
 * @CreateDate 2023-2-14
 * @License Apache License Version 2.0
 */
public class BaseHtmlTreeController extends BaseTreeViewController<HtmlNode> {

    @FXML
    protected TreeTableColumn<HtmlNode, String> idColumn, classnameColumn, dataColumn, rvalueColumn;

    /*
        init
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
            classnameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("classname"));
            dataColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("data"));
            dataColumn.setCellFactory(new TreeTableTextTrimCell());
            rvalueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
            rvalueColumn.setCellFactory(new TreeTableTextTrimCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    public TreeItem<HtmlNode> loadHtml(String html) {
        try {
            if (html == null) {
                clearTree();
                return null;
            } else {
                return loadElement(Jsoup.parse(html));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String html() {
        try {
            return treeView.getRoot().getValue().getElement().html();
        } catch (Exception e) {
            return null;
        }
    }

    public TreeItem<HtmlNode> loadElement(Element element) {
        try {
            clearTree();
            TreeItem<HtmlNode> root = makeTreeItem(element);
            treeView.setRoot(root);
            return root;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<HtmlNode> makeTreeItem(Element element) {
        try {
            if (element == null) {
                return null;
            }
            TreeItem<HtmlNode> elementItem = new TreeItem(new HtmlNode(element));
            elementItem.setExpanded(true);
            Elements elementChildren = element.children();
            if (elementChildren != null) {
                for (Element child : elementChildren) {
                    addTreeItem(elementItem, child);
                }
            }
            return elementItem;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<HtmlNode> addTreeItem(TreeItem<HtmlNode> parent, Element element) {
        return addTreeItem(parent, -1, element);
    }

    public TreeItem<HtmlNode> addTreeItem(TreeItem<HtmlNode> parent, int index, Element element) {
        try {
            if (parent == null || element == null) {
                return null;
            }
            TreeItem<HtmlNode> elementItem = makeTreeItem(element);
            if (elementItem == null) {
                return null;
            }
            ObservableList<TreeItem<HtmlNode>> parentChildren = parent.getChildren();
            if (index >= 0 && index < parentChildren.size() - 1) {
                parentChildren.add(index, elementItem);
            } else {
                parentChildren.add(elementItem);
            }
            return elementItem;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<HtmlNode> addElement(TreeItem<HtmlNode> parent, Element element) {
        return addElement(parent, -1, element);
    }

    public TreeItem<HtmlNode> addElement(TreeItem<HtmlNode> parent, int index, Element element) {
        try {
            if (parent == null || element == null) {
                return null;
            }
            Element parentElement = parent.getValue().getElement();
            if (index >= 0 && index < parentElement.childrenSize()) {
                parentElement.insertChildren(index, element);
            } else {
                parentElement.appendChild(element);
            }
            return updateTreeItem(parent, parentElement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<HtmlNode> updateTreeItem(TreeItem<HtmlNode> item) {
        try {
            if (item == null) {
                return null;
            }
            return updateTreeItem(item, item.getValue().getElement());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public TreeItem<HtmlNode> updateTreeItem(TreeItem<HtmlNode> item, Element element) {
        try {
            if (item == null || element == null) {
                return null;
            }
            TreeItem<HtmlNode> parent = item.getParent();
            if (parent == null) {
                return loadElement(element);
            }
            int index = parent.getChildren().indexOf(item);
            if (index < 0) {
                return null;
            }
            TreeItem<HtmlNode> elementItem = makeTreeItem(element);
            if (elementItem == null) {
                return null;
            }
            parent.getChildren().set(index, elementItem);
            focusItem(elementItem);
            return elementItem;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        values
     */
    @Override
    public boolean validNode(HtmlNode node) {
        return node != null && node.getElement() != null;
    }

    @Override
    public String title(HtmlNode node) {
        return node == null ? null : node.getTitle();
    }

    @Override
    public String value(HtmlNode node) {
        return node == null ? null : node.getValue();
    }

    /*
        actions
     */
    @Override
    public List<MenuItem> functionItems(TreeItem<HtmlNode> item) {
        List<MenuItem> items = new ArrayList<>();

        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        items.add(viewMenu);

        viewMenu.getItems().addAll(viewItems(item));

        items.add(new SeparatorMenuItem());

        items.addAll(codesMenu(item));

        List<MenuItem> more = moreMenu(item);
        if (more != null) {
            items.add(new SeparatorMenuItem());
            items.addAll(more);
        }
        return items;
    }

    public List<MenuItem> codesMenu(TreeItem<HtmlNode> item) {
        List<MenuItem> items = new ArrayList<>();
        if (item == null) {
            return items;
        }
        Menu viewMenu = new Menu(message("Codes"), StyleTools.getIconImageView("iconMeta.png"));
        items.add(viewMenu);

        MenuItem menuItem = new MenuItem(message("InnerHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popCodes(item, false);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("OuterHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popCodes(item, true);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("WholeHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popCodes(null, true);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("ElementRawText"), StyleTools.getIconImageView("iconTxt.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popText(item.getValue().getWholeOwnText());
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("ElementWholeText"), StyleTools.getIconImageView("iconTxt.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popText(item.getValue().getWholeText());
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("ElementNormalizedText"), StyleTools.getIconImageView("iconTxt.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popText(item.getValue().getText());
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Data"), StyleTools.getIconImageView("iconTxt.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popText(item.getValue().getData());
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Value"), StyleTools.getIconImageView("iconTxt.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popText(item.getValue().getElementValue());
        });
        viewMenu.getItems().add(menuItem);

        return items;
    }

    public List<MenuItem> moreMenu(TreeItem<HtmlNode> item) {
        return null;
    }

    public void popCodes(TreeItem<HtmlNode> item, boolean outer) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            return;
        }
        HtmlNode node = validItem.getValue();
        popText(outer ? node.getOuterHtml() : node.getInnerHtml());
    }

    public void popText(String text) {
        if (text == null || text.isBlank()) {
            popInformation(message("NullOrBlank"));
            return;
        }
        TextPopController.loadText(this, text);
    }

    @FXML
    @Override
    public void refreshAction() {
        updateTreeItem(treeView.getRoot());
    }
}
