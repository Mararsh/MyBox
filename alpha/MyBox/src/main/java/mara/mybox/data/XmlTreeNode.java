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
import org.w3c.dom.NodeList;
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

    protected String title, value;
    protected Node node;

    public XmlTreeNode() {
        node = null;
        title = null;
        value = null;
    }

    public XmlTreeNode(String title, Node node) {
        this.title = title;
        this.node = node;
        if (node != null) {
            NodeList children = node.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        value = child.getNodeValue();
                        break;
                    }
                }
            }
        }
    }

    /*
        static
     */
    public static DocumentBuilder builder(BaseController controller) {
        try (Connection conn = DerbyBase.getConnection()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(UserConfig.getBoolean(conn, "XmlDTDValidation", false));
            factory.setCoalescing(UserConfig.getBoolean(conn, "XmlConvertCDATA", false));
            factory.setIgnoringComments(UserConfig.getBoolean(conn, "XmlIgnoreComments", false));
            factory.setIgnoringElementContentWhitespace(UserConfig.getBoolean(conn, "XmlIgnoreElementContentWhitespace", false));
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

    /*
        get
     */
    public Node getNode() {
        return node;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

}
