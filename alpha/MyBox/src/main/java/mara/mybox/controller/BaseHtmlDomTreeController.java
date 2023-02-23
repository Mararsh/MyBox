package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
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
import javafx.scene.robot.Robot;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
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
public class BaseHtmlDomTreeController extends BaseController {

    protected int count;

    @FXML
    protected TreeTableView<HtmlNode> domTree;
    @FXML
    protected TreeTableColumn<HtmlNode, String> sequenceColumn, tagColumn, textColumn, idColumn,
            classnameColumn, dataColumn, rvalueColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            sequenceColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("serialNumber"));
            tagColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("tag"));
            textColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("textStart"));
            idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
            classnameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("classname"));
            dataColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("dataStart"));
            rvalueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("valueStart"));

            domTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            domTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<HtmlNode> item = domTree.getSelectionModel().getSelectedItem();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popFunctionsMenu(event, item);
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

    public void load(String html) {
        try {
            load(Jsoup.parse(html));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String html() {
        try {
            return domTree.getRoot().getValue().getElement().html();
        } catch (Exception e) {
            return null;
        }
    }

    public TreeItem<HtmlNode> load(Element element) {
        try {
            clearDom();
            if (element == null) {
                return null;
            }
            TreeItem<HtmlNode> root = new TreeItem(new HtmlNode(element));
            root.setExpanded(true);
            domTree.setRoot(root);

            Elements children = element.children();
            if (children == null || children.isEmpty()) {
                return root;
            }
            for (Element child : children) {
                createTreeNode(root, -1, child);
            }
            return root;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // element is not changed
    public void createTreeNode(TreeItem<HtmlNode> parent, int childIndex, Element element) {
        try {
            TreeItem<HtmlNode> elementNode = new TreeItem(new HtmlNode(element));
            elementNode.setExpanded(true);
            ObservableList<TreeItem<HtmlNode>> parentChildren = parent.getChildren();
            if (childIndex >= 0 && childIndex < parentChildren.size() - 1) {
                parentChildren.add(childIndex, elementNode);
            } else {
                parentChildren.add(elementNode);
            }
            count++;
            Elements elementChildren = element.children();
            if (elementChildren != null) {
                for (Element child : elementChildren) {
                    createTreeNode(elementNode, -1, child);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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

    public void updateDom(TreeItem<HtmlNode> item) {
        try {
            TreeItem<HtmlNode> validItem = validItem(item);
            if (validItem == null) {
                return;
            }
            Element element = item.getValue().getElement();
            element.children().clear();
            List< TreeItem<HtmlNode>> children = item.getChildren();
            if (children == null || children.isEmpty()) {
                return;
            }
            for (TreeItem<HtmlNode> child : children) {
                element.children().add(child.getValue().getElement());
                updateDom(child);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void foldAction() {
        setExpanded(domTree.getSelectionModel().getSelectedItem(), false);
    }

    @FXML
    public void unfoldAction() {
        setExpanded(domTree.getSelectionModel().getSelectedItem(), true);
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

    public void popFunctionsMenu(MouseEvent event, TreeItem<HtmlNode> inItem) {
        if (getMyWindow() == null) {
            return;
        }
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menuItem = new MenuItem(item.getValue().getSerialNumber() + "  "
                + PopTools.limitMenuName(item.getValue().getTag()));
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

        items.add(new SeparatorMenuItem());

        items.addAll(viewMenu(item));

        List<MenuItem> more = moreMenu(item);
        if (more != null) {
            items.addAll(more);
        }

        menuItem = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
        menuItem.setStyle("-fx-text-fill: #2e598a;");
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menuItem);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        if (event == null) {
            Robot r = new Robot();
            popMenu.show(domTree, r.getMouseX() + 40, r.getMouseY() + 20);
        } else {
            popMenu.show(domTree, event.getScreenX(), event.getScreenY());
        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent event) {
        popFunctionsMenu(event, domTree.getSelectionModel().getSelectedItem());
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
