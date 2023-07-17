package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

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

        MenuItem menu = new MenuItem(message("SvgAddShape"), StyleTools.getIconImageView("iconNewItem.png"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addShape(treeItem);
        });
        menu.setDisable(treeItem.getValue() == null || !treeItem.getValue().canAddSvgShape());
        items.add(menu);

        items.addAll(super.modifyMenus(treeItem));

        return items;
    }

    @FXML
    public void addShape() {
        addShape(selected());
    }

    public void addShape(TreeItem<XmlTreeNode> treeItem) {
        if (treeItem == null) {
            popInformation(message("SelectToHandle"));
            return;
        }
        SvgAddShapeController.open(editorController, treeItem);
    }

}
