package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import mara.mybox.data.XmlTreeNode;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgNodeEdit extends ControlXmlNodeEdit {

    @FXML
    protected Button drawButton;

    @Override
    public void editNode(TreeItem<XmlTreeNode> item) {
        drawButton.setDisable(false);
        super.editNode(item);
        if (treeItem == null) {
            return;
        }
        XmlTreeNode currentTreeNode = treeItem.getValue();
        if (currentTreeNode == null) {
            return;
        }
        drawButton.setDisable(!currentTreeNode.canDraw());
    }

    @FXML
    public void drawAction() {
        SvgElementEditController.open(((ControlSvgTree) treeController).editorController, treeItem);
    }

}
