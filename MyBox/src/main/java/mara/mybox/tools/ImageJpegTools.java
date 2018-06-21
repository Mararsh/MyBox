package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.ImageInformation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageJpegTools {

    private static final Logger logger = LogManager.getLogger();

    public static String[] getJpegCompressionTypes() {
        return new JPEGImageWriteParam(null).getCompressionTypes();
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#image
    public static void writeJPEGImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        try {

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG");
            param.unsetCompression();
            param.setCompressionQuality(attributes.getQuality() / 100f);

            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            String format = metaData.getNativeMetadataFormatName(); // "javax_imageio_jpeg_image_1.0"
            Element tree = (Element) metaData.getAsTree(format);
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", attributes.getDensity() + "");
            jfif.setAttribute("Ydensity", attributes.getDensity() + "");
            jfif.setAttribute("resUnits", "1"); // density is dots per inch
            metaData.mergeTree(format, tree);

            try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
            }
            writer.dispose();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void explainJpegMetaData(Map<String, Map<String, Map<String, String>>> metaData, ImageInformation info) {
        try {
            if (!metaData.containsKey("javax_imageio_jpeg_image_1.0")) {
                return;
            }
//            logger.debug("explainPngMetaData");
            Map<String, Map<String, String>> javax_imageio_jpeg = metaData.get("javax_imageio_jpeg_image_1.0");
            if (javax_imageio_jpeg.containsKey("app0JFIF")) {
                Map<String, String> app0JFIF = javax_imageio_jpeg.get("app0JFIF");
                if (app0JFIF.containsKey("resUnits")) {
                    boolean isDpi = Integer.valueOf(app0JFIF.get("resUnits")) == 1;
                    if (app0JFIF.containsKey("Xdensity")) {
                        int v = Integer.valueOf(app0JFIF.get("Xdensity"));
                        if (!isDpi) {
                            info.setxDensity(ImageTools.dpi2dpcm(v));  // density value should be dpi
                        } else {
                            info.setxDensity(v);
                        }
                    }
                    if (app0JFIF.containsKey("Ydensity")) {
                        int v = Integer.valueOf(app0JFIF.get("Ydensity"));
                        if (!isDpi) {
                            info.setyDensity(ImageTools.dpi2dpcm(v));  // density value should be dpi
                        } else {
                            info.setyDensity(v);
                        }
                    }
                }
            }
            if (javax_imageio_jpeg.containsKey("sof")) {
                Map<String, String> sof = javax_imageio_jpeg.get("sof");
                if (sof.containsKey("numLines")) {
                    info.setyPixels(Integer.valueOf(sof.get("numLines")));
                }
                if (sof.containsKey("samplesPerLine")) {
                    info.setxPixels(Integer.valueOf(sof.get("samplesPerLine")));
                }
            }
        } catch (Exception e) {

        }
    }

}
