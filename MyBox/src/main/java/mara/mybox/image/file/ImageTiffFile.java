package mara.mybox.image.file;

//import com.github.jaiimageio.impl.plugins.tiff.IIOMetadata;
//import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader;
//import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
//import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
//import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
//import com.github.jaiimageio.plugins.tiff.BaselineTIFFTagSet;
//import com.github.jaiimageio.plugins.tiff.TIFFField;
//import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
//import com.github.jaiimageio.plugins.tiff.TIFFTag;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.tools.FileTools;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/tiff_metadata.html#image
public class ImageTiffFile {

    public static IIOMetadata getTiffIIOMetadata(File file) {
        try {
            IIOMetadata metadata;
            ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
            try ( ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                metadata = reader.getImageMetadata(0);
                reader.dispose();
            }
            return metadata;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageWriter getWriter() {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("tif").next();
        return writer;
    }

    public static ImageWriteParam getPara(ImageAttributes attributes, ImageWriter writer) {
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (attributes != null && attributes.getCompressionType() != null) {
                    param.setCompressionType(attributes.getCompressionType());
                } else {
                    param.setCompressionType("LZW");
                }
                if (attributes != null && attributes.getQuality() > 0) {
                    param.setCompressionQuality(attributes.getQuality() / 100.0f);
                } else {
                    param.setCompressionQuality(1.0f);
                }
            }
            return param;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static IIOMetadata getWriterMeta(ImageAttributes attributes, BufferedImage image,
            ImageWriter writer, ImageWriteParam param) {
        try {
            IIOMetadata metaData;
            if (attributes != null && attributes.getColorType() != null) {
                ImageTypeSpecifier imageTypeSpecifier;
                switch (attributes.getColorType()) {
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
                metaData = writer.getDefaultImageMetadata(imageTypeSpecifier, param);
            } else if (image != null) {
                metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            } else {
                metaData = writer.getDefaultImageMetadata(
                        ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB), param);
            }

            if (metaData == null || metaData.isReadOnly() || attributes == null) {
                return metaData;
            }
            if (attributes.getDensity() > 0) {
                String nativeName = metaData.getNativeMetadataFormatName();  // javax_imageio_tiff_image_1.0
                Node nativeTree = metaData.getAsTree(nativeName);
                long[] xRes = new long[]{attributes.getDensity(), 1};
                long[] yRes = new long[]{attributes.getDensity(), 1};
                TIFFField fieldXRes = new TIFFField(
                        BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION),
                        TIFFTag.TIFF_RATIONAL, 1, new long[][]{xRes});
                TIFFField fieldYRes = new TIFFField(
                        BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION),
                        TIFFTag.TIFF_RATIONAL, 1, new long[][]{yRes});
                nativeTree.getFirstChild().appendChild(fieldXRes.getAsNativeNode());
                nativeTree.getFirstChild().appendChild(fieldYRes.getAsNativeNode());
                char[] fieldUnit = new char[]{BaselineTIFFTagSet.RESOLUTION_UNIT_INCH};
                TIFFField fieldResUnit = new TIFFField(
                        BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT),
                        TIFFTag.TIFF_SHORT, 1, fieldUnit);
                nativeTree.getFirstChild().appendChild(fieldResUnit.getAsNativeNode());
                metaData.mergeTree(nativeName, nativeTree);
            }

            return metaData;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Node findNode(Node nativeTree, String name) {
        try {
            NodeList nodes = nativeTree.getFirstChild().getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
                NamedNodeMap attrs = node.getAttributes();
                for (int j = 0; j < attrs.getLength(); ++j) {
                    Node attr = attrs.item(j);
                    if ("name".equals(attr.getNodeName()) && name.equals(attr.getNodeValue())) {
                        return node;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean writeTiffImage(BufferedImage image, ImageAttributes attributes, File file) {
        try {
            ImageWriter writer = getWriter();
            File tmpFile = FileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                ImageWriteParam param = getPara(attributes, writer);
                IIOMetadata metaData = getWriterMeta(attributes, image, writer, param);
                writer.write(null, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();

            return FileTools.rename(tmpFile, file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean writeTiffImages(List<BufferedImage> images,
            ImageAttributes attributes, File file) {
        try {
            if (images == null || file == null || images.isEmpty()) {
                return false;
            }
            ImageWriter writer = getWriter();
            File tmpFile = FileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                ImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                for (BufferedImage bufferedImage : images) {
                    bufferedImage = ImageConvert.convertColorType(bufferedImage, attributes);
                    IIOMetadata metaData = getWriterMeta(attributes, bufferedImage, writer, param);
                    writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                }
                writer.endWriteSequence();
                out.flush();
            }
            writer.dispose();

            return FileTools.rename(tmpFile, file);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static String writeTiffImagesWithInfo(List<ImageInformation> imagesInfo,
            ImageAttributes attributes, File file) {
        try {
            if (imagesInfo == null || imagesInfo.isEmpty() || file == null) {
                return "InvalidParameters";
            }
            ImageWriter writer = getWriter();
            File tmpFile = FileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                ImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                for (ImageInformation info : imagesInfo) {
                    BufferedImage bufferedImage = ImageInformation.getBufferedImage(info);
                    if (bufferedImage != null) {
                        bufferedImage = ImageConvert.convertColorType(bufferedImage, attributes);
                        IIOMetadata metaData = getWriterMeta(attributes, bufferedImage, writer, param);
                        writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                writer.endWriteSequence();
                out.flush();
            }
            writer.dispose();

            return FileTools.rename(tmpFile, file) ? "" : "Failed";

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return e.toString();
        }
    }

    public static boolean writeTiffFiles(List<String> files,
            ImageAttributes attributes, boolean FrameByFrame, File outFile) {
        try {
            if (files == null || outFile == null || files.isEmpty()) {
                return false;
            }
            ImageWriter writer = getWriter();
            File tmpFile = FileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                ImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                for (String file : files) {
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(file);
                    if (finfo == null) {
                        continue;
                    }
                    String format = finfo.getImageFormat();
                    if (FrameByFrame) {
                        for (int i = 0; i < finfo.getNumberOfImages(); ++i) {
                            BufferedImage bufferedImage = ImageFileReaders.readFrame(format, file, i);
                            if (bufferedImage == null) {
                                continue;
                            }
                            bufferedImage = ImageConvert.convertColorType(bufferedImage, attributes);
                            IIOMetadata metaData = getWriterMeta(attributes, bufferedImage, writer, param);
                            writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                        }
                    } else {
                        List<BufferedImage> images = ImageFileReaders.readFrames(format, file);
                        if (images == null) {
                            continue;
                        }
                        for (BufferedImage bufferedImage : images) {
                            bufferedImage = ImageConvert.convertColorType(bufferedImage, attributes);
                            IIOMetadata metaData = getWriterMeta(attributes, bufferedImage, writer, param);
                            writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                        }
                    }
                }
                writer.endWriteSequence();
                out.flush();
            }
            writer.dispose();

            return FileTools.rename(tmpFile, outFile);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            ImageWriter writer = getWriter();
            File tmpFile = FileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                ImageWriteParam param = getPara(attributes, writer);
                writer.prepareWriteSequence(null);
                int x1, y1, x2, y2;
                BufferedImage wholeSource = null;
                if (imageInformation.getImage() != null && !imageInformation.isIsScaled()) {
                    wholeSource = FxmlImageManufacture.bufferedImage(imageInformation.getImage());
                }
                for (int i = 0; i < rows.size() - 1; ++i) {
                    y1 = rows.get(i);
                    y2 = rows.get(i + 1);
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        x1 = cols.get(j);
                        x2 = cols.get(j + 1);
                        BufferedImage bufferedImage;
                        if (wholeSource != null) {
                            bufferedImage = ImageManufacture.cropOutside(wholeSource, x1, y1, x2, y2);
                        } else {
                            bufferedImage = ImageFileReaders.readFrame(sourceFormat, sourceFile, x1, y1, x2, y2);
                        }
                        bufferedImage = ImageConvert.convertColorType(bufferedImage, attributes);
                        IIOMetadata metaData = getWriterMeta(attributes, bufferedImage, writer, param);
                        writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                    }
                }
                writer.endWriteSequence();
                out.flush();
            }
            writer.dispose();

            return FileTools.rename(tmpFile, targetFile);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    // Ignore list values, which can be anaylse if need in future.
    public static void explainTiffMetaData(IIOMetadata iioMetaData, ImageInformation info) {
        try {
            String nativeName = iioMetaData.getNativeMetadataFormatName();  // javax_imageio_tiff_image_1.0
            Node nativeTree = iioMetaData.getAsTree(nativeName);
            NodeList nodes = nativeTree.getFirstChild().getChildNodes();
            BaselineTIFFTagSet tagsSet = BaselineTIFFTagSet.getInstance();
            int unit = -1, x = -1, y = -1;
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
                TIFFField field = TIFFField.createFromMetadataNode(tagsSet, node);
                String name = field.getTag().getName();
                if ("ICC Profile".equals(name)) {
                    info.setIccProfile(field.getAsBytes());
                    info.setNativeAttribute(name, "skip...");
                } else {
                    String des = null;
                    try {
                        des = node.getFirstChild().getFirstChild().getAttributes().item(1).getNodeValue();
                    } catch (Exception e) {
                    }
                    List<String> values = new ArrayList<>();
                    for (int j = 0; j < field.getCount(); ++j) {
                        values.add(field.getValueAsString(j));
                    }
                    if (values.isEmpty()) {
                        continue;
                    } else if (values.size() == 1) {
                        if (des != null) {
                            info.setNativeAttribute(name, des + "(" + values.get(0) + ")");
                        } else {
                            info.setNativeAttribute(name, values.get(0));
                        }
                    } else {
                        info.setNativeAttribute(name, values);
                    }
                    switch (name) {
                        case "ImageWidth":
                            info.setWidth(Integer.valueOf(values.get(0)));
                            break;
                        case "ImageLength":
                            info.setHeight(Integer.valueOf(values.get(0)));
                            break;
                        case "ResolutionUnit":
                            unit = Integer.valueOf(values.get(0));
                            break;
                        case "XResolution":
                            x = getRationalValue(values.get(0));
                            break;
                        case "YResolution":
                            y = getRationalValue(values.get(0));
                            break;
                    }
                }
            }

            if (x > 0 && y > 0) {
                if (BaselineTIFFTagSet.RESOLUTION_UNIT_CENTIMETER == unit) {
                    info.setXDpi(ImageConvert.dpcm2dpi(x));
                    info.setYDpi(ImageConvert.dpcm2dpi(y));
                } else if (BaselineTIFFTagSet.RESOLUTION_UNIT_INCH == unit) {
                    info.setXDpi(x);
                    info.setYDpi(y);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private static int getRationalValue(String v) {
        int pos = v.indexOf('/');
        String vv = v;
        if (pos > 0) {
            vv = v.substring(0, pos);
        }
        return Integer.valueOf(vv);
    }

}
