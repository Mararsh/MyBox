package mara.mybox.image.tools;

import java.awt.image.BufferedImage;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.PixelSampler;
import mara.mybox.image.data.PixelSampler.SampleMode;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 *
 * Helped with DeepSeek and DouBao
 */
public class LiquifyTools {

    public static enum InterpolationAlgorithm {
        Nearest, Bilinear, BicubicCatmullRom, Lanczos2
    }

    public static BufferedImage forwardWarp(FxTask task, BufferedImage source,
            IntPoint startPoint, IntPoint endPoint, SampleMode mode,
            int radius, int step, float strengthMax, float strengthThreshold, boolean isGauss) {
        if (startPoint == null || endPoint == null) {
            return source;
        }
        int w = source.getWidth();
        int h = source.getHeight();

        float[][] dxField = new float[h][w];
        float[][] dyField = new float[h][w];
        float[][] strengthField = new float[h][w];

        int startX = startPoint.getX();
        int startY = startPoint.getY();
        int endX = endPoint.getX();
        int endY = endPoint.getY();

        float len = (float) Math.hypot(endX - startX, endY - startY);
        if (len < 1.0f) {
            return source;
        }

        int stepCount = Math.max(1, (int) Math.ceil(len / step));
        float stepX = (endX - startX) / (float) stepCount;
        float stepY = (endY - startY) / (float) stepCount;

        float currentX = startX;
        float currentY = startY;

        strengthMax = Math.clamp(strengthMax, 0f, 1f);
        strengthThreshold = Math.clamp(strengthThreshold, 0f, 1f);
        for (int i = 0; i < stepCount; i++) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            addWarpToField(dxField, dyField, strengthField, w, h,
                    currentX, currentY, stepX, stepY,
                    radius, strengthMax, isGauss);
            currentX += stepX;
            currentY += stepY;
        }
//        for (int y = 0; y < h; y++) {
//            for (int x = 0; x < w; x++) {
//                strengthField[y][x] = Math.min(strengthField[y][x], 1.0f);
//            }
//        }
        BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelSampler.Sampler sampler = mode.sampler;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float sx = x - dxField[y][x];
                float sy = y - dyField[y][x];
                float strength = strengthField[y][x];
                int color;
                if (strength < strengthThreshold) {
                    int ix = Math.round(sx);
                    int iy = Math.round(sy);
                    if (ix >= 0 && iy >= 0 && ix < w && iy < h) {
                        color = source.getRGB(ix, iy);
                    } else {
                        color = 0;
                    }
                } else {
                    color = sampler.sample(source, sx, sy);
                }
                target.setRGB(x, y, color);
            }
            if (task != null && !task.isWorking()) {
                return null;
            }
        }
        return target;
    }

    private static void addWarpToField(float[][] dxField, float[][] dyField, float[][] strengthField,
            int w, int h,
            float cx, float cy,
            float vecX, float vecY,
            int radius, float strengthMax, boolean isGauss) {
        int xMin = Math.max(0, (int) Math.floor(cx - radius));
        int xMax = Math.min(w - 1, (int) Math.ceil(cx + radius));
        int yMin = Math.max(0, (int) Math.floor(cy - radius));
        int yMax = Math.min(h - 1, (int) Math.ceil(cy + radius));

        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.hypot(dx, dy);
                if (dist > radius) {
                    continue;
                }
                float t = dist / radius;
                float weight;
                if (isGauss) {
                    // 高斯，在 t=1 平滑落到接近 0
                    weight = (float) Math.exp(-4.0f * t * t);
                } else {
                    // smoothstep，画笔边缘更柔和
                    weight = (1 - t) * (1 - t);
                }
                weight = weight * strengthMax;
                strengthField[y][x] += weight;
                dxField[y][x] += vecX * weight;
                dyField[y][x] += vecY * weight;
            }
        }
    }

}
