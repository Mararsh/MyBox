package mara.mybox.image.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxTask;
import static mara.mybox.image.tools.ColorConvertTools.getAlpha;
import static mara.mybox.image.tools.ColorConvertTools.getBlue;
import static mara.mybox.image.tools.ColorConvertTools.getGreen;
import static mara.mybox.image.tools.ColorConvertTools.getRed;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 *
 * Helped with DeepSeek and DouBao
 */
public class LiquifyTools {

    public static BufferedImage forwardWarp(FxTask task, BufferedImage source,
            IntPoint startPoint, IntPoint endPoint,
            int radius, int step, float strength, boolean isGauss) {
        if (startPoint == null || endPoint == null) {
            return source;
        }
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = target.createGraphics();
        if (AppVariables.ImageHints != null) {
            g.addRenderingHints(AppVariables.ImageHints);
        }
        Color bgColor = Colors.TRANSPARENT;
        g.setBackground(bgColor);
        if (task != null && !task.isWorking()) {
            return null;
        }
        g.drawImage(source, null, null);
        g.dispose();

        int startX = startPoint.getX();
        int startY = startPoint.getY();
        int endX = endPoint.getX();
        int endY = endPoint.getY();

        float len = (float) Math.hypot(endX - startX, endY - startY);
        if (len < 1.0f) {
            return source;
        }
//        float step = radius * 0.4f;
        int stepCount = (int) Math.ceil(len / step);
        if (stepCount < 1) {
            stepCount = 1;
        }

        int dxTotal = endX - startX;
        int dyTotal = endY - startY;
        int stepX = dxTotal / stepCount;
        int stepY = dyTotal / stepCount;
        int currentX = startX;
        int currentY = startY;
        for (int i = 0; i < stepCount; i++) {
            forwardStep(task, source, target,
                    currentX, currentY, stepX, stepY,
                    radius, strength, isGauss);
            currentX += stepX;
            currentY += stepY;
        }
        return target;
    }

    public static BufferedImage forwardStep(FxTask task,
            BufferedImage source, BufferedImage target,
            int currentX, int currentY, int stepX, int stepY,
            int radius, float strength, boolean isGauss) {
        int w = source.getWidth();
        int h = source.getHeight();

        int xMin = Math.max(0, currentX - radius);
        int xMax = Math.min(w - 1, currentX + radius);
        int yMin = Math.max(0, currentY - radius);
        int yMax = Math.min(h - 1, currentY + radius);

        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                int dx = x - currentX;
                int dy = y - currentY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > radius) {
                    continue;
                }
                float t = dist / radius;
                float weight;
                if (isGauss) {
                    weight = (float) Math.exp(-t * t);
                } else {
                    weight = 1.0f - t * t;
                }
                float srcX = x - stepX * weight;
                float srcY = y - stepY * weight;
                srcX = Math.clamp(srcX, 0, w - 1);
                srcY = Math.clamp(srcY, 0, h - 1);
                int color = bilinearInterpolationSample2(source, srcX, srcY);
                target.setRGB(x, y, color);
            }
        }
        return target;
    }

    private static int bilinearInterpolationSample(BufferedImage img, float x, float y) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) {
            return 0;
        }
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
//        if (img.getRGB(x0, y0) == 0) {
//            return 0;
//        }
        int x1 = Math.min(x0 + 1, img.getWidth() - 1);
        int y1 = Math.min(y0 + 1, img.getHeight() - 1);
        float fx = x - x0;
        float fy = y - y0;
        float w00 = (1 - fx) * (1 - fy);
        float w10 = fx * (1 - fy);
        float w01 = (1 - fx) * fy;
        float w11 = fx * fy;
        int p00 = img.getRGB(x0, y0);
        int p10 = img.getRGB(x1, y0);
        int p01 = img.getRGB(x0, y1);
        int p11 = img.getRGB(x1, y1);
        float sumW = 0;
        float sumR = 0, sumG = 0, sumB = 0, sumA = 0;

        if (p00 > 0) {
            sumW += w00;
            sumR += getRed(p00) * w00;
            sumG += getGreen(p00) * w00;
            sumB += getBlue(p00) * w00;
            sumA += getAlpha(p00) * w00;
        }

        if (p10 > 0) {
            sumW += w10;
            sumR += getRed(p10) * w10;
            sumG += getGreen(p10) * w10;
            sumB += getBlue(p10) * w10;
            sumA += getAlpha(p10) * w10;
        }

        if (p01 > 0) {
            sumW += w01;
            sumR += getRed(p01) * w01;
            sumG += getGreen(p01) * w01;
            sumB += getBlue(p01) * w01;
            sumA += getAlpha(p01) * w01;
        }

        if (p11 > 0) {
            sumW += w11;
            sumR += getRed(p11) * w11;
            sumG += getGreen(p11) * w11;
            sumB += getBlue(p11) * w11;
            sumA += getAlpha(p11) * w11;
        }

        if (sumW == 0) {
            return 0;
        }
        float invSum = 1f / sumW;
        float r = Math.clamp(sumR * invSum, 0, 255);
        float g = Math.clamp(sumG * invSum, 0, 255);
        float b = Math.clamp(sumB * invSum, 0, 255);
        float a = Math.clamp(sumA * invSum, 0, 255);

        return ((int) a << 24) | ((int) r << 16) | ((int) g << 8) | (int) b;
    }

    private static int bilinearInterpolationSample2(BufferedImage img, float sx, float sy) {
        int x0 = (int) Math.floor(sx);
        int y0 = (int) Math.floor(sy);
        int x1 = Math.min(x0 + 1, img.getWidth() - 1);
        int y1 = Math.min(y0 + 1, img.getHeight() - 1);
        float u = sx - x0;
        float v = sy - y0;

        int c00 = img.getRGB(x0, y0);
        int c01 = img.getRGB(x0, y1);
        int c10 = img.getRGB(x1, y0);
        int c11 = img.getRGB(x1, y1);

        int r = bilinearInterpolation(getRed(c00), getRed(c01), getRed(c10), getRed(c11), u, v);
        int g = bilinearInterpolation(getGreen(c00), getGreen(c01), getGreen(c10), getGreen(c11), u, v);
        int b = bilinearInterpolation(getBlue(c00), getBlue(c01), getBlue(c10), getBlue(c11), u, v);
        return (0xff << 24) | (r << 16) | (g << 8) | b;
    }

    private static int bilinearInterpolation(int v00, int v01, int v10, int v11, float u, float v) {
        float a = v00 * (1 - u) + v10 * u;
        float b = v01 * (1 - u) + v11 * u;
        return (int) (a * (1 - v) + b * v);
    }

}
