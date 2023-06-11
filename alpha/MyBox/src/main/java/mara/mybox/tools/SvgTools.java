package mara.mybox.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import mara.mybox.dev.MyBoxLog;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SvgTools {

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
