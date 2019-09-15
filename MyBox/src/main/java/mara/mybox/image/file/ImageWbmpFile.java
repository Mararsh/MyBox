package mara.mybox.image.file;

import com.github.jaiimageio.impl.plugins.wbmp.WBMPImageReader;
import com.github.jaiimageio.impl.plugins.wbmp.WBMPImageReaderSpi;
import com.github.jaiimageio.impl.plugins.wbmp.WBMPMetadata;
import java.io.File;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import static mara.mybox.value.AppVariables.logger;


/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageWbmpFile {

    

    public static WBMPMetadata getWbmpMetadata(File file) {
        try {
            WBMPImageReader reader = new WBMPImageReader(new WBMPImageReaderSpi());
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                reader.setInput(iis, false);
                WBMPMetadata metadata = (WBMPMetadata) reader.getImageMetadata(0);
                reader.dispose();
                return metadata;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
