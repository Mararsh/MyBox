package mara.mybox.tools;

import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriterSpi;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageReaderSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageReaderSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriterSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import static mara.mybox.tools.ImageJpegTools.getJpegCompressionTypes;

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

    // dpi(dot per inch) convert to dpm(dot per millimeter)
    public static int dpi2dpmm(int dpi) {
        return (int) (dpi / 25.4f + 0.5f);
    }

    // dpi(dot per inch) convert to  ppm(dot per meter)
    public static int dpi2dpm(int dpi) {
        return (int) (1000 * dpi / 25.4f + 0.5f);
    }

    // ppm(dot Per Meter)  convert to  dpi(dot per inch)
    public static int dpm2dpi(int dpm) {
        return (int) (dpm * 25.4f / 1000f + 0.5f);
    }

    // dpi(dot per inch) convert to dpm(dot per centimeter)
    public static int dpi2dpcm(int dpi) {
        return (int) (dpi / 2.54f + 0.5f);
    }

    // dpm(dot per centimeter) convert to  dpi(dot per inch)
    public static int dpcm2dpi(int dpcm) {
        return (int) (dpcm * 2.54f + 0.5f);
    }

    public static int inch2cm(float inch) {
        return (int) (inch / 2.54f + 0.5f);
    }

    // "pixel size in millimeters" convert to  dpi(dot per inch)
    public static int pixelSizeMm2dpi(float psmm) { //
        if (psmm == 0) {
            return 0;
        }
        float f = 25.4f / psmm + 0.5f;
        return (int) f;
    }

    //  dpi(dot per inch) convert to  "pixel size in millimeters"
    public static float dpi2pixelSizeMm(int dpi) { //
        if (dpi == 0) {
            return 0;
        }
        return 25.4f / dpi;
    }

    public static void registrySupportedImageFormats() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new TIFFImageWriterSpi());
        registry.registerServiceProvider(new TIFFImageReaderSpi());
        registry.registerServiceProvider(new RawImageWriterSpi());
        registry.registerServiceProvider(new RawImageReaderSpi());
        registry.registerServiceProvider(new BMPImageWriterSpi());
        registry.registerServiceProvider(new BMPImageReaderSpi());
//        registry.registerServiceProvider(new GIFImageWriterSpi());
//        registry.registerServiceProvider(new PCXImageWriterSpi());
//        registry.registerServiceProvider(new PNMImageWriterSpi());
//        registry.registerServiceProvider(new WBMPImageWriterSpi());

//        String readFormats[] = ImageIO.getReaderFormatNames();
//        String writeFormats[] = ImageIO.getWriterFormatNames();
//        System.out.println("Readers:" + Arrays.asList(readFormats));
//        System.out.println("Writers:" + Arrays.asList(writeFormats));
//Readers:[JPG, jpg, JPEG 2000, JPEG2000, tiff, bmp, BMP, gif, GIF, WBMP, png, PNG, raw, RAW, JPEG, tif, TIF, TIFF, jpeg2000, wbmp, jpeg, jpeg 2000]
//Writers:[JPEG 2000, JPG, jpg, JPEG2000, tiff, bmp, BMP, gif, GIF, WBMP, png, PNG, raw, RAW, JPEG, tif, TIF, TIFF, jpeg2000, wbmp, jpeg, jpeg 2000]
    }

    public static String[] getCompressionTypes(String imageFormat, ImageType imageColor) {
        if (imageFormat == null || imageColor == null) {
            return null;
        }
        String[] compressionTypes = null;
        switch (imageFormat) {
            case "jpg":
                compressionTypes = getJpegCompressionTypes();
                break;
            case "tif":   // Summarized based on API of class "com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam" and debugging
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
                        String[] types = {"ZLib", "Deflate", "LZW", "JPEG", "PackBits"};
                        return types;
                    }
                    case ARGB: {
                        String[] types = {"ZLib", "Deflate", "LZW", "PackBits"};
                        return types;
                    }
                    default:
                        break;
                }
//                compressionTypes = ImageTools.getTiffCompressionTypes();
                break;
            case "bmp":  // Summarized based on API of class "com.github.jaiimageio.plugins.bmp.BMPImageWriteParam" and debugging
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

    public static boolean canImageCompressed(String imageFormat) {
        return "jpg".equals(imageFormat) || "tif".equals(imageFormat) || "bmp".equals(imageFormat);
    }

}
