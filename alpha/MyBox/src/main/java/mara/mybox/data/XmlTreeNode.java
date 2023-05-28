package mara.mybox.data;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mara.mybox.dev.MyBoxLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
    public static DocumentBuilder builder() {
        try {
            if (builder == null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);

                builder = factory.newDocumentBuilder();
            }
            return builder;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Document doc(String xml) {
        try {

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));

            return builder().parse(is);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void read(String xmlFile) {
        try {
            Document doc = doc(xmlFile);
            Element root = doc.getDocumentElement();
            if (root == null) {
                return;
            }
            System.err.println(root.getAttribute("name"));
            NodeList collegeNodes = root.getChildNodes();
            if (collegeNodes == null) {
                return;
            }
            for (int i = 0; i < collegeNodes.getLength(); i++) {
                Node college = collegeNodes.item(i);
                if (college != null && college.getNodeType() == Node.ELEMENT_NODE) {
                    System.err.println("\t" + college.getAttributes().getNamedItem("name").getNodeValue());
                    // all class node
                    NodeList classNodes = college.getChildNodes();
                    if (classNodes == null) {
                        continue;
                    }
                    for (int j = 0; j < classNodes.getLength(); j++) {
                        Node clazz = classNodes.item(j);
                        if (clazz != null && clazz.getNodeType() == Node.ELEMENT_NODE) {
                            System.err.println("\t\t" + clazz.getAttributes().getNamedItem("name").getNodeValue());
                            // all student node
                            NodeList studentNodes = clazz.getChildNodes();
                            if (studentNodes == null) {
                                continue;
                            }
                            for (int k = 0; k < studentNodes.getLength(); k++) {
                                Node student = studentNodes.item(k);
                                if (student != null && student.getNodeType() == Node.ELEMENT_NODE) {
                                    System.err.print("\t\t\t" + student.getAttributes().getNamedItem("name").getNodeValue());
                                    System.err.print(" " + student.getAttributes().getNamedItem("sex").getNodeValue());
                                    System.err.println(" " + student.getAttributes().getNamedItem("age").getNodeValue());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
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
