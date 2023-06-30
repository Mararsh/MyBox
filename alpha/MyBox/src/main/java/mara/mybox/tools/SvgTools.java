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
import mara.mybox.controller.BaseController;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
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
    public static File textToImageFile(BaseController controller, String svg,
            float width, float height, Rectangle area) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(svg.getBytes("utf-8"))) {
            return toImageFile(controller, new TranscoderInput(inputStream), width, height, area);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static File svgToImageFile(BaseController controller, SVG svg) {
        if (svg == null) {
            return null;
        }
        return toImageFile(controller, new TranscoderInput(svg.getDoc()),
                svg.getWidth(), svg.getHeight(), svg.getViewBox());
    }

    public static File toImageFile(BaseController controller, TranscoderInput input,
            float width, float height, Rectangle area) {
        if (input == null) {
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
    public static File textToPDF(BaseController controller, String svg,
            float width, float height, Rectangle area) {
        {
            if (svg == null || svg.isBlank()) {
                return null;
            }
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(svg.getBytes("utf-8"))) {
                return toPDF(controller, inputStream, width, height, area);
            } catch (Exception e) {
                PopTools.showError(controller, e.toString());
                return null;
            }
        }
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
    public static String blankSVG(int width, int height) {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"";
        if (width > 0) {
            svg += "width=\"" + width + "\" ";
        }
        if (height > 0) {
            svg += "height=\"" + height + "\" ";
        }
        return svg += "></svg>";
    }

    public static Document blankDoc(int width, int height) {
        return XmlTools.doc(null, blankSVG(width, height));
    }

    public static String nodeSVG(Document doc, Node node, float bgOpacity) {
        if (node == null) {
            return null;
        }
        Document nodeDoc = (Document) doc.cloneNode(true);
        NodeList svglist = nodeDoc.getElementsByTagName("svg");
        if (svglist == null || svglist.getLength() == 0) {
            return null;
        }
        Element nodeSVG = (Element) svglist.item(0);
        NodeList nodes = nodeSVG.getChildNodes();
        Element allG = nodeDoc.createElement("g");
        if (bgOpacity > 0) {
            allG.setAttribute("opacity", bgOpacity + "");
            for (int i = 0; i < nodes.getLength(); i++) {
                allG.appendChild(nodes.item(i));
            }
        }
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            nodeSVG.removeChild(nodes.item(i));
        }
        nodeSVG.appendChild(allG);
        nodeSVG.appendChild(nodeDoc.importNode(node, true));
        return XmlTools.transform(nodeDoc, true);
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
