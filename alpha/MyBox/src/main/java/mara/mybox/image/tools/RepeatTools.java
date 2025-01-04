package mara.mybox.image.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.image.data.CropTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2022-9-25
 * @License Apache License Version 2.0
 */
public class RepeatTools {

    public static BufferedImage repeat(FxTask task, BufferedImage source,
            int repeatH, int repeatV, int interval, int margin, Color bgColor) {
        try {
            if (source == null || repeatH <= 0 || repeatV <= 0) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            int stepx = width + interval;
            int stepy = height + interval;
            int totalWidth = width + stepx * (repeatH - 1) + margin * 2;
            int totalHeight = height + stepy * (repeatV - 1) + margin * 2;
            BufferedImage target = new BufferedImage(totalWidth, totalHeight, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, totalWidth, totalHeight);

            int x, y = margin;
            for (int v = 0; v < repeatV; ++v) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                x = margin;
                for (int h = 0; h < repeatH; ++h) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
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

    public static BufferedImage tile(FxTask task, BufferedImage source,
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
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, canvasWidth, canvasHeight);

            int x = margin, y = margin;
            int stepx = width + interval;
            int stepy = height + interval;
            for (int v = margin; v < canvasHeight - height - margin; v += stepy) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int h = margin; h < canvasWidth - width - margin; h += stepx) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    g.drawImage(source, h, v, width, height, null);
                    x = h + stepx;
                    y = v + stepy;
                }
            }
            int leftWidth = canvasWidth - margin - x;
            if (leftWidth > 0) {
                BufferedImage cropped = CropTools.cropOutside(task, source, 0, 0, leftWidth, height);
                if (cropped == null || (task != null && !task.isWorking())) {
                    return null;
                }
                for (int v = margin; v < canvasHeight - height - margin; v += stepy) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    g.drawImage(cropped, x, v, leftWidth, height, null);
                }
            }
            int leftHeight = canvasHeight - margin - y;
            if (leftHeight > 0) {
                BufferedImage cropped = CropTools.cropOutside(task, source, 0, 0, width, leftHeight);
                if (cropped == null || (task != null && !task.isWorking())) {
                    return null;
                }
                for (int h = margin; h < canvasWidth - width - margin; h += stepx) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    g.drawImage(cropped, h, y, width, leftHeight, null);
                }
            }
            if (leftWidth > 0 && leftHeight > 0) {
                BufferedImage cropped = CropTools.cropOutside(task, source, 0, 0, leftWidth, leftHeight);
                if (cropped == null || (task != null && !task.isWorking())) {
                    return null;
                }
                g.drawImage(cropped, x, y, leftWidth, leftHeight, null);
            }

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

}
