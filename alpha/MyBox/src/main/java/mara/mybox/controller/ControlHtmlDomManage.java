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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-14
 * @License Apache License Version 2.0
 */
public class ControlHtmlDomManage extends BaseHtmlDomTreeController {

    protected ObservableList<Attribute> attributesData;
    protected ControlHtmlEditor htmlEditor;
    protected TreeItem<HtmlNode> currentItem, copiedItem;

    @FXML
    protected ControlHtmlDomNode nodeController;
    @FXML
    protected VBox elementBox;

    public void setEditor(ControlHtmlEditor htmlEditor) {
        try {
            super.initValues();

            this.htmlEditor = htmlEditor;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        tree
     */
    @Override
    public void treeClicked(MouseEvent event, TreeItem<HtmlNode> item) {
        editNode(item);
    }

    public void add(TreeItem<HtmlNode> inItem) {
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            popError(message("NoData"));
            return;
        }
        HtmlDomAddController.open(htmlEditor, domTree.getRow(item));
    }

    public void copy(TreeItem<HtmlNode> inItem) {
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            popError(message("NoData"));
            return;
        }
        HtmlDomCopyController.open(htmlEditor, domTree.getRow(item));
    }

    public void cut(TreeItem<HtmlNode> item) {
        TreeItem<HtmlNode> validItem = validItem(item);
        if (validItem == null) {
            popError(message("NoData"));
            return;
        }
        copiedItem = validItem;
        delete(validItem);
    }

    public void delete(TreeItem<HtmlNode> item) {
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

    public void moveTo(TreeItem<HtmlNode> item) {
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

    @Override
    public List<MenuItem> moreMenu(TreeItem<HtmlNode> item) {
        List<MenuItem> items = new ArrayList<>();
        if (item == null) {
            return items;
        }
        Menu modifyMenu = new Menu(message("ModifyTree"), StyleTools.getIconImageView("iconTree.png"));
        items.add(modifyMenu);

        MenuItem menuItem = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            add(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("CopyNodes"), StyleTools.getIconImageView("iconCopy.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            copy(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("Duplicate"), StyleTools.getIconImageView("iconCopy.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            duplicate(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("DeleteNode"), StyleTools.getIconImageView("iconDelete.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            delete(item);
        });
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

        menuItem = new MenuItem(message("MoveNodes"), StyleTools.getIconImageView("iconDown.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            moveTo(item);
        });
        modifyMenu.getItems().add(menuItem);

        menuItem = new MenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            editNode(item);
        });
        items.add(menuItem);

        items.add(new SeparatorMenuItem());

        return items;
    }

    @FXML
    @Override
    protected void clearDom() {
        super.clearDom();
        nodeController.load(null);
        elementBox.setDisable(true);
    }

    /*
        node
     */
    public void editNode(TreeItem<HtmlNode> item) {
        currentItem = item;
        if (item == null) {
            elementBox.setDisable(true);
            return;
        }
        nodeController.load(item.getValue().getElement());
        elementBox.setDisable(false);
    }

    @FXML
    public void okNode() {
        if (currentItem == null) {
            return;
        }
        Element e = nodeController.pickValues();
        if (e == null) {
            popError(message("Invalid"));
            return;
        }
        currentItem.setValue(new HtmlNode(e));
    }

    @FXML
    public void recoverNode() {
        nodeController.recover();
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
