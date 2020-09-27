package mara.mybox.image.file;

import com.github.jaiimageio.impl.plugins.pcx.PCXImageReader;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriter;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.ImageAttributes;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePcxFile {

    public static ImageWriter getWriter() {
        PCXImageWriter writer = new PCXImageWriter(new PCXImageWriterSpi());
        return writer;
    }

    public static boolean writePcxImageFile(BufferedImage image,
            ImageAttributes attributes, File file) {
        try {
            ImageWriter writer = getWriter();
            File tmpFile = FileTools.getTempFile();
            try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, null), null);
                out.flush();
            }
            writer.dispose();
            try {
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
