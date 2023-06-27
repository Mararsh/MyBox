package mara.mybox.tools;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseLogs;
import mara.mybox.data.XmlTreeNode;
import static mara.mybox.data.XmlTreeNode.NodeType.CDATA;
import static mara.mybox.data.XmlTreeNode.NodeType.Comment;
import static mara.mybox.data.XmlTreeNode.NodeType.ProcessingInstruction;
import static mara.mybox.data.XmlTreeNode.NodeType.Text;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class XmlTools {

    public static Transformer transformer;
    public static DocumentBuilder builder;
    public static boolean ignoreBlankInstrution;
    public static boolean ignoreBlankComment;
    public static boolean ignoreBlankText;
    public static boolean ignoreComment;
    public static boolean ignoreBlankCDATA;


    /*
        parse
     */
    public static Document doc(BaseController controller, String xml) {
        try {
            Document doc = builder(controller).parse(new InputSource(new StringReader(xml)));
            Strip(controller, doc);
            return doc;
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static DocumentBuilder builder(BaseController controller) {
        try (Connection conn = DerbyBase.getConnection()) {
            ignoreComment = UserConfig.getBoolean(conn, "XmlIgnoreComments", false);
            ignoreBlankComment = UserConfig.getBoolean(conn, "XmlIgnoreBlankComment", false);
            ignoreBlankText = UserConfig.getBoolean(conn, "XmlIgnoreBlankText", false);
            ignoreBlankCDATA = UserConfig.getBoolean(conn, "XmlIgnoreBlankCDATA", false);
            ignoreBlankInstrution = UserConfig.getBoolean(conn, "XmlIgnoreBlankInstruction", false);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(UserConfig.getBoolean(conn, "XmlDTDValidation", false));
            factory.setNamespaceAware(UserConfig.getBoolean(conn, "XmlSupportNamespaces", false));
            factory.setCoalescing(false);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setIgnoringComments(ignoreComment);
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException e) {
                PopTools.showError(controller, e.toString());
            }

            @Override
            public void fatalError(SAXParseException e) {
                PopTools.showError(controller, e.toString());
            }

            @Override
            public void warning(SAXParseException e) {
                PopTools.showError(controller, e.toString());
            }
        });
        return builder;
    }

    public static Node Strip(BaseController controller, Node node) {
        try {
            if (node == null) {
                return node;
            }
            NodeList nodeList = node.getChildNodes();
            if (nodeList == null) {
                return node;
            }
            List<Node> children = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                children.add(nodeList.item(i));
            }
            for (Node child : children) {
                XmlTreeNode.NodeType t = type(child);
                if (ignoreComment && t == XmlTreeNode.NodeType.Comment) {
                    node.removeChild(child);
                    continue;
                }
                String value = child.getNodeValue();
                if (value == null || value.isBlank()) {
                    switch (t) {
                        case Comment:
                            if (ignoreBlankComment) {
                                node.removeChild(child);
                            }
                            continue;
                        case Text:
                            if (ignoreBlankText) {
                                node.removeChild(child);
                            }
                            continue;
                        case CDATA:
                            if (ignoreBlankCDATA) {
                                node.removeChild(child);
                            }
                            continue;
                        case ProcessingInstruction:
                            if (ignoreBlankInstrution) {
                                node.removeChild(child);
                                continue;
                            }
                            break;
                    }
                }
                Strip(controller, child);
            }
        } catch (Exception e) {
            if (controller == null) {
                MyBoxLog.error(e);
            } else if (controller instanceof BaseLogs) {
                ((BaseLogs) controller).updateLogs(e.toString(), true, true);
            } else {
                Platform.runLater(() -> {
                    controller.alertError(e.toString());
                });
            }
        }
        return node;
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
            case Node.DOCUMENT_FRAGMENT_NODE:
                return node.getNodeValue();
            case Node.ELEMENT_NODE:
                return elementInfo((Element) node);
            case Node.DOCUMENT_NODE:
                return docInfo((Document) node);
            case Node.DOCUMENT_TYPE_NODE:
                return docTypeInfo((DocumentType) node);
            case Node.ENTITY_NODE:
                return entityInfo((Entity) node, "");
            case Node.ENTITY_REFERENCE_NODE:
                return entityReferenceInfo((EntityReference) node);
            case Node.NOTATION_NODE:
                return notationInfo((Notation) node, "");
            default:
                return null;
        }
    }

    public static String docInfo(Document document) {
        if (document == null) {
            return null;
        }
        String info = "";
        String v = document.getXmlVersion();
        if (v != null && !v.isBlank()) {
            info += "version=\"" + v + "\"\n";
        }
        v = document.getXmlEncoding();
        if (v != null && !v.isBlank()) {
            info += "encoding=\"" + v + "\"\n";
        }
        v = document.getDocumentURI();
        if (v != null && !v.isBlank()) {
            info += "uri=\"" + v + "\"\n";
        }
        return info;
    }

    public static XmlTreeNode.NodeType type(Node node) {
        if (node == null) {
            return null;
        }
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return XmlTreeNode.NodeType.Element;
            case Node.ATTRIBUTE_NODE:
                return XmlTreeNode.NodeType.Attribute;
            case Node.TEXT_NODE:
                return XmlTreeNode.NodeType.Text;
            case Node.CDATA_SECTION_NODE:
                return XmlTreeNode.NodeType.CDATA;
            case Node.ENTITY_REFERENCE_NODE:
                return XmlTreeNode.NodeType.EntityRefrence;
            case Node.ENTITY_NODE:
                return XmlTreeNode.NodeType.Entity;
            case Node.PROCESSING_INSTRUCTION_NODE:
                return XmlTreeNode.NodeType.ProcessingInstruction;
            case Node.COMMENT_NODE:
                return XmlTreeNode.NodeType.Comment;
            case Node.DOCUMENT_NODE:
                return XmlTreeNode.NodeType.Document;
            case Node.DOCUMENT_TYPE_NODE:
                return XmlTreeNode.NodeType.DocumentType;
            case Node.DOCUMENT_FRAGMENT_NODE:
                return XmlTreeNode.NodeType.DocumentFragment;
            case Node.NOTATION_NODE:
                return XmlTreeNode.NodeType.Notation;
            default:
                return XmlTreeNode.NodeType.Unknown;
        }
    }

    public static String entityReferenceInfo(EntityReference ref) {
        if (ref == null) {
            return null;
        }
        String info = "\t" + ref.getNodeName() + "=\"" + ref.getNodeValue() + "\"";
        return info;
    }

    public static String docTypeInfo(DocumentType documentType) {
        if (documentType == null) {
            return null;
        }
        String info = "";
        String v = documentType.getName();
        if (v != null && !v.isBlank()) {
            info += "name=\"" + v + "\"\n";
        }
        v = documentType.getPublicId();
        if (v != null && !v.isBlank()) {
            info += "Public Id=\"" + v + "\"\n";
        }
        v = documentType.getSystemId();
        if (v != null && !v.isBlank()) {
            info += "System Id=\"" + v + "\"\n";
        }
        v = documentType.getInternalSubset();
        if (v != null && !v.isBlank()) {
            info += "Internal subset=\"" + v + "\"\n";
        }
        NamedNodeMap entities = documentType.getEntities();
        if (entities != null && entities.getLength() > 0) {
            info += "Entities: \n";
            for (int i = 0; i < entities.getLength(); i++) {
                info += entityInfo((Entity) entities.item(i), "\t");
            }
        }
        NamedNodeMap notations = documentType.getNotations();
        if (notations != null && notations.getLength() > 0) {
            info += "Notations: \n";
            for (int i = 0; i < notations.getLength(); i++) {
                info += notationInfo((Notation) notations.item(i), "\t");
            }
        }
        return info;
    }

    public static String notationInfo(Notation notation, String indent) {
        if (notation == null) {
            return null;
        }
        String info = indent + notation.getNodeName() + "=" + notation.getNodeValue() + "\n";
        String v = notation.getPublicId();
        if (v != null && !v.isBlank()) {
            info += indent + "\t" + "Public Id=" + v + "\n";
        }
        v = notation.getSystemId();
        if (v != null && !v.isBlank()) {
            info += indent + "\t" + "System Id=" + v + "\n";
        }
        return info;
    }

    public static String elementInfo(Element element) {
        if (element == null) {
            return null;
        }
        String info = "";
        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                info += attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"\n";
            }
        }
        return info;
    }

    public static String entityInfo(Entity entity, String indent) {
        if (entity == null) {
            return null;
        }
        String info = indent + entity.getNodeName() + "=" + entity.getNodeValue();
        String v = entity.getNotationName();
        if (v != null && !v.isBlank()) {
            info += indent + "\t" + "Notation name=\"" + v + "\"\n";
        }
        v = entity.getXmlVersion();
        if (v != null && !v.isBlank()) {
            info += indent + "\t" + "version=\"" + v + "\"\n";
        }
        v = entity.getXmlEncoding();
        if (v != null && !v.isBlank()) {
            info += indent + "\t" + "encoding=\"" + v + "\"\n";
        }
        v = entity.getPublicId();
        if (v != null && !v.isBlank()) {
            info += indent + "\t" + "Public Id=\"" + v + "\"\n";
        }
        v = entity.getSystemId();
        if (v != null && !v.isBlank()) {
            info += indent + "\t" + "System Id=\"" + v + "\"\n";
        }
        return info;
    }


    /*
        tree
     */
    public static int index(Node node) {
        if (node == null) {
            return -1;
        }
        Node parent = node.getParentNode();
        if (parent == null) {
            return -2;
        }
        NodeList nodeList = parent.getChildNodes();
        if (nodeList == null) {
            return -3;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (node.equals(nodeList.item(i))) {
                return i;
            }
        }
        return -4;
    }

    public static String hierarchyNumber(Node node) {
        if (node == null) {
            return "";
        }
        Node parent = node.getParentNode();
        if (parent == null) {
            return "";
        }
        int index = index(node);
        if (index < 0) {
            return "";
        }
        String p = hierarchyNumber(parent);
        return (p == null || p.isBlank() ? "" : p + ".") + (index + 1);
    }

    public static Node find(Node node, String sequenceNumber) {
        try {
            if (node == null || sequenceNumber == null || sequenceNumber.isBlank()) {
                return null;
            }
            String[] numbers = sequenceNumber.split("\\.", -1);
            if (numbers == null || numbers.length == 0) {
                return null;
            }
            int index;
            Node current = node;
            for (String n : numbers) {
                index = Integer.parseInt(n);
                NodeList nodeList = current.getChildNodes();
                if (nodeList == null || index < 1 || index > nodeList.getLength()) {
                    return null;
                }
                current = nodeList.item(index - 1);
            }
            return current;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void remove(Node sourceNode, Node targetNode) {
        try {
            if (sourceNode == null || targetNode == null) {
                return;
            }
            NodeList nodeList = sourceNode.getChildNodes();
            if (nodeList == null) {
                return;
            }
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                if (targetNode.equals(child)) {
                    sourceNode.removeChild(child);
                    return;
                }
                remove(child, targetNode);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        transform
     */
    public static String transform(Node node) {
        if (node == null) {
            return null;
        }
        return transform(node, UserConfig.getBoolean("XmlTransformerIndent", true));
    }

    public static String transform(Node node, boolean indent) {
        if (node == null) {
            return null;
        }
        String encoding = node instanceof Document ? ((Document) node).getXmlEncoding() : node.getOwnerDocument().getXmlEncoding();
        return transform(node, encoding, indent);
    }

    public static String transform(Node node, String encoding, boolean indent) {
        if (node == null) {
            return null;
        }
        if (encoding == null) {
            encoding = "utf-8";
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            if (XmlTools.transformer == null) {
                XmlTools.transformer = TransformerFactory.newInstance().newTransformer();
                XmlTools.transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                XmlTools.transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            }
            XmlTools.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, node instanceof Document ? "no" : "yes");
            XmlTools.transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            XmlTools.transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(os);
            XmlTools.transformer.transform(new DOMSource(node), streamResult);
            os.flush();
            os.close();
            String s = os.toString(encoding);
            if (indent) {
                s = s.replaceAll("><", ">\n<");
            }
            return s;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
