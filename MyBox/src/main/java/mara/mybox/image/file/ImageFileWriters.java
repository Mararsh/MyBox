package mara.mybox.image.file;

import com.github.jaiimageio.impl.plugins.gif.GIFImageMetadata;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import static mara.mybox.image.file.ImageTiffFile.getWriterMeta;
import mara.mybox.tools.FileTools;
import mara.mybox.value.CommonValues;
import net.sf.image4j.codec.ico.ICOEncoder;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileWriters {

    public static boolean saveAs(File srcFile, String targetFile) {
        try {
            BufferedImage image = ImageFileReaders.readImage(srcFile);
            return writeImageFile(image, targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean saveAs(File srcFile, File targetFile) {
        try {
            return saveAs(srcFile, targetFile.getAbsolutePath());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean writeImageFile(BufferedImage image, File outFile) {
        try {
            return writeImageFile(image, outFile.getAbsolutePath());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean writeImageFile(BufferedImage image, String outFile) {
        try {
            return writeImageFile(image, FileTools.getFileSuffix(outFile), outFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean writeImageFile(BufferedImage image, String format, String outFile) {
        if (image == null || outFile == null) {
            return false;
        }
        try {
            if (format == null || !CommonValues.SupportedImages.contains(format)) {
                format = "png";
            }
            format = format.toLowerCase();
            ImageAttributes attributes = attributes(image, format);
            switch (format) {
                case "jpx":
                case "jpeg2000":
                case "jpeg 2000":
                case "jp2":
                case "jpm":
                    return ImageIO.write(image, "JPEG2000", new File(outFile));
                case "wbmp":
                    image = ImageBinary.byteBinary(image);
                    break;
            }
            return writeImageFile(image, attributes, outFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static ImageAttributes attributes(BufferedImage image, String format) {
        try {
            if (format == null || !CommonValues.SupportedImages.contains(format)) {
                format = "png";
            }
            format = format.toLowerCase();
            ImageAttributes attributes = new ImageAttributes();
            attributes.setImageFormat(format);
            switch (format) {
                case "jpg":
                case "jpeg":
                    attributes.setCompressionType("JPEG");
                    break;
                case "gif":
                    attributes.setCompressionType("LZW");
                    break;
                case "tif":
                case "tiff":
                    if (image != null && image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
                        attributes.setCompressionType("CCITT T.6");
                    } else {
                        attributes.setCompressionType("Deflate");
                    }
                    break;
                case "bmp":
                    attributes.setCompressionType("BI_RGB");
                    break;
            }
            attributes.setQuality(100);
            return attributes;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static boolean writeImageFile(BufferedImage image, ImageAttributes attributes, String outFile) {
        if (image == null || attributes == null || outFile == null) {
            return false;
        }
        try {
            String targetFormat = attributes.getImageFormat().toLowerCase();
            if ("ico".equals(targetFormat) || "icon".equals(targetFormat)) {
                return writeIcon(image, attributes, new File(outFile));
            }
            BufferedImage checked = ImageManufacture.checkAlpha(image, targetFormat);
            ImageWriter writer = getWriter(targetFormat);
            ImageWriteParam param = getWriterParam(attributes, writer);
            IIOMetadata metaData = ImageFileWriters.getWriterMetaData(targetFormat, attributes, checked, writer, param);
            File tmpFile = FileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(checked, null, metaData), param);
                out.flush();
                writer.dispose();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return false;
            }
            File file = new File(outFile);
            return FileTools.rename(tmpFile, file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

    }

    public static boolean writeIcon(BufferedImage image, ImageAttributes attributes, File targetFile) {
        try {
            if (image == null || targetFile == null || attributes == null) {
                return false;
            }
            int width = attributes.getWidth();
            if (width <= 0) {
                width = Math.min(512, image.getWidth());
            }
            BufferedImage scaled = ImageManufacture.scaleImageWidthKeep(image, width);
            ICOEncoder.write(scaled, targetFile);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static String writeFrame(File sourcefile, int targetIndex, BufferedImage image) {
        return writeFrame(sourcefile, targetIndex, image, sourcefile, FileTools.getFileSuffix(sourcefile));
    }

    public static String writeFrame(File sourcefile, int targetIndex, BufferedImage image,
            File targetFile, String targetFormat) {
        try {
            if (image == null || sourcefile == null || !sourcefile.exists()
                    || targetFile == null || targetFormat == null || targetIndex < 0) {
                return "InvalidParemeters";
            }
            if (!CommonValues.MultiFramesImages.contains(targetFormat)) {
                return writeImageFile(image, targetFormat, targetFile.getAbsolutePath()) ? null : "Failed";
            }
            String sourceFormat = FileTools.getFileSuffix(sourcefile);
            if (sourceFormat == null || !CommonValues.SupportedImages.contains(sourceFormat)) {
                sourceFormat = "png";
            }
            List<ImageInformation> imagesInfo = null;
            boolean targetGif = "gif".equalsIgnoreCase(targetFormat);
            if (targetGif) {
                ImageFileInformation imageFileInformation = ImageFileReaders.readImageFileMetaData(sourcefile);
                if (imageFileInformation == null || imageFileInformation.getImagesInformation() == null) {
                    return "InvalidData";
                }
                imagesInfo = imageFileInformation.getImagesInformation();
            }
            ImageAttributes attributes = attributes(image, targetFormat);
            ImageReader reader = ImageFileReaders.getReader(sourceFormat);
            File tmpFile = FileTools.getTempFile();
            try ( BufferedInputStream bin = new BufferedInputStream(new FileInputStream(sourcefile));
                     ImageInputStream in = ImageIO.createImageInputStream(bin)) {
                reader.setInput(in, false);
                try ( BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(tmpFile));
                         ImageOutputStream out = ImageIO.createImageOutputStream(bout)) {
                    int readIndex = 0, duration;
                    ImageWriter writer = getWriter(targetFormat);
                    writer.setOutput(out);
                    ImageWriteParam param = getWriterParam(attributes, writer);
                    writer.prepareWriteSequence(null);
                    while (true) {
                        BufferedImage frame;
                        if (readIndex == targetIndex) {
                            frame = image;
                        } else {
                            try {
                                frame = reader.read(readIndex);
                            } catch (Exception e) {
                                frame = ImageFileReaders.readBrokenImage(e, sourcefile, readIndex);
                            }
                        }
                        if (frame == null) {
                            break;
                        }
                        if (targetGif) {
                            duration = 500;
                            try {
                                Object d = imagesInfo.get(readIndex).getNativeAttribute("delayTime");
                                if (d != null) {
                                    duration = Integer.valueOf((String) d) * 10;
                                }
                            } catch (Exception e) {
                            }
                            GIFImageMetadata metaData = (GIFImageMetadata) writer.getDefaultImageMetadata(
                                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);
                            ImageGifFile.getParaMeta(duration, true, param, metaData);
                            writer.writeToSequence(new IIOImage(frame, null, metaData), param);
                        } else {
                            frame = ImageConvert.convertColorType(frame, attributes);
                            IIOMetadata metaData = getWriterMeta(attributes, frame, writer, param);
                            writer.writeToSequence(new IIOImage(frame, null, metaData), param);
                        }
                        readIndex++;
                    }
                    writer.endWriteSequence();
                    out.flush();
                    writer.dispose();
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
                reader.dispose();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return e.toString();
            }
            if (FileTools.rename(tmpFile, targetFile)) {
                return null;
            } else {
                return "Failed";
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return e.toString();
        }
    }

    public static ImageWriter getWriter(String targetFormat) {
        if (targetFormat == null) {
            return null;
        }
        try {
            ImageWriter writer;
            switch (targetFormat.toLowerCase()) {
                case "png":
                    writer = ImagePngFile.getWriter();
                    break;
                case "jpg":
                case "jpeg":
                    writer = ImageJpgFile.getWriter();
                    break;
                case "tif":
                case "tiff":
                    writer = ImageTiffFile.getWriter();
                    break;
                case "raw":
                    writer = ImageRawFile.getWriter();
                    break;
                case "bmp":
                    writer = ImageBmpFile.getWriter();
                    break;
                case "gif":
                    writer = ImageGifFile.getWriter();
                    break;
                default:
                    return ImageIO.getImageWritersByFormatName(targetFormat).next();
            }
            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageWriteParam getWriterParam(ImageAttributes attributes, ImageWriter writer) {
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (attributes != null && attributes.getCompressionType() != null) {
                    param.setCompressionType(attributes.getCompressionType());
                } else {
                    String[] compressionTypes = param.getCompressionTypes();
                    if (compressionTypes != null) {
                        param.setCompressionType(compressionTypes[0]);
                    }
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

    public static IIOMetadata getWriterMetaData(String targetFormat,
            ImageAttributes attributes, BufferedImage image, ImageWriter writer,
            ImageWriteParam param) {
        try {
            IIOMetadata metaData;
            switch (targetFormat) {
                case "png":
                    metaData = ImagePngFile.getWriterMeta(attributes, image, writer, param);
                    break;
                case "gif":
                    metaData = ImageGifFile.getWriterMeta(attributes, image, writer, param);
                    break;
                case "jpg":
                case "jpeg":
                    metaData = ImageJpgFile.getWriterMeta(attributes, image, writer, param);
                    break;
                case "tif":
                case "tiff":
                    metaData = ImageTiffFile.getWriterMeta(attributes, image, writer, param);
                    break;
                case "bmp":
                    metaData = ImageBmpFile.getWriterMeta(attributes, image, writer, param);
                    break;
                default:
                    metaData = getWriterMetaData(attributes, image, writer, param);
            }
            return metaData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static IIOMetadata getWriterMetaData(ImageAttributes attributes,
            BufferedImage image, ImageWriter writer, ImageWriteParam param) {
        try {
            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (metaData == null || metaData.isReadOnly() || attributes == null
                    || !metaData.isStandardMetadataFormatSupported()) {
                return metaData;
            }
            // This standard mothod does not write density into meta data of the image.
            // If density data need be written, then use methods defined for different image format but not this method.
            if (attributes.getDensity() > 0) {
                try {
                    float pixelSizeMm = 25.4f / attributes.getDensity();
                    String metaFormat = IIOMetadataFormatImpl.standardMetadataFormatName; // "javax_imageio_1.0"
                    IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(metaFormat);
                    IIOMetadataNode Dimension = new IIOMetadataNode("Dimension");
                    IIOMetadataNode HorizontalPixelSize = new IIOMetadataNode("HorizontalPixelSize");
                    HorizontalPixelSize.setAttribute("value", pixelSizeMm + "");
                    Dimension.appendChild(HorizontalPixelSize);
                    IIOMetadataNode VerticalPixelSize = new IIOMetadataNode("VerticalPixelSize");
                    VerticalPixelSize.setAttribute("value", pixelSizeMm + "");
                    Dimension.appendChild(VerticalPixelSize);
                    tree.appendChild(Dimension);

                    metaData.mergeTree(metaFormat, tree);
                } catch (Exception e) {
//                    MyBoxLog.error(e.toString());
                }
            }

            return metaData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
