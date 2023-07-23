package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
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
public class ControlHtmlDomManage extends BaseHtmlTreeController {

    protected ObservableList<Attribute> attributesData;
    protected ControlHtmlEditor htmlEditor;
    protected TreeItem<HtmlNode> currentItem, copiedItem;

    @FXML
    protected ControlHtmlDomNode nodeController;
    @FXML
    protected TextArea codesArea;
    @FXML
    protected Tab attributesTab, codesTab;
    @FXML
    protected Label tagLabel;

    public void setEditor(ControlHtmlEditor htmlEditor) {
        try {
            super.initValues();

            this.htmlEditor = htmlEditor;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        tree
     */
    @Override
    public void itemClicked(MouseEvent event, TreeItem<HtmlNode> item) {
        editNode(item);
    }

    @Override
    public List<MenuItem> moreMenu(TreeItem<HtmlNode> item) {
        List<MenuItem> items = new ArrayList<>();
        if (item == null) {
            return items;
        }
        MenuItem menuItem = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            add(item);
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("DeleteNode"), StyleTools.getIconImageView("iconDelete.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            delete(item);
        });
        items.add(menuItem);

        menuItem = new MenuItem(message("DeleteNodes"), StyleTools.getIconImageView("iconDelete.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            deleteNodes(item);
        });
        items.add(menuItem);

        if (treeView.getTreeItemLevel(item) > 1) {
            menuItem = new MenuItem(message("CopyNodes"), StyleTools.getIconImageView("iconCopy.png"));
            menuItem.setOnAction((ActionEvent menuItemEvent) -> {
                copy(item);
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("DuplicateAfterNode"), StyleTools.getIconImageView("iconCopy.png"));
            menuItem.setOnAction((ActionEvent menuItemEvent) -> {
                duplicate(item, true);
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("DuplicateToParentEnd"), StyleTools.getIconImageView("iconCopy.png"));
            menuItem.setOnAction((ActionEvent menuItemEvent) -> {
                duplicate(item, false);
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("MoveUp"), StyleTools.getIconImageView("iconUp.png"));
            menuItem.setOnAction((ActionEvent menuItemEvent) -> {
                up(item);
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("MoveDown"), StyleTools.getIconImageView("iconDown.png"));
            menuItem.setOnAction((ActionEvent menuItemEvent) -> {
                down(item);
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("MoveNodes"), StyleTools.getIconImageView("iconRef.png"));
            menuItem.setOnAction((ActionEvent menuItemEvent) -> {
                move(item);
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());
        }

        menuItem = new MenuItem(message("EditNode"), StyleTools.getIconImageView("iconEdit.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
            editNode(item);
        });
        items.add(menuItem);

        if (htmlEditor != null && htmlEditor.sourceFile != null && htmlEditor.sourceFile.exists()) {
            items.add(new SeparatorMenuItem());
            menuItem = new MenuItem(message("Recover"), StyleTools.getIconImageView("iconRecover.png"));
            menuItem.setOnAction((ActionEvent menuItemEvent) -> {
                recoverAction();
            });
            items.add(menuItem);
        }

        return items;
    }

    public void add(TreeItem<HtmlNode> item) {
        HtmlDomAddController.open(htmlEditor, item);
    }

    public void delete(TreeItem<HtmlNode> inItem) {
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = item.getParent();
        if (parent == null) {
            clearTree();
        } else {
            item.getValue().getElement().remove();
            updateTreeItem(parent);
        }
        htmlEditor.domChanged(true);
    }

    public void deleteNodes(TreeItem<HtmlNode> item) {
        HtmlDomDeleteController.open(htmlEditor, item);
    }

    public void copy(TreeItem<HtmlNode> item) {
        HtmlDomCopyController.open(htmlEditor, item);
    }

    public void duplicate(TreeItem<HtmlNode> inItem, boolean after) {
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = item.getParent();
        if (parent == null) {
            popError(message("NoData"));
            return;
        }
        Element element = item.getValue().getElement();
        String newCodes = element.outerHtml();
        if (after) {
            element.after(newCodes);
        } else {
            parent.getValue().getElement().append(newCodes);
        }
        updateTreeItem(parent);
        htmlEditor.domChanged(true);
    }

    public void up(TreeItem<HtmlNode> inItem) {
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = item.getParent();
        if (parent == null) {
            popError(message("NoData"));
            return;
        }
        int index = parent.getChildren().indexOf(item);
        if (index < 1) {
            return;
        }
        TreeItem<HtmlNode> previousItem = parent.getChildren().get(index - 1);
        if (previousItem == null) {
            return;
        }
        Element thisElement = item.getValue().getElement();
        Element previousElement = previousItem.getValue().getElement();
        if (previousElement == null) {
            return;
        }
        previousElement.remove();
        thisElement.after(previousElement);
        updateTreeItem(item, previousElement);
        updateTreeItem(previousItem, thisElement);
        htmlEditor.domChanged(true);
    }

    public void down(TreeItem<HtmlNode> inItem) {
        TreeItem<HtmlNode> item = validItem(inItem);
        if (item == null) {
            popError(message("NoData"));
            return;
        }
        TreeItem<HtmlNode> parent = item.getParent();
        if (parent == null) {
            return;
        }
        int index = parent.getChildren().indexOf(item);
        if (index >= parent.getChildren().size() - 1) {
            return;
        }
        TreeItem<HtmlNode> nextItem = parent.getChildren().get(index + 1);
        if (nextItem == null) {
            return;
        }
        Element thisElement = item.getValue().getElement();
        Element nextElement = nextItem.getValue().getElement();
        if (nextElement == null) {
            return;
        }
        thisElement.remove();
        nextElement.after(thisElement);
        updateTreeItem(item, nextElement);
        updateTreeItem(nextItem, thisElement);
        htmlEditor.domChanged(true);
    }

    public void move(TreeItem<HtmlNode> item) {
        HtmlDomMoveController.open(htmlEditor, item);
    }

    @FXML
    @Override
    public void recoverAction() {
        if (htmlEditor != null && htmlEditor.sourceFile != null && htmlEditor.sourceFile.exists()) {
            htmlEditor.fileChanged = false;
            htmlEditor.sourceFileChanged(htmlEditor.sourceFile);
        }
    }

    @FXML
    @Override
    public void clearTree() {
        super.clearTree();
        clearNode();
    }

    /*
        node
     */
    public void editNode(TreeItem<HtmlNode> item) {
        currentItem = item;
        if (currentItem == null) {
            clearNode();
            return;
        }
        Element element = currentItem.getValue().getElement();
        nodeController.load(element);
        setCodes();
        tabPane.setDisable(false);
    }

    @FXML
    public void okAttrs() {
        if (currentItem == null) {
            return;
        }
        okNode(nodeController.pickValues());
    }

    public void okNode(Element element) {
        if (currentItem == null || element == null) {
            return;
        }
        updateTreeItem(currentItem, element);
        editNode(currentItem);
        htmlEditor.domChanged(true);
        htmlEditor.popInformation(message("UpdateSuccessfully"));
    }

    @FXML
    public void recoverAttrs() {
        nodeController.recover();
    }

    @FXML
    public void okCodes() {
        if (currentItem == null) {
            return;
        }
        Element element = currentItem.getValue().getElement();
        element.html(codesArea.getText());
        okNode(element);
    }

    public void setCodes() {
        if (currentItem == null) {
            return;
        }
        Element element = currentItem.getValue().getElement();
        tagLabel.setText(element.tagName());
        codesArea.setText(element.html());
    }

    @FXML
    public void recoverCodes() {
        setCodes();
    }

    protected void clearNode() {
        currentItem = null;
        nodeController.load(null);
        tagLabel.setText("");
        codesArea.clear();
        tabPane.setDisable(true);
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
