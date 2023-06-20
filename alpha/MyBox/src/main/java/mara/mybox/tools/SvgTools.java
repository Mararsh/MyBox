package mara.mybox.tools;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SvgTools {

    /*
        Transcoder
     */
    public static File textToImage(BaseController controller, String svg) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(svg.getBytes("utf-8"))) {
            return toImage(controller, inputStream);
        } catch (Exception e) {
            if (controller != null) {
                controller.displayError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static File fileToImage(BaseController controller, File svgFile) {
        if (svgFile == null || !svgFile.exists()) {
            return null;
        }
        try (FileInputStream inputStream = new FileInputStream(svgFile)) {
            return toImage(controller, inputStream);
        } catch (Exception e) {
            if (controller != null) {
                controller.displayError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static File toImage(BaseController controller, InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        File tmpFile = FileTmpTools.generateFile("png");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            if (controller != null) {
                controller.displayError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        if (tmpFile.exists() && tmpFile.length() > 0) {
            return tmpFile;
        }
        FileDeleteTools.delete(tmpFile);
        return null;
    }

    public static File textToPDF(BaseController controller, String svg) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(svg.getBytes("utf-8"))) {
            return toPDF(controller, inputStream);
        } catch (Exception e) {
            if (controller != null) {
                controller.displayError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static File fileToPDF(BaseController controller, File svgFile) {
        if (svgFile == null || !svgFile.exists()) {
            return null;
        }
        try (FileInputStream inputStream = new FileInputStream(svgFile)) {
            return toPDF(controller, inputStream);
        } catch (Exception e) {
            if (controller != null) {
                controller.displayError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static File toPDF(BaseController controller, InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        File tmpFile = FileTmpTools.generateFile("pdf");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            PDFTranscoder transcoder = new PDFTranscoder();
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            if (controller != null) {
                controller.displayError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
        if (tmpFile.exists() && tmpFile.length() > 0) {
            return tmpFile;
        }
        FileDeleteTools.delete(tmpFile);
        return null;
    }

    /*
        SVGGraphics2D
     */
    public static Document document() {
        return GenericDOMImplementation.getDOMImplementation()
                .createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
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

}
