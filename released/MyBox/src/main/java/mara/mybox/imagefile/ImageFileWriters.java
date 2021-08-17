package mara.mybox.imagefile;

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
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.FileExtensions;
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

    public static boolean writeImageFile(BufferedImage image, File targetFile) {
        try {
            return writeImageFile(image, targetFile.getAbsolutePath());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean writeImageFile(BufferedImage image, String targetFile) {
        try {
            return writeImageFile(image, FileNameTools.getFileSuffix(targetFile), targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean writeImageFile(BufferedImage image, String format, String targetFile) {
        if (image == null || targetFile == null) {
            return false;
        }
        try {
            if (format == null || !FileExtensions.SupportedImages.contains(format)) {
                format = FileNameTools.getFileSuffix(targetFile);
            }
            format = format.toLowerCase();
            ImageAttributes attributes = new ImageAttributes(image, format);
            switch (format) {
                case "jpx":
                case "jpeg2000":
                case "jpeg 2000":
                case "jp2":
                case "jpm":
                    return ImageIO.write(image, "JPEG2000", new File(targetFile));
                case "wbmp":
                    image = ImageBinary.byteBinary(image);
                    break;
            }
            return writeImageFile(image, attributes, targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static ImageAttributes attributes(BufferedImage image, String format) {
        try {
            if (format == null || !FileExtensions.SupportedImages.contains(format)) {
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

    // Not convert color space
    public static boolean writeImageFile(BufferedImage image, ImageAttributes attributes, String targetFile) {
        if (image == null || targetFile == null) {
            return false;
        }
        if (attributes == null) {
            return writeImageFile(image, targetFile);
        }
        try {
            String targetFormat = attributes.getImageFormat().toLowerCase();
            if ("ico".equals(targetFormat) || "icon".equals(targetFormat)) {
                return writeIcon(image, attributes.getWidth(), new File(targetFile));
            }
            BufferedImage targetImage = AlphaTools.checkAlpha(image, targetFormat);
            ImageWriter writer = getWriter(targetFormat);
            ImageWriteParam param = getWriterParam(attributes, writer);
            IIOMetadata metaData = ImageFileWriters.getWriterMetaData(targetFormat, attributes, targetImage, writer, param);
            File tmpFile = TmpFileTools.getTempFile();
            try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(targetImage, null, metaData), param);
                out.flush();
                writer.dispose();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return false;
            }
            File file = new File(targetFile);
            return FileTools.rename(tmpFile, file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean writeIcon(BufferedImage image, int width, File targetFile) {
        try {
            if (image == null || targetFile == null) {
                return false;
            }
            int targetWidth = width;
            if (targetWidth <= 0) {
                targetWidth = Math.min(512, image.getWidth());
            }
            BufferedImage scaled = ScaleTools.scaleImageWidthKeep(image, targetWidth);
            ICOEncoder.write(scaled, targetFile);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static String writeFrame(File sourcefile, int frameIndex, BufferedImage frameImage) {
        return writeFrame(sourcefile, frameIndex, frameImage, sourcefile, null);
    }

    // Convert color space if inAttributes is not null
    public static String writeFrame(File sourcefile, int frameIndex, BufferedImage frameImage,
            File targetFile, ImageAttributes inAttributes) {
        try {
            if (frameImage == null || sourcefile == null || !sourcefile.exists()
                    || targetFile == null || frameIndex < 0) {
                return "InvalidParemeters";
            }
            String targetFormat;
            ImageAttributes targetAttributes = inAttributes;
            if (targetAttributes == null) {
                targetFormat = FileNameTools.getFileSuffix(targetFile.getName()).toLowerCase();
                targetAttributes = attributes(frameImage, targetFormat);
            } else {
                targetFormat = inAttributes.getImageFormat().toLowerCase();
            }
            if (!FileExtensions.MultiFramesImages.contains(targetFormat)) {
                return writeImageFile(frameImage, targetAttributes, targetFile.getAbsolutePath()) ? null : "Failed";
            }
            List<ImageInformation> gifInfos = null;
            if ("gif".equals(targetFormat)) {
                ImageFileInformation imageFileInformation = ImageFileReaders.readImageFileMetaData(sourcefile);
                if (imageFileInformation == null || imageFileInformation.getImagesInformation() == null) {
                    return "InvalidData";
                }
                gifInfos = imageFileInformation.getImagesInformation();
            }
            File tmpFile = TmpFileTools.getTempFile();
            try ( ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(sourcefile)))) {
                ImageReader reader = ImageFileReaders.getReader(iis, FileNameTools.getFileSuffix(sourcefile));
                if (reader == null) {
                    return "InvalidData";
                }
                reader.setInput(iis, false);
                int size = reader.getNumImages(true);
                try ( ImageOutputStream out = ImageIO.createImageOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)))) {
                    int readIndex = 0, duration;
                    ImageWriter writer = getWriter(targetFormat);
                    if (writer == null) {
                        return "InvalidData";
                    }
                    writer.setOutput(out);
                    ImageWriteParam param = getWriterParam(targetAttributes, writer);
                    writer.prepareWriteSequence(null);
                    while (readIndex < size) {
                        BufferedImage bufferedImage;
                        if (readIndex == frameIndex) {
                            bufferedImage = frameImage;
                        } else {
                            try {
                                bufferedImage = reader.read(readIndex);
                            } catch (Exception e) {
                                if (e.toString().contains("java.lang.IndexOutOfBoundsException")) {
                                    break;
                                }
                                bufferedImage = ImageFileReaders.readBrokenImage(e, sourcefile.getAbsolutePath(), readIndex, null, -1);
                            }
                        }
                        if (bufferedImage == null) {
                            break;
                        }
                        BufferedImage targetFrame = bufferedImage;
                        IIOMetadata metaData;
                        if (inAttributes != null) {
                            targetFrame = ImageConvertTools.convertColorSpace(targetFrame, inAttributes);
                        } else {
                            targetFrame = AlphaTools.checkAlpha(bufferedImage, targetFormat);
                        }
                        metaData = getWriterMetaData(targetFormat, targetAttributes, targetFrame, writer, param);
                        if (gifInfos != null) {
                            duration = 500;
                            try {
                                Object d = gifInfos.get(readIndex).getNativeAttribute("delayTime");
                                if (d != null) {
                                    duration = Integer.valueOf((String) d) * 10;
                                }
                            } catch (Exception e) {
                            }
                            GIFImageMetadata gifMetaData = (GIFImageMetadata) metaData;
                            ImageGifFile.getParaMeta(duration, true, param, gifMetaData);
                        }
                        writer.writeToSequence(new IIOImage(targetFrame, null, metaData), param);
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
