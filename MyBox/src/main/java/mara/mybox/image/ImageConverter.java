package mara.mybox.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageConverter {

    private static final Logger logger = LogManager.getLogger();

    public static BufferedImage toBufferedImage(Image img, int colorSpace) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), colorSpace);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    public static Image toScaledImage(BufferedImage image, int width, int height) {
        try {
            return image.getScaledInstance(width, height, BufferedImage.SCALE_DEFAULT);
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    public static class KeepRatioType {

        public static final int BaseOnWidth = 0;
        public static final int BaseOnHeight = 1;
        public static final int BaseOnLarger = 2;
        public static final int BaseOnSmaller = 3;
        public static final int None = 9;

    }

    public static BufferedImage resizeImage(BufferedImage source,
            int targetW, int targetH) {
        return resizeImage(source, targetW, targetH, false, KeepRatioType.None);
    }

    public static BufferedImage resizeImage(BufferedImage source,
            int targetW, int targetH,
            boolean keepRatio, int keepType) {

        double ratioW = (double) targetW / source.getWidth();
        double ratioH = (double) targetH / source.getHeight();
        if (keepRatio && ratioW != ratioH) {
            switch (keepType) {
                case KeepRatioType.BaseOnWidth:
                    targetH = (int) (ratioW * source.getWidth());
                    break;
                case KeepRatioType.BaseOnHeight:
                    targetW = (int) (ratioH * source.getWidth());
                    break;
                case KeepRatioType.BaseOnLarger:
                    if (ratioW > ratioH) {
                        targetH = (int) (ratioW * source.getWidth());
                    } else {
                        targetW = (int) (ratioH * source.getWidth());
                    }
                    break;
                case KeepRatioType.BaseOnSmaller:
                    if (ratioW < ratioH) {
                        targetH = (int) (ratioW * source.getWidth());
                    } else {
                        targetW = (int) (ratioH * source.getWidth());
                    }
                    break;
            }
        }
        int imageType = source.getType();
        BufferedImage target = new BufferedImage(targetW, targetH, imageType);
        Graphics2D g = target.createGraphics();
        if (imageType == BufferedImage.TYPE_INT_ARGB) {
            g.setBackground(new Color(0, 0, 0, 0));
        } else {
            g.setBackground(Color.WHITE);
        }
//        g.clearRect(0, 0, targetW, targetH);
        g.drawImage(source, 0, 0, targetW, targetH, null);
        g.dispose();
        return target;
    }

    public static BufferedImage addAlpha(BufferedImage src, int alpha) {
        try {
            int width = src.getWidth();
            int height = src.getHeight();
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int rgb = src.getRGB(i, j);
                    Color color = new Color(rgb);
                    Color newcolor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                    target.setRGB(i, j, newcolor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
