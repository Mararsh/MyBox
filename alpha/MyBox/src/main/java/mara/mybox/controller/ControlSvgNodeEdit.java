package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import mara.mybox.data.XmlTreeNode;
import static mara.mybox.data.XmlTreeNode.NodeType.Element;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgNodeEdit extends ControlXmlNodeEdit {

    protected SvgEditorController editor;

    @FXML
    protected VBox pathBox, styleBox;
    @FXML
    protected TextArea pathArea, styleArea;

    @Override
    public void editNode(TreeItem<XmlTreeNode> item) {
        super.editNode(item);
        if (treeItem != null) {
            XmlTreeNode currentTreeNode = treeItem.getValue();
            if (currentTreeNode != null && currentTreeNode.getType() == Element) {
                String name = currentTreeNode.getNode().getNodeName();
                if (!name.equalsIgnoreCase("svg")) {
                    if (name.equalsIgnoreCase("path")) {
                        setBox.getChildren().add(0, pathBox);
                        setBox.getChildren().add(1, styleBox);
                    } else {
                        setBox.getChildren().add(0, styleBox);
                    }
                }
            }
        }
        Node focusedNode = null;
        try {
            focusedNode = treeItem.getValue().getNode();
        } catch (Exception e) {
        }
        editor.htmlController.loadDoc(editor.treeController.doc, focusedNode);
    }

    @Override
    public void setAttributes() {
        if (node == null) {
            return;
        }
        boolean isPath = "path".equalsIgnoreCase(node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                String attrName = attr.getNodeName();
                String value = attr.getNodeValue();
                if (isPath && "d".equalsIgnoreCase(attrName)) {
                    pathArea.setText(value);
                    continue;
                }
                if ("style".equalsIgnoreCase(attrName)) {
                    styleArea.setText(value);
                    continue;
                }
                tableData.add(attrs.item(i));
            }
        }
    }

    @Override
    public Node pickValue() {
        try {
            if (node == null) {
                return null;
            }
            super.pickValue();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                return node;
            }
            Element element = (Element) node;
            String style = styleArea.getText();
            if (style != null && !style.isBlank()) {
                element.setAttribute("style", StringTools.trimBlanks(style));
            } else {
                element.removeAttribute("style");
            }
            if ("path".equalsIgnoreCase(node.getNodeName())) {
                String path = pathArea.getText();
                if (path != null && !path.isBlank()) {
                    element.setAttribute("d", StringTools.trimBlanks(path));
                } else {
                    element.removeAttribute("d");
                }
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void clearNode() {
        super.clearNode();
        pathArea.clear();
        styleArea.clear();
    }

    @FXML
    public void popExamplesPathMenu(Event event) {
        if (UserConfig.getBoolean("SvgPathExamplesPopWhenMouseHovering", false)) {
            showExamplesPathMenu(event);
        }
    }

    @FXML
    public void showExamplesPathMenu(Event event) {
        PopTools.popValues(this, pathArea, "SvgPathExamples", HelpTools.svgPathExamples(), event);
    }

    @FXML
    public void popExamplesStyleMenu(Event event) {
        if (UserConfig.getBoolean("SvgStyleExamplesPopWhenMouseHovering", false)) {
            showExamplesStyleMenu(event);
        }
    }

    @FXML
    public void showExamplesStyleMenu(Event event) {
        PopTools.popValues(this, styleArea, "SvgStyleExamples", HelpTools.svgStyleExamples(), event);
    }

}
