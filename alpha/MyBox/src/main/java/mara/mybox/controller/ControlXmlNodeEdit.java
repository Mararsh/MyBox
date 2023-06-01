package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlXmlNodeEdit extends ControlXmlNodeBase {

    protected TreeItem<XmlTreeNode> treeItem;

    @FXML
    protected Label infoLabel;

    public void setParameters(ControlXmlTree treeController) {
        this.treeController = treeController;
    }

    public void editNode(TreeItem<XmlTreeNode> item) {
        treeItem = item;
        if (treeItem == null) {
            clearNode();
            return;
        }
        XmlTreeNode currentTreeNode = treeItem.getValue();
        if (currentTreeNode == null) {
            clearNode();
            return;
        }
        thisPane.setDisable(false);
        infoLabel.setText(treeController.hierarchyNumber(item));

        load(currentTreeNode.getNode());
    }

    @FXML
    public void okNode() {
        try {
            if (treeItem == null) {
                return;
            }
            XmlTreeNode currentTreeNode = treeItem.getValue();
            if (currentTreeNode == null) {
                return;
            }
            Node updatedNode = pickValue();
            if (updatedNode == null) {
                return;
            }
            XmlTreeNode updatedTreeNode = new XmlTreeNode()
                    .setNode(updatedNode)
                    .setTitle(nameInput.getText())
                    .setValue(valueArea.getText());
            treeItem.setValue(updatedTreeNode);
            treeController.xmlEditor.domChanged(true);
            treeController.xmlEditor.popInformation(message("UpdateSuccessfully"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void recoverNode() {
        editNode(treeItem);
    }

    @Override
    public void clearNode() {
        super.clearNode();
        thisPane.setDisable(true);
    }

    @FXML
    public void addAttribute() {
        if (node == null) {
            return;
        }
        Attr attr = node.getOwnerDocument().createAttribute("attr");
        attr.setValue("value");
        attributesData.add(attr);
    }

    @FXML
    public void deleteAttributes() {
        try {
            List<Node> selected = attributesTable.getSelectionModel().getSelectedItems();
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

}
