package mara.mybox.tools;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.objects.ImageFileInformation;
import static mara.mybox.tools.FileTools.getFileCreateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import static mara.mybox.tools.ImageTools.pixelSizeMm2dpi;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageReaders {

    private static final Logger logger = LogManager.getLogger();

    public static ImageFileInformation readImageInformation(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                return null;
            }
            ImageFileInformation info = new ImageFileInformation();
            info.setFile(file);
            info.setCreateTime(getFileCreateTime(fileName));
            info.setImageFormat(FileTools.getFileSuffix(fileName).toLowerCase());

            readImageFileInformation(info, file);
            readImageMetaData(info, file);
//            displayMetadata(fileName);
            return info;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void readImageFileInformation(ImageFileInformation info, File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return;
            }
            info.setxPixels(image.getWidth());
            info.setyPixels(image.getHeight());
            ColorModel cm = image.getColorModel();
            if (cm != null) {
                ColorSpace cs = cm.getColorSpace();
                if (cs != null) {
                    info.setColorSpace(ColorTools.getColorSpaceName(cs.getType()));
                }
                info.setColorChannels(cm.getNumComponents());
                info.setBitDepth(cm.getPixelSize() + "");
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void readImageMetaData(ImageFileInformation info, File file) {
        try {
            String format = FileTools.getFileSuffix(file.getName());

            IIOMetadata iioMetaData = getIIOMetadata(format, file);
            if (iioMetaData == null) {
                return;
            }
            Map<String, Map<String, Map<String, String>>> metaData = new HashMap();
            StringBuilder metaDataXml = new StringBuilder();
            readImageMetaData(iioMetaData, metaData, metaDataXml);

            info.setMetaData(metaDataXml.toString());

            explainCommonMetaData(metaData, info);

            switch (format) {
                case "png":
                    ImagePngTools.explainPngMetaData(metaData, info);
                    break;
                case "jpg":
                case "jpeg":
                    ImageJpegTools.explainJpegMetaData(metaData, info);
                    break;
                case "bmp":
                    ImageBmpTools.explainBmpMetaData(metaData, info);
                    break;
                case "tif":
                case "tiff":
                    ImageTiffTools.explainTiffMetaData(info.getMetaData(), info);
                    break;
                default:

            }
//            logger.debug(metaData);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static IIOMetadata getIIOMetadata(String format, File file) {
        switch (format) {
            case "png":
                return getIIOMetadata(file);
            case "jpg":
            case "jpeg":
                return getIIOMetadata(file);
            case "bmp":
                return ImageBmpTools.getBmpIIOMetadata(file);
            case "tif":
            case "tiff":
                return ImageTiffTools.getTiffIIOMetadata(file);
            case "raw":
                return ImageRawTools.getBmpIIOMetadata(file);
            default:
                return getIIOMetadata(file);
        }
    }

    public static IIOMetadata getIIOMetadata(File file) {
        try {
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(iis, false);
                    IIOMetadata iioMetaData = reader.getImageMetadata(0);
                    return iioMetaData;
                }
            }
            return null;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void readImageMetaData(IIOMetadata iioMetaData,
            Map<String, Map<String, Map<String, String>>> meteData, StringBuilder metaDataXml) {
        try {
            if (iioMetaData == null) {
                return;
            }
            String[] formatNames = iioMetaData.getMetadataFormatNames();
            int length = formatNames.length;
            for (int i = 0; i < length; i++) {
                Map<String, Map<String, String>> formatMetaData = new HashMap();
                readImageMetaData(formatMetaData, metaDataXml, iioMetaData.getAsTree(formatNames[i]), 0);
                meteData.put(formatNames[i], formatMetaData);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void readImageMetaData(Map<String, Map<String, String>> formatMetaData,
            StringBuilder metaDataXml, Node node, int level) {
        String lineSeparator = System.getProperty("line.separator");
        for (int i = 0; i < level; i++) {
            metaDataXml.append("    ");
        }
        metaDataXml.append("<").append(node.getNodeName());

        NamedNodeMap map = node.getAttributes();
        if (map != null && map.getLength() > 0) {
            Map<String, String> nodeAttrs = new HashMap();
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                nodeAttrs.put(attr.getNodeName(), attr.getNodeValue());
                metaDataXml.append(" ").append(attr.getNodeName()).append("=\"")
                        .append(attr.getNodeValue()).append("\"");
            }
            formatMetaData.put(node.getNodeName(), nodeAttrs);
        }

        Node child = node.getFirstChild();
        if (child == null) {
            metaDataXml.append("/>").append(lineSeparator);
            return;
        }
        metaDataXml.append(">").append(lineSeparator);
        while (child != null) {
            readImageMetaData(formatMetaData, metaDataXml, child, level + 1);
            child = child.getNextSibling();
        }
        for (int i = 0; i < level; i++) {
            metaDataXml.append("    ");
        }
        metaDataXml.append("</").append(node.getNodeName()).append(">").append(lineSeparator);
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
    public static void explainCommonMetaData(Map<String, Map<String, Map<String, String>>> metaData, ImageFileInformation info) {
        try {
            if (!metaData.containsKey("javax_imageio_1.0")) {
                return;
            }
            Map<String, Map<String, String>> javax_imageio = metaData.get("javax_imageio_1.0");
//            logger.debug("explainCommonMetaData");
            if (javax_imageio.containsKey("ColorSpaceType")) {
                Map<String, String> ColorSpaceType = javax_imageio.get("ColorSpaceType");
                if (ColorSpaceType.containsKey("name")) {
                    info.setColorSpace(ColorSpaceType.get("name"));
                }
            }
            if (javax_imageio.containsKey("NumChannels")) {
                Map<String, String> NumChannels = javax_imageio.get("NumChannels");
                if (NumChannels.containsKey("value")) {
                    info.setColorChannels(Integer.valueOf(NumChannels.get("value")));
                }
            }
            if (javax_imageio.containsKey("CompressionTypeName")) {
                Map<String, String> CompressionTypeName = javax_imageio.get("CompressionTypeName");
                if (CompressionTypeName.containsKey("value")) {
                    info.setCompressionType(CompressionTypeName.get("value"));
                }
            }
            if (javax_imageio.containsKey("Lossless")) {
                Map<String, String> Lossless = javax_imageio.get("Lossless");
                if (Lossless.containsKey("value")) {
                    info.setIsLossless(Lossless.get("value").equals("TRUE"));
                }
            }
            if (javax_imageio.containsKey("ImageOrientation")) {
                Map<String, String> ImageOrientation = javax_imageio.get("ImageOrientation");
                if (ImageOrientation.containsKey("value")) {
                    info.setImageRotation(ImageOrientation.get("value"));
                }
            }
            if (javax_imageio.containsKey("Alpha")) {
                Map<String, String> Alpha = javax_imageio.get("Alpha");
                if (Alpha.containsKey("value")) {
                    info.setHasAlpha(!Alpha.get("value").equals("none"));
                }
            }
            if (javax_imageio.containsKey("HorizontalPixelSize")) { // The width of a pixel, in millimeters
                Map<String, String> HorizontalPixelSize = javax_imageio.get("HorizontalPixelSize");
                if (HorizontalPixelSize.containsKey("value")) {
                    float v = Float.valueOf(HorizontalPixelSize.get("value"));
                    info.setxDensity(pixelSizeMm2dpi(v));
//                    logger.debug("HorizontalPixelSize:" + info.gethResolution());
                }
            }
            if (javax_imageio.containsKey("VerticalPixelSize")) { // The height of a pixel, in millimeters
                Map<String, String> VerticalPixelSize = javax_imageio.get("VerticalPixelSize");
                if (VerticalPixelSize.containsKey("value")) {
                    float v = Float.valueOf(VerticalPixelSize.get("value"));
                    info.setyDensity(pixelSizeMm2dpi(v));
//                    logger.debug("VerticalPixelSize:" + info.getvResolution());
                }
            }
            if (javax_imageio.containsKey("FormatVersion")) { // The height of a pixel, in millimeters
                Map<String, String> FormatVersion = javax_imageio.get("FormatVersion");
                if (FormatVersion.containsKey("value")) {
                    info.setImageFormat(FormatVersion.get("value"));
                }
            }
            if (javax_imageio.containsKey("BitsPerSample")) { // The height of a pixel, in millimeters
                Map<String, String> BitsPerSample = javax_imageio.get("BitsPerSample");
                if (BitsPerSample.containsKey("value")) {
                    info.setBitDepth(BitsPerSample.get("value"));
                }
            }

        } catch (Exception e) {

        }
    }

    // The following 4 methods are from internet
    public static void displayMetadata(String fileName) {
        try {
            System.out.println("\n\n" + fileName);
            File file = new File(fileName);
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(iis, false);
                    IIOMetadata metadata = reader.getImageMetadata(0);
                    String[] names = metadata.getMetadataFormatNames();
                    int length = names.length;
                    for (int i = 0; i < length; i++) {
                        System.out.println("Format name: " + names[i]);
                        displayMetadata(metadata.getAsTree(names[i]));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void displayMetadata(Node root) {
        displayMetadata(root, 0);
    }

    public static void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
    }

    public static void displayMetadata(Node node, int level) {
        // print open tag of element
        indent(level);
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) {

            // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                System.out.print(" " + attr.getNodeName()
                        + "=\"" + attr.getNodeValue() + "\"");
            }
        }

        Node child = node.getFirstChild();
        if (child == null) {
            // no children, so close element and return
            System.out.println("/>");
            return;
        }

        // children, so close current tag
        System.out.println(">");
        while (child != null) {
            // print children recursively
            displayMetadata(child, level + 1);
            child = child.getNextSibling();
        }

        // print close tag of element
        indent(level);
        System.out.println("</" + node.getNodeName() + ">");
    }

}
