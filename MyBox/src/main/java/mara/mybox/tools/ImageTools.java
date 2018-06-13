package mara.mybox.tools;

import com.github.jaiimageio.impl.plugins.bmp.BMPImageReaderSpi;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriter;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriterSpi;
import com.github.jaiimageio.impl.plugins.gif.GIFImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageWriterSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageReaderSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriteParam;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriter;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriterSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.impl.plugins.wbmp.WBMPImageWriterSpi;
import com.github.jaiimageio.plugins.bmp.BMPImageWriteParam;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTools {

    private static final Logger logger = LogManager.getLogger();

    public static void checkImageFormats() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new TIFFImageWriterSpi());
        registry.registerServiceProvider(new TIFFImageReaderSpi());
        registry.registerServiceProvider(new RawImageWriterSpi());
        registry.registerServiceProvider(new RawImageReaderSpi());
        registry.registerServiceProvider(new BMPImageWriterSpi());
        registry.registerServiceProvider(new BMPImageReaderSpi());
        registry.registerServiceProvider(new GIFImageWriterSpi());
        registry.registerServiceProvider(new PCXImageWriterSpi());
        registry.registerServiceProvider(new PNMImageWriterSpi());
        registry.registerServiceProvider(new WBMPImageWriterSpi());

        String readFormats[] = ImageIO.getReaderFormatNames();
        String writeFormats[] = ImageIO.getWriterFormatNames();
        System.out.println("Readers:" + Arrays.asList(readFormats));
        System.out.println("Writers:" + Arrays.asList(writeFormats));
        // Readers:[JPEG 2000, JPG, jpg, JPEG2000, bmp, BMP, gif, GIF, WBMP, png, PNG, JPEG, jpeg2000, wbmp, jpeg, jpeg 2000]
        //Writers:[JPEG 2000, JPG, jpg, JPEG2000, tiff, bmp, BMP, gif, GIF, WBMP, png, PNG, JPEG, tif, TIF, TIFF, jpeg2000, wbmp, jpeg, jpeg 2000]
    }

    public static String[] getCompressionTypes(String imageFormat, ImageType imageColor) {
        String[] compressionTypes = null;
        switch (imageFormat) {
            case "jpg":
                compressionTypes = ImageTools.getJpegCompressionTypes();
                break;
            case "tif":   // Summarized based on API of class "com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam" and debugging
                if (null != imageColor) {
                    switch (imageColor) {
                        case BINARY: {
                            String[] types = {"CCITT RLE", "CCITT T.4", "CCITT T.6", "ZLib", "Deflate", "LZW", "PackBits"};
                            return types;
                        }
                        case GRAY: {
                            String[] types = {"ZLib", "Deflate", "LZW", "JPEG", "PackBits"};
                            return types;
                        }
                        case RGB: {
                            String[] types = {"ZLib", "Deflate", "EXIF JPEG", "LZW", "JPEG", "PackBits"};
                            return types;
                        }
                        case ARGB: {
                            String[] types = {"ZLib", "Deflate", "LZW", "PackBits"};
                            return types;
                        }
                        default:
                            break;
                    }
                }
//                compressionTypes = ImageTools.getTiffCompressionTypes();
                break;
            case "bmp":  // Summarized based on API of class "com.github.jaiimageio.plugins.bmp.BMPImageWriteParam" and debugging
                if (null != imageColor) {
                    switch (imageColor) {
                        case BINARY: {
                            String[] types = {"BI_RGB", "BI_BITFIELDS", "BI_JPEG", "BI_PNG"};
                            return types;
                        }
                        case GRAY: {
                            String[] types = {"BI_RGB", "BI_RLE8", "BI_BITFIELDS", "BI_JPEG", "BI_PNG"};
                            return types;
                        }
                        case RGB: {
                            String[] types = {"BI_RGB", "BI_BITFIELDS", "BI_JPEG", "BI_PNG"};
                            return types;
                        }
                        case ARGB: {
                            return null;
                        }
                        default:
                            break;
                    }
                }
//                 compressionTypes = ImageTools.getBmpCompressionTypes();
                break;
            default:
                try {
                    ImageWriter writer = ImageIO.getImageWritersByFormatName(imageFormat).next();
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    compressionTypes = param.getCompressionTypes();
                } catch (Exception e) {

                }
        }
        return compressionTypes;
    }

    public static String[] getJpegCompressionTypes() {
        return new JPEGImageWriteParam(null).getCompressionTypes();
    }

    public static String[] getTiffCompressionTypes() {
        return new TIFFImageWriteParam(null).getCompressionTypes();
    }

    public static String[] getBmpCompressionTypes() {
        return new BMPImageWriteParam(null).getCompressionTypes();
    }

    public static boolean canImageCompressed(String imageFormat) {
        return "jpg".equals(imageFormat) || "tif".equals(imageFormat) || "bmp".equals(imageFormat);
    }

    public static void writeImageFile(BufferedImage image,
            String imageFormat, Map<String, Object> parameters, String outFile) {
        if (image == null || imageFormat == null || outFile == null) {
            return;
        }
        try {
            try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                switch (imageFormat.toLowerCase()) {
                    case "tif":
                        writeTiffImageFile(image, parameters, out);
                        break;
                    case "raw":
                        writeRawImageFile(image, parameters, out);
                        break;
                    case "jpg":
                        writeJPEGImageFile(image, parameters, out);
                        break;
                    case "bmp":
                        writeBmpImageFile(image, parameters, out);
                        break;
                    default:
                        writeImageFile(image, imageFormat, parameters, out);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void writeImageFile(BufferedImage image,
            String imageFormat, Map<String, Object> parameters, ImageOutputStream out) {
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName(imageFormat).next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            writeImageFile(writer, param, image, parameters, out);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void writeJPEGImageFile(BufferedImage image,
            Map<String, Object> parameters, ImageOutputStream out) {
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            JPEGImageWriteParam param = (JPEGImageWriteParam) writer.getDefaultWriteParam();

            writeImageFile(writer, param, image, parameters, out);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void writeBmpImageFile(BufferedImage image,
            Map<String, Object> parameters, ImageOutputStream out) {
        try {
            BMPImageWriterSpi spi = new BMPImageWriterSpi();
            BMPImageWriter writer = new BMPImageWriter(spi);
            BMPImageWriteParam param = (BMPImageWriteParam) writer.getDefaultWriteParam();

            writeImageFile(writer, param, image, parameters, out);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void writeRawImageFile(BufferedImage image,
            Map<String, Object> parameters, ImageOutputStream out) {
        try {
            RawImageWriterSpi spi = new RawImageWriterSpi();
            RawImageWriter writer = new RawImageWriter(spi);
            RawImageWriteParam param = (RawImageWriteParam) writer.getDefaultWriteParam();

            writeImageFile(writer, param, image, parameters, out);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void writeTiffImageFile(BufferedImage image,
            Map<String, Object> parameters, ImageOutputStream out) {
        try {
            TIFFImageWriterSpi tiffspi = new TIFFImageWriterSpi();
            TIFFImageWriter writer = new TIFFImageWriter(tiffspi);
            TIFFImageWriteParam param = (TIFFImageWriteParam) writer.getDefaultWriteParam();

            writeImageFile(writer, param, image, parameters, out);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void writeImageFile(ImageWriter writer, ImageWriteParam param,
            BufferedImage image, Map<String, Object> parameters, ImageOutputStream out) {
        try {
            if (parameters.containsKey("compressionType") && param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionType((String) parameters.get("compressionType"));
//                logger.debug((String) parameters.get("compressionType"));

                if (parameters.containsKey("quality")) {
                    float f = (int) parameters.get("quality") / (float) 100;
                    param.setCompressionQuality(f);
                }
            }

            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
