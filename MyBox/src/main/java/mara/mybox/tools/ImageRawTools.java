package mara.mybox.tools;

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
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.objects.ImageAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageRawTools {

    private static final Logger logger = LogManager.getLogger();

    public static void writeRawImageFile(BufferedImage image,
            ImageAttributes attributes, String outFile) {
        try {
            RawImageWriterSpi spi = new RawImageWriterSpi();
            RawImageWriter writer = new RawImageWriter(spi);
            RawImageWriteParam param = (RawImageWriteParam) writer.getDefaultWriteParam();

            IIOMetadata metaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(outFile))) {
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, metaData), param);
                out.flush();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static IIOMetadata getBmpIIOMetadata(File file) {
        try {
            RawImageReaderSpi tiffspi = new RawImageReaderSpi();
            RawImageReader reader = new RawImageReader(tiffspi);
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                IIOMetadata metadata = reader.getStreamMetadata();
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
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
