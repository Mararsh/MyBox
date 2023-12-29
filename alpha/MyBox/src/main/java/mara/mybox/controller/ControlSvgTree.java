package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgTree extends ControlXmlTree {

    protected SvgEditorController editorController;

    @FXML
    protected ControlSvgNodeEdit svgNodeController;
    @FXML
    protected Button addShapeButton;

    @Override
    public void initValues() {
        try {
            super.initValues();

            nodeController = svgNodeController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            StyleTools.setIconTooltips(addShapeButton, "iconNewItem.png", message("SvgAddShape"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setRoot(TreeItem<XmlTreeNode> root) {
        super.setRoot(root);
        addShapeButton.setDisable(true);
    }

    @Override
    public void itemClicked(MouseEvent event, TreeItem<XmlTreeNode> item) {
        super.itemClicked(event, item);
        addShapeButton.setDisable(item == null || item.getValue() == null
                || !item.getValue().canAddSvgShape());
    }

    @Override
    public List<MenuItem> modifyMenus(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        if (treeItem.getValue() == null || !treeItem.getValue().isSvgShape()) {
            Menu drawMenu = new Menu(message("Draw"), StyleTools.getIconImageView("iconDraw.png"));
            items.add(drawMenu);
            drawMenu.getItems().addAll(drawShapeMenus(treeItem));
        }

        if (treeItem.getValue() == null || !treeItem.getValue().canAddSvgShape()) {
            Menu addMenu = new Menu(message("SvgAddShape"), StyleTools.getIconImageView("iconNewItem.png"));
            items.add(addMenu);
            addMenu.getItems().addAll(addShapeMenus(treeItem));
        }

        if (!items.isEmpty()) {
            items.add(new SeparatorMenuItem());
        }

        items.addAll(super.modifyMenus(treeItem));

        return items;
    }

    @FXML
    public void popAddShapeMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "AddShapePopWhenMouseHovering", true)) {
            showAddShapeMenu(event);
        }
    }

    @FXML
    public void showAddShapeMenu(Event event) {
        TreeItem<XmlTreeNode> treeItem = selected();
        if (treeItem == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(treeItem)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(addShapeMenus(treeItem));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "AddShapePopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "AddShapePopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(treeView, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> addShapeMenus(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("Rectangle"), StyleTools.getIconImageView("iconRectangle.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {

        });
        items.add(menu);

        menu = new MenuItem(message("Circle"), StyleTools.getIconImageView("iconCircle.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {

        });
        items.add(menu);

        return items;
    }

    @FXML
    public void popDrawShapeMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "DrawShapePopWhenMouseHovering", true)) {
            showDrawShapeMenu(event);
        }
    }

    @FXML
    public void showDrawShapeMenu(Event event) {
        TreeItem<XmlTreeNode> treeItem = selected();
        if (treeItem == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(StringTools.menuPrefix(label(treeItem)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        items.addAll(drawShapeMenus(treeItem));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "DrawShapePopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "DrawShapePopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        if (event == null) {
            popNodeMenu(treeView, items);
        } else {
            popEventMenu(event, items);
        }
    }

    public List<MenuItem> drawShapeMenus(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();

        MenuItem menu = new MenuItem(message("Rectangle"), StyleTools.getIconImageView("iconRectangle.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {

        });
        items.add(menu);

        menu = new MenuItem(message("Circle"), StyleTools.getIconImageView("iconCircle.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {

        });
        items.add(menu);

        return items;
    }

    public void addShape(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            popInformation(message("SelectToHandle"));
            return;
        }
        SvgAddShapeController.open(editorController, treeItem);
    }

    public void drawShape(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            popInformation(message("SelectToHandle"));
            return;
        }
        SvgEditShapeController.open(editorController, treeItem);
    }

}
