package mara.mybox.image.file;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageManufacture;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileWriters {

    public static boolean writeImageFile(BufferedImage image, File outFile) {
        try {
            return writeImageFile(image, outFile.getAbsolutePath());
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean writeImageFile(BufferedImage image, String outFile) {
        try {
            return writeImageFile(image, FileTools.getFileSuffix(outFile), outFile);
        } catch (Exception e) {
            logger.debug(e.toString());
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
//                outFile += ".png";
            }
            format = format.toLowerCase();
            ImageAttributes attributes = new ImageAttributes();
            attributes.setImageFormat(format);
            switch (format) {
                case "jpg":
                case "jpeg":
                    attributes.setCompressionType("JPEG");
                    break;
                case "jpx":
                case "jpeg2000":
                case "jpeg 2000":
                case "jp2":
                case "jpm":
                    return ImageIO.write(image, "JPEG2000", new File(outFile));
//                    attributes.setCompressionType("JPEG2000");
//                    break;
                case "gif":
                    attributes.setCompressionType("LZW");
                    break;
                case "tif":
                case "tiff":
                    if (image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
                        attributes.setCompressionType("CCITT T.6");
                    } else {
                        attributes.setCompressionType("Deflate");
                    }
                    break;
                case "bmp":
                    attributes.setCompressionType("BI_RGB");
                    break;
                case "wbmp":
                    image = ImageBinary.byteBinary(image);
                    break;
            }
            attributes.setQuality(100);
            return writeImageFile(image, attributes, outFile);
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean writeImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        if (image == null || attributes == null || outFile == null) {
            return false;
        }

        try {
            String targetFormat = attributes.getImageFormat().toLowerCase();
            BufferedImage checked = ImageManufacture.checkAlpha(image, targetFormat);
            ImageWriter writer = getWriter(targetFormat);
            ImageWriteParam param = getWriterParam(attributes, writer);
            IIOMetadata metaData = ImageFileWriters.getWriterMetaData(targetFormat, attributes, checked, writer, param);
            File tmpFile = FileTools.getTempFile();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(checked, null, metaData), param);
                out.flush();
            }
            writer.dispose();

            try {
                File file = new File(outFile);
                if (file.exists()) {
                    file.delete();
                }
                tmpFile.renameTo(file);
            } catch (Exception e) {
                logger.debug(e.toString());
                tmpFile.delete();
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
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
                case "jpx":
                case "jpeg2000":
                case "jpeg 2000":
                case "jp2":
                case "jpm":
                    writer = ImageJpeg2000File.getWriter();
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
                default:
                    return ImageIO.getImageWritersByFormatName(targetFormat).next();
            }
            return writer;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageWriteParam getWriterParam(ImageAttributes attributes, ImageWriter writer) {
        try {
            if (attributes != null && ImageJpeg2000File.isJpeg2000(attributes.getImageFormat())) {
                return ImageJpeg2000File.getWriterParam(attributes);
            }
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
            logger.error(e.toString());
            return null;
        }
    }

    public static IIOMetadata getWriterMetaData(String targetFormat,
            ImageAttributes attributes, BufferedImage image, ImageWriter writer, ImageWriteParam param) {
        try {
            IIOMetadata metaData;
            switch (targetFormat) {
                case "png":
                    metaData = ImagePngFile.getWriterMeta(attributes, image, writer, param);
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
            logger.error(e.toString());
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
//                    logger.error(e.toString());
                }
            }

            return metaData;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
