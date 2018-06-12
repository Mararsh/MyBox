package mara.mybox.tools;

import java.util.Arrays;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTools {

    private static final Logger logger = LogManager.getLogger();

    public static void checkImageFormats() {
        String readFormats[] = ImageIO.getReaderFormatNames();
        String writeFormats[] = ImageIO.getWriterFormatNames();
        System.out.println("Readers:" + Arrays.asList(readFormats));
        System.out.println("Writers:" + Arrays.asList(writeFormats));
    }

}
