package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @Author Mara
 * @CreateDate 2023-2-14
 * @License Apache License Version 2.0
 */
public class ControlHtmlDom extends BaseController {

    protected HtmlNode currentNode;
    protected ObservableList<Attribute> attributesData;

    @FXML
    protected TreeTableView<HtmlNode> domTree;
    @FXML
    protected TreeTableColumn<HtmlNode, String> tagColumn, idColumn, classnameColumn;
    @FXML
    protected VBox elementBox;
    @FXML
    protected TextField tagInput;
    @FXML
    protected TextArea textInput, valueInput;
    @FXML
    protected TableView<Attribute> attributesTable;
    @FXML
    protected TableColumn<Attribute, String> keyColumn, valueColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTree();
            initNode();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    public void initTree() {
        try {
            tagColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("tag"));
            idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
            classnameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("classname"));

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
                        if (item == null) {
                            return;
                        }
                        editDomNode(item.getValue());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(String html) {
        try {
            TreeItem treeRoot = clearDom();
            if (html == null) {
                return;
            }
            Document doc = Jsoup.parse(html);
            makeNodes(treeRoot, doc);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String html() {
        try {
            HtmlNode htmlNode = domTree.getRoot().getChildren().get(0).getValue();
            return htmlNode.getElement().html();
        } catch (Exception e) {
            return null;
        }
    }

    public void makeNodes(TreeItem parent, Element element) {
        try {
            if (parent == null || element == null) {
                return;
            }
            TreeItem<HtmlNode> node = new TreeItem(new HtmlNode(element));
            node.setExpanded(false);
            parent.getChildren().add(node);

            Elements children = element.children();
            if (children == null || children.isEmpty()) {
                return;
            }
            for (Element child : children) {
                makeNodes(node, child);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void foldAction() {
        unfold(domTree.getSelectionModel().getSelectedItem(), false);
    }

    @FXML
    public void unfoldAction() {
        unfold(domTree.getSelectionModel().getSelectedItem(), true);
    }

    public void unfold(TreeItem item, boolean unfold) {
        TreeItem nodeItem = item;
        if (item == null) {
            nodeItem = domTree.getRoot();
        }
        nodeItem.setExpanded(unfold);
        List<TreeItem> children = nodeItem.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (TreeItem child : children) {
            unfold(child, unfold);
        }
    }

    @FXML
    public void codesAction() {
        codes(domTree.getSelectionModel().getSelectedItem());
    }

    public void codes(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> nodeItem = item;
        if (item == null) {
            nodeItem = domTree.getRoot();
        }
        HtmlNode node = nodeItem.getValue();
        if (node == null) {
            return;
        }
        String html = node.getInnerHtml();
        if (html != null) {
            TextPopController.loadText(this, html);
        }
    }

    public void popFunctionsMenu(MouseEvent event, TreeItem<HtmlNode> node) {
        if (getMyWindow() == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPLus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfold(node, true);
        });
        items.add(menu);

        menu = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfold(node, false);
        });
        items.add(menu);

        menu = new MenuItem(message("Codes"), StyleTools.getIconImageView("iconMeta.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            codes(node);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
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
        if (event == null) {
            Robot r = new Robot();
            popMenu.show(domTree, r.getMouseX() + 40, r.getMouseY() + 20);
        } else {
            popMenu.show(domTree, event.getScreenX(), event.getScreenY());
        }
    }

    @FXML
    protected TreeItem clearDom() {
        TreeItem treeRoot = new TreeItem(new HtmlNode("DOM"));
        treeRoot.setExpanded(true);
        domTree.setRoot(treeRoot);
        return treeRoot;
    }

    /*
        Node
     */
    public void initNode() {
        try {

            attributesData = FXCollections.observableArrayList();
            attributesTable.setItems(attributesData);
            keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            keyColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            keyColumn.setOnEditCommit((TableColumn.CellEditEvent<Attribute, String> t) -> {
                if (t == null) {
                    return;
                }
                Attribute row = t.getRowValue();
                if (row == null) {
                    return;
                }
                row.setKey(t.getNewValue());
            });
            keyColumn.getStyleClass().add("editable-column");

            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            valueColumn.setOnEditCommit((TableColumn.CellEditEvent<Attribute, String> t) -> {
                if (t == null) {
                    return;
                }
                Attribute row = t.getRowValue();
                if (row == null) {
                    return;
                }
                row.setValue(t.getNewValue());
            });
            valueColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void editDomNode(HtmlNode node) {
        clearNode();
        if (node == null) {
            return;
        }
        currentNode = node;
        if (currentNode == null || currentNode.getElement() == null) {
            currentNode = null;
            return;
        }
        tagInput.setText(currentNode.getTag());
        textInput.setText(currentNode.getText());
        valueInput.setText(currentNode.getValue());
        Attributes attributes = currentNode.getAttributes();
        if (attributes != null) {
            for (Attribute a : attributes) {
                attributesData.add(a);
            }
        }
        elementBox.setDisable(false);
    }

    public void clearNode() {
        currentNode = null;
        tagInput.clear();
        textInput.clear();
        valueInput.clear();
        attributesData.clear();
        elementBox.setDisable(true);
    }

    @FXML
    public void okDomNode() {
        if (currentNode == null) {
            return;
        }
        Element e = currentNode.getElement();
        if (e == null) {
            return;
        }
        e.tagName(tagInput.getText());
        e.text(textInput.getText());
        e.val(valueInput.getText());
        e.clearAttributes();
        for (Attribute a : attributesData) {
            e.attr(a.getKey(), a.getValue());
        }
    }

    @FXML
    public void recoverDomNode() {
        editDomNode(currentNode);
    }

    @FXML
    public void addAttribute() {
        attributesData.add(new Attribute("k", "v"));
    }

    @FXML
    public void deleteAttributes() {
        try {
            List<Attribute> selected = attributesTable.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                return;
            }
            attributesData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void clearAttributes() {
        attributesData.clear();
    }

    /*
        pane
     */
    @Override
    public void cleanPane() {
        try {

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
