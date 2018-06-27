package mara.mybox.imagefile;

import com.github.jaiimageio.impl.plugins.pnm.PNMImageReader;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageWriter;
import com.github.jaiimageio.impl.plugins.pnm.PNMImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pnm.PNMMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.objects.ImageAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePnmFile {

    private static final Logger logger = LogManager.getLogger();

    public static void writePnmImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        try {
            PNMImageWriter writer = new PNMImageWriter(new PNMImageWriterSpi());
            ImageWriteParam param = writer.getDefaultWriteParam();
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

            PNMMetadata metaData = (PNMMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            if (attributes.getDensity() > 0) {
                float pixelSizeMm = 25.4f / attributes.getDensity();
                String format = IIOMetadataFormatImpl.standardMetadataFormatName; // "javax_imageio_1.0"
                Element tree = (Element) metaData.getAsTree(format);
                Element HorizontalPixelSize = (Element) tree.getElementsByTagName("HorizontalPixelSize").item(0);
                HorizontalPixelSize.setAttribute("value", pixelSizeMm + "");
                Element VerticalPixelSize = (Element) tree.getElementsByTagName("VerticalPixelSize").item(0);
                VerticalPixelSize.setAttribute("value", pixelSizeMm + "");
                metaData.mergeTree(format, tree);
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
                out.flush();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static PNMMetadata getPnmMetadata(File file) {
        try {
            PNMImageReader reader = new PNMImageReader(new PNMImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                PNMMetadata metadata = (PNMMetadata) reader.getImageMetadata(0);
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
