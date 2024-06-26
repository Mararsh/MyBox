package mara.mybox.data;

import java.awt.Rectangle;
import java.io.File;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-27
 * @License Apache License Version 2.0
 */
public class SVG {

    protected Document doc;
    protected Element svgNode;
    protected float width, height, renderedWidth, renderedheight;
    protected Rectangle viewBox;
    protected File imageFile;

    public SVG() {
        doc = null;
        svgNode = null;
        width = -1;
        height = -1;
        viewBox = null;
        imageFile = null;
    }

    public SVG(Document doc) {
        try {
            this.doc = doc;
            width = -1;
            height = -1;
            viewBox = null;
            if (doc == null) {
                return;
            }
            svgNode = XmlTools.findName(doc, "svg", 0);
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

            if (viewBox != null) {
                if (width <= 0) {
                    width = (float) viewBox.getWidth();
                }
                if (height <= 0) {
                    height = (float) viewBox.getHeight();
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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

    /*
        set
     */
    public SVG setDoc(Document doc) {
        this.doc = doc;
        return this;
    }

    public SVG setSvgNode(Element svgNode) {
        this.svgNode = svgNode;
        return this;
    }

    public SVG setWidth(float width) {
        this.width = width;
        return this;
    }

    public SVG setHeight(float height) {
        this.height = height;
        return this;
    }

    public SVG setRenderedWidth(float renderedWidth) {
        this.renderedWidth = renderedWidth;
        return this;
    }

    public SVG setRenderedheight(float renderedheight) {
        this.renderedheight = renderedheight;
        return this;
    }

    public SVG setViewBox(Rectangle viewBox) {
        this.viewBox = viewBox;
        return this;
    }

    public SVG setImageFile(File imageFile) {
        this.imageFile = imageFile;
        return this;
    }

}
