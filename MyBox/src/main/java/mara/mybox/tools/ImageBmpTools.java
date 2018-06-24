package mara.mybox.tools;

import com.github.jaiimageio.impl.plugins.bmp.BMPImageReader;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageReaderSpi;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriter;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriterSpi;
import com.github.jaiimageio.impl.plugins.bmp.BMPMetadata;
import com.github.jaiimageio.plugins.bmp.BMPImageWriteParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.ImageFileInformation;
import static mara.mybox.tools.ImageTools.dpi2dpm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/bmp_metadata.html
public class ImageBmpTools {

    private static final Logger logger = LogManager.getLogger();

    public static String[] getBmpCompressionTypes() {
        return new BMPImageWriteParam(null).getCompressionTypes();
    }

    public static void writeBmpImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        try {
            // BMP's meta data can not be modified and read correctly if standard classes are used.
            // So classes in plugins are used.
            BMPImageWriterSpi spi = new BMPImageWriterSpi();
            BMPImageWriter writer = new BMPImageWriter(spi);
            BMPImageWriteParam param = (BMPImageWriteParam) writer.getDefaultWriteParam();
            if (attributes.getCompressionType() != null) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionType(attributes.getCompressionType());
                param.setCompressionQuality(attributes.getQuality() / 100.0f);
            }

            BMPMetadata metaData = (BMPMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (attributes.getDensity() > 0) {
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

            try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static BMPMetadata getBmpIIOMetadata(File file) {
        try {
            BMPImageReaderSpi tiffspi = new BMPImageReaderSpi();
            BMPImageReader reader = new BMPImageReader(tiffspi);
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                BMPMetadata metadata = (BMPMetadata) reader.getImageMetadata(0);
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void explainBmpMetaData(Map<String, Map<String, Map<String, String>>> metaData, ImageFileInformation info) {
        try {
            String format = "com_sun_media_imageio_plugins_bmp_image_1.0";
            if (!metaData.containsKey(format)) {
                return;
            }
            Map<String, Map<String, String>> javax_imageio_bmp = metaData.get(format);
            if (javax_imageio_bmp.containsKey("Width")) {
                Map<String, String> Width = javax_imageio_bmp.get("Width");
                if (Width.containsKey("value")) {
                    info.setxPixels(Integer.valueOf(Width.get("value")));
                }
            }
            if (javax_imageio_bmp.containsKey("Height")) {
                Map<String, String> Height = javax_imageio_bmp.get("Height");
                if (Height.containsKey("value")) {
                    info.setyPixels(Integer.valueOf(Height.get("value")));
                }
            }
            if (javax_imageio_bmp.containsKey("X")) {    // PixelsPerMeter
                Map<String, String> X = javax_imageio_bmp.get("X");
                if (X.containsKey("value")) {
                    info.setxDensity(ImageTools.dpm2dpi(Integer.valueOf(X.get("value"))));
                }
            }
            if (javax_imageio_bmp.containsKey("Y")) {   // PixelsPerMeter
                Map<String, String> Y = javax_imageio_bmp.get("Y");
                if (Y.containsKey("value")) {
                    info.setyDensity(ImageTools.dpm2dpi(Integer.valueOf(Y.get("value"))));
                }
            }
            if (javax_imageio_bmp.containsKey("BitsPerPixel")) {
                Map<String, String> BitsPerPixel = javax_imageio_bmp.get("BitsPerPixel");
                if (BitsPerPixel.containsKey("value")) {
                    info.setBitDepth(BitsPerPixel.get("value"));
                }
            }
            if (javax_imageio_bmp.containsKey("Compression")) {
                Map<String, String> Compression = javax_imageio_bmp.get("Compression");
                if (Compression.containsKey("value")) {
                    info.setCompressionType(Compression.get("value"));
                }
            }
        } catch (Exception e) {

        }
    }

}
