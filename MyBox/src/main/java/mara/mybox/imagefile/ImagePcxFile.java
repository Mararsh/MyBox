package mara.mybox.imagefile;

import com.github.jaiimageio.impl.plugins.pcx.PCXImageReader;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageReaderSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXMetadata;
import java.io.File;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 *
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagePcxFile {

    private static final Logger logger = LogManager.getLogger();

    public static PCXMetadata getPcxMetadata(File file) {
        try {
            PCXImageReader reader = new PCXImageReader(new PCXImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                PCXMetadata metadata = (PCXMetadata) reader.getImageMetadata(0);
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
