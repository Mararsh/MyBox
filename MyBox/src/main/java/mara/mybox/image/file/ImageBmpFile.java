package mara.mybox.image.file;

import com.github.jaiimageio.impl.plugins.bmp.BMPImageReader;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageReaderSpi;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriter;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriterSpi;
import com.github.jaiimageio.impl.plugins.bmp.BMPMetadata;
import com.github.jaiimageio.plugins.bmp.BMPImageWriteParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import static mara.mybox.image.ImageConvert.dpi2dpm;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/bmp_metadata.html
public class ImageBmpFile {

    public static String[] getBmpCompressionTypes() {
        return new BMPImageWriteParam(null).getCompressionTypes();
    }

    public static ImageWriter getWriter() {
        // BMP's meta data can not be modified and read correctly if standard classes are used.
        // So classes in plugins are used.
        BMPImageWriterSpi spi = new BMPImageWriterSpi();
        BMPImageWriter writer = new BMPImageWriter(spi);
        return writer;
    }

    public static ImageWriteParam getPara(ImageAttributes attributes, ImageWriter writer) {
        try {
            BMPImageWriteParam param = (BMPImageWriteParam) writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (attributes.getCompressionType() != null) {
                    param.setCompressionType(attributes.getCompressionType());
                }
                if (attributes.getQuality() > 0) {
                    param.setCompressionQuality(attributes.getQuality() / 100.0f);
                } else {
                    param.setCompressionQuality(1.0f);
                }
            }
            return param;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static IIOMetadata getWriterMeta(ImageAttributes attributes, BufferedImage image,
            ImageWriter writer, ImageWriteParam param) {
        try {
            BMPMetadata metaData = (BMPMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (metaData != null && !metaData.isReadOnly() && attributes != null && attributes.getDensity() > 0) {
                String format = metaData.getNativeMetadataFormatName(); // "com_sun_media_imageio_plugins_bmp_image_1.0"
                int dpm = dpi2dpm(attributes.getDensity());
                // If set nodes' attributes in normal way, error will be popped about "Meta Data is read only"
                // By setting its fields, the class will write resolution data under standard format "javax_imageio_1.0"
                // but leave itself's section as empty~ Anyway, the data is record.
                metaData.xPixelsPerMeter = dpm;
                metaData.yPixelsPerMeter = dpm;
                metaData.palette = null; // Error will happen if not define this for Black-white bmp.
                IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(format);
                metaData.mergeTree(format, tree);
            }
            return metaData;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean writeBmpImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {

            ImageWriter writer = getWriter();
            ImageWriteParam param = getPara(attributes, writer);
            IIOMetadata metaData = getWriterMeta(attributes, image, writer, param);

            File tmpFile = FileTools.getTempFile();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            try {
                if (file.exists()) {
                    file.delete();
                }
                tmpFile.renameTo(file);
            } catch (Exception e) {
                return false;
            }
            return true;
        } catch (Exception e) {
            try {
                return ImageIO.write(image, attributes.getImageFormat(), file);
            } catch (Exception e2) {
                return false;
            }
        }
    }

    public static BMPMetadata getBmpIIOMetadata(File file) {
        try {
            BMPImageReader reader = new BMPImageReader(new BMPImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                BMPMetadata metadata = (BMPMetadata) reader.getImageMetadata(0);
                reader.dispose();
                return metadata;
            }
        } catch (Exception e) {
//            logger.error(e.toString());
            return null;
        }
    }

    public static void explainBmpMetaData(Map<String, Map<String, List<Map<String, Object>>>> metaData,
            ImageInformation info) {
        try {
            String format = "com_sun_media_imageio_plugins_bmp_image_1.0";
            if (!metaData.containsKey(format)) {
                return;
            }
            Map<String, List<Map<String, Object>>> javax_imageio_bmp = metaData.get(format);
            if (javax_imageio_bmp.containsKey("Width")) {
                Map<String, Object> Width = javax_imageio_bmp.get("Width").get(0);
                if (Width.containsKey("value")) {
                    info.setWidth(Integer.valueOf((String) Width.get("value")));
                }
            }
            if (javax_imageio_bmp.containsKey("Height")) {
                Map<String, Object> Height = javax_imageio_bmp.get("Height").get(0);
                if (Height.containsKey("value")) {
                    info.setHeight(Integer.valueOf((String) Height.get("value")));
                }
            }
            if (javax_imageio_bmp.containsKey("X")) {    // PixelsPerMeter
                Map<String, Object> X = javax_imageio_bmp.get("X").get(0);
                if (X.containsKey("value")) {
                    info.setXDpi(ImageConvert.dpm2dpi(Integer.valueOf((String) X.get("value"))));
                }
            }
            if (javax_imageio_bmp.containsKey("Y")) {   // PixelsPerMeter
                Map<String, Object> Y = javax_imageio_bmp.get("Y").get(0);
                if (Y.containsKey("value")) {
                    info.setYDpi(ImageConvert.dpm2dpi(Integer.valueOf((String) Y.get("value"))));
                }
            }
            if (javax_imageio_bmp.containsKey("BitsPerPixel")) {
                Map<String, Object> BitsPerPixel = javax_imageio_bmp.get("BitsPerPixel").get(0);
                if (BitsPerPixel.containsKey("value")) {
                    info.setBitDepth(Integer.valueOf((String) BitsPerPixel.get("value")));
                }
            }
            if (javax_imageio_bmp.containsKey("Compression")) {
                Map<String, Object> Compression = javax_imageio_bmp.get("Compression").get(0);
                if (Compression.containsKey("value")) {
                    info.setCompressionType((String) Compression.get("value"));
                }
            }
        } catch (Exception e) {

        }
    }

}
