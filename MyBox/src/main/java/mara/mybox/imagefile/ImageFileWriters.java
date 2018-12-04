package mara.mybox.imagefile;

import com.github.jaiimageio.plugins.bmp.BMPImageWriteParam;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriter;
import com.github.jaiimageio.impl.plugins.bmp.BMPImageWriterSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriteParam;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriter;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriterSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.objects.ImageAttributes;
import static mara.mybox.imagefile.ImageBmpFile.writeBmpImageFile;
import static mara.mybox.imagefile.ImageJpegFile.writeJPEGImageFile;
import static mara.mybox.imagefile.ImagePngFile.writePNGImageFile;
import static mara.mybox.imagefile.ImageRawFile.writeRawImageFile;
import static mara.mybox.imagefile.ImagePcxFile.writePcxImageFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static mara.mybox.imagefile.ImageTiffFile.writeTiffImage;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 *
 * @Description
 * @License Apache License Version 2.0
 */
// https://docs.oracle.com/javase/10/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
public class ImageFileWriters {

    private static final Logger logger = LogManager.getLogger();

    public static boolean writeImageFile(BufferedImage image, String format, String outFile) {
        if (image == null || outFile == null || format == null) {
            return false;
        }
        try {
            ImageAttributes attributes = new ImageAttributes();
            attributes.setImageFormat(format);
            switch (format.toLowerCase()) {
                case "jpg":
                case "jpeg":
                    attributes.setCompressionType("JPEG");
                    break;
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
        String format;
        File file;
        try {
            format = attributes.getImageFormat().toLowerCase();
            image = ImageConvertTools.checkAlpha(image, format);

            file = new File(outFile);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

        try {
            switch (format) {
                case "png":
                    return writePNGImageFile(image, attributes, file);
//                    displayMetadata(outFile);
                case "tif":
                case "tiff":
                    return writeTiffImage(image, attributes, file);
//                    displayTiffMetadata(outFile);
//                    displayMetadata(outFile);
                case "raw":
                    return writeRawImageFile(image, attributes, file);

                case "jpg":
                case "jpeg":
                    return writeJPEGImageFile(image, attributes, file);
//                    displayMetadata(outFile);
                case "bmp":
                    return writeBmpImageFile(image, attributes, file);
//                    displayBmpMetadata(outFile);
//                    displayMetadata(outFile);
//                case "gif":
//                    writeGifImageFile(image, attributes, outFile);
//                    displayMetadata(outFile);
//                    break;
//                case "pnm":
//                    writePnmImageFile(image, attributes, outFile);
////                    displayMetadata(outFile);
//                    break;
                case "pcx":
                    return writePcxImageFile(image, attributes, file);

                default:
                    return writeCommonImageFile(image, attributes, file);
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static ImageWriter getWriter(String imageFormat) {
        if (imageFormat == null) {
            return null;
        }
        try {
            ImageWriter writer;
            switch (imageFormat.toLowerCase()) {
                case "tif":
                case "tiff":
                    writer = new TIFFImageWriter(new TIFFImageWriterSpi());
                    break;
                case "raw":
                    RawImageWriterSpi spi = new RawImageWriterSpi();
                    writer = new RawImageWriter(spi);
                    break;
                case "jpg":
                case "jpeg":
                    writer = ImageIO.getImageWritersByFormatName("jpg").next();
                    break;
                case "bmp":
                    writer = new BMPImageWriter(new BMPImageWriterSpi());
                    break;
                default:
                    writer = ImageIO.getImageWritersByFormatName(imageFormat).next();
            }
            return writer;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageWriteParam getWriterParam(String imageFormat) {
        if (imageFormat == null) {
            return null;
        }
        try {
            ImageWriteParam writerParam;
            switch (imageFormat.toLowerCase()) {
                case "tif":
                    writerParam = (TIFFImageWriteParam) new TIFFImageWriter(new TIFFImageWriterSpi()).getDefaultWriteParam();
                    break;
                case "raw":
                    writerParam = (RawImageWriteParam) new RawImageWriter(new RawImageWriterSpi()).getDefaultWriteParam();
                    break;
                case "jpg":
                    writerParam = (JPEGImageWriteParam) ImageIO.getImageWritersByFormatName("jpg").next().getDefaultWriteParam();
                    break;
                case "bmp":
                    writerParam = (BMPImageWriteParam) new BMPImageWriter(new BMPImageWriterSpi()).getDefaultWriteParam();
                    break;
                default:
                    writerParam = ImageIO.getImageWritersByFormatName(imageFormat).next().getDefaultWriteParam();
            }
            return writerParam;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // This standard mothod does not write density into meta data of the image.
    // If density data need be written, then use methods defined for different image format but not this method.
    public static boolean writeCommonImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            String imageFormat = attributes.getImageFormat().toLowerCase();
            ImageWriter writer = null;
            ImageWriteParam param = null;
            IIOMetadata metaData = null;
            for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(imageFormat); iw.hasNext();) {
                writer = iw.next();
                param = writer.getDefaultWriteParam();
                metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                if (!metaData.isReadOnly() && metaData.isStandardMetadataFormatSupported()) {
                    break;
                }
            }
            if (writer == null || param == null || metaData == null) {
                return false;
            }
            if (param.canWriteCompressed() && attributes.getCompressionType() != null) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionType(attributes.getCompressionType());
                if (attributes.getQuality() > 0) {
                    param.setCompressionQuality(attributes.getQuality() / 100.0f);
                } else {
                    param.setCompressionQuality(1.0f);
                }
            }

            if (!metaData.isReadOnly() && metaData.isStandardMetadataFormatSupported()
                    && attributes.getDensity() > 0) {
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

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }
//                Element tree = (Element) metaData.getAsTree(format);
//                Element HorizontalPixelSize = (Element) tree.getElementsByTagName("HorizontalPixelSize").item(0);
//                HorizontalPixelSize.setAttribute("value", pixelSizeMm + "");
//                Element VerticalPixelSize = (Element) tree.getElementsByTagName("VerticalPixelSize").item(0);
//                VerticalPixelSize.setAttribute("value", pixelSizeMm + "");

    public static BufferedImage convertColor(BufferedImage bufferedImage, ImageAttributes attributes) {
        try {
            if (bufferedImage == null || attributes == null || attributes.getColorSpace() == null) {
                return bufferedImage;
            }
            int color = bufferedImage.getType();
            if (ImageType.BINARY == attributes.getColorSpace()) {
                if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD
                        && attributes.getThreshold() >= 0) {
                    bufferedImage = ImageGrayTools.color2BinaryWithPercentage(bufferedImage, attributes.getThreshold());

                } else if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                    bufferedImage = ImageGrayTools.color2BinaryByCalculation(bufferedImage);

                } else if (color != BufferedImage.TYPE_BYTE_BINARY) {
                    bufferedImage = ImageGrayTools.color2Binary(bufferedImage);
                }
            } else if (ImageType.GRAY == attributes.getColorSpace() && color != BufferedImage.TYPE_BYTE_GRAY) {
                bufferedImage = ImageGrayTools.color2Gray(bufferedImage);
            } else if (ImageType.RGB == attributes.getColorSpace()) {
                bufferedImage = ImageConvertTools.clearAlpha(bufferedImage);
            }
            return bufferedImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return bufferedImage;
        }
    }

}
