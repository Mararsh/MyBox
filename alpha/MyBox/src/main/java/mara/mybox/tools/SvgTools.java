package mara.mybox.tools;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;
import javafx.scene.paint.Color;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.PopTools;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.*;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SvgTools {


    /*
        image
     */
    public static File docToImage(BaseController controller, Document doc,
            float width, float height, Rectangle area) {
        if (doc == null) {
            return null;
        }
        return textToImage(controller, XmlTools.transform(doc), width, height, area);
    }

    public static File textToImage(BaseController controller, String svg,
            float width, float height, Rectangle area) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        String handled = handleAlpha(controller, svg); // Looks batik does not supper color formats with alpha
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(handled.getBytes("utf-8"))) {
            return toImage(controller, inputStream, width, height, area);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static File fileToImage(BaseController controller, File file,
            float width, float height, Rectangle area) {
        if (file == null || !file.exists()) {
            return null;
        }
        return textToImage(controller, TextFileTools.readTexts(file), width, height, area);
    }

    public static File toImage(BaseController controller, InputStream inputStream,
            float width, float height, Rectangle area) {
        if (inputStream == null) {
            return null;
        }
        File tmpFile = FileTmpTools.generateFile("png");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            PNGTranscoder transcoder = new PNGTranscoder();
            if (width > 0) {
                transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
            }
            if (height > 0) {
                transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
            }
            if (area != null) {
                transcoder.addTranscodingHint(PNGTranscoder.KEY_AOI, area);
            }
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
        }
        if (tmpFile.exists() && tmpFile.length() > 0) {
            return tmpFile;
        }
        FileDeleteTools.delete(tmpFile);
        return null;
    }

    /*
        pdf
     */
    public static File docToPDF(BaseController controller, Document doc,
            float width, float height, Rectangle area) {
        if (doc == null) {
            return null;
        }
        return textToPDF(controller, XmlTools.transform(doc), width, height, area);
    }

    public static File textToPDF(BaseController controller, String svg,
            float width, float height, Rectangle area) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        String handled = handleAlpha(controller, svg); // Looks batik does not supper color formats with alpha
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(handled.getBytes("utf-8"))) {
            return toPDF(controller, inputStream, width, height, area);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static File fileToPDF(BaseController controller, File file,
            float width, float height, Rectangle area) {
        if (file == null || !file.exists()) {
            return null;
        }
        return textToPDF(controller, TextFileTools.readTexts(file), width, height, area);
    }

    public static File toPDF(BaseController controller, InputStream inputStream,
            float width, float height, Rectangle area) {
        if (inputStream == null) {
            return null;
        }
        File tmpFile = FileTmpTools.generateFile("pdf");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            PDFTranscoder transcoder = new PDFTranscoder();
            if (width > 0) {
                transcoder.addTranscodingHint(PDFTranscoder.KEY_WIDTH, width);
            }
            if (height > 0) {
                transcoder.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, height);
            }
            if (area != null) {
                transcoder.addTranscodingHint(PDFTranscoder.KEY_AOI, area);
            }
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
        }
        if (tmpFile.exists() && tmpFile.length() > 0) {
            return tmpFile;
        }
        FileDeleteTools.delete(tmpFile);
        return null;
    }

    /*
        color
     */
    public static String handleAlpha(BaseController controller, String svg) {
        if (svg == null || svg.isBlank()) {
            return svg;
        }
        try {
            Document doc = XmlTools.textToDoc(controller, svg);
            if (doc == null) {
                return svg;
            }
            handleAlpha(controller, doc);
            return XmlTools.transform(doc);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static boolean handleAlpha(BaseController controller, Node node) {
        if (node == null) {
            return false;
        }
        try {
            if (node instanceof Element) {
                Element element = (Element) node;
                try {
                    Color c = Color.web(element.getAttribute("stroke"));
                    String opacity = element.getAttribute("stroke-opacity");
                    if (c != null) {
                        element.setAttribute("stroke", FxColorTools.color2rgb(c));
                        if (c.getOpacity() < 1 && (opacity == null || opacity.isBlank())) {
                            element.setAttribute("stroke-opacity", c.getOpacity() + "");
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Color c = Color.web(element.getAttribute("fill"));
                    String opacity = element.getAttribute("fill-opacity");
                    if (c != null) {
                        element.setAttribute("fill", FxColorTools.color2rgb(c));
                        if (c.getOpacity() < 1 && (opacity == null || opacity.isBlank())) {
                            element.setAttribute("fill-opacity", c.getOpacity() + "");
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Color c = Color.web(element.getAttribute("color"));
                    String opacity = element.getAttribute("opacity");
                    if (c != null) {
                        element.setAttribute("color", FxColorTools.color2rgb(c));
                        if (c.getOpacity() < 1 && (opacity == null || opacity.isBlank())) {
                            element.setAttribute("opacity", c.getOpacity() + "");
                        }
                    }
                } catch (Exception e) {
                }
            }
            NodeList children = node.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    handleAlpha(controller, child);
                }
            }
            return true;
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return false;
        }
    }

    /*
        values
     */
    public static Rectangle viewBox(String value) {
        Rectangle rect = null;
        try {
            String[] v = value.trim().split("\\s+");
            if (v != null && v.length >= 4) {
                rect = new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]),
                        Integer.parseInt(v[2]), Integer.parseInt(v[3]));
            }
        } catch (Exception e) {
        }
        return rect;
    }

    public static String viewBoxString(Rectangle rect) {
        if (rect == null) {
            return null;
        }
        return (int) rect.getX() + " "
                + (int) rect.getY() + " "
                + (int) rect.getWidth() + " "
                + (int) rect.getHeight();
    }

    /*
        generate
     */
    public static String blankSVG(float width, float height) {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" ";
        if (width > 0) {
            svg += " width=\"" + width + "\" ";
        }
        if (height > 0) {
            svg += " height=\"" + height + "\" ";
        }
        return svg += " ></svg>";
    }

    public static Document blankDoc(float width, float height) {
        return XmlTools.textToDoc(null, blankSVG(width, height));
    }

    public static Document blankDoc() {
        return blankDoc(500.0f, 500.0f);
    }

    public static Document focus(Document doc, Node node, float bgOpacity) {
        if (doc == null) {
            return doc;
        }
        Document clonedDoc = (Document) doc.cloneNode(true);
        if (node == null || !(node instanceof Element)
                || "svg".equalsIgnoreCase(node.getNodeName())) {
            return clonedDoc;
        }
        String hierarchyNumber = XmlTools.hierarchyNumber(node);
        if (hierarchyNumber == null) {
            return clonedDoc;
        }
        Node targetNode = XmlTools.find(clonedDoc, hierarchyNumber);
        Node cnode = targetNode;
        while (cnode != null) {
            Node parent = cnode.getParentNode();
            if (parent == null) {
                break;
            }
            NodeList nodes = parent.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node child = nodes.item(i);
                if (child.equals(cnode) || !(child instanceof Element)) {
                    continue;
                }
                ((Element) child).setAttribute("opacity", bgOpacity + "");
            }
            cnode = parent;
        }
        return clonedDoc;
    }

    public static File toFile(SVGGraphics2D g, File file) {
        if (g == null || file == null) {
            return null;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, Charset.forName("utf-8"), false))) {
            g.stream(writer, true);
            writer.flush();
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File toFile(SVGGraphics2D g) {
        if (g == null) {
            return null;
        }
        File tmpFile = FileTmpTools.getTempFile(".svg");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("utf-8"), false))) {
            g.stream(writer, true);
            writer.flush();
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String toText(SVGGraphics2D g) {
        String s = TextFileTools.readTexts(toFile(g), Charset.forName("utf-8"));
        return s;
//        if (s == null) {
//            return null;
//        }
//        return s.replaceAll("><", ">\n<");
    }

    /*
        SVGGraphics2D
     */
    public static Document document() {
        return GenericDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
    }

}
