package mara.mybox.image.data;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2026-11-5
 * @License Apache License Version 2.0
 *
 * Assisted by Doubao and DeepSeek
 */
public class ImageLiquify {

    public enum LiquifyOperation {
        PUSH, // Push, drag and shift pixels
        EXPAND, // Expand, push pixels outward
        SHRINK, // Shrink, squeeze pixels inward
        ROTATE_CLOCKWISE, // Rotate-Clockwise, clockwise pixel rotation
        ROTATE_COUNTER_CLOCKWISE, // Rotate-Counter-Clockwise, counter-clockwise pixel rotation
        TURBULENCE // Turbulence, small chaotic pixel perturbation
    }

    public enum FalloffAlgorithm {
        SMOOTH_QUADRATIC, // Smooth-Quadratic, quadratic falloff: (1‑t)²
        GAUSSIAN, // Gaussian, gaussian falloff: exp(-4t²)
        SMOOTH_STEP, // Smooth-Step, standard smoothstep: 3t²−2t³
        SMOOTH_QUARTIC, // Smooth-Quartic, quartic falloff: (1‑t)^4
        PS_LIKE, // Photoshop-Like, PS-style brush; hardness controls the size of solid center
        HARD_CIRCLE // Hard-Circle, hard edged circle brush, for debugging
    }

    private FxTask task;
    private BufferedImage source, target;
    private LiquifyOperation operation = LiquifyOperation.PUSH;

    private IntPoint startPoint; // Start coordinate point
    private IntPoint endPoint; // End coordinate point
    private FalloffAlgorithm fallOff = FalloffAlgorithm.SMOOTH_QUADRATIC; // Brush falloff algorithm
    private PixelSampler.Sampler sampler; // Pixel sampler
    private PerlinNoise perlin; // Perlin noise instance for turbulence

    private int radius = 80; // Brush radius; range: >0; recommended: 20～200
    private int step = 20; // Stroke sampling step; range: ≥1; recommended: 5～40, smaller for smoother drag
    private float strengthMax = 0.6f; // Maximum distortion strength; range: 0～1; recommended: 0.3～0.8
    private float strengthThreshold = 0.05f; // Strength threshold, skip distortion below this value; range: 0～0.2; recommended: 0.02～0.08
    private float hardness = 0.5f; // Brush hardness; range: 0～1; recommended: 0.2～0.7; 0 = fully feathered, 1 = solid hard core, only works for PS_LIKE

    private float expandShrinkPower = 0.35f; // Expand / shrink power factor; range: 0～1; recommended: 0.2～0.6
    private float rotatePower = 0.45f; // Rotation power factor; range: 0～1; recommended: 0.3～0.7
    private long turbulenceSeed = 12345L; // Turbulence noise seed; range: any long; recommended: arbitrary fixed long value for different pattern
    private float turbulencePower = 0.22f; // Overall turbulence strength; range: 0～1; recommended: 0.1～0.35
    private float turbulenceFreq = 0.055f; // Turbulence frequency, higher for finer details; range: >0; recommended: 0.02～0.1
    private int turbulenceOctaves = 3; // Noise octaves; stacked noise layers for fine details; range: ≥1; recommended: 2～4
    private int safeMargin = 12; // Border safe margin to avoid out-of-bounds; range: ≥0; recommended: 8～20

    private float[][] localDxArray, localDyArray, localStrengthArray; // Local warp field: x offset, y offset, weight strength

    private int imageWidth, imageHeight;
    private int boxX, boxY, boxW, boxH;

    public ImageLiquify() {
    }

    public Image render(Image image, FxTask inTask) {
        if (image == null) {
            return null;
        }
        source = SwingFXUtils.fromFXImage(image, null);
        task = inTask;
        target = render();
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public BufferedImage render() {
        try {
            if (!isValid()) {
                return null;
            }
            imageWidth = source.getWidth();
            imageHeight = source.getHeight();

            if (!writeFields()) {
                return null;
            }

            target = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();

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

                    float dxVal = localDxArray[ly][lx];
                    float dyVal = localDyArray[ly][lx];
                    float strength = Math.min(localStrengthArray[ly][lx], 1.0f);

                    float sx = x - dxVal;
                    float sy = y - dyVal;
                    int color;

                    boolean sourcePosInBox = (sx >= boxX && sx <= boxX + boxW - 1)
                            && (sy >= boxY && sy <= boxY + boxH - 1);
                    if (strength < strengthThreshold || !sourcePosInBox) {
                        int ix = Math.round(sx);
                        int iy = Math.round(sy);
                        if (ix >= 0 && iy >= 0 && ix < imageWidth && iy < imageHeight) {
                            color = source.getRGB(ix, iy);
                        } else {
                            color = 0;
                        }
                    } else {
                        color = sampler.sample(source, sx, sy);
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

    public boolean writeFields() {
        try {
            if (operation == LiquifyOperation.TURBULENCE && perlin == null) {
                perlin = new PerlinNoise(turbulenceSeed);
            }

            float minX, maxX, minY, maxY;
            if (endPoint != null) {
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

            minX -= safeMargin;
            maxX += safeMargin;
            minY -= safeMargin;
            maxY += safeMargin;

            minX = Math.max(0, minX);
            maxX = Math.min(imageWidth - 1, maxX);
            minY = Math.max(0, minY);
            maxY = Math.min(imageHeight - 1, maxY);

            boxX = (int) Math.floor(minX);
            boxY = (int) Math.floor(minY);
            boxW = (int) Math.ceil(maxX) - boxX + 1;
            boxH = (int) Math.ceil(maxY) - boxY + 1;

            if (boxW <= 0 || boxH <= 0) {
                return false;
            }

            localDxArray = new float[boxH][boxW];
            localDyArray = new float[boxH][boxW];
            localStrengthArray = new float[boxH][boxW];
            strengthMax = Math.clamp(strengthMax, 0f, 1f);

            if (endPoint != null) {
                int startX = startPoint.getX();
                int startY = startPoint.getY();
                int endX = endPoint.getX();
                int endY = endPoint.getY();

                float len = (float) Math.hypot(endX - startX, endY - startY);
                if (len < 1.0f) {
                    return addWarpToField(startX, startY, 0, 0);
                }

                int stepCount = Math.max(1, (int) Math.ceil(len / step));
                float stepX = (operation == LiquifyOperation.PUSH) ? (endX - startX) / (float) stepCount : 0f;
                float stepY = (operation == LiquifyOperation.PUSH) ? (endY - startY) / (float) stepCount : 0f;

                float currentX = startX;
                float currentY = startY;
                for (int i = 0; i < stepCount; i++) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    if (!addWarpToField(currentX, currentY, stepX, stepY)) {
                        return false;
                    }
                    float segDx = (endX - startX) / (float) stepCount;
                    float segDy = (endY - startY) / (float) stepCount;
                    currentX += segDx;
                    currentY += segDy;
                }
            } else {
                if (!addWarpToField(startPoint.getX(), startPoint.getY(), 0, 0)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean addWarpToField(float currentX, float currentY, float stepX, float stepY) {
        try {
            int xMin = Math.max(0, (int) Math.floor(currentX - radius));
            int xMax = Math.min(imageWidth - 1, (int) Math.ceil(currentX + radius));
            int yMin = Math.max(0, (int) Math.floor(currentY - radius));
            int yMax = Math.min(imageHeight - 1, (int) Math.ceil(currentY + radius));

            for (int y = yMin; y <= yMax; y++) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                for (int x = xMin; x <= xMax; x++) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }

                    float dx = x - currentX;
                    float dy = y - currentY;
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
                    float weight = calculateKernelWeight(t);
                    weight *= strengthMax;

                    float addDx = 0;
                    float addDy = 0;

                    switch (operation) {
                        case PUSH:
                            addDx = stepX * weight;
                            addDy = stepY * weight;
                            break;
                        case EXPAND: {
                            addDx = dx * weight * expandShrinkPower;
                            addDy = dy * weight * expandShrinkPower;
                            break;
                        }
                        case SHRINK: {
                            addDx = -dx * weight * expandShrinkPower;
                            addDy = -dy * weight * expandShrinkPower;
                            break;
                        }
                        case ROTATE_CLOCKWISE: {
                            addDx = dy * weight * rotatePower;
                            addDy = -dx * weight * rotatePower;
                            break;
                        }
                        case ROTATE_COUNTER_CLOCKWISE: {
                            addDx = -dy * weight * rotatePower;
                            addDy = dx * weight * rotatePower;
                            break;
                        }
                        case TURBULENCE: {
                            float relX = x - currentX;
                            float relY = y - currentY;
                            float nX = perlin.fbm(relX, relY, turbulenceFreq, turbulenceOctaves);
                            float nY = perlin.fbm(relX + 1000f, relY + 1000f, turbulenceFreq, turbulenceOctaves);
                            float noiseGain = 1.5f;
                            addDx = nX * noiseGain * weight * radius * turbulencePower;
                            addDy = nY * noiseGain * weight * radius * turbulencePower;
                            break;

                        }
                    }

                    localStrengthArray[ly][lx] += weight;
                    localDxArray[ly][lx] += addDx;
                    localDyArray[ly][lx] += addDy;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean isValid() {
        if (operation == null || source == null || startPoint == null
                || fallOff == null || sampler == null) {
            return false;
        }
        return radius > 0;
    }

    public float calculateKernelWeight(float t) {
        if (t < 0f || t > 1f) {
            return 0f;
        }
        return switch (fallOff) {
            case SMOOTH_QUADRATIC -> (1 - t) * (1 - t);
            case GAUSSIAN -> (float) Math.exp(-4.0f * t * t);
            case SMOOTH_STEP -> 1f - (3 * t * t - 2 * t * t * t);
            case SMOOTH_QUARTIC -> {
                float v = 1 - t;
                yield v * v * v * v;
            }
            case HARD_CIRCLE -> 1f;
            case PS_LIKE -> {
                float inner = hardness;
                float w = 1.0f - inner;
                if (w <= 1e-6f) {
                    yield 1.0f;
                }
                float s = (t - inner) / w;
                s = Math.clamp(s, 0, 1);
                yield 1f - (3 * s * s - 2 * s * s * s);
            }
        };
    }

    public ImageLiquify copy() {
        ImageLiquify dst = new ImageLiquify();
        dst.task = this.task;
        dst.source = this.source;
        dst.startPoint = this.startPoint;
        dst.endPoint = endPoint;
        dst.operation = this.operation;
        dst.fallOff = this.fallOff;
        dst.sampler = this.sampler;
        dst.radius = this.radius;
        dst.step = this.step;
        dst.strengthMax = this.strengthMax;
        dst.strengthThreshold = this.strengthThreshold;
        dst.hardness = this.hardness;
        dst.turbulenceSeed = this.turbulenceSeed;
        dst.expandShrinkPower = this.expandShrinkPower;
        dst.rotatePower = this.rotatePower;
        dst.turbulencePower = this.turbulencePower;
        dst.turbulenceSeed = this.turbulenceSeed;
        dst.turbulenceFreq = this.turbulenceFreq;
        dst.turbulenceOctaves = this.turbulenceOctaves;
        dst.safeMargin = this.safeMargin;
        return dst;
    }

    public class PerlinNoise {

        private final int[] perm;
        private static final float[] GRAD_X = {1, -1, 1, -1, 1, -1, 0, 0};
        private static final float[] GRAD_Y = {1, 1, -1, -1, 0, 0, 1, -1};

        public PerlinNoise(long seed) {
            Random rnd = new Random(seed);
            perm = new int[256];
            for (int i = 0; i < 256; i++) {
                perm[i] = i;
            }
            for (int i = 255; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int t = perm[i];
                perm[i] = perm[j];
                perm[j] = t;
            }
        }

        private float fade(float t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }

        private float lerp(float a, float b, float t) {
            return a + t * (b - a);
        }

        private float grad(int hash, float x, float y) {
            int idx = hash & 7;
            return GRAD_X[idx] * x + GRAD_Y[idx] * y;
        }

        public float noise(float x, float y) {
            int xi = (int) Math.floor(x) & 255;
            int yi = (int) Math.floor(y) & 255;

            float xf = x - (float) Math.floor(x);
            float yf = y - (float) Math.floor(y);

            int aa = perm[(perm[xi] + (yi & 255)) & 255];
            int ab = perm[(perm[xi] + ((yi + 1) & 255)) & 255];
            int ba = perm[(perm[(xi + 1) & 255] + (yi & 255)) & 255];
            int bb = perm[(perm[(xi + 1) & 255] + ((yi + 1) & 255)) & 255];

            float u = fade(xf);
            float v = fade(yf);

            return lerp(
                    lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u),
                    lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u),
                    v
            );
        }

        public float fbm(float x, float y, float freq, int octaves) {
            float sum = 0;
            float amp = 1;
            float totalAmp = 0;
            for (int i = 0; i < octaves; i++) {
                sum += noise(x * freq, y * freq) * amp;
                totalAmp += amp;
                freq *= 2;
                amp *= 0.5f;
            }
            return sum / totalAmp;
        }
    }

    /*
        get/set
     */
    public FxTask getTask() {
        return task;
    }

    public ImageLiquify setTask(FxTask task) {
        this.task = task;
        return this;
    }

    public BufferedImage getSource() {
        return source;
    }

    public ImageLiquify setSource(BufferedImage source) {
        this.source = source;
        return this;
    }

    public BufferedImage getTarget() {
        return target;
    }

    public ImageLiquify setTarget(BufferedImage target) {
        this.target = target;
        return this;
    }

    public IntPoint getStartPoint() {
        return startPoint;
    }

    public ImageLiquify setStartPoint(IntPoint startPoint) {
        this.startPoint = startPoint;
        return this;
    }

    public IntPoint getEndPoint() {
        return endPoint;
    }

    public ImageLiquify setEndPoint(IntPoint endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public LiquifyOperation getOperation() {
        return operation;
    }

    public ImageLiquify setOperation(LiquifyOperation operation) {
        this.operation = operation;
        return this;
    }

    public FalloffAlgorithm getFallOff() {
        return fallOff;
    }

    public ImageLiquify setFallOff(FalloffAlgorithm kernel) {
        this.fallOff = kernel;
        return this;
    }

    public PixelSampler.Sampler getSampler() {
        return sampler;
    }

    public ImageLiquify setSampler(PixelSampler.Sampler sampler) {
        this.sampler = sampler;
        return this;
    }

    public ImageLiquify setSampler(PixelSampler.SamplingAlgorithm mode, PixelSampler.EdgeMode edgeMode) {
        this.sampler = mode.create().setEdgeMode(edgeMode);
        return this;
    }

    public PerlinNoise getPerlin() {
        return perlin;
    }

    public ImageLiquify setPerlin(PerlinNoise perlin) {
        this.perlin = perlin;
        return this;
    }

    public int getRadius() {
        return radius;
    }

    public ImageLiquify setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public int getStep() {
        return step;
    }

    public ImageLiquify setStep(int step) {
        this.step = step;
        return this;
    }

    public float getStrengthMax() {
        return strengthMax;
    }

    public ImageLiquify setStrengthMax(float strengthMax) {
        this.strengthMax = strengthMax;
        return this;
    }

    public float getStrengthThreshold() {
        return strengthThreshold;
    }

    public ImageLiquify setStrengthThreshold(float strengthThreshold) {
        this.strengthThreshold = strengthThreshold;
        return this;
    }

    public float getHardness() {
        return hardness;
    }

    public ImageLiquify setHardness(float hardness) {
        this.hardness = hardness;
        return this;
    }

    public float getExpandShrinkPower() {
        return expandShrinkPower;
    }

    public ImageLiquify setExpandShrinkPower(float expandShrinkPower) {
        this.expandShrinkPower = expandShrinkPower;
        return this;
    }

    public float getRotatePower() {
        return rotatePower;
    }

    public ImageLiquify setRotatePower(float rotatePower) {
        this.rotatePower = rotatePower;
        return this;
    }

    public float getTurbulencePower() {
        return turbulencePower;
    }

    public ImageLiquify setTurbulencePower(float turbulencePower) {
        this.turbulencePower = turbulencePower;
        return this;
    }

    public long getTurbulenceSeed() {
        return turbulenceSeed;
    }

    public ImageLiquify setTurbulenceSeed(long turbulenceSeed) {
        this.turbulenceSeed = turbulenceSeed;
        return this;
    }

    public float getTurbulenceFreq() {
        return turbulenceFreq;
    }

    public ImageLiquify setTurbulenceFreq(float turbulenceFreq) {
        this.turbulenceFreq = turbulenceFreq;
        return this;
    }

    public int getTurbulenceOctaves() {
        return turbulenceOctaves;
    }

    public ImageLiquify setTurbulenceOctaves(int turbulenceOctaves) {
        this.turbulenceOctaves = turbulenceOctaves;
        return this;
    }

    public int getSafeMargin() {
        return safeMargin;
    }

    public ImageLiquify setSafeMargin(int safeMargin) {
        this.safeMargin = safeMargin;
        return this;
    }

}
