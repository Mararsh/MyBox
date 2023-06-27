package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.SVG;
import mara.mybox.data.XmlTreeNode;
import static mara.mybox.data.XmlTreeNode.NodeType.Document;
import static mara.mybox.data.XmlTreeNode.NodeType.Element;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgNodeEdit extends ControlXmlNodeEdit {

    protected SvgEditorController editor;

    @Override
    public void editNode(TreeItem<XmlTreeNode> item) {
        super.editNode(item);
        String xml = null;
        if (treeItem != null) {
            SVG svg = editor.treeController.svg;
            if (svg != null) {
                XmlTreeNode currentTreeNode = treeItem.getValue();
                if (currentTreeNode != null) {
                    switch (currentTreeNode.getType()) {
                        case Document:
                        case DocumentType:
                            xml = editor.xmlByText();
                            break;
                        case Element:
                            String name = currentTreeNode.getNode().getNodeName();
                            if (name.equalsIgnoreCase("svg")) {
                                xml = editor.xmlByText();
                            } else {
                                xml = svg.nodeSVG(currentTreeNode.getNode());
                            }
                            break;
                        default:
                    }
                }
            }
        }
        editor.loadHtml(xml);
    }

    @FXML
    public void drawAction() {
        SvgElementEditController.open(((ControlSvgTree) treeController).editorController, treeItem);
    }

}
