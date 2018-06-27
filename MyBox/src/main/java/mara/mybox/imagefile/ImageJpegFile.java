package mara.mybox.imagefile;

import mara.mybox.image.ImageTools;
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
import mara.mybox.objects.ImageFileInformation;

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
public class ImageJpegFile {

    private static final Logger logger = LogManager.getLogger();

    public static String[] getJpegCompressionTypes() {
        return new JPEGImageWriteParam(null).getCompressionTypes();
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#image
    public static void writeJPEGImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return;
            }
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (attributes != null && attributes.getCompressionType() != null) {
                    param.setCompressionType(attributes.getCompressionType());
                }
                if (attributes != null && attributes.getQuality() > 0) {
                    param.setCompressionQuality(attributes.getQuality() / 100.0f);
                } else {
                    param.setCompressionQuality(1.0f);
                }
            }
//            logger.debug(param.getCompressionQuality());

            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (attributes != null && attributes.getDensity() > 0) {
                String format = metaData.getNativeMetadataFormatName(); // "javax_imageio_jpeg_image_1.0"
                Element tree = (Element) metaData.getAsTree(format);
                Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
                jfif.setAttribute("Xdensity", attributes.getDensity() + "");
                jfif.setAttribute("Ydensity", attributes.getDensity() + "");
                jfif.setAttribute("resUnits", "1"); // density is dots per inch
                metaData.mergeTree(format, tree);
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void explainJpegMetaData(Map<String, Map<String, Map<String, String>>> metaData, ImageFileInformation info) {
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
//                if (sof.containsKey("samplePrecision")) {
//                    info.setBitDepth(sof.get("samplePrecision"));
//                }
            }
        } catch (Exception e) {

        }
    }

}
