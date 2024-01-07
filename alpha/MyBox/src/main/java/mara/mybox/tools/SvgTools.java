package mara.mybox.tools;

import java.awt.Rectangle;
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
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ImageQuantization;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.KMeansClustering;
import mara.mybox.bufferedimage.ImageRGBKMeans;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlImageQuantization;
import mara.mybox.controller.ControlSvgFromImage;
import mara.mybox.controller.ControlSvgFromImage.Algorithm;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxTask;
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
import thridparty.miguelemosreverte.ImageTracer;
import static thridparty.miguelemosreverte.ImageTracer.bytetrans;
import thridparty.miguelemosreverte.SVGUtils;
import thridparty.miguelemosreverte.VectorizingUtils;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SvgTools {

    /*
        to image
     */
    public static File docToImage(FxTask task, BaseController controller, Document doc,
            float width, float height, Rectangle area) {
        if (doc == null) {
            return null;
        }
        return textToImage(task, controller, XmlTools.transform(doc), width, height, area);
    }

    public static File textToImage(FxTask task, BaseController controller, String svg,
            float width, float height, Rectangle area) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        String handled = handleAlpha(task, controller, svg); // Looks batik does not supper color formats with alpha
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(handled.getBytes("utf-8"))) {
            return toImage(task, controller, inputStream, width, height, area);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static File fileToImage(FxTask task, BaseController controller, File file,
            float width, float height, Rectangle area) {
        if (file == null || !file.exists()) {
            return null;
        }
        return textToImage(task, controller, TextFileTools.readTexts(task, file), width, height, area);
    }

    public static BufferedImage pathToImage(FxTask task, BaseController controller, String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" ><path d=\""
                + path + "\"></path></svg>";
        File tmpFile = textToImage(task, controller, svg, -1, -1, null);
        if (tmpFile == null || !tmpFile.exists()) {
            return null;
        }
        if (tmpFile.length() <= 0) {
            FileDeleteTools.delete(tmpFile);
            return null;
        }
        return ImageFileReaders.readImage(task, tmpFile);
    }

    public static File toImage(FxTask task, BaseController controller, InputStream inputStream,
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
    public static File docToPDF(FxTask task, BaseController controller, Document doc,
            float width, float height, Rectangle area) {
        if (doc == null) {
            return null;
        }
        return textToPDF(task, controller, XmlTools.transform(doc), width, height, area);
    }

    public static File textToPDF(FxTask task, BaseController controller, String svg,
            float width, float height, Rectangle area) {
        if (svg == null || svg.isBlank()) {
            return null;
        }
        String handled = handleAlpha(task, controller, svg); // Looks batik does not supper color formats with alpha
        if (handled == null || (task != null && !task.isWorking())) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(handled.getBytes("utf-8"))) {
            return toPDF(task, controller, inputStream, width, height, area);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static File fileToPDF(FxTask task, BaseController controller, File file,
            float width, float height, Rectangle area) {
        if (file == null || !file.exists()) {
            return null;
        }
        return textToPDF(task, controller, TextFileTools.readTexts(task, file), width, height, area);
    }

    public static File toPDF(FxTask task, BaseController controller, InputStream inputStream,
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
    public static String handleAlpha(FxTask task, BaseController controller, String svg) {
        if (svg == null || svg.isBlank()) {
            return svg;
        }
        try {
            Document doc = XmlTools.textToDoc(task, controller, svg);
            if (doc == null || (task != null && !task.isWorking())) {
                return svg;
            }
            handleAlpha(task, controller, doc);
            return XmlTools.transform(doc);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static boolean handleAlpha(FxTask task, BaseController controller, Node node) {
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
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    Node child = children.item(i);
                    handleAlpha(task, controller, child);
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
        return XmlTools.textToDoc(null, null, blankSVG(width, height));
    }

    public static Document blankDoc() {
        return blankDoc(500.0f, 500.0f);
    }

    public static Document focus(FxTask task, Document doc, Node node, float bgOpacity) {
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
            if (task != null && !task.isWorking()) {
                return doc;
            }
            Node parent = cnode.getParentNode();
            if (parent == null) {
                break;
            }
            NodeList nodes = parent.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (task != null && !task.isWorking()) {
                    return doc;
                }
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

    public static Document removeSize(Document doc) {
        if (doc == null) {
            return doc;
        }
        Document clonedDoc = (Document) doc.cloneNode(true);
        Element svgNode = XmlTools.findName(clonedDoc, "svg", 0);
        svgNode.removeAttribute("width");
        svgNode.removeAttribute("height");
        svgNode.removeAttribute("viewBox");
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

    public static String toText(FxTask task, SVGGraphics2D g) {
        String s = TextFileTools.readTexts(task, toFile(g), Charset.forName("utf-8"));
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
    public static String imagefileToSvg(FxTask task, BaseController controller,
            File imageFile, ControlSvgFromImage optionsController) {
        try {
            if (imageFile == null || !imageFile.exists()) {
                return null;
            }
            BufferedImage image = ImageFileReaders.readImage(task, imageFile);
            if (image == null || (task != null && !task.isWorking())) {
                return null;
            }
            return imageToSvg(task, controller, image, optionsController);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    // https://github.com/miguelemosreverte/imagetracerjava
    public static String imageToSvg(FxTask task, BaseController controller,
            BufferedImage inImage, ControlSvgFromImage optionsController) {
        try {
            if (optionsController == null || inImage == null) {
                PopTools.showError(controller, message("InvalidData"));
                return null;
            }
            BufferedImage image = AlphaTools.removeAlpha(task, inImage);
            switch (optionsController.getQuantization()) {
                case jankovicsandras:
                    return jankovicsandras(task, controller, image, optionsController);
                case mybox:
                    return mybox(task, controller, image, optionsController);
                default:
                    return miguelemosreverte(task, controller, image, optionsController);
            }

        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static String miguelemosreverte(FxTask task, BaseController controller,
            BufferedImage image, ControlSvgFromImage optionsController) {
        try {
            if (image == null || optionsController == null) {
                return null;
            }
            HashMap<String, Float> options = optionsController.getOptions();
            options = ImageTracer.checkoptions(options);
            if (options == null || (task != null && !task.isWorking())) {
                return null;
            }
            ImageTracer.IndexedImage ii
                    = miguelemosreverteQuantization(task, controller, image, options);
            if (ii == null || (task != null && !task.isWorking())) {
                return null;
            }
            return miguelemosreverteSVG(task, controller, ii, options);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static ImageTracer.IndexedImage miguelemosreverteQuantization(FxTask task,
            BaseController controller, BufferedImage image, HashMap<String, Float> options) {
        try {
            if (image == null) {
                PopTools.showError(controller, message("InvalidData"));
                return null;
            }
            if (options == null || (task != null && !task.isWorking())) {
                return null;
            }
            ImageTracer.ImageData imgd = ImageTracer.loadImageData(image);
            if (imgd == null || (task != null && !task.isWorking())) {
                return null;
            }
            byte[][] palette = ImageTracer.getPalette(image, options);
            if (palette == null || (task != null && !task.isWorking())) {
                return null;
            }
            return VectorizingUtils.colorquantization(imgd, palette, options);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static String miguelemosreverteSVG(FxTask task, BaseController controller,
            ImageTracer.IndexedImage ii, HashMap<String, Float> options) {
        try {
            if (ii == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 2. Layer separation and edge detection
            int[][][] rawlayers = VectorizingUtils.layering(ii);
            if (rawlayers == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 3. Batch pathscan
            ArrayList<ArrayList<ArrayList<Integer[]>>> bps
                    = VectorizingUtils.batchpathscan(rawlayers, (int) (Math.floor(options.get("pathomit"))));
            if (bps == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 4. Batch interpollation
            ArrayList<ArrayList<ArrayList<Double[]>>> bis = VectorizingUtils.batchinternodes(bps);
            if (bis == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 5. Batch tracing
            ii.layers = VectorizingUtils.batchtracelayers(bis, options.get("ltres"), options.get("qtres"));
            if (ii.layers == null || (task != null && !task.isWorking())) {
                return null;
            }

            return SVGUtils.getsvgstring(ii, options);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static String jankovicsandras(FxTask task, BaseController controller,
            BufferedImage image, ControlSvgFromImage optionsController) {
        try {
            if (image == null || optionsController == null) {
                return null;
            }
            HashMap<String, Float> options = optionsController.getOptions();
            options = thridparty.jankovicsandras.ImageTracer.checkoptions(options);
            if (options == null || (task != null && !task.isWorking())) {
                return null;
            }
            thridparty.jankovicsandras.ImageTracer.IndexedImage ii
                    = jankovicsandrasQuantization(task, controller, image, options);
            if (ii == null || (task != null && !task.isWorking())) {
                return null;
            }
            return jankovicsandrasSVG(task, controller, ii, options);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    // https://github.com/jankovicsandras/imagetracerjava
    public static thridparty.jankovicsandras.ImageTracer.IndexedImage
            jankovicsandrasQuantization(FxTask task, BaseController controller,
                    BufferedImage image, HashMap<String, Float> options) {
        try {
            if (image == null || options == null) {
                return null;
            }
            thridparty.jankovicsandras.ImageTracer.ImageData imgd
                    = thridparty.jankovicsandras.ImageTracer.loadImageData(image);
            if (imgd == null || (task != null && !task.isWorking())) {
                return null;
            }
            return thridparty.jankovicsandras.ImageTracer.colorquantization(imgd, null, options);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static String jankovicsandrasSVG(FxTask task, BaseController controller,
            thridparty.jankovicsandras.ImageTracer.IndexedImage ii,
            HashMap<String, Float> options) {
        try {
            if (ii == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 2. Layer separation and edge detection
            int[][][] rawlayers = thridparty.jankovicsandras.ImageTracer.layering(ii);
            if (rawlayers == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 3. Batch pathscan
            ArrayList<ArrayList<ArrayList<Integer[]>>> bps
                    = thridparty.jankovicsandras.ImageTracer.batchpathscan(rawlayers,
                            (int) (Math.floor(options.get("pathomit"))));
            if (bps == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 4. Batch interpollation
            ArrayList<ArrayList<ArrayList<Double[]>>> bis
                    = thridparty.jankovicsandras.ImageTracer.batchinternodes(bps);
            if (bis == null || (task != null && !task.isWorking())) {
                return null;
            }

            // 5. Batch tracing
            ii.layers = thridparty.jankovicsandras.ImageTracer.batchtracelayers(bis,
                    options.get("ltres"), options.get("qtres"));
            if (ii.layers == null || (task != null && !task.isWorking())) {
                return null;
            }

            return thridparty.jankovicsandras.ImageTracer.getsvgstring(ii, options);
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static ImageKMeans myboxQuantization(FxTask task,
            BaseController controller, BufferedImage image,
            ControlImageQuantization quantization) {
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
                    .setIsDithering(quantization.getQuanDitherCheck().isSelected())
                    .setTask(task);
            kmeans.makePalette().start();
            return kmeans;
        } catch (Exception e) {
            PopTools.showError(controller, e.toString());
            return null;
        }
    }

    public static String mybox(FxTask task, BaseController controller,
            BufferedImage image, ControlSvgFromImage optionsController) {
        try {
            if (image == null || optionsController == null) {
                return null;
            }
            ImageKMeans kmeans = myboxQuantization(task, controller, image,
                    optionsController.getQuantizationController());
            if (kmeans == null
                    || kmeans.paletteBytes == null
                    || kmeans.getColorIndice() == null
                    || (task != null && !task.isWorking())) {
                return null;
            }
            HashMap<String, Float> options = optionsController.getOptions();
            MyBoxLog.console(optionsController.getLayer().name());
            if (optionsController.getLayer() == Algorithm.jankovicsandras) {
                options = thridparty.jankovicsandras.ImageTracer.checkoptions(options);
                if (options == null || (task != null && !task.isWorking())) {
                    return null;
                }
                thridparty.jankovicsandras.ImageTracer.IndexedImage ii
                        = new thridparty.jankovicsandras.ImageTracer.IndexedImage(
                                kmeans.getColorIndice(), kmeans.paletteBytes);
                return jankovicsandrasSVG(task, controller, ii, options);
            } else {
                options = ImageTracer.checkoptions(options);
                if (options == null || (task != null && !task.isWorking())) {
                    return null;
                }
                ImageTracer.IndexedImage ii = new ImageTracer.IndexedImage(
                        kmeans.getColorIndice(), kmeans.paletteBytes);
                return miguelemosreverteSVG(task, controller, ii, options);
            }

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
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    colorIndice[j][0] = -1;
                    colorIndice[j][width + 1] = -1;
                }
                for (int i = 0; i < (width + 2); i++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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

}
