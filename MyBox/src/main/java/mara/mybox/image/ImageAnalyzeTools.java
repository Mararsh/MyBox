package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-27 19:21:31
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAnalyzeTools {

    public int[][] calculateHistogram(BufferedImage source) {
        try {
            int[] red = new int[256];
            int[] green = new int[256];
            int[] blue = new int[256];
            int width = source.getWidth();
            int height = source.getHeight();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int color = source.getRGB(i, j);
                    Color c = new Color(color);
                    red[c.getRed()]++;
                    green[c.getGreen()]++;
                    blue[c.getBlue()]++;
                }
            }
            int[][] h = {red, green, blue};
            return h;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
