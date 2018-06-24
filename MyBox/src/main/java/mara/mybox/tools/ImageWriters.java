package mara.mybox.tools;

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
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.objects.ImageAttributes;
import static mara.mybox.tools.ImageBmpTools.writeBmpImageFile;
import static mara.mybox.tools.ImageJpegTools.writeJPEGImageFile;
import static mara.mybox.tools.ImagePngTools.writePNGImageFile;
import static mara.mybox.tools.ImageRawTools.writeRawImageFile;
import static mara.mybox.tools.ImageTiffTools.writeTiffImageFile;

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
public class ImageWriters {

    private static final Logger logger = LogManager.getLogger();

    public static void writeImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        if (image == null || attributes == null || outFile == null) {
            return;
        }
        String format = attributes.getImageFormat().toLowerCase();
        try {
            switch (format) {
                case "png":
                    writePNGImageFile(image, attributes, outFile);
//                    displayMetadata(outFile);
                    break;
                case "tif":
                    writeTiffImageFile(image, attributes, outFile);
//                    displayTiffMetadata(outFile);
//                    displayMetadata(outFile);
                    break;
                case "raw":
                    writeRawImageFile(image, attributes, outFile);
                    break;
                case "jpg":
                    writeJPEGImageFile(image, attributes, outFile);
//                    displayMetadata(outFile);
                    break;
                case "bmp":
                    writeBmpImageFile(image, attributes, outFile);
//                    displayBmpMetadata(outFile);
//                    displayMetadata(outFile);
                    break;
                default:
                    writeCommonImageFile(image, attributes, outFile);
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
            ImageAttributes attributes, String outFile) {
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName(attributes.getImageFormat().toLowerCase()).next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed() && attributes.getCompressionType() != null) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionType(attributes.getCompressionType());
                param.setCompressionQuality(attributes.getQuality() / 100.0f);
            }

            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (attributes.getDensity() > 0) {
                try {
//                float pixelSizeMm = ImageTools.dpi2pixelSizeMm(attributes.getDensity());
                    float pixelSizeMm = attributes.getDensity() / 25.4f;  // This may be a bug of ImageIO!
                    String format = "javax_imageio_1.0"; // "javax_imageio_png_1.0"
                    IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(format);
                    IIOMetadataNode Dimension = new IIOMetadataNode("Dimension");
                    IIOMetadataNode HorizontalPixelSize = new IIOMetadataNode("HorizontalPixelSize");
                    HorizontalPixelSize.setAttribute("value", pixelSizeMm + "");
                    Dimension.appendChild(HorizontalPixelSize);
                    IIOMetadataNode VerticalPixelSize = new IIOMetadataNode("VerticalPixelSize");
                    VerticalPixelSize.setAttribute("value", pixelSizeMm + "");
                    Dimension.appendChild(VerticalPixelSize);
                    tree.appendChild(Dimension);
                    metaData.mergeTree(format, tree);
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
