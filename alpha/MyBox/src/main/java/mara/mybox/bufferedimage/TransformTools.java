package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class TransformTools {

    public static BufferedImage rotateImage(FxTask task, BufferedImage source, int inAngle) {
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
        int newWidth = (int) (width * cos + height * sin) + 1;
        int newHeight = (int) (height * cos + width * sin) + 1;
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D g = target.createGraphics();
        if (AppVariables.ImageHints != null) {
            g.addRenderingHints(AppVariables.ImageHints);
        }
        Color bgColor = Colors.TRANSPARENT;
        g.setBackground(bgColor);
        g.translate((newWidth - width) / 2, (newHeight - height) / 2);
        if (task != null && !task.isWorking()) {
            return null;
        }
        g.rotate(radians, width / 2, height / 2);
        if (task != null && !task.isWorking()) {
            return null;
        }
        g.drawImage(source, null, null);
        g.dispose();
        target = MarginTools.cutMarginsByColor(task, target, bgColor, true, true, true, true);
        return target;
    }

    public static BufferedImage shearImage(FxTask task, BufferedImage source, float shearX, float shearY) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int newWidth = (int) (width + height * Math.abs(shearX) + 1) * 2;
            int newHeight = (int) (width * Math.abs(shearY) + height + 1) * 2;
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(newWidth, newHeight, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            Color bgColor = Colors.TRANSPARENT;
            g.setBackground(bgColor);
            g.translate(newWidth / 2, newHeight / 2);
            g.shear(shearX, shearY);
            if (task != null && !task.isWorking()) {
                return null;
            }
            g.drawImage(source, null, null);
            if (task != null && !task.isWorking()) {
                return null;
            }
            g.dispose();
            target = MarginTools.cutMarginsByColor(task, target, bgColor, true, true, true, true);
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage horizontalMirrorImage(FxTask task, BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int l = 0;
                int r = width - 1;
                while (l < r) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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

    public static BufferedImage verticalMirrorImage(FxTask task, BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int i = 0; i < width; ++i) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int t = 0;
                int b = height - 1;
                while (t < b) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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
