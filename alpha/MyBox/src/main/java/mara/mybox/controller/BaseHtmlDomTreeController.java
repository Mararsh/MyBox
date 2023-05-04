package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TreeTableHierachyCell;
import mara.mybox.fxml.cell.TreeTableTextTrimCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @Author Mara
 * @CreateDate 2023-2-14
 * @License Apache License Version 2.0
 */
public class BaseHtmlDomTreeController extends BaseController {

    @FXML
    protected TreeTableView<HtmlNode> domTree;
    @FXML
    protected TreeTableColumn<HtmlNode, String> hierarchyColumn, tagColumn, textColumn, idColumn,
            classnameColumn, dataColumn, rvalueColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            hierarchyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("tag"));
            hierarchyColumn.setCellFactory(new TreeTableHierachyCell());
            tagColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("tag"));
            textColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("wholeText"));
            textColumn.setCellFactory(new TreeTableTextTrimCell());
            idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
            classnameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("classname"));
            dataColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("data"));
            dataColumn.setCellFactory(new TreeTableTextTrimCell());
            rvalueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
            rvalueColumn.setCellFactory(new TreeTableTextTrimCell());

            domTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            domTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<HtmlNode> item = selected();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popNodeMenu(domTree, makeFunctionsMenu(item));
                    } else {
                        treeClicked(event, item);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void treeClicked(MouseEvent event, TreeItem<HtmlNode> item) {

    }

    public TreeItem<HtmlNode> loadHtml(String html) {
        try {
            if (html == null) {
                clearDom();
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
            return domTree.getRoot().getValue().getElement().html();
        } catch (Exception e) {
            return null;
        }
    }

    public TreeItem<HtmlNode> loadElement(Element element) {
        try {
            clearDom();
            TreeItem<HtmlNode> root = makeTreeItem(element);
            domTree.setRoot(root);
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
            focus(elementItem);
            return elementItem;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void focus(TreeItem<HtmlNode> item) {
        if (item == null) {
            return;
        }
        try {
            domTree.getSelectionModel().select(item);
            int index = domTree.getRow(item);
            domTree.scrollTo(index > 3 ? index - 3 : index);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public TreeItem<HtmlNode> selected() {
        TreeItem<HtmlNode> item = domTree.getSelectionModel().getSelectedItem();
        return validItem(item);
    }

    public TreeItem<HtmlNode> validItem(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = item;
        if (validItem == null) {
            validItem = domTree.getRoot();
        }
        if (validItem == null) {
            return null;
        }
        HtmlNode node = validItem.getValue();
        if (node == null || node.getElement() == null) {
            return null;
        }
        return validItem;
    }

    public String tag(TreeItem<HtmlNode> item) {
        try {
            return item.getValue().getTag();
        } catch (Exception e) {
            return null;
        }
    }

    public String hierarchyNumber(TreeItem<HtmlNode> item) {
        if (item == null) {
            return "";
        }
        TreeItem<HtmlNode> parent = item.getParent();
        if (parent == null) {
            return "";
        }
        String p = hierarchyNumber(parent);
        return (p == null || p.isBlank() ? "" : p + ".") + (parent.getChildren().indexOf(item) + 1);
    }

    public String label(TreeItem<HtmlNode> item) {
        if (item == null) {
            return "";
        }
        return hierarchyNumber(item) + " " + tag(item);
    }

    public boolean isSameLocation(TreeItem<HtmlNode> item1, TreeItem<HtmlNode> item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        String s1 = hierarchyNumber(item1);
        String s2 = hierarchyNumber(item2);
        return s1.equals(s2);
    }

    // true when item1 is a descendant of item2
    public boolean isSameOrDescendantLocation(TreeItem<HtmlNode> item1, TreeItem<HtmlNode> item2) {
        try {
            if (item1 == null || item2 == null) {
                return false;
            }
            String s1 = hierarchyNumber(item1);
            String s2 = hierarchyNumber(item2);
            return s1.startsWith(s2);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public TreeItem<HtmlNode> find(String number) {
        return find(domTree.getRoot(), number);
    }

    public TreeItem<HtmlNode> find(TreeItem<HtmlNode> parent, String number) {
        try {
            if (parent == null || number == null || number.isBlank()) {
                return parent;
            }
            String[] numbers = number.split("\\.", -1);
            if (numbers == null || numbers.length == 0) {
                return null;
            }
            int index;
            TreeItem<HtmlNode> item = parent;
            for (String n : numbers) {
                index = Integer.parseInt(n);
                List<TreeItem<HtmlNode>> children = item.getChildren();
                if (index < 1 || index > children.size()) {
                    return null;
                }
                item = children.get(index - 1);
            }
            return item;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void foldAction() {
        setExpanded(selected(), false);
    }

    @FXML
    public void unfoldAction() {
        setExpanded(selected(), true);
    }

    @FXML
    @Override
    public void refreshAction() {
        updateTreeItem(domTree.getRoot());
    }

    public void setExpanded(TreeItem<HtmlNode> item, boolean unfold) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            return;
        }
        validItem.setExpanded(unfold);
        List<TreeItem<HtmlNode>> children = validItem.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (TreeItem child : children) {
            setExpanded(child, unfold);
        }
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "DomFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        List<MenuItem> items = makeFunctionsMenu(selected());
        if (items == null) {
            return;
        }

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "DomFunctionsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                UserConfig.setBoolean(baseName + "DomFunctionsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(domTree, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> makeFunctionsMenu(TreeItem<HtmlNode> inItem) {
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menuItem = new MenuItem(StringTools.menuPrefix(label(item)));
        menuItem.setStyle("-fx-text-fill: #2e598a;");
        items.add(menuItem);
        items.add(new SeparatorMenuItem());

        menuItem = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPlus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            setExpanded(item, true);
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            setExpanded(item, false);
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            updateTreeItem(item);
        });
        items.add(menuItem);

        items.add(new SeparatorMenuItem());

        items.addAll(viewMenu(item));

        List<MenuItem> more = moreMenu(item);
        if (more != null) {
            items.addAll(more);

        }
        return items;
    }

    public List<MenuItem> viewMenu(TreeItem<HtmlNode> item) {
        List<MenuItem> items = new ArrayList<>();
        if (item == null) {
            return items;
        }
        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
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
            popText(item.getValue().getValue());
        });
        viewMenu.getItems().add(menuItem);

        items.add(new SeparatorMenuItem());

        return items;
    }

    public List<MenuItem> moreMenu(TreeItem<HtmlNode> inItem) {
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
    protected void clearDom() {
        domTree.setRoot(null);
    }

}
