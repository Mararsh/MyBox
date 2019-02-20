package mara.mybox.image.file;

import com.github.jaiimageio.impl.plugins.raw.RawImageReader;
import com.github.jaiimageio.impl.plugins.raw.RawImageReaderSpi;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriteParam;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriter;
import com.github.jaiimageio.impl.plugins.raw.RawImageWriterSpi;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.data.ImageAttributes;
import static mara.mybox.value.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageRawFile {

    

    public static boolean writeRawImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }
            RawImageWriterSpi spi = new RawImageWriterSpi();
            RawImageWriter writer = new RawImageWriter(spi);
            RawImageWriteParam param = (RawImageWriteParam) writer.getDefaultWriteParam();
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

            IIOMetadata metaData;
            try {
                metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            } catch (Exception e) {
                logger.error(e.toString());
                metaData = null;
            }

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, metaData), param);
                out.flush();
            }
            writer.dispose();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static BufferedImage readRawData(File file) {
        try {
            RawImageReaderSpi tiffspi = new RawImageReaderSpi();
            RawImageReader reader = new RawImageReader(tiffspi);

            byte[] rawData;
            try (InputStream fileInput = new FileInputStream(file)) {
                rawData = new byte[fileInput.available()];
                logger.debug(fileInput.available());
                fileInput.read(rawData);
                fileInput.close();
                logger.debug(rawData.length);

                // convert byte array back to BufferedImage
                InputStream in = new ByteArrayInputStream(rawData);
                logger.debug(in.available());

                reader.setInput(in);
                return reader.read(0);
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static File readRawData2(File file) {
        try {
            byte[] rawData;
            try (InputStream fileInput = new FileInputStream(file)) {
                rawData = new byte[fileInput.available()];
                logger.debug(fileInput.available());
                fileInput.read(rawData);
            }

            logger.debug(rawData.length);
            InputStream in = new ByteArrayInputStream(rawData);
            BufferedImage image = ImageIO.read(in);
            in.close();

            File newFile = new File(file.getPath() + ".png");
            ImageIO.write(image, "png", newFile);
            return newFile;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
