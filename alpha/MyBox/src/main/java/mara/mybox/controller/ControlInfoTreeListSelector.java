package mara.mybox.controller;

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
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class ControlInfoTreeListSelector extends ControlInfoTreeList {

    protected BaseInfoTreeHandleController handler;
    protected ControlInfoTreeList sourceList;

    @Override
    public boolean isSourceNode(InfoNode node) {
        return handler.isSourceNode(node);
    }

    /*
        action
     */
    @Override
    protected void viewNode(TreeItem<InfoNode> item) {
        handler.handlerController.viewNode(item.getValue());
    }

    @Override
    public void nodeAdded(InfoNode parent, InfoNode newNode) {
        sourceList.addNewNode(sourceList.find(parent), newNode, true);
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<InfoNode> item) {
        viewNode(item);
    }

    @Override
    public void doubleClicked(MouseEvent event, TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        String clickAction = UserConfig.getString(baseName + "TreeListSelectorWhenDoubleClickNode", "OK");
        if (clickAction == null) {
            return;
        }
        switch (clickAction) {
            case "PopMenu":
                showPopMenu(item);
                break;
            case "PopNode":
                popNode(item);
                break;
            case "OK":
                handler.okAction();
                break;
            case "LoadChildren":
                listChildren(item);
                break;
            case "LoadDescendants":
                listDescentants(item);
                break;
            default:
                break;
        }
    }

    @Override
    public List<MenuItem> viewMenuItems(TreeItem<InfoNode> item) {
        if (item == null) {
            return null;
        }
        List<MenuItem> items = super.viewMenuItems(item);

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(item));

        return items;
    }

    public Menu doubleClickMenu(TreeItem<InfoNode> treeItem) {
        Menu clickMenu = new Menu(message("WhenDoubleClickNode"), StyleTools.getIconImageView("iconSelectAll.png"));

        ToggleGroup clickGroup = new ToggleGroup();
        String currentClick = UserConfig.getString(baseName + "TreeListSelectorWhenDoubleClickNode", "OK");

        RadioMenuItem okMenu = new RadioMenuItem(message("OK"), StyleTools.getIconImageView("iconOK.png"));
        okMenu.setSelected("OK".equals(currentClick));
        okMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeListSelectorWhenDoubleClickNode", "OK");
            }
        });
        okMenu.setToggleGroup(clickGroup);

        RadioMenuItem popNodeMenu = new RadioMenuItem(message("PopNode"), StyleTools.getIconImageView("iconPop.png"));
        popNodeMenu.setSelected("PopNode".equals(currentClick));
        popNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeListSelectorWhenDoubleClickNode", "PopNode");
            }
        });
        popNodeMenu.setToggleGroup(clickGroup);

        RadioMenuItem loadChildrenMenu = new RadioMenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
        loadChildrenMenu.setSelected("LoadChildren".equals(currentClick));
        loadChildrenMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeListSelectorWhenDoubleClickNode", "LoadChildren");
            }
        });
        loadChildrenMenu.setToggleGroup(clickGroup);

        RadioMenuItem loadDescendantsMenu = new RadioMenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
        loadDescendantsMenu.setSelected("LoadDescendants".equals(currentClick));
        loadDescendantsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeListSelectorWhenDoubleClickNode", "LoadDescendants");
            }
        });
        loadDescendantsMenu.setToggleGroup(clickGroup);

        RadioMenuItem nothingMenu = new RadioMenuItem(message("DoNothing"));
        nothingMenu.setSelected(currentClick == null || "DoNothing".equals(currentClick));
        nothingMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeListSelectorWhenDoubleClickNode", "DoNothing");
            }
        });
        nothingMenu.setToggleGroup(clickGroup);

        RadioMenuItem clickPopMenu = new RadioMenuItem(message("PopMenu"), StyleTools.getIconImageView("iconMenu.png"));
        clickPopMenu.setSelected("PopMenu".equals(currentClick));
        clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setString(baseName + "TreeListSelectorWhenDoubleClickNode", "PopMenu");
            }
        });
        clickPopMenu.setToggleGroup(clickGroup);

        clickMenu.getItems().addAll(okMenu, popNodeMenu, loadChildrenMenu, loadDescendantsMenu,
                clickPopMenu, nothingMenu);

        return clickMenu;
    }

    @FXML
    public void showPopMenu(TreeItem<InfoNode> item) {
        if (item == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(item)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(viewMenuItems(item));

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AddNode"), StyleTools.getIconImageView("iconAdd.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addChild(item);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        items.add(doubleClickMenu(item));

        popNodeMenu(treeView, items);
    }

}
