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
import java.awt.Color;
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

    public static int color2Pixel(int a, int r, int g, int b) {
        return color2Pixel(new Color(a, r, g, b));
//        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int color2Pixel(int r, int g, int b) {
        return color2Pixel(255, r, g, b);
    }

    public static int color2Pixel(Color color) {
        return adjustPixelColor(color.getRGB());
//        try {
//            int a, r, g, b;
//            a = color.getAlpha();
//            r = color.getRed();
//            g = color.getGreen();
//            b = color.getBlue();
//            return a | (r << 16) | (g << 8) | b;
//        } catch (Exception e) {
//            return -1;
//        }
    }

    /*
        加上颜色最大值就是实际颜色值
            -16777216 对应 0xff000000
            -1 对应 0xffffffff
            0xffffff 的值 16777215
     */
    public static int adjustPixelColor(int pixelColor) {
        return 16777216 + pixelColor;
//        if (pixelColor > 8388608) {
//            return pixelColor - 16777216;
//        } else {
//            return pixelColor;
//        }
    }

    public static Color pixel2Color(int pixel) {
        return new Color(pixel);
//        try {
//            int a, r, g, b;
//            a = pixel & 0xff000000;
//            r = (pixel & 0xff0000) >> 16;
//            g = (pixel & 0xff00) >> 8;
//            b = (pixel & 0xff);
//            return new Color(a, r, g, b);
//        } catch (Exception e) {
//            return null;
//        }
    }

    public static int color2GrayPixel(int a, int r, int g, int b) {
        return color2GrayPixel(r, g, b);
    }

    public static int color2GrayPixel(int r, int g, int b) {
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        int pixel = color2Pixel(gray, gray, gray);
        return pixel;
    }

    public static int pixel2GrayPixel(int pixel) {
        Color c = pixel2Color(pixel);
        return color2GrayPixel(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static int color2Gray(int r, int g, int b) {
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        return gray;
    }

    public static int pixel2Gray(int pixel) {
        Color c = pixel2Color(pixel);
        return color2Gray(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static void checkSupportedImageFormats() {
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
                            String[] types = {"CCITT T.6", "CCITT RLE", "CCITT T.4", "ZLib", "Deflate", "LZW", "PackBits"};
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

    public static BufferedImage color2Gray(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int pixel = image.getRGB(i, j);
                    int grayPixel = pixel2GrayPixel(pixel);
                    grayImage.setRGB(i, j, grayPixel);
                }
            }
            return grayImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    //  OTSU algorithm: ICV=PA∗(MA−M)2+PB∗(MB−M)2
    // https://blog.csdn.net/taoyanbian1022/article/details/9030825
    // https://blog.csdn.net/liyuanbhu/article/details/49387483
    public static int calculateThresholdOnGary(BufferedImage grayImage) {
        try {
            int width = grayImage.getWidth();
            int height = grayImage.getHeight();

            int[] grayNumber = new int[256];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
//                    int r = 0xFF & grayImage.getRGB(i, j);
                    int gray = pixel2Gray(grayImage.getRGB(i, j));
                    grayNumber[gray]++;
                }
            }

            float pixelTotal = width * height;
            float[] grayRadio = new float[256];
            for (int i = 0; i < 256; i++) {
                grayRadio[i] = grayNumber[i] / pixelTotal;
            }

            float backgroundNumber, foregroundNumber, backgoundValue, foregroundValue;
            float backgoundAverage, foregroundAverage, imageAverage, delta, deltaMax = 0;
            int threshold = 0;
            for (int gray = 0; gray < 256; gray++) {
                backgroundNumber = 0;
                foregroundNumber = 0;
                backgoundValue = 0;
                foregroundValue = 0;
                for (int i = 0; i < 256; i++) {
                    if (i <= gray) {
                        backgroundNumber += grayRadio[i];
                        backgoundValue += i * grayRadio[i];
                    } else {
                        foregroundNumber += grayRadio[i];
                        foregroundValue += i * grayRadio[i];
                    }
                }

                backgoundAverage = backgoundValue / backgroundNumber;
                foregroundAverage = foregroundValue / foregroundNumber;
                imageAverage = backgoundValue + foregroundValue;

                delta = backgroundNumber * (backgoundAverage - imageAverage) * (backgoundAverage - imageAverage)
                        + foregroundNumber * (foregroundAverage - imageAverage) * (foregroundAverage - imageAverage);

                if (delta > deltaMax) {
                    deltaMax = delta;
                    threshold = gray;
                }
            }
//            logger.debug("threshold:" + threshold);
            return threshold;

        } catch (Exception e) {
            logger.error(e.toString());
            return -1;
        }
    }

    public static BufferedImage color2BinaryWithPercentage(BufferedImage image, int percentage) {
        int threshold = 256 * percentage / 100;
        return color2BinaryWithThreshold(image, threshold);
    }

    public static BufferedImage color2BinaryWithThreshold(BufferedImage image, int threshold) {
        try {
//            logger.error("color2BinaryWithThreshold:" + threshold);
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
            int white = Color.WHITE.getRGB();
            int black = Color.BLACK.getRGB();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int pixel = image.getRGB(i, j);
                    int gray = pixel2Gray(pixel);
                    if (gray < threshold) {
                        binaryImage.setRGB(i, j, black);
                    } else {
                        binaryImage.setRGB(i, j, white);
                    }
                }
            }
            return binaryImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    public static BufferedImage color2Binary(BufferedImage image) {
        try {
            BufferedImage grayImage = color2Gray(image);
            return gray2Binary(grayImage);
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    public static BufferedImage gray2Binary(BufferedImage grayImage) {
        try {
            int threshold = calculateThresholdOnGary(grayImage);
//            logger.error("gray2Binary:" + threshold);
            int width = grayImage.getWidth();
            int height = grayImage.getHeight();
            BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
            int white = Color.WHITE.getRGB();
            int black = Color.BLACK.getRGB();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int pixel = grayImage.getRGB(i, j);
                    int gray = pixel2Gray(pixel);
                    if (gray < threshold) {
                        binaryImage.setRGB(i, j, black);
                    } else {
                        binaryImage.setRGB(i, j, white);
                    }
                }
            }
            return binaryImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return grayImage;
        }
    }

}
