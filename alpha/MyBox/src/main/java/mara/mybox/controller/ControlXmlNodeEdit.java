package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlXmlNodeEdit extends ControlXmlNodeBase {
    
    protected TreeItem<XmlTreeNode> treeItem;
    protected Node node;
    
    @FXML
    protected Label infoLabel;
    
    public void setParameters(ControlXmlTree treeController) {
        this.treeController = treeController;
    }
    
    public void editNode(TreeItem<XmlTreeNode> item) {
        clearNode();
        treeItem = item;
        if (treeItem == null) {
            return;
        }
        XmlTreeNode currentTreeNode = treeItem.getValue();
        if (currentTreeNode == null) {
            return;
        }
        thisPane.setDisable(false);
        infoLabel.setText(treeController.hierarchyNumber(item));
        
        node = currentTreeNode.getNode();
        if (node == null) {
            return;
        }
        typeInput.setText(XmlTools.type(node).name());
        baseUriInput.setText(node.getBaseURI());
        namespaceInput.setText(node.getNamespaceURI());
        
        nameInput.setText(node.getNodeName());
        nameInput.setDisable(true);
        
        prefixInput.setText(node.getPrefix());
        prefixInput.setDisable(true);
        
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.ATTRIBUTE_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                tabPane.getTabs().add(0, valueTab);
                valueArea.setText(XmlTools.value(node));
                valueArea.setDisable(false);
                valueArea.setEditable(true);
                break;
            case Node.ELEMENT_NODE:
                tabPane.getTabs().add(0, attrTab);
                setAttributes();
                break;
            case Node.DOCUMENT_NODE:
                tabPane.getTabs().add(0, docTab);
                Document document = (Document) node;
                uriInput.setText(document.getDocumentURI());
                versionInput.setText(document.getXmlVersion());
                encodingInput.setText(document.getXmlEncoding());
                standaloneCheck.setSelected(document.getXmlStandalone());
                break;
            case Node.DOCUMENT_TYPE_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
                tabPane.getTabs().add(0, valueTab);
                valueArea.setText(XmlTools.value(node));
                valueArea.setDisable(true);
                valueArea.setEditable(false);
                break;
            default:
        }
        refreshStyle(tabPane);
        tabPane.getSelectionModel().select(0);
        thisPane.setDisable(false);
    }
    
    public void setAttributes() {
        if (node == null) {
            return;
        }
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                tableData.add(attrs.item(i));
            }
        }
    }
    
    public Node pickValue() {
        try {
            if (node == null) {
                return null;
            }
            switch (node.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                case Node.COMMENT_NODE:
                case Node.ATTRIBUTE_NODE:
                case Node.PROCESSING_INSTRUCTION_NODE:
                    String s = valueArea.getText();
                    node.setNodeValue(s);
                    if (s != null && !s.isBlank()) {
                        TableStringValues.add("XmlNodeValueHistories", s);
                    }
                    break;
                case Node.ELEMENT_NODE:
                    NamedNodeMap attrs = node.getAttributes();
                    if (attrs != null) {
                        Element element = (Element) node;
                        for (int i = attrs.getLength() - 1; i >= 0; i--) {
                            element.removeAttribute(attrs.item(i).getNodeName());
                        }
                        for (Node attr : tableData) {
                            element.setAttribute(attr.getNodeName(), attr.getNodeValue());
                        }
                    }
                    break;
                case Node.DOCUMENT_NODE:
                    Document document = (Document) node;
//                    document.setDocumentURI(uriInput.getText());
//                    document.setXmlVersion(versionInput.getText());
                    document.setXmlStandalone(standaloneCheck.isSelected());
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                case Node.DOCUMENT_FRAGMENT_NODE:
                case Node.ENTITY_NODE:
                case Node.ENTITY_REFERENCE_NODE:
                case Node.NOTATION_NODE:
                default:
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
                    .setType(XmlTools.type(updatedNode))
                    .setTitle(updatedNode.getNodeName())
                    .setValue(XmlTools.value(updatedNode));
            treeItem.setValue(updatedTreeNode);
            editNode(treeItem);
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
        node = null;
        thisPane.setDisable(true);
    }
    
}
