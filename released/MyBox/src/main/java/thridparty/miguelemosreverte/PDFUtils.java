package thridparty.miguelemosreverte;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
import de.erichseifert.vectorgraphics2d.pdf.PDFProcessor;
import de.erichseifert.vectorgraphics2d.util.PageSize;
import thridparty.miguelemosreverte.ImageTracer.IndexedImage;

public class PDFUtils {

    public static String getPDFString(IndexedImage ii, HashMap<String, Float> options) {
        // Document setup
        int w = (int) (ii.width * options.get("scale")), h = (int) (ii.height * options.get("scale"));
        VectorGraphics2D vg2d = new VectorGraphics2D();

        // creating Z-index
        TreeMap<Double, Integer[]> zindex = new TreeMap<Double, Integer[]>();
        double label;
        // Layer loop
        for (int k = 0; k < ii.layers.size(); k++) {

            // Path loop
            for (int pcnt = 0; pcnt < ii.layers.get(k).size(); pcnt++) {

                // Label (Z-index key) is the startpoint of the path, linearized
                label = (ii.layers.get(k).get(pcnt).get(0)[2] * w) + ii.layers.get(k).get(pcnt).get(0)[1];
                // Creating new list if required
                if (!zindex.containsKey(label)) {
                    zindex.put(label, new Integer[2]);
                }
                // Adding layer and path number to list
                zindex.get(label)[0] = new Integer(k);
                zindex.get(label)[1] = new Integer(pcnt);
            }// End of path loop

        }// End of layer loop

        // Drawing
        // Z-index loop
        String thisdesc = "";
        for (Map.Entry<Double, Integer[]> entry : zindex.entrySet()) {
            byte[] c = ii.palette[entry.getValue()[0]];
            if (options.get("desc") != 0) {
                thisdesc = "desc=\"l " + entry.getValue()[0] + " p " + entry.getValue()[1] + "\" ";
            } else {
                thisdesc = "";
            }
            drawPdfPath(vg2d,
                    thisdesc,
                    ii.layers.get(entry.getValue()[0]).get(entry.getValue()[1]),
                    new Color(c[0] + 128, c[1] + 128, c[2] + 128),
                    options);
        }

        // Write result
        PDFProcessor pdfProcessor = new PDFProcessor(false);
        CommandSequence commands = vg2d.getCommands();
        Document doc = pdfProcessor.getDocument(commands, new PageSize(0.0, 0.0, w, h));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            doc.writeTo(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toString();
    }

    public static void drawPdfPath(VectorGraphics2D graphics, String desc, ArrayList<Double[]> segments, Color color, HashMap<String, Float> options) {
        float scale = options.get("scale"), lcpr = options.get("lcpr"), qcpr = options.get("qcpr"), roundcoords = (float) Math.floor(options.get("roundcoords"));
        graphics.setColor(color);

        final Path2D path = new Path2D.Double();
        path.moveTo(segments.get(0)[1] * scale, segments.get(0)[2] * scale);

        if (roundcoords == -1) {
            for (int pcnt = 0; pcnt < segments.size(); pcnt++) {
                if (segments.get(pcnt)[0] == 1.0) {
                    path.lineTo(segments.get(pcnt)[3] * scale, segments.get(pcnt)[4] * scale);
                } else {
                    path.quadTo(
                            segments.get(pcnt)[3] * scale, segments.get(pcnt)[4] * scale,
                            segments.get(pcnt)[5] * scale, segments.get(pcnt)[6] * scale
                    );
                }
            }
        } else {
            for (int pcnt = 0; pcnt < segments.size(); pcnt++) {
                if (segments.get(pcnt)[0] == 1.0) {
                    path.lineTo(
                            roundtodec((float) (segments.get(pcnt)[3] * scale), roundcoords),
                            roundtodec((float) (segments.get(pcnt)[4] * scale), roundcoords)
                    );
                } else {
                    path.quadTo(
                            roundtodec((float) (segments.get(pcnt)[3] * scale), roundcoords),
                            roundtodec((float) (segments.get(pcnt)[4] * scale), roundcoords),
                            roundtodec((float) (segments.get(pcnt)[5] * scale), roundcoords),
                            roundtodec((float) (segments.get(pcnt)[6] * scale), roundcoords));
                }
            }
        }

        graphics.fill(path);
        graphics.draw(path);

        // Rendering control points
        for (int pcnt = 0; pcnt < segments.size(); pcnt++) {
            if ((lcpr > 0) && (segments.get(pcnt)[0] == 1.0)) {
                graphics.setColor(Color.BLACK);
                graphics.fill(new Ellipse2D.Double(segments.get(pcnt)[3] * scale, segments.get(pcnt)[4] * scale, lcpr, lcpr));
            }
            if ((qcpr > 0) && (segments.get(pcnt)[0] == 2.0)) {
                graphics.setColor(Color.CYAN);
                graphics.setStroke(new BasicStroke((float) (qcpr * 0.2), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, (float[]) null, 0.0F));
                graphics.fill(new Ellipse2D.Double(segments.get(pcnt)[3] * scale, segments.get(pcnt)[4] * scale, qcpr, qcpr));
                graphics.fill(new Ellipse2D.Double(segments.get(pcnt)[5] * scale, segments.get(pcnt)[6] * scale, qcpr, qcpr));
                graphics.draw(new Line2D.Double(segments.get(pcnt)[1] * scale, segments.get(pcnt)[2] * scale, segments.get(pcnt)[3] * scale, segments.get(pcnt)[4] * scale));
                graphics.draw(new Line2D.Double(segments.get(pcnt)[3] * scale, segments.get(pcnt)[4] * scale, segments.get(pcnt)[5] * scale, segments.get(pcnt)[6] * scale));
            }// End of quadratic control points
        }
    }

    public static float roundtodec(float val, float places) {
        return (float) (Math.round(val * Math.pow(10, places)) / Math.pow(10, places));
    }
}
