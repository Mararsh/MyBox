package mara.mybox.imagefile;

import mara.mybox.image.ImageValueTools;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageMetadata;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.BaselineTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.TIFFField;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import com.github.jaiimageio.plugins.tiff.TIFFTag;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.ImageFileInformation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTiffFile {

    private static final Logger logger = LogManager.getLogger();

    public static String[] getTiffCompressionTypes() {
        return new TIFFImageWriteParam(null).getCompressionTypes();
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/tiff_metadata.html#image
    public static boolean writeTiffImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }
            // tiff is not supported by standard classes, so classes in plugins are used.
            TIFFImageWriterSpi tiffspi = new TIFFImageWriterSpi();
            TIFFImageWriter writer = new TIFFImageWriter(tiffspi);
            TIFFImageWriteParam param = (TIFFImageWriteParam) writer.getDefaultWriteParam();
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

            TIFFImageMetadata metaData;
            try {
                metaData = (TIFFImageMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                if (metaData != null && !metaData.isReadOnly() && attributes != null && attributes.getDensity() > 0) {
                    long[] xRes = new long[]{attributes.getDensity(), 1};
                    long[] yRes = new long[]{attributes.getDensity(), 1};
                    Node node = metaData.getAsTree(metaData.getNativeMetadataFormatName());
                    TIFFField fieldXRes = new TIFFField(
                            BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION),
                            TIFFTag.TIFF_RATIONAL, 1, new long[][]{xRes});
                    TIFFField fieldYRes = new TIFFField(
                            BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION),
                            TIFFTag.TIFF_RATIONAL, 1, new long[][]{yRes});
                    node.getFirstChild().appendChild(fieldXRes.getAsNativeNode());
                    node.getFirstChild().appendChild(fieldYRes.getAsNativeNode());
                    char[] fieldUnit = new char[]{BaselineTIFFTagSet.RESOLUTION_UNIT_INCH};
                    TIFFField fieldResUnit = new TIFFField(
                            BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT),
                            TIFFTag.TIFF_SHORT, 1, fieldUnit);
                    node.getFirstChild().appendChild(fieldResUnit.getAsNativeNode());
                    metaData.mergeTree(metaData.getNativeMetadataFormatName(), node);
                }
            } catch (Exception e) {
                logger.error(e.toString());
                metaData = null;
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static TIFFImageMetadata getTiffIIOMetadata(File file) {
        try {
            TIFFImageReader reader = new TIFFImageReader(new TIFFImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                TIFFImageMetadata metadata = (TIFFImageMetadata) reader.getImageMetadata(0);
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // Ignore list values, which can be anaylse if need in future.
    public static Map<String, String> explainTiffMetaData(String metaDataXml) {
        try {
            Map<String, String> metaData = new HashMap();
            String[] lines = metaDataXml.split(System.getProperty("line.separator"));
            String name = null, data;
            for (String line : lines) {
                data = checkAttribute(line, " name");
                if (data != null) {
                    name = data;
                    continue;
                }
                data = checkAttribute(line, " description");
                if (data != null) {
                    if (name != null) {
                        metaData.put(name, data);
                    }
                    continue;
                }
                data = checkAttribute(line, " value");
                if (data != null) {
                    if (name != null) {
                        metaData.put(name, data);
                    }
                }
            }
            return metaData;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    private static String checkAttribute(String line, String attribute) {
        if (line == null || line.isEmpty() || attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            String key = attribute + "=\"";
            int pos1 = line.indexOf(key);
            if (pos1 < 0) {
                return null;
            }
            String v = line.substring(pos1 + key.length());
            int pos2 = v.indexOf("\"");
            if (pos2 < 0) {
                return null;
            }
            return v.substring(0, pos2);
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    private static int getRationalValue(String v) {
        int pos = v.indexOf("/");
        String vv = v;
        if (pos > 0) {
            vv = v.substring(0, pos);
        }
        return Integer.valueOf(vv);
    }

    public static void explainTiffMetaData(String metaDataXml, ImageFileInformation info) {
        try {
            Map<String, String> metaData = explainTiffMetaData(metaDataXml);
            if (metaData.containsKey("ImageWidth")) {
                info.setxPixels(Integer.valueOf(metaData.get("ImageWidth")));
            }
            if (metaData.containsKey("ImageLength")) {
                info.setyPixels(Integer.valueOf(metaData.get("ImageLength")));
            }
            if (metaData.containsKey("Compression")) {
                info.setCompressionType(metaData.get("Compression"));
            }
//            if (metaData.containsKey("PhotometricInterpretation")) {
//                info.setColorSpace(metaData.get("PhotometricInterpretation"));
//            }
            if (metaData.containsKey("ResolutionUnit")) {
                int unit = Integer.valueOf(metaData.get("ResolutionUnit"));
                int x = 0, y = 0;
                if (metaData.containsKey("XResolution")) {
                    x = getRationalValue(metaData.get("XResolution"));
                }
                if (metaData.containsKey("YResolution")) {
                    y = getRationalValue(metaData.get("YResolution"));
                }
                if (BaselineTIFFTagSet.RESOLUTION_UNIT_CENTIMETER == unit) {
                    info.setxDensity(ImageValueTools.dpcm2dpi(x));
                    info.setyDensity(ImageValueTools.dpcm2dpi(y));
                } else {
                    info.setxDensity(x);
                    info.setyDensity(y);
                }
            }
        } catch (Exception e) {

        }
    }

}
