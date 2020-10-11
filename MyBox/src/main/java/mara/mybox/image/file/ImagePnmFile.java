package mara.mybox.image.file;

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
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageAttributes;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePnmFile {

    public static ImageWriter getWriter() {
        PNMImageWriter writer = new PNMImageWriter(new PNMImageWriterSpi());
        return writer;
    }

    public static ImageWriteParam getPara(ImageAttributes attributes, ImageWriter writer) {
        try {
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
            return param;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static IIOMetadata getWriterMeta(ImageAttributes attributes, BufferedImage image,
            ImageWriter writer, ImageWriteParam param) {
        try {
            PNMMetadata metaData = (PNMMetadata) writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            return metaData;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean writePnmImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        try {
            ImageWriter writer = getWriter();
            ImageWriteParam param = getPara(attributes, writer);
            IIOMetadata metaData = getWriterMeta(attributes, image, writer, param);
            File tmpFile = FileTools.getTempFile();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(metaData, new IIOImage(image, null, metaData), param);
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
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static PNMMetadata getPnmMetadata(File file) {
        try {
            PNMImageReader reader = new PNMImageReader(new PNMImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                PNMMetadata metadata = (PNMMetadata) reader.getImageMetadata(0);
                reader.dispose();
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
