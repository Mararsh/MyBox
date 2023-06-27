package mara.mybox.data;

import java.awt.Rectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2023-6-27
 * @License Apache License Version 2.0
 */
public class SVG {

    protected Document doc;
    protected Element svgNode;
    protected float width, height;
    protected Rectangle viewBox;

    public SVG() {
        doc = null;
        svgNode = null;
        width = -1;
        height = -1;
        viewBox = null;
    }

    public SVG(Document doc) {
        try {
            if (doc == null) {
                return;
            }
            NodeList svglist = doc.getElementsByTagName("svg");
            if (svglist == null || svglist.getLength() == 0) {
                return;
            }
            svgNode = (Element) svglist.item(0);
            NamedNodeMap attrs = svgNode.getAttributes();
            if (attrs == null || attrs.getLength() == 0) {
                return;
            }
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                String name = attr.getNodeName();
                String value = attr.getNodeValue();
                if (name == null || value == null) {
                    continue;
                }
                try {
                    switch (name.toLowerCase()) {
                        case "width":
                            width = Float.parseFloat(value);
                            break;
                        case "height":
                            height = Float.parseFloat(value);
                            break;
                        case "viewbox":
                            viewBox = SvgTools.viewBox(value);
                            break;
                    }
                } catch (Exception e) {
                }

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String makeSVG(String content) {
        if (content == null) {
            return null;
        }
        String xml = "<svg xmlns=\"http://www.w3.org/2000/svg\" ";
        if (width > 0) {
            xml += " width=\"" + width + "\" ";
        }
        if (height > 0) {
            xml += " height=\"" + height + "\" ";
        }
        if (viewBox != null) {
            xml += " viewBox=\"" + SvgTools.viewBoxString(viewBox) + "\" ";
        }
        xml += ">\n" + content + "\n</svg>";
        return xml;
    }

    public String nodeSVG(Node node) {
        return makeSVG(XmlTools.transform(node, true));
    }

    /*
        get
     */
    public Document getDoc() {
        return doc;
    }

    public Element getSvgNode() {
        return svgNode;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle getViewBox() {
        return viewBox;
    }

}
