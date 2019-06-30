package mara.mybox.image;

import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriterSpi;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageReaderSpi;
import com.github.jaiimageio.impl.plugins.gif.GIFImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageWriterSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageReaderSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriterSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.impl.plugins.wbmp.WBMPImageReaderSpi;
import com.github.jaiimageio.impl.plugins.wbmp.WBMPImageWriterSpi;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import static mara.mybox.image.file.ImageJpgFile.getJpegCompressionTypes;

import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageValue {

    // dpi(dot per inch) convert to dpm(dot per millimeter)
    public static int dpi2dpmm(int dpi) {
        return Math.round(dpi / 25.4f);
    }

    // dpi(dot per inch) convert to  ppm(dot per meter)
    public static int dpi2dpm(int dpi) {
        return Math.round(1000 * dpi / 25.4f);
    }

    // ppm(dot Per Meter)  convert to  dpi(dot per inch)
    public static int dpm2dpi(int dpm) {
        return Math.round(dpm * 25.4f / 1000f);
    }

    // dpi(dot per inch) convert to dpm(dot per centimeter)
    public static int dpi2dpcm(int dpi) {
        return Math.round(dpi / 2.54f);
    }

    // dpm(dot per centimeter) convert to  dpi(dot per inch)
    public static int dpcm2dpi(int dpcm) {
        return Math.round(dpcm * 2.54f);
    }

    public static int inch2cm(float inch) {
        return Math.round(inch / 2.54f);
    }

    // "pixel size in millimeters" convert to  dpi(dot per inch)
    public static int pixelSizeMm2dpi(float psmm) { //
        if (psmm == 0) {
            return 0;
        }
        return Math.round(25.4f / psmm);
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
        registry.registerServiceProvider(new GIFImageWriterSpi());
        registry.registerServiceProvider(new PCXImageWriterSpi());
        registry.registerServiceProvider(new PCXImageReaderSpi());
        registry.registerServiceProvider(new PNMImageWriterSpi());
        registry.registerServiceProvider(new PNMImageReaderSpi());
        registry.registerServiceProvider(new WBMPImageWriterSpi());
        registry.registerServiceProvider(new WBMPImageReaderSpi());
        try {
            registry.registerServiceProvider(new GIFImageReaderSpi());
        } catch (Exception e) {
        }

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
            case "gif":
                String[] giftypes = {"LZW"};
                return giftypes;
            case "tif":   // Summarized based on API of class "com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam" and debugging
                switch (imageColor) {
                    case BINARY: {
                        String[] types = {"CCITT T.6", "CCITT RLE", "CCITT T.4", "ZLib", "Deflate", "LZW", "PackBits"};
                        return types;
                    }
                    case GRAY: {
                        String[] types = {"LZW", "ZLib", "Deflate", "JPEG", "PackBits"};
                        return types;
                    }
                    case RGB: {
                        String[] types = {"LZW", "ZLib", "Deflate", "JPEG", "PackBits"};
                        return types;
                    }
                    case ARGB: {
                        String[] types = {"LZW", "ZLib", "Deflate", "PackBits"};
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
//                        compressionTypes = getCompressionTypes(imageFormat);
                }
                break;
            default:
                compressionTypes = getCompressionTypes(imageFormat);
        }
        return compressionTypes;
    }

    public static String[] getCompressionTypes(String imageFormat) {
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName(imageFormat).next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            return param.getCompressionTypes();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean canImageCompressed(String imageFormat) {
        return getCompressionTypes(imageFormat) != null;
    }

}
