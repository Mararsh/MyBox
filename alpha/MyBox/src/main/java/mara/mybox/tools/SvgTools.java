package mara.mybox.tools;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SvgTools {

    /*
        text
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

    /*
        image
     */
    public static File textToImageFile(BaseController controller, String svg,
            float width, float height, Rectangle area) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(svg.getBytes("utf-8"))) {
            return toImageFile(controller, inputStream, width, height, area);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static File toImageFile(BaseController controller, InputStream inputStream,
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
