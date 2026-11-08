package mara.mybox.image.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.LiquifyConfig;
import mara.mybox.image.data.PixelSampler;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 *
 * Helped with DeepSeek and DouBao
 */
public class LiquifyTools {

    public enum LiquifyType {
        PUSH, // Push，拖拽推移
        EXPAND, // Expand，膨胀：向外扩张
        SHRINK, // Shrink，收缩：向内挤压
        ROTATE_CLOCKWISE, // Rotate‑Clockwise，顺时针旋转
        ROTATE_COUNTER_CLOCKWISE,// Rotate‑Counter‑Clockwise，逆时针旋转
        TURBULENCE              // Turbulence，褶皱/湍流，小幅度扰动
    }

    public enum BrushKernel {
        SMOOTH_QUADRATIC, // Smooth‑Quadratic，二次平方衰减，(1‑t)²
        GAUSSIAN, // Gaussian，高斯衰减，exp(-4t²)
        SMOOTH_STEP, // Smooth‑Step，标准平滑阶跃，3t²−2t³
        SMOOTH_QUARTIC, // Smooth‑Quartic，四次方衰减，(1‑t)^4
        PS_LIKE, // Photoshop‑Like，PS风格画笔（依赖hardness参数）
        HARD_CIRCLE         // Hard‑Circle，硬圆形画笔，调试用
    }

    public static BufferedImage liquify(LiquifyConfig config) {
        try {
            if (config == null) {
                return null;
            }
            LiquifyConfig cfg = config.copy();
            if (!cfg.isValid()) {
                return null;
            }

            FxTask task = cfg.getTask();
            BufferedImage source = cfg.getSource();
            IntPoint startPoint = cfg.getStartPoint();
            IntPoint endPoint = cfg.getEndPoint();
            LiquifyType liquifyType = cfg.getLiquifyType();

            int w = source.getWidth();
            int h = source.getHeight();
            int radius = cfg.getRadius();

            float minX, maxX, minY, maxY;
            if (liquifyType == LiquifyType.PUSH) {
                minX = Math.min(startPoint.getX(), endPoint.getX()) - radius;
                maxX = Math.max(startPoint.getX(), endPoint.getX()) + radius;
                minY = Math.min(startPoint.getY(), endPoint.getY()) - radius;
                maxY = Math.max(startPoint.getY(), endPoint.getY()) + radius;
            } else {
                minX = startPoint.getX() - radius;
                maxX = startPoint.getX() + radius;
                minY = startPoint.getY() - radius;
                maxY = startPoint.getY() + radius;
            }

            // A+B安全余量
            minX -= cfg.getSafeMargin();
            maxX += cfg.getSafeMargin();
            minY -= cfg.getSafeMargin();
            maxY += cfg.getSafeMargin();

            minX = Math.max(0, minX);
            maxX = Math.min(w - 1, maxX);
            minY = Math.max(0, minY);
            maxY = Math.min(h - 1, maxY);

            int boxX = (int) Math.floor(minX);
            int boxY = (int) Math.floor(minY);
            int boxW = (int) Math.ceil(maxX) - boxX + 1;
            int boxH = (int) Math.ceil(maxY) - boxY + 1;

            if (boxW <= 0 || boxH <= 0) {
                return source;
            }

            float[][] localDx = new float[boxH][boxW];
            float[][] localDy = new float[boxH][boxW];
            float[][] localStrength = new float[boxH][boxW];

            float strengthMax = Math.clamp(cfg.getStrengthMax(), 0f, 1f);
            float currentX, currentY;

            if (liquifyType == LiquifyType.PUSH) {
                int startX = startPoint.getX();
                int startY = startPoint.getY();
                int endX = endPoint.getX();
                int endY = endPoint.getY();
                float len = (float) Math.hypot(endX - startX, endY - startY);
                if (len < 1.0f) {
                    return null;
                }
                int stepCount = Math.max(1, (int) Math.ceil(len / cfg.getStep()));
                float stepX = (endX - startX) / (float) stepCount;
                float stepY = (endY - startY) / (float) stepCount;
                currentX = startX;
                currentY = startY;

                for (int i = 0; i < stepCount; i++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    addWarpToField(task, cfg,
                            localDx, localDy, localStrength,
                            boxX, boxY, boxW, boxH,
                            w, h, currentX, currentY, stepX, stepY,
                            radius, strengthMax);
                    currentX += stepX;
                    currentY += stepY;
                }
            } else {
                currentX = startPoint.getX();
                currentY = startPoint.getY();
                addWarpToField(task, cfg,
                        localDx, localDy, localStrength,
                        boxX, boxY, boxW, boxH,
                        w, h, currentX, currentY, 0, 0,
                        radius, strengthMax);
            }

            BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, null);
            PixelSampler.Sampler sampler = cfg.getSampler();
            float strengthThreshold = Math.clamp(cfg.getStrengthThreshold(), 0f, 1f);

            for (int ly = 0; ly < boxH; ly++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                int y = boxY + ly;
                for (int lx = 0; lx < boxW; lx++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    int x = boxX + lx;

                    float dxVal = localDx[ly][lx];
                    float dyVal = localDy[ly][lx];
                    float strength = Math.min(localStrength[ly][lx], 1.0f);

                    float sx = x - dxVal;
                    float sy = y - dyVal;

                    int color;
                    boolean sourcePosInBox = (sx >= boxX && sx <= boxX + boxW - 1)
                            && (sy >= boxY && sy <= boxY + boxH - 1);

                    if (sourcePosInBox) {
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
                    } else {
                        int ix = Math.round(sx);
                        int iy = Math.round(sy);
                        if (ix >= 0 && iy >= 0 && ix < w && iy < h) {
                            color = source.getRGB(ix, iy);
                        } else {
                            color = 0;
                        }
                    }
                    target.setRGB(x, y, color);
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    private static void addWarpToField(FxTask task, LiquifyConfig cfg,
            float[][] localDx, float[][] localDy, float[][] localStrength,
            int boxX, int boxY, int boxW, int boxH,
            int imgW, int imgH,
            float cx, float cy,
            float vecX, float vecY,
            int radius, float strengthMax) {

        if (radius <= 0) {
            return;
        }

        int xMin = Math.max(0, (int) Math.floor(cx - radius));
        int xMax = Math.min(imgW - 1, (int) Math.ceil(cx + radius));
        int yMin = Math.max(0, (int) Math.floor(cy - radius));
        int yMax = Math.min(imgH - 1, (int) Math.ceil(cy + radius));

        LiquifyType liquifyType = cfg.getLiquifyType();
        BrushKernel kernel = cfg.getKernel();

        for (int y = yMin; y <= yMax; y++) {
            if (task != null && !task.isWorking()) {
                return;
            }
            for (int x = xMin; x <= xMax; x++) {
                if (task != null && !task.isWorking()) {
                    return;
                }

                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.hypot(dx, dy);
                if (dist > radius) {
                    continue;
                }

                int lx = x - boxX;
                int ly = y - boxY;
                if (lx < 0 || lx >= boxW || ly < 0 || ly >= boxH) {
                    continue;
                }

                float t = dist / radius;
                float weight = calculateKernelWeight(kernel, t, cfg.getHardness());
                weight *= strengthMax;

                float addDx = 0;
                float addDy = 0;

                switch (liquifyType) {
                    case PUSH:
                        addDx = vecX * weight;
                        addDy = vecY * weight;
                        break;
                    case EXPAND: {
                        if (dist > 1e-6f) {
                            float nx = dx / dist;
                            float ny = dy / dist;
                            addDx = nx * weight * radius * cfg.getExpandShrinkPower();
                            addDy = ny * weight * radius * cfg.getExpandShrinkPower();
                        }
                        break;
                    }
                    case SHRINK: {
                        if (dist > 1e-6f) {
                            float nx = dx / dist;
                            float ny = dy / dist;
                            addDx = -nx * weight * radius * cfg.getExpandShrinkPower();
                            addDy = -ny * weight * radius * cfg.getExpandShrinkPower();
                        }
                        break;
                    }
                    case ROTATE_CLOCKWISE: {
                        addDx = dy * weight * cfg.getRotatePower();
                        addDy = -dx * weight * cfg.getRotatePower();
                        break;
                    }
                    case ROTATE_COUNTER_CLOCKWISE: {
                        addDx = -dy * weight * cfg.getRotatePower();
                        addDy = dx * weight * cfg.getRotatePower();
                        break;
                    }
                    case TURBULENCE: {
                        float ang = (float) (Math.sin(dx * 0.22 + dy * 0.13));
                        addDx = ang * weight * radius * cfg.getTurbulencePower();
                        addDy = ang * weight * radius * cfg.getTurbulencePower();
                        break;
                    }
                }
                localStrength[ly][lx] += weight;
                localDx[ly][lx] += addDx;
                localDy[ly][lx] += addDy;
            }
        }
    }

    private static float calculateKernelWeight(BrushKernel kernel, float t, float hardness) {
        if (t < 0f || t > 1f) {
            return 0f;
        }
        return switch (kernel) {
            case SMOOTH_QUADRATIC -> (1 - t) * (1 - t);
            case GAUSSIAN -> (float) Math.exp(-4.0f * t * t);
            case SMOOTH_STEP -> 3 * t * t - 2 * t * t * t;
            case SMOOTH_QUARTIC -> {
                float v = 1 - t;
                yield v * v * v * v;
            }
            case HARD_CIRCLE -> 1f;
            case PS_LIKE -> {
                float inner = 1f - hardness;
                if (t <= inner) {
                    yield 1f;
                }
                float u = (t - inner) / hardness;
                yield 1f - (3 * u * u - 2 * u * u * u);
            }
        };
    }
}
