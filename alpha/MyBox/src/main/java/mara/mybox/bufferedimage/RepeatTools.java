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

    public static BufferedImage repeat(BufferedImage source, int scaleWidth, int scaleHeight,
            int repeatH, int repeatV, int interval, int margin, Color bgColor) {
        try {
            if (source == null || scaleWidth <= 0 || scaleHeight <= 0 || repeatH <= 0 || repeatV <= 0) {
                return source;
            }
            BufferedImage scaled = ScaleTools.scaleImage(source, scaleWidth, scaleHeight);
            int width = scaled.getWidth();
            int height = scaled.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            int totalWidth = width * repeatH + interval * (repeatH - 1) + margin * 2;
            int totalHeight = height * repeatV + interval * (repeatV - 1) + margin * 2;
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
                    g.drawImage(scaled, x, y, width, height, null);
                    x += width + interval;
                }
                y += height + interval;
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return source;
        }
    }

    public static BufferedImage tile(BufferedImage source, int scaleWidth, int scaleHeight,
            int canvasWidth, int canvasHeight, int interval, int margin, Color bgColor) {
        try {
            if (source == null || scaleWidth <= 0 || scaleHeight <= 0 || canvasWidth <= 0 || canvasHeight <= 0) {
                return source;
            }
            BufferedImage scaled = ScaleTools.scaleImage(source, scaleWidth, scaleHeight);
            int width = scaled.getWidth();
            int height = scaled.getHeight();
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
            for (int v = margin; v < canvasHeight - height - margin; v += height + interval) {
                for (int h = margin; h < canvasWidth - width - margin; h += width + interval) {
                    g.drawImage(scaled, h, v, width, height, null);
                    x = h + width + interval;
                    y = v + height + interval;
                }
            }
            int leftWidth = canvasWidth - margin - x;
            if (leftWidth > 0) {
                BufferedImage cropped = CropTools.cropOutside(scaled, 0, 0, leftWidth - 1, height - 1);
                for (int v = margin; v < canvasHeight - height - margin; v += height + interval) {
                    g.drawImage(cropped, x, v, leftWidth, height, null);
                }
            }
            int leftHeight = canvasHeight - margin - y;
            if (leftHeight > 0) {
                BufferedImage cropped = CropTools.cropOutside(scaled, 0, 0, width - 1, leftHeight - 1);
                for (int h = margin; h < canvasWidth - width - margin; h += width + interval) {
                    g.drawImage(cropped, h, y, width, leftHeight, null);
                }
            }
            if (leftWidth > 0 && leftHeight > 0) {
                BufferedImage cropped = CropTools.cropOutside(scaled, 0, 0, leftWidth - 1, leftHeight - 1);
                g.drawImage(cropped, x, y, leftWidth, leftHeight, null);
            }

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return source;
        }
    }

}
