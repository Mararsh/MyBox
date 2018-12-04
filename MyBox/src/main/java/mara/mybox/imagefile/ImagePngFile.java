package mara.mybox.imagefile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.objects.ImageAttributes;
import static mara.mybox.image.ImageValueTools.dpi2dpm;
import static mara.mybox.image.ImageValueTools.dpm2dpi;
import mara.mybox.objects.ImageInformation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePngFile {

    private static final Logger logger = LogManager.getLogger();

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/png_metadata.html#image
    public static boolean writePNGImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
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

            IIOMetadata metaData;
            try {
                metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                if (metaData != null && !metaData.isReadOnly() && attributes != null && attributes.getDensity() > 0) {
                    String format = metaData.getNativeMetadataFormatName(); // "javax_imageio_png_1.0"
                    IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(format);
                    IIOMetadataNode pHYs = new IIOMetadataNode("pHYs");
                    String dpm = dpi2dpm(attributes.getDensity()) + "";
                    pHYs.setAttribute("pixelsPerUnitXAxis", dpm);
                    pHYs.setAttribute("pixelsPerUnitYAxis", dpm);
                    pHYs.setAttribute("unitSpecifier", "meter");  // density is dots per !Meter!
                    tree.appendChild(pHYs);
                    metaData.mergeTree(format, tree);
                }
            } catch (Exception e) {
                logger.error(e.toString());
                metaData = null;
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/png_metadata.html#image
    public static void explainPngMetaData(Map<String, Map<String, Map<String, String>>> metaData,
            ImageInformation info) {
        try {
            if (!metaData.containsKey("javax_imageio_png_1.0")) {
                return;
            }
//            logger.debug("explainPngMetaData");
            Map<String, Map<String, String>> javax_imageio_png = metaData.get("javax_imageio_png_1.0");
            if (javax_imageio_png.containsKey("IHDR")) {
                Map<String, String> IHDR = javax_imageio_png.get("IHDR");
                if (IHDR.containsKey("width")) {
                    info.setWidth(Integer.valueOf(IHDR.get("width")));
                }
                if (IHDR.containsKey("height")) {
                    info.setHeight(Integer.valueOf(IHDR.get("height")));
                }
                if (IHDR.containsKey("bitDepth")) {
                    info.setBitDepth(IHDR.get("bitDepth"));
                }
                if (IHDR.containsKey("colorType")) {
                    info.setColorSpace(IHDR.get("colorType"));
//                    logger.debug("colorType:" + info.getColorSpace());
                }
                if (IHDR.containsKey("compressionMethod")) {
                    info.setCompressionType(IHDR.get("compressionMethod"));
//                    logger.debug("compressionMethod:" + info.getCompressionType());
                }
            }
            if (javax_imageio_png.containsKey("pHYs")) {
                Map<String, String> pHYs = javax_imageio_png.get("pHYs");
                if (pHYs.containsKey("unitSpecifier")) {
                    boolean isMeter = "meter".equals(pHYs.get("unitSpecifier"));
                    if (pHYs.containsKey("pixelsPerUnitXAxis")) {
                        int v = Integer.valueOf(pHYs.get("pixelsPerUnitXAxis"));
                        if (isMeter) {
                            info.setwDensity(dpm2dpi(v));  // resolution value should be dpi
                        } else {
                            info.setwDensity(v);
                        }
//                        logger.debug("pixelsPerUnitXAxis:" + info.gethResolution());
                    }
                    if (pHYs.containsKey("pixelsPerUnitYAxis")) {
                        int v = Integer.valueOf(pHYs.get("pixelsPerUnitYAxis"));
                        if (isMeter) {
                           info.sethDensity(dpm2dpi(v));   // resolution value should be dpi
                        } else {
                           info.sethDensity(v);
                        }
//                        logger.debug("pixelsPerUnitYAxis:" + info.getvResolution());
                    }
                }
            }

        } catch (Exception e) {

        }
    }

}
