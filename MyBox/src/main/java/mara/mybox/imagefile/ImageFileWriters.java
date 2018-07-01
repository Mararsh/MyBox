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
import mara.mybox.objects.ImageAttributes;
import static mara.mybox.imagefile.ImageBmpFile.writeBmpImageFile;
import static mara.mybox.imagefile.ImageJpegFile.writeJPEGImageFile;
import static mara.mybox.imagefile.ImagePngFile.writePNGImageFile;
import static mara.mybox.imagefile.ImageRawFile.writeRawImageFile;
import static mara.mybox.imagefile.ImageTiffFile.writeTiffImageFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static void writeImageFile(BufferedImage image, String format, String outFile) {
        if (image == null || outFile == null || format == null) {
            return;
        }
        ImageAttributes attributes = new ImageAttributes();
        attributes.setImageFormat(format);
        switch (format) {
            case "jpg":
//                attributes.setCompressionType("JPEG");
                break;
            case "gif":
                attributes.setCompressionType("LZW");
                break;
            case "tif":
                attributes.setCompressionType("Deflate");
                break;
            case "bmp":
                attributes.setCompressionType("BI_JPEG");
                break;
        }
        attributes.setQuality(100);
        writeImageFile(image, attributes, outFile);
    }

    public static void writeImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        if (image == null || attributes == null || outFile == null) {
            return;
        }
        File file = new File(outFile);
        try {
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            return;
        }

        String format = attributes.getImageFormat().toLowerCase();
        try {
            switch (format) {
                case "png":
                    writePNGImageFile(image, attributes, file);
//                    displayMetadata(outFile);
                    break;
                case "tif":
                    writeTiffImageFile(image, attributes, file);
//                    displayTiffMetadata(outFile);
//                    displayMetadata(outFile);
                    break;
                case "raw":
                    writeRawImageFile(image, attributes, file);
                    break;
                case "jpg":
                    writeJPEGImageFile(image, attributes, file);
//                    displayMetadata(outFile);
                    break;
                case "bmp":
                    writeBmpImageFile(image, attributes, file);
//                    displayBmpMetadata(outFile);
//                    displayMetadata(outFile);
                    break;
//                case "gif":
//                    writeGifImageFile(image, attributes, outFile);
//                    displayMetadata(outFile);
//                    break;
//                case "pnm":
//                    writePnmImageFile(image, attributes, outFile);
////                    displayMetadata(outFile);
//                    break;

                default:
                    writeCommonImageFile(image, attributes, file);
            }

        } catch (Exception e) {
            logger.error(e.toString());
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
                    writer = new TIFFImageWriter(new TIFFImageWriterSpi());
                    break;
                case "raw":
                    RawImageWriterSpi spi = new RawImageWriterSpi();
                    writer = new RawImageWriter(spi);
                    break;
                case "jpg":
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
    public static void writeCommonImageFile(BufferedImage image,
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
                return;
            }
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

            if (!metaData.isReadOnly() && metaData.isStandardMetadataFormatSupported()
                    && attributes != null && attributes.getDensity() > 0) {
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
                    logger.error(e.toString());
                }
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
//                Element tree = (Element) metaData.getAsTree(format);
//                Element HorizontalPixelSize = (Element) tree.getElementsByTagName("HorizontalPixelSize").item(0);
//                HorizontalPixelSize.setAttribute("value", pixelSizeMm + "");
//                Element VerticalPixelSize = (Element) tree.getElementsByTagName("VerticalPixelSize").item(0);
//                VerticalPixelSize.setAttribute("value", pixelSizeMm + "");

}
