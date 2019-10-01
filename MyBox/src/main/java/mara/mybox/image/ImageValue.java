package mara.mybox.image;

import com.github.jaiimageio.impl.plugins.pcx.PCXImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageWriterSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageReaderSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriterSpi;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.image.file.ImageFileWriters;
import static mara.mybox.image.file.ImageJpgFile.getJpegCompressionTypes;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageValue {

    public static List<String> RGBColorSpaces = new ArrayList<>() {
        {
            addAll(Arrays.asList(
                    "sRGB", "Linear sRGB", "Apple RGB", "Adobe RGB", "Color Match RGB", "ECI RGB"
            ));
        }
    };

    public static List<String> CMYKColorSpaces = new ArrayList<>() {
        {
            addAll(Arrays.asList(
                    "ECI CMYK", "Adobe CMYK - CoatedFOGRA27", "Adobe CMYK - CoatedFOGRA39",
                    "Adobe CMYK - JapanColor2001Coated", "Adobe CMYK_JapanColor2001Uncoated",
                    "Adobe CMYK - JapanColor2002Newspaper", "Adobe CMYK - JapanWebCoated",
                    "Adobe CMYK - USSheetfedCoated", "Adobe CMYK - USSheetfedUncoated",
                    "Adobe CMYK - USWebCoatedSWOP", "Adobe CMYK - USWebUncoated",
                    "Adobe CMYK - UncoatedFOGRA29", "Adobe CMYK - WebCoatedFOGRA28"
            ));
        }
    };

    public static List<String> OtherColorSpaces = new ArrayList<>() {
        {
            addAll(Arrays.asList(
                    "Gray", "BlackOrWhite"
            ));
        }
    };

    public static void registrySupportedImageFormats() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
//        registry.registerServiceProvider(new TIFFImageWriterSpi());
//        registry.registerServiceProvider(new TIFFImageReaderSpi());
//        registry.registerServiceProvider(new BMPImageWriterSpi());
//        registry.registerServiceProvider(new BMPImageReaderSpi());
//        registry.registerServiceProvider(new GIFImageWriterSpi());
//        registry.registerServiceProvider(new WBMPImageWriterSpi());
//        registry.registerServiceProvider(new WBMPImageReaderSpi());

        registry.registerServiceProvider(new RawImageWriterSpi());
        registry.registerServiceProvider(new RawImageReaderSpi());
        registry.registerServiceProvider(new PCXImageWriterSpi());
        registry.registerServiceProvider(new PCXImageReaderSpi());
        registry.registerServiceProvider(new PNMImageWriterSpi());
        registry.registerServiceProvider(new PNMImageReaderSpi());
        registry.registerServiceProvider(new J2KImageWriterSpi());
        registry.registerServiceProvider(new J2KImageReaderSpi());

//        String readFormats[] = ImageIO.getReaderFormatNames();
//        String writeFormats[] = ImageIO.getWriterFormatNames();
//        logger.info("Readers:" + Arrays.asList(readFormats));
//        logger.info("Writers:" + Arrays.asList(writeFormats));
//Readers:[JPG, JPEG 2000, tiff, bmp, PCX, gif, WBMP, PNG, RAW, JPEG, PNM, tif, TIFF, wbmp, jpeg, jpg, JPEG2000, BMP, pcx, GIF, png, raw, pnm, TIF, jpeg2000, jpeg 2000]
//Writers:[JPEG 2000, JPG, tiff, bmp, PCX, gif, WBMP, PNG, RAW, JPEG, PNM, tif, TIFF, wbmp, jpeg, jpg, JPEG2000, BMP, pcx, GIF, png, raw, pnm, TIF, jpeg2000, jpeg 2000]
    }

    public static String[] getCompressionTypes(String imageFormat, String colorSpace, boolean hasAlpha) {
        if (imageFormat == null || colorSpace == null) {
            return null;
        }
        String[] compressionTypes;
        switch (imageFormat) {
            case "jpg":
                compressionTypes = getJpegCompressionTypes();
                break;
            case "gif":
                compressionTypes = new String[]{"LZW"};
                break;
            case "tif":   // Summarized based on API of class "com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam" and debugging
                if (message("BlackOrWhite").equals(colorSpace)) {
                    compressionTypes = new String[]{"CCITT T.6", "CCITT RLE", "CCITT T.4", "ZLib", "Deflate", "LZW", "PackBits"};
                } else {
                    if (hasAlpha || ImageValue.CMYKColorSpaces.contains(colorSpace)) {
                        compressionTypes = new String[]{"LZW", "ZLib", "Deflate", "PackBits"};
                    } else {
                        compressionTypes = new String[]{"LZW", "ZLib", "Deflate", "JPEG", "PackBits"};
                    }
                }
//                compressionTypes = ImageTools.getTiffCompressionTypes();
                break;
            case "bmp":  // Summarized based on API of class "com.github.jaiimageio.plugins.bmp.BMPImageWriteParam" and debugging
                if (message("Gray").equals(colorSpace)) {
                    compressionTypes = new String[]{"BI_RGB", "BI_RLE8", "BI_BITFIELDS"};
                } else if (message("BlackOrWhite").equals(colorSpace)) {
                    compressionTypes = new String[]{"BI_RGB", "BI_BITFIELDS"};
                } else {
                    compressionTypes = new String[]{"BI_RGB", "BI_BITFIELDS"};
                }
                break;
            default:
                compressionTypes = getCompressionTypes(imageFormat);
        }
        return compressionTypes;
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
                        String[] types = {"BI_RGB", "BI_BITFIELDS"};
                        return types;
                    }
                    case GRAY: {
                        String[] types = {"BI_RGB", "BI_RLE8", "BI_BITFIELDS"};
                        return types;
                    }
                    case RGB: {
                        String[] types = {"BI_RGB", "BI_BITFIELDS"};
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
            ImageWriter writer = ImageFileWriters.getWriter(imageFormat);
            ImageWriteParam param = writer.getDefaultWriteParam();
            return param.getCompressionTypes();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean canImageCompressed(String imageFormat) {
        return getCompressionTypes(imageFormat) != null;
    }

    public static String imageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR:
                return "3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "4BYTE_ABGR_PRE";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "BYTE_BINARY";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "BYTE_GRAY";
            case BufferedImage.TYPE_BYTE_INDEXED:
                return "BYTE_INDEXED";
            case BufferedImage.TYPE_CUSTOM:
                return "CUSTOM";
            case BufferedImage.TYPE_INT_ARGB:
                return "INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR:
                return "INT_BGR";
            case BufferedImage.TYPE_INT_RGB:
                return "INT_RGB";
            case BufferedImage.TYPE_USHORT_555_RGB:
                return "USHORT_555_RGB";
            case BufferedImage.TYPE_USHORT_565_RGB:
                return "USHORT_565_RGB";
        }
        return type + "";
    }

    public static ColorSpace srgbColorSpace() {
        return ICC_ColorSpace.getInstance(ICC_ColorSpace.CS_sRGB);
    }

    public static ColorSpace grayColorSpace() {
        return ICC_ColorSpace.getInstance(ICC_ColorSpace.CS_GRAY);
    }

    public static ColorSpace linearSRGBColorSpace() {
        return ICC_ColorSpace.getInstance(ICC_ColorSpace.CS_LINEAR_RGB);
    }

    public static ColorSpace ciexyzColorSpace() {
        return ICC_ColorSpace.getInstance(ICC_ColorSpace.CS_CIEXYZ);
    }

    public static ColorSpace pyccColorSpace() {
        return ICC_ColorSpace.getInstance(ICC_ColorSpace.CS_PYCC);
    }

    public static ColorSpace appleRGBColorSpace() {
        return new ICC_ColorSpace(appleRGBProfile());
    }

    public static ColorSpace adobeRGBColorSpace() {
        return new ICC_ColorSpace(adobeRGBProfile());
    }

    public static ColorSpace colorMatchRGBColorSpace() {
        return new ICC_ColorSpace(colorMatchRGBProfile());
    }

    public static ColorSpace eciRGBColorSpace() {
        return new ICC_ColorSpace(eciRGBProfile());
    }

    public static ICC_ColorSpace eciCmykColorSpace() {
        try {
            return new ICC_ColorSpace(eciCmykProfile());
        } catch (Exception ex) {
            return null;
        }
    }
//

    public static ICC_ColorSpace internalColorSpace(String profileName) {
        try {
            return new ICC_ColorSpace(internalProfile(profileName));
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static ICC_ColorSpace colorSpace(String profileName) {
        try {
            return new ICC_ColorSpace(iccProfile(profileName));
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static ICC_ColorSpace adobeCmykColorSpace() {
        try {
            return new ICC_ColorSpace(adobeCmykProfile());
        } catch (Exception ex) {
            return null;
        }
    }

    // https://stackoverflow.com/questions/8118712/java-cmyk-to-rgb-with-profile-output-is-too-dark/12132556#12132556
    public static ICC_Profile internalProfileByName(String name) {
        try {
            switch (name) {
                case "sRGB":
                    return sRGBProfile();
                case "Linear sRGB":
                    return linearRGBProfile();
                case "Apple RGB":
                    return appleRGBProfile();
                case "Adobe RGB":
                    return adobeRGBProfile();
                case "Color Match RGB":
                    return colorMatchRGBProfile();
                case "ECI RGB":
                    return eciRGBProfile();
                case "ECI CMYK":
                    return eciCmykProfile();
                case "Adobe CMYK - CoatedFOGRA27":
                    return internalProfile("AdobeCMYK_CoatedFOGRA27.icc");
                case "Adobe CMYK - CoatedFOGRA39":
                    return internalProfile("AdobeCMYK_CoatedFOGRA39.icc");
                case "Adobe CMYK - JapanColor2001Coated":
                    return internalProfile("AdobeCMYK_JapanColor2001Coated.icc");
                case "Adobe CMYK - JapanColor2001Uncoated":
                    return internalProfile("AdobeCMYK_JapanColor2001Uncoated.icc");
                case "Adobe CMYK - JapanColor2002Newspaper":
                    return internalProfile("AdobeCMYK_JapanColor2002Newspaper.icc");
                case "Adobe CMYK - JapanWebCoated":
                    return internalProfile("AdobeCMYK_JapanWebCoated.icc");
                case "Adobe CMYK - USSheetfedCoated":
                    return internalProfile("AdobeCMYK_USSheetfedCoated.icc");
                case "Adobe CMYK - USSheetfedUncoated":
                    return internalProfile("AdobeCMYK_USSheetfedUncoated.icc");
                case "Adobe CMYK - USWebCoatedSWOP":
                    return internalProfile("AdobeCMYK_USWebCoatedSWOP.icc");
                case "Adobe CMYK - USWebUncoated":
                    return internalProfile("AdobeCMYK_USWebUncoated.icc");
                case "Adobe CMYK - UncoatedFOGRA29":
                    return internalProfile("AdobeCMYK_UncoatedFOGRA29.icc");
                case "Adobe CMYK - WebCoatedFOGRA28":
                    return internalProfile("AdobeCMYK_WebCoatedFOGRA28.icc");
                case "Gray":
                    return grayProfile();
                default:
                    if (message("Gray").equals(name)) {
                        return grayProfile();
                    }
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static ICC_Profile iccProfile(String file) {
        try {
            ICC_Profile profile = ICC_Profile.getInstance(file);
            return iccProfile(profile);
        } catch (Exception ex) {
            return null;
        }
    }

    public static ICC_Profile internalProfile(String filename) {
        try {
            File file = FxmlControl.getInternalFile("/data/ICC/" + filename, "ICC", filename);
            ICC_Profile profile = ICC_Profile.getInstance(file.getAbsolutePath());
            return iccProfile(profile);
        } catch (Exception ex) {
            return null;
        }
    }

    public static ICC_Profile iccProfile(ICC_Profile profile) {
        try {
            if (profile.getProfileClass() != ICC_Profile.CLASS_DISPLAY) {
                byte[] profileData = profile.getData(); // Need to clone entire profile, due to a JDK 7 bug
                if (profileData[ICC_Profile.icHdrRenderingIntent] == ICC_Profile.icPerceptual) {
                    intToBigEndian(ICC_Profile.icSigDisplayClass, profileData, ICC_Profile.icHdrDeviceClass); // Header is first
                    profile = ICC_Profile.getInstance(profileData);
                }
            }
            return profile;
        } catch (Exception ex) {
            return null;
        }
    }

    static void intToBigEndian(int value, byte[] array, int index) {
        array[index] = (byte) (value >> 24);
        array[index + 1] = (byte) (value >> 16);
        array[index + 2] = (byte) (value >> 8);
        array[index + 3] = (byte) (value);
    }

    public static ICC_Profile sRGBProfile() {
        return ICC_Profile.getInstance(ICC_ColorSpace.CS_sRGB);
    }

    public static ICC_Profile grayProfile() {
        return ICC_Profile.getInstance(ICC_ColorSpace.CS_GRAY);
    }

    public static ICC_Profile linearRGBProfile() {
        return ICC_Profile.getInstance(ICC_ColorSpace.CS_LINEAR_RGB);
    }

    public static ICC_Profile xyzProfile() {
        return ICC_Profile.getInstance(ICC_ColorSpace.CS_CIEXYZ);
    }

    public static ICC_Profile pyccProfile() {
        return ICC_Profile.getInstance(ICC_ColorSpace.CS_PYCC);
    }

    public static ICC_Profile eciRGBProfile() {
        return internalProfile("ECI_RGB_v2_ICCv4.icc");
    }

    public static ICC_Profile appleRGBProfile() {
        return internalProfile("Adobe_AppleRGB.icc");
    }

    public static ICC_Profile adobeRGBProfile() {
        return internalProfile("AdobeRGB_1998.icc");
    }

    public static ICC_Profile colorMatchRGBProfile() {
        return internalProfile("Adobe_ColorMatchRGB.icc");
    }

    public static ICC_Profile eciCmykProfile() {
        return internalProfile("ECI_CMYK.icc");
    }

    public static ICC_Profile adobeCmykProfile() {
        return internalProfile("AdobeCMYK_UncoatedFOGRA29.icc");
    }

}
