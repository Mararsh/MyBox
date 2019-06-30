package mara.mybox.image.file;

import com.github.jaiimageio.impl.plugins.pcx.PCXImageReader;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXMetadata;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriter;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriterSpi;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageAttributes;
import static mara.mybox.value.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePcxFile {

    

    public static boolean writePcxImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            logger.debug("writePcxImageFile");
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                return false;
            }

            PCXImageWriter writer = new PCXImageWriter(new PCXImageWriterSpi());

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, null), null);
                out.flush();
            }
            writer.dispose();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static PCXMetadata getPcxMetadata(File file) {
        try {
            PCXImageReader reader = new PCXImageReader(new PCXImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                PCXMetadata metadata = (PCXMetadata) reader.getImageMetadata(0);
                reader.dispose();
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
