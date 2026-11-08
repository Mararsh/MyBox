/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mara.mybox.image.data;

import java.awt.image.BufferedImage;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.tools.LiquifyTools.BrushKernel;
import mara.mybox.image.tools.LiquifyTools.LiquifyType;

public class LiquifyConfig {

    private FxTask task;
    private BufferedImage source;
    private IntPoint startPoint;
    private IntPoint endPoint;
    private LiquifyType liquifyType;
    private BrushKernel kernel;

    // 直接持有 Sampler 对象，不再保存 SampleMode
    private PixelSampler.Sampler sampler;

    private int radius = 40;
    private int step = 8;
    private float strengthMax = 0.6f;
    private float strengthThreshold = 0.05f;
    private float hardness = 0.5f;

    private float expandShrinkPower = 0.35f;
    private float rotatePower = 0.45f;
    private float turbulencePower = 0.12f;

    private long turbulenceSeed = 12345L;
    private float turbulenceFreq = 0.03f;
    private int turbulenceOctaves = 3;

    private int safeMargin = 0;

    public static LiquifyConfig defaultConfig() {
        LiquifyConfig c = new LiquifyConfig();
        // 默认：LANCZOS2 + CLAMP_EDGE
        c.sampler = PixelSampler.SamplingAlgorithm.LANCZOS2.create()
                .setEdgeMode(PixelSampler.EdgeMode.CLAMP_EDGE);
        return c;
    }

    public FxTask getTask() {
        return task;
    }

    public LiquifyConfig setTask(FxTask task) {
        this.task = task;
        return this;
    }

    public BufferedImage getSource() {
        return source;
    }

    public LiquifyConfig setSource(BufferedImage source) {
        this.source = source;
        return this;
    }

    public IntPoint getStartPoint() {
        return startPoint;
    }

    public LiquifyConfig setStartPoint(IntPoint startPoint) {
        this.startPoint = startPoint;
        return this;
    }

    public IntPoint getEndPoint() {
        return endPoint;
    }

    public LiquifyConfig setEndPoint(IntPoint endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public LiquifyType getLiquifyType() {
        return liquifyType;
    }

    public LiquifyConfig setLiquifyType(LiquifyType liquifyType) {
        this.liquifyType = liquifyType;
        return this;
    }

    public BrushKernel getKernel() {
        return kernel;
    }

    public LiquifyConfig setKernel(BrushKernel kernel) {
        this.kernel = kernel;
        return this;
    }

    // ---------------- Sampler ----------------
    public PixelSampler.Sampler getSampler() {
        return sampler;
    }

    /**
     * 直接设置已经构建好的Sampler实例
     */
    public LiquifyConfig setSampler(PixelSampler.Sampler sampler) {
        this.sampler = sampler;
        return this;
    }

    /**
     * 快捷：通过SampleMode + EdgeMode内部create全新Sampler实例
     */
    public LiquifyConfig setSampler(PixelSampler.SamplingAlgorithm mode, PixelSampler.EdgeMode edgeMode) {
        this.sampler = mode.create().setEdgeMode(edgeMode);
        return this;
    }

    public int getRadius() {
        return radius;
    }

    public LiquifyConfig setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public int getStep() {
        return step;
    }

    public LiquifyConfig setStep(int step) {
        this.step = step;
        return this;
    }

    public float getStrengthMax() {
        return strengthMax;
    }

    public LiquifyConfig setStrengthMax(float strengthMax) {
        this.strengthMax = strengthMax;
        return this;
    }

    public float getStrengthThreshold() {
        return strengthThreshold;
    }

    public LiquifyConfig setStrengthThreshold(float strengthThreshold) {
        this.strengthThreshold = strengthThreshold;
        return this;
    }

    public float getHardness() {
        return hardness;
    }

    public LiquifyConfig setHardness(float hardness) {
        this.hardness = hardness;
        return this;
    }

    public float getExpandShrinkPower() {
        return expandShrinkPower;
    }

    public LiquifyConfig setExpandShrinkPower(float v) {
        expandShrinkPower = v;
        return this;
    }

    public float getRotatePower() {
        return rotatePower;
    }

    public LiquifyConfig setRotatePower(float v) {
        rotatePower = v;
        return this;
    }

    public float getTurbulencePower() {
        return turbulencePower;
    }

    public LiquifyConfig setTurbulencePower(float v) {
        turbulencePower = v;
        return this;
    }

    public long getTurbulenceSeed() {
        return turbulenceSeed;
    }

    public LiquifyConfig setTurbulenceSeed(long v) {
        turbulenceSeed = v;
        return this;
    }

    public float getTurbulenceFreq() {
        return turbulenceFreq;
    }

    public LiquifyConfig setTurbulenceFreq(float v) {
        turbulenceFreq = v;
        return this;
    }

    public int getTurbulenceOctaves() {
        return turbulenceOctaves;
    }

    public LiquifyConfig setTurbulenceOctaves(int v) {
        turbulenceOctaves = v;
        return this;
    }

    public int getSafeMargin() {
        return safeMargin;
    }

    public LiquifyConfig setSafeMargin(int safeMargin) {
        this.safeMargin = safeMargin;
        return this;
    }

    public LiquifyConfig copy() {
        return LiquifyConfig.defaultConfig()
                .setTask(getTask())
                .setSource(getSource())
                .setStartPoint(getStartPoint())
                .setEndPoint(getEndPoint())
                .setLiquifyType(getLiquifyType())
                .setKernel(getKernel())
                .setSampler(getSampler())
                .setRadius(getRadius())
                .setStep(getStep())
                .setStrengthMax(getStrengthMax())
                .setStrengthThreshold(getStrengthThreshold())
                .setHardness(getHardness())
                .setExpandShrinkPower(getExpandShrinkPower())
                .setRotatePower(getRotatePower())
                .setTurbulencePower(getTurbulencePower())
                .setTurbulenceSeed(getTurbulenceSeed())
                .setTurbulenceFreq(getTurbulenceFreq())
                .setTurbulenceOctaves(getTurbulenceOctaves())
                .setSafeMargin(getSafeMargin());
    }

    public boolean isValid() {
        if (getSource() == null || getStartPoint() == null
                || getLiquifyType() == null || getKernel() == null || getSampler() == null) {
            return false;
        }
        if (getRadius() <= 0) {
            return false;
        }
        if (getLiquifyType() == LiquifyType.PUSH && getEndPoint() == null) {
            return false;
        }
        return true;
    }
}
