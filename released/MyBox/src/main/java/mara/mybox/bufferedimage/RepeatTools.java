package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2022-9-25
 * @License Apache License Version 2.0
 */
public class RepeatTools {

    public static BufferedImage repeat(BufferedImage source,
            int repeatH, int repeatV, int interval, int margin, Color bgColor) {
        try {
            if (source == null || repeatH <= 0 || repeatV <= 0) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            // Borders between repeats are overriden
            int stepx = width + interval - 1;
            int stepy = height + interval - 1;
            int totalWidth = width + stepx * (repeatH - 1) + margin * 2;
            int totalHeight = height + stepy * (repeatV - 1) + margin * 2;
            BufferedImage target = new BufferedImage(totalWidth, totalHeight, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, totalWidth, totalHeight);

            int x, y = margin;
            for (int v = 0; v < repeatV; ++v) {
                x = margin;
                for (int h = 0; h < repeatH; ++h) {
                    g.drawImage(source, x, y, width, height, null);
                    x += stepx;
                }
                y += stepy;
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage tile(BufferedImage source,
            int canvasWidth, int canvasHeight, int interval, int margin, Color bgColor) {
        try {
            if (source == null || canvasWidth <= 0 || canvasHeight <= 0) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(canvasWidth, canvasHeight, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, canvasWidth, canvasHeight);

            int x = margin, y = margin;
            int stepx = width + interval - 1;
            int stepy = height + interval - 1;
            for (int v = margin; v < canvasHeight - height - margin; v += stepy) {
                for (int h = margin; h < canvasWidth - width - margin; h += stepx) {
                    g.drawImage(source, h, v, width, height, null);
                    x = h + stepx;
                    y = v + stepy;
                }
            }
            int leftWidth = canvasWidth - margin - x;
            if (leftWidth > 0) {
                BufferedImage cropped = CropTools.cropOutside(source, 0, 0, leftWidth - 1, height - 1);
                for (int v = margin; v < canvasHeight - height - margin; v += stepy) {
                    g.drawImage(cropped, x, v, leftWidth, height, null);
                }
            }
            int leftHeight = canvasHeight - margin - y;
            if (leftHeight > 0) {
                BufferedImage cropped = CropTools.cropOutside(source, 0, 0, width - 1, leftHeight - 1);
                for (int h = margin; h < canvasWidth - width - margin; h += stepx) {
                    g.drawImage(cropped, h, y, width, leftHeight, null);
                }
            }
            if (leftWidth > 0 && leftHeight > 0) {
                BufferedImage cropped = CropTools.cropOutside(source, 0, 0, leftWidth - 1, leftHeight - 1);
                g.drawImage(cropped, x, y, leftWidth, leftHeight, null);
            }

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

}
