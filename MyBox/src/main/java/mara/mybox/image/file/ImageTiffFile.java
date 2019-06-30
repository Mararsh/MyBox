package mara.mybox.image.file;

import mara.mybox.image.ImageValue;
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
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.fxml.FxmlImageManufacture;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTiffFile {

    public static String[] getTiffCompressionTypes() {
        return new TIFFImageWriteParam(null).getCompressionTypes();
    }

    public static TIFFImageMetadata getTiffIIOMetadata(File file) {
        try {
            TIFFImageReader reader = new TIFFImageReader(new TIFFImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                TIFFImageMetadata metadata = (TIFFImageMetadata) reader.getImageMetadata(0);
                reader.dispose();
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

    public static void explainTiffMetaData(String metaDataXml, ImageInformation info) {
        try {
            Map<String, String> metaData = explainTiffMetaData(metaDataXml);
            if (metaData.containsKey("ImageWidth")) {
                info.setWidth(Integer.valueOf(metaData.get("ImageWidth")));
            }
            if (metaData.containsKey("ImageLength")) {
                info.setHeight(Integer.valueOf(metaData.get("ImageLength")));
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
                    info.setXDpi(ImageValue.dpcm2dpi(x));
                    info.setYDpi(ImageValue.dpcm2dpi(y));
                } else {
                    info.setXDpi(x);
                    info.setYDpi(y);
                }
            }
        } catch (Exception e) {

        }
    }

    public static TIFFImageWriter getWriter() {
        TIFFImageWriterSpi tiffspi = new TIFFImageWriterSpi();
        TIFFImageWriter writer = new TIFFImageWriter(tiffspi);
        return writer;
    }

    public static TIFFImageWriteParam getPara(ImageAttributes attributes, TIFFImageWriter writer) {
        try {
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
            return param;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static TIFFImageMetadata getMeta(ImageAttributes attributes, BufferedImage image,
            TIFFImageWriter writer, TIFFImageWriteParam param) {
        try {
            TIFFImageMetadata metaData;
            if (attributes != null && attributes.getColorSpace() != null) {
                ImageTypeSpecifier imageTypeSpecifier;
                switch (attributes.getColorSpace()) {
                    case ARGB:
                        imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
                        break;
                    case RGB:
                        imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
                        break;
                    case BINARY:
                        imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_BINARY);
                        break;
                    case GRAY:
                        imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_GRAY);
                        break;
                    default:
                        imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
                        break;
                }
                metaData = (TIFFImageMetadata) writer.getDefaultImageMetadata(imageTypeSpecifier, param);
            } else if (image != null) {
                metaData = (TIFFImageMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            } else {
                metaData = (TIFFImageMetadata) writer.getDefaultImageMetadata(
                        ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB), param);
            }

            if (!metaData.isReadOnly() && attributes != null && attributes.getDensity() > 0) {
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

            return metaData;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/tiff_metadata.html#image
    public static boolean writeTiffImage(BufferedImage image, ImageAttributes attributes, File file) {
        try {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }

            TIFFImageWriter writer = getWriter();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                TIFFImageWriteParam param = getPara(attributes, writer);
                TIFFImageMetadata metaData = getMeta(attributes, image, writer, param);
                writer.write(null, new IIOImage(image, null, metaData), param);
                out.flush();
                writer.dispose();
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean writeTiffImages(List<BufferedImage> images,
            ImageAttributes attributes, File file) {
        try {
            if (images == null || file == null || images.isEmpty()) {
                return false;
            }
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }
            TIFFImageWriter writer = getWriter();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                TIFFImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                for (BufferedImage bufferedImage : images) {
                    bufferedImage = ImageFileWriters.convertColor(bufferedImage, attributes);
                    TIFFImageMetadata metaData = getMeta(attributes, bufferedImage, writer, param);
                    writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                }
                writer.endWriteSequence();
                out.flush();
            }
            writer.dispose();
            return true;

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static String writeTiffImagesWithInfo(List<ImageInformation> imagesInfo,
            ImageAttributes attributes, File file) {
        try {
            if (imagesInfo == null || imagesInfo.isEmpty() || file == null) {
                return "InvalidParameters";
            }
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return e.toString();
            }
            TIFFImageWriter writer = getWriter();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                TIFFImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                for (ImageInformation info : imagesInfo) {
                    BufferedImage bufferedImage = ImageFileReaders.getBufferedImage(info);
                    if (bufferedImage != null) {
                        bufferedImage = ImageFileWriters.convertColor(bufferedImage, attributes);
                        TIFFImageMetadata metaData = getMeta(attributes, bufferedImage, writer, param);
                        writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                writer.endWriteSequence();
                out.flush();
            }
            writer.dispose();
            return "";

        } catch (Exception e) {
            logger.error(e.toString());
            return e.toString();
        }
    }

    public static boolean writeTiffFiles(List<String> files,
            ImageAttributes attributes, boolean FrameByFrame, File outFile) {
        try {
            if (files == null || outFile == null || files.isEmpty()) {
                return false;
            }
            try {
                if (outFile.exists()) {
                    outFile.delete();
                }
            } catch (Exception e) {
                return false;
            }

            TIFFImageWriter writer = getWriter();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(outFile)) {
                writer.setOutput(out);
                TIFFImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                for (String file : files) {
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(file);
                    if (finfo == null) {
                        continue;
                    }
                    String format = finfo.getImageFormat();
                    if (FrameByFrame) {
                        for (int i = 0; i < finfo.getNumberOfImages(); i++) {
                            BufferedImage bufferedImage = ImageFileReaders.readFrame(format, file, i);
                            if (bufferedImage == null) {
                                continue;
                            }
                            bufferedImage = ImageFileWriters.convertColor(bufferedImage, attributes);
                            TIFFImageMetadata metaData = getMeta(attributes, bufferedImage, writer, param);
                            writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                        }
                    } else {
                        List<BufferedImage> images = ImageFileReaders.readFrames(format, file);
                        if (images == null) {
                            continue;
                        }
                        for (BufferedImage bufferedImage : images) {
                            bufferedImage = ImageFileWriters.convertColor(bufferedImage, attributes);
                            TIFFImageMetadata metaData = getMeta(attributes, bufferedImage, writer, param);
                            writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                        }
                    }
                }
                writer.endWriteSequence();
                out.flush();
                writer.dispose();
            }
            return true;

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean writeSplitImages(String sourceFormat, String sourceFile,
            ImageInformation imageInformation, List<Integer> rows, List<Integer> cols,
            ImageAttributes attributes, File targetFile) {
        try {
            if (sourceFormat == null || sourceFile == null || imageInformation == null
                    || rows == null || rows.isEmpty()
                    || cols == null || cols.isEmpty() || targetFile == null) {
                return false;
            }
            try {
                if (targetFile.exists()) {
                    targetFile.delete();
                }
            } catch (Exception e) {
                return false;
            }
            TIFFImageWriter writer = getWriter();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(targetFile)) {
                writer.setOutput(out);
                TIFFImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                int x1, y1, x2, y2;
                BufferedImage wholeSource = null;
                if (!imageInformation.isIsSampled()) {
                    wholeSource = FxmlImageManufacture.getBufferedImage(imageInformation.getImage());
                }
                for (int i = 0; i < rows.size() - 1; i++) {
                    y1 = rows.get(i);
                    y2 = rows.get(i + 1);
                    for (int j = 0; j < cols.size() - 1; j++) {
                        x1 = cols.get(j);
                        x2 = cols.get(j + 1);
                        BufferedImage bufferedImage;
                        if (!imageInformation.isIsSampled()) {
                            bufferedImage = ImageManufacture.cropOutside(wholeSource, x1, y1, x2, y2);
                        } else {
                            bufferedImage = ImageFileReaders.readRectangle(sourceFormat, sourceFile, x1, y1, x2, y2);
                        }
                        bufferedImage = ImageFileWriters.convertColor(bufferedImage, attributes);
                        TIFFImageMetadata metaData = getMeta(attributes, bufferedImage, writer, param);
                        writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                writer.endWriteSequence();
                out.flush();
            }
            writer.dispose();
            return true;

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

}
