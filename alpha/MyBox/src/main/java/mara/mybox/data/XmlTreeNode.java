package mara.mybox.data;

import java.io.StringReader;
import java.sql.Connection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mara.mybox.controller.BaseController;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * @Author Mara
 * @CreateDate 2023-5-27
 * @License Apache License Version 2.0
 */
public class XmlTreeNode {

    public static DocumentBuilder builder;
    public static boolean ignoreWhite;

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
        type = type(node);
    }

    /*
       custimized get
     */
    public String getTitle() {
        return name(node);
    }

    public String getValue() {
        return value(node);
    }

    public String getTypename() {
        return type == null ? null : type.name();
    }

    /*
        static
     */
    public static DocumentBuilder builder(BaseController controller) {
        try (Connection conn = DerbyBase.getConnection()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(UserConfig.getBoolean(conn, "XmlDTDValidation", false));
            factory.setCoalescing(false);
            factory.setIgnoringComments(UserConfig.getBoolean(conn, "XmlIgnoreComments", false));
            ignoreWhite = UserConfig.getBoolean(conn, "XmlIgnoreBlankString", true);
            factory.setIgnoringElementContentWhitespace(ignoreWhite);
            factory.setNamespaceAware(UserConfig.getBoolean(conn, "XmlSupportNamespaces", false));

            builder = factory.newDocumentBuilder();

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException arg0) {
                controller.alertError(arg0.toString());
            }

            @Override
            public void fatalError(SAXParseException arg0) {
                controller.alertError(arg0.toString());
            }

            @Override
            public void warning(SAXParseException arg0) {
                controller.alertWarning(arg0.toString());
            }
        });
        return builder;
    }

    public static Document doc(BaseController controller, String xml) {
        try {
            return builder(controller).parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            controller.alertError(e.toString());
            return null;
        }
    }

    public static NodeType type(Node node) {
        if (node == null) {
            return null;
        }
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return NodeType.Element;
            case Node.ATTRIBUTE_NODE:
                return NodeType.Attribute;
            case Node.TEXT_NODE:
                return NodeType.Text;
            case Node.CDATA_SECTION_NODE:
                return NodeType.CDATA;
            case Node.ENTITY_REFERENCE_NODE:
                return NodeType.EntityRefrence;
            case Node.ENTITY_NODE:
                return NodeType.Entity;
            case Node.PROCESSING_INSTRUCTION_NODE:
                return NodeType.ProcessingInstruction;
            case Node.COMMENT_NODE:
                return NodeType.Comment;
            case Node.DOCUMENT_NODE:
                return NodeType.Document;
            case Node.DOCUMENT_TYPE_NODE:
                return NodeType.DocumentType;
            case Node.DOCUMENT_FRAGMENT_NODE:
                return NodeType.DocumentFragment;
            case Node.NOTATION_NODE:
                return NodeType.Notation;
            default:
                return NodeType.Unknown;
        }
    }

    public static String name(Node node) {
        return node == null ? null : node.getNodeName();
    }

    public static String value(Node node) {
        if (node == null) {
            return null;
        }
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.ATTRIBUTE_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                String value = node.getNodeValue();
                if (value != null && ignoreWhite && value.isBlank()) {
                    value = null;
                }
                return value;
            default:
                return null;
        }
    }

    public static boolean canIgnore(Node node) {
        if (node == null) {
            return true;
        }
        if (!ignoreWhite) {
            return false;
        }
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.ATTRIBUTE_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                String value = node.getNodeValue();
                return value == null || value.isBlank();
            default:
                return false;
        }
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

    public NodeType getType() {
        return type;
    }

}
