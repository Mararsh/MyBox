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
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageAttributes;
import mara.mybox.image.data.ImageBinary;
import mara.mybox.image.data.ImageFileInformation;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.image.tools.AlphaTools;
import mara.mybox.image.tools.ImageConvertTools;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.FileExtensions;
import static mara.mybox.value.Languages.message;
import thridparty.image4j.ICOEncoder;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileWriters {

    public static boolean saveAs(FxTask task, File srcFile, String targetFile) {
        try {
            BufferedImage image = ImageFileReaders.readImage(task, srcFile);
            if (image == null) {
                return false;
            }
            return writeImageFile(task, image, targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean saveAs(FxTask task, File srcFile, File targetFile) {
        try {
            return saveAs(task, srcFile, targetFile.getAbsolutePath());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean writeImageFile(FxTask task, BufferedImage image, File targetFile) {
        try {
            return writeImageFile(task, image, targetFile.getAbsolutePath());
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean writeImageFile(FxTask task, BufferedImage image, String targetFile) {
        try {
            return writeImageFile(task, image, FileNameTools.ext(targetFile), targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean writeImageFile(FxTask task, BufferedImage image,
            String format, String targetFile) {
        if (image == null || targetFile == null) {
            return false;
        }
        try {
            if (format == null || !FileExtensions.SupportedImages.contains(format)) {
                format = FileNameTools.ext(targetFile);
            }
            format = format.toLowerCase();
            ImageAttributes attributes = new ImageAttributes(image, format);
            switch (format) {
                case "jpx":
                case "jpeg2000":
                case "jpeg 2000":
                case "jp2":
                case "jpm":
                    return ImageIO.write(AlphaTools.removeAlpha(task, image), "JPEG2000", new File(targetFile));
                case "wbmp":
                    image = ImageBinary.byteBinary(task, image);
                    break;
            }
            return writeImageFile(task, image, attributes, targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);
            return null;
        }
    }

    // Not convert color space
    public static boolean writeImageFile(FxTask task, BufferedImage srcImage,
            ImageAttributes attributes, String targetFile) {
        if (srcImage == null || targetFile == null) {
            return false;
        }
        if (attributes == null) {
            return writeImageFile(task, srcImage, targetFile);
        }
        try {
            String targetFormat = attributes.getImageFormat().toLowerCase();
            if ("ico".equals(targetFormat) || "icon".equals(targetFormat)) {
                return writeIcon(task, srcImage, attributes.getWidth(), new File(targetFile));
            }
            BufferedImage targetImage = AlphaTools.checkAlpha(task, srcImage, targetFormat);
            if (targetImage == null || (task != null && !task.isWorking())) {
                return false;
            }
            ImageWriter writer = getWriter(targetFormat);
            ImageWriteParam param = getWriterParam(attributes, writer);
            IIOMetadata metaData = ImageFileWriters.getWriterMetaData(targetFormat, attributes, targetImage, writer, param);
            File tmpFile = FileTmpTools.getTempFile();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(targetImage, null, metaData), param);
                out.flush();
                writer.dispose();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return false;
            }
            if (task != null && !task.isWorking()) {
                return false;
            }
            File file = new File(targetFile);
            return FileTools.override(tmpFile, file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean writeIcon(FxTask task, BufferedImage image, int width, File targetFile) {
        try {
            if (image == null || targetFile == null) {
                return false;
            }
            int targetWidth = width;
            if (targetWidth <= 0) {
                targetWidth = Math.min(512, image.getWidth());
            }
            BufferedImage scaled = ScaleTools.scaleImageWidthKeep(image, targetWidth);
            if (task != null && !task.isWorking()) {
                return false;
            }
            ICOEncoder.write(scaled, targetFile);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static String writeFrame(FxTask task, File sourcefile, int frameIndex, BufferedImage frameImage) {
        return writeFrame(task, sourcefile, frameIndex, frameImage, sourcefile, null);
    }

    // Convert color space if inAttributes is not null
    public static String writeFrame(FxTask task, File sourcefile, int frameIndex, BufferedImage frameImage,
            File targetFile, ImageAttributes inAttributes) {
        try {
            if (frameImage == null || sourcefile == null || !sourcefile.exists()
                    || targetFile == null || frameIndex < 0) {
                return "InvalidParameters";
            }
            String targetFormat;
            ImageAttributes targetAttributes = inAttributes;
            if (targetAttributes == null) {
                targetFormat = FileNameTools.ext(targetFile.getName()).toLowerCase();
                targetAttributes = attributes(frameImage, targetFormat);
            } else {
                targetFormat = inAttributes.getImageFormat().toLowerCase();
            }
            if (!FileExtensions.MultiFramesImages.contains(targetFormat)) {
                return writeImageFile(task, frameImage, targetAttributes, targetFile.getAbsolutePath()) ? null : "Failed";
            }
            List<ImageInformation> gifInfos = null;
            if ("gif".equals(targetFormat)) {
                ImageFileInformation imageFileInformation = ImageFileReaders.readImageFileMetaData(task, sourcefile);
                if (task != null && !task.isWorking()) {
                    return message("Cancelled");
                }
                if (imageFileInformation == null || imageFileInformation.getImagesInformation() == null) {
                    return "InvalidData";
                }
                gifInfos = imageFileInformation.getImagesInformation();
            }
            File tmpFile = FileTmpTools.getTempFile();
            try (ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(sourcefile)))) {
                ImageReader reader = ImageFileReaders.getReader(iis, FileNameTools.ext(sourcefile.getName()));
                if (reader == null) {
                    return "InvalidData";
                }
                reader.setInput(iis, false);
                int size = reader.getNumImages(true);
                try (ImageOutputStream out = ImageIO.createImageOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)))) {
                    int readIndex = 0, duration;
                    ImageWriter writer = getWriter(targetFormat);
                    if (writer == null) {
                        return "InvalidData";
                    }
                    writer.setOutput(out);
                    ImageWriteParam param = getWriterParam(targetAttributes, writer);
                    writer.prepareWriteSequence(null);
                    ImageInformation info = new ImageInformation(sourcefile);
                    while (readIndex < size) {
                        if (task != null && !task.isWorking()) {
                            writer.dispose();
                            reader.dispose();
                            return message("Cancelled");
                        }
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
                                bufferedImage = ImageFileReaders.readBrokenImage(task, e, info.setIndex(readIndex));
                            }
                        }
                        if (task != null && !task.isWorking()) {
                            writer.dispose();
                            reader.dispose();
                            return message("Cancelled");
                        }
                        if (bufferedImage == null) {
                            break;
                        }
                        BufferedImage targetFrame = bufferedImage;
                        IIOMetadata metaData;
                        if (inAttributes != null) {
                            targetFrame = ImageConvertTools.convertColorSpace(task, targetFrame, inAttributes);
                        } else {
                            targetFrame = AlphaTools.checkAlpha(task, bufferedImage, targetFormat);
                        }
                        if (task != null && !task.isWorking()) {
                            writer.dispose();
                            reader.dispose();
                            return message("Cancelled");
                        }
                        if (targetFrame == null) {
                            break;
                        }
                        metaData = getWriterMetaData(targetFormat, targetAttributes, targetFrame, writer, param);
                        if (gifInfos != null) {
                            duration = 500;
                            try {
                                Object d = gifInfos.get(readIndex).getNativeAttribute("delayTime");
                                if (d != null) {
                                    duration = Integer.parseInt((String) d) * 10;
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
            if (task != null && !task.isWorking()) {
                return message("Cancelled");
            }
            if (FileTools.override(tmpFile, targetFile)) {
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
