package mara.mybox.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import static mara.mybox.value.CommonValues.AlphaColor;
import static mara.mybox.value.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-11-10 20:11:57
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTransform {

    

    public static BufferedImage rotateImage(BufferedImage source, int angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle = 360 + angle;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth, newHeight;
        boolean isSkew = false;
        switch (angle) {
            case 180:
            case 0:
            case 360:
                newWidth = width;
                newHeight = height;
                break;
            case 90:
            case 270:
                if (width > height) {
                    newWidth = width;
                    newHeight = width;
                } else {
                    newWidth = height;
                    newHeight = height;
                }
                break;
            default:
                if (width > height) {
                    newWidth = 2 * width;
                    newHeight = 2 * width;
                } else {
                    newWidth = 2 * height;
                    newHeight = 2 * height;
                }
                isSkew = true;
                break;
        }
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D g = target.createGraphics();
        Color bgColor = AlphaColor;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setBackground(bgColor);
        if (!isSkew) {
            g.rotate(Math.toRadians(angle), newWidth / 2, newHeight / 2);
            g.drawImage(source, 0, 0, null);
        } else {
            g.rotate(Math.toRadians(angle), width, height);
            g.drawImage(source, width / 2, height / 2, null);
        }
        g.dispose();
//        logger.debug("Rotated: " + newWidth + ", " + newHeight);

        target = ImageMargins.cutMargins(target, bgColor, true, true, true, true);
        return target;
    }

    public static BufferedImage horizontalMirrorImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                int l = 0, r = width - 1;
                while (l < r) {
                    int pl = source.getRGB(l, j);
                    int pr = source.getRGB(r, j);
                    target.setRGB(l, j, pr);
                    target.setRGB(r, j, pl);
                    l++;
                    r--;
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage verticalMirrorImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int i = 0; i < width; i++) {
                int t = 0, b = height - 1;
                while (t < b) {
                    int pt = source.getRGB(i, t);
                    int pb = source.getRGB(i, b);
                    target.setRGB(i, t, pb);
                    target.setRGB(i, b, pt);
                    t++;
                    b--;
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage shearImage(BufferedImage source, float shearX, float shearY) {
        try {
            int scale = Math.round(Math.abs(shearX));
            if (scale <= 1) {
                scale = 2;
            }
            scale = scale * scale;
//            if (scale > 64) {
//                scale = 64;
//            }
            int width = source.getWidth() * scale;
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            Color bgColor = AlphaColor;
            g.setBackground(bgColor);
            if (shearX < 0) {
                g.translate(width / 2, 0);
            }
            g.shear(shearX, shearY);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            target = ImageMargins.cutMargins(target, bgColor, true, true, true, true);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
