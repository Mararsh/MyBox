package mara.mybox.tools;

import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageQuantization;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.KMeansClustering;
import mara.mybox.bufferedimage.ImageRGBKMeans;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlImageQuantization;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.imagefile.ImageFileReaders;
import static mara.mybox.value.Languages.message;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.*;
import thridparty.jankovicsandras.ImageTracer;
import static thridparty.jankovicsandras.ImageTracer.bytetrans;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SvgTools {

    /*
        to image
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

    public static BufferedImage pathToImage(BaseController controller, String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" ><path d=\""
                + path + "\"></path></svg>";
        File tmpFile = textToImage(controller, svg, -1, -1, null);
        if (tmpFile == null || !tmpFile.exists()) {
            return null;
        }
        if (tmpFile.length() <= 0) {
            FileDeleteTools.delete(tmpFile);
            return null;
        }
        return ImageFileReaders.readImage(tmpFile);
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
       to pdf
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

    /*
        image to svg
     */
    public static File imageToSvgFile(BaseController controller, File imageFile,
            ControlImageQuantization quantizationController,
            HashMap<String, Float> options) {
        try {
            if (imageFile == null || !imageFile.exists()) {
                return null;
            }
            BufferedImage image = ImageFileReaders.readImage(imageFile);
            String svg = imageToSvg(controller, image, quantizationController, options);
            if (svg == null || svg.isBlank()) {
                return null;
            }
            File svgFile = FileTmpTools.generateFile("svg");
            ImageTracer.saveString(svgFile.getAbsolutePath(), svg);
            return svgFile;
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static String imageToSvg(BaseController controller, BufferedImage image,
            ControlImageQuantization quantizationController,
            HashMap<String, Float> options) {
        try {
            if (image == null) {
                PopTools.showError(controller, message("InvalidData"));
                return null;
            }
            options = ImageTracer.checkoptions(options);

            // 1. Color quantization
            ImageTracer.IndexedImage ii;
            if (quantizationController != null) {
                ii = colorQuantization(controller, image, quantizationController);
            } else {
                ImageTracer.ImageData imgd = ImageTracer.loadImageData(image);
                ii = ImageTracer.colorquantization(imgd, null, options);
            }

            // 2. Layer separation and edge detection
            int[][][] rawlayers = ImageTracer.layering(ii);

            // 3. Batch pathscan
            ArrayList<ArrayList<ArrayList<Integer[]>>> bps
                    = ImageTracer.batchpathscan(rawlayers, (int) (Math.floor(options.get("pathomit"))));

            // 4. Batch interpollation
            ArrayList<ArrayList<ArrayList<Double[]>>> bis = ImageTracer.batchinternodes(bps);

            // 5. Batch tracing
            ii.layers = ImageTracer.batchtracelayers(bis, options.get("ltres"), options.get("qtres"));

            return ImageTracer.getsvgstring(ii, options);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static ImageTracer.IndexedImage colorQuantization(BaseController controller,
            BufferedImage image, ControlImageQuantization quantization) {
        try {
            ImageKMeans kmeans = ImageKMeans.create();
            kmeans.setAlgorithm(KMeansClustering).
                    setQuantizationSize(quantization.getQuanColors())
                    .setRegionSize(quantization.getRegionSize())
                    .setWeight1(quantization.getRgbWeight1())
                    .setWeight2(quantization.getRgbWeight2())
                    .setWeight3(quantization.getRgbWeight3())
                    .setRecordCount(false)
                    .setFirstColor(quantization.getFirstColorCheck().isSelected())
                    .setOperationType(PixelsOperation.OperationType.Quantization)
                    .setImage(image).setScope(null)
                    .setIsDithering(quantization.getQuanDitherCheck().isSelected());
            kmeans.makePalette().operate();
            return new ImageTracer.IndexedImage(kmeans.getColorIndice(), kmeans.paletteBytes);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static class ImageKMeans extends ImageQuantization {

        public int[][] colorIndice;
        public byte[][] paletteBytes;
        protected ImageRGBKMeans kmeans;
        protected List<java.awt.Color> paletteColors;

        public static ImageKMeans create() {
            return new ImageKMeans();
        }

        public ImageKMeans makePalette() {
            try {
                kmeans = imageKMeans();
                paletteColors = kmeans.getCenters();
                int size = paletteColors.size();
                paletteBytes = new byte[size + 1][4];
                for (int i = 0; i < size; i++) {
                    int value = paletteColors.get(i).getRGB();
                    paletteBytes[i][3] = bytetrans((byte) (value >>> 24));
                    paletteBytes[i][0] = bytetrans((byte) (value >>> 16));
                    paletteBytes[i][1] = bytetrans((byte) (value >>> 8));
                    paletteBytes[i][2] = bytetrans((byte) (value));
                }
                int transparent = 0;
                paletteBytes[size][3] = bytetrans((byte) (transparent >>> 24));
                paletteBytes[size][0] = bytetrans((byte) (transparent >>> 16));
                paletteBytes[size][1] = bytetrans((byte) (transparent >>> 8));
                paletteBytes[size][2] = bytetrans((byte) (transparent));

                int width = image.getWidth();
                int height = image.getHeight();
                colorIndice = new int[height + 2][width + 2];
                for (int j = 0; j < (height + 2); j++) {
                    colorIndice[j][0] = -1;
                    colorIndice[j][width + 1] = -1;
                }
                for (int i = 0; i < (width + 2); i++) {
                    colorIndice[0][i] = -1;
                    colorIndice[height + 1][i] = -1;
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            return this;
        }

        @Override
        public java.awt.Color operateColor(java.awt.Color color) {
            try {
                java.awt.Color mappedColor = kmeans.map(color);
                int index = paletteColors.indexOf(mappedColor);
                colorIndice[currentY + 1][currentX + 1] = index;
                return mappedColor;
            } catch (Exception e) {
                return color;
            }
        }

        /*
            get/set
         */
        public int[][] getColorIndice() {
            return colorIndice;
        }

        public byte[][] getPaletteBytes() {
            return paletteBytes;
        }

    }

    // svg to awt
    // Following method is copied from "org.apache.batik.ext.awt.geom.ExtendedGeneralPath "
    /**
     * This constructs an unrotated Arc2D from the SVG specification of an
     * Elliptical arc. To get the final arc you need to apply a rotation
     * transform such as:
     *
     * AffineTransform.getRotateInstance (angle, arc.getX()+arc.getWidth()/2,
     * arc.getY()+arc.getHeight()/2);
     */
    public static Arc2D computeArc(double x0, double y0,
            double rx, double ry,
            double angle,
            boolean largeArcFlag,
            boolean sweepFlag,
            double x, double y) {
        //
        // Elliptical arc implementation based on the SVG specification notes
        //

        // Compute the half distance between the current and the final point
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        // Convert angle from degrees to radians
        angle = Math.toRadians(angle % 360.0);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);

        //
        // Step 1 : Compute (x1, y1)
        //
        double x1 = (cosAngle * dx2 + sinAngle * dy2);
        double y1 = (-sinAngle * dx2 + cosAngle * dy2);
        // Ensure radii are large enough
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;
        // check that radii are large enough
        double radiiCheck = Px1 / Prx + Py1 / Pry;
        if (radiiCheck > 0.99999) {  // don't cut it too close
            double radiiScale = Math.sqrt(radiiCheck) * 1.00001;
            rx = radiiScale * rx;
            ry = radiiScale * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        //
        // Step 2 : Compute (cx1, cy1)
        //
        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1)) / ((Prx * Py1) + (Pry * Px1));
        sq = (sq < 0) ? 0 : sq;
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        //
        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        //
        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        //
        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        //
        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;
        // Compute the angle start
        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1.0 : 1.0;
        double angleStart = Math.toDegrees(sign * Math.acos(p / n));

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
        double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
        if (!sweepFlag && angleExtent > 0) {
            angleExtent -= 360f;
        } else if (sweepFlag && angleExtent < 0) {
            angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        //
        // We can now build the resulting Arc2D in double precision
        //
        Arc2D.Double arc = new Arc2D.Double();
        arc.x = cx - rx;
        arc.y = cy - ry;
        arc.width = rx * 2.0;
        arc.height = ry * 2.0;
        arc.start = -angleStart;
        arc.extent = -angleExtent;

        return arc;
    }

}
