package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class TransformTools {

    public static BufferedImage rotateImage(BufferedImage source, int inAngle) {
        int angle = inAngle % 360;
        if (angle == 0) {
            return source;
        }
        if (angle < 0) {
            angle = 360 + angle;
        }
        double radians = Math.toRadians(angle);
        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = (int) (width * cos + height * sin);
        int newHeight = (int) (height * cos + width * sin);
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D g = target.createGraphics();
        if (AppVariables.imageRenderHints != null) {
            g.addRenderingHints(AppVariables.imageRenderHints);
        }
        Color bgColor = Colors.TRANSPARENT;
        g.setBackground(bgColor);
        g.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g.rotate(radians, width / 2, height / 2);
        g.drawImage(source, null, null);
        g.dispose();
        target = MarginTools.cutMargins(target, bgColor, true, true, true, true);
        return target;
    }

    public static BufferedImage shearImage(BufferedImage source, float shearX, float shearY) {
        try {
            int scale = Math.round(Math.abs(Math.max(shearX, shearY)));
            if (scale <= 1) {
                scale = 2;
            }
            scale = scale * scale;
            int width = source.getWidth() * scale;
            int height = source.getHeight() * scale;
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            Color bgColor = Colors.TRANSPARENT;
            g.setBackground(bgColor);
            if (shearX < 0) {
                g.translate(width / 2, 0);
            }
            g.shear(shearX, shearY);
            g.drawImage(source, 0, 0, null);
            g.dispose();
            target = MarginTools.cutMargins(target, bgColor, true, true, true, true);
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage horizontalMirrorImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; ++j) {
                int l = 0;
                int r = width - 1;
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
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage verticalMirrorImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int i = 0; i < width; ++i) {
                int t = 0;
                int b = height - 1;
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
            MyBoxLog.error(e);
            return null;
        }
    }

}
