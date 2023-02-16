package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
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
import mara.mybox.fxml.PopTools;
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

    protected ObservableList<Attribute> attributesData;
    protected ControlHtmlEditor htmlEditor;
    protected TreeItem<HtmlNode> currentItem, copiedItem;

    @FXML
    protected TreeTableView<HtmlNode> domTree;
    @FXML
    protected TreeTableColumn<HtmlNode, String> tagColumn, textColumn, idColumn, classnameColumn;
    @FXML
    protected VBox elementBox;
    @FXML
    protected TextField tagInput;
    @FXML
    protected TextArea textInput;
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
            textColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("textStart"));
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
                        editDomNode(item);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(String html) {
        try {
            clearDom();
            if (html == null) {
                return;
            }
            Document doc = Jsoup.parse(html);
            makeItem(null, doc);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String html() {
        try {
            HtmlNode htmlNode = domTree.getRoot().getValue();
            return htmlNode.getElement().html();
        } catch (Exception e) {
            return null;
        }
    }

    public TreeItem<HtmlNode> makeItem(TreeItem<HtmlNode> parent, Element element) {
        try {
            if (element == null) {
                return parent;
            }
            TreeItem<HtmlNode> node = new TreeItem(new HtmlNode(element));
            if (parent == null) {
                node.setExpanded(true);
                domTree.setRoot(node);
            } else {
                node.setExpanded(false);
                parent.getChildren().add(node);
            }

            Elements children = element.children();
            if (children == null || children.isEmpty()) {
                return node;
            }
            for (Element child : children) {
                makeItem(node, child);
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
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
        unfold(domTree.getSelectionModel().getSelectedItem(), false);
    }

    @FXML
    public void unfoldAction() {
        unfold(domTree.getSelectionModel().getSelectedItem(), true);
    }

    public void unfold(TreeItem<HtmlNode> item, boolean unfold) {
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
            unfold(child, unfold);
        }
    }

    public void codes(TreeItem<HtmlNode> item, boolean outer) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            return;
        }
        HtmlNode node = validItem.getValue();
        popText(outer ? node.getOuterHtml() : node.getInnerHtml());
    }

    public void add(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        Element newElement = new Element("div");
        TreeItem<HtmlNode> newItem = new TreeItem<>(new HtmlNode(newElement));
        validItem.getChildren().add(newItem);
        validItem.getValue().getElement().appendChild(newElement);
        domTree.getSelectionModel().select(newItem);
        htmlEditor.domChanged(true);
    }

    public void cut(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        copiedItem = validItem;
        remove(validItem);
    }

    public void remove(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = validItem.getParent();
        if (parent == null) {
            return;
        }
        int pos = parent.getChildren().indexOf(validItem);
        if (pos < 0) {
            return;
        }
        parent.getChildren().remove(pos);
        parent.getValue().getElement().children().remove(pos);
        htmlEditor.domChanged(true);
    }

    public void duplicate(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = validItem.getParent();
        if (parent == null) {
            return;
        }
        int pos = parent.getChildren().indexOf(validItem);
        if (pos < 0) {
            return;
        }
        HtmlNode newNode = validItem.getValue().copy();
        TreeItem<HtmlNode> newItem = new TreeItem<>(newNode);
        parent.getChildren().add(pos + 1, newItem);
        parent.getValue().getElement().children().add(pos + 1, newNode.getElement());
        domTree.getSelectionModel().select(newItem);
        htmlEditor.domChanged(true);
    }

    public void copy(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        copiedItem = new TreeItem(validItem.getValue().copy());
    }

    public void paste(TreeItem<HtmlNode> parent) {
        if (copiedItem == null || parent == null) {
            popError(message("NoData"));
            return;
        }
        parent.getChildren().add(copiedItem);
        parent.getValue().getElement().appendChild(copiedItem.getValue().getElement());
        domTree.getSelectionModel().select(copiedItem);
        htmlEditor.domChanged(true);
    }

    public void up(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = validItem.getParent();
        if (parent == null) {
            return;
        }
        int pos = parent.getChildren().indexOf(validItem);
        if (pos <= 0) {
            return;
        }
        Element thisElement = validItem.getValue().getElement();
        TreeItem<HtmlNode> upItem = parent.getChildren().get(pos - 1);
        Element upElement = upItem.getValue().getElement();
        parent.getChildren().set(pos, upItem);
        parent.getChildren().set(pos - 1, validItem);
        parent.getValue().getElement().children().set(pos, upElement);
        parent.getValue().getElement().children().set(pos - 1, thisElement);
        domTree.getSelectionModel().select(validItem);
        htmlEditor.domChanged(true);
    }

    public void down(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = validItem.getParent();
        if (parent == null) {
            return;
        }
        int pos = parent.getChildren().indexOf(validItem);
        if (pos == parent.getChildren().size() - 1) {
            return;
        }
        Element thisElement = validItem.getValue().getElement();
        TreeItem<HtmlNode> downItem = parent.getChildren().get(pos + 1);
        Element downElement = downItem.getValue().getElement();
        parent.getChildren().set(pos + 1, validItem);
        parent.getChildren().set(pos, downItem);
        parent.getValue().getElement().children().set(pos + 1, thisElement);
        parent.getValue().getElement().children().set(pos, downElement);
        domTree.getSelectionModel().select(validItem);
        htmlEditor.domChanged(true);
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
        MenuItem menuItem = new MenuItem(PopTools.limitMenuName(item.getValue().getTag()));
        menuItem.setStyle("-fx-text-fill: #2e598a;");
        items.add(menuItem);
        items.add(new SeparatorMenuItem());

        menuItem = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPLus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            unfold(item, true);
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            unfold(item, false);
        });
        items.add(menuItem);

        items.add(new SeparatorMenuItem());

        Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
        items.add(viewMenu);

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

        menuItem = new MenuItem(message("InnerHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            codes(item, false);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("OuterHtml"), StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            codes(item, true);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem("WholeHtml", StyleTools.getIconImageView("iconMeta.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            codes(null, true);
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Data"), StyleTools.getIconImageView("iconData.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popText(item.getValue().getData());
        });
        viewMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Value"), StyleTools.getIconImageView("iconData.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            popText(item.getValue().getValue());
        });
        viewMenu.getItems().add(menuItem);

        items.add(new SeparatorMenuItem());

        Menu modifyMenu = new Menu(message("Modify"), StyleTools.getIconImageView("iconEdit.png"));
        items.add(modifyMenu);

        menuItem = new MenuItem(message("Add"), StyleTools.getIconImageView("iconAdd.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            add(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Remove"), StyleTools.getIconImageView("iconDelete.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            remove(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Duplicate"), StyleTools.getIconImageView("iconCopy.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            copiedItem = item;
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Paste"), StyleTools.getIconImageView("iconPaste.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            paste(item);
        });
        menuItem.setDisable(copiedItem == null);
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Crop"), StyleTools.getIconImageView("iconCrop.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            cut(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("MoveUp"), StyleTools.getIconImageView("iconUp.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            up(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("MoveDown"), StyleTools.getIconImageView("iconDown.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            down(item);
        });
        modifyMenu.getItems().add(menuItem);

        items.add(new SeparatorMenuItem());

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

    public void editDomNode(TreeItem<HtmlNode> item) {
        clearNode();
        currentItem = validItem(item);
        if (currentItem == null) {
            popError(message("NoData"));
            return;
        }
        HtmlNode currentNode = currentItem.getValue();
        tagInput.setText(currentNode.getTag());
        textInput.setText(currentNode.getWholeOwnText());
        Attributes attributes = currentNode.getAttributes();
        if (attributes != null) {
            for (Attribute a : attributes) {
                attributesData.add(a);
            }
        }
        elementBox.setDisable(false);
    }

    public void clearNode() {
        currentItem = null;
        tagInput.clear();
        textInput.clear();
        attributesData.clear();
        elementBox.setDisable(true);
    }

    @FXML
    public void okDomNode() {
        if (currentItem == null) {
            return;
        }
        HtmlNode currentNode = currentItem.getValue();
        Element e = currentNode.getElement();
        e.tagName(tagInput.getText());
        e.text(textInput.getText());
        e.clearAttributes();
        for (Attribute a : attributesData) {
            e.attr(a.getKey(), a.getValue());
        }
        currentItem.setValue(currentNode);
        htmlEditor.domChanged(true);
    }

    @FXML
    public void recoverDomNode() {
        editDomNode(currentItem);
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
