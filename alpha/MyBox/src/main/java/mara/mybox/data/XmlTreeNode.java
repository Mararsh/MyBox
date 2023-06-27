package mara.mybox.data;

import mara.mybox.tools.XmlTools;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2023-5-27
 * @License Apache License Version 2.0
 */
public class XmlTreeNode {

    protected String title, value;
    protected NodeType type;
    protected Node node;

    public static enum NodeType {
        Element, Attribute, Text, CDATA, EntityRefrence, Entity, ProcessingInstruction,
        Comment, Document, DocumentType, DocumentFragment, Notation, Unknown
    }

    public XmlTreeNode() {
        node = null;
        title = null;
        value = null;
        type = NodeType.Unknown;
    }

    public XmlTreeNode(Node node) {
        this.node = node;
        type = XmlTools.type(node);
    }

    public int childrenSize() {
        if (node == null) {
            return 0;
        }
        NodeList children = node.getChildNodes();
        if (children == null) {
            return 0;
        }
        return children.getLength();
    }

    public boolean canAddNode() {
        NodeType t = XmlTools.type(node);
        return t == NodeType.Document
                || t == NodeType.Element || t == NodeType.Attribute
                || t == NodeType.Entity || t == NodeType.EntityRefrence
                || t == NodeType.DocumentFragment;
    }

    public boolean isDocWithoutElement() {
        if (node == null) {
            return false;
        }
        NodeType t = XmlTools.type(node);
        if (t != NodeType.Document) {
            return false;
        }
        NodeList children = node.getChildNodes();
        if (children == null) {
            return true;
        }
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }

    public int index() {
        return XmlTools.index(node);
    }

    public String hierarchyNumber() {
        return XmlTools.hierarchyNumber(node);
    }

    public boolean canDraw() {
        if (node == null) {
            return false;
        }
        NodeType t = XmlTools.type(node);
        if (t != NodeType.Element) {
            return false;
        }
        return true;
//        String name = node.getNodeName();
//        if (name == null) {
//            return false;
//        }
//        return name.equalsIgnoreCase("rect") || name.equalsIgnoreCase("circle")
//                || name.equalsIgnoreCase("ellipse") || name.equalsIgnoreCase("line")
//                || name.equalsIgnoreCase("polyline") || name.equalsIgnoreCase("polygon")
//                || name.equalsIgnoreCase("path");
    }


    /*
       custimized get
     */
    public String getTitle() {
        return XmlTools.name(node);
    }

    public String getValue() {
        return XmlTools.value(node);
    }

    public NodeType getType() {
        return type;
    }

    public String getTypename() {
        return type == null ? null : type.name();
    }


    /*
        set
     */
    public XmlTreeNode setNode(Node node) {
        this.node = node;
        return this;
    }

    public XmlTreeNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public XmlTreeNode setValue(String value) {
        this.value = value;
        return this;
    }

    public XmlTreeNode setType(NodeType type) {
        this.type = type;
        return this;
    }

    /*
        get
     */
    public Node getNode() {
        return node;
    }

}
